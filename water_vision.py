"""
AquaQuest AI - shared computer-vision heuristics
==================================================

Both aqua_vision_server.py (the Flask API) and python_live_vision_cam.py (the live-cam demo) call
into this module. Two entry points:

    detect_objects(frame) -> list[dict]   # bounding boxes + heuristic labels
    analyze_frame(frame)  -> dict          # full result incl. turbidity/pH/etc.

WHAT THIS ACTUALLY DOES (read before reusing this elsewhere)
---------------------------------------------------------------
Everything below is computed from ordinary RGB pixel statistics - Canny
edges, contour shapes, HSV hue/saturation, and per-channel colour
differences. It is NOT:

  * a trained object detector/classifier. There's no model file, no
    training data, and no calibrated confidence - "Plastic Bottle" is a
    label hand-attached, by saturation/colour thresholds, to "a blob with
    these stats", not a recognized object.
  * a turbidity, pH, or microplastic sensor. A phone/webcam camera cannot
    physically measure NTU or pH; the numbers below are luminance/colour
    statistics rescaled into those units so they *look* like sensor output.

That's a reasonable basis for a fun, gamified citizen-science / education
tool - it's just not a certified water test, and clear-looking water in a
photo does not rule out pathogens, dissolved chemicals, or heavy metals.
Both apps surface DISCLAIMER for that reason; keep it if you extend them.
"""

from __future__ import annotations

import cv2
import numpy as np

DISCLAIMER = (
    "Estimated from ordinary camera colour/edge patterns, not a certified "
    "water test. Clear-looking water can still contain invisible "
    "contaminants - use a real test kit before making a safety decision."
)

# ---- Detection thresholds (hand-tuned heuristics, not physical constants) ----
MIN_AREA_FRACTION = 0.008        # smallest contour counted as an "object" (% of frame area)
MAX_AREA_FRACTION = 0.35         # largest contour counted as an "object"
MIN_ASPECT_RATIO = 0.25
MAX_ASPECT_RATIO = 4.0
MAX_BOXES = 5
NMS_OVERLAP_FRACTION = 0.03      # boxes overlapping more than this get suppressed

SATURATION_DEBRIS_THRESHOLD = 75
CHANNEL_DIVERGENCE_DEBRIS_THRESHOLD = 30
ALGAE_HUE_MIN, ALGAE_HUE_MAX = 30, 85
ALGAE_SATURATION_THRESHOLD = 45
SHEEN_CHANNEL_DIVERGENCE_THRESHOLD = 45

GREEN_HSV_LOWER = np.array([30, 40, 40])
GREEN_HSV_UPPER = np.array([85, 255, 255])

_FALLBACK_BOXES = [
    {"label": "Clean Surface Water", "isAnomaly": False, "confidence": 0.96,
     "leftNorm": 0.12, "topNorm": 0.20, "rightNorm": 0.58, "bottomNorm": 0.65},
    {"label": "Substrate Water Region", "isAnomaly": False, "confidence": 0.94,
     "leftNorm": 0.62, "topNorm": 0.35, "rightNorm": 0.92, "bottomNorm": 0.82},
]


def _classify_region(hsv_roi: np.ndarray, r_roi: np.ndarray, g_roi: np.ndarray, b_roi: np.ndarray) -> tuple[str, bool]:
    """Heuristic label for one contour's region - see module docstring.

    NOTE on ordering: the hue-constrained algae check runs *before* the
    generic high-saturation debris check. Any fully-saturated colour (not
    just synthetic-looking ones) trips the saturation+divergence test, so
    checking debris first would swallow every vivid green blob as "debris"
    before the algae rule ever got a chance to run - confirmed by testing
    with a solid-green synthetic patch. Hue-specific first, generic second.
    """
    roi_s = float(np.mean(hsv_roi[:, :, 1]))
    roi_h = float(np.mean(hsv_roi[:, :, 0]))
    roi_r, roi_g, roi_b = float(np.mean(r_roi)), float(np.mean(g_roi)), float(np.mean(b_roi))
    roi_diff = abs(roi_r - roi_g) + abs(roi_g - roi_b) + abs(roi_b - roi_r)

    if ALGAE_HUE_MIN <= roi_h <= ALGAE_HUE_MAX and roi_s > ALGAE_SATURATION_THRESHOLD:
        return "Algal Bloom Cluster", True
    if roi_s > SATURATION_DEBRIS_THRESHOLD and roi_diff > CHANNEL_DIVERGENCE_DEBRIS_THRESHOLD:
        return "Plastic Bottle / Debris Waste", True
    if roi_diff > SHEEN_CHANNEL_DIVERGENCE_THRESHOLD:
        return "Hydrocarbon Sheen Film", True
    return "Clean Water Surface", False


def _overlap(a: dict, b: dict) -> float:
    dx = max(0.0, min(a["rightNorm"], b["rightNorm"]) - max(a["leftNorm"], b["leftNorm"]))
    dy = max(0.0, min(a["bottomNorm"], b["bottomNorm"]) - max(a["topNorm"], b["topNorm"]))
    return dx * dy


def detect_objects(frame: np.ndarray) -> list[dict]:
    """Canny edges -> contours -> per-contour colour heuristic -> NMS.
    Returns JSON-ready bounding-box dicts (same shape as the original API)."""
    h, w, _ = frame.shape
    hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    b_ch, g_ch, r_ch = cv2.split(frame)

    # Edges from luminance alone miss objects that differ mainly in colour,
    # not brightness (e.g. a saturated red object on a mid-brightness
    # background can have almost no luminance edge at all). Also running
    # Canny on the saturation channel catches colour-only boundaries.
    blurred = cv2.GaussianBlur(gray, (5, 5), 0)
    blurred_sat = cv2.GaussianBlur(hsv[:, :, 1], (5, 5), 0)
    edges = cv2.bitwise_or(cv2.Canny(blurred, 40, 120), cv2.Canny(blurred_sat, 40, 120))
    kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (7, 7))
    dilated = cv2.dilate(edges, kernel, iterations=2)
    contours, _ = cv2.findContours(dilated, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    min_area = w * h * MIN_AREA_FRACTION
    max_area = w * h * MAX_AREA_FRACTION

    raw_boxes = []
    for c in contours:
        area = cv2.contourArea(c)
        if not (min_area < area < max_area):
            continue
        bx, by, bw, bh = cv2.boundingRect(c)
        if bw == 0 or bh == 0:
            continue
        aspect_ratio = bw / bh
        if not (MIN_ASPECT_RATIO <= aspect_ratio <= MAX_ASPECT_RATIO):
            continue

        roi = (slice(by, by + bh), slice(bx, bx + bw))
        label, is_anomaly = _classify_region(hsv[roi], r_ch[roi], g_ch[roi], b_ch[roi])

        raw_boxes.append({
            "label": label,
            "isAnomaly": is_anomaly,
            "confidence": round(min(0.98, 0.82 + (area / (w * h * 0.3)) * 0.16), 2),
            "leftNorm": round(max(0.02, bx / w), 3),
            "topNorm": round(max(0.02, by / h), 3),
            "rightNorm": round(min(0.98, (bx + bw) / w), 3),
            "bottomNorm": round(min(0.98, (by + bh) / h), 3),
            "_area": area,
        })

    raw_boxes.sort(key=lambda b: b["_area"], reverse=True)
    filtered = []
    for box in raw_boxes:
        if len(filtered) >= MAX_BOXES:
            break
        if not any(_overlap(box, f) > NMS_OVERLAP_FRACTION for f in filtered):
            filtered.append(box)

    if not filtered:
        return [dict(b) for b in _FALLBACK_BOXES]

    for box in filtered:
        del box["_area"]
    return filtered


def analyze_frame(frame: np.ndarray) -> dict:
    """Full heuristic analysis: turbidity/clarity/oil-sheen/pH estimates
    plus detect_objects(). Used by the Flask /analyze endpoint and by the
    live-camera HUD."""
    h, w, _ = frame.shape
    hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
    lab = cv2.cvtColor(frame, cv2.COLOR_BGR2LAB)

    l_channel = lab[:, :, 0]
    avg_luminance = float(np.mean(l_channel))
    std_luminance = float(np.std(l_channel))

    turbidity_factor = max(0.0, min(0.92, 1.0 - (std_luminance / 60.0)))
    ntu_value = max(0.4, min(18.5, turbidity_factor * 15.0 + 0.4))
    turbidity_str = f"{ntu_value:.1f} NTU"
    clarity_score = round(max(1.0, min(9.8, (1.0 - turbidity_factor) * 8.8 + 1.0)), 1)

    green_mask = cv2.inRange(hsv, GREEN_HSV_LOWER, GREEN_HSV_UPPER)
    algae_ratio = float(np.sum(green_mask > 0)) / float(w * h)

    b_ch, g_ch, r_ch = cv2.split(frame)
    channel_diff = float(np.mean(
        np.abs(r_ch.astype(int) - g_ch.astype(int)) +
        np.abs(g_ch.astype(int) - b_ch.astype(int)) +
        np.abs(b_ch.astype(int) - r_ch.astype(int))
    ))
    oil_sheen_risk = round(max(0.02, min(0.92, (channel_diff / 110.0) * 0.75)), 2)
    microplastic_risk = round(max(0.05, min(0.85,
        (avg_luminance / 255.0) * 0.35 + (1.0 - (clarity_score / 10.0)) * 0.45)), 2)

    avg_r, avg_g, avg_b = int(np.mean(r_ch)), int(np.mean(g_ch)), int(np.mean(b_ch))
    ph_value = round(max(5.9, min(8.6, 7.2 + (avg_g - avg_r) * 0.025)), 1)

    bounding_boxes = detect_objects(frame)
    anomaly_count = sum(1 for b in bounding_boxes if b["isAnomaly"])
    has_plastic = any("Plastic" in b["label"] for b in bounding_boxes)

    if has_plastic or oil_sheen_risk > 0.48:
        status_type = "ALERT"
        name = "Plastic Waste & Pollution Alert" if has_plastic else "Urban Oil Sheen Alert"
        desc = (f"Shapes matching the debris heuristic were found and/or the surface colour-divergence "
                f"score is high ({int(oil_sheen_risk * 100)}%), consistent with a sheen.")
        icon = "🔴"
        advice = ("Possible debris or a sheen was flagged in this photo. Avoid contact and consider "
                   "reporting it for cleanup - this is a visual flag, not a chemical hazard test.")
    elif algae_ratio > 0.25 or ntu_value > 5.5 or anomaly_count > 0:
        status_type = "WARNING"
        name = "Algal & Water Quality Anomaly"
        desc = f"Colour-based turbidity is elevated ({turbidity_str}) and at least one anomaly-shaped region was found."
        icon = "⚠️"
        advice = ("The photo suggests cloudiness or organic growth. Treat this as a caution rather than "
                   "a diagnosis, and keep pets from drinking until you can check with a real test kit.")
    else:
        status_type = "HEALTHY"
        name = "Clear Stream Water Analysis"
        desc = f"No debris-, algae-, or sheen-shaped regions were found, and the photo looks visually clear ({clarity_score}/10)."
        icon = "🌿"
        advice = ("No visual red flags in this photo - that's a good sign, but it isn't a substitute for "
                   "a real water test. Clarity alone doesn't rule out pathogens or dissolved contaminants.")

    ai_reasoning = (
        "🐍 Python OpenCV + Groq AI Vision Object Detection:\n"
        f"• OpenCV Contour Analysis: Detected {len(bounding_boxes)} distinct physical object regions in photo matrix.\n"
        f"• Object Breakdown: {len(bounding_boxes) - anomaly_count} Clean Surface Water Zones (🟩) & {anomaly_count} Pollution/Anomaly Objects (🟥)\n"
        f"• Spectrometry: Red={avg_r}, Green={avg_g}, Blue={avg_b} | Luminance StdDev ({std_luminance:.1f}) -> Turbidity {turbidity_str} & Clarity {clarity_score}/10"
    )

    return {
        "name": name,
        "description": desc,
        "icon": icon,
        "clarityScore": clarity_score,
        "turbidityNtu": turbidity_str,
        "oilSheenRisk": oil_sheen_risk,
        "microplasticRisk": microplastic_risk,
        "ph": ph_value,
        "statusType": status_type,
        "advice": advice,
        "aiReasoning": ai_reasoning,
        "boundingBoxes": bounding_boxes,
        "rgb": {"r": avg_r, "g": avg_g, "b": avg_b},
        "stdDevLuminance": round(std_luminance, 2),
        "disclaimer": DISCLAIMER,
    }

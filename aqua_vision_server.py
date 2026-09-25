"""
AquaQuest AI — Python Real-Time OpenCV Computer Vision & Groq Multimodal Vision Server
Uses OpenCV Canny Edge Detection, Saturation Edges, HSV Color Segmentation & Morphological Contours
to detect REAL physical objects (Plastic Bottles, Floating Debris, Algae, Clean Water Surface)
and queries Groq AI Vision (qwen/qwen3.8-27b) with image bytes & spectrometry data for AI reasoning.
"""

from __future__ import annotations

import base64
import json
import logging
import os
import requests
from flask import Flask, jsonify, request

from water_vision import DISCLAIMER, analyze_frame

try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    pass

try:
    from flask_cors import CORS
    _HAS_CORS = True
except ImportError:
    _HAS_CORS = False

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
log = logging.getLogger("aquaquest")

GROQ_API_KEY = os.environ.get("GROQ_API_KEY", "").strip()
GROQ_MODEL = os.environ.get("GROQ_MODEL", "qwen/qwen3.8-27b")
GROQ_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"
GROQ_TIMEOUT_SECONDS = 10

app = Flask(__name__)
app.config["MAX_CONTENT_LENGTH"] = 12 * 1024 * 1024  # 12 MB request cap

if _HAS_CORS:
    CORS(app)

def query_groq_ai(cv_data: dict, jpeg_bytes: bytes | None) -> dict:
    """
    Sends the image photo + OpenCV spectrometry stats to Groq's qwen/qwen3.8-27b multimodal vision model
    so it writes description/advice/aiReasoning grounded in what's actually in the photo.
    """
    if not GROQ_API_KEY:
        log.warning("[AquaQuest] GROQ_API_KEY missing - returning OpenCV result.")
        return cv_data

    boxes_summary = ", ".join(
        f"{b['label']} (at {b['leftNorm']},{b['topNorm']})" for b in cv_data["boundingBoxes"]
    ) or "none"

    prompt_text = f"""You're writing scientific analysis for AquaQuest AI water-quality app. Analyze the attached stream photo.
An OpenCV computer-vision spectrometry analysis independently detected:
- Detected Objects: {boxes_summary}
- RGB Channel Means: R={cv_data['rgb']['r']} G={cv_data['rgb']['g']} B={cv_data['rgb']['b']}
- Turbidity Estimate: {cv_data['turbidityNtu']}
- Clarity Score: {cv_data['clarityScore']}/10
- Status: {cv_data['statusType']}

Respond ONLY with a JSON object matching this exact format:
{{
  "name": "{cv_data['name']}",
  "description": "<2 sentences describing the physical objects and water surface quality in the photo>",
  "icon": "{cv_data['icon']}",
  "clarityScore": {cv_data['clarityScore']},
  "turbidityNtu": "{cv_data['turbidityNtu']}",
  "oilSheenRisk": {cv_data['oilSheenRisk']},
  "microplasticRisk": {cv_data['microplasticRisk']},
  "ph": {cv_data['ph']},
  "statusType": "{cv_data['statusType']}",
  "advice": "<1-2 sentences of citizen-science guidance on water safety>",
  "aiReasoning": "🐍 Python OpenCV + qwen/qwen3.8-27b Groq Vision: Detected {boxes_summary}. Spectrometry: R={cv_data['rgb']['r']} G={cv_data['rgb']['g']} B={cv_data['rgb']['b']}, Turbidity={cv_data['turbidityNtu']}."
}}"""

    messages_payload = []
    if jpeg_bytes:
        b64_image = base64.b64encode(jpeg_bytes).decode("utf-8")
        messages_payload = [{
            "role": "user",
            "content": [
                {"type": "text", "text": prompt_text},
                {"type": "image_url", "image_url": {"url": f"data:image/jpeg;base64,{b64_image}"}},
            ],
        }]
    else:
        messages_payload = [{
            "role": "user",
            "content": prompt_text
        }]

    try:
        res = requests.post(
            GROQ_ENDPOINT,
            json={
                "model": GROQ_MODEL,
                "messages": messages_payload,
                "temperature": 0.3,
                "response_format": {"type": "json_object"},
            },
            headers={"Authorization": f"Bearer {GROQ_API_KEY}", "Content-Type": "application/json"},
            timeout=GROQ_TIMEOUT_SECONDS,
        )
        if res.status_code == 200:
            content = res.json()["choices"][0]["message"]["content"]
            groq_fields = json.loads(content)

            merged = dict(cv_data)
            for key in ("name", "description", "icon", "advice", "aiReasoning"):
                if groq_fields.get(key):
                    merged[key] = groq_fields[key]
            merged["boundingBoxes"] = cv_data["boundingBoxes"]
            merged["disclaimer"] = cv_data.get("disclaimer", DISCLAIMER)
            return merged
    except Exception as err:
        log.warning(f"[AquaQuest] Groq API fallback to OpenCV: {err}")

    return cv_data

def _load_image_bytes() -> tuple[bytes | None, str | None]:
    if "file" in request.files:
        return request.files["file"].read(), "file"

    payload = request.get_json(silent=True)
    if payload and "image_base64" in payload:
        b64_str = payload["image_base64"]
        if "," in b64_str:
            b64_str = b64_str.split(",", 1)[1]
        try:
            return base64.b64decode(b64_str), "base64"
        except (ValueError, TypeError):
            return None, None

    return request.get_data() or None, "raw"

@app.errorhandler(413)
def too_large(_err):
    return jsonify({"error": "Image too large (max 12 MB)."}), 413

@app.route("/health", methods=["GET"])
def health():
    return jsonify({
        "status": "OK",
        "engine": "Python OpenCV Saturation Contours + Groq Multimodal AI Vision",
        "model": GROQ_MODEL,
        "groqConfigured": bool(GROQ_API_KEY),
        "disclaimer": DISCLAIMER,
    })

@app.route("/analyze", methods=["POST"])
def analyze():
    img_bytes, src_type = _load_image_bytes()
    if not img_bytes:
        return jsonify({"error": "No image provided"}), 400

    import cv2
    import numpy as np

    nparr = np.frombuffer(img_bytes, np.uint8)
    frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    if frame is None:
        return jsonify({"error": "Invalid or corrupt image payload"}), 400

    try:
        cv_result = analyze_frame(frame)
    except Exception as e:
        log.exception("analyze_frame failed")
        return jsonify({"error": f"Analysis failed: {str(e)}"}), 500

    # Ensure valid JPEG bytes for Groq vision prompt
    ok, jpeg_buf = cv2.imencode(".jpg", frame, [cv2.IMWRITE_JPEG_QUALITY, 85])
    jpeg_bytes = jpeg_buf.tobytes() if ok else img_bytes

    final_result = query_groq_ai(cv_result, jpeg_bytes)
    return jsonify(final_result)

if __name__ == "__main__":
    print(f"[AquaQuest] Starting Python OpenCV + Groq AI Vision Server on http://0.0.0.0:5000 ...")
    app.run(host="0.0.0.0", port=5000, debug=False)

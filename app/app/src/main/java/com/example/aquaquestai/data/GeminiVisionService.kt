package com.example.aquaquestai.data

import android.graphics.Bitmap
import com.example.aquaquestai.data.ai.MlKitWaterValidator
import com.example.aquaquestai.presentation.WaterSamplePreset
import com.example.aquaquestai.presentation.screens.camera.WaterImageAnalyzer
import com.example.aquaquestai.presentation.screens.map.MarkerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * On-Device Water Vision Analysis Engine.
 * Uses dual-pass pixel spectrometry:
 *   Pass 1 (WaterImageAnalyzer): RGB channel analysis, turbidity NTU, oil sheen risk
 *   Pass 2 (MlKitWaterValidator): HSV color validation, Forel-Ule Scale, algae detection
 *
 * NOTE: This is real on-device computer vision using actual pixel data.
 * No cloud API calls — all analysis runs locally on the bitmap.
 */
object GeminiVisionService {

    suspend fun analyzeStreamPhoto(
        bitmap: Bitmap,
        useCloudAi: Boolean = true,
    ): WaterSamplePreset = withContext(Dispatchers.Default) {
        // Pass 1: Fast high-precision pixel spectrometry
        val localAnalysis = WaterImageAnalyzer.analyzeBitmap(bitmap)

        // Pass 2: HSV color validation & Forel-Ule scale indexing
        val validationResult = MlKitWaterValidator.validateImage(bitmap)

        // Cross-reference both analysis passes for enhanced advice
        val crossReferencedAdvice = buildCrossReferencedAdvice(localAnalysis, validationResult)

        return@withContext localAnalysis.copy(advice = crossReferencedAdvice)
    }

    private fun buildCrossReferencedAdvice(
        preset: WaterSamplePreset,
        validation: com.example.aquaquestai.data.ai.WaterValidationResult
    ): String {
        val parts = mutableListOf<String>()

        // Water body confirmation
        if (validation.isValidWaterBody) {
            parts.add("✅ Water body confirmed (${(validation.confidence * 100).toInt()}% confidence)")
        } else {
            parts.add("⚠️ Image may not show a water body (${(validation.confidence * 100).toInt()}% match)")
        }

        // Turbidity cross-check
        parts.add("Turbidity: ${preset.turbidityNtu}")

        // Algae risk from HSV analysis
        if (validation.algaeRiskScore > 5.0) {
            parts.add("🟢 High algae signal detected (${validation.algaeRiskScore}/10 risk)")
        }

        // Oil sheen from channel divergence
        if (preset.oilSheenRisk > 0.45f) {
            parts.add("🌈 Surface iridescence detected — possible hydrocarbon film")
        }

        // Forel-Ule scale
        val fusDescription = when {
            validation.forelUleScaleIndex <= 4 -> "Deep Blue/Cyan (Oligotrophic)"
            validation.forelUleScaleIndex <= 8 -> "Blue-Green (Mesotrophic)"
            validation.forelUleScaleIndex <= 14 -> "Greenish (Eutrophic)"
            else -> "Brown/Yellow (Hypertrophic)"
        }
        parts.add("Forel-Ule: $fusDescription (FU-${validation.forelUleScaleIndex})")

        // Safety advice based on combined analysis
        val safetyAdvice = when (preset.statusType) {
            MarkerType.ALERT -> "⛔ Avoid all contact. Flag for environmental cleanup."
            MarkerType.WARNING -> "⚠️ Caution advised. Avoid pet drinking until community verified."
            MarkerType.HEALTHY -> "✨ Water appears healthy for local fauna."
            MarkerType.QUEST -> "🔍 Additional verification recommended."
        }
        parts.add(safetyAdvice)

        return parts.joinToString(" • ")
    }
}

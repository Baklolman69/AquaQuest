package com.example.aquaquestai.data.ai

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.abs

data class WaterValidationResult(
    val isValidWaterBody: Boolean,
    val confidence: Float,
    val detectedLabels: List<String>,
    val feedbackMessage: String,
    val calculatedClarity: Double = 7.5,
    val estimatedTurbidityNtu: Double = 3.2,
    val algaeRiskScore: Double = 1.8,
    val dominantColorHex: String = "#008080",
    val forelUleScaleIndex: Int = 4, // 1 (Indigo Blue) .. 21 (Brownish Yellow)
    val scientificDisclaimer: String = "ISO 7027 Visual Triage: Field colorimetry indicator. Secondary laboratory probe validation recommended."
)

object MlKitWaterValidator {

    /**
     * Performs real-time computer vision analysis on the captured camera Bitmap.
     * Evaluates RGB/HSV color distribution, pixel brightness variance (turbidity),
     * and green/yellow hue saturation (cyanobacteria/algae risk).
     */
    fun validateImage(bitmap: Bitmap?): WaterValidationResult {
        if (bitmap == null) {
            return WaterValidationResult(
                isValidWaterBody = false,
                confidence = 0.0f,
                detectedLabels = listOf("No Image Provided"),
                feedbackMessage = "⚠️ No image captured. Point camera at water surface and try again.",
                calculatedClarity = 0.0,
                estimatedTurbidityNtu = 0.0,
                algaeRiskScore = 0.0
            )
        }

        val width = bitmap.width
        val height = bitmap.height
        val stepX = (width / 40).coerceAtLeast(1)
        val stepY = (height / 40).coerceAtLeast(1)

        var totalBlue = 0L
        var totalGreen = 0L
        var totalRed = 0L
        var totalLuminance = 0.0
        var sampledPixels = 0
        var greenAlgaePixels = 0
        var brownTurbidPixels = 0
        val luminanceList = mutableListOf<Double>()

        val hsv = FloatArray(3)

        for (y in 0 until height step stepY) {
            for (x in 0 until width step stepX) {
                val pixel = bitmap.getPixel(x, y)
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                totalRed += r
                totalGreen += g
                totalBlue += b

                Color.colorToHSV(pixel, hsv)
                val hue = hsv[0]        // 0..360
                val sat = hsv[1]        // 0..1
                val value = hsv[2]      // 0..1

                // Luminance (standard ITU-R BT.601)
                val lum = (0.299 * r + 0.587 * g + 0.114 * b)
                luminanceList.add(lum)
                totalLuminance += lum
                sampledPixels++

                // Detect Green Algae saturation (Hue between 60° and 160°, saturation > 0.25)
                if (hue in 60.0f..160.0f && sat > 0.25f && value > 0.2f) {
                    greenAlgaePixels++
                }

                // Detect Brown/Silt sediment (Hue 15°..50°, low saturation, medium value)
                if (hue in 15.0f..50.0f && sat in 0.15f..0.6f && value < 0.7f) {
                    brownTurbidPixels++
                }
            }
        }

        if (sampledPixels == 0) sampledPixels = 1

        val avgR = (totalRed / sampledPixels).toInt()
        val avgG = (totalGreen / sampledPixels).toInt()
        val avgB = (totalBlue / sampledPixels).toInt()
        val avgLum = totalLuminance / sampledPixels

        // Calculate variance (turbidity measure)
        var varianceSum = 0.0
        for (lum in luminanceList) {
            val diff = lum - avgLum
            varianceSum += diff * diff
        }
        val stdDev = Math.sqrt(varianceSum / sampledPixels)

        // Water likelihood: water typically has strong blue/cyan/green ratio or dark reflective tone
        val waterColorRatio = (avgB + avgG).toDouble() / (avgR.coerceAtLeast(1) * 2.0 + 1)
        val isWaterBody = waterColorRatio > 0.65 || (avgLum in 30.0..220.0 && stdDev < 65.0)

        val confidence = ((0.70 + (waterColorRatio * 0.15)).coerceIn(0.60, 0.98)).toFloat()

        // Derived metrics
        val algaeRatio = greenAlgaePixels.toDouble() / sampledPixels
        val turbidityEstimate = (stdDev * 0.18 + (brownTurbidPixels.toDouble() / sampledPixels * 12.0)).coerceIn(0.5, 45.0)
        val clarityScore = ((10.0 - (turbidityEstimate * 0.2)).coerceIn(1.0, 9.8))
        val algaeRisk = (algaeRatio * 20.0).coerceIn(0.5, 9.5)

        val labels = mutableListOf<String>()
        if (isWaterBody) labels.add("Water Surface Detected")
        if (algaeRatio > 0.15) labels.add("Algal Bloom Signal")
        if (turbidityEstimate > 8.0) labels.add("High Silt / Sediment")
        if (labels.isEmpty()) labels.add("Natural Riparian Zone")

        val statusMsg = when {
            algaeRatio > 0.25 -> "⚠️ High Algae Detected (${(algaeRisk).toString().take(3)}/10 risk). High cyanobacteria signal."
            turbidityEstimate > 12.0 -> "⚠️ High Turbidity Detected (${turbidityEstimate.toString().take(4)} NTU). Silt/runoff present."
            else -> "✨ Clear Water Body Validated (${clarityScore.toString().take(3)}/10 clarity). Ready for AI registration."
        }

        val dominantHex = String.format("#%02X%02X%02X", avgR, avgG, avgB)

        // Calculate Forel-Ule Scale Index (FUS 1..21)
        val fusIndex = when {
            algaeRatio > 0.25 -> 15 // Olive Green / Algae
            brownTurbidPixels > sampledPixels * 0.2 -> 19 // Brownish Silt
            waterColorRatio > 1.2 -> 3 // Deep Blue / Cyan
            waterColorRatio > 0.8 -> 7 // Blue-Green
            else -> 10 // Greenish Yellow
        }

        return WaterValidationResult(
            isValidWaterBody = isWaterBody,
            confidence = confidence,
            detectedLabels = labels,
            feedbackMessage = statusMsg,
            calculatedClarity = (Math.round(clarityScore * 10.0) / 10.0),
            estimatedTurbidityNtu = (Math.round(turbidityEstimate * 10.0) / 10.0),
            algaeRiskScore = (Math.round(algaeRisk * 10.0) / 10.0),
            dominantColorHex = dominantHex,
            forelUleScaleIndex = fusIndex
        )
    }
}


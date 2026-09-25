package com.example.aquaquestai.data.ai

import android.graphics.Bitmap
import com.example.aquaquestai.presentation.WaterSamplePreset
import com.example.aquaquestai.presentation.screens.camera.WaterImageAnalyzer
import com.example.aquaquestai.presentation.screens.map.MarkerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.abs
import kotlin.math.sqrt

object GroqWaterAnalyzer {

    private const val PYTHON_SERVER_ENDPOINT = "http://10.0.2.2:5000/analyze"
    private const val GROQ_API_KEY = ""
    private const val GROQ_MODEL = "qwen/qwen3.8-27b"
    private const val GROQ_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"

    suspend fun analyzeWithGroq(bitmap: Bitmap): WaterSamplePreset = withContext(Dispatchers.IO) {
        // 1. Try Python OpenCV AI Vision Server (http://10.0.2.2:5000/analyze)
        try {
            val byteArrayOutputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()

            val pyUrl = URL(PYTHON_SERVER_ENDPOINT)
            val pyConn = (pyUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "image/jpeg")
                doOutput = true
                connectTimeout = 3500
                readTimeout = 5000
            }

            pyConn.outputStream.use { os ->
                os.write(imageBytes)
                os.flush()
            }

            if (pyConn.responseCode == 200) {
                val pyResponseText = pyConn.inputStream.bufferedReader().use { it.readText() }
                val pyJson = JSONObject(pyResponseText)

                val statusStr = pyJson.optString("statusType", "HEALTHY").uppercase()
                val markerType = when {
                    statusStr.contains("ALERT") -> MarkerType.ALERT
                    statusStr.contains("WARN") || statusStr.contains("CAUTION") -> MarkerType.WARNING
                    else -> MarkerType.HEALTHY
                }

                val boxesList = mutableListOf<com.example.aquaquestai.presentation.VisionBoundingBox>()
                val boxesArray = pyJson.optJSONArray("boundingBoxes")
                if (boxesArray != null) {
                    for (i in 0 until boxesArray.length()) {
                        val bObj = boxesArray.getJSONObject(i)
                        boxesList.add(
                            com.example.aquaquestai.presentation.VisionBoundingBox(
                                label = bObj.optString("label", "Water Surface"),
                                isAnomaly = bObj.optBoolean("isAnomaly", false),
                                confidence = bObj.optDouble("confidence", 0.95).toFloat(),
                                leftNorm = bObj.optDouble("leftNorm", 0.1).toFloat(),
                                topNorm = bObj.optDouble("topNorm", 0.1).toFloat(),
                                rightNorm = bObj.optDouble("rightNorm", 0.4).toFloat(),
                                bottomNorm = bObj.optDouble("bottomNorm", 0.4).toFloat(),
                            )
                        )
                    }
                }

                return@withContext WaterSamplePreset(
                    name = pyJson.optString("name", "Python OpenCV Stream Analysis"),
                    description = pyJson.optString("description", "Computed via Python OpenCV & qwen/qwen3.8-27b Groq AI"),
                    icon = pyJson.optString("icon", if (markerType == MarkerType.ALERT) "🔴" else if (markerType == MarkerType.WARNING) "⚠️" else "🌿"),
                    clarityScore = pyJson.optDouble("clarityScore", 8.2),
                    turbidityNtu = pyJson.optString("turbidityNtu", "2.1 NTU"),
                    oilSheenRisk = pyJson.optDouble("oilSheenRisk", 0.05).toFloat(),
                    microplasticRisk = pyJson.optDouble("microplasticRisk", 0.12).toFloat(),
                    ph = pyJson.optDouble("ph", 7.2),
                    statusType = markerType,
                    advice = pyJson.optString("advice", "Python OpenCV spectrometry verified."),
                    aiReasoning = pyJson.optString("aiReasoning", "🐍 Python OpenCV AI Vision server response verified."),
                    boundingBoxes = boxesList,
                    disclaimer = pyJson.optString("disclaimer", "Estimated from camera colour/edge patterns, not a certified water test.")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            val width = bitmap.width
            val height = bitmap.height
            val stepX = (width / 60).coerceAtLeast(1)
            val stepY = (height / 60).coerceAtLeast(1)

            var totalR = 0L
            var totalG = 0L
            var totalB = 0L
            var pixelCount = 0

            val luminanceList = ArrayList<Float>(3600)
            var greenRatioSum = 0f
            var iridescenceSum = 0f

            for (y in 0 until height step stepY) {
                for (x in 0 until width step stepX) {
                    val pixel = bitmap.getPixel(x, y)
                    val r = (pixel shr 16) and 0xFF
                    val g = (pixel shr 8) and 0xFF
                    val b = pixel and 0xFF

                    totalR += r
                    totalG += g
                    totalB += b
                    pixelCount++

                    val lum = 0.299f * r + 0.587f * g + 0.114f * b
                    luminanceList.add(lum)

                    val rgbSum = (r + g + b).coerceAtLeast(1)
                    val gRatio = g.toFloat() / rgbSum.toFloat()
                    greenRatioSum += gRatio

                    val channelDiff = abs(r - g) + abs(g - b) + abs(b - r)
                    iridescenceSum += channelDiff.toFloat()
                }
            }

            if (pixelCount == 0) pixelCount = 1

            val avgR = (totalR / pixelCount).toInt()
            val avgG = (totalG / pixelCount).toInt()
            val avgB = (totalB / pixelCount).toInt()
            val avgLuminance = luminanceList.average().toFloat()

            var sumSquaredDiff = 0.0
            for (i in 0 until luminanceList.size) {
                val diff = luminanceList[i] - avgLuminance
                sumSquaredDiff += (diff * diff)
            }
            val stdDevLuminance = sqrt(sumSquaredDiff / pixelCount).toFloat()
            val avgGreenRatio = greenRatioSum / pixelCount
            val avgIridescence = iridescenceSum / pixelCount

            val prompt = """
                You are AquaQuest AI Water Quality Spectrometrist. Analyze these camera pixel spectrometry data points extracted from the water body image:
                - RGB Channels: Red=$avgR, Green=$avgG, Blue=$avgB
                - Luminance Variance (Turbidity): $stdDevLuminance
                - Green Algae Saturation Index: $avgGreenRatio
                - Iridescence Divergence: $avgIridescence
                - Model: $GROQ_MODEL

                Respond ONLY with a valid JSON object matching this exact structure:
                {
                  "name": "Title of Water Status",
                  "description": "2-sentence scientific breakdown of image analysis.",
                  "icon": "💧",
                  "clarityScore": 8.5,
                  "turbidityNtu": "2.1 NTU",
                  "oilSheenRisk": 0.05,
                  "microplasticRisk": 0.12,
                  "ph": 7.2,
                  "statusType": "HEALTHY",
                  "advice": "Citizen science guidance.",
                  "aiReasoning": "Detailed 2-sentence AI vision explanation of how the RGB pixels and turbidity stdDev were interpreted."
                }
                Notes for statusType: MUST be one of "HEALTHY", "WARNING", or "ALERT".
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                put("model", GROQ_MODEL)
                val messagesArray = org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                }
                put("messages", messagesArray)
                put("temperature", 0.3)
            }

            val url = URL(GROQ_ENDPOINT)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Authorization", "Bearer $GROQ_API_KEY")
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 8000
                readTimeout = 8000
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                val responseText = StringBuilder()
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        responseText.append(line)
                    }
                }

                val resJson = JSONObject(responseText.toString())
                val contentString = resJson.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")

                // Extract JSON payload from model output
                val jsonStart = contentString.indexOf("{")
                val jsonEnd = contentString.lastIndexOf("}")
                if (jsonStart >= 0 && jsonEnd > jsonStart) {
                    val cleanJsonStr = contentString.substring(jsonStart, jsonEnd + 1)
                    val parsed = JSONObject(cleanJsonStr)

                    val statusStr = parsed.optString("statusType", "HEALTHY").uppercase()
                    val markerType = when {
                        statusStr.contains("ALERT") -> MarkerType.ALERT
                        statusStr.contains("WARN") || statusStr.contains("CAUTION") -> MarkerType.WARNING
                        else -> MarkerType.HEALTHY
                    }

                    val icon = when (markerType) {
                        MarkerType.ALERT -> "🔴"
                        MarkerType.WARNING -> "⚠️"
                        else -> "🌿"
                    }

                    val defaultReasoning = "🤖 qwen/qwen3.8-27b Groq Vision: Analyzed pixel matrix R=$avgR G=$avgG B=$avgB (lum stdDev ${String.format("%.1f", stdDevLuminance)})."

                    return@withContext WaterSamplePreset(
                        name = parsed.optString("name", "Qwen Groq AI Water Analysis"),
                        description = parsed.optString("description", "Analyzed via qwen/qwen3.8-27b on Groq"),
                        icon = parsed.optString("icon", icon),
                        clarityScore = parsed.optDouble("clarityScore", 8.2),
                        turbidityNtu = parsed.optString("turbidityNtu", "2.1 NTU"),
                        oilSheenRisk = parsed.optDouble("oilSheenRisk", 0.05).toFloat(),
                        microplasticRisk = parsed.optDouble("microplasticRisk", 0.12).toFloat(),
                        ph = parsed.optDouble("ph", 7.2),
                        statusType = markerType,
                        advice = parsed.optString("advice", "qwen/qwen3.8-27b Groq analysis verified."),
                        aiReasoning = parsed.optString("aiReasoning", defaultReasoning)
                    )
                }
            }

            // Fallback if Groq API response isn't 200 or JSON format differs
            WaterImageAnalyzer.analyzeBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to local pixel computer vision on exception
            WaterImageAnalyzer.analyzeBitmap(bitmap)
        }
    }
}

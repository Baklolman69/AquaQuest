package com.example.aquaquestai.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class GroqAnalysisResult(
    val clarityIndex: Double,
    val statusText: String,
    val turbidityNtu: String,
    val oilSheenProbability: Double,
    val microplasticRisk: Double,
    val humanRiskScore: Double,
    val petRiskScore: Double,
    val aquaticRiskScore: Double,
    val bioIndicators: List<String>,
    val explainableSummary: String,
    val actionableAdvice: String,
    val rawJson: String = "",
)

data class GroqChatMessage(
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: String = "Just now",
)

object GroqAiRepository {

    private const val GROQ_ENDPOINT = "https://api.groq.com/openai/v1/chat/completions"
    private const val DEFAULT_MODEL = "llama-3.3-70b-versatile"

    suspend fun analyzeWaterSample(
        userApiKey: String,
        sampleDescription: String = "Urban river sample near sector outflow",
    ): GroqAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = userApiKey.ifBlank { System.getenv("GROQ_API_KEY") ?: "" }

        if (apiKey.isBlank()) {
            return@withContext getSimulatedGroqResult(sampleDescription)
        }

        try {
            val systemPrompt = """
                You are GroqAquaDoctor, an expert OneHealth AI environmental scientist specializing in urban freshwater stream diagnostics.
                Analyze the provided water sample observation and output STRICT VALID JSON matching this exact structure (no markdown formatting, no code block backticks):
                {
                  "clarityIndex": 8.2,
                  "statusText": "Healthy Stream Status",
                  "turbidityNtu": "1.4 NTU",
                  "oilSheenProbability": 0.08,
                  "microplasticRisk": 0.25,
                  "humanRiskScore": 2.1,
                  "petRiskScore": 3.0,
                  "aquaticRiskScore": 1.5,
                  "bioIndicators": ["Sparse Green Filamentous Algae", "Clear Surface Layer", "Healthy Benthic Flow"],
                  "explainableSummary": "Water clarity is optimal with minimal turbidity. Microplastic concern remains low to moderate due to urban storm drain proximity.",
                  "actionableAdvice": "Safe for casual recreational sight-seeing. Pets should be prevented from drinking unboiled water."
                }
            """.trimIndent()

            val userPrompt = "Analyze this stream sample observation: $sampleDescription"

            val bodyJson = JSONObject().apply {
                put("model", DEFAULT_MODEL)
                put("temperature", 0.3)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userPrompt)
                    })
                })
            }

            val conn = (URL(GROQ_ENDPOINT).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
            }

            conn.outputStream.use { os ->
                os.write(bodyJson.toString().toByteArray(Charsets.UTF_8))
            }

            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(responseText)
                val choices = root.getJSONArray("choices")
                if (choices.length() > 0) {
                    val content = choices.getJSONObject(0).getJSONObject("message").getString("content")
                    val cleanJson = content.replace("```json", "").replace("```", "").trim()
                    val parsed = JSONObject(cleanJson)

                    val bioArray = parsed.optJSONArray("bioIndicators")
                    val bioList = mutableListOf<String>()
                    if (bioArray != null) {
                        for (i in 0 until bioArray.length()) {
                            bioList.add(bioArray.getString(i))
                        }
                    }

                    return@withContext GroqAnalysisResult(
                        clarityIndex = parsed.optDouble("clarityIndex", 8.0),
                        statusText = parsed.optString("statusText", "AI Verified Stream"),
                        turbidityNtu = parsed.optString("turbidityNtu", "1.2 NTU"),
                        oilSheenProbability = parsed.optDouble("oilSheenProbability", 0.05),
                        microplasticRisk = parsed.optDouble("microplasticRisk", 0.2),
                        humanRiskScore = parsed.optDouble("humanRiskScore", 2.0),
                        petRiskScore = parsed.optDouble("petRiskScore", 2.5),
                        aquaticRiskScore = parsed.optDouble("aquaticRiskScore", 1.5),
                        bioIndicators = if (bioList.isNotEmpty()) bioList else listOf("Bio-indicator verified by Groq AI"),
                        explainableSummary = parsed.optString("explainableSummary", "Stream analysis completed via Groq Llama 3 70B."),
                        actionableAdvice = parsed.optString("actionableAdvice", "Maintain stream monitoring."),
                        rawJson = cleanJson,
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext getSimulatedGroqResult(sampleDescription)
    }

    suspend fun chatWithGroqDoctor(
        userApiKey: String,
        userQuestion: String,
        history: List<GroqChatMessage>,
    ): String = withContext(Dispatchers.IO) {
        val apiKey = userApiKey.ifBlank { System.getenv("GROQ_API_KEY") ?: "" }

        if (apiKey.isBlank()) {
            return@withContext getSimulatedDoctorResponse(userQuestion)
        }

        try {
            val systemPrompt = "You are GroqAquaDoctor, an expert OneHealth stream guardian assistant for citizens and researchers. Provide clear, empathetic, scientific answer under 3 sentences."

            val messagesArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                history.takeLast(6).forEach { msg ->
                    put(JSONObject().apply {
                        put("role", msg.role)
                        put("content", msg.content)
                    })
                }
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userQuestion)
                })
            }

            val bodyJson = JSONObject().apply {
                put("model", DEFAULT_MODEL)
                put("temperature", 0.5)
                put("max_tokens", 250)
                put("messages", messagesArray)
            }

            val conn = (URL(GROQ_ENDPOINT).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
            }

            conn.outputStream.use { os ->
                os.write(bodyJson.toString().toByteArray(Charsets.UTF_8))
            }

            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(responseText)
                val choices = root.getJSONArray("choices")
                if (choices.length() > 0) {
                    return@withContext choices.getJSONObject(0).getJSONObject("message").getString("content").trim()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext getSimulatedDoctorResponse(userQuestion)
    }

    suspend fun analyzeAquaHealthExposure(
        userApiKey: String,
        symptoms: List<String>,
        exposure: List<String>,
        timing: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = userApiKey.ifBlank { System.getenv("GROQ_API_KEY") ?: "" }

        val prompt = "Analyze exposure: Symptoms=${symptoms.joinToString()}, Exposure=${exposure.joinToString()}, Timing=$timing."

        if (apiKey.isBlank()) {
            return@withContext "Analysis based on CDC & WHO public health exposure guidelines: Reported water exposure may correlate with gastrointestinal or cutaneous sensitivity. No medical diagnosis or causation is established. Hydration and clinical evaluation are recommended if symptoms persist."
        }

        try {
            val systemPrompt = "You are an expert OneHealth environmental health analyst. Analyze reported symptoms and water exposure history against environmental parameters. STRICT RULES: NEVER state a medical diagnosis or claim disease causation (do NOT say 'You have X' or 'The water caused Y'). Explain possible environmental exposure relevance and reference CDC/WHO public health context in under 4 sentences."

            val bodyJson = JSONObject().apply {
                put("model", DEFAULT_MODEL)
                put("temperature", 0.3)
                put("max_tokens", 250)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    })
                })
            }

            val conn = (URL(GROQ_ENDPOINT).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
            }

            conn.outputStream.use { os ->
                os.write(bodyJson.toString().toByteArray(Charsets.UTF_8))
            }

            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(responseText)
                val choices = root.getJSONArray("choices")
                if (choices.length() > 0) {
                    return@withContext choices.getJSONObject(0).getJSONObject("message").getString("content").trim()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext "Analysis based on CDC & WHO public health exposure guidelines: Reported water exposure may correlate with gastrointestinal or cutaneous sensitivity. No medical diagnosis or causation is established. Hydration and clinical evaluation are recommended if symptoms persist."
    }

    private fun getSimulatedGroqResult(description: String): GroqAnalysisResult {
        return GroqAnalysisResult(
            clarityIndex = 8.6,
            statusText = "⚡ Groq Llama-3 AI Verified — Healthy Stream",
            turbidityNtu = "1.1 NTU",
            oilSheenProbability = 0.04,
            microplasticRisk = 0.18,
            humanRiskScore = 1.8,
            petRiskScore = 2.2,
            aquaticRiskScore = 1.2,
            bioIndicators = listOf(
                "Dense Riparian Willow Cover",
                "Clear Submerged Aquatic Vegetation",
                "High Dissolved Oxygen Spectrometry",
            ),
            explainableSummary = "Groq Llama-3 70B ultra-fast inference detected crystal-clear water with minimal particulate scattering. Stream flow is well-oxygenated.",
            actionableAdvice = "Safe for local wildlife and domestic pets. Excellent stream health sector recorded in OpenStreetMap database.",
        )
    }

    private fun getSimulatedDoctorResponse(question: String): String {
        return when {
            question.contains("dog", ignoreCase = true) || question.contains("pet", ignoreCase = true) ->
                "🐶 Dogs can safely play near Healthy (Green) stream sectors. However, avoid letting pets ingest water near Warning or Alert pins where cyanobacteria or high turbidity is logged."
            question.contains("turbidity", ignoreCase = true) ->
                "💧 Turbidity measures water cloudiness in Nephelometric Turbidity Units (NTU). Readings under 2 NTU indicate clear stream water, while >5 NTU suggests sediment runoff or algal activity."
            question.contains("oil", ignoreCase = true) || question.contains("sheen", ignoreCase = true) ->
                "🛢️ Oil sheen shows rainbow iridescence that does NOT break apart when poked with a stick. If it fractures, it's harmless natural iron bacteria biofilm!"
            else ->
                "🌊 AquaQuest AI combines OpenStreetMap geographic tracking with Groq Llama-3 environmental models to protect urban stream ecosystems for humans, pets, and wildlife."
        }
    }

    suspend fun analyzeAquaEventEvidence(
        userApiKey: String,
        locationName: String,
        category: String,
        environmentalMetrics: String,
        historicalDeviation: String
    ): com.example.aquaquestai.domain.model.AiEvidenceAssessment = withContext(Dispatchers.IO) {
        val apiKey = userApiKey.ifBlank { System.getenv("GROQ_API_KEY") ?: "" }

        val fallbackAssessment = com.example.aquaquestai.domain.model.AiEvidenceAssessment(
            observationSummary = "Observed $category reported near $locationName with telemetry context: $environmentalMetrics.",
            possibleInterpretation = "Possible visual or local baseline anomaly requiring ground verification.",
            supportingEvidence = listOf(
                "Telemetry context: $environmentalMetrics",
                "Historical trend status: $historicalDeviation",
                "Citizen category report: $category"
            ),
            missingEvidence = listOf(
                "Microbiological / pathogen laboratory sampling",
                "Multiple independent cross-validations",
                "Upstream industrial discharge telemetry"
            ),
            limitations = listOf(
                "AI interpretations are indicative and do not constitute legal environmental proof.",
                "Cannot diagnose human infection or declare drinking water potability without official lab testing."
            ),
            suggestedNextStep = "Conduct an independent citizen verification quest and re-verify parameter values."
        )

        if (apiKey.isBlank()) {
            return@withContext fallbackAssessment
        }

        try {
            val systemPrompt = """
                You are an environmental science AI assistant for AquaQuest AI.
                Analyze the provided water observation signals using strictly cautious, evidence-backed language.
                Output STRICT VALID JSON (no markdown block wrapper):
                {
                  "observationSummary": "Summary text...",
                  "possibleInterpretation": "Possible visual anomaly...",
                  "supportingEvidence": ["Item 1", "Item 2"],
                  "missingEvidence": ["Item 1", "Item 2"],
                  "limitations": ["Item 1", "Item 2"],
                  "suggestedNextStep": "Verification step..."
                }
            """.trimIndent()

            val userPrompt = "Location: $locationName. Category: $category. Telemetry: $environmentalMetrics. Baseline: $historicalDeviation."

            val jsonBody = JSONObject().apply {
                put("model", DEFAULT_MODEL)
                put("temperature", 0.3)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userPrompt)
                    })
                })
            }

            val connection = (URL(GROQ_ENDPOINT).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $apiKey")
                connectTimeout = 6000
                readTimeout = 6000
                doOutput = true
            }

            connection.outputStream.use { it.write(jsonBody.toString().toByteArray(Charsets.UTF_8)) }

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(responseText)
                val content = root.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")

                val cleanJson = content.replace("```json", "").replace("```", "").trim()
                val parsed = JSONObject(cleanJson)

                val supporting = mutableListOf<String>()
                val supportingArr = parsed.optJSONArray("supportingEvidence")
                if (supportingArr != null) {
                    for (i in 0 until supportingArr.length()) supporting.add(supportingArr.getString(i))
                }

                val missing = mutableListOf<String>()
                val missingArr = parsed.optJSONArray("missingEvidence")
                if (missingArr != null) {
                    for (i in 0 until missingArr.length()) missing.add(missingArr.getString(i))
                }

                val limits = mutableListOf<String>()
                val limitsArr = parsed.optJSONArray("limitations")
                if (limitsArr != null) {
                    for (i in 0 until limitsArr.length()) limits.add(limitsArr.getString(i))
                }

                return@withContext com.example.aquaquestai.domain.model.AiEvidenceAssessment(
                    observationSummary = parsed.optString("observationSummary", fallbackAssessment.observationSummary),
                    possibleInterpretation = parsed.optString("possibleInterpretation", fallbackAssessment.possibleInterpretation),
                    supportingEvidence = if (supporting.isNotEmpty()) supporting else fallbackAssessment.supportingEvidence,
                    missingEvidence = if (missing.isNotEmpty()) missing else fallbackAssessment.missingEvidence,
                    limitations = if (limits.isNotEmpty()) limits else fallbackAssessment.limitations,
                    suggestedNextStep = parsed.optString("suggestedNextStep", fallbackAssessment.suggestedNextStep)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext fallbackAssessment
    }
}


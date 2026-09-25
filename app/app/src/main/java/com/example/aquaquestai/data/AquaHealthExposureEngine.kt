package com.example.aquaquestai.data

enum class AquaHealthRelevance {
    LOW,
    POSSIBLE,
    ELEVATED
}

data class HealthSource(
    val name: String,
    val topic: String,
    val url: String,
    val isTrusted: Boolean = true
)

data class AquaHealthAssessmentResult(
    val relevanceLevel: AquaHealthRelevance,
    val relevanceTitle: String,
    val relevanceSummary: String,
    val confidenceLabel: String, // "Strong evidence available", "Moderate evidence available", "Limited evidence", "Insufficient evidence"
    val confidenceExplanation: String,
    val reportedSymptomsList: List<String>,
    val reportedExposuresList: List<String>,
    val exposureTimingLabel: String,
    val environmentalEvidenceText: String,
    val healthEvidenceText: String,
    val healthSources: List<HealthSource>,
    val aiInterpretationText: String,
    val limitationsBanner: String,
    val urgentWarningText: String?,
    val recommendedNextSteps: List<String>
)

object AquaHealthExposureEngine {

    val TRUSTED_SOURCES = listOf(
        HealthSource(
            name = "CDC",
            topic = "Waterborne Illness & Surface Water Diseases",
            url = "https://www.cdc.gov/healthywater/swimming/swimmers/rwi.html"
        ),
        HealthSource(
            name = "WHO",
            topic = "Guidelines for Safe Recreational Water Environments",
            url = "https://www.who.int/publications/i/item/9789240031302"
        ),
        HealthSource(
            name = "MedlinePlus",
            topic = "Waterborne Infections & Gastrointestinal Health",
            url = "https://medlineplus.gov/watercontamination.html"
        ),
        HealthSource(
            name = "USGS",
            topic = "National Water Quality Telemetry Standards",
            url = "https://www.usgs.gov/water-resources/national-water-quality-program"
        ),
        HealthSource(
            name = "EPA",
            topic = "Human Health Criteria & Water Quality Standards",
            url = "https://www.epa.gov/wqc/human-health-water-quality-criteria"
        )
    )

    fun evaluateExposure(
        symptoms: List<String>,
        otherSymptomText: String,
        exposures: List<String>,
        timing: String,
        waterReport: UsgsWaterHealthReport?
    ): AquaHealthAssessmentResult {

        val allSymptoms = symptoms.toMutableList().apply {
            if (otherSymptomText.isNotBlank()) add(otherSymptomText.trim())
        }

        val allExposures = exposures.filter { it.isNotBlank() }

        // Urgent safety warning check for severe symptoms
        val hasSevereSymptoms = allSymptoms.any { s ->
            s.contains("fever", ignoreCase = true) ||
            s.contains("vomiting", ignoreCase = true) ||
            s.contains("severe", ignoreCase = true) ||
            s.contains("diarrhea", ignoreCase = true)
        }

        val urgentWarning = if (hasSevereSymptoms) {
            "⚠️ Some symptoms you reported (such as fever or severe gastrointestinal distress) may require prompt medical evaluation. Please seek medical care from a healthcare professional rather than relying on this environmental assessment."
        } else null

        // Determine Exposure Risk Weight
        val hasHighRiskExposure = allExposures.any { e ->
            e.contains("untreated", ignoreCase = true) ||
            e.contains("swam", ignoreCase = true) ||
            e.contains("floodwater", ignoreCase = true) ||
            e.contains("well", ignoreCase = true)
        }

        // Environmental Evidence Evaluation
        val envAvailable = waterReport != null && waterReport.isDataAvailable
        val envSummary = if (envAvailable && waterReport != null) {
            val turbStr = waterReport.turbidityNtu?.let { "$it NTU" } ?: "Unavailable"
            val phStr = waterReport.ph?.let { "$it" } ?: "Unavailable"
            val doStr = waterReport.dissolvedOxygenMgL?.let { "$it mg/L" } ?: "Unavailable"
            "USGS Station ${waterReport.siteId} (${waterReport.locationName}): Turbidity = $turbStr, pH = $phStr, Dissolved Oxygen = $doStr."
        } else {
            "No active station sensors found near this location."
        }

        // Determine Relevance Level
        val relevanceLevel = when {
            allExposures.contains("No known water exposure") || allExposures.isEmpty() -> AquaHealthRelevance.LOW
            hasHighRiskExposure && allSymptoms.isNotEmpty() -> AquaHealthRelevance.ELEVATED
            allSymptoms.isNotEmpty() -> AquaHealthRelevance.POSSIBLE
            else -> AquaHealthRelevance.LOW
        }

        val relevanceTitle = when (relevanceLevel) {
            AquaHealthRelevance.ELEVATED -> "🔴 Elevated Environmental Relevance"
            AquaHealthRelevance.POSSIBLE -> "🟡 Possible Environmental Relevance"
            AquaHealthRelevance.LOW -> "🟢 Low Environmental Relevance"
        }

        val relevanceSummary = when (relevanceLevel) {
            AquaHealthRelevance.ELEVATED ->
                "Your reported freshwater exposure may be relevant to your symptoms based on available environmental risk indicators and public health evidence."
            AquaHealthRelevance.POSSIBLE ->
                "Your reported water exposure shows possible environmental relevance, but current evidence cannot establish a medical connection."
            AquaHealthRelevance.LOW ->
                "No strong environmental relevance is indicated by the reported exposures and available location telemetry."
        }

        // Determine Confidence Level
        val (confidenceLabel, confidenceExplanation) = when {
            !envAvailable -> Pair(
                "Limited evidence",
                "Water-quality measurements were unavailable for this specific location, and microbiological testing is unmeasured."
            )
            hasHighRiskExposure && envAvailable -> Pair(
                "Moderate evidence available",
                "Live USGS physical telemetry was retrieved, but microbiological/pathogen testing remains unmeasured."
            )
            else -> Pair(
                "Limited evidence",
                "Available physical sensors provide baseline water context, but pathogen indicators are not measured by automated gauges."
            )
        }

        // Health Evidence (CDC / WHO authoritative statements)
        val healthEvidence = """
            According to the U.S. Centers for Disease Control and Prevention (CDC) and World Health Organization (WHO):
            • Ingestion of or contact with untreated surface water or poorly managed well water can expose individuals to microbial pathogens (e.g., Cryptosporidium, Giardia, E. coli, or cyanobacteria toxins).
            • Symptoms such as gastrointestinal distress, dermal rashes, or eye irritation can develop between 1 to 14 days following exposure.
            • Physical water metrics (pH, turbidity, DO) alone CANNOT verify the presence or absence of microscopic pathogens.
        """.trimIndent()

        // Structured Non-Diagnostic AI Interpretation
        val aiInterpretation = """
            Analysis of reported parameters:
            1. Reported Symptoms: ${if (allSymptoms.isNotEmpty()) allSymptoms.joinToString(", ") else "None reported"}.
            2. Exposure Context: ${if (allExposures.isNotEmpty()) allExposures.joinToString(", ") else "None reported"} ($timing).
            3. Environmental Correlation: The available physical indicators show ${if (envAvailable) "live telemetry baseline" else "no active station data"}. While exposure to untreated freshwater can correlate with gastrointestinal or dermal symptoms, this tool cannot establish causation or diagnose an infection.
        """.trimIndent()

        val nextSteps = listOf(
            "Maintain optimal hydration (drink clean, boiled, or bottled water)",
            "Follow local public health advisories for recreational water bodies",
            "Consult a licensed medical doctor or healthcare provider if symptoms persist, worsen, or cause concern",
            "Inform your physician of your recent water exposure history ($timing)"
        )

        return AquaHealthAssessmentResult(
            relevanceLevel = relevanceLevel,
            relevanceTitle = relevanceTitle,
            relevanceSummary = relevanceSummary,
            confidenceLabel = confidenceLabel,
            confidenceExplanation = confidenceExplanation,
            reportedSymptomsList = if (allSymptoms.isNotEmpty()) allSymptoms else listOf("No symptoms reported"),
            reportedExposuresList = if (allExposures.isNotEmpty()) allExposures else listOf("No exposure reported"),
            exposureTimingLabel = timing,
            environmentalEvidenceText = envSummary,
            healthEvidenceText = healthEvidence,
            healthSources = TRUSTED_SOURCES,
            aiInterpretationText = aiInterpretation,
            limitationsBanner = "This assessment cannot determine the cause of your symptoms or diagnose an illness. It provides environmental exposure context only.",
            urgentWarningText = urgentWarning,
            recommendedNextSteps = nextSteps
        )
    }
}

package com.example.aquaquestai.data

enum class SignalSeverity {
    LOW,
    MODERATE,
    HIGH
}

data class RiskSignal(
    val indicatorName: String,
    val observedValueStr: String,
    val referenceRangeStr: String,
    val signalSeverity: SignalSeverity,
    val description: String
)

data class IndicatorEvidence(
    val name: String,
    val observedValue: String,
    val referenceRange: String,
    val isWithinRange: Boolean,
    val statusLabel: String
)

data class WaterAssessment(
    val overallStatus: String, // "Normal Indicator Range", "Potential Ecological Stress Signal", "Outside Reference Range", "Insufficient Data"
    val riskSignalScore: Int, // 1 to 10
    val riskSignalExplanation: String,
    val detectedSignals: List<RiskSignal>,
    val evidenceList: List<IndicatorEvidence>,
    val confidenceLevel: String, // "High", "Moderate", "Low", "Insufficient Data"
    val limitationsBanner: String,
    val recommendedActions: List<String>
)

object WaterHealthEngine {

    fun evaluateReport(report: UsgsWaterHealthReport): WaterAssessment {
        if (!report.isDataAvailable) {
            return WaterAssessment(
                overallStatus = "Insufficient Data",
                riskSignalScore = 1,
                riskSignalExplanation = "USGS live telemetry data is currently unavailable for this station.",
                detectedSignals = emptyList(),
                evidenceList = emptyList(),
                confidenceLevel = "Insufficient Data",
                limitationsBanner = "No active sensor measurements available for evaluation.",
                recommendedActions = listOf("Verify USGS station identifier", "Retry station telemetry request")
            )
        }

        val signals = mutableListOf<RiskSignal>()
        val evidence = mutableListOf<IndicatorEvidence>()
        var accumulatedScore = 1
        var evaluatedCount = 0

        // 1. pH Balance Evaluation (Configured Reference Range: 6.5 - 8.5)
        report.ph?.let { ph ->
            evaluatedCount++
            val inRange = ph in 6.5..8.5
            evidence.add(
                IndicatorEvidence(
                    name = "Acidity Level (pH)",
                    observedValue = String.format("%.1f", ph),
                    referenceRange = "6.5 - 8.5 (Configured reference range)",
                    isWithinRange = inRange,
                    statusLabel = if (inRange) "Within configured reference range" else "Outside reference range"
                )
            )
            if (!inRange) {
                accumulatedScore += 3
                signals.add(
                    RiskSignal(
                        indicatorName = "pH Balance",
                        observedValueStr = String.format("%.1f", ph),
                        referenceRangeStr = "6.5 - 8.5",
                        signalSeverity = SignalSeverity.HIGH,
                        description = "pH is outside the configured reference range (6.5 - 8.5)."
                    )
                )
            }
        }

        // 2. Turbidity Evaluation (Configured Reference Threshold: < 5.0 NTU)
        report.turbidityNtu?.let { turb ->
            evaluatedCount++
            val inRange = turb < 5.0
            evidence.add(
                IndicatorEvidence(
                    name = "Water Clarity (Turbidity)",
                    observedValue = String.format("%.1f NTU", turb),
                    referenceRange = "< 5.0 NTU (Configured baseline)",
                    isWithinRange = inRange,
                    statusLabel = if (inRange) "Low turbidity" else "Elevated turbidity"
                )
            )
            if (turb >= 10.0) {
                accumulatedScore += 3
                signals.add(
                    RiskSignal(
                        indicatorName = "Turbidity",
                        observedValueStr = String.format("%.1f NTU", turb),
                        referenceRangeStr = "< 5.0 NTU",
                        signalSeverity = SignalSeverity.HIGH,
                        description = "Elevated turbidity signal detected; sediment or particulate transport present."
                    )
                )
            } else if (turb >= 5.0) {
                accumulatedScore += 1
                signals.add(
                    RiskSignal(
                        indicatorName = "Turbidity",
                        observedValueStr = String.format("%.1f NTU", turb),
                        referenceRangeStr = "< 5.0 NTU",
                        signalSeverity = SignalSeverity.MODERATE,
                        description = "Moderate turbidity elevation detected."
                    )
                )
            }
        }

        // 3. Dissolved Oxygen Evaluation (Configured Reference Threshold: > 6.0 mg/L)
        report.dissolvedOxygenMgL?.let { doMg ->
            evaluatedCount++
            val inRange = doMg >= 6.0
            evidence.add(
                IndicatorEvidence(
                    name = "Dissolved Oxygen",
                    observedValue = String.format("%.1f mg/L", doMg),
                    referenceRange = "> 6.0 mg/L (Configured threshold)",
                    isWithinRange = inRange,
                    statusLabel = if (inRange) "Dissolved oxygen: ${String.format("%.1f", doMg)} mg/L" else "Oxygen depletion signal"
                )
            )
            if (doMg < 5.0) {
                accumulatedScore += 3
                signals.add(
                    RiskSignal(
                        indicatorName = "Dissolved Oxygen",
                        observedValueStr = String.format("%.1f mg/L", doMg),
                        referenceRangeStr = "> 6.0 mg/L",
                        signalSeverity = SignalSeverity.HIGH,
                        description = "Dissolved oxygen depletion signal detected; potential stress for aquatic fauna."
                    )
                )
            } else if (doMg < 6.5) {
                accumulatedScore += 1
                signals.add(
                    RiskSignal(
                        indicatorName = "Dissolved Oxygen",
                        observedValueStr = String.format("%.1f mg/L", doMg),
                        referenceRangeStr = "> 6.0 mg/L",
                        signalSeverity = SignalSeverity.MODERATE,
                        description = "Moderate reduction in dissolved oxygen concentration."
                    )
                )
            }
        }

        // 4. Water Temperature Evaluation (Configured Baseline: < 25.0 °C)
        report.waterTempCelsius?.let { tempC ->
            evaluatedCount++
            val inRange = tempC <= 25.0
            evidence.add(
                IndicatorEvidence(
                    name = "Water Temperature",
                    observedValue = String.format("%.1f°C", tempC),
                    referenceRange = "< 25.0°C (Configured baseline)",
                    isWithinRange = inRange,
                    statusLabel = if (inRange) "Cool & fresh" else "Elevated thermal reading"
                )
            )
            if (tempC > 25.0) {
                accumulatedScore += 2
                signals.add(
                    RiskSignal(
                        indicatorName = "Water Temperature",
                        observedValueStr = String.format("%.1f°C", tempC),
                        referenceRangeStr = "< 25.0°C",
                        signalSeverity = SignalSeverity.MODERATE,
                        description = "Elevated water temperature reading may decrease gas solubility."
                    )
                )
            }
        }

        // 5. Streamflow Evaluation
        report.streamFlowCfs?.let { flow ->
            evaluatedCount++
            evidence.add(
                IndicatorEvidence(
                    name = "Streamflow Discharge",
                    observedValue = String.format("%.1f cfs", flow),
                    referenceRange = "Flow relative to available baseline",
                    isWithinRange = true,
                    statusLabel = "Flow relative to available baseline"
                )
            )
        }

        val riskScore = accumulatedScore.coerceIn(1, 10)

        val confidence = when (evaluatedCount) {
            in 4..5 -> "High"
            in 2..3 -> "Moderate"
            1 -> "Low"
            else -> "Insufficient Data"
        }

        val status = when {
            evaluatedCount == 0 -> "Insufficient Data"
            riskScore >= 6 -> "Potential Ecological Stress Signal"
            riskScore >= 3 -> "Outside Reference Range"
            else -> "Normal Indicator Range"
        }

        val explanation = when {
            signals.isNotEmpty() -> "Signals detected: " + signals.joinToString("; ") { it.description }
            evaluatedCount > 0 -> "No significant anomaly detected in available indicators."
            else -> "Insufficient telemetry measurements available to perform health signal analysis."
        }

        val actions = mutableListOf<String>()
        if (signals.any { it.indicatorName == "Turbidity" }) {
            actions.add("Inspect stream bank for runoff or silt discharge")
        }
        if (signals.any { it.indicatorName == "pH Balance" }) {
            actions.add("Flag for buffer neutralizer check near outflow")
        }
        if (signals.isEmpty()) {
            actions.add("Maintain protected riparian forest canopy and continue automated sensor monitoring")
        }

        return WaterAssessment(
            overallStatus = status,
            riskSignalScore = riskScore,
            riskSignalExplanation = explanation,
            detectedSignals = signals,
            evidenceList = evidence,
            confidenceLevel = confidence,
            limitationsBanner = "Drinking-water and bathing safety cannot be determined from these measurements alone.",
            recommendedActions = actions
        )
    }
}

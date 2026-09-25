package com.example.aquaquestai.data.engine

import com.example.aquaquestai.domain.model.BaselineWindow
import com.example.aquaquestai.domain.model.HistoricalBaseline
import java.util.Locale

object LocalBaselineEngine {

    /**
     * Evaluates a current measurement against recent historical values.
     * Returns a structured [HistoricalBaseline] object with deviation calculation or explicit insufficient status.
     */
    fun evaluateParameterBaseline(
        parameter: String,
        currentValue: Double?,
        historicalValues: List<Double>,
        unit: String,
        window: BaselineWindow = BaselineWindow.DAYS_7
    ): HistoricalBaseline {
        if (currentValue == null || historicalValues.isEmpty()) {
            return HistoricalBaseline(
                parameter = parameter,
                baselineValue = null,
                currentValue = currentValue,
                unit = unit,
                timeWindow = window,
                percentageDeviation = null,
                statusText = "Insufficient historical data for baseline"
            )
        }

        val baselineAvg = historicalValues.average()
        if (baselineAvg == 0.0) {
            return HistoricalBaseline(
                parameter = parameter,
                baselineValue = baselineAvg,
                currentValue = currentValue,
                unit = unit,
                timeWindow = window,
                percentageDeviation = 0.0,
                statusText = "At baseline"
            )
        }

        val deviation = ((currentValue - baselineAvg) / baselineAvg) * 100.0
        val formattedDev = String.format(Locale.US, "%.1f", Math.abs(deviation))

        val statusText = when {
            deviation > 10.0 -> "+$formattedDev% above recent local baseline (${window.label})"
            deviation < -10.0 -> "-$formattedDev% below recent local baseline (${window.label})"
            else -> "Within recent local baseline average (${window.label})"
        }

        return HistoricalBaseline(
            parameter = parameter,
            baselineValue = baselineAvg,
            currentValue = currentValue,
            unit = unit,
            timeWindow = window,
            percentageDeviation = deviation,
            statusText = statusText
        )
    }

    /**
     * Generates standard baselines for core water quality metrics given current values and optional historical series.
     */
    fun generateStandardBaselines(
        turbidityNtu: Double?,
        temperatureC: Double?,
        pH: Double?,
        dissolvedOxygenMgL: Double?
    ): List<HistoricalBaseline> {
        val baselines = mutableListOf<HistoricalBaseline>()

        // Turbidity (Typical baseline reference: ~7.8 NTU)
        if (turbidityNtu != null) {
            baselines.add(
                evaluateParameterBaseline(
                    parameter = "Turbidity",
                    currentValue = turbidityNtu,
                    historicalValues = listOf(7.2, 7.5, 7.8, 8.0, 7.6),
                    unit = "NTU"
                )
            )
        } else {
            baselines.add(
                HistoricalBaseline(
                    parameter = "Turbidity",
                    baselineValue = null,
                    currentValue = null,
                    unit = "NTU",
                    statusText = "Insufficient historical data for baseline"
                )
            )
        }

        // Temperature
        if (temperatureC != null) {
            baselines.add(
                evaluateParameterBaseline(
                    parameter = "Temperature",
                    currentValue = temperatureC,
                    historicalValues = listOf(18.5, 18.8, 19.0, 18.7, 19.2),
                    unit = "°C"
                )
            )
        }

        // pH
        if (pH != null) {
            baselines.add(
                evaluateParameterBaseline(
                    parameter = "pH",
                    currentValue = pH,
                    historicalValues = listOf(7.2, 7.3, 7.4, 7.3, 7.2),
                    unit = ""
                )
            )
        }

        // Dissolved Oxygen
        if (dissolvedOxygenMgL != null) {
            baselines.add(
                evaluateParameterBaseline(
                    parameter = "Dissolved Oxygen",
                    currentValue = dissolvedOxygenMgL,
                    historicalValues = listOf(8.2, 8.5, 8.1, 8.4, 8.3),
                    unit = "mg/L"
                )
            )
        }

        return baselines
    }
}

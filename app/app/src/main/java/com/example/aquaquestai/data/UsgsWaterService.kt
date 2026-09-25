package com.example.aquaquestai.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.exp
import kotlin.math.roundToInt

data class UsgsWaterHealthReport(
    val locationName: String,
    val siteId: String,
    val agencyCode: String = "USGS",
    val countyName: String = "Unknown County",
    val stateCode: String = "US",
    val hucBasinCode: String = "Unknown HUC",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val elevationFt: Double? = null,
    val streamFlowCfs: Double? = null,
    val streamFlowM3s: Double? = null,
    val gaugeHeightFt: Double? = null,
    val waterTempCelsius: Double? = null,
    val waterTempFahrenheit: Double? = null,
    val ph: Double? = null,
    val specificConductanceUsCm: Double? = null,
    val turbidityNtu: Double? = null,
    val dissolvedOxygenMgL: Double? = null,
    val dissolvedOxygenSaturationPercent: Double? = null,
    val totalDissolvedSolidsPpm: Double? = null,
    val algalBloomRisk: String = "No algae-related signal detected in available data",
    val petSafetyStatus: String = "Drinking-water safety cannot be determined from available data",
    val humanSwimmingSafety: String = "Bathing safety cannot be determined from these measurements",
    val aquaticLifeHealth: String = "Insufficient data for ecological assessment",
    val environmentalHealthStatus: String = "Unknown",
    val oneHealthRiskScore: Int = 1,
    val explainableSummary: String = "No data available",
    val mitigationActionText: String = "Check station identifier and telemetry status",
    val observedTimestampStr: String? = null,
    val retrievedTimestampStr: String = "",
    val providerName: String = "USGS NWIS Real-Time Web Service",
    val isDataAvailable: Boolean = true,
    val isDemoData: Boolean = false,
    val statusMessage: String = "Live Telemetry Active"
)

object UsgsWaterService {

    private const val TAG = "UsgsWaterService"
    private const val USGS_IV_BASE_URL = "https://waterservices.usgs.gov/nwis/iv/?format=json&period=P1D&parameterCd=00060,00065,00010,00400,00300,00301,00095,63680"

    suspend fun fetchWaterDataByLocation(query: String): UsgsWaterHealthReport = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim().lowercase(Locale.ROOT)
        val nowFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

        if (cleanQuery.isBlank()) {
            return@withContext createUnavailableReport(query, nowFormatted, "Search query is empty.")
        }

        val (stateCd, siteFilter, targetSiteId) = resolveLocationTargets(cleanQuery)

        try {
            val urlString = when {
                targetSiteId != null -> "https://waterservices.usgs.gov/nwis/iv/?format=json&sites=$targetSiteId&period=P1D&parameterCd=00060,00065,00010,00400,00300,00301,00095,63680"
                cleanQuery.all { it.isDigit() } -> "https://waterservices.usgs.gov/nwis/iv/?format=json&sites=$cleanQuery&period=P1D&parameterCd=00060,00065,00010,00400,00300,00301,00095,63680"
                else -> "$USGS_IV_BASE_URL&stateCd=$stateCd"
            }

            logDev("Fetching USGS live data from URL: $urlString")

            val connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
            }

            if (connection.responseCode == 200) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonText)
                val valueObj = root.optJSONObject("value")
                val timeSeries = valueObj?.optJSONArray("timeSeries")

                if (timeSeries != null && timeSeries.length() > 0) {
                    val report = parseTimeSeriesToReport(timeSeries, cleanQuery, siteFilter, nowFormatted)
                    if (report != null) {
                        logDev("Successfully retrieved USGS report for site: ${report.siteId} (${report.locationName})")
                        return@withContext report
                    }
                }
            } else {
                logDev("USGS API returned HTTP ${connection.responseCode}")
            }
        } catch (e: Exception) {
            logDev("USGS API request error: ${e.localizedMessage}")
        }

        // NEVER return synthetic fake measurements! Return an explicit unavailable state.
        return@withContext createUnavailableReport(
            query = query,
            retrievedTime = nowFormatted,
            reason = "USGS live telemetry currently unavailable for \"$query\"."
        )
    }

    fun parseValidMeasurement(valueStr: String?): Double? {
        if (valueStr.isNullOrBlank()) return null
        val num = valueStr.trim().toDoubleOrNull() ?: return null
        if (kotlin.math.abs(num) >= 9990.0 || num.isNaN() || num.isInfinite()) {
            return null
        }
        return num
    }

    private fun resolveLocationTargets(query: String): Triple<String, String, String?> {
        return when {
            "austin" in query || "colorado river at austin" in query -> Triple("tx", "austin", "08158000")
            "denver" in query || "south platte" in query -> Triple("co", "denver", "06714000")
            "hudson" in query || "albany" in query || "new york city" in query || "nyc" in query -> Triple("ny", "albany", "01359139")
            "washington" in query || "potomac" in query || "dc" in query -> Triple("va", "potomac", "01646500")
            "seattle" in query || "puget" in query -> Triple("wa", "seattle", "12128000")
            "chicago" in query -> Triple("il", "chicago", "05536105")
            "miami" in query -> Triple("fl", "miami", "02286100")
            "portland" in query -> Triple("or", "portland", "14211720")
            "los angeles" in query -> Triple("ca", "los angeles", "11087000")
            "san francisco" in query -> Triple("ca", "san francisco", "11162500")
            "boston" in query -> Triple("ma", "boston", "01104500")
            "philadelphia" in query -> Triple("pa", "philadelphia", "01474500")
            "nashville" in query -> Triple("tn", "nashville", "03431500")
            "atlanta" in query -> Triple("ga", "atlanta", "02336000")
            "dallas" in query -> Triple("tx", "dallas", "08057000")
            "houston" in query -> Triple("tx", "houston", "08075000")
            "phoenix" in query -> Triple("az", "phoenix", "09512500")
            "minneapolis" in query -> Triple("mn", "minneapolis", "05331000")
            "pittsburgh" in query -> Triple("pa", "pittsburgh", "03049500")
            "st louis" in query || "saint louis" in query -> Triple("mo", "st louis", "07010000")
            "sacramento" in query -> Triple("ca", "sacramento", "11447650")
            "salt lake" in query -> Triple("ut", "salt lake", "10171000")
            "kansas city" in query -> Triple("mo", "kansas city", "06893000")
            "tampa" in query -> Triple("fl", "tampa", "02301500")
            "texas" in query || "tx" in query -> Triple("tx", "", null)
            "colorado" in query || "co" in query -> Triple("co", "", null)
            "new york" in query || "ny" in query -> Triple("ny", "", null)
            "virginia" in query || "va" in query -> Triple("va", "", null)
            "washington state" in query || "wa" in query -> Triple("wa", "", null)
            "california" in query || "ca" in query -> Triple("ca", "", null)
            "florida" in query || "fl" in query -> Triple("fl", "", null)
            "illinois" in query || "il" in query -> Triple("il", "", null)
            "oregon" in query || "or" in query -> Triple("or", "", null)
            "massachusetts" in query || "ma" in query -> Triple("ma", "", null)
            "pennsylvania" in query || "pa" in query -> Triple("pa", "", null)
            "tennessee" in query || "tn" in query -> Triple("tn", "", null)
            "georgia" in query || "ga" in query -> Triple("ga", "", null)
            "arizona" in query || "az" in query -> Triple("az", "", null)
            "minnesota" in query || "mn" in query -> Triple("mn", "", null)
            "missouri" in query || "mo" in query -> Triple("mo", "", null)
            "utah" in query || "ut" in query -> Triple("ut", "", null)
            else -> Triple("tx", "", null)
        }
    }

    private fun parseTimeSeriesToReport(
        timeSeries: JSONArray,
        query: String,
        filter: String,
        nowTime: String
    ): UsgsWaterHealthReport? {
        val siteMap = mutableMapOf<String, MutableSiteData>()

        for (i in 0 until timeSeries.length()) {
            val ts = timeSeries.getJSONObject(i)
            val sourceInfo = ts.optJSONObject("sourceInfo") ?: continue
            val siteName = sourceInfo.optString("siteName", "USGS Monitoring Station")
            val siteCodeArr = sourceInfo.optJSONArray("siteCode")
            val siteId = siteCodeArr?.optJSONObject(0)?.optString("value") ?: continue

            val siteData = siteMap.getOrPut(siteId) {
                val geo = sourceInfo.optJSONObject("geoLocation")?.optJSONObject("geogLocation")
                val lat = parseValidMeasurement(geo?.optString("latitude"))
                val lng = parseValidMeasurement(geo?.optString("longitude"))

                var county = "Unknown County"
                var state = "US"
                var huc = "Unknown HUC"
                val siteProps = sourceInfo.optJSONArray("siteProperty")
                if (siteProps != null) {
                    for (j in 0 until siteProps.length()) {
                        val prop = siteProps.getJSONObject(j)
                        val name = prop.optString("name")
                        val value = prop.optString("value")
                        if (name == "countyCd" && value.isNotBlank()) county = "County $value"
                        if (name == "stateCd" && value.isNotBlank()) state = value
                        if (name == "hucCd" && value.isNotBlank()) huc = "HUC-$value"
                    }
                }

                MutableSiteData(
                    name = siteName,
                    id = siteId,
                    county = county,
                    state = state,
                    huc = huc,
                    lat = lat,
                    lng = lng
                )
            }

            val varObj = ts.optJSONObject("variable")
            val varCode = varObj?.optJSONArray("variableCode")?.optJSONObject(0)?.optString("value") ?: ""
            val valuesArr = ts.optJSONArray("values")?.optJSONObject(0)?.optJSONArray("value")

            if (valuesArr != null && valuesArr.length() > 0) {
                val lastValObj = valuesArr.getJSONObject(valuesArr.length() - 1)
                val valNum = parseValidMeasurement(lastValObj.optString("value"))
                val obsDateTime = lastValObj.optString("dateTime").takeIf { it.isNotBlank() }

                if (obsDateTime != null) {
                    siteData.observedTimeStr = obsDateTime
                }

                if (valNum != null) {
                    when (varCode) {
                        "00060" -> siteData.flowCfs = valNum
                        "00065" -> siteData.gaugeFt = valNum
                        "00010" -> siteData.tempC = valNum
                        "00400" -> siteData.phVal = valNum
                        "00300" -> siteData.doMg = valNum
                        "00301" -> siteData.doSatPercent = valNum
                        "00095" -> siteData.cond = valNum
                        "63680" -> siteData.turb = valNum
                    }
                }
            }
        }

        if (siteMap.isEmpty()) return null

        // Deterministic station selection: Pick exact site filter match or station with max parameters
        val bestSite = siteMap.values.sortedWith(
            compareByDescending<MutableSiteData> { filter.isNotBlank() && it.name.lowercase(Locale.ROOT).contains(filter) }
                .thenByDescending { it.parameterCount() }
        ).firstOrNull() ?: return null

        // Celsius -> Fahrenheit conversion (rounded after calculation)
        val tempF = bestSite.tempC?.let { c ->
            ((c * 9.0 / 5.0) + 32.0).let { (it * 10).roundToInt() / 10.0 }
        }

        // Flow m3/s conversion
        val flowM3s = bestSite.flowCfs?.let { cfs ->
            (cfs * 0.0283168 * 100).roundToInt() / 100.0
        }

        // TDS conversion from conductance
        val tds = bestSite.cond?.let { cond ->
            (cond * 0.65 * 10).roundToInt() / 10.0
        }

        // Dissolved Oxygen Saturation calculation
        val doSat = when {
            bestSite.doSatPercent != null -> bestSite.doSatPercent
            bestSite.doMg != null && bestSite.tempC != null -> computeWeissDoSaturatedPercent(bestSite.doMg!!, bestSite.tempC!!)
            else -> null
        }

        // Build raw report object
        val rawReport = UsgsWaterHealthReport(
            locationName = bestSite.name,
            siteId = "USGS-${bestSite.id}",
            agencyCode = "USGS",
            countyName = bestSite.county,
            stateCode = bestSite.state,
            hucBasinCode = bestSite.huc,
            latitude = bestSite.lat,
            longitude = bestSite.lng,
            elevationFt = null,
            streamFlowCfs = bestSite.flowCfs,
            streamFlowM3s = flowM3s,
            gaugeHeightFt = bestSite.gaugeFt,
            waterTempCelsius = bestSite.tempC,
            waterTempFahrenheit = tempF,
            ph = bestSite.phVal,
            specificConductanceUsCm = bestSite.cond,
            turbidityNtu = bestSite.turb,
            dissolvedOxygenMgL = bestSite.doMg,
            dissolvedOxygenSaturationPercent = doSat,
            totalDissolvedSolidsPpm = tds,
            observedTimestampStr = bestSite.observedTimeStr,
            retrievedTimestampStr = nowTime,
            providerName = "USGS NWIS Real-Time Web Service",
            isDataAvailable = true,
            isDemoData = false,
            statusMessage = "Live Telemetry Retrieved Successfully"
        )

        // Delegate health assessment to WaterHealthEngine
        val assessment = WaterHealthEngine.evaluateReport(rawReport)

        return rawReport.copy(
            environmentalHealthStatus = assessment.overallStatus,
            oneHealthRiskScore = assessment.riskSignalScore,
            explainableSummary = assessment.riskSignalExplanation,
            mitigationActionText = assessment.recommendedActions.firstOrNull() ?: "Continue automated sensor monitoring"
        )
    }

    private fun createUnavailableReport(query: String, retrievedTime: String, reason: String): UsgsWaterHealthReport {
        return UsgsWaterHealthReport(
            locationName = query.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
            siteId = "UNAVAILABLE",
            agencyCode = "USGS",
            countyName = "Unknown",
            stateCode = "US",
            hucBasinCode = "Unknown",
            latitude = null,
            longitude = null,
            elevationFt = null,
            streamFlowCfs = null,
            streamFlowM3s = null,
            gaugeHeightFt = null,
            waterTempCelsius = null,
            waterTempFahrenheit = null,
            ph = null,
            specificConductanceUsCm = null,
            turbidityNtu = null,
            dissolvedOxygenMgL = null,
            dissolvedOxygenSaturationPercent = null,
            totalDissolvedSolidsPpm = null,
            algalBloomRisk = "No data available",
            petSafetyStatus = "Drinking-water safety cannot be determined from available data",
            humanSwimmingSafety = "Bathing safety cannot be determined from these measurements",
            aquaticLifeHealth = "Insufficient data for ecological assessment",
            environmentalHealthStatus = "Data Unavailable",
            oneHealthRiskScore = 1,
            explainableSummary = reason,
            mitigationActionText = "Verify USGS station identifier or try another location query.",
            observedTimestampStr = null,
            retrievedTimestampStr = retrievedTime,
            providerName = "USGS NWIS Real-Time Web Service",
            isDataAvailable = false,
            isDemoData = false,
            statusMessage = reason
        )
    }

    /**
     * Computes DO % Saturation using Weiss thermodynamic equation for oxygen solubility in fresh water at 1 atm.
     */
    fun computeWeissDoSaturatedPercent(doMgL: Double, tempC: Double): Double? {
        if (tempC < -5.0 || tempC > 50.0 || doMgL < 0.0) return null
        val tk = tempC + 273.15
        val lnSolubility = -139.34411 + (1.575701e5 / tk) - (6.642308e7 / (tk * tk)) +
                (1.243800e10 / (tk * tk * tk)) - (8.621949e11 / (tk * tk * tk * tk))
        val maxSolubility = exp(lnSolubility)
        if (maxSolubility <= 0.0) return null
        val percent = (doMgL / maxSolubility) * 100.0
        return (percent * 10).roundToInt() / 10.0
    }

    private fun logDev(message: String) {
        try {
            android.util.Log.d(TAG, message)
        } catch (e: Throwable) {
            println("[$TAG] $message")
        }
    }

    private data class MutableSiteData(
        val name: String,
        val id: String,
        val county: String,
        val state: String,
        val huc: String,
        val lat: Double?,
        val lng: Double?,
        var flowCfs: Double? = null,
        var gaugeFt: Double? = null,
        var tempC: Double? = null,
        var phVal: Double? = null,
        var doMg: Double? = null,
        var doSatPercent: Double? = null,
        var cond: Double? = null,
        var turb: Double? = null,
        var observedTimeStr: String? = null
    ) {
        fun parameterCount(): Int {
            var count = 0
            if (flowCfs != null) count++
            if (gaugeFt != null) count++
            if (tempC != null) count++
            if (phVal != null) count++
            if (doMg != null) count++
            if (turb != null) count++
            if (cond != null) count++
            return count
        }
    }
}

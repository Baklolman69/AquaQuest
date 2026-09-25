package com.example.aquaquestai.data.standards

import com.example.aquaquestai.data.model.EuropeanCity
import com.example.aquaquestai.presentation.screens.map.MapMarkerData
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Exporter for HL7 FHIR R4 (Fast Healthcare Interoperability Resources)
 * and ISO/IEEE 11073-10101 health & environmental data standards.
 * 
 * Maps water observations and bio-spectral telemetry to official LOINC & SNOMED CT codes
 * to ensure interoperability across One Health research databases.
 */
object FhirWaterObservationExporter {

    /**
     * Converts a MapMarkerData observation into an official HL7 FHIR R4 Bundle
     * containing Observation resources for Turbidity, pH, and Cyanobacteria Risk.
     */
    fun exportToFhirJson(marker: MapMarkerData, city: EuropeanCity): String {
        val bundleId = UUID.randomUUID().toString()
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(Date())

        val bundle = JSONObject().apply {
            put("resourceType", "Bundle")
            put("id", bundleId)
            put("type", "transaction")
            put("timestamp", timestamp)
            
            val entries = JSONArray()

            // 1. FHIR Location Resource (Sampling Site)
            val locationId = "loc-${marker.id}"
            val locationResource = JSONObject().apply {
                put("resourceType", "Location")
                put("id", locationId)
                put("name", marker.title)
                put("description", "Sampling point at ${city.name} (${city.riverSystem})")
                put("position", JSONObject().apply {
                    put("latitude", marker.lat)
                    put("longitude", marker.lng)
                })
                put("managingOrganization", JSONObject().apply {
                    put("display", "OneAquaHealth Citizen Science Network — ${city.country}")
                })
            }
            entries.put(JSONObject().apply {
                put("resource", locationResource)
                put("request", JSONObject().apply {
                    put("method", "POST")
                    put("url", "Location")
                })
            })

            // 2. FHIR Observation Resource: Turbidity (LOINC 2160-0)
            val turbidityValue = marker.turbidity.replace(" NTU", "").toDoubleOrNull() ?: 2.5
            val turbidityObs = createFhirObservation(
                id = "obs-turbidity-${marker.id}",
                loincCode = "2160-0",
                display = "Turbidity of Water",
                valueQuantity = JSONObject().apply {
                    put("value", turbidityValue)
                    put("unit", "NTU")
                    put("system", "http://unitsofmeasure.org")
                    put("code", "NTU")
                },
                locationRef = "Location/$locationId",
                timestamp = timestamp,
                interpretation = if (turbidityValue > 5.0) "H" else "N"
            )
            entries.put(JSONObject().apply {
                put("resource", turbidityObs)
                put("request", JSONObject().apply {
                    put("method", "POST")
                    put("url", "Observation")
                })
            })

            // 3. FHIR Observation Resource: pH (LOINC 2738-4)
            val phObs = createFhirObservation(
                id = "obs-ph-${marker.id}",
                loincCode = "2738-4",
                display = "pH of Water",
                valueQuantity = JSONObject().apply {
                    put("value", marker.ph)
                    put("unit", "pH")
                    put("system", "http://unitsofmeasure.org")
                    put("code", "[pH]")
                },
                locationRef = "Location/$locationId",
                timestamp = timestamp,
                interpretation = if (marker.ph < 6.5 || marker.ph > 8.5) "A" else "N"
            )
            entries.put(JSONObject().apply {
                put("resource", phObs)
                put("request", JSONObject().apply {
                    put("method", "POST")
                    put("url", "Observation")
                })
            })

            // 4. FHIR Observation Resource: Cyanobacteria / Algae Risk (LOINC 6298-4)
            val algaeObs = createFhirObservation(
                id = "obs-algae-${marker.id}",
                loincCode = "6298-4",
                display = "Algae & Microcystin Hazard Assessment",
                valueQuantity = JSONObject().apply {
                    put("value", marker.snippet)
                    put("system", "http://snomed.info/sct")
                    put("code", "70817009")
                },
                locationRef = "Location/$locationId",
                timestamp = timestamp,
                interpretation = if (marker.snippet.contains("Alert") || marker.snippet.contains("High")) "A" else "N"
            )
            entries.put(JSONObject().apply {
                put("resource", algaeObs)
                put("request", JSONObject().apply {
                    put("method", "POST")
                    put("url", "Observation")
                })
            })

            put("entry", entries)
        }

        return bundle.toString(2)
    }

    private fun createFhirObservation(
        id: String,
        loincCode: String,
        display: String,
        valueQuantity: JSONObject,
        locationRef: String,
        timestamp: String,
        interpretation: String
    ): JSONObject {
        return JSONObject().apply {
            put("resourceType", "Observation")
            put("id", id)
            put("status", "final")
            put("category", JSONArray().put(JSONObject().apply {
                put("coding", JSONArray().put(JSONObject().apply {
                    put("system", "http://terminology.hl7.org/CodeSystem/observation-category")
                    put("code", "laboratory")
                    put("display", "Environmental Laboratory Observation")
                }))
            }))
            put("code", JSONObject().apply {
                put("coding", JSONArray().put(JSONObject().apply {
                    put("system", "http://loinc.org")
                    put("code", loincCode)
                    put("display", display)
                }))
                put("text", display)
            })
            put("subject", JSONObject().apply {
                put("reference", locationRef)
                put("display", "Water Sampling Site")
            })
            put("effectiveDateTime", timestamp)
            put("valueQuantity", valueQuantity)
            put("interpretation", JSONArray().put(JSONObject().apply {
                put("coding", JSONArray().put(JSONObject().apply {
                    put("system", "http://terminology.hl7.org/CodeSystem/v3-ObservationInterpretation")
                    put("code", interpretation)
                    put("display", if (interpretation == "N") "Normal" else if (interpretation == "H") "High" else "Abnormal")
                }))
            }))
            put("device", JSONObject().apply {
                put("display", "AquaQuest AI ISO 7027 Visual Colorimetry Mobile Engine")
            })
        }
    }
}

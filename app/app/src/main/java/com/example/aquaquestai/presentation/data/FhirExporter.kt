package com.example.aquaquestai.presentation.data

import com.example.aquaquestai.presentation.screens.map.MapMarkerData
import org.json.JSONArray
import org.json.JSONObject

/**
 * Utility for exporting stream observation data into standard interoperability formats:
 * 1. GeoJSON (RFC 7946 spatial format)
 * 2. HL7 FHIR (R4 Observation Resource format for IEEE OneAquaHealth Hub)
 */
object FhirExporter {

    /**
     * Converts a list of MapMarkerData into standard GeoJSON FeatureCollection.
     */
    fun exportToGeoJson(markers: List<MapMarkerData>): String {
        val featureCollection = JSONObject()
        featureCollection.put("type", "FeatureCollection")

        val features = JSONArray()
        markers.forEach { marker ->
            val feature = JSONObject()
            feature.put("type", "Feature")

            val geometry = JSONObject()
            geometry.put("type", "Point")
            geometry.put("coordinates", JSONArray().apply {
                put(marker.lng)
                put(marker.lat)
            })
            feature.put("geometry", geometry)

            val properties = JSONObject()
            properties.put("id", marker.id)
            properties.put("title", marker.title)
            properties.put("status", marker.type.name)
            properties.put("ph", marker.ph)
            properties.put("turbidity", marker.turbidity)
            properties.put("microplasticRisk", marker.microplasticRisk)
            properties.put("author", marker.author)
            properties.put("timestamp", marker.timestamp)
            feature.put("properties", properties)

            features.put(feature)
        }

        featureCollection.put("features", features)
        return featureCollection.toString(2)
    }

    /**
     * Converts a single stream observation into an HL7 FHIR R4 Observation Resource.
     * Aligned with LOINC code 3770-8 (Water Turbidity) and SNOMED CT environmental codes.
     */
    fun exportToFhirResource(marker: MapMarkerData): String {
        val fhirObj = JSONObject()
        fhirObj.put("resourceType", "Observation")
        fhirObj.put("id", "aquaquest-obs-${marker.id}")
        fhirObj.put("status", "final")

        // Category: Environmental Health
        val categoryArr = JSONArray()
        val categoryObj = JSONObject()
        val catCodingArr = JSONArray()
        catCodingArr.put(JSONObject().apply {
            put("system", "http://terminology.hl7.org/CodeSystem/observation-category")
            put("code", "social-history")
            put("display", "Environmental OneHealth Observation")
        })
        categoryObj.put("coding", catCodingArr)
        categoryArr.put(categoryObj)
        fhirObj.put("category", categoryArr)

        // Code: Water Quality Assessment (LOINC 3770-8)
        val codeObj = JSONObject()
        val codeCodingArr = JSONArray()
        codeCodingArr.put(JSONObject().apply {
            put("system", "http://loinc.org")
            put("code", "3770-8")
            put("display", "Water Turbidity and Ecological Health")
        })
        codeObj.put("coding", codeCodingArr)
        codeObj.put("text", marker.snippet)
        fhirObj.put("code", codeObj)

        // Subject: Local Urban Stream Basin
        val subjectObj = JSONObject()
        subjectObj.put("display", marker.title)
        fhirObj.put("subject", subjectObj)

        // Effective DateTime
        fhirObj.put("effectiveDateTime", "2026-09-20T12:00:00Z")

        // Value & Interpretation
        fhirObj.put("valueString", "pH: ${marker.ph}, Turbidity: ${marker.turbidity}, Microplastics: ${marker.microplasticRisk}")

        // Component 1: Human Risk
        val componentsArr = JSONArray()
        componentsArr.put(JSONObject().apply {
            put("code", JSONObject().apply {
                put("text", "Human Health Exposure Risk")
            })
            put("valueString", if (marker.ph in 6.5..8.5) "Low Risk" else "Moderate Concern")
        })

        // Component 2: Aquatic Bio-Health
        componentsArr.put(JSONObject().apply {
            put("code", JSONObject().apply {
                put("text", "Aquatic Ecosystem Health Index")
            })
            put("valueString", marker.snippet)
        })

        fhirObj.put("component", componentsArr)
        return fhirObj.toString(2)
    }
}

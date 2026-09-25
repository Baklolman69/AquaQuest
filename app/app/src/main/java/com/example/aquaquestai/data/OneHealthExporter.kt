package com.example.aquaquestai.data

import com.example.aquaquestai.presentation.screens.map.MapMarkerData
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object OneHealthExporter {

    /**
     * Generates standard GeoJSON 1.0 FeatureCollection format
     * Compatible with QGIS, ArcGIS, and OpenStreetMap GIS hubs.
     */
    fun exportGeoJson(markers: List<MapMarkerData>): String {
        val root = JSONObject()
        root.put("type", "FeatureCollection")
        root.put("name", "AquaQuestAI_OneHealth_Stream_Observations")

        val crs = JSONObject()
        crs.put("type", "name")
        crs.put("properties", JSONObject().put("name", "urn:ogc:def:crs:OGC:1.3:CRS84"))
        root.put("crs", crs)

        val features = JSONArray()
        markers.forEach { marker ->
            val feature = JSONObject()
            feature.put("type", "Feature")

            val geometry = JSONObject()
            geometry.put("type", "Point")
            geometry.put("coordinates", JSONArray().apply {
                put(marker.lng)
                put(marker.lat)
                put(0.0) // Elevation
            })
            feature.put("geometry", geometry)

            val properties = JSONObject().apply {
                put("id", marker.id)
                put("title", marker.title)
                put("status", marker.type.name)
                put("ph_balance", marker.ph)
                put("turbidity_ntu", marker.turbidity)
                put("microplastic_risk", marker.microplasticRisk)
                put("author", marker.author)
                put("timestamp", marker.timestamp)
                put("source", "AquaQuest AI — Citizen Science Guardian")
                put("ieee_track", "OneAquaHealth Track 7")
            }
            feature.put("properties", properties)

            features.put(feature)
        }

        root.put("features", features)
        return root.toString(2)
    }

    /**
     * Generates IEEE HL7 FHIR v4.0.1 Observation Bundle resource JSON
     * Mapped to LOINC standards for digital health interoperability.
     */
    fun exportHl7FhirObservations(markers: List<MapMarkerData>): String {
        val bundle = JSONObject()
        bundle.put("resourceType", "Bundle")
        bundle.put("type", "collection")
        bundle.put("timestamp", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()))

        val entries = JSONArray()
        markers.forEach { marker ->
            val entry = JSONObject()
            entry.put("fullUrl", "urn:uuid:aquaquest-obs-${marker.id}")

            val observation = JSONObject().apply {
                put("resourceType", "Observation")
                put("id", "aquaquest-obs-${marker.id}")
                put("status", "final")

                // Category: Environmental & OneHealth
                val category = JSONArray().put(JSONObject().apply {
                    put("coding", JSONArray().put(JSONObject().apply {
                        put("system", "http://terminology.hl7.org/CodeSystem/observation-category")
                        put("code", "social-history")
                        put("display", "Environmental OneHealth Assessment")
                    }))
                })
                put("category", category)

                // LOINC Code for Environmental Water Quality
                val code = JSONObject().apply {
                    put("coding", JSONArray().put(JSONObject().apply {
                        put("system", "http://loinc.org")
                        put("code", "21612-7")
                        put("display", "pH & Turbidity Water Quality Panel")
                    }))
                    put("text", marker.title)
                }
                put("code", code)

                // Subject / Stream Sector Location
                val subject = JSONObject().apply {
                    put("display", "Urban Stream Sector Lat:${marker.lat} Lng:${marker.lng}")
                }
                put("subject", subject)

                // Component measurements (pH & Turbidity)
                val components = JSONArray().apply {
                    // Component 1: pH
                    put(JSONObject().apply {
                        put("code", JSONObject().apply {
                            put("coding", JSONArray().put(JSONObject().apply {
                                put("system", "http://loinc.org")
                                put("code", "2748-2")
                                put("display", "pH of Water")
                            }))
                        })
                        put("valueQuantity", JSONObject().apply {
                            put("value", marker.ph)
                            put("unit", "pH")
                            put("system", "http://unitsofmeasure.org")
                            put("code", "[pH]")
                        })
                    })
                    // Component 2: Microplastic Risk Text
                    put(JSONObject().apply {
                        put("code", JSONObject().apply {
                            put("coding", JSONArray().put(JSONObject().apply {
                                put("system", "http://loinc.org")
                                put("code", "48767-8")
                                put("display", "Annotation comment")
                            }))
                        })
                        put("valueString", "Microplastics: ${marker.microplasticRisk} | Turbidity: ${marker.turbidity}")
                    })
                }
                put("component", components)

                // Author note
                put("note", JSONArray().put(JSONObject().put("text", "Logged by ${marker.author} via AquaQuest AI")))
            }

            entry.put("resource", observation)
            entries.put(entry)
        }

        bundle.put("entry", entries)
        return bundle.toString(2)
    }

    /**
     * Converts AquaEvent into FHIR R4 Observation Bundle
     */
    fun exportAquaEventFhir(event: com.example.aquaquestai.domain.model.AquaEvent): String {
        val bundle = JSONObject()
        bundle.put("resourceType", "Bundle")
        bundle.put("type", "transaction")
        bundle.put("id", "aqua-event-${event.id}")
        bundle.put("timestamp", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date(event.createdAt)))

        val entries = JSONArray()
        event.evidenceList.forEach { ev ->
            val entry = JSONObject()
            entry.put("fullUrl", "urn:uuid:aqua-ev-${ev.id}")

            val obs = JSONObject().apply {
                put("resourceType", "Observation")
                put("id", ev.id)
                put("status", "final")
                put("effectiveDateTime", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date(ev.observationTimestamp)))

                put("code", JSONObject().apply {
                    put("coding", JSONArray().put(JSONObject().apply {
                        put("system", "http://loinc.org")
                        put("code", "21612-7")
                        put("display", ev.parameter)
                    }))
                    put("text", "${ev.sourceType.label}: ${ev.parameter}")
                })

                put("valueString", "${ev.value} ${ev.unit}")
                if (ev.limitations.isNotEmpty()) {
                    put("note", JSONArray().put(JSONObject().put("text", "Limitations: ${ev.limitations}")))
                }
            }

            entry.put("resource", obs)
            entries.put(entry)
        }

        bundle.put("entry", entries)
        return bundle.toString(2)
    }

    /**
     * Converts AquaEvents list into GeoJSON FeatureCollection
     */
    fun exportAquaEventsGeoJson(events: List<com.example.aquaquestai.domain.model.AquaEvent>): String {
        val root = JSONObject()
        root.put("type", "FeatureCollection")
        root.put("name", "AquaEvents_OneHealth_Stream_Intelligence")

        val features = JSONArray()
        events.forEach { event ->
            val feature = JSONObject()
            feature.put("type", "Feature")
            feature.put("geometry", JSONObject().apply {
                put("type", "Point")
                put("coordinates", JSONArray().apply {
                    put(event.longitude)
                    put(event.latitude)
                })
            })
            feature.put("properties", JSONObject().apply {
                put("id", event.id)
                put("title", event.title)
                put("status", event.status.name)
                put("location", event.locationName)
                put("evidence_count", event.evidenceList.size)
                put("independent_observers", event.verificationSummary.independentObserverCount)
                put("created_at", event.createdAt)
            })
            features.put(feature)
        }

        root.put("features", features)
        return root.toString(2)
    }
}


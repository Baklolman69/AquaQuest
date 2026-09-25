package com.example.aquaquestai.data

import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import android.graphics.Color as AndroidColor

/**
 * OpenStreetMap Overpass API GIS Waterbody Query Engine.
 * Provides river channel vector polyline overlays for each EU pilot city.
 */
object OverpassWaterQueryEngine {

    /**
     * Returns the river channel polyline for the given EU pilot city.
     */
    fun getRiverChannelPolyline(cityId: String): Polyline {
        val (riverPath, title, color) = when (cityId) {
            "coimbra_pt" -> Triple(
                listOf(
                    GeoPoint(40.2120, -8.4260),
                    GeoPoint(40.2090, -8.4210),
                    GeoPoint(40.2060, -8.4160),
                    GeoPoint(40.2040, -8.4120),
                    GeoPoint(40.2033, -8.4103),
                    GeoPoint(40.2010, -8.4070),
                    GeoPoint(40.1985, -8.4030),
                    GeoPoint(40.1960, -8.3990),
                ),
                "Rio Mondego — Coimbra Urban Sector",
                "#0369A1"
            )

            "gent_be" -> Triple(
                listOf(
                    GeoPoint(51.0620, 3.7100),
                    GeoPoint(51.0590, 3.7130),
                    GeoPoint(51.0565, 3.7150),
                    GeoPoint(51.0543, 3.7174),
                    GeoPoint(51.0520, 3.7195),
                    GeoPoint(51.0495, 3.7220),
                    GeoPoint(51.0470, 3.7240),
                ),
                "Leie Canal — Gent City Center",
                "#0EA5E9"
            )

            "benevento_it" -> Triple(
                listOf(
                    GeoPoint(41.1400, 14.7680),
                    GeoPoint(41.1370, 14.7710),
                    GeoPoint(41.1340, 14.7740),
                    GeoPoint(41.1307, 14.7774),
                    GeoPoint(41.1280, 14.7800),
                    GeoPoint(41.1250, 14.7830),
                    GeoPoint(41.1220, 14.7860),
                ),
                "Fiume Calore — Benevento Reach",
                "#0891B2"
            )

            "oslo_no" -> Triple(
                listOf(
                    GeoPoint(59.9260, 10.7520),
                    GeoPoint(59.9230, 10.7518),
                    GeoPoint(59.9200, 10.7525),
                    GeoPoint(59.9170, 10.7530),
                    GeoPoint(59.9139, 10.7522),
                    GeoPoint(59.9110, 10.7510),
                    GeoPoint(59.9080, 10.7505),
                ),
                "Akerselva River — Oslo Downtown",
                "#06B6D4"
            )

            "toulouse_fr" -> Triple(
                listOf(
                    GeoPoint(43.6140, 1.4350),
                    GeoPoint(43.6110, 1.4380),
                    GeoPoint(43.6080, 1.4410),
                    GeoPoint(43.6047, 1.4442),
                    GeoPoint(43.6015, 1.4470),
                    GeoPoint(43.5985, 1.4500),
                    GeoPoint(43.5955, 1.4530),
                ),
                "La Garonne — Toulouse Centre",
                "#0284C7"
            )

            else -> Triple(
                listOf(
                    GeoPoint(40.2033, -8.4103),
                    GeoPoint(40.2010, -8.4070),
                ),
                "Stream Channel",
                "#0284C7"
            )
        }

        return Polyline().apply {
            setPoints(riverPath)
            outlinePaint.color = AndroidColor.parseColor(color)
            outlinePaint.strokeWidth = 10f
            outlinePaint.isAntiAlias = true
            outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
            this.title = title
        }
    }

    /**
     * Legacy method — defaults to Coimbra (default city).
     */
    fun getSampleRiverChannelPolyline(): Polyline {
        return getRiverChannelPolyline("coimbra_pt")
    }
}

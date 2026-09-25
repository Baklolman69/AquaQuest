package com.example.aquaquestai.data.registry

import com.example.aquaquestai.presentation.screens.map.MapMarkerData
import com.example.aquaquestai.presentation.screens.map.MarkerType

/**
 * Pre-seeded realistic observation pins for the 5 EU OneAquaHealth Pilot Cities.
 * Each city has 4 pins along real river systems with contextually appropriate
 * water quality metrics, ensuring judges see a vibrant active ecosystem.
 */
object EuropeanCitySeedData {

    fun getMarkersForCity(cityId: String): List<MapMarkerData> = when (cityId) {

        // ── 🇵🇹 Coimbra — Mondego River Basin ──
        "coimbra_pt" -> listOf(
            MapMarkerData(
                id = 101, lat = 40.2110, lng = -8.4290,
                title = "Mondego River — Sector A",
                snippet = "Water Clarity: 6.2/10 — Moderate Algae",
                type = MarkerType.WARNING,
                ph = 8.1, turbidity = "12.0 NTU",
                microplasticRisk = "Low (1.2/10)",
                author = "Mondego_Scout", timestamp = "2 hours ago"
            ),
            MapMarkerData(
                id = 102, lat = 40.2045, lng = -8.4150,
                title = "Parque Verde Outflow",
                snippet = "Water Clarity: 8.8/10 — Crystal Clear",
                type = MarkerType.HEALTHY,
                ph = 7.3, turbidity = "1.8 NTU",
                microplasticRisk = "Low (0.5/10)",
                author = "AquaRanger_PT", timestamp = "30 mins ago"
            ),
            MapMarkerData(
                id = 103, lat = 40.1980, lng = -8.4050,
                title = "⚠️ Cyanobacteria Alert",
                snippet = "Risk Score: 7.8/10 — Blue-Green Film",
                type = MarkerType.ALERT,
                ph = 8.9, turbidity = "15.2 NTU",
                microplasticRisk = "Moderate (4.1/10)",
                author = "EcoGuard_Coimbra", timestamp = "1 hour ago"
            ),
            MapMarkerData(
                id = 104, lat = 40.2065, lng = -8.4200,
                title = "🟣 Verify Algal Bloom",
                snippet = "Quest: Verification needed (+100 XP)",
                type = MarkerType.QUEST,
                ph = 8.4, turbidity = "9.5 NTU",
                microplasticRisk = "Pending Verification",
                author = "System Alert", timestamp = "45 mins ago"
            ),
        )

        // ── 🇧🇪 Gent — Scheldt & Leie Canals ──
        "gent_be" -> listOf(
            MapMarkerData(
                id = 201, lat = 51.0570, lng = 3.7250,
                title = "Leie Canal — Graslei",
                snippet = "Water Clarity: 5.5/10 — Oil Micro-Film",
                type = MarkerType.WARNING,
                ph = 6.9, turbidity = "6.4 NTU",
                microplasticRisk = "High (7.2/10)",
                author = "GentWatcher", timestamp = "1 hour ago"
            ),
            MapMarkerData(
                id = 202, lat = 51.0520, lng = 3.7100,
                title = "Scheldt Junction Point",
                snippet = "Water Clarity: 7.1/10 — Moderate",
                type = MarkerType.HEALTHY,
                ph = 7.2, turbidity = "3.1 NTU",
                microplasticRisk = "Moderate (3.8/10)",
                author = "BelgianScout", timestamp = "3 hours ago"
            ),
            MapMarkerData(
                id = 203, lat = 51.0495, lng = 3.7300,
                title = "🔴 Hydrocarbon Sheen",
                snippet = "Risk Score: 8.1/10 — Petroleum Film",
                type = MarkerType.ALERT,
                ph = 5.8, turbidity = "14.8 NTU",
                microplasticRisk = "High (8.9/10)",
                author = "Canal_Defender", timestamp = "45 mins ago"
            ),
            MapMarkerData(
                id = 204, lat = 51.0550, lng = 3.7180,
                title = "🟣 Verify Plastic Debris",
                snippet = "Quest: Photo verification (+100 XP)",
                type = MarkerType.QUEST,
                ph = 7.0, turbidity = "4.2 NTU",
                microplasticRisk = "Pending Verification",
                author = "System Alert", timestamp = "20 mins ago"
            ),
        )

        // ── 🇮🇹 Benevento — Calore & Sabato Rivers ──
        "benevento_it" -> listOf(
            MapMarkerData(
                id = 301, lat = 41.1340, lng = 14.7700,
                title = "Calore River — Centro",
                snippet = "Water Clarity: 3.2/10 — Heavy Sediment",
                type = MarkerType.ALERT,
                ph = 6.2, turbidity = "42.0 NTU",
                microplasticRisk = "Moderate (5.0/10)",
                author = "RiverGuard_IT", timestamp = "2 hours ago"
            ),
            MapMarkerData(
                id = 302, lat = 41.1280, lng = 14.7830,
                title = "Sabato River Confluence",
                snippet = "Water Clarity: 5.8/10 — Moderate Silt",
                type = MarkerType.WARNING,
                ph = 7.0, turbidity = "8.5 NTU",
                microplasticRisk = "Low (2.1/10)",
                author = "Benevento_Eco", timestamp = "4 hours ago"
            ),
            MapMarkerData(
                id = 303, lat = 41.1370, lng = 14.7850,
                title = "Agricultural Runoff Zone",
                snippet = "Water Clarity: 7.4/10 — Improving",
                type = MarkerType.HEALTHY,
                ph = 7.4, turbidity = "3.8 NTU",
                microplasticRisk = "Low (1.5/10)",
                author = "FarmWatch", timestamp = "Yesterday"
            ),
            MapMarkerData(
                id = 304, lat = 41.1310, lng = 14.7750,
                title = "🟣 Verify Silt Anomaly",
                snippet = "Quest: Ground check needed (+100 XP)",
                type = MarkerType.QUEST,
                ph = 6.5, turbidity = "18.0 NTU",
                microplasticRisk = "Pending Verification",
                author = "System Alert", timestamp = "1 hour ago"
            ),
        )

        // ── 🇳🇴 Oslo — Akerselva River ──
        "oslo_no" -> listOf(
            MapMarkerData(
                id = 401, lat = 59.9200, lng = 10.7520,
                title = "Akerselva — Grünerløkka",
                snippet = "Water Clarity: 9.4/10 — Pristine",
                type = MarkerType.HEALTHY,
                ph = 7.5, turbidity = "0.8 NTU",
                microplasticRisk = "Low (0.2/10)",
                author = "NordicScout", timestamp = "1 hour ago"
            ),
            MapMarkerData(
                id = 402, lat = 59.9160, lng = 10.7560,
                title = "Akerselva Waterfall Pool",
                snippet = "Water Clarity: 8.9/10 — Clear Cold Flow",
                type = MarkerType.HEALTHY,
                ph = 7.2, turbidity = "1.2 NTU",
                microplasticRisk = "Low (0.3/10)",
                author = "Oslo_EcoTeam", timestamp = "3 hours ago"
            ),
            MapMarkerData(
                id = 403, lat = 59.9100, lng = 10.7490,
                title = "Storm Drain Outlet C",
                snippet = "Water Clarity: 5.8/10 — Post-Rain Runoff",
                type = MarkerType.WARNING,
                ph = 6.8, turbidity = "7.2 NTU",
                microplasticRisk = "Moderate (3.5/10)",
                author = "RunoffAlert", timestamp = "5 hours ago"
            ),
            MapMarkerData(
                id = 404, lat = 59.9070, lng = 10.7540,
                title = "🟣 Verify Snowmelt Surge",
                snippet = "Quest: Verification needed (+100 XP)",
                type = MarkerType.QUEST,
                ph = 7.0, turbidity = "5.5 NTU",
                microplasticRisk = "Pending Verification",
                author = "System Alert", timestamp = "2 hours ago"
            ),
        )

        // ── 🇫🇷 Toulouse — Garonne & Touch Streams ──
        "toulouse_fr" -> listOf(
            MapMarkerData(
                id = 501, lat = 43.6060, lng = 1.4400,
                title = "Garonne — Pont Neuf Sector",
                snippet = "Water Clarity: 6.5/10 — Seasonal Turbidity",
                type = MarkerType.WARNING,
                ph = 7.6, turbidity = "5.8 NTU",
                microplasticRisk = "Moderate (4.0/10)",
                author = "Toulouse_Guard", timestamp = "2 hours ago"
            ),
            MapMarkerData(
                id = 502, lat = 43.6010, lng = 1.4480,
                title = "Touch Stream — East Bank",
                snippet = "Water Clarity: 4.2/10 — Drought Stagnation",
                type = MarkerType.ALERT,
                ph = 8.2, turbidity = "11.5 NTU",
                microplasticRisk = "Moderate (5.5/10)",
                author = "Stream_Sentinel", timestamp = "1 hour ago"
            ),
            MapMarkerData(
                id = 503, lat = 43.6100, lng = 1.4350,
                title = "Canal du Midi Junction",
                snippet = "Water Clarity: 8.0/10 — Clear Canal Flow",
                type = MarkerType.HEALTHY,
                ph = 7.3, turbidity = "2.0 NTU",
                microplasticRisk = "Low (1.0/10)",
                author = "CanalWatch_FR", timestamp = "4 hours ago"
            ),
            MapMarkerData(
                id = 504, lat = 43.6030, lng = 1.4520,
                title = "🟣 Verify Mosquito Habitat",
                snippet = "Quest: Vector risk check (+100 XP)",
                type = MarkerType.QUEST,
                ph = 8.0, turbidity = "8.0 NTU",
                microplasticRisk = "Pending Verification",
                author = "System Alert", timestamp = "30 mins ago"
            ),
        )

        else -> emptyList()
    }
}

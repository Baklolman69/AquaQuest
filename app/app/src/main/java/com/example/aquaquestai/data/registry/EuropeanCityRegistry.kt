package com.example.aquaquestai.data.registry

import com.example.aquaquestai.data.model.EuropeanCity

object EuropeanCityRegistry {
    val PILOT_CITIES = listOf(
        EuropeanCity(
            id = "coimbra_pt",
            name = "Coimbra",
            country = "Portugal",
            countryFlag = "🇵🇹",
            riverSystem = "Mondego River Basin",
            latitude = 40.2033,
            longitude = -8.4103,
            description = "Main research hub of OneAquaHealth monitoring urban stream warming & pharmaceutical runoff.",
            primaryEcologicalStressor = "Cyanobacteria / Algal Blooms & Summer Stream Warming",
            baselineWfdStatus = "Moderate",
            heroImageUrl = "https://cdn.britannica.com/18/150518-050-973E4D60/Mondego-River-Penacova-Port.jpg"
        ),
        EuropeanCity(
            id = "gent_be",
            name = "Gent",
            country = "Belgium",
            countryFlag = "🇧🇪",
            riverSystem = "Scheldt & Leie Canals",
            latitude = 51.0543,
            longitude = 3.7174,
            description = "Urbanized canal system dealing with industrial runoff and micro-plastic accumulation.",
            primaryEcologicalStressor = "Urban Heat Island & Micro-Plastic Runoff",
            baselineWfdStatus = "Moderate",
            heroImageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSeedHGRYHOdDClijkZ5jh45aglAopJCE5FawvUImRw1od898O7H07Txgay&s=10"
        ),
        EuropeanCity(
            id = "benevento_it",
            name = "Benevento",
            country = "Italy",
            countryFlag = "🇮🇹",
            riverSystem = "Calore & Sabato Rivers",
            latitude = 41.1307,
            longitude = 14.7774,
            description = "Agricultural & urban stream junction suffering from seasonal sediment turbidity.",
            primaryEcologicalStressor = "Agricultural Runoff & Macroinvertebrate Loss",
            baselineWfdStatus = "Poor",
            heroImageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0b/Valle_del_Calore_e_ponte_della_SP152_tra_Foglianise_e_Benevento%2C_vista_da_Castelpoto.jpg/960px-Valle_del_Calore_e_ponte_della_SP152_tra_Foglianise_e_Benevento%2C_vista_da_Castelpoto.jpg.webp"
        ),
        EuropeanCity(
            id = "oslo_no",
            name = "Oslo",
            country = "Norway",
            countryFlag = "🇳🇴",
            riverSystem = "Akerselva River",
            latitude = 59.9139,
            longitude = 10.7522,
            description = "Sub-arctic urban freshwater river monitoring storm runoff surges & cold-water bio-indicators.",
            primaryEcologicalStressor = "Storm Runoff Surges & Snowmelt Pollutants",
            baselineWfdStatus = "Good",
            heroImageUrl = "https://visitlokka.no/wp-content/uploads/2023/03/IMG_9215.jpeg"
        ),
        EuropeanCity(
            id = "toulouse_fr",
            name = "Toulouse",
            country = "France",
            countryFlag = "🇫🇷",
            riverSystem = "Garonne & Touch Streams",
            latitude = 43.6047,
            longitude = 1.4442,
            description = "Southern European river network facing extreme summer drought stream drying & vector-borne risk.",
            primaryEcologicalStressor = "Drought Stream Drying & Vector Mosquito Alerts",
            baselineWfdStatus = "Moderate",
            heroImageUrl = "https://s3.eu-west-1.amazonaws.com/ryo-prod-public-398019065442/large_public_visiter_toulouse_3_jours_body_1_38748d8b38.jpg"
        )
    )

    fun getDefaultCity(): EuropeanCity = PILOT_CITIES.first()

    fun getCityById(id: String): EuropeanCity? = PILOT_CITIES.find { it.id == id }
}

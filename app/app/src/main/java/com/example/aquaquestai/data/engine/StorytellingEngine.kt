package com.example.aquaquestai.data.engine

import com.example.aquaquestai.data.remote.ItunesAudioTrack

/**
 * Storytelling & Awareness Engine for Track 4 (Awareness & Storytelling).
 * Provides interactive eco-tales, EU pilot city transformation narratives,
 * OneHealth micro-academy quizzes, iTunes audio preview integration, and social story cards.
 */

data class BioIndicatorStory(
    val id: String,
    val title: String,
    val narratorName: String,
    val narratorSpecies: String,
    val narratorEmoji: String,
    val narratorQuoteBubble: String,
    val targetStream: String,
    val clarityNtu: Double,
    val ph: Double,
    val summary: String,
    val fullNarrative: String,
    val keyTakeaway: String,
    val audioDurationSeconds: Int = 45,
    val oneHealthAngle: String,
    val itunesSearchQuery: String,
    var itunesTrack: ItunesAudioTrack? = null,
    val interactiveChoicePrompt: String,
    val choiceOptions: List<String>,
    val correctChoiceIndex: Int,
    val choiceOutcomeText: String,
)

data class EuPilotCityTale(
    val cityId: String,
    val cityName: String,
    val countryFlag: String,
    val riverBasin: String,
    val ecoStatus: String, // "High", "Good", "Moderate", "Poor", "Bad"
    val mainChallenge: String,
    val transformationStory: String,
    val citizenImpactTip: String,
    val wfdMeasureCode: String,
    val wfdMeasureDescription: String,
    val historicalTimelineYear: String,
    val restorationGoal: String,
)

data class QuizQuestion(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String,
)

data class AcademyModule(
    val id: String,
    val title: String,
    val category: String, // "One Health Triad", "Water Clarity", "Pet Safety", "EU WFD Standards"
    val icon: String,
    val readTimeMinutes: Int,
    val contentSummary: String,
    val fullBodyText: String,
    val quiz: QuizQuestion,
    val xpReward: Int = 50,
)

data class SocialStoryCard(
    val id: String,
    val headline: String,
    val streamName: String,
    val statusBadge: String,
    val petSafetyStatus: String,
    val ecoMetricHighlight: String,
    val actionableCallToAction: String,
    val formattedShareText: String,
)

object StorytellingEngine {

    // 1. First-Person Bio-Indicator Eco-Tales with iTunes Audio & Interactive Choices
    fun getBioIndicatorStories(): List<BioIndicatorStory> {
        return listOf(
            BioIndicatorStory(
                id = "story-mayfly-01",
                title = "Tales of Mayfly Maya: Clear Waters, Safe Futures",
                narratorName = "Maya",
                narratorSpecies = "Ephemeroptera (Mayfly Nymph)",
                narratorEmoji = "🦋",
                narratorQuoteBubble = "“My delicate external gills need crystal-clear water (< 1.0 NTU) to filter oxygen for my river family!”",
                targetStream = "Colorado River Sector 4",
                clarityNtu = 0.8,
                ph = 7.4,
                summary = "Why crystal clarity (< 1.0 NTU) allows delicate gills to breathe and thrive.",
                fullNarrative = "Hi there! I'm Maya, a tiny mayfly nymph living under the river pebbles. When the stream clarity stays below 1.0 NTU, my delicate external gills can filter oxygen effortlessly. But when storm drains flush heavy silt into our river, my home gets buried under mud. By logging water clarity with AquaQuest AI, citizen stream guardians keep our stream oxygen-rich and clean!",
                keyTakeaway = "Mayfly nymphs are sensitive biological indicators: their presence signals pristine water quality and high dissolved oxygen.",
                audioDurationSeconds = 48,
                oneHealthAngle = "High stream macroinvertebrate biodiversity prevents toxic pathogen overgrowth, directly safeguarding public swimming areas.",
                itunesSearchQuery = "gentle mountain stream water nature sounds",
                interactiveChoicePrompt = "Maya notices silt runoff approaching from a nearby construction site. What should Stream Guardian Alex do?",
                choiceOptions = listOf(
                    "Log a 📸 CameraX Turbidity Scan to alert municipal inspectors.",
                    "Ignore it and wait for heavy rain to wash the silt away."
                ),
                correctChoiceIndex = 0,
                choiceOutcomeText = "🎉 Excellent Guardian Choice! Logging the turbidity anomaly triggers a 2km geofenced verification quest, preventing sediment damage to Maya's gravel bed."
            ),
            BioIndicatorStory(
                id = "story-otter-02",
                title = "Otter Ollie's Quest: Navigating Urban Runoff",
                narratorName = "Ollie",
                narratorSpecies = "Lutra lutra (River Otter)",
                narratorEmoji = "🦦",
                narratorQuoteBubble = "“Swirling rainbows on the water aren't pretty—they're petroleum oil film! Help us keep Rio Manzanares clean.”",
                targetStream = "Rio Manzanares Basin",
                clarityNtu = 2.4,
                ph = 7.1,
                summary = "How urban stream bank vegetation shields aquatic predators from heavy metals and oil sheen.",
                fullNarrative = "Greetings from Rio Manzanares! As a river otter, I rely on clear water to dive for fish. Last summer, oil sheen runoff from nearby parking lots coated our reed beds. Thanks to AquaQuest stream scouts who logged the anomaly, local authorities installed bio-swale filters. Now the fish are back, and my family is thriving!",
                keyTakeaway = "Riparian vegetation buffers act as natural bio-filters, trapping 80% of urban hydrocarbon runoff before it reaches river otters.",
                audioDurationSeconds = 52,
                oneHealthAngle = "Healthy otter habitats indicate low chemical toxicity in the food chain, protecting surrounding community groundwater.",
                itunesSearchQuery = "river water ambient nature sounds",
                interactiveChoicePrompt = "Ollie sees an iridescent sheen near the storm drain outlet. How do you test if it's natural bio-film or petroleum?",
                choiceOptions = listOf(
                    "Poke with a stick: natural bio-films break into sharp plates; oil sheens swirl and rejoin.",
                    "Taste the water to check for fuel flavor."
                ),
                correctChoiceIndex = 0,
                choiceOutcomeText = "🎉 Spot on! Poking with a stick safely differentiates natural iron bacteria bio-films from petroleum contamination without contact."
            ),
            BioIndicatorStory(
                id = "story-trout-03",
                title = "Tina the Trout & The Cyanobacteria Shield",
                narratorName = "Tina",
                narratorSpecies = "Salmo trutta (Brown Trout)",
                narratorEmoji = "🐟",
                narratorQuoteBubble = "“When stream temperature rises above 18°C, green cyanobacteria blooms appear overnight. Keep riverbanks shaded!”",
                targetStream = "Akerselva Cold Water Sector",
                clarityNtu = 1.2,
                ph = 7.6,
                summary = "Understanding how summer warming and nitrogen runoff trigger dangerous cyanobacteria microcystins.",
                fullNarrative = "Cold mountain streams are my paradise! When stream temperatures rise above 18°C combined with lawn fertilizer runoff, green cyanobacteria blooms can appear overnight. These algae release microcystin toxins that hurt fish gills and make water dangerous for neighborhood dogs. Keeping shade trees along riverbanks keeps the water cool and algae-free!",
                keyTakeaway = "Overhanging tree canopies suppress cyanobacteria blooms by reducing direct solar heating of urban stream beds.",
                audioDurationSeconds = 50,
                oneHealthAngle = "Preventing algal blooms protects domestic pets from microcystin poisoning and prevents municipal water supply taste/odor degradation.",
                itunesSearchQuery = "cold mountain river water sounds",
                interactiveChoicePrompt = "Tina reports green scum forming near the sunny river bend. What community action helps most?",
                choiceOptions = listOf(
                    "Plant native willow and alder trees along the southern stream bank for natural shading.",
                    "Pour chlorine bleach directly into the stream to kill the green scum."
                ),
                correctChoiceIndex = 0,
                choiceOutcomeText = "🎉 Perfect Ecological Solution! Planting overhanging trees lowers stream temperature below 18°C, suppressing cyanobacteria microcystin blooms naturally."
            )
        )
    }

    // 2. European Pilot Cities Transformation Tales
    fun getEuPilotCityTales(): List<EuPilotCityTale> {
        return listOf(
            EuPilotCityTale(
                cityId = "coimbra",
                cityName = "Coimbra",
                countryFlag = "🇵🇹",
                riverBasin = "Mondego River Basin",
                ecoStatus = "Good",
                mainChallenge = "Summer water warming & cyanobacteria bloom alerts along urban embankments.",
                transformationStory = "The Mondego River in Coimbra is a vital ecological lifeline. Through community macroinvertebrate sampling and satellite water temperature tracking, citizens helped identify stagnant warming sectors early.",
                citizenImpactTip = "Inspect river bank shallows for greenish scum or foam during dry summer weeks and log photos via AI Scan.",
                wfdMeasureCode = "EU Measure #14",
                wfdMeasureDescription = "Riparian buffer strip restoration and river bank shading to control water thermal spikes.",
                historicalTimelineYear = "2024 - 2026 EU Horizon Pilot",
                restorationGoal = "Achieve 100% High Ecological Status across Mondego urban reach by 2027."
            ),
            EuPilotCityTale(
                cityId = "gent",
                cityName = "Gent",
                countryFlag = "🇧🇪",
                riverBasin = "Scheldt & Leie Canals",
                ecoStatus = "Moderate",
                mainChallenge = "Urban heat island runoff and microplastic accumulation in historic canals.",
                transformationStory = "Gent's urban waterways connect ancient canals with dense urban quarters. Citizen scouts deployed foam and litter traps, mapping plastics with GeoJSON coordinates.",
                citizenImpactTip = "Participate in local weekend canal clean-ups and submit verified photo quests to earn Stream Scout badges.",
                wfdMeasureCode = "EU Measure #22",
                wfdMeasureDescription = "Constructed urban wetland bio-filters and storm outflow microplastic retention gates.",
                historicalTimelineYear = "2023 - 2026 Canal Bio-Filter Program",
                restorationGoal = "Reduce canal microplastic transport into the Scheldt estuary by 65%."
            ),
            EuPilotCityTale(
                cityId = "benevento",
                cityName = "Benevento",
                countryFlag = "🇮🇹",
                riverBasin = "Calore & Sabato Rivers",
                ecoStatus = "Moderate",
                mainChallenge = "Agricultural nutrient wash-off & seasonal benthic siltation.",
                transformationStory = "Surrounded by vineyards and agriculture, Benevento's rivers face periodic sediment spikes. Farmers and citizen scientists collaborated using USGS and local sensor telemetry to time irrigation.",
                citizenImpactTip = "Check turbidity levels after heavy rainfall to ensure agricultural topsoil retention measures are working.",
                wfdMeasureCode = "EU Measure #08",
                wfdMeasureDescription = "Agricultural buffer zones and no-till riverbank soil stabilization.",
                historicalTimelineYear = "2025 Calore Basin Restoration",
                restorationGoal = "Stabilize agricultural topsoil and restore benthic macroinvertebrate nurseries."
            ),
            EuPilotCityTale(
                cityId = "oslo",
                cityName = "Oslo",
                countryFlag = "🇳🇴",
                riverBasin = "Akerselva River",
                ecoStatus = "High",
                mainChallenge = "Sub-arctic cold-water salmonid habitat protection during winter thaws.",
                transformationStory = "Akerselva runs straight through Oslo into the fjord. Citizen stream guardians maintain year-round monitoring of water acidity and dissolved oxygen for spawning wild Atlantic salmon.",
                citizenImpactTip = "Log stream temperature and ice cover observations during winter thaw cycles.",
                wfdMeasureCode = "EU Measure #03",
                wfdMeasureDescription = "Fish passage ladder maintenance and environmental stream flow guarantees.",
                historicalTimelineYear = "2022 - 2026 Cold Stream Sanctuary",
                restorationGoal = "Maintain 9.5+ mg/L dissolved oxygen for wild Atlantic salmon spawning."
            ),
            EuPilotCityTale(
                cityId = "toulouse",
                cityName = "Toulouse",
                countryFlag = "🇫🇷",
                riverBasin = "Garonne & Touch Streams",
                ecoStatus = "Good",
                mainChallenge = "Summer low-flow drying & vector-borne mosquito risk (Aedes albopictus).",
                transformationStory = "In Toulouse, summer droughts cause small tributaries like the Touch to form isolated stagnant pools. Citizens track stagnant pockets, helping city health teams treat mosquito breeding sites safely.",
                citizenImpactTip = "Map isolated stagnant puddle clusters during July-August to assist eco-friendly biological vector control.",
                wfdMeasureCode = "EU Measure #19",
                wfdMeasureDescription = "Ecological low-flow augmentation and natural river corridor re-wetting.",
                historicalTimelineYear = "2024 Drought Resilience Plan",
                restorationGoal = "Eliminate stagnant toxic pools and maintain continuous eco-flow."
            )
        )
    }

    // 3. OneHealth Micro-Academy Modules & Interactive Quizzes
    fun getAcademyModules(): List<AcademyModule> {
        return listOf(
            AcademyModule(
                id = "mod-onehealth-101",
                title = "The One Health Triad: Connecting Water, Animals & Humans",
                category = "One Health Triad",
                icon = "🌐",
                readTimeMinutes = 2,
                contentSummary = "Discover how environmental stream health directly controls pet safety and human disease prevention.",
                fullBodyText = "The One Health approach recognizes that human health, animal health, and ecosystem viability are inextricably linked. Urban rivers act as environmental barometers: when stream biodiversity drops or chemical contamination spikes, domestic pets drinking at the bank and children playing downstream face immediate health risks. Monitoring water clarity, pH, and bio-indicators creates a early warning net for the entire community.",
                quiz = QuizQuestion(
                    id = "q-onehealth-101",
                    questionText = "What core concept defines the 'One Health' approach?",
                    options = listOf(
                        "Focusing exclusively on hospital medical technology.",
                        "The interconnection between human health, animal health, and environmental ecosystems.",
                        "Monitoring only tap water in municipal purification plants.",
                        "Filtering all river water through chemical treatment facilities."
                    ),
                    correctOptionIndex = 1,
                    explanation = "One Health unites human health, animal health, and ecosystem stewardship into a single holistic strategy!"
                )
            ),
            AcademyModule(
                id = "mod-turbidity-102",
                title = "De-coding Water Clarity (NTU) & Pet Safety",
                category = "Water Clarity",
                icon = "💧",
                readTimeMinutes = 3,
                contentSummary = "Learn why high Nephelometric Turbidity Units (NTU) signal microcystin risks for dogs.",
                fullBodyText = "Turbidity measures how cloudy or murky water is, expressed in NTU. Drinking tap water is typically < 0.5 NTU. Pristine mountain streams measure < 2.0 NTU. When urban runoff causes turbidity to exceed 10.0 NTU, light penetration drops, submerged plants die, and toxic cyanobacteria (blue-green algae) can bloom. Dogs swimming in or drinking turbid algal water can ingest lethal microcystins.",
                quiz = QuizQuestion(
                    id = "q-turbidity-102",
                    questionText = "Why should dog owners avoid letting pets drink from streams with high green turbidity (> 10 NTU)?",
                    options = listOf(
                        "High turbidity means the water is too cold.",
                        "Murky green water can contain microcystin toxins produced by algal blooms.",
                        "Dogs prefer salt water over freshwater streams.",
                        "Turbid water has too much oxygen for mammals."
                    ),
                    correctOptionIndex = 1,
                    explanation = "High turbidity combined with greenish algae can harbor microcystins, which are liver toxins dangerous to pets!"
                )
            ),
            AcademyModule(
                id = "mod-wfd-103",
                title = "EU Water Framework Directive (WFD 2000/60/EC) Made Simple",
                category = "EU WFD Standards",
                icon = "🇪🇺",
                readTimeMinutes = 3,
                contentSummary = "Understand how Europe classifies river health into 5 ecological status tiers.",
                fullBodyText = "The EU Water Framework Directive (2000/60/EC) sets a mandatory goal for all European water bodies to achieve 'Good Ecological Status'. Rivers are classified into 5 color-coded tiers: High (Blue), Good (Green), Moderate (Yellow), Poor (Orange), and Bad (Red). Classification considers biological quality (fish & macroinvertebrates), hydromorphology (stream bank naturalness), and general physicochemical metrics (pH & dissolved oxygen).",
                quiz = QuizQuestion(
                    id = "q-wfd-103",
                    questionText = "Under the EU Water Framework Directive, what ecological status is represented by the color GREEN?",
                    options = listOf(
                        "High Ecological Status",
                        "Good Ecological Status",
                        "Moderate Ecological Status",
                        "Bad Ecological Status"
                    ),
                    correctOptionIndex = 1,
                    explanation = "Green represents 'Good Ecological Status', the standard goal for all urban rivers under EU directive 2000/60/EC!"
                )
            )
        )
    }

    // 4. Social Story Cards Generator
    fun generateSocialStoryCard(
        streamName: String,
        clarityNtu: Double,
        ph: Double,
        isPetSafe: Boolean,
        observerName: String
    ): SocialStoryCard {
        val statusBadge = if (clarityNtu < 3.0 && ph in 6.5..8.5) "🟢 HEALTHY STREAM" else "⚠️ ANOMALY ALERT"
        val petSafety = if (isPetSafe) "🐶 SAFE FOR PETS" else "⚠️ KEEP PETS ON LEASH"
        val headline = if (isPetSafe) "Pristine Stream Health Reported at $streamName!" else "Caution: Water Anomaly Detected at $streamName"
        val ecoHighlight = "Water Clarity: $clarityNtu NTU • Acidity: pH $ph"
        val cta = "Join $observerName on AquaQuest AI — Stream Guardian Citizen Science!"

        val formattedText = """
            🌊 AquaQuest OneHealth Story Card 🌊
            📍 Location: $streamName
            STATUS: $statusBadge
            PET SAFETY: $petSafety
            💧 Metrics: $ecoHighlight
            
            "$headline"
            
            $cta
            #OneAquaHealth #CitizenScience #OneHealth #IEEE #CleanWater
        """.trimIndent()

        return SocialStoryCard(
            id = "card-${System.currentTimeMillis()}",
            headline = headline,
            streamName = streamName,
            statusBadge = statusBadge,
            petSafetyStatus = petSafety,
            ecoMetricHighlight = ecoHighlight,
            actionableCallToAction = cta,
            formattedShareText = formattedText
        )
    }
}

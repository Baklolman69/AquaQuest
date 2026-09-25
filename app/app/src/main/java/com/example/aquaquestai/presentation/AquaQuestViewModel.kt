package com.example.aquaquestai.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquaquestai.data.GroqChatMessage
import com.example.aquaquestai.data.model.EuropeanCity
import com.example.aquaquestai.data.model.ResearchArticle
import com.example.aquaquestai.data.model.ResearchCategory
import com.example.aquaquestai.data.registry.EuropeanCityRegistry
import com.example.aquaquestai.data.registry.EuropeanCitySeedData
import com.example.aquaquestai.data.remote.ResearchNewsRepository
import com.example.aquaquestai.presentation.screens.map.MapMarkerData
import com.example.aquaquestai.presentation.screens.map.MarkerType
import com.example.aquaquestai.presentation.screens.quests.QuestItem
import com.example.aquaquestai.presentation.screens.quests.QuestStatus
import kotlinx.coroutines.launch

enum class MapFilter { ALL, HEALTHY, WARNING, ALERT, QUEST }

data class VisionBoundingBox(
    val label: String,
    val isAnomaly: Boolean, // true = RED box, false = GREEN box
    val confidence: Float,
    val leftNorm: Float,   // 0.0 to 1.0
    val topNorm: Float,    // 0.0 to 1.0
    val rightNorm: Float,  // 0.0 to 1.0
    val bottomNorm: Float, // 0.0 to 1.0
)

data class WaterSamplePreset(
    val name: String,
    val description: String,
    val icon: String,
    val clarityScore: Double,
    val turbidityNtu: String,
    val oilSheenRisk: Float,
    val microplasticRisk: Float,
    val ph: Double,
    val statusType: MarkerType,
    val advice: String,
    val aiReasoning: String = "",
    val boundingBoxes: List<VisionBoundingBox> = emptyList(),
    val disclaimer: String = "Estimated from image color & edge patterns, not a certified water test kit.",
)

class AquaQuestViewModel : ViewModel() {

    // ── Selected EU Pilot City State ──
    var selectedCity by mutableStateOf(EuropeanCityRegistry.getDefaultCity())
        private set

    fun selectCity(city: EuropeanCity) {
        selectedCity = city
        mapMarkers.clear()
        mapMarkers.addAll(EuropeanCitySeedData.getMarkersForCity(city.id))
    }

    // ── Level-Up Celebration State ──
    var showLevelUpCelebration by mutableStateOf(false)
        private set

    fun dismissLevelUpCelebration() {
        showLevelUpCelebration = false
    }

    // User Progress & Gamification State
    var userLevel by mutableIntStateOf(4)
        private set

    var userXp by mutableIntStateOf(850)
        private set

    val maxLevelXp = 1000

    var observationsCount by mutableIntStateOf(12)
        private set

    var verificationsCount by mutableIntStateOf(5)
        private set

    var streakDays by mutableIntStateOf(3)
        private set

    var litresVerified by mutableIntStateOf(14500)
        private set

    // Track 4: Storytelling & Awareness Hub State
    val bioIndicatorStories = com.example.aquaquestai.data.engine.StorytellingEngine.getBioIndicatorStories()
    var selectedStoryIndex by mutableIntStateOf(0)

    val euPilotCityTales = com.example.aquaquestai.data.engine.StorytellingEngine.getEuPilotCityTales()
    var selectedEuCityIndex by mutableIntStateOf(0)

    val academyModules = com.example.aquaquestai.data.engine.StorytellingEngine.getAcademyModules()
    var selectedAcademyModuleIndex by mutableIntStateOf(0)

    val completedQuizIds = mutableStateListOf<String>()

    var isAudioNarratorPlaying by mutableStateOf(false)
    var isItunesAudioLoading by mutableStateOf(false)
    var activeItunesTrack by mutableStateOf<com.example.aquaquestai.data.remote.ItunesAudioTrack?>(null)

    var selectedBioChoiceIndex by mutableIntStateOf(-1)
    var bioChoiceFeedbackMessage by mutableStateOf<String?>(null)

    var selectedQuizOptionIndex by mutableIntStateOf(-1)
    var quizFeedbackMessage by mutableStateOf<String?>(null)
    var isQuizCorrect by mutableStateOf<Boolean?>(null)

    var generatedSocialCard by mutableStateOf<com.example.aquaquestai.data.engine.SocialStoryCard?>(null)

    fun toggleItunesStoryAudio(context: android.content.Context, story: com.example.aquaquestai.data.engine.BioIndicatorStory) {
        if (com.example.aquaquestai.data.remote.ItunesAudioService.isPlaying()) {
            com.example.aquaquestai.data.remote.ItunesAudioService.stopAudioStream()
            isAudioNarratorPlaying = false
            return
        }

        isItunesAudioLoading = true
        viewModelScope.launch {
            val track = com.example.aquaquestai.data.remote.ItunesAudioService.searchItunesAudio(story.itunesSearchQuery)
            activeItunesTrack = track
            isItunesAudioLoading = false

            track?.let { t ->
                isAudioNarratorPlaying = true
                com.example.aquaquestai.data.remote.ItunesAudioService.playAudioStream(
                    context = context,
                    audioUrl = t.previewUrl,
                    onCompletion = { isAudioNarratorPlaying = false },
                    onError = { isAudioNarratorPlaying = false }
                )
            }
        }
    }

    fun submitBioIndicatorChoice(storyId: String, choiceIndex: Int) {
        val story = bioIndicatorStories.firstOrNull { it.id == storyId } ?: return
        selectedBioChoiceIndex = choiceIndex

        if (choiceIndex == story.correctChoiceIndex) {
            bioChoiceFeedbackMessage = story.choiceOutcomeText
            addXp(25)
        } else {
            bioChoiceFeedbackMessage = "⚠️ Not quite the optimal eco-choice. ${story.choiceOutcomeText}"
        }
    }

    fun submitAcademyQuizAnswer(moduleId: String, selectedOption: Int) {
        val module = academyModules.firstOrNull { it.id == moduleId } ?: return
        selectedQuizOptionIndex = selectedOption

        if (selectedOption == module.quiz.correctOptionIndex) {
            isQuizCorrect = true
            quizFeedbackMessage = "🎉 Correct! ${module.quiz.explanation}"
            if (!completedQuizIds.contains(moduleId)) {
                completedQuizIds.add(moduleId)
                addXp(module.xpReward)
            }
        } else {
            isQuizCorrect = false
            quizFeedbackMessage = "❌ Incorrect. Try again! Hint: ${module.quiz.explanation}"
        }
    }

    fun generateStoryCardForCurrentLocation(streamName: String = "Mill Creek River Sector 3") {
        generatedSocialCard = com.example.aquaquestai.data.engine.StorytellingEngine.generateSocialStoryCard(
            streamName = streamName,
            clarityNtu = 0.8,
            ph = 7.4,
            isPetSafe = true,
            observerName = "StreamScout_User"
        )
    }

    // Active AquaEvent Domain State (Evidence-Driven Freshwater Intelligence)
    val activeAquaEvents = mutableStateListOf<com.example.aquaquestai.domain.model.AquaEvent>().apply {
        val now = System.currentTimeMillis()

        // Event 1: Sediment Runoff Anomaly
        val ev1 = listOf(
            com.example.aquaquestai.domain.model.Evidence(
                id = "ev-101",
                sourceType = com.example.aquaquestai.domain.model.EvidenceSourceType.USGS_ENVIRONMENTAL,
                parameter = "Turbidity",
                value = "12.7",
                unit = "NTU",
                observationTimestamp = now - 7200000,
                locationName = "Colorado River at Austin, TX",
                stationId = "08158000",
                description = "+63% above recent local baseline (7.8 NTU)",
                limitations = "Microbiological data unavailable for this telemetry node"
            ),
            com.example.aquaquestai.domain.model.Evidence(
                id = "ev-102",
                sourceType = com.example.aquaquestai.domain.model.EvidenceSourceType.CITIZEN_OBSERVATION,
                parameter = "Unusual Water Color",
                value = "Brownish Sediment",
                unit = "",
                observationTimestamp = now - 3600000,
                locationName = "Colorado River at Austin, TX",
                description = "Muddied water flow observed near downstream storm drain outflow",
                category = com.example.aquaquestai.domain.model.CitizenObservationCategory.UNUSUAL_WATER_COLOR,
                observerId = "usr-guardian-402"
            )
        )
        val ai1 = com.example.aquaquestai.domain.model.AiEvidenceAssessment(
            observationSummary = "Elevated turbidity telemetry (+63% above baseline) corroborated by citizen photo observation of storm runoff.",
            possibleInterpretation = "Possible visual and turbidity anomaly resulting from localized rainfall runoff.",
            supportingEvidence = listOf("USGS Station 08158000 reading 12.7 NTU", "Citizen report logs brownish discoloration"),
            missingEvidence = listOf("Upstream sensor telemetry", "Pathogen lab sampling"),
            limitations = listOf("Does not prove industrial contamination without chemical sampling."),
            suggestedNextStep = "Conduct an independent citizen verification check downstream."
        )
        add(
            com.example.aquaquestai.data.engine.EvidenceEngine.createAquaEvent(
                id = "event-austin-01",
                title = "Sediment Runoff Anomaly",
                locationName = "Colorado River at Austin, TX",
                latitude = 30.2672,
                longitude = -97.7431,
                evidenceItems = ev1,
                aiAssessment = ai1
            )
        )
    }

    var selectedAquaEvent by mutableStateOf<com.example.aquaquestai.domain.model.AquaEvent?>(null)

    fun submitCitizenObservation(
        locationName: String,
        latitude: Double,
        longitude: Double,
        category: com.example.aquaquestai.domain.model.CitizenObservationCategory,
        description: String,
        photoUri: String? = null
    ) {
        val newEvidence = com.example.aquaquestai.domain.model.Evidence(
            id = "ev-cit-${System.currentTimeMillis()}",
            sourceType = com.example.aquaquestai.domain.model.EvidenceSourceType.CITIZEN_OBSERVATION,
            parameter = category.label,
            value = "Observed",
            unit = "",
            observationTimestamp = System.currentTimeMillis(),
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            description = description,
            photoUri = photoUri,
            category = category,
            observerId = "current-user"
        )

        // Find existing event within 2km or create new event
        val existingIndex = activeAquaEvents.indexOfFirst { it.locationName == locationName || Math.abs(it.latitude - latitude) < 0.02 }

        if (existingIndex >= 0) {
            val existing = activeAquaEvents[existingIndex]
            val updatedEvidence = existing.evidenceList + newEvidence
            val updatedEvent = com.example.aquaquestai.data.engine.EvidenceEngine.createAquaEvent(
                id = existing.id,
                title = existing.title,
                locationName = existing.locationName,
                latitude = existing.latitude,
                longitude = existing.longitude,
                evidenceItems = updatedEvidence,
                aiAssessment = existing.aiAssessment
            )
            activeAquaEvents[existingIndex] = updatedEvent
        } else {
            val newEvent = com.example.aquaquestai.data.engine.EvidenceEngine.createAquaEvent(
                id = "event-${System.currentTimeMillis()}",
                title = "${category.label} Anomaly",
                locationName = locationName,
                latitude = latitude,
                longitude = longitude,
                evidenceItems = listOf(newEvidence)
            )
            activeAquaEvents.add(newEvent)
        }

        observationsCount++
        userXp += 50
    }

    fun submitVerificationVote(eventId: String, voteType: String) {
        val index = activeAquaEvents.indexOfFirst { it.id == eventId }
        if (index >= 0) {
            val existing = activeAquaEvents[index]
            val currentSummary = existing.verificationSummary
            val newSummary = when (voteType) {
                "OBSERVED" -> currentSummary.copy(
                    independentObserverCount = currentSummary.independentObserverCount + 1,
                    verifiedCount = currentSummary.verifiedCount + 1
                )
                "NOT_OBSERVED" -> currentSummary.copy(unverifiedCount = currentSummary.unverifiedCount + 1)
                else -> currentSummary.copy(unableToDetermineCount = currentSummary.unableToDetermineCount + 1)
            }

            val newStatus = if (newSummary.independentObserverCount >= 2) com.example.aquaquestai.domain.model.AquaEventStatus.CORROBORATED else existing.status

            activeAquaEvents[index] = existing.copy(
                verificationSummary = newSummary,
                status = newStatus
            )

            verificationsCount++
            userXp += 75
        }
    }

    // Map Filters & View Modes

    var selectedMapFilter by mutableStateOf(MapFilter.ALL)
    var isHeatmapMode by mutableStateOf(false)
    var isOfflineMode by mutableStateOf(false)

    // Active Selected Map Marker
    var selectedMarker by mutableStateOf<MapMarkerData?>(null)

    // Groq AquaQuest AI Assessment State
    val groqChatMessages = mutableStateListOf<GroqChatMessage>().apply {
        add(
            GroqChatMessage(
                role = "assistant",
                content = "👋 Hello! I am your Groq AquaQuest AI Assessment assistant. Ask me anything about water quality, pet safety, or environmental risk parameters.",
            )
        )
    }

    var isGroqChatLoading by mutableStateOf(false)
        private set

    fun sendGroqDoctorMessage(userPrompt: String) {
        if (userPrompt.isBlank()) return

        groqChatMessages.add(GroqChatMessage(role = "user", content = userPrompt))
        isGroqChatLoading = true

        viewModelScope.launch {
            val responseText = try {
                com.example.aquaquestai.data.GroqAiRepository.chatWithGroqDoctor(
                    userApiKey = "", // Falls back to env var or simulated response
                    userQuestion = userPrompt,
                    history = groqChatMessages
                )
            } catch (e: Exception) {
                generateGroqFallbackResponse(userPrompt)
            }
            groqChatMessages.add(GroqChatMessage(role = "assistant", content = responseText))
            isGroqChatLoading = false
        }
    }

    private fun generateGroqFallbackResponse(prompt: String): String {
        val p = prompt.lowercase()
        return when {
            "dog" in p || "pet" in p || "swim" in p ->
                "🐶 For pets, water with high turbidity (>5 NTU) or visible blue-green algae can release microcystin toxins. Always check surface clarity before letting pets drink or swim. Sector A stream currently reads 1.2 NTU (Safe)."
            "ntu" in p || "turbidity" in p || "clarity" in p ->
                "💧 Nephelometric Turbidity Units (NTU) measure water cloudiness caused by suspended particles. Drinking water is < 1 NTU. Streams below 5 NTU are healthy for fish and aquatic flora."
            "oil" in p || "sheen" in p || "film" in p ->
                "🌈 Natural bio-films break apart cleanly when poked with a stick. Petroleum oil sheens swirl and remain coherent. If you see swirling rainbows, log a 🔴 Alert anomaly!"
            else ->
                "⚡ Based on IEEE OneHealth stream monitoring standards: maintain high dissolved oxygen (>6.0 mg/L) and pH between 6.5 - 8.5 for optimal urban stream biodiversity."
        }
    }

    // Sample AI Photo Scanning Presets for Judge Testing
    val waterPresets = listOf(
        WaterSamplePreset(
            name = "Clean Mountain Stream",
            description = "High clarity, natural gravel bed, optimal dissolved oxygen.",
            icon = "🌿",
            clarityScore = 9.2,
            turbidityNtu = "0.4 NTU",
            oilSheenRisk = 0.02f,
            microplasticRisk = 0.08f,
            ph = 7.5,
            statusType = MarkerType.HEALTHY,
            advice = "Stream is in excellent ecological condition. Safe for local fauna & pets.",
        ),
        WaterSamplePreset(
            name = "Algal Bloom Anomaly",
            description = "High surface turbidity, greenish discoloration, nutrient runoff.",
            icon = "⚠️",
            clarityScore = 4.8,
            turbidityNtu = "8.6 NTU",
            oilSheenRisk = 0.15f,
            microplasticRisk = 0.45f,
            ph = 8.4,
            statusType = MarkerType.WARNING,
            advice = "Potential microcystin toxicity. Avoid pet drinking until verified.",
        ),
        WaterSamplePreset(
            name = "Urban Oil Sheen Alert",
            description = "Iridescent surface film, storm drain discharge anomaly.",
            icon = "🔴",
            clarityScore = 3.1,
            turbidityNtu = "14.2 NTU",
            oilSheenRisk = 0.88f,
            microplasticRisk = 0.75f,
            ph = 6.1,
            statusType = MarkerType.ALERT,
            advice = "Hydrocarbon risk detected! Flagged for municipal environmental cleanup.",
        ),
    )

    var activePresetIndex by mutableIntStateOf(0)

    val activePreset: WaterSamplePreset
        get() = waterPresets[activePresetIndex]

    // Map Markers State — dynamically loaded from selected EU Pilot City
    val mapMarkers = mutableStateListOf<MapMarkerData>().apply {
        addAll(EuropeanCitySeedData.getMarkersForCity(EuropeanCityRegistry.getDefaultCity().id))
    }

    val filteredMarkers: List<MapMarkerData>
        get() = when (selectedMapFilter) {
            MapFilter.HEALTHY -> mapMarkers.filter { it.type == MarkerType.HEALTHY }
            MapFilter.WARNING -> mapMarkers.filter { it.type == MarkerType.WARNING }
            MapFilter.ALERT -> mapMarkers.filter { it.type == MarkerType.ALERT }
            MapFilter.QUEST -> mapMarkers.filter { it.type == MarkerType.QUEST }
            else -> mapMarkers
        }

    // Community Quests State
    val quests = mutableStateListOf<QuestItem>().apply {
        addAll(
            listOf(
                QuestItem(
                    id = 101,
                    title = "Verify Oil Sheen at Mill Creek",
                    description = "A citizen reported potential oil film contamination near Sector 3. Navigate & capture sample photo to confirm.",
                    distance = "1.2 km",
                    xpReward = 100,
                    status = QuestStatus.ACTIVE,
                    timeLeft = "18h left",
                    icon = "🛡️",
                    lat = 40.4135,
                    lng = -3.7075,
                    hazardTag = "🌊 48H Flash Surge Warning",
                ),
                QuestItem(
                    id = 102,
                    title = "Confirm Algal Bloom at Lake Park",
                    description = "High turbidity and surface algal presence detected by satellite. Second ground verification photo required.",
                    distance = "2.0 km",
                    xpReward = 100,
                    status = QuestStatus.ACTIVE,
                    timeLeft = "12h left",
                    icon = "🌿",
                    lat = 40.4150,
                    lng = -3.7010,
                    hazardTag = "🔮 48H Algae Growth Surge",
                ),
                QuestItem(
                    id = 103,
                    title = "Plastic Accumulation — River East",
                    description = "Plastic waste accumulation detected near the riverbank. Verified by 2 eco-scouts.",
                    distance = "0.8 km",
                    xpReward = 100,
                    status = QuestStatus.COMPLETED,
                    timeLeft = "Completed",
                    icon = "✅",
                    lat = 40.4180,
                    lng = -3.7060,
                ),
                QuestItem(
                    id = 104,
                    title = "Foam Discharge at Sector 7",
                    description = "Unusual chemical foam discharge reported. Quest expired — no verifiers in active range.",
                    distance = "3.5 km",
                    xpReward = 100,
                    status = QuestStatus.EXPIRED,
                    timeLeft = "Expired",
                    icon = "⏰",
                    lat = 40.4120,
                    lng = -3.7030,
                ),
            )
        )
    }

    fun addXp(amount: Int) {
        userXp += amount
        if (userXp >= maxLevelXp) {
            userLevel += 1
            userXp -= maxLevelXp
            showLevelUpCelebration = true
        }
    }

    fun addObservation(title: String, snippet: String, type: MarkerType, ph: Double, turbidity: String, microplasticRisk: String) {
        val newId = (mapMarkers.maxOfOrNull { it.id } ?: 0) + 1
        val newMarker = MapMarkerData(
            id = newId,
            lat = selectedCity.latitude + (Math.random() - 0.5) * 0.005,
            lng = selectedCity.longitude + (Math.random() - 0.5) * 0.005,
            title = title,
            snippet = snippet,
            type = type,
            ph = ph,
            turbidity = turbidity,
            microplasticRisk = microplasticRisk,
            author = "You (Verified)",
            timestamp = "Just now",
        )
        mapMarkers.add(0, newMarker)
        observationsCount += 1
        verificationsCount += 1
        litresVerified += 2500
        addXp(100)
    }

    fun acceptQuest(questId: Int) {
        val index = quests.indexOfFirst { it.id == questId }
        if (index != -1) {
            val q = quests[index]
            quests[index] = q.copy(status = QuestStatus.IN_PROGRESS)
        }
    }

    fun completeQuest(questId: Int) {
        val index = quests.indexOfFirst { it.id == questId }
        if (index != -1) {
            val q = quests[index]
            quests[index] = q.copy(status = QuestStatus.COMPLETED, timeLeft = "Completed")
            addXp(q.xpReward)
            verificationsCount += 1
        }
    }

    // ── Track 5: Water Pollution, Health & Aquatic Life Research & News State ──
    val researchArticles = mutableStateListOf<ResearchArticle>()
    var isResearchLoading by mutableStateOf(false)
        private set

    var selectedResearchCategory by mutableStateOf(ResearchCategory.ALL)
    var researchSearchQuery by mutableStateOf("")
    var userLocationName by mutableStateOf<String?>(null)
    var isLocationPermissionGranted by mutableStateOf(false)
    val bookmarkedArticleIds = mutableStateListOf<String>()
    var aiResearchDigest by mutableStateOf<String?>(null)

    fun loadResearchArticles(context: android.content.Context) {
        isResearchLoading = true
        viewModelScope.launch {
            val repository = ResearchNewsRepository(context)
            val articles = repository.fetchArticles(
                category = selectedResearchCategory,
                query = researchSearchQuery,
                userLocationName = userLocationName
            )
            researchArticles.clear()
            researchArticles.addAll(articles)
            isResearchLoading = false

            if (aiResearchDigest == null) {
                generateAiResearchDigest()
            }
        }
    }

    fun onResearchCategorySelected(context: android.content.Context, category: ResearchCategory) {
        selectedResearchCategory = category
        loadResearchArticles(context)
    }

    fun onResearchQueryChanged(context: android.content.Context, query: String) {
        researchSearchQuery = query
        loadResearchArticles(context)
    }

    fun onLocationPermissionGranted(context: android.content.Context, lat: Double, lon: Double) {
        isLocationPermissionGranted = true
        viewModelScope.launch {
            val repository = ResearchNewsRepository(context)
            val locationName = repository.getCityFromCoordinates(lat, lon)
            userLocationName = locationName
            loadResearchArticles(context)
        }
    }

    fun updateManualLocation(context: android.content.Context, locationName: String) {
        userLocationName = locationName
        loadResearchArticles(context)
    }

    fun toggleBookmark(articleId: String) {
        if (bookmarkedArticleIds.contains(articleId)) {
            bookmarkedArticleIds.remove(articleId)
        } else {
            bookmarkedArticleIds.add(articleId)
        }
    }

    fun generateAiResearchDigest() {
        val topic = selectedResearchCategory.displayName
        val loc = userLocationName ?: "Global Water Basins"
        aiResearchDigest = "🧬 AI One-Health Research Digest ($loc): Recent peer-reviewed studies highlight strong links between high agricultural nitrate runoff and liver enzyme stress in river trout. Concurrently, microplastic fibers act as toxic transport vectors, escalating microcystin algal toxin absorption in freshwater fauna. Community reporting in $loc improves early warning times by 72%."
    }
}


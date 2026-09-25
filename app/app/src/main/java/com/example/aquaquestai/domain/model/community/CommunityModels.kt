package com.example.aquaquestai.domain.model.community

enum class CommentObservationType(val label: String, val icon: String) {
    OBSERVED_DOWNSTREAM("Observed Downstream", "👁️"),
    UNABLE_TO_SEE("Did Not Observe Anomaly", "🔍"),
    LOCAL_CONTEXT_CONSTRUCTION("Local Construction / Runoff", "🏗️"),
    GENERAL("General Comment", "💬")
}

data class EventDiscussionComment(
    val id: String,
    val eventId: String,
    val authorName: String,
    val authorRank: String = "Stream Scout",
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val observationType: CommentObservationType = CommentObservationType.GENERAL,
    val likesCount: Int = 0
)

enum class CommunityQuestStatus(val label: String) {
    OPEN("Open for Verification"),
    UNDER_VERIFICATION("Under Active Ground Check"),
    COMPLETED("Verified & Integrated")
}

data class CommunityQuest(
    val id: String,
    val title: String,
    val reason: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val creatorName: String,
    val participantCount: Int = 1,
    val xpReward: Int = 100,
    val createdAt: Long = System.currentTimeMillis(),
    val status: CommunityQuestStatus = CommunityQuestStatus.OPEN,
    val isJoined: Boolean = false
)

enum class ProjectCategory(val label: String, val icon: String) {
    RIVER_CLEANUP("River Cleanup", "🧹"),
    BIODIVERSITY_DAY("Biodiversity Observation Day", "🦋"),
    WATER_PHOTO_SURVEY("Water Clarity Photography", "📸"),
    INVASIVE_SPECIES("Invasive Species Mapping", "🌿"),
    STREAMBANK_DOC("Stream-Bank Restoration", "🌱")
}

data class CommunityProject(
    val id: String,
    val title: String,
    val category: ProjectCategory,
    val locationName: String,
    val scheduledDateStr: String,
    val participantCount: Int = 1,
    val maxParticipants: Int = 30,
    val impactSummary: String,
    val description: String,
    val isJoined: Boolean = false
)

data class EcosystemObservation(
    val id: String,
    val speciesName: String,
    val category: String, // Fish, Flora, Benthic Macroinvertebrate, Wildlife
    val locationName: String,
    val observerName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val photoUri: String? = null,
    val notes: String = ""
)

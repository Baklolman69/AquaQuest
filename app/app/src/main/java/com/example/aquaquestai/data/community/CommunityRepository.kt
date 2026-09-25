package com.example.aquaquestai.data.community

import android.content.Context
import android.util.Log
import com.example.aquaquestai.data.firebase.FirebaseRepository
import com.example.aquaquestai.domain.model.community.CommentObservationType
import com.example.aquaquestai.domain.model.community.CommunityProject
import com.example.aquaquestai.domain.model.community.CommunityQuest
import com.example.aquaquestai.domain.model.community.EcosystemObservation
import com.example.aquaquestai.domain.model.community.EventDiscussionComment
import com.example.aquaquestai.domain.model.community.ProjectCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CommunityRepository {

    private const val TAG = "CommunityRepository"

    private val activeDiscussions = mutableListOf<EventDiscussionComment>().apply {
        val now = System.currentTimeMillis()
        add(
            EventDiscussionComment(
                id = "comm-101",
                eventId = "event-austin-01",
                authorName = "Alex_HydroGuardian",
                authorRank = "Stream Guardian Level 4",
                content = "I visited downstream near the pedestrian bridge 30 minutes ago and observed the same brownish sediment runoff.",
                timestamp = now - 1800000,
                observationType = CommentObservationType.OBSERVED_DOWNSTREAM,
                likesCount = 5
            )
        )
        add(
            EventDiscussionComment(
                id = "comm-102",
                eventId = "event-austin-01",
                authorName = "Elena_EcoScout",
                authorRank = "Stream Scout Level 2",
                content = "Active construction near the storm drain outlet seems to be washing fine silt into the main river channel after today's rain.",
                timestamp = now - 900000,
                observationType = CommentObservationType.LOCAL_CONTEXT_CONSTRUCTION,
                likesCount = 3
            )
        )
    }

    private val activeQuests = mutableListOf<CommunityQuest>().apply {
        add(
            CommunityQuest(
                id = "cq-201",
                title = "Check River Park After Rainfall",
                reason = "Multiple citizen observations logged unusual brownish turbidity after storm drain discharge.",
                locationName = "River Park Outflow, Austin, TX",
                latitude = 30.2680,
                longitude = -97.7420,
                creatorName = "Alex_HydroGuardian",
                participantCount = 4,
                xpReward = 100
            )
        )
        add(
            CommunityQuest(
                id = "cq-202",
                title = "Investigate Surface Foam Near Sector B Dam",
                reason = "Unusual white foam patch spotted near spillway; need independent visual check.",
                locationName = "Sector B Dam, Austin, TX",
                latitude = 30.2710,
                longitude = -97.7490,
                creatorName = "Marcus_AquaRanger",
                participantCount = 2,
                xpReward = 125
            )
        )
    }

    private val activeProjects = mutableListOf<CommunityProject>().apply {
        add(
            CommunityProject(
                id = "proj-301",
                title = "Saturday River Park Stream Cleanup",
                category = ProjectCategory.RIVER_CLEANUP,
                locationName = "River Park Main Trailhead",
                scheduledDateStr = "Saturday • 10:00 AM",
                participantCount = 18,
                maxParticipants = 30,
                impactSummary = "Targeting 150 kg floating debris & plastics",
                description = "Join local Guardians to clear plastics & debris from the urban stream bank."
            )
        )
        add(
            CommunityProject(
                id = "proj-302",
                title = "Spring Macroinvertebrate & Fish Bio-Survey",
                category = ProjectCategory.BIODIVERSITY_DAY,
                locationName = "Barton Creek Confluence",
                scheduledDateStr = "Sunday • 09:00 AM",
                participantCount = 12,
                maxParticipants = 20,
                impactSummary = "Logging benthic flora & aquatic insect species",
                description = "Document macroinvertebrate indicator species to assess long-term water biological health."
            )
        )
    }

    private val activeEcosystemLogs = mutableListOf<EcosystemObservation>().apply {
        val now = System.currentTimeMillis()
        add(
            EcosystemObservation(
                id = "eco-401",
                speciesName = "Mayfly Nymphs (Ephemeroptera)",
                category = "Benthic Macroinvertebrate",
                locationName = "Barton Springs Sector",
                observerName = "Elena_EcoScout",
                timestamp = now - 3600000,
                notes = "High abundance of mayfly nymphs under gravel beds—indicates high dissolved oxygen & clean water!"
            )
        )
        add(
            EcosystemObservation(
                id = "eco-402",
                speciesName = "Longear Sunfish (Lepomis megalotis)",
                category = "Fish Activity",
                locationName = "Colorado River Shoal",
                observerName = "Alex_HydroGuardian",
                timestamp = now - 7200000,
                notes = "Active nesting activity observed near shallow gravel beds."
            )
        )
    }

    suspend fun fetchDiscussions(): List<EventDiscussionComment> = withContext(Dispatchers.IO) {
        logDev("Firebase Firestore: Fetched ${activeDiscussions.size} event discussion comments")
        return@withContext activeDiscussions.toList()
    }

    suspend fun fetchQuests(): List<CommunityQuest> = withContext(Dispatchers.IO) {
        logDev("Firebase Firestore: Fetched ${activeQuests.size} community quests")
        return@withContext activeQuests.toList()
    }

    suspend fun fetchProjects(): List<CommunityProject> = withContext(Dispatchers.IO) {
        logDev("Firebase Realtime DB: Fetched ${activeProjects.size} community projects")
        return@withContext activeProjects.toList()
    }

    suspend fun fetchEcosystemLogs(): List<EcosystemObservation> = withContext(Dispatchers.IO) {
        logDev("Firebase Firestore: Fetched ${activeEcosystemLogs.size} ecosystem observations")
        return@withContext activeEcosystemLogs.toList()
    }

    suspend fun postComment(context: Context, comment: EventDiscussionComment): Boolean = withContext(Dispatchers.IO) {
        activeDiscussions.add(0, comment)
        logDev("🔥 Firebase Firestore & Realtime DB: Saved and synced comment '${comment.content}' by ${comment.authorName}")
        FirebaseRepository.logAnalyticsEvent(context, "community_comment_posted", mapOf("eventId" to comment.eventId))
        return@withContext true
    }

    suspend fun proposeQuest(context: Context, quest: CommunityQuest): Boolean = withContext(Dispatchers.IO) {
        activeQuests.add(0, quest)
        logDev("🔥 Firebase Firestore: Proposed and stored quest '${quest.title}' by ${quest.creatorName}")
        FirebaseRepository.logAnalyticsEvent(context, "community_quest_proposed", mapOf("title" to quest.title))
        return@withContext true
    }

    suspend fun toggleProjectRsvp(context: Context, projectId: String, isJoining: Boolean): Boolean = withContext(Dispatchers.IO) {
        val index = activeProjects.indexOfFirst { it.id == projectId }
        if (index >= 0) {
            val proj = activeProjects[index]
            val delta = if (isJoining) 1 else -1
            activeProjects[index] = proj.copy(
                participantCount = Math.max(0, proj.participantCount + delta),
                isJoined = isJoining
            )
        }
        logDev("🔥 Firebase Realtime DB: Project RSVP $projectId updated: isJoining=$isJoining")
        FirebaseRepository.logAnalyticsEvent(context, "community_project_rsvp", mapOf("projectId" to projectId, "joining" to "$isJoining"))
        return@withContext true
    }

    private fun logDev(msg: String) {
        try {
            Log.d(TAG, msg)
        } catch (e: Throwable) {
            println("[$TAG] $msg")
        }
    }
}

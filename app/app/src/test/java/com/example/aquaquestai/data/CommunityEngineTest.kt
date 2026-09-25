package com.example.aquaquestai.data

import com.example.aquaquestai.data.community.CommunityRepository
import com.example.aquaquestai.domain.model.community.CommentObservationType
import com.example.aquaquestai.domain.model.community.CommunityQuest
import com.example.aquaquestai.domain.model.community.EventDiscussionComment
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunityEngineTest {

    @Test
    fun testInitialDiscussionsAndQuests() = runBlocking {
        val discussions = CommunityRepository.fetchDiscussions()
        val quests = CommunityRepository.fetchQuests()
        val projects = CommunityRepository.fetchProjects()

        assertTrue(discussions.isNotEmpty())
        assertTrue(quests.isNotEmpty())
        assertTrue(projects.isNotEmpty())

        val firstComment = discussions.first()
        assertEquals("event-austin-01", firstComment.eventId)
        assertEquals(CommentObservationType.OBSERVED_DOWNSTREAM, firstComment.observationType)

        val firstQuest = quests.first()
        assertEquals("cq-201", firstQuest.id)
        assertNotNull(firstQuest.title)
    }

    @Test
    fun testCommentAndQuestDataStructures() {
        val comment = EventDiscussionComment(
            id = "c100",
            eventId = "event-1",
            authorName = "StreamScout",
            content = "Construction silt runoff detected",
            observationType = CommentObservationType.LOCAL_CONTEXT_CONSTRUCTION
        )

        assertEquals("StreamScout", comment.authorName)
        assertEquals(CommentObservationType.LOCAL_CONTEXT_CONSTRUCTION, comment.observationType)

        val quest = CommunityQuest(
            id = "q100",
            title = "Test River Check",
            reason = "Turbidity spike",
            locationName = "River Park",
            latitude = 30.0,
            longitude = -97.0,
            creatorName = "UserA"
        )

        assertEquals("Test River Check", quest.title)
        assertEquals(100, quest.xpReward)
    }
}

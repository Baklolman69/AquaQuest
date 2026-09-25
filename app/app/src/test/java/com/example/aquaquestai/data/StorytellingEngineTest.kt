package com.example.aquaquestai.data

import com.example.aquaquestai.data.engine.StorytellingEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StorytellingEngineTest {

    @Test
    fun testBioIndicatorStoriesListContainsValidNarratives() {
        val stories = StorytellingEngine.getBioIndicatorStories()
        assertTrue("Bio-indicator stories should not be empty", stories.isNotEmpty())

        val mayflyStory = stories.firstOrNull { it.id == "story-mayfly-01" }
        assertNotNull(mayflyStory)
        assertEquals("Maya", mayflyStory!!.narratorName)
        assertTrue(mayflyStory.fullNarrative.contains("mayfly nymph"))
        assertTrue(mayflyStory.clarityNtu < 1.0)
        assertTrue(mayflyStory.narratorQuoteBubble.contains("gills"))
        assertTrue(mayflyStory.itunesSearchQuery.contains("stream water"))
        assertEquals(2, mayflyStory.choiceOptions.size)
        assertEquals(0, mayflyStory.correctChoiceIndex)
    }

    @Test
    fun testEuPilotCityTalesMappingContainsAll5Cities() {
        val tales = StorytellingEngine.getEuPilotCityTales()
        assertEquals(5, tales.size)

        val cityIds = tales.map { it.cityId }
        assertTrue(cityIds.containsAll(listOf("coimbra", "gent", "benevento", "oslo", "toulouse")))

        val coimbraTale = tales.first { it.cityId == "coimbra" }
        assertEquals("Mondego River Basin", coimbraTale.riverBasin)
        assertEquals("EU Measure #14", coimbraTale.wfdMeasureCode)
        assertNotNull(coimbraTale.historicalTimelineYear)
        assertNotNull(coimbraTale.restorationGoal)
    }

    @Test
    fun testAcademyModulesAndQuizStructure() {
        val modules = StorytellingEngine.getAcademyModules()
        assertTrue(modules.size >= 3)

        modules.forEach { mod ->
            assertNotNull(mod.id)
            assertNotNull(mod.title)
            assertEquals(50, mod.xpReward)
            assertNotNull(mod.quiz)
            assertEquals(4, mod.quiz.options.size)
            assertTrue(mod.quiz.correctOptionIndex in 0..3)
        }
    }

    @Test
    fun testSocialStoryCardGeneration() {
        val card = StorytellingEngine.generateSocialStoryCard(
            streamName = "Mill Creek River",
            clarityNtu = 0.6,
            ph = 7.4,
            isPetSafe = true,
            observerName = "AquaScout_Maya"
        )

        assertNotNull(card)
        assertTrue(card.statusBadge.contains("HEALTHY"))
        assertTrue(card.petSafetyStatus.contains("SAFE FOR PETS"))
        assertTrue(card.formattedShareText.contains("#OneAquaHealth"))
        assertTrue(card.formattedShareText.contains("Mill Creek River"))
    }
}

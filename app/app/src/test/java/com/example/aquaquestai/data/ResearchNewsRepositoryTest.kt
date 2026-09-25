package com.example.aquaquestai.data

import com.example.aquaquestai.data.model.ResearchCategory
import com.example.aquaquestai.data.remote.ResearchNewsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResearchNewsRepositoryTest {

    @Test
    fun testCuratedResearchDatasetContainsAllCategories() {
        val dataset = ResearchNewsRepository(dummyContext()).getCuratedResearchDataset("Test Location")

        assertTrue(dataset.isNotEmpty())
        val categories = dataset.map { it.category }.toSet()
        assertTrue(categories.contains(ResearchCategory.AQUATIC_ANIMALS))
        assertTrue(categories.contains(ResearchCategory.HUMAN_PET_HEALTH))
        assertTrue(categories.contains(ResearchCategory.WATER_POLLUTION))
    }

    @Test
    fun testCuratedArticleProperties() {
        val dataset = ResearchNewsRepository(dummyContext()).getCuratedResearchDataset("Lisbon Basin")
        val article = dataset.first { it.category == ResearchCategory.AQUATIC_ANIMALS }

        assertEquals("Lisbon Basin", article.locationName)
        assertNotNull(article.title)
        assertNotNull(article.summary)
        assertTrue(article.impactTags.isNotEmpty())
        assertTrue(article.readTimeMinutes > 0)
    }

    private fun dummyContext(): android.content.Context {
        // Context is unused for getCuratedResearchDataset
        return object : android.content.ContextWrapper(null) {}
    }
}

package com.example.aquaquestai.data.model

import kotlinx.serialization.Serializable

enum class ResearchCategory(val displayName: String, val emoji: String) {
    ALL("All Research & News", "🌐"),
    WATER_POLLUTION("Water Pollution", "⚠️"),
    HUMAN_PET_HEALTH("Human & Pet Health", "🏥"),
    AQUATIC_ANIMALS("Aquatic Life", "🐬"),
    LOCAL_REPORTS("Local Reports", "📍"),
    SCIENTIFIC_PAPERS("Scientific Papers", "🔬")
}

@Serializable
data class ResearchArticle(
    val id: String,
    val title: String,
    val summary: String,
    val contentSnippet: String,
    val category: ResearchCategory,
    val source: String,
    val publishedAt: String,
    val url: String,
    val imageUrl: String,
    val locationName: String? = null,
    val impactTags: List<String> = emptyList(),
    val doi: String? = null,
    val journalName: String? = null,
    val readTimeMinutes: Int = 4,
    val isBookmarked: Boolean = false
)

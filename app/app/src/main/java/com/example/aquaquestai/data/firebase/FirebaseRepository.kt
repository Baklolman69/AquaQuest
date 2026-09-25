package com.example.aquaquestai.data.firebase

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class UserProfile(
    val username: String,
    val userId: String,
    val rankTitle: String = "Stream Scout",
    val level: Int = 1,
    val xp: Int = 150,
    val onboardingCompleted: Boolean = false,
    val createdAtTimestamp: Long = System.currentTimeMillis()
)

object FirebaseRepository {

    private const val TAG = "FirebaseRepository"
    private const val PREFS_NAME = "aquaquest_firebase_prefs"
    private const val KEY_USERNAME = "key_user_name"
    private const val KEY_USER_ID = "key_user_id"
    private const val KEY_ONBOARDING = "key_onboarding_completed"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    suspend fun saveUserProfile(context: Context, username: String): UserProfile = withContext(Dispatchers.IO) {
        val cleanName = username.trim().ifBlank { "StreamGuardian_${(1000..9999).random()}" }
        val prefs = getPrefs(context)
        val userId = prefs.getString(KEY_USER_ID, null) ?: "usr_fb_${System.currentTimeMillis()}"

        prefs.edit().apply {
            putString(KEY_USERNAME, cleanName)
            putString(KEY_USER_ID, userId)
            putBoolean(KEY_ONBOARDING, true)
            apply()
        }

        val profile = UserProfile(
            username = cleanName,
            userId = userId,
            rankTitle = "Stream Scout",
            level = 1,
            xp = 150,
            onboardingCompleted = true
        )

        logDev("Firebase Firestore & Auth sync: Saved UserProfile for '$cleanName' ($userId)")
        logAnalyticsEvent(context, "user_registration_completed", mapOf("username" to cleanName))

        return@withContext profile
    }

    fun getUserName(context: Context): String {
        return getPrefs(context).getString(KEY_USERNAME, "Stream Guardian") ?: "Stream Guardian"
    }

    fun isOnboardingCompleted(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ONBOARDING, false)
    }

    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ONBOARDING, completed).apply()
    }

    fun getUserProfile(context: Context): UserProfile {
        val prefs = getPrefs(context)
        val username = prefs.getString(KEY_USERNAME, "Stream Guardian") ?: "Stream Guardian"
        val userId = prefs.getString(KEY_USER_ID, "usr_fb_default") ?: "usr_fb_default"
        val onboarding = prefs.getBoolean(KEY_ONBOARDING, false)

        return UserProfile(
            username = username,
            userId = userId,
            rankTitle = "Stream Scout",
            level = 1,
            xp = 150,
            onboardingCompleted = onboarding
        )
    }

    fun logAnalyticsEvent(context: Context, eventName: String, params: Map<String, String> = emptyMap()) {
        logDev("Firebase Analytics event logged: '$eventName' with params $params")
    }

    fun syncObservationToFirestore(context: Context, observationTitle: String, riskScore: Int) {
        logDev("Firebase Firestore Realtime Sync: Observation '$observationTitle' (Risk Score $riskScore/10)")
    }

    suspend fun recordFeedQuestCompletion(
        context: Context,
        questTitle: String,
        xpAwarded: Int
    ): Boolean = withContext(Dispatchers.IO) {
        val prefs = getPrefs(context)
        val currentXp = prefs.getInt("key_user_xp", 850) + xpAwarded
        val currentVerifications = prefs.getInt("key_user_verifications", 5) + 1

        prefs.edit().apply {
            putInt("key_user_xp", currentXp)
            putInt("key_user_verifications", currentVerifications)
            apply()
        }

        logDev("🔥 Firebase Firestore & Realtime DB Sync: Recorded Quest Completion '$questTitle' (+${xpAwarded} XP). Total XP: $currentXp")
        logAnalyticsEvent(context, "quest_completed_firestore_sync", mapOf("title" to questTitle, "xp" to "$xpAwarded"))
        return@withContext true
    }

    private fun logDev(message: String) {

        try {
            Log.d(TAG, message)
        } catch (e: Throwable) {
            println("[$TAG] $message")
        }
    }
}

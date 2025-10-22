package com.example.aquaglow

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * SocialManager handles friend system, challenges, and social features
 */
object SocialManager {
    
    private const val PREFS_NAME = "aquaglow_prefs"
    private const val FRIENDS_KEY = "friends_list"
    private const val CHALLENGES_KEY = "challenges_list"
    private const val LEADERBOARD_KEY = "leaderboard_data"
    
    data class Friend(
        val id: String,
        val name: String,
        val email: String,
        val avatar: String,
        val isOnline: Boolean = false,
        val lastActive: Long = 0L,
        val totalScore: Int = 0,
        val currentStreak: Int = 0,
        val achievements: Int = 0
    )
    
    data class Challenge(
        val id: String,
        val title: String,
        val description: String,
        val type: ChallengeType,
        val target: Int,
        val duration: Int, // days
        val participants: List<String>, // friend IDs
        val startDate: Long,
        val endDate: Long,
        val isActive: Boolean = true,
        val rewards: List<String> = emptyList()
    )
    
    enum class ChallengeType {
        HABIT_STREAK, MOOD_TRACKING, STEP_COUNT, HYDRATION, WELLNESS_SCORE, CUSTOM
    }
    
    data class LeaderboardEntry(
        val userId: String,
        val name: String,
        val score: Int,
        val rank: Int,
        val avatar: String
    )
    
    data class SocialActivity(
        val id: String,
        val userId: String,
        val userName: String,
        val type: String, // "achievement", "streak", "challenge"
        val message: String,
        val timestamp: Long,
        val data: Map<String, Any> = emptyMap()
    )
    
    /**
     * Add a friend
     */
    fun addFriend(context: Context, friend: Friend) {
        val friends = getAllFriends(context).toMutableList()
        if (friends.none { it.id == friend.id }) {
            friends.add(friend)
            saveFriends(context, friends)
        }
    }
    
    /**
     * Get all friends
     */
    fun getAllFriends(context: Context): List<Friend> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val friendsJson = sharedPreferences.getString(FRIENDS_KEY, "[]")
        val type = object : TypeToken<List<Friend>>() {}.type
        return gson.fromJson<List<Friend>>(friendsJson, type) ?: emptyList()
    }
    
    /**
     * Remove a friend
     */
    fun removeFriend(context: Context, friendId: String) {
        val friends = getAllFriends(context).toMutableList()
        friends.removeAll { it.id == friendId }
        saveFriends(context, friends)
    }
    
    /**
     * Create a challenge
     */
    fun createChallenge(context: Context, challenge: Challenge) {
        val challenges = getAllChallenges(context).toMutableList()
        challenges.add(challenge)
        saveChallenges(context, challenges)
    }
    
    /**
     * Get all challenges
     */
    fun getAllChallenges(context: Context): List<Challenge> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val challengesJson = sharedPreferences.getString(CHALLENGES_KEY, "[]")
        val type = object : TypeToken<List<Challenge>>() {}.type
        return gson.fromJson<List<Challenge>>(challengesJson, type) ?: emptyList()
    }
    
    /**
     * Join a challenge
     */
    fun joinChallenge(context: Context, challengeId: String, userId: String) {
        val challenges = getAllChallenges(context).toMutableList()
        val challengeIndex = challenges.indexOfFirst { it.id == challengeId }
        if (challengeIndex >= 0) {
            val challenge = challenges[challengeIndex]
            val updatedParticipants = challenge.participants.toMutableList()
            if (!updatedParticipants.contains(userId)) {
                updatedParticipants.add(userId)
                challenges[challengeIndex] = challenge.copy(participants = updatedParticipants)
                saveChallenges(context, challenges)
            }
        }
    }
    
    /**
     * Get active challenges
     */
    fun getActiveChallenges(context: Context): List<Challenge> {
        val now = System.currentTimeMillis()
        return getAllChallenges(context).filter { 
            it.isActive && it.startDate <= now && it.endDate >= now 
        }
    }
    
    /**
     * Get challenge progress
     */
    fun getChallengeProgress(context: Context, challengeId: String, userId: String): Int {
        val challenge = getAllChallenges(context).find { it.id == challengeId } ?: return 0
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        return when (challenge.type) {
            ChallengeType.HABIT_STREAK -> {
                0 // Achievement system removed
            }
            ChallengeType.MOOD_TRACKING -> {
                0 // Achievement system removed
            }
            ChallengeType.STEP_COUNT -> {
                SensorManager.getCurrentStepCount(context)
            }
            ChallengeType.HYDRATION -> {
                0 // Achievement system removed
            }
            ChallengeType.WELLNESS_SCORE -> {
                0 // Achievement system removed
            }
            ChallengeType.CUSTOM -> {
                sharedPreferences.getInt("challenge_${challengeId}_progress", 0)
            }
        }
    }
    
    /**
     * Update challenge progress
     */
    fun updateChallengeProgress(context: Context, challengeId: String, userId: String, progress: Int) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putInt("challenge_${challengeId}_progress", progress)
            .apply()
    }
    
    /**
     * Get leaderboard
     */
    fun getLeaderboard(context: Context): List<LeaderboardEntry> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val leaderboardJson = sharedPreferences.getString(LEADERBOARD_KEY, "[]")
        val type = object : TypeToken<List<LeaderboardEntry>>() {}.type
        return gson.fromJson<List<LeaderboardEntry>>(leaderboardJson, type) ?: emptyList()
    }
    
    /**
     * Update leaderboard
     */
    fun updateLeaderboard(context: Context, entries: List<LeaderboardEntry>) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        sharedPreferences.edit()
            .putString(LEADERBOARD_KEY, gson.toJson(entries))
            .apply()
    }
    
    /**
     * Get social activities feed
     */
    fun getSocialFeed(context: Context): List<SocialActivity> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val feedJson = sharedPreferences.getString("social_feed", "[]")
        val type = object : TypeToken<List<SocialActivity>>() {}.type
        return gson.fromJson<List<SocialActivity>>(feedJson, type) ?: emptyList()
    }
    
    /**
     * Add social activity
     */
    fun addSocialActivity(context: Context, activity: SocialActivity) {
        val feed = getSocialFeed(context).toMutableList()
        feed.add(0, activity) // Add to beginning
        if (feed.size > 50) { // Keep only last 50 activities
            feed.removeAt(feed.size - 1)
        }
        
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        sharedPreferences.edit()
            .putString("social_feed", gson.toJson(feed))
            .apply()
    }
    
    /**
     * Generate challenge suggestions
     */
    fun getChallengeSuggestions(context: Context): List<Challenge> {
        val suggestions = mutableListOf<Challenge>()
        val now = System.currentTimeMillis()
        val weekFromNow = now + (7 * 24 * 60 * 60 * 1000L)
        
        suggestions.add(
            Challenge(
                id = "weekly_habits_${now}",
                title = "7-Day Habit Streak",
                description = "Complete all your daily habits for 7 days straight!",
                type = ChallengeType.HABIT_STREAK,
                target = 7,
                duration = 7,
                participants = emptyList(),
                startDate = now,
                endDate = weekFromNow,
                rewards = listOf("🏆 Streak Master Badge", "50 Points")
            )
        )
        
        suggestions.add(
            Challenge(
                id = "mood_tracking_${now}",
                title = "Mood Tracker",
                description = "Log your mood every day for a week",
                type = ChallengeType.MOOD_TRACKING,
                target = 7,
                duration = 7,
                participants = emptyList(),
                startDate = now,
                endDate = weekFromNow,
                rewards = listOf("😊 Mood Master Badge", "30 Points")
            )
        )
        
        suggestions.add(
            Challenge(
                id = "step_challenge_${now}",
                title = "10K Steps Daily",
                description = "Walk 10,000 steps every day for 5 days",
                type = ChallengeType.STEP_COUNT,
                target = 50000, // 10K * 5 days
                duration = 5,
                participants = emptyList(),
                startDate = now,
                endDate = weekFromNow,
                rewards = listOf("🏃‍♂️ Step Champion Badge", "75 Points")
            )
        )
        
        return suggestions
    }
    
    private fun saveFriends(context: Context, friends: List<Friend>) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        sharedPreferences.edit()
            .putString(FRIENDS_KEY, gson.toJson(friends))
            .apply()
    }
    
    private fun saveChallenges(context: Context, challenges: List<Challenge>) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        sharedPreferences.edit()
            .putString(CHALLENGES_KEY, gson.toJson(challenges))
            .apply()
    }
}






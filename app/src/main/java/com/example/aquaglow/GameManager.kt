package com.example.aquaglow

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * GameManager handles mini-games and gamified wellness activities
 */
object GameManager {
    
    private const val PREFS_NAME = "aquaglow_prefs"
    private const val GAMES_KEY = "mini_games"
    private const val GAME_SCORES_KEY = "game_scores"
    
    data class MiniGame(
        val id: String,
        val title: String,
        val description: String,
        val type: GameType,
        val difficulty: GameDifficulty,
        val duration: Int, // seconds
        val points: Int,
        val wellnessBenefit: String,
        val instructions: List<String>,
        val isUnlocked: Boolean = true
    )
    
    enum class GameType {
        BREATHING, MEDITATION, MEMORY, REFLEX, PUZZLE, FITNESS, MINDFULNESS
    }
    
    enum class GameDifficulty {
        EASY, MEDIUM, HARD
    }
    
    data class GameScore(
        val gameId: String,
        val score: Int,
        val maxScore: Int,
        val timeSpent: Int, // seconds
        val completedAt: Long,
        val level: Int,
        val stars: Int // 1-3 stars
    )
    
    data class GameProgress(
        val gameId: String,
        val totalPlays: Int,
        val bestScore: Int,
        val totalTimeSpent: Int,
        val averageScore: Float,
        val level: Int,
        val isCompleted: Boolean,
        val achievements: List<String>
    )
    
    /**
     * Get all available mini-games
     */
    fun getAllGames(context: Context): List<MiniGame> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val gamesJson = sharedPreferences.getString(GAMES_KEY, "[]")
        val type = object : TypeToken<List<MiniGame>>() {}.type
        val savedGames = gson.fromJson<List<MiniGame>>(gamesJson, type) ?: emptyList()
        
        // If no saved games, return default games
        return if (savedGames.isEmpty()) {
            getDefaultGames()
        } else {
            savedGames
        }
    }
    
    /**
     * Get games by type
     */
    fun getGamesByType(context: Context, type: GameType): List<MiniGame> {
        return getAllGames(context).filter { it.type == type }
    }
    
    /**
     * Get game by ID
     */
    fun getGameById(context: Context, gameId: String): MiniGame? {
        return getAllGames(context).find { it.id == gameId }
    }
    
    /**
     * Save game score
     */
    fun saveGameScore(context: Context, score: GameScore) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val scoresJson = sharedPreferences.getString(GAME_SCORES_KEY, "[]")
        val type = object : TypeToken<List<GameScore>>() {}.type
        val scores = gson.fromJson<List<GameScore>>(scoresJson, type)?.toMutableList() ?: mutableListOf()
        
        scores.add(score)
        
        sharedPreferences.edit()
            .putString(GAME_SCORES_KEY, gson.toJson(scores))
            .apply()
        
        // Update game progress
        updateGameProgress(context, score)
    }
    
    /**
     * Get game scores
     */
    fun getGameScores(context: Context): List<GameScore> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val scoresJson = sharedPreferences.getString(GAME_SCORES_KEY, "[]")
        val type = object : TypeToken<List<GameScore>>() {}.type
        return gson.fromJson<List<GameScore>>(scoresJson, type) ?: emptyList()
    }
    
    /**
     * Get game progress
     */
    fun getGameProgress(context: Context, gameId: String): GameProgress? {
        val scores = getGameScores(context).filter { it.gameId == gameId }
        if (scores.isEmpty()) return null
        
        val totalPlays = scores.size
        val bestScore = scores.maxOfOrNull { it.score } ?: 0
        val totalTimeSpent = scores.sumOf { it.timeSpent }
        val averageScore = scores.map { it.score }.average().toFloat()
        val level = calculateLevel(bestScore)
        val isCompleted = bestScore >= 100 // Assuming 100 is max score
        val achievements = getGameAchievements(context, gameId, scores)
        
        return GameProgress(
            gameId = gameId,
            totalPlays = totalPlays,
            bestScore = bestScore,
            totalTimeSpent = totalTimeSpent,
            averageScore = averageScore,
            level = level,
            isCompleted = isCompleted,
            achievements = achievements
        )
    }
    
    /**
     * Get all game progress
     */
    fun getAllGameProgress(context: Context): List<GameProgress> {
        val games = getAllGames(context)
        return games.mapNotNull { game ->
            getGameProgress(context, game.id)
        }
    }
    
    /**
     * Get total score across all games
     */
    fun getTotalScore(context: Context): Int {
        val scores = getGameScores(context)
        return scores.sumOf { it.score }
    }
    
    /**
     * Get completed games count
     */
    fun getCompletedGamesCount(context: Context): Int {
        val allProgress = getAllGameProgress(context)
        return allProgress.count { it.isCompleted }
    }
    
    /**
     * Get game statistics
     */
    fun getGameStatistics(context: Context): Map<String, Any> {
        val allProgress = getAllGameProgress(context)
        
        if (allProgress.isEmpty()) {
            return mapOf(
                "totalGamesPlayed" to 0,
                "totalTimeSpent" to 0,
                "averageScore" to 0f,
                "gamesCompleted" to 0,
                "totalAchievements" to 0
            )
        }
        
        val totalGamesPlayed = allProgress.sumOf { it.totalPlays }
        val totalTimeSpent = allProgress.sumOf { it.totalTimeSpent }
        val averageScore = allProgress.map { it.averageScore }.average().toFloat()
        val gamesCompleted = allProgress.count { it.isCompleted }
        val totalAchievements = allProgress.sumOf { it.achievements.size }
        
        return mapOf(
            "totalGamesPlayed" to totalGamesPlayed,
            "totalTimeSpent" to totalTimeSpent,
            "averageScore" to averageScore,
            "gamesCompleted" to gamesCompleted,
            "totalAchievements" to totalAchievements
        )
    }
    
    /**
     * Get default mini-games
     */
    private fun getDefaultGames(): List<MiniGame> {
        return listOf(
            MiniGame(
                id = "breathing_circle",
                title = "Breathing Circle",
                description = "Follow the circle to practice deep breathing",
                type = GameType.BREATHING,
                difficulty = GameDifficulty.EASY,
                duration = 60,
                points = 10,
                wellnessBenefit = "Reduces stress and anxiety",
                instructions = listOf(
                    "Watch the circle expand and contract",
                    "Breathe in as the circle grows",
                    "Breathe out as the circle shrinks",
                    "Follow the rhythm for 1 minute"
                )
            ),
            
            MiniGame(
                id = "memory_sequence",
                title = "Memory Sequence",
                description = "Remember and repeat the color sequence",
                type = GameType.MEMORY,
                difficulty = GameDifficulty.MEDIUM,
                duration = 120,
                points = 20,
                wellnessBenefit = "Improves memory and focus",
                instructions = listOf(
                    "Watch the sequence of colors",
                    "Tap the colors in the same order",
                    "The sequence gets longer each round",
                    "Try to remember as many as possible"
                )
            ),
            
            MiniGame(
                id = "reflex_tap",
                title = "Reflex Tap",
                description = "Tap the targets as quickly as possible",
                type = GameType.REFLEX,
                difficulty = GameDifficulty.HARD,
                duration = 90,
                points = 25,
                wellnessBenefit = "Improves reaction time and focus",
                instructions = listOf(
                    "Tap the targets that appear on screen",
                    "Be quick but accurate",
                    "Avoid tapping the wrong targets",
                    "Score points for speed and accuracy"
                )
            ),
            
            MiniGame(
                id = "meditation_timer",
                title = "Meditation Timer",
                description = "Guided meditation with visual cues",
                type = GameType.MEDITATION,
                difficulty = GameDifficulty.EASY,
                duration = 300,
                points = 15,
                wellnessBenefit = "Promotes mindfulness and relaxation",
                instructions = listOf(
                    "Find a comfortable position",
                    "Follow the guided breathing",
                    "Focus on the visual cues",
                    "Let your mind relax"
                )
            ),
            
            MiniGame(
                id = "puzzle_slider",
                title = "Puzzle Slider",
                description = "Solve the sliding puzzle",
                type = GameType.PUZZLE,
                difficulty = GameDifficulty.MEDIUM,
                duration = 180,
                points = 30,
                wellnessBenefit = "Enhances problem-solving skills",
                instructions = listOf(
                    "Arrange the tiles in numerical order",
                    "Slide tiles to empty spaces",
                    "Use strategy to solve efficiently",
                    "Complete in the shortest time possible"
                )
            ),
            
            MiniGame(
                id = "fitness_plank",
                title = "Plank Challenge",
                description = "Hold a plank position with visual guidance",
                type = GameType.FITNESS,
                difficulty = GameDifficulty.HARD,
                duration = 60,
                points = 20,
                wellnessBenefit = "Strengthens core muscles",
                instructions = listOf(
                    "Get into plank position",
                    "Hold the position steady",
                    "Follow the visual timer",
                    "Maintain proper form"
                )
            ),
            
            MiniGame(
                id = "mindfulness_coloring",
                title = "Mindfulness Coloring",
                description = "Color patterns to practice mindfulness",
                type = GameType.MINDFULNESS,
                difficulty = GameDifficulty.EASY,
                duration = 240,
                points = 15,
                wellnessBenefit = "Promotes creativity and relaxation",
                instructions = listOf(
                    "Choose your colors carefully",
                    "Color within the lines",
                    "Focus on the present moment",
                    "Enjoy the creative process"
                )
            ),
            
            MiniGame(
                id = "blink_sequence",
                title = "Blink Sequence",
                description = "Watch and repeat the blinking pattern",
                type = GameType.MEMORY,
                difficulty = GameDifficulty.MEDIUM,
                duration = 120,
                points = 20,
                wellnessBenefit = "Improves focus and attention",
                instructions = listOf(
                    "Watch the button blink",
                    "Count the number of blinks",
                    "Click the button the same number of times",
                    "Sequences get longer as you progress"
                )
            )
        )
    }
    
    private fun calculateLevel(score: Int): Int {
        return when {
            score >= 90 -> 5
            score >= 80 -> 4
            score >= 70 -> 3
            score >= 60 -> 2
            else -> 1
        }
    }
    
    private fun getGameAchievements(context: Context, gameId: String, scores: List<GameScore>): List<String> {
        val achievements = mutableListOf<String>()
        
        val bestScore = scores.maxOfOrNull { it.score } ?: 0
        val totalPlays = scores.size
        val averageScore = scores.map { it.score }.average()
        
        when {
            bestScore >= 100 -> achievements.add("🏆 Perfect Score")
            bestScore >= 90 -> achievements.add("🥇 Excellent")
            bestScore >= 80 -> achievements.add("🥈 Great Job")
            bestScore >= 70 -> achievements.add("🥉 Good Work")
        }
        
        when {
            totalPlays >= 50 -> achievements.add("🎮 Dedicated Player")
            totalPlays >= 20 -> achievements.add("🎯 Regular Player")
            totalPlays >= 10 -> achievements.add("🎲 Frequent Player")
        }
        
        when {
            averageScore >= 90 -> achievements.add("⭐ Consistent Performer")
            averageScore >= 80 -> achievements.add("📈 Improving Player")
        }
        
        return achievements
    }
    
    private fun updateGameProgress(context: Context, score: GameScore) {
        // This would update the game progress in SharedPreferences
        // Implementation depends on how you want to store progress data
    }
}

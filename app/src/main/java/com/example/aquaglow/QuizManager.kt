package com.example.aquaglow

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * QuizManager handles wellness quizzes and interactive content
 */
object QuizManager {
    
    private const val PREFS_NAME = "aquaglow_prefs"
    private const val QUIZZES_KEY = "wellness_quizzes"
    private const val QUIZ_RESULTS_KEY = "quiz_results"
    
    data class Quiz(
        val id: String,
        val title: String,
        val description: String,
        val category: QuizCategory,
        val questions: List<Question>,
        val estimatedTime: Int, // minutes
        val difficulty: Difficulty,
        val rewards: List<String> = emptyList()
    )
    
    data class Question(
        val id: String,
        val text: String,
        val type: QuestionType,
        val options: List<String> = emptyList(),
        val correctAnswer: Int = -1, // For multiple choice
        val points: Int = 1
    )
    
    enum class QuestionType {
        MULTIPLE_CHOICE, TRUE_FALSE, SCALE, TEXT_INPUT
    }
    
    enum class QuizCategory {
        MENTAL_HEALTH, PHYSICAL_HEALTH, NUTRITION, SLEEP, STRESS, HABITS, GENERAL
    }
    
    enum class Difficulty {
        EASY, MEDIUM, HARD
    }
    
    data class QuizResult(
        val quizId: String,
        val score: Int,
        val totalQuestions: Int,
        val percentage: Float,
        val completedAt: Long,
        val answers: List<Int>,
        val insights: List<String>
    )
    
    /**
     * Get all available quizzes
     */
    fun getAllQuizzes(context: Context): List<Quiz> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val quizzesJson = sharedPreferences.getString(QUIZZES_KEY, "[]")
        val type = object : TypeToken<List<Quiz>>() {}.type
        val savedQuizzes = gson.fromJson<List<Quiz>>(quizzesJson, type) ?: emptyList()
        
        // If no saved quizzes, return default quizzes
        return if (savedQuizzes.isEmpty()) {
            getDefaultQuizzes()
        } else {
            savedQuizzes
        }
    }
    
    /**
     * Get quizzes by category
     */
    fun getQuizzesByCategory(context: Context, category: QuizCategory): List<Quiz> {
        return getAllQuizzes(context).filter { it.category == category }
    }
    
    /**
     * Get quiz by ID
     */
    fun getQuizById(context: Context, quizId: String): Quiz? {
        return getAllQuizzes(context).find { it.id == quizId }
    }
    
    /**
     * Save quiz result
     */
    fun saveQuizResult(context: Context, result: QuizResult) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val resultsJson = sharedPreferences.getString(QUIZ_RESULTS_KEY, "[]")
        val type = object : TypeToken<List<QuizResult>>() {}.type
        val results = gson.fromJson<List<QuizResult>>(resultsJson, type)?.toMutableList() ?: mutableListOf()
        
        results.add(result)
        
        sharedPreferences.edit()
            .putString(QUIZ_RESULTS_KEY, gson.toJson(results))
            .apply()
    }
    
    /**
     * Get quiz results
     */
    fun getQuizResults(context: Context): List<QuizResult> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val resultsJson = sharedPreferences.getString(QUIZ_RESULTS_KEY, "[]")
        val type = object : TypeToken<List<QuizResult>>() {}.type
        return gson.fromJson<List<QuizResult>>(resultsJson, type) ?: emptyList()
    }
    
    /**
     * Get user's quiz statistics
     */
    fun getQuizStatistics(context: Context): Map<String, Any> {
        val results = getQuizResults(context)
        
        if (results.isEmpty()) {
            return mapOf(
                "totalQuizzes" to 0,
                "averageScore" to 0f,
                "totalPoints" to 0,
                "categoriesCompleted" to emptyList<String>()
            )
        }
        
        val totalQuizzes = results.size
        val averageScore = results.map { it.percentage }.average().toFloat()
        val totalPoints = results.sumOf { it.score }
        val categoriesCompleted = results.mapNotNull { result ->
            getQuizById(context, result.quizId)?.category?.name
        }.distinct()
        
        return mapOf(
            "totalQuizzes" to totalQuizzes,
            "averageScore" to averageScore,
            "totalPoints" to totalPoints,
            "categoriesCompleted" to categoriesCompleted
        )
    }
    
    /**
     * Get completed quizzes count
     */
    fun getCompletedQuizzesCount(context: Context): Int {
        return getQuizResults(context).size
    }
    
    /**
     * Generate insights based on quiz results
     */
    fun generateQuizInsights(context: Context): List<String> {
        val results = getQuizResults(context)
        val insights = mutableListOf<String>()
        
        if (results.isEmpty()) {
            insights.add("Complete your first quiz to get personalized insights!")
            return insights
        }
        
        val averageScore = results.map { it.percentage }.average()
        
        when {
            averageScore >= 80 -> {
                insights.add("Excellent! You have a strong understanding of wellness principles.")
                insights.add("Consider sharing your knowledge with friends or taking advanced quizzes.")
            }
            averageScore >= 60 -> {
                insights.add("Good progress! You're building a solid foundation in wellness knowledge.")
                insights.add("Try focusing on areas where you scored lower to improve your understanding.")
            }
            else -> {
                insights.add("There's room for improvement in your wellness knowledge.")
                insights.add("Consider taking beginner-level quizzes and reading wellness articles.")
            }
        }
        
        // Category-specific insights
        val categoryScores = results.groupBy { result ->
            getQuizById(context, result.quizId)?.category
        }.mapValues { (_, results) ->
            results.map { it.percentage }.average()
        }
        
        val lowestCategory = categoryScores.minByOrNull { it.value }
        if (lowestCategory != null && lowestCategory.value < 60) {
            insights.add("Focus on improving your knowledge in ${lowestCategory.key?.name?.lowercase()}.")
        }
        
        return insights
    }
    
    /**
     * Get default quizzes
     */
    private fun getDefaultQuizzes(): List<Quiz> {
        return listOf(
            Quiz(
                id = "mental_health_basics",
                title = "Mental Health Basics",
                description = "Test your knowledge about mental health and wellness",
                category = QuizCategory.MENTAL_HEALTH,
                difficulty = Difficulty.EASY,
                estimatedTime = 5,
                questions = listOf(
                    Question(
                        id = "q1",
                        text = "What is the recommended amount of sleep for adults?",
                        type = QuestionType.MULTIPLE_CHOICE,
                        options = listOf("6-7 hours", "7-9 hours", "9-10 hours", "10+ hours"),
                        correctAnswer = 1,
                        points = 1
                    ),
                    Question(
                        id = "q2",
                        text = "Regular exercise can help improve mental health",
                        type = QuestionType.TRUE_FALSE,
                        correctAnswer = 0, // True
                        points = 1
                    ),
                    Question(
                        id = "q3",
                        text = "How often should you practice mindfulness or meditation?",
                        type = QuestionType.MULTIPLE_CHOICE,
                        options = listOf("Never", "Once a month", "A few times a week", "Daily"),
                        correctAnswer = 3,
                        points = 1
                    )
                ),
                rewards = listOf("🧠 Mental Health Badge", "50 Points")
            ),
            
            Quiz(
                id = "stress_management",
                title = "Stress Management",
                description = "Learn about effective stress management techniques",
                category = QuizCategory.STRESS,
                difficulty = Difficulty.MEDIUM,
                estimatedTime = 8,
                questions = listOf(
                    Question(
                        id = "q1",
                        text = "What is the first step in managing stress?",
                        type = QuestionType.MULTIPLE_CHOICE,
                        options = listOf("Ignore it", "Identify the source", "Take medication", "Avoid the situation"),
                        correctAnswer = 1,
                        points = 2
                    ),
                    Question(
                        id = "q2",
                        text = "Deep breathing exercises can help reduce stress",
                        type = QuestionType.TRUE_FALSE,
                        correctAnswer = 0, // True
                        points = 1
                    ),
                    Question(
                        id = "q3",
                        text = "Rate your current stress level (1-10)",
                        type = QuestionType.SCALE,
                        points = 1
                    )
                ),
                rewards = listOf("🧘‍♀️ Stress Master Badge", "75 Points")
            ),
            
            Quiz(
                id = "nutrition_fundamentals",
                title = "Nutrition Fundamentals",
                description = "Test your knowledge about healthy eating",
                category = QuizCategory.NUTRITION,
                difficulty = Difficulty.MEDIUM,
                estimatedTime = 10,
                questions = listOf(
                    Question(
                        id = "q1",
                        text = "How many servings of fruits and vegetables should you eat daily?",
                        type = QuestionType.MULTIPLE_CHOICE,
                        options = listOf("2-3", "3-4", "5-7", "8-10"),
                        correctAnswer = 2,
                        points = 2
                    ),
                    Question(
                        id = "q2",
                        text = "Drinking water is important for overall health",
                        type = QuestionType.TRUE_FALSE,
                        correctAnswer = 0, // True
                        points = 1
                    ),
                    Question(
                        id = "q3",
                        text = "What percentage of your plate should be vegetables?",
                        type = QuestionType.MULTIPLE_CHOICE,
                        options = listOf("25%", "50%", "75%", "100%"),
                        correctAnswer = 1,
                        points = 2
                    )
                ),
                rewards = listOf("🥗 Nutrition Expert Badge", "100 Points")
            ),
            
            Quiz(
                id = "sleep_hygiene",
                title = "Sleep Hygiene",
                description = "Learn about healthy sleep habits",
                category = QuizCategory.SLEEP,
                difficulty = Difficulty.EASY,
                estimatedTime = 6,
                questions = listOf(
                    Question(
                        id = "q1",
                        text = "What is the best time to stop using electronic devices before bed?",
                        type = QuestionType.MULTIPLE_CHOICE,
                        options = listOf("30 minutes", "1 hour", "2 hours", "3 hours"),
                        correctAnswer = 1,
                        points = 1
                    ),
                    Question(
                        id = "q2",
                        text = "A consistent sleep schedule is important for good sleep",
                        type = QuestionType.TRUE_FALSE,
                        correctAnswer = 0, // True
                        points = 1
                    ),
                    Question(
                        id = "q3",
                        text = "What temperature is best for sleep?",
                        type = QuestionType.MULTIPLE_CHOICE,
                        options = listOf("Hot (75°F+)", "Warm (70-75°F)", "Cool (65-70°F)", "Cold (60°F or less)"),
                        correctAnswer = 2,
                        points = 1
                    )
                ),
                rewards = listOf("😴 Sleep Master Badge", "60 Points")
            )
        )
    }
}

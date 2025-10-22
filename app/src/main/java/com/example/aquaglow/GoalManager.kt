package com.example.aquaglow

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * GoalManager handles user goals and targets
 */
object GoalManager {
    
    private const val PREFS_NAME = "aquaglow_prefs"
    private const val GOALS_KEY = "user_goals"
    private const val WEEKLY_GOALS_KEY = "weekly_goals"
    
    data class Goal(
        val id: String,
        val title: String,
        val description: String,
        val targetValue: Int,
        val currentValue: Int = 0,
        val unit: String,
        val category: String,
        val isCompleted: Boolean = false,
        val createdAt: Long = System.currentTimeMillis(),
        val completedAt: Long = 0L
    )
    
    data class WeeklyGoal(
        val weekStart: String, // YYYY-MM-DD format
        val habitGoal: Int, // Number of habits to complete daily
        val moodGoal: Int, // Number of days to log mood
        val stepGoal: Int, // Daily step target
        val hydrationGoal: Int, // Daily hydration reminders
        val currentHabitDays: Int = 0,
        val currentMoodDays: Int = 0,
        val currentStepDays: Int = 0,
        val currentHydrationDays: Int = 0
    )
    
    /**
     * Get all user goals
     */
    fun getAllGoals(context: Context): List<Goal> {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val goalsJson = sharedPreferences.getString(GOALS_KEY, "[]")
        val type = object : TypeToken<List<Goal>>() {}.type
        return gson.fromJson<List<Goal>>(goalsJson, type) ?: emptyList()
    }
    
    /**
     * Add a new goal
     */
    fun addGoal(context: Context, goal: Goal) {
        val goals = getAllGoals(context).toMutableList()
        goals.add(goal)
        saveGoals(context, goals)
    }
    
    /**
     * Update goal progress
     */
    fun updateGoalProgress(context: Context, goalId: String, newValue: Int) {
        val goals = getAllGoals(context).toMutableList()
        val goalIndex = goals.indexOfFirst { it.id == goalId }
        if (goalIndex >= 0) {
            val goal = goals[goalIndex]
            val updatedGoal = goal.copy(
                currentValue = newValue,
                isCompleted = newValue >= goal.targetValue,
                completedAt = if (newValue >= goal.targetValue) System.currentTimeMillis() else goal.completedAt
            )
            goals[goalIndex] = updatedGoal
            saveGoals(context, goals)
        }
    }
    
    /**
     * Delete a goal
     */
    fun deleteGoal(context: Context, goalId: String) {
        val goals = getAllGoals(context).toMutableList()
        goals.removeAll { it.id == goalId }
        saveGoals(context, goals)
    }
    
    /**
     * Get current weekly goal
     */
    fun getCurrentWeeklyGoal(context: Context): WeeklyGoal? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val weeklyGoalsJson = sharedPreferences.getString(WEEKLY_GOALS_KEY, "[]")
        val type = object : TypeToken<List<WeeklyGoal>>() {}.type
        val weeklyGoals = gson.fromJson<List<WeeklyGoal>>(weeklyGoalsJson, type) ?: emptyList()
        
        val currentWeek = getCurrentWeekStart()
        return weeklyGoals.find { it.weekStart == currentWeek }
    }
    
    /**
     * Set weekly goal
     */
    fun setWeeklyGoal(context: Context, weeklyGoal: WeeklyGoal) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        val weeklyGoalsJson = sharedPreferences.getString(WEEKLY_GOALS_KEY, "[]")
        val type = object : TypeToken<List<WeeklyGoal>>() {}.type
        val weeklyGoals = gson.fromJson<List<WeeklyGoal>>(weeklyGoalsJson, type)?.toMutableList() ?: mutableListOf()
        
        val existingIndex = weeklyGoals.indexOfFirst { it.weekStart == weeklyGoal.weekStart }
        if (existingIndex >= 0) {
            weeklyGoals[existingIndex] = weeklyGoal
        } else {
            weeklyGoals.add(weeklyGoal)
        }
        
        sharedPreferences.edit()
            .putString(WEEKLY_GOALS_KEY, gson.toJson(weeklyGoals))
            .apply()
    }
    
    /**
     * Update weekly goal progress
     */
    fun updateWeeklyGoalProgress(context: Context, category: String, completed: Boolean) {
        val currentGoal = getCurrentWeeklyGoal(context) ?: return
        val updatedGoal = when (category) {
            "habits" -> currentGoal.copy(
                currentHabitDays = if (completed) currentGoal.currentHabitDays + 1 else currentGoal.currentHabitDays
            )
            "mood" -> currentGoal.copy(
                currentMoodDays = if (completed) currentGoal.currentMoodDays + 1 else currentGoal.currentMoodDays
            )
            "steps" -> currentGoal.copy(
                currentStepDays = if (completed) currentGoal.currentStepDays + 1 else currentGoal.currentStepDays
            )
            "hydration" -> currentGoal.copy(
                currentHydrationDays = if (completed) currentGoal.currentHydrationDays + 1 else currentGoal.currentHydrationDays
            )
            else -> currentGoal
        }
        
        setWeeklyGoal(context, updatedGoal)
    }
    
    /**
     * Get weekly goal progress percentage
     */
    fun getWeeklyGoalProgress(context: Context): Int {
        val goal = getCurrentWeeklyGoal(context) ?: return 0
        
        val totalPossible = goal.habitGoal + goal.moodGoal + goal.stepGoal + goal.hydrationGoal
        val totalCompleted = goal.currentHabitDays + goal.currentMoodDays + goal.currentStepDays + goal.currentHydrationDays
        
        return if (totalPossible > 0) {
            (totalCompleted.toFloat() / totalPossible * 100).toInt()
        } else 0
    }
    
    /**
     * Get completed goals count
     */
    fun getCompletedGoalsCount(context: Context): Int {
        return getAllGoals(context).count { it.isCompleted }
    }
    
    /**
     * Get total goals count
     */
    fun getTotalGoalsCount(context: Context): Int {
        return getAllGoals(context).size
    }
    
    private fun saveGoals(context: Context, goals: List<Goal>) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val gson = Gson()
        sharedPreferences.edit()
            .putString(GOALS_KEY, gson.toJson(goals))
            .apply()
    }
    
    private fun getCurrentWeekStart(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(calendar.time)
    }
}








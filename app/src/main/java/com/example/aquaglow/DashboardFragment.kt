package com.example.aquaglow

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

/**
 * DashboardFragment - Home screen showing daily wellness summary
 * Combines: habits progress, mood status, water intake, steps
 */
class DashboardFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gson: Gson
    
    // Views
    private lateinit var welcomeText: TextView
    private lateinit var motivationQuoteText: TextView
    private lateinit var habitsProgressBar: ProgressBar
    private lateinit var habitsProgressText: TextView
    private lateinit var moodStatusText: TextView
    private lateinit var waterProgressBar: ProgressBar
    private lateinit var waterProgressText: TextView
    private lateinit var stepsProgressBar: ProgressBar
    private lateinit var stepsProgressText: TextView
    private lateinit var streakText: TextView
    private lateinit var wellnessScoreText: TextView
    
    
    // Cards
    private lateinit var habitsCard: MaterialCardView
    private lateinit var moodCard: MaterialCardView
    private lateinit var waterCard: MaterialCardView
    private lateinit var stepsCard: MaterialCardView
    
    // Floating button
    private lateinit var gameFloatingButton: com.google.android.material.floatingactionbutton.FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupSharedPreferences()
        loadDashboardData()
        setupCardClicks()
        setupGlowEffects()
    }

    private fun initializeViews(view: View) {
        welcomeText = view.findViewById(R.id.welcomeText)
        motivationQuoteText = view.findViewById(R.id.motivationQuoteText)
        habitsProgressBar = view.findViewById(R.id.habitsProgressBar)
        habitsProgressText = view.findViewById(R.id.habitsProgressText)
        moodStatusText = view.findViewById(R.id.moodStatusText)
        waterProgressBar = view.findViewById(R.id.waterProgressBar)
        waterProgressText = view.findViewById(R.id.waterProgressText)
        stepsProgressBar = view.findViewById(R.id.stepsProgressBar)
        stepsProgressText = view.findViewById(R.id.stepsProgressText)
        streakText = view.findViewById(R.id.streakText)
        wellnessScoreText = view.findViewById(R.id.wellnessScoreText)
        
        
        habitsCard = view.findViewById(R.id.habitsCard)
        moodCard = view.findViewById(R.id.moodCard)
        waterCard = view.findViewById(R.id.waterCard)
        stepsCard = view.findViewById(R.id.stepsCard)
        gameFloatingButton = view.findViewById(R.id.gameFloatingButton)
    }

    private fun setupSharedPreferences() {
        sharedPreferences = requireContext().getSharedPreferences("aquaglow_prefs", 0)
        gson = Gson()
    }

    private fun loadDashboardData() {
        // Welcome message
        val userName = sharedPreferences.getString("auth_user_name", "User") ?: "User"
        val greeting = getGreeting()
        welcomeText.text = "$greeting, $userName! 👋"
        
        // Motivation quote
        motivationQuoteText.text = getRandomMotivationQuote()
        
        // Load habits progress
        loadHabitsProgress()
        
        // Load mood status
        loadMoodStatus()
        
        // Load water intake (if tracked)
        loadWaterIntake()
        
        // Load steps
        loadStepsProgress()
        
        // Load streak
        loadStreakInfo()
        
        // Load wellness score
        loadWellnessScore()
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }

    private fun getRandomMotivationQuote(): String {
        val quotes = arrayOf(
            "Every small step counts towards wellness 💪",
            "You're doing amazing! Keep it up! ⭐",
            "Progress, not perfection 🌟",
            "Your health journey is unique and beautiful 🌸",
            "One day at a time, one habit at a time 🌱",
            "You're stronger than you think 💙",
            "Wellness is a journey, not a destination 🚀",
            "Today is a fresh start! ✨"
        )
        return quotes.random()
    }

    private fun loadHabitsProgress() {
        val habitsJson = sharedPreferences.getString("habits_list", "[]")
        val type = object : TypeToken<List<HabitsFragment.Habit>>() {}.type
        val habits = gson.fromJson<List<HabitsFragment.Habit>>(habitsJson, type) ?: emptyList()
        
        if (habits.isNotEmpty()) {
            val completed = habits.count { it.isCompleted }
            val total = habits.size
            val percentage = (completed * 100) / total
            
            habitsProgressBar.progress = percentage
            habitsProgressText.text = "$completed/$total Habits"
        } else {
            habitsProgressBar.progress = 0
            habitsProgressText.text = "No habits tracked yet. Start by adding one!"
        }
    }

    private fun loadMoodStatus() {
        val moodJson = sharedPreferences.getString("mood_entries", "[]")
        val type = object : TypeToken<List<MoodFragment.MoodEntry>>() {}.type
        val moods = gson.fromJson<List<MoodFragment.MoodEntry>>(moodJson, type) ?: emptyList()
        
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayMood = moods.find {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.timestamp) == today
        }
        
        moodStatusText.text = if (todayMood != null) {
            "${todayMood.moodEmoji} ${todayMood.moodName}"
        } else {
            "Not logged today"
        }
    }

    private fun loadWaterIntake() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        // Fix: Use the correct key for water intake data (ml instead of glasses)
        val currentWaterMl = sharedPreferences.getInt("water_intake_ml_$today", 0)
        val dailyGoal = sharedPreferences.getInt("water_daily_goal", 8)
        
        // Convert ml to glasses (assuming 250ml per glass)
        val currentGlasses = currentWaterMl / 250
        val percentage = if (dailyGoal > 0) ((currentGlasses.toFloat() / dailyGoal) * 100).toInt().coerceAtMost(100) else 0
        waterProgressBar.progress = percentage
        waterProgressText.text = "$currentGlasses/$dailyGoal glasses"
    }

    private fun loadStepsProgress() {
        try {
            val steps = SensorManager.getCurrentStepCount(requireContext())
            val goal = SensorManager.getDailyStepGoal(requireContext())
            val percentage = if (goal > 0) ((steps.toFloat() / goal) * 100).toInt() else 0
            
            stepsProgressBar.progress = percentage.coerceAtMost(100)
            stepsProgressText.text = "$steps / $goal steps"
        } catch (e: Exception) {
            // Fallback if sensor data is not available
            stepsProgressBar.progress = 0
            stepsProgressText.text = "Steps not available"
        }
    }

    private fun loadStreakInfo() {
        try {
            // Achievement streak removed - using default value
            streakText.text = "🔥 0 day streak"
        } catch (e: Exception) {
            // Fallback if streak data is not available
            streakText.text = "🔥 0 day streak"
        }
    }

    private fun loadWellnessScore() {
        try {
            // Achievement score removed - using default value
            wellnessScoreText.text = "Wellness Score: 0/100"
        } catch (e: Exception) {
            // Fallback if wellness score calculation fails
            wellnessScoreText.text = "Wellness Score: 0/100"
        }
    }

    private fun setupCardClicks() {
        // Card clicks for quick navigation
        habitsCard.setOnClickListener {
            navigateToBottomNavTab(R.id.trackFragment)
        }
        
        moodCard.setOnClickListener {
            navigateToBottomNavTab(R.id.trackFragment)
        }
        
        waterCard.setOnClickListener {
            navigateToBottomNavTab(R.id.trackFragment)
        }
        
        stepsCard.setOnClickListener {
            navigateToBottomNavTab(R.id.stepsFragment)
        }
        
        // Floating game button
        gameFloatingButton.setOnClickListener {
            findNavController().navigate(R.id.gamesFragment)
        }
    }
    
    /**
     * Navigate to bottom navigation tabs properly
     */
    private fun navigateToBottomNavTab(tabId: Int) {
        val bottomNav = activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigationView)
        bottomNav?.selectedItemId = tabId
    }

    private fun setupGlowEffects() {
        GlowAnimationUtils.createBreathingEffect(welcomeText, 4000L)
        GlowAnimationUtils.createBreathingEffect(motivationQuoteText, 5000L)
        GlowAnimationUtils.createPulseEffect(wellnessScoreText, 3000L)
        GlowAnimationUtils.createPulseEffect(gameFloatingButton, 2000L)
    }

    override fun onResume() {
        super.onResume()
        // Refresh dashboard data when returning
        loadDashboardData()
    }
}


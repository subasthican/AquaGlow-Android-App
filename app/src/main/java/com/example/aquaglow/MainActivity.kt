package com.example.aquaglow

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.aquaglow.SensorManager

/**
 * MainActivity hosts the main navigation with BottomNavigationView
 * containing three fragments: Habits, Mood, and Settings
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var welcomeText: TextView
    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_main)

            // Create notification channels
            createNotificationChannels()

            // Initialize views first
            initializeViews()

            // Setup navigation
            setupNavigation()

            // Load user data
            loadUserData()

            // Handle widget intents
            handleWidgetIntents()

            // Setup sensor tracking (with delay to prevent startup issues)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                setupSensorTracking()
            }, 1000)

            // Setup animations last
            setupAnimations()
        } catch (e: Exception) {
            e.printStackTrace()
            // If MainActivity fails to initialize, go back to splash
            val intent = Intent(this, SplashActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
    
    /**
     * Creates notification channels for the app
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            
            // Hydration reminder channel
            val hydrationChannel = android.app.NotificationChannel(
                "hydration_reminders",
                "Hydration Reminders",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to drink water and stay hydrated"
                enableVibration(true)
                setShowBadge(true)
            }
            
            notificationManager.createNotificationChannel(hydrationChannel)
            android.util.Log.d("NotificationChannel", "Hydration reminder channel created successfully")
        } else {
            android.util.Log.d("NotificationChannel", "Android version < O, no channels needed")
        }
    }

    /**
     * Initializes all UI views
     */
    private fun initializeViews() {
        try {
            bottomNavigationView = findViewById(R.id.bottomNavigationView)
            navHostFragment = supportFragmentManager
                .findFragmentById(R.id.navHostFragment) as? NavHostFragment
                ?: throw IllegalStateException("NavHostFragment not found")
            welcomeText = findViewById(R.id.welcomeText)
            sharedPreferences = getSharedPreferences("aquaglow_prefs", MODE_PRIVATE)
            
            // Setup glow effects after views are initialized
            setupGlowEffects()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    
    /**
     * Sets up the bottom navigation with NavController
     */
    private fun setupNavigation() {
        try {
            val navController = navHostFragment.navController
            
            // Ensure the navigation graph is loaded
            if (navController.currentDestination == null) {
                throw IllegalStateException("Navigation graph not loaded")
            }
            
            bottomNavigationView.setupWithNavController(navController)
            
            // Set default selected item - NEW 5-TAB STRUCTURE
            bottomNavigationView.selectedItemId = R.id.dashboardFragment
            
            // Add navigation listener to update welcome text - NEW 5-TAB STRUCTURE
            navController.addOnDestinationChangedListener { _, destination, _ ->
                try {
                    when (destination.id) {
                        R.id.dashboardFragment -> {
                            welcomeText.text = getString(R.string.dashboard_title)
                            welcomeText.visibility = View.VISIBLE
                        }
                        R.id.trackFragment -> {
                            welcomeText.text = getString(R.string.dashboard_track_progress)
                            welcomeText.visibility = View.VISIBLE
                        }
                        R.id.insightsFragment -> {
                            welcomeText.text = "Your Wellness Insights"
                            welcomeText.visibility = View.VISIBLE
                        }
                        R.id.stepsFragment -> {
                            welcomeText.text = "Steps & Activity"
                            welcomeText.visibility = View.VISIBLE
                        }
                        R.id.profileFragment -> {
                            welcomeText.text = "Your Profile & Settings"
                            welcomeText.visibility = View.VISIBLE
                        }
                        // Secondary pages (accessed from Profile)
                        R.id.settingsFragment -> {
                            welcomeText.text = "App Settings"
                            welcomeText.visibility = View.VISIBLE
                        }
                        R.id.goalsFragment -> {
                            welcomeText.text = "Your Goals"
                            welcomeText.visibility = View.VISIBLE
                        }
                        R.id.gamesFragment -> {
                            welcomeText.text = "Wellness Games"
                            welcomeText.visibility = View.VISIBLE
                        }
                        R.id.quizzesFragment -> {
                            welcomeText.text = "Wellness Quizzes"
                            welcomeText.visibility = View.VISIBLE
                        }
                        R.id.healthMetricsFragment -> {
                            welcomeText.text = "Health Metrics"
                            welcomeText.visibility = View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Navigation errors shouldn't crash the app
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    
        /**
         * Loads user data and displays personalized welcome message
         */
        private fun loadUserData() {
            val userName = sharedPreferences.getString("user_name", "User") ?: "User"
            welcomeText.text = "Welcome back, $userName! 👋"
        }

    /**
     * Sets up entrance animations for a smooth user experience
     */
    private fun setupAnimations() {
        try {
            // Fade in animation for the header
            val fadeIn = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in)
            findViewById<View>(R.id.headerLayout)?.startAnimation(fadeIn)

            // Slide up animation for the content
            val slideUp = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up)
            findViewById<View>(R.id.navHostFragment)?.startAnimation(slideUp)

            // Bounce animation for the bottom navigation
            val bounceIn = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.bounce_in)
            bottomNavigationView.startAnimation(bounceIn)
        } catch (e: Exception) {
            e.printStackTrace()
            // Animation errors shouldn't crash the app
        }
    }

    /**
     * Handles intents from widget clicks and notifications - UPDATED FOR NEW STRUCTURE
     */
    private fun handleWidgetIntents() {
        val intent = this.intent
        when {
            intent.getBooleanExtra("open_hydration", false) -> {
                // Navigate to Track tab (Hydration) from hydration notification
                bottomNavigationView.selectedItemId = R.id.trackFragment
            }
            intent.getBooleanExtra("open_mood_fragment", false) -> {
                // Navigate to Track tab (contains mood)
                bottomNavigationView.selectedItemId = R.id.trackFragment
            }
            intent.getBooleanExtra("open_statistics_fragment", false) -> {
                // Navigate to Insights tab (contains statistics)
                bottomNavigationView.selectedItemId = R.id.insightsFragment
            }
            intent.getBooleanExtra("open_settings_fragment", false) -> {
                // Navigate to Profile tab (contains settings)
                bottomNavigationView.selectedItemId = R.id.profileFragment
            }
        }
    }

    /**
     * Sets up sensor tracking if enabled
     */
    private fun setupSensorTracking() {
        try {
            if (SensorManager.isSensorTrackingEnabled(this)) {
                SensorManager.startStepCounter(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Sensor errors shouldn't crash the app
        }
    }

    /**
     * Sets up glow effects for enhanced visual appeal
     */
    private fun setupGlowEffects() {
        // Add breathing effect to the welcome text
        // GlowAnimationUtils.createBreathingEffect(welcomeText, 5000L)
        
        // Add pulse effect to bottom navigation
        // GlowAnimationUtils.createPulseEffect(bottomNavigationView, 4000L)
    }
}

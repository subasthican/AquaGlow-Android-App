package com.example.aquaglow

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

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
        setContentView(R.layout.activity_main)

        // Initialize views
        initializeViews()

        // Setup navigation
        setupNavigation()

        // Load user data
        loadUserData()

        // Setup animations
        setupAnimations()
    }
    
    /**
     * Initializes all UI views
     */
    private fun initializeViews() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        welcomeText = findViewById(R.id.welcomeText)
        sharedPreferences = getSharedPreferences("aquaglow_prefs", MODE_PRIVATE)
    }
    
    /**
     * Sets up the bottom navigation with NavController
     */
    private fun setupNavigation() {
        val navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)
        
        // Set default selected item
        bottomNavigationView.selectedItemId = R.id.habitsFragment
        
        // Add navigation listener to update welcome text
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.habitsFragment -> {
                    welcomeText.text = "Track your daily habits"
                    welcomeText.visibility = View.VISIBLE
                }
                R.id.moodFragment -> {
                    welcomeText.text = "How are you feeling today?"
                    welcomeText.visibility = View.VISIBLE
                }
                R.id.statisticsFragment -> {
                    welcomeText.text = "View your wellness insights"
                    welcomeText.visibility = View.VISIBLE
                }
                R.id.settingsFragment -> {
                    welcomeText.text = "Customize your experience"
                    welcomeText.visibility = View.VISIBLE
                }
            }
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
            // Fade in animation for the header
            val fadeIn = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.fade_in)
            findViewById<View>(R.id.headerLayout).startAnimation(fadeIn)

            // Slide up animation for the content
            val slideUp = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.slide_up)
            findViewById<View>(R.id.navHostFragment).startAnimation(slideUp)

            // Bounce animation for the bottom navigation
            val bounceIn = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.bounce_in)
            bottomNavigationView.startAnimation(bounceIn)
        }
    }
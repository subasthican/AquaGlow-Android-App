package com.example.aquaglow

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * SplashActivity displays the AquaGlow logo and app name for 3 seconds
 * with fade-in animation, then navigates to either OnboardingActivity
 * (first run) or MainActivity (subsequent runs)
 */
class SplashActivity : AppCompatActivity() {
    
    private lateinit var sharedPreferences: SharedPreferences
    private val splashDuration = 3000L // 3 seconds
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        
        // Set up UI elements
        setupUI()
        
        // Start fade-in animation
        startFadeInAnimation()
        
        // Navigate after splash duration
        navigateAfterDelay()
    }
    
    /**
     * Sets up the UI elements for the splash screen
     */
    private fun setupUI() {
        val logoImageView = findViewById<ImageView>(R.id.logoImageView)
        val appNameTextView = findViewById<TextView>(R.id.appNameTextView)
        
        // Set logo
        logoImageView.setImageResource(R.drawable.ic_aquaglow_logo)
        
        // Set app name with AquaGlow branding
        appNameTextView.text = getString(R.string.app_name)
        appNameTextView.setTextColor(ContextCompat.getColor(this, R.color.white))
    }
    
    /**
     * Starts the fade-in animation for logo and app name
     */
    private fun startFadeInAnimation() {
        val logoImageView = findViewById<ImageView>(R.id.logoImageView)
        val appNameTextView = findViewById<TextView>(R.id.appNameTextView)
        
        val fadeIn = AlphaAnimation(0.0f, 1.0f).apply {
            duration = 1000
            fillAfter = true
        }
        
        logoImageView.startAnimation(fadeIn)
        appNameTextView.startAnimation(fadeIn)
    }
    
    /**
     * Navigates to the appropriate activity after splash duration
     */
    private fun navigateAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            val isOnboardingCompleted = sharedPreferences.getBoolean("onboarding_completed", false)
            
            val intent = if (isOnboardingCompleted) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, OnboardingActivity::class.java)
            }
            
            startActivity(intent)
            finish()
        }, splashDuration)
    }
    
    companion object {
        const val PREFS_NAME = "AquaGlowPrefs"
        const val KEY_FIRST_RUN = "first_run"
    }
}

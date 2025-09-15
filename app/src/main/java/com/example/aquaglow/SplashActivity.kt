package com.example.aquaglow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.aquaglow.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySplashBinding
    private val splashDuration = 3000L // 3 seconds
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Hide system bars for immersive experience
        hideSystemBars()
        
        // Start animations
        startAnimations()
        
        // Set up skip button click listener
        setupSkipButton()
        
        // Auto transition after splash duration
        scheduleAutoTransition()
    }
    
    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
    
    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
    
    private fun startAnimations() {
        // Logo fade-in animation
        val logoFadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 800
            startOffset = 200
        }
        
        // Logo glow pulse animation
        val logoPulse = ScaleAnimation(
            1f, 1.1f, 1f, 1.1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 1500
            repeatCount = Animation.INFINITE
            repeatMode = Animation.REVERSE
            startOffset = 1000
        }
        
        // App name fade-in animation
        val appNameFadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 600
            startOffset = 600
        }
        
        // Tagline fade-in animation
        val taglineFadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 600
            startOffset = 1000
        }
        
        // Skip button fade-in animation
        val skipButtonFadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 400
            startOffset = 1500
        }
        
        // Apply animations
        binding.logoImage.startAnimation(logoFadeIn)
        binding.logoImage.startAnimation(logoPulse)
        binding.appNameText.startAnimation(appNameFadeIn)
        binding.taglineText.startAnimation(taglineFadeIn)
        binding.skipButton.startAnimation(skipButtonFadeIn)
    }
    
    private fun setupSkipButton() {
        binding.skipButton.setOnClickListener {
            navigateNext()
        }
    }
    
    private fun scheduleAutoTransition() {
        Handler(Looper.getMainLooper()).postDelayed({
            navigateNext()
        }, splashDuration)
    }
    
    private fun navigateNext() {
        val prefs = getSharedPreferences(OnboardingActivity.PREFS, Context.MODE_PRIVATE)
        val onboardingDone = prefs.getBoolean(OnboardingActivity.KEY_ONBOARDING_COMPLETE, false)
        val isLoggedIn = prefs.getBoolean(AuthActivity.KEY_IS_LOGGED_IN, false)

        val next = when {
            !onboardingDone -> Intent(this, OnboardingActivity::class.java)
            !isLoggedIn -> Intent(this, AuthActivity::class.java)
            else -> Intent(this, MainActivity::class.java)
        }
        startActivity(next)
        finish()
        
        // Add smooth transition animation
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
    
    override fun onResume() {
        super.onResume()
        // Ensure system bars remain hidden when returning to splash
        hideSystemBars()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Disable back button on splash screen
        // Do nothing to prevent going back
    }
}

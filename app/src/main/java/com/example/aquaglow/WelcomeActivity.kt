package com.example.aquaglow

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/**
 * WelcomeActivity displays a personalized welcome screen after name input
 * with app description and navigation options.
 */
class WelcomeActivity : AppCompatActivity() {

    private lateinit var welcomeIllustration: ImageView
    private lateinit var welcomeText: TextView
    private lateinit var userNameText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var termsText: TextView
    private lateinit var privacyText: TextView
    private lateinit var backButton: MaterialButton
    private lateinit var letsBeginButton: MaterialButton
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        initializeViews()
        setupSharedPreferences()
        loadUserName()
        setupListeners()
        applyAnimations()
        setupBackPressedCallback()
        setupGlowEffects()
    }

    /**
     * Initialize all UI views from the layout
     */
    private fun initializeViews() {
        welcomeIllustration = findViewById(R.id.welcomeIllustration)
        welcomeText = findViewById(R.id.welcomeText)
        userNameText = findViewById(R.id.userNameText)
        descriptionText = findViewById(R.id.descriptionText)
        termsText = findViewById(R.id.termsText)
        privacyText = findViewById(R.id.privacyText)
        backButton = findViewById(R.id.backButton)
        letsBeginButton = findViewById(R.id.letsBeginButton)
    }

    /**
     * Set up SharedPreferences for data access
     */
    private fun setupSharedPreferences() {
        sharedPreferences = getSharedPreferences("aquaglow_prefs", MODE_PRIVATE)
    }

    /**
     * Load and display the user's name
     */
    private fun loadUserName() {
        val userName = sharedPreferences.getString("user_name", "User") ?: "User"
        userNameText.text = userName
    }

    /**
     * Set up click listeners for buttons and links
     */
    private fun setupListeners() {
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        letsBeginButton.setOnClickListener {
            navigateToMainActivity()
        }

        // Terms of Use link
        termsText.setOnClickListener {
            val intent = Intent(this, TermsOfServiceActivity::class.java)
            startActivity(intent)
        }

        // Privacy Policy link
        privacyText.setOnClickListener {
            val intent = Intent(this, PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Apply entrance animations to UI elements
     */
    private fun applyAnimations() {
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        // Stagger the animations
        welcomeIllustration.startAnimation(fadeInAnimation)
        
        welcomeText.startAnimation(slideUpAnimation)
        userNameText.startAnimation(slideUpAnimation)
        descriptionText.startAnimation(slideUpAnimation)
        termsText.startAnimation(slideUpAnimation)
        privacyText.startAnimation(slideUpAnimation)
        backButton.startAnimation(slideUpAnimation)
        letsBeginButton.startAnimation(slideUpAnimation)
    }

    /**
     * Navigate to MainActivity
     */
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Set up back pressed callback for modern Android
     */
    private fun setupBackPressedCallback() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Go back to name input screen
                val intent = Intent(this@WelcomeActivity, NameInputActivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }

    private fun setupGlowEffects() {
        // Add breathing effect to welcome text
        GlowAnimationUtils.createBreathingEffect(welcomeText, 4000L)
        GlowAnimationUtils.createBreathingEffect(userNameText, 4500L)
        
        // Add glow movement to description
        GlowAnimationUtils.createBreathingEffect(descriptionText, 4000L)
        
        // Add twinkling effect to illustration
        GlowAnimationUtils.createTwinkleEffect(welcomeIllustration, 5000L)
        
        // Add pulse effect to terms and privacy links
        GlowAnimationUtils.createPulseEffect(termsText, 3000L)
        GlowAnimationUtils.createPulseEffect(privacyText, 3500L)
        
        // Apply material glow effects to button
        GlowAnimationUtils.applyMaterialGlow(letsBeginButton)
    }
}

package com.example.aquaglow

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * NameInputActivity allows users to enter their name during the onboarding process
 * This creates a personalized experience for the AquaGlow wellness app
 */
class NameInputActivity : AppCompatActivity() {
    
    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var nameEditText: TextInputEditText
    private lateinit var letsBeginButton: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_name_input)
        
        initializeViews()
        setupAnimations()
        setupNameInput()
        setupLetsBeginButton()
        setupGlowEffects()
    }
    
    /**
     * Initialize all views and set up click listeners
     */
    private fun initializeViews() {
        nameInputLayout = findViewById(R.id.nameInputLayout)
        nameEditText = findViewById(R.id.nameEditText)
        letsBeginButton = findViewById(R.id.letsBeginButton)
    }
    
    /**
     * Set up entrance animations for a smooth user experience
     */
    private fun setupAnimations() {
        // Fade in animation for the main content
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        findViewById<View>(R.id.mainContent).startAnimation(fadeIn)
        
        // Slide up animation for the input section
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        findViewById<View>(R.id.inputSection).startAnimation(slideUp)
    }
    
        /**
         * Set up the name input field with validation and real-time feedback
         */
        private fun setupNameInput() {
            nameEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    validateNameInput(s.toString())
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            // Set focus and show keyboard
            nameEditText.requestFocus()
        }
    
    /**
     * Validate the name input and provide real-time feedback
     */
    private fun validateNameInput(name: String) {
        when {
            name.isEmpty() -> {
                nameInputLayout.error = null
                nameInputLayout.helperText = null
                letsBeginButton.isEnabled = false
                letsBeginButton.alpha = 0.6f
            }
            name.length < 2 -> {
                nameInputLayout.error = "Name must be at least 2 characters"
                letsBeginButton.isEnabled = false
                letsBeginButton.alpha = 0.6f
            }
            name.length > 50 -> {
                nameInputLayout.error = "Name must be less than 50 characters"
                letsBeginButton.isEnabled = false
                letsBeginButton.alpha = 0.6f
            }
            !name.matches(Regex("^[a-zA-Z\\s]+$")) -> {
                nameInputLayout.error = "Name can only contain letters and spaces"
                letsBeginButton.isEnabled = false
                letsBeginButton.alpha = 0.6f
            }
            else -> {
                nameInputLayout.error = null
                nameInputLayout.helperText = null
                letsBeginButton.isEnabled = true
                letsBeginButton.alpha = 1.0f
            }
        }
    }
    
    /**
     * Set up the "Let's Begin" button with click handling
     */
    private fun setupLetsBeginButton() {
        letsBeginButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            
            if (isValidName(name)) {
                // Save user name to SharedPreferences
                saveUserName(name)
                
                // Start WelcomeActivity
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
        }
    }
    
    /**
     * Validate if the name meets all requirements
     */
    private fun isValidName(name: String): Boolean {
        return name.length >= 2 && 
               name.length <= 50 && 
               name.matches(Regex("^[a-zA-Z\\s]+$"))
    }
    
    /**
     * Save the user's name to SharedPreferences for personalization
     */
    private fun saveUserName(name: String) {
        val sharedPreferences = getSharedPreferences("aquaglow_prefs", MODE_PRIVATE)
        sharedPreferences.edit()
            .putString("user_name", name)
            .putBoolean("onboarding_completed", true)
            .apply()
    }
    
    /**
     * Handle skip button click - proceed without name
     */
    fun onSkipClicked(view: View) {
        // Save default name and mark onboarding as completed
        val sharedPreferences = getSharedPreferences("aquaglow_prefs", MODE_PRIVATE)
        sharedPreferences.edit()
            .putString("user_name", "User")
            .putBoolean("onboarding_completed", true)
            .apply()
        
        // Start WelcomeActivity
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }

    private fun setupGlowEffects() {
        // Add breathing effect to input field
        GlowAnimationUtils.createBreathingEffect(nameInputLayout, 4000L)
        
        // Add glow movement to button
        GlowAnimationUtils.createBreathingEffect(letsBeginButton, 4000L)
        
        // Apply material glow effects
        GlowAnimationUtils.applyMaterialGlow(letsBeginButton)
    }
}

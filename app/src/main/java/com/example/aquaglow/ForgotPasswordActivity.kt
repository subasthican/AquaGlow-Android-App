package com.example.aquaglow

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailLayout: TextInputLayout
    private lateinit var emailEdit: TextInputEditText
    private lateinit var sendResetButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        initializeViews()
        setupListeners()
        setupGlowAnimations()

        // Back button handler - always go to Login
        findViewById<View>(R.id.backRow)?.setOnClickListener { 
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun initializeViews() {
        emailLayout = findViewById(R.id.emailInputLayout)
        emailEdit = findViewById(R.id.emailEditText)
        sendResetButton = findViewById(R.id.sendResetButton)
    }

    private fun setupListeners() {
        sendResetButton.setOnClickListener {
            val email = emailEdit.text?.toString()?.trim().orEmpty()

            emailLayout.error = null

            if (!AuthUtils.isValidEmail(email)) {
                emailLayout.error = "Enter a valid email"
                return@setOnClickListener
            }

            // Simulate sending reset email
            Toast.makeText(this, String.format(getString(R.string.auth_reset_link_sent), email), Toast.LENGTH_LONG).show()
            
            // Navigate back to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Back to login functionality
        findViewById<TextView>(R.id.backToLoginText).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupGlowAnimations() {
        // Add breathing effect to send reset button
        GlowAnimationUtils.createBreathingEffect(sendResetButton, 4000L)
        
        // Add breathing effect to back to login text
        GlowAnimationUtils.createBreathingEffect(findViewById<TextView>(R.id.backToLoginText), 5000L)

        // Apply material glow effects
        GlowAnimationUtils.applyMaterialGlow(sendResetButton)
    }
}

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

class LoginActivity : AppCompatActivity() {

    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var emailEdit: TextInputEditText
    private lateinit var passwordEdit: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var goToRegisterButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_new)

        initializeViews()
        setupListeners()
        setupGlowAnimations()

        // Back button handler - go to Onboarding
        findViewById<View>(R.id.backRow)?.setOnClickListener { 
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
        }
    }

    private fun initializeViews() {
        emailLayout = findViewById(R.id.emailInputLayout)
        passwordLayout = findViewById(R.id.passwordInputLayout)
        emailEdit = findViewById(R.id.emailEditText)
        passwordEdit = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        // goToRegisterButton removed from new layout
    }

    private fun setupListeners() {
        loginButton.setOnClickListener {
            if (validateInputs()) {
                val email = emailEdit.text.toString().trim()
                val password = passwordEdit.text.toString().trim()
                
                if (AuthUtils.login(this, email, password)) {
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Add forgot password functionality
        findViewById<TextView>(R.id.forgotPasswordText).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Add create account functionality
        findViewById<TextView>(R.id.goToRegisterText).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        
        val email = emailEdit.text.toString().trim()
        val password = passwordEdit.text.toString().trim()
        
        // Validate email
        if (email.isEmpty()) {
            emailLayout.error = "Email is required"
            isValid = false
        } else if (!AuthUtils.isValidEmail(email)) {
            emailLayout.error = "Please enter a valid email"
            isValid = false
        } else {
            emailLayout.error = null
        }
        
        // Validate password
        if (password.isEmpty()) {
            passwordLayout.error = "Password is required"
            isValid = false
        } else if (!AuthUtils.isValidPassword(password)) {
            passwordLayout.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            passwordLayout.error = null
        }
        
        return isValid
    }

    private fun setupGlowAnimations() {
        // Add breathing effect to login button
        GlowAnimationUtils.createBreathingEffect(loginButton, 4000L)
        
        // Add breathing effect to forgot password text
        GlowAnimationUtils.createBreathingEffect(findViewById<TextView>(R.id.forgotPasswordText), 5000L)

        // Apply material glow effects
        GlowAnimationUtils.applyMaterialGlow(loginButton)
    }
}



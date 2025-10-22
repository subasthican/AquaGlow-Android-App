package com.example.aquaglow

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.widget.TextView
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameLayout: TextInputLayout
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var usernameEdit: TextInputEditText
    private lateinit var emailEdit: TextInputEditText
    private lateinit var passwordEdit: TextInputEditText
    private lateinit var registerButton: MaterialButton
    private lateinit var termsCheckBox: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_new)

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
        usernameLayout = findViewById(R.id.usernameInputLayout)
        emailLayout = findViewById(R.id.emailInputLayout)
        passwordLayout = findViewById(R.id.passwordInputLayout)
        usernameEdit = findViewById(R.id.usernameEditText)
        emailEdit = findViewById(R.id.emailEditText)
        passwordEdit = findViewById(R.id.passwordEditText)
        registerButton = findViewById(R.id.registerButton)
        termsCheckBox = findViewById(R.id.termsCheckBox)
    }

    private fun setupListeners() {
        registerButton.setOnClickListener {
            val username = usernameEdit.text?.toString()?.trim().orEmpty()
            val email = emailEdit.text?.toString()?.trim().orEmpty()
            val password = passwordEdit.text?.toString()?.trim().orEmpty()

            usernameLayout.error = null
            emailLayout.error = null
            passwordLayout.error = null

            var valid = true
            if (!AuthUtils.isValidName(username)) {
                usernameLayout.error = "Enter a valid username"
                valid = false
            }
            if (!AuthUtils.isValidEmail(email)) {
                emailLayout.error = "Enter a valid email"
                valid = false
            }
            if (!AuthUtils.isValidPassword(password)) {
                passwordLayout.error = "Password must be 6+ characters"
                valid = false
            }
            if (!termsCheckBox.isChecked) {
                Toast.makeText(this, getString(R.string.auth_agree_terms), Toast.LENGTH_SHORT).show()
                valid = false
            }
            if (!valid) return@setOnClickListener

            val ok = AuthUtils.register(this, username, email, password)
            if (ok) {
                Toast.makeText(this, getString(R.string.auth_account_created), Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, getString(R.string.auth_account_exists), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupGlowAnimations() {
        // Add breathing effect to register button
        GlowAnimationUtils.createBreathingEffect(registerButton, 4000L)
        
        // Add breathing effect to terms checkbox
        GlowAnimationUtils.createBreathingEffect(termsCheckBox, 5000L)

        // Apply material glow effects
        GlowAnimationUtils.applyMaterialGlow(registerButton)
    }

    override fun onStart() {
        super.onStart()
        // Wire up the "Already have an account? Sign in" link
        findViewById<TextView>(R.id.goToLoginText)?.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}



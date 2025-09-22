package com.example.aquaglow

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/**
 * AboutActivity displays information about the AquaGlow app
 * including version, features, and contact information
 */
class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        initializeViews()
        setupClickListeners()
    }

    /**
     * Initialize all UI views
     */
    private fun initializeViews() {
        // Set app version
        val versionTextView: TextView = findViewById(R.id.versionTextView)
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            versionTextView.text = "Version ${packageInfo.versionName}"
        } catch (e: Exception) {
            versionTextView.text = "Version 1.0.0"
        }
    }

    /**
     * Set up click listeners for interactive elements
     */
    private fun setupClickListeners() {
        // Back button
        findViewById<MaterialButton>(R.id.backButton).setOnClickListener {
            onBackPressed()
        }

        // Rate app button
        findViewById<MaterialButton>(R.id.rateAppButton).setOnClickListener {
            openPlayStore()
        }

        // Share app button
        findViewById<MaterialButton>(R.id.shareAppButton).setOnClickListener {
            shareApp()
        }

        // Privacy Policy button
        findViewById<MaterialButton>(R.id.privacyPolicyButton).setOnClickListener {
            openPrivacyPolicy()
        }

        // Terms of Service button
        findViewById<MaterialButton>(R.id.termsOfServiceButton).setOnClickListener {
            openTermsOfService()
        }

        // Contact Support button
        findViewById<MaterialButton>(R.id.contactSupportButton).setOnClickListener {
            contactSupport()
        }
    }

    /**
     * Open Play Store for rating the app
     */
    private fun openPlayStore() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            startActivity(intent)
        }
    }

    /**
     * Share the app with others
     */
    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out AquaGlow!")
        shareIntent.putExtra(Intent.EXTRA_TEXT, "I've been using AquaGlow to track my daily habits and mood. It's a great wellness app! Download it from: https://play.google.com/store/apps/details?id=$packageName")
        startActivity(Intent.createChooser(shareIntent, "Share AquaGlow"))
    }

    /**
     * Open Privacy Policy page
     */
    private fun openPrivacyPolicy() {
        val intent = Intent(this, PrivacyPolicyActivity::class.java)
        startActivity(intent)
    }

    /**
     * Open Terms of Service page
     */
    private fun openTermsOfService() {
        val intent = Intent(this, TermsOfServiceActivity::class.java)
        startActivity(intent)
    }

    /**
     * Contact support via email
     */
    private fun contactSupport() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:support@aquaglow.app")
        intent.putExtra(Intent.EXTRA_SUBJECT, "AquaGlow Support Request")
        intent.putExtra(Intent.EXTRA_TEXT, "Hi AquaGlow Team,\n\nI need help with:\n\n")
        startActivity(Intent.createChooser(intent, "Send Email"))
    }

    /**
     * Handle back button press
     */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}


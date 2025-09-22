package com.example.aquaglow

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/**
 * PrivacyPolicyActivity displays the privacy policy for AquaGlow app
 */
class PrivacyPolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

        initializeViews()
        setupContent()
    }

    /**
     * Initialize all UI views
     */
    private fun initializeViews() {
        // Back button
        findViewById<MaterialButton>(R.id.backButton).setOnClickListener {
            onBackPressed()
        }
    }

    /**
     * Set up privacy policy content
     */
    private fun setupContent() {
        val contentTextView: TextView = findViewById(R.id.contentTextView)
        
        val privacyPolicy = """
            PRIVACY POLICY
            
            Last updated: ${getCurrentDate()}
            
            INTRODUCTION
            
            AquaGlow ("we," "our," or "us") is committed to protecting your privacy. This Privacy Policy explains how we collect, use, and safeguard your information when you use our mobile application.
            
            INFORMATION WE COLLECT
            
            Personal Information:
            • Name (optional) - for personalization
            • App usage data - for improving user experience
            
            Data Stored Locally:
            • Daily habits and completion status
            • Mood entries and notes
            • App settings and preferences
            • Hydration reminder intervals
            
            HOW WE USE YOUR INFORMATION
            
            We use the information we collect to:
            • Provide and maintain the app's functionality
            • Personalize your experience
            • Send hydration reminders (if enabled)
            • Improve our app's features and performance
            
            DATA STORAGE AND SECURITY
            
            • All data is stored locally on your device using SharedPreferences
            • We do not transmit your personal data to external servers
            • Your data remains under your control
            • You can clear all data anytime from app settings
            
            THIRD-PARTY SERVICES
            
            Our app may use the following third-party services:
            • WorkManager (Android) - for background notifications
            • MPAndroidChart - for mood trend visualization
            
            These services do not collect or access your personal data.
            
            YOUR RIGHTS
            
            You have the right to:
            • Access your data stored in the app
            • Delete all your data at any time
            • Disable notifications
            • Use the app without providing personal information
            
            DATA RETENTION
            
            Your data is retained on your device until you:
            • Delete the app
            • Clear app data from device settings
            • Use the "Clear All Data" option in app settings
            
            CHILDREN'S PRIVACY
            
            Our app is suitable for users of all ages. We do not knowingly collect personal information from children under 13.
            
            CHANGES TO THIS PRIVACY POLICY
            
            We may update this Privacy Policy from time to time. We will notify you of any changes by posting the new Privacy Policy in the app.
            
            CONTACT US
            
            If you have any questions about this Privacy Policy, please contact us at:
            Email: privacy@aquaglow.app
            
            This Privacy Policy is effective as of the date listed above and will remain in effect except with respect to any changes in its provisions in the future.
        """.trimIndent()
        
        contentTextView.text = privacyPolicy
    }

    /**
     * Get current date in readable format
     */
    private fun getCurrentDate(): String {
        val sdf = java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
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


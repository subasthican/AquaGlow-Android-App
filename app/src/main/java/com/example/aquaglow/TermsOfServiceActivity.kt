package com.example.aquaglow

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

/**
 * TermsOfServiceActivity displays the terms of service for AquaGlow app
 */
class TermsOfServiceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_of_service)

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
     * Set up terms of service content
     */
    private fun setupContent() {
        val contentTextView: TextView = findViewById(R.id.contentTextView)
        
        val termsOfService = """
            TERMS OF SERVICE
            
            Last updated: ${getCurrentDate()}
            
            AGREEMENT TO TERMS
            
            By downloading, installing, or using the AquaGlow mobile application ("App"), you agree to be bound by these Terms of Service ("Terms"). If you do not agree to these Terms, please do not use the App.
            
            DESCRIPTION OF SERVICE
            
            AquaGlow is a wellness application that helps users:
            • Track daily habits and wellness activities
            • Log mood entries with emoji and notes
            • Set hydration reminders
            • View mood trends and statistics
            
            USER RESPONSIBILITIES
            
            You agree to:
            • Use the App only for personal, non-commercial purposes
            • Provide accurate information when using the App
            • Not attempt to reverse engineer or modify the App
            • Not use the App for any illegal or unauthorized purpose
            
            DATA AND PRIVACY
            
            • All data is stored locally on your device
            • We do not collect or transmit personal data to external servers
            • You retain full control over your data
            • You can delete your data at any time through the App settings
            
            APP AVAILABILITY
            
            We strive to maintain App availability but cannot guarantee:
            • Uninterrupted access to the App
            • Error-free operation
            • Compatibility with all devices or operating system versions
            
            INTELLECTUAL PROPERTY
            
            The App and its original content, features, and functionality are owned by AquaGlow and are protected by international copyright, trademark, and other intellectual property laws.
            
            DISCLAIMER OF WARRANTIES
            
            THE APP IS PROVIDED "AS IS" AND "AS AVAILABLE" WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT.
            
            LIMITATION OF LIABILITY
            
            IN NO EVENT SHALL AQUAGLOW BE LIABLE FOR ANY INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL, OR PUNITIVE DAMAGES, INCLUDING WITHOUT LIMITATION, LOSS OF PROFITS, DATA, USE, GOODWILL, OR OTHER INTANGIBLE LOSSES.
            
            TERMINATION
            
            We may terminate or suspend your access to the App immediately, without prior notice or liability, for any reason whatsoever, including without limitation if you breach the Terms.
            
            CHANGES TO TERMS
            
            We reserve the right to modify or replace these Terms at any time. If a revision is material, we will try to provide at least 30 days notice prior to any new terms taking effect.
            
            CONTACT INFORMATION
            
            If you have any questions about these Terms of Service, please contact us at:
            Email: legal@aquaglow.app
            
            GOVERNING LAW
            
            These Terms shall be interpreted and governed by the laws of the jurisdiction in which AquaGlow operates, without regard to its conflict of law provisions.
            
            SEVERABILITY
            
            If any provision of these Terms is held to be invalid or unenforceable by a court, the remaining provisions of these Terms will remain in effect.
            
            ENTIRE AGREEMENT
            
            These Terms constitute the sole and entire agreement between you and AquaGlow regarding the App and supersede all prior and contemporaneous understandings, agreements, representations, and warranties.
        """.trimIndent()
        
        contentTextView.text = termsOfService
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


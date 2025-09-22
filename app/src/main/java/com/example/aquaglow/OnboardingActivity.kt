package com.example.aquaglow

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * OnboardingActivity provides a 3-page introduction to AquaGlow
 * using ViewPager2 with dots indicator and navigation buttons
 */
class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var skipButton: MaterialButton
    private lateinit var nextButton: MaterialButton
    private lateinit var getStartedButton: MaterialButton
    private lateinit var sharedPreferences: SharedPreferences
    
    private lateinit var onboardingAdapter: OnboardingAdapter
    private var currentPage = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(SplashActivity.PREFS_NAME, MODE_PRIVATE)
        
        // Initialize views
        initializeViews()
        
        // Setup ViewPager
        setupViewPager()
        
        // Setup button listeners
        setupButtonListeners()
    }
    
    /**
     * Initializes all UI views
     */
    private fun initializeViews() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        skipButton = findViewById(R.id.skipButton)
        nextButton = findViewById(R.id.nextButton)
        getStartedButton = findViewById(R.id.getStartedButton)
    }
    
    /**
     * Sets up the ViewPager2 with onboarding pages
     */
    private fun setupViewPager() {
        onboardingAdapter = OnboardingAdapter(this)
        viewPager.adapter = onboardingAdapter
        
        // Setup dots indicator
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()
        
        // Listen for page changes
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPage = position
                updateButtonVisibility()
            }
        })
        
        // Initialize button visibility
        updateButtonVisibility()
    }
    
    /**
     * Sets up button click listeners
     */
    private fun setupButtonListeners() {
        skipButton.setOnClickListener {
            navigateToMainActivity()
        }
        
        nextButton.setOnClickListener {
            if (currentPage < onboardingAdapter.itemCount - 1) {
                viewPager.currentItem = currentPage + 1
            }
        }
        
        getStartedButton.setOnClickListener {
            navigateToMainActivity()
        }
    }
    
    /**
     * Updates button visibility based on current page
     */
    private fun updateButtonVisibility() {
        val isLastPage = currentPage == onboardingAdapter.itemCount - 1
        
        if (isLastPage) {
            nextButton.visibility = View.GONE
            getStartedButton.visibility = View.VISIBLE
        } else {
            nextButton.visibility = View.VISIBLE
            getStartedButton.visibility = View.GONE
        }
    }
    
    /**
     * Navigates to NameInputActivity for user personalization
     */
    private fun navigateToMainActivity() {
        // Navigate to NameInputActivity
        val intent = Intent(this, NameInputActivity::class.java)
        startActivity(intent)
        finish()
    }
}

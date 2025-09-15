package com.example.aquaglow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.aquaglow.databinding.ActivityOnboardingBinding
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragments = listOf(
            OnboardingFragment.newInstance(
                R.drawable.ic_onboarding_habits,
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_desc_1),
                false
            ),
            OnboardingFragment.newInstance(
                R.drawable.ic_onboarding_mood,
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_desc_2),
                false
            ),
            OnboardingFragment.newInstance(
                R.drawable.ic_onboarding_hydration,
                getString(R.string.onboarding_title_3),
                getString(R.string.onboarding_desc_3),
                true
            )
        )

        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

        binding.viewPager.adapter = adapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        TabLayoutMediator(binding.tabDots, binding.viewPager) { _, _ -> }.attach()

        binding.skip.setOnClickListener {
            completeOnboardingAndGo()
        }

        // Handle next/get started button inside fragments
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {})
    }

    private fun completeOnboardingAndGo() {
        getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ONBOARDING_COMPLETE, true)
            .apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        const val PREFS = "aquaglow_prefs"
        const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    }
}



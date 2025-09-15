package com.example.aquaglow

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.aquaglow.databinding.ActivityAuthBinding
import com.google.android.material.tabs.TabLayoutMediator

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragments: List<Pair<String, Fragment>> = listOf(
            getString(R.string.login) to LoginFragment(),
            getString(R.string.sign_up) to SignupFragment()
        )

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            override fun createFragment(position: Int): Fragment = fragments[position].second
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = fragments[position].first
        }.attach()
    }

    companion object {
        const val PREFS = "aquaglow_prefs"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_USER_PASSWORD = "user_password" // stored hashed
        const val KEY_IS_LOGGED_IN = "isLoggedIn"
    }
}



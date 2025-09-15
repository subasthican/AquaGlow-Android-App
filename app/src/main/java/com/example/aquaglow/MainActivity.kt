package com.example.aquaglow

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_container, HomeFragment())
                .commit()
        }

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_container, HomeFragment())
                        .commit()
                    true
                }
                R.id.nav_habits -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_container, HabitTrackerFragment())
                        .commit()
                    true
                }
                R.id.nav_mood -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_container, MoodJournalFragment())
                        .commit()
                    true
                }
                R.id.nav_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_container, ProfileFragment())
                        .commit()
                    true
                }
                R.id.nav_search -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_container, SearchFilterFragment())
                        .commit()
                    true
                }
                R.id.nav_notifications -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_container, NotificationsFragment())
                        .commit()
                    true
                }
                R.id.nav_settings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_container, SettingsFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
}
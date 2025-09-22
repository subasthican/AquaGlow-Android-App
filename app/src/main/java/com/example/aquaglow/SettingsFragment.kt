package com.example.aquaglow

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 * SettingsFragment provides app configuration and user preferences
 */
class SettingsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var themeSwitch: SwitchMaterial
    private lateinit var hydrationIntervalSpinner: Spinner
    private lateinit var clearDataButton: MaterialButton
    private lateinit var appVersionText: TextView
    private lateinit var developerText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupSharedPreferences()
        setupThemeSwitch()
        setupHydrationInterval()
        setupClearDataButton()
        setupAppInfoButtons()
    }

    private fun initializeViews(view: View) {
        themeSwitch = view.findViewById(R.id.themeSwitch)
        hydrationIntervalSpinner = view.findViewById(R.id.hydrationIntervalSpinner)
        clearDataButton = view.findViewById(R.id.clearDataButton)
        appVersionText = view.findViewById(R.id.appVersionText)
        developerText = view.findViewById(R.id.developerText)
    }

    private fun setupSharedPreferences() {
        sharedPreferences = requireContext().getSharedPreferences("aquaglow_prefs", 0)
    }

    private fun setupThemeSwitch() {
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        themeSwitch.isChecked = isDarkMode

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply()
            
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun setupHydrationInterval() {
        val intervals = arrayOf("30 minutes", "1 hour", "2 hours", "3 hours", "4 hours", "6 hours")
        val intervalValues = arrayOf(30, 60, 120, 180, 240, 360)
        
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, intervals)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        hydrationIntervalSpinner.adapter = adapter

        val currentInterval = sharedPreferences.getInt("hydration_interval_minutes", 120)
        val selectedIndex = intervalValues.indexOf(currentInterval)
        if (selectedIndex >= 0) {
            hydrationIntervalSpinner.setSelection(selectedIndex)
        }

        hydrationIntervalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedInterval = intervalValues[position]
                sharedPreferences.edit().putInt("hydration_interval_minutes", selectedInterval).apply()
                
                // Schedule hydration reminders with new interval
                WorkManagerUtils.scheduleHydrationReminder(requireContext(), selectedInterval)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupClearDataButton() {
        clearDataButton.setOnClickListener {
            showClearDataDialog()
        }
    }

    private fun setupAppInfoButtons() {
        // About button
        view?.findViewById<View>(R.id.aboutButton)?.setOnClickListener {
            val intent = Intent(requireContext(), AboutActivity::class.java)
            startActivity(intent)
        }

        // Help button
        view?.findViewById<View>(R.id.helpButton)?.setOnClickListener {
            val intent = Intent(requireContext(), HelpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showClearDataDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear All Data")
            .setMessage("Are you sure you want to clear all your habits, moods, and settings? This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                clearAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearAllData() {
        sharedPreferences.edit().clear().apply()
        
        // Reset to default values
        sharedPreferences.edit()
            .putString("user_name", "User")
            .putBoolean("onboarding_completed", true)
            .putBoolean("dark_mode", false)
            .putInt("hydration_interval_minutes", 120)
            .apply()

        // Show success message
        Toast.makeText(requireContext(), "All data cleared successfully", Toast.LENGTH_SHORT).show()
    }

    private fun setupAppInfo() {
        appVersionText.text = "Version 1.0.0"
        developerText.text = "Developed by AquaGlow Team"
    }
}
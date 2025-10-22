package com.example.aquaglow

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

/**
 * ProfileFragment - User profile and settings hub
 * Combines: Settings, Games, Quizzes
 */
class ProfileFragment : Fragment() {

    private lateinit var userNameText: TextView
    private lateinit var userEmailText: TextView
    private lateinit var editProfileButton: MaterialButton
    
    // Feature cards
    private lateinit var settingsCard: MaterialCardView
    private lateinit var communityCard: MaterialCardView
    private lateinit var gamesCard: MaterialCardView
    private lateinit var quizzesCard: MaterialCardView
    private lateinit var aboutCard: MaterialCardView
    private lateinit var helpCard: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        loadUserInfo()
        setupEditProfileButton()
        setupCardClicks()
    }

    private fun initializeViews(view: View) {
        userNameText = view.findViewById(R.id.userNameText)
        userEmailText = view.findViewById(R.id.userEmailText)
        editProfileButton = view.findViewById(R.id.editProfileButton)
        
        settingsCard = view.findViewById(R.id.settingsCard)
        communityCard = view.findViewById(R.id.communityCard)
        gamesCard = view.findViewById(R.id.gamesCard)
        quizzesCard = view.findViewById(R.id.quizzesCard)
        aboutCard = view.findViewById(R.id.aboutCard)
        helpCard = view.findViewById(R.id.helpCard)
    }

    private fun loadUserInfo() {
        val sharedPreferences = requireContext().getSharedPreferences("aquaglow_prefs", 0)
        val userName = sharedPreferences.getString("auth_user_name", "User") ?: "User"
        val userEmail = sharedPreferences.getString("auth_user_email", "") ?: ""
        
        userNameText.text = userName
        userEmailText.text = userEmail
    }

    private fun setupEditProfileButton() {
        editProfileButton.setOnClickListener {
            showEditProfileDialog()
        }
    }

    private fun showEditProfileDialog() {
        val options = arrayOf(
            getString(R.string.change_username_option),
            getString(R.string.change_password_option)
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.edit_profile_title))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditUsernameDialog()
                    1 -> showChangePasswordDialog()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showEditUsernameDialog() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
            hint = getString(R.string.edit_username_hint)
            setText(AuthUtils.getUserName(requireContext()) ?: "")
            setPadding(50, 30, 50, 30)
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.edit_username_title))
            .setMessage(getString(R.string.edit_username_message))
            .setView(input)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val newUsername = input.text.toString().trim()
                if (AuthUtils.updateUserName(requireContext(), newUsername)) {
                    userNameText.text = newUsername
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.username_updated),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.invalid_username),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showChangePasswordDialog() {
        // Create custom layout for password change
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }
        
        val currentPasswordInput = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = getString(R.string.current_password_hint)
        }
        
        val newPasswordInput = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = getString(R.string.new_password_hint)
        }
        
        val confirmPasswordInput = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            hint = getString(R.string.confirm_new_password_hint)
        }
        
        layout.addView(currentPasswordInput)
        layout.addView(newPasswordInput)
        layout.addView(confirmPasswordInput)
        
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.change_password_title))
            .setMessage(getString(R.string.change_password_message))
            .setView(layout)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val currentPassword = currentPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()
                
                when {
                    newPassword != confirmPassword -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.passwords_dont_match),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    !AuthUtils.isValidPassword(newPassword) -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.invalid_password),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    AuthUtils.changePassword(requireContext(), currentPassword, newPassword) -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.password_updated),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.incorrect_current_password),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun setupCardClicks() {
        settingsCard.setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }
        
        communityCard.setOnClickListener {
            findNavController().navigate(R.id.communityFragment)
        }
        
        gamesCard.setOnClickListener {
            findNavController().navigate(R.id.gamesFragment)
        }
        
        quizzesCard.setOnClickListener {
            findNavController().navigate(R.id.quizzesFragment)
        }
        
        aboutCard.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }
        
        helpCard.setOnClickListener {
            startActivity(Intent(requireContext(), HelpActivity::class.java))
        }
    }
}


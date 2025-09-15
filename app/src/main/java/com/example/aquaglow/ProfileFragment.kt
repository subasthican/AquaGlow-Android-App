package com.example.aquaglow

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.aquaglow.AuthActivity.Companion.KEY_IS_LOGGED_IN
import com.example.aquaglow.AuthActivity.Companion.KEY_USER_EMAIL
import com.example.aquaglow.AuthActivity.Companion.PREFS
import com.example.aquaglow.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private var editMode: Boolean = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                binding.imageAvatar.setImageURI(uri)
                savePref("avatarUri", uri.toString())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = getString(R.string.profile)
        binding.toolbar.inflateMenu(R.menu/menu_profile)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    toggleEditMode()
                    true
                }
                else -> false
            }
        }

        binding.cardAvatar.setOnClickListener {
            if (editMode) pickImage()
        }

        binding.buttonSave.setOnClickListener { saveChanges() }
        binding.buttonCancel.setOnClickListener { bindData() }
        binding.buttonLogout.setOnClickListener { logout() }

        bindData()
        setEditEnabled(false)
    }

    override fun onResume() {
        super.onResume()
        bindData()
    }

    private fun bindData() {
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val name = prefs.getString("userName", null) ?: prefs.getString(KEY_USER_EMAIL, "Guest")?.substringBefore('@')
        val email = prefs.getString(KEY_USER_EMAIL, getString(R.string.not_available))
        val goal = prefs.getInt("dailyWaterGoal", 8)
        val dark = prefs.getBoolean("darkModeEnabled", false)
        val reminders = prefs.getBoolean("hydrationReminders", true)
        val sound = prefs.getBoolean("reminderSound", true)
        val avatarUri = prefs.getString("avatarUri", null)

        binding.inputName.editText?.setText(name)
        binding.textEmail.text = email
        binding.sliderWater.value = goal.toFloat()
        binding.switchDarkMode.isChecked = dark
        binding.switchReminders.isChecked = reminders
        binding.switchSound.isChecked = sound
        if (avatarUri != null) binding.imageAvatar.setImageURI(Uri.parse(avatarUri))
    }

    private fun saveChanges() {
        val name = binding.inputName.editText?.text?.toString()?.trim().orEmpty()
        val goal = binding.sliderWater.value.toInt()
        val dark = binding.switchDarkMode.isChecked
        val reminders = binding.switchReminders.isChecked
        val sound = binding.switchSound.isChecked

        savePref("userName", name)
        savePref("dailyWaterGoal", goal)
        savePref("darkModeEnabled", dark)
        savePref("hydrationReminders", reminders)
        savePref("reminderSound", sound)

        setEditEnabled(false)
        editMode = false
        binding.toolbar.menu.findItem(R.id.action_edit)?.setIcon(R.drawable.ic_edit)
        bindData()
    }

    private fun savePref(key: String, value: Any) {
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        when (value) {
            is String -> editor.putString(key, value)
            is Int -> editor.putInt(key, value)
            is Boolean -> editor.putBoolean(key, value)
        }
        editor.apply()
    }

    private fun toggleEditMode() {
        editMode = !editMode
        setEditEnabled(editMode)
        binding.toolbar.menu.findItem(R.id.action_edit)?.setIcon(if (editMode) R.drawable.ic_check else R.drawable.ic_edit)
    }

    private fun setEditEnabled(enabled: Boolean) {
        binding.inputName.isEnabled = enabled
        binding.sliderWater.isEnabled = enabled
        binding.switchDarkMode.isEnabled = enabled
        binding.switchReminders.isEnabled = enabled
        binding.switchSound.isEnabled = enabled
        binding.buttonSave.visibility = if (enabled) View.VISIBLE else View.GONE
        binding.buttonCancel.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun logout() {
        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply()
        startActivity(Intent(requireContext(), AuthActivity::class.java))
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



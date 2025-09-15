package com.example.aquaglow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.aquaglow.AuthActivity.Companion.KEY_IS_LOGGED_IN
import com.example.aquaglow.AuthActivity.Companion.KEY_USER_EMAIL
import com.example.aquaglow.AuthActivity.Companion.KEY_USER_PASSWORD
import com.example.aquaglow.AuthActivity.Companion.PREFS
import com.example.aquaglow.databinding.FragmentSignupBinding
import java.security.MessageDigest

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonPrimary.setOnClickListener {
            attemptSignup()
        }
    }

    private fun attemptSignup() {
        val email = binding.inputEmail.editText?.text?.toString()?.trim().orEmpty()
        val password = binding.inputPassword.editText?.text?.toString().orEmpty()
        val confirm = binding.inputConfirmPassword.editText?.text?.toString().orEmpty()

        var valid = true
        if (email.isEmpty()) {
            binding.inputEmail.error = getString(R.string.error_email_required)
            valid = false
        } else {
            binding.inputEmail.error = null
        }
        if (password.length < 6) {
            binding.inputPassword.error = getString(R.string.error_password_length)
            valid = false
        } else {
            binding.inputPassword.error = null
        }
        if (confirm != password) {
            binding.inputConfirmPassword.error = getString(R.string.error_password_mismatch)
            valid = false
        } else {
            binding.inputConfirmPassword.error = null
        }
        if (!valid) return

        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(KEY_USER_EMAIL, email)
        editor.putString(KEY_USER_PASSWORD, hash(password))
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()

        startActivity(Intent(requireContext(), MainActivity::class.java))
        requireActivity().finish()
    }

    private fun hash(text: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(text.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



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
import com.example.aquaglow.databinding.FragmentLoginBinding
import java.security.MessageDigest

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonPrimary.setOnClickListener {
            attemptLogin()
        }

        binding.textForgotPassword.setOnClickListener {
            // Optional: no-op or simple toast in local-only mode
        }
    }

    private fun attemptLogin() {
        val email = binding.inputEmail.editText?.text?.toString()?.trim().orEmpty()
        val password = binding.inputPassword.editText?.text?.toString().orEmpty()

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
        if (!valid) return

        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val storedEmail = prefs.getString(KEY_USER_EMAIL, null)
        val storedHash = prefs.getString(KEY_USER_PASSWORD, null)

        val inputHash = hash(password)
        if (storedEmail != null && storedHash != null && storedEmail.equals(email, true) && storedHash == inputHash) {
            prefs.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply()
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        } else {
            binding.textError.visibility = View.VISIBLE
            binding.textError.text = getString(R.string.error_invalid_credentials)
        }
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



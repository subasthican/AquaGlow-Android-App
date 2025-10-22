package com.example.aquaglow

import android.content.Context
import android.content.SharedPreferences
import android.util.Patterns
import java.security.MessageDigest

/**
 * AuthUtils provides minimal SharedPreferences-based authentication helpers.
 * This is not secure for production but sufficient for offline demo use.
 */
object AuthUtils {

    private const val PREFS_NAME = "aquaglow_prefs"
    private const val KEY_USER_EMAIL = "auth_user_email"
    private const val KEY_USER_NAME = "auth_user_name"
    private const val KEY_USER_PASSWORD_HASH = "auth_user_password_hash"
    private const val KEY_LOGGED_IN = "auth_logged_in"

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidName(name: String): Boolean {
        return name.length in 2..50 && name.matches(Regex("^[a-zA-Z\\s]+$"))
    }

    fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray())
        return bytes.joinToString("") { byte -> "%02x".format(byte) }
    }

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun register(context: Context, name: String, email: String, password: String): Boolean {
        val sp = prefs(context)
        
        // Check if user already exists with this email
        val existingEmail = sp.getString(KEY_USER_EMAIL, null)
        if (existingEmail != null && existingEmail.equals(email.lowercase(), ignoreCase = true)) {
            return false // already registered with this email
        }
        
        // Register new user
        sp.edit()
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email.lowercase())
            .putString(KEY_USER_PASSWORD_HASH, hashPassword(password))
            .putBoolean(KEY_LOGGED_IN, true)
            .putString("user_name", name) // keep existing personalization
            .putBoolean("onboarding_completed", true)
            .apply()
        return true
    }

    fun login(context: Context, email: String, password: String): Boolean {
        val sp = prefs(context)
        val storedEmail = sp.getString(KEY_USER_EMAIL, null)
        val storedHash = sp.getString(KEY_USER_PASSWORD_HASH, null)
        
        // Check if user exists
        if (storedEmail == null || storedHash == null) {
            return false // No user registered
        }
        
        // Verify credentials
        if (storedEmail.equals(email.lowercase(), ignoreCase = true) && storedHash == hashPassword(password)) {
            sp.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putBoolean("onboarding_completed", true)
                .apply()
            return true
        }
        return false
    }

    fun logout(context: Context) {
        val sp = prefs(context)
        sp.edit().putBoolean(KEY_LOGGED_IN, false).apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        val sp = prefs(context)
        return sp.getBoolean(KEY_LOGGED_IN, false)
    }
    
    fun getUserEmail(context: Context): String? {
        return prefs(context).getString(KEY_USER_EMAIL, null)
    }
    
    fun getUserName(context: Context): String? {
        return prefs(context).getString(KEY_USER_NAME, null)
    }
    
    fun isUserRegistered(context: Context): Boolean {
        val sp = prefs(context)
        return sp.contains(KEY_USER_EMAIL) && sp.contains(KEY_USER_PASSWORD_HASH)
    }
    
    /**
     * Update user's display name
     * @return true if successful, false if name is invalid
     */
    fun updateUserName(context: Context, newName: String): Boolean {
        if (!isValidName(newName)) {
            return false
        }
        
        val sp = prefs(context)
        sp.edit()
            .putString(KEY_USER_NAME, newName)
            .putString("user_name", newName) // keep existing personalization
            .apply()
        return true
    }
    
    /**
     * Change user's password
     * @param currentPassword The current password for verification
     * @param newPassword The new password to set
     * @return true if successful, false if current password is incorrect or new password is invalid
     */
    fun changePassword(context: Context, currentPassword: String, newPassword: String): Boolean {
        if (!isValidPassword(newPassword)) {
            return false
        }
        
        val sp = prefs(context)
        val storedHash = sp.getString(KEY_USER_PASSWORD_HASH, null) ?: return false
        
        // Verify current password
        if (storedHash != hashPassword(currentPassword)) {
            return false // Current password is incorrect
        }
        
        // Update to new password
        sp.edit()
            .putString(KEY_USER_PASSWORD_HASH, hashPassword(newPassword))
            .apply()
        return true
    }
}



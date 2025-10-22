package com.example.aquaglow

import android.content.Context
import android.content.SharedPreferences

/**
 * ThemeManager handles app theme customization
 */
object ThemeManager {
    
    private const val PREFS_NAME = "aquaglow_prefs"
    private const val THEME_KEY = "selected_theme"
    
    enum class Theme(val displayName: String, val primaryColor: String, val secondaryColor: String) {
        AQUA("Aqua Glow", "#00D4AA", "#1A1A2E"),
        OCEAN("Ocean Blue", "#2196F3", "#0D47A1"),
        SUNSET("Sunset Orange", "#FF9800", "#E65100"),
        FOREST("Forest Green", "#4CAF50", "#1B5E20"),
        LAVENDER("Lavender", "#9C27B0", "#4A148C"),
        CORAL("Coral Pink", "#FF5722", "#BF360C"),
        MIDNIGHT("Midnight", "#607D8B", "#263238"),
        ROSE("Rose Gold", "#E91E63", "#880E4F")
    }
    
    fun getCurrentTheme(context: Context): Theme {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val themeName = sharedPreferences.getString(THEME_KEY, Theme.AQUA.name) ?: Theme.AQUA.name
        return Theme.valueOf(themeName)
    }
    
    fun setTheme(context: Context, theme: Theme) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putString(THEME_KEY, theme.name)
            .apply()
    }
    
    fun getAllThemes(): List<Theme> {
        return Theme.values().toList()
    }
    
    fun getThemeColor(theme: Theme, isPrimary: Boolean): String {
        return if (isPrimary) theme.primaryColor else theme.secondaryColor
    }
}








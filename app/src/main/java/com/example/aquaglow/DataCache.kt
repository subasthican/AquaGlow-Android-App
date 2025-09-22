package com.example.aquaglow

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.ConcurrentHashMap

/**
 * DataCache provides optimized data management with caching
 * Reduces SharedPreferences reads and improves app performance
 */
object DataCache {
    
    private val cache = ConcurrentHashMap<String, Any>()
    private var sharedPreferences: SharedPreferences? = null
    private val gson = Gson()
    
    /**
     * Initializes the cache with SharedPreferences
     */
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(SplashActivity.PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Gets data from cache or SharedPreferences
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String, defaultValue: T): T {
        return cache[key] as? T ?: run {
            val value = getFromSharedPreferences(key, defaultValue)
            cache[key] = value as Any
            value
        }
    }
    
    /**
     * Sets data in both cache and SharedPreferences
     */
    fun <T> set(key: String, value: T) {
        cache[key] = value as Any
        saveToSharedPreferences(key, value)
    }
    
    /**
     * Clears cache
     */
    fun clearCache() {
        cache.clear()
    }
    
    /**
     * Gets data from SharedPreferences
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> getFromSharedPreferences(key: String, defaultValue: T): T {
        return when (defaultValue) {
            is String -> sharedPreferences?.getString(key, defaultValue) as T
            is Boolean -> sharedPreferences?.getBoolean(key, defaultValue) as T
            is Int -> sharedPreferences?.getInt(key, defaultValue) as T
            is Float -> sharedPreferences?.getFloat(key, defaultValue) as T
            is Long -> sharedPreferences?.getLong(key, defaultValue) as T
            is Set<*> -> {
                val defaultStringSet = defaultValue.filterIsInstance<String>().toSet()
                sharedPreferences?.getStringSet(key, defaultStringSet) as T
            }
            else -> {
                val json = sharedPreferences?.getString(key, null)
                if (json != null) {
                    try {
                        val type = object : TypeToken<T>() {}.type
                        gson.fromJson(json, type)
                    } catch (e: Exception) {
                        defaultValue
                    }
                } else {
                    defaultValue
                }
            }
        }
    }
    
    /**
     * Saves data to SharedPreferences
     */
    private fun <T> saveToSharedPreferences(key: String, value: T) {
        val editor = sharedPreferences?.edit()
        when (value) {
            is String -> editor?.putString(key, value)
            is Boolean -> editor?.putBoolean(key, value)
            is Int -> editor?.putInt(key, value)
            is Float -> editor?.putFloat(key, value)
            is Long -> editor?.putLong(key, value)
            is Set<*> -> {
                val stringSet = value.filterIsInstance<String>().toSet()
                editor?.putStringSet(key, stringSet)
            }
            else -> {
                val json = gson.toJson(value)
                editor?.putString(key, json)
            }
        }
        editor?.apply()
    }
}

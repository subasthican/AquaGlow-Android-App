package com.example.aquaglow

import android.app.Application

/**
 * AquaGlowApplication initializes the app and sets up WorkManager
 * for background tasks like hydration reminders and habit resets
 */
class AquaGlowApplication : Application() {
    
    companion object {
        @Volatile
        private var INSTANCE: AquaGlowApplication? = null
        
        fun getInstance(): AquaGlowApplication {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AquaGlowApplication().also { INSTANCE = it }
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        
        try {
            // Initialize performance monitoring
            PerformanceMonitor.startMonitoring()
            
            // Initialize DataCache
            DataCache.initialize(this)
            
            // Schedule background tasks (with error handling)
            try {
                WorkManagerUtils.scheduleHabitReset(this)
            } catch (e: Exception) {
                e.printStackTrace()
                // WorkManager errors shouldn't crash the app
            }
            
            try {
                WorkManagerUtils.scheduleStepCountReset(this)
            } catch (e: Exception) {
                e.printStackTrace()
                // WorkManager errors shouldn't crash the app
            }
            
            // Log memory usage
            PerformanceMonitor.logMemoryUsage("App Start")
        } catch (e: Exception) {
            e.printStackTrace()
            // App initialization errors shouldn't crash the app
        }
    }
    
    
    override fun onLowMemory() {
        super.onLowMemory()
        try {
            // Clear caches when memory is low
            DataCache.clearCache()
            ImageUtils.clearCache()
            PerformanceMonitor.logMemoryUsage("Low Memory")
        } catch (e: Exception) {
            e.printStackTrace()
            // Memory cleanup errors shouldn't crash the app
        }
    }
}

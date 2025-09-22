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
        
        // Initialize performance monitoring
        PerformanceMonitor.startMonitoring()
        
        // Initialize DataCache
        DataCache.initialize(this)
        
        // Schedule habit reset task (WorkManager is auto-initialized)
        WorkManagerUtils.scheduleHabitReset(this)
        
        // Log memory usage
        PerformanceMonitor.logMemoryUsage("App Start")
    }
    
    
    override fun onLowMemory() {
        super.onLowMemory()
        // Clear caches when memory is low
        DataCache.clearCache()
        ImageUtils.clearCache()
        PerformanceMonitor.logMemoryUsage("Low Memory")
    }
}

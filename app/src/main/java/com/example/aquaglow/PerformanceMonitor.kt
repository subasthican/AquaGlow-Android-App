package com.example.aquaglow

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.util.Log

/**
 * PerformanceMonitor tracks app performance metrics
 * Helps identify memory leaks and performance bottlenecks
 */
object PerformanceMonitor {
    
    private const val TAG = "PerformanceMonitor"
    private var startTime = 0L
    private var startMemory = 0L
    
    /**
     * Starts performance monitoring
     */
    fun startMonitoring() {
        startTime = System.currentTimeMillis()
        startMemory = getUsedMemory()
        Log.d(TAG, "Performance monitoring started")
    }
    
    /**
     * Logs performance metrics
     */
    fun logMetrics(tag: String) {
        val currentTime = System.currentTimeMillis()
        val currentMemory = getUsedMemory()
        val timeElapsed = currentTime - startTime
        val memoryUsed = currentMemory - startMemory
        
        Log.d(TAG, "$tag - Time: ${timeElapsed}ms, Memory: ${memoryUsed / 1024}KB")
    }
    
    /**
     * Gets current memory usage
     */
    private fun getUsedMemory(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }
    
    /**
     * Gets memory info from ActivityManager
     */
    fun getMemoryInfo(context: Context): ActivityManager.MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo
    }
    
    /**
     * Checks if device is low on memory
     */
    fun isLowMemory(context: Context): Boolean {
        val memoryInfo = getMemoryInfo(context)
        return memoryInfo.lowMemory
    }
    
    /**
     * Logs memory usage
     */
    fun logMemoryUsage(tag: String) {
        val memoryInfo = getMemoryInfo(AquaGlowApplication.getInstance())
        Log.d(TAG, "$tag - Available Memory: ${memoryInfo.availMem / 1024 / 1024}MB")
        Log.d(TAG, "$tag - Low Memory: ${memoryInfo.lowMemory}")
    }
}


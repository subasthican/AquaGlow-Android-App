package com.example.aquaglow

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * NearbyConnectionsManager handles peer-to-peer communication using Nearby Connections API
 * Allows nearby AquaGlow users to discover each other and exchange wellness data
 */
class NearbyConnectionsManager(private val context: Context) {
    
    private val connectionsClient = Nearby.getConnectionsClient(context)
    private val gson = Gson()
    
    private val TAG = "NearbyConnections"
    private val SERVICE_ID = "com.example.aquaglow.nearby"
    private val STRATEGY = Strategy.P2P_CLUSTER // Allows multiple connections
    
    // Callbacks
    private var onEndpointDiscovered: ((String, String) -> Unit)? = null // endpointId, userName
    private var onEndpointLost: ((String) -> Unit)? = null
    private var onConnectionInitiated: ((String, String) -> Unit)? = null // endpointId, userName
    private var onConnected: ((String) -> Unit)? = null
    private var onDisconnected: ((String) -> Unit)? = null
    private var onDataReceived: ((String, NearbyUserData) -> Unit)? = null // endpointId, data
    
    // Store connected endpoints
    private val connectedEndpoints = mutableMapOf<String, String>() // endpointId -> userName
    
    /**
     * Data model for user information exchanged between devices
     */
    data class NearbyUserData(
        val userId: String,
        val userName: String,
        val userEmail: String,
        val avatar: String,
        val totalScore: Int,
        val currentStreak: Int,
        val achievements: Int,
        val todaySteps: Int,
        val todayMood: String,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Start advertising this device to nearby users
     */
    fun startAdvertising(userName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val advertisingOptions = AdvertisingOptions.Builder()
            .setStrategy(STRATEGY)
            .build()
        
        connectionsClient
            .startAdvertising(
                userName,
                SERVICE_ID,
                connectionLifecycleCallback,
                advertisingOptions
            )
            .addOnSuccessListener {
                Log.d(TAG, "Now advertising as: $userName")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to start advertising", exception)
                onFailure(exception)
            }
    }
    
    /**
     * Start discovering nearby AquaGlow users
     */
    fun startDiscovery(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val discoveryOptions = DiscoveryOptions.Builder()
            .setStrategy(STRATEGY)
            .build()
        
        connectionsClient
            .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener {
                Log.d(TAG, "Started discovering nearby users")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to start discovery", exception)
                onFailure(exception)
            }
    }
    
    /**
     * Stop advertising
     */
    fun stopAdvertising() {
        connectionsClient.stopAdvertising()
        Log.d(TAG, "Stopped advertising")
    }
    
    /**
     * Stop discovery
     */
    fun stopDiscovery() {
        connectionsClient.stopDiscovery()
        Log.d(TAG, "Stopped discovery")
    }
    
    /**
     * Request connection to a discovered endpoint
     */
    fun requestConnection(endpointId: String, userName: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        connectionsClient
            .requestConnection(userName, endpointId, connectionLifecycleCallback)
            .addOnSuccessListener {
                Log.d(TAG, "Connection requested to: $endpointId")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to request connection", exception)
                onFailure(exception)
            }
    }
    
    /**
     * Accept an incoming connection
     */
    fun acceptConnection(endpointId: String) {
        connectionsClient
            .acceptConnection(endpointId, payloadCallback)
            .addOnSuccessListener {
                Log.d(TAG, "Accepted connection: $endpointId")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to accept connection", exception)
            }
    }
    
    /**
     * Reject an incoming connection
     */
    fun rejectConnection(endpointId: String) {
        connectionsClient.rejectConnection(endpointId)
        Log.d(TAG, "Rejected connection: $endpointId")
    }
    
    /**
     * Disconnect from an endpoint
     */
    fun disconnect(endpointId: String) {
        connectionsClient.disconnectFromEndpoint(endpointId)
        connectedEndpoints.remove(endpointId)
        Log.d(TAG, "Disconnected from: $endpointId")
    }
    
    /**
     * Disconnect from all endpoints
     */
    fun disconnectAll() {
        connectionsClient.stopAllEndpoints()
        connectedEndpoints.clear()
        Log.d(TAG, "Disconnected from all endpoints")
    }
    
    /**
     * Send user data to a connected endpoint
     */
    fun sendUserData(endpointId: String, userData: NearbyUserData) {
        val json = gson.toJson(userData)
        val payload = Payload.fromBytes(json.toByteArray())
        
        connectionsClient.sendPayload(endpointId, payload)
        Log.d(TAG, "Sent data to: $endpointId")
    }
    
    /**
     * Send user data to all connected endpoints
     */
    fun broadcastUserData(userData: NearbyUserData) {
        val json = gson.toJson(userData)
        val payload = Payload.fromBytes(json.toByteArray())
        
        connectedEndpoints.keys.forEach { endpointId ->
            connectionsClient.sendPayload(endpointId, payload)
        }
        Log.d(TAG, "Broadcast data to ${connectedEndpoints.size} endpoints")
    }
    
    /**
     * Get current user data from app
     */
    fun getCurrentUserData(): NearbyUserData {
        val sharedPreferences = context.getSharedPreferences("aquaglow_prefs", Context.MODE_PRIVATE)
        
        return NearbyUserData(
            userId = "current_user",
            userName = sharedPreferences.getString("auth_user_name", "User") ?: "User",
            userEmail = sharedPreferences.getString("auth_user_email", "") ?: "",
            avatar = "👤",
            totalScore = calculateWellnessScore(context),
            currentStreak = getCurrentStreak(context),
            achievements = 0, // Achievement system removed
            todaySteps = SensorManager.getCurrentStepCount(context),
            todayMood = getTodayMood(context)
        )
    }
    
    /**
     * Calculate total wellness score
     */
    private fun calculateWellnessScore(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("aquaglow_prefs", Context.MODE_PRIVATE)
        val steps = SensorManager.getCurrentStepCount(context)
        val achievements = 0 // Achievement system removed
        
        // Simplified score based on available data
        return (steps / 100) + (achievements * 50)
    }
    
    /**
     * Get current habit streak
     */
    private fun getCurrentStreak(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("aquaglow_prefs", Context.MODE_PRIVATE)
        // Get the highest streak from any habit (simplified)
        return sharedPreferences.getInt("longest_streak", 0)
    }
    
    /**
     * Get today's mood
     */
    private fun getTodayMood(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("aquaglow_prefs", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayMood = sharedPreferences.getString("mood_$today", null)
        
        return todayMood ?: "😐"
    }
    
    /**
     * Get list of connected endpoints
     */
    fun getConnectedEndpoints(): Map<String, String> = connectedEndpoints.toMap()
    
    // Callback for endpoint discovery
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG, "Discovered endpoint: ${info.endpointName} ($endpointId)")
            onEndpointDiscovered?.invoke(endpointId, info.endpointName)
        }
        
        override fun onEndpointLost(endpointId: String) {
            Log.d(TAG, "Lost endpoint: $endpointId")
            onEndpointLost?.invoke(endpointId)
        }
    }
    
    // Callback for connection lifecycle
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            Log.d(TAG, "Connection initiated with: ${info.endpointName} ($endpointId)")
            onConnectionInitiated?.invoke(endpointId, info.endpointName)
        }
        
        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.d(TAG, "Connected to: $endpointId")
                    onConnected?.invoke(endpointId)
                    
                    // Automatically send user data when connected
                    sendUserData(endpointId, getCurrentUserData())
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    Log.d(TAG, "Connection rejected: $endpointId")
                    onDisconnected?.invoke(endpointId)
                }
                else -> {
                    Log.d(TAG, "Connection failed: $endpointId")
                    onDisconnected?.invoke(endpointId)
                }
            }
        }
        
        override fun onDisconnected(endpointId: String) {
            Log.d(TAG, "Disconnected from: $endpointId")
            connectedEndpoints.remove(endpointId)
            onDisconnected?.invoke(endpointId)
        }
    }
    
    // Callback for receiving data
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val json = String(payload.asBytes()!!)
                try {
                    val userData = gson.fromJson(json, NearbyUserData::class.java)
                    Log.d(TAG, "Received data from: $endpointId - ${userData.userName}")
                    connectedEndpoints[endpointId] = userData.userName
                    onDataReceived?.invoke(endpointId, userData)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse received data", e)
                }
            }
        }
        
        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Handle transfer progress if needed
        }
    }
    
    // Set callbacks
    fun setOnEndpointDiscovered(callback: (String, String) -> Unit) {
        onEndpointDiscovered = callback
    }
    
    fun setOnEndpointLost(callback: (String) -> Unit) {
        onEndpointLost = callback
    }
    
    fun setOnConnectionInitiated(callback: (String, String) -> Unit) {
        onConnectionInitiated = callback
    }
    
    fun setOnConnected(callback: (String) -> Unit) {
        onConnected = callback
    }
    
    fun setOnDisconnected(callback: (String) -> Unit) {
        onDisconnected = callback
    }
    
    fun setOnDataReceived(callback: (String, NearbyUserData) -> Unit) {
        onDataReceived = callback
    }
}


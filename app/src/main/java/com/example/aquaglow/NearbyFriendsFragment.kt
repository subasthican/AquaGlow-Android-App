package com.example.aquaglow

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

/**
 * NearbyFriendsFragment - Discover and connect with nearby AquaGlow users via Bluetooth
 */
class NearbyFriendsFragment : Fragment() {
    
    private lateinit var nearbyConnectionsManager: NearbyConnectionsManager
    private lateinit var scanButton: MaterialButton
    private lateinit var stopButton: MaterialButton
    private lateinit var statusText: TextView
    private lateinit var nearbyUsersRecyclerView: RecyclerView
    private lateinit var connectedUsersRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var nearbyEmptyText: TextView
    private lateinit var connectedEmptyText: TextView
    private lateinit var toolbar: com.google.android.material.appbar.MaterialToolbar
    
    private val nearbyUsers = mutableListOf<NearbyUser>()
    private val connectedUsers = mutableListOf<ConnectedUser>()
    private lateinit var nearbyUsersAdapter: NearbyUsersAdapter
    private lateinit var connectedUsersAdapter: ConnectedUsersAdapter
    
    private var isScanning = false
    private var isAdvertising = false
    
    private val TAG = "NearbyFriends"
    
    data class NearbyUser(
        val endpointId: String,
        val name: String
    )
    
    data class ConnectedUser(
        val endpointId: String,
        val name: String,
        val data: NearbyConnectionsManager.NearbyUserData?
    )
    
    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d(TAG, "Permission results: $permissions")
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            Log.d(TAG, "All permissions granted, starting scan")
            startScanning()
        } else {
            val deniedPermissions = permissions.filter { !it.value }.keys
            Log.w(TAG, "Permissions denied: $deniedPermissions")
            
            AlertDialog.Builder(requireContext())
                .setTitle("Permissions Required")
                .setMessage("Nearby Friends requires Bluetooth and Location permissions to work. Please grant all permissions.")
                .setPositiveButton("Try Again") { _, _ ->
                    checkPermissionsAndScan()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nearby_friends, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d(TAG, "onViewCreated called")
        initializeViews(view)
        setupNearbyConnections()
        setupRecyclerViews()
        setupClickListeners()
        
        // Show helpful tip
        statusText.text = "📱 Make sure both users:\n• Have Bluetooth ON\n• Have Location ON\n• Tap 'Start Scanning'"
    }
    
    private fun initializeViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        scanButton = view.findViewById(R.id.scanButton)
        stopButton = view.findViewById(R.id.stopButton)
        statusText = view.findViewById(R.id.statusText)
        nearbyUsersRecyclerView = view.findViewById(R.id.nearbyUsersRecyclerView)
        connectedUsersRecyclerView = view.findViewById(R.id.connectedUsersRecyclerView)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        nearbyEmptyText = view.findViewById(R.id.nearbyEmptyText)
        connectedEmptyText = view.findViewById(R.id.connectedEmptyText)
        
        stopButton.isEnabled = false
        
        // Setup back button
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
    
    private fun setupNearbyConnections() {
        Log.d(TAG, "setupNearbyConnections called")
        nearbyConnectionsManager = NearbyConnectionsManager(requireContext())
        
        // Set up callbacks
        nearbyConnectionsManager.setOnEndpointDiscovered { endpointId, userName ->
            Log.d(TAG, "Endpoint discovered: $userName ($endpointId)")
            activity?.runOnUiThread {
                addNearbyUser(endpointId, userName)
                Toast.makeText(requireContext(), "Found: $userName", Toast.LENGTH_SHORT).show()
            }
        }
        
        nearbyConnectionsManager.setOnEndpointLost { endpointId ->
            activity?.runOnUiThread {
                removeNearbyUser(endpointId)
            }
        }
        
        nearbyConnectionsManager.setOnConnectionInitiated { endpointId, userName ->
            activity?.runOnUiThread {
                showConnectionRequestDialog(endpointId, userName)
            }
        }
        
        nearbyConnectionsManager.setOnConnected { endpointId ->
            activity?.runOnUiThread {
                moveToConnected(endpointId)
                Toast.makeText(requireContext(), "Connected!", Toast.LENGTH_SHORT).show()
            }
        }
        
        nearbyConnectionsManager.setOnDisconnected { endpointId ->
            activity?.runOnUiThread {
                removeConnectedUser(endpointId)
                Toast.makeText(requireContext(), "Disconnected", Toast.LENGTH_SHORT).show()
            }
        }
        
        nearbyConnectionsManager.setOnDataReceived { endpointId, userData ->
            activity?.runOnUiThread {
                updateConnectedUserData(endpointId, userData)
            }
        }
    }
    
    private fun setupRecyclerViews() {
        nearbyUsersAdapter = NearbyUsersAdapter(nearbyUsers) { nearbyUser ->
            requestConnection(nearbyUser)
        }
        nearbyUsersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = nearbyUsersAdapter
        }
        
        connectedUsersAdapter = ConnectedUsersAdapter(connectedUsers,
            onDisconnect = { connectedUser ->
                disconnectUser(connectedUser)
            },
            onAddFriend = { connectedUser ->
                addAsFriend(connectedUser)
            }
        )
        connectedUsersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = connectedUsersAdapter
        }
        
        updateEmptyStates()
    }
    
    private fun setupClickListeners() {
        scanButton.setOnClickListener {
            checkPermissionsAndScan()
        }
        
        stopButton.setOnClickListener {
            stopScanning()
        }
    }
    
    private fun checkPermissionsAndScan() {
        Log.d(TAG, "checkPermissionsAndScan called")
        
        // Check if Bluetooth is enabled
        if (!isBluetoothEnabled()) {
            showEnableBluetoothDialog()
            return
        }
        
        // Check if Location is enabled (required for Bluetooth discovery)
        if (!isLocationEnabled()) {
            showEnableLocationDialog()
            return
        }
        
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        
        Log.d(TAG, "Missing permissions: ${missingPermissions.size}")
        
        if (missingPermissions.isEmpty()) {
            startScanning()
        } else {
            Log.d(TAG, "Requesting permissions: $missingPermissions")
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
    
    /**
     * Check if Bluetooth is enabled
     */
    private fun isBluetoothEnabled(): Boolean {
        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val bluetoothAdapter = bluetoothManager?.adapter
        val enabled = bluetoothAdapter?.isEnabled == true
        Log.d(TAG, "Bluetooth enabled: $enabled")
        return enabled
    }
    
    /**
     * Check if Location services are enabled
     */
    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val enabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true ||
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
        Log.d(TAG, "Location enabled: $enabled")
        return enabled
    }
    
    /**
     * Show dialog to enable Bluetooth
     */
    private fun showEnableBluetoothDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Bluetooth Required")
            .setMessage("Please enable Bluetooth to discover nearby friends.")
            .setPositiveButton("Enable") { _, _ ->
                try {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivity(enableBtIntent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to open Bluetooth settings", e)
                    Toast.makeText(requireContext(), "Please enable Bluetooth manually in Settings", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    /**
     * Show dialog to enable Location
     */
    private fun showEnableLocationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Required")
            .setMessage("Location services are required for Bluetooth device discovery. Please enable Location.")
            .setPositiveButton("Enable") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to open Location settings", e)
                    Toast.makeText(requireContext(), "Please enable Location in Settings", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun startScanning() {
        Log.d(TAG, "startScanning called")
        val userName = AuthUtils.getUserName(requireContext()) ?: "AquaGlow User"
        Log.d(TAG, "Username: $userName")
        
        // Start advertising
        nearbyConnectionsManager.startAdvertising(
            userName = userName,
            onSuccess = {
                Log.d(TAG, "Advertising started successfully")
                isAdvertising = true
                activity?.runOnUiThread {
                    updateScanningUI()
                    Toast.makeText(requireContext(), "Broadcasting your presence...", Toast.LENGTH_SHORT).show()
                }
            },
            onFailure = { exception ->
                Log.e(TAG, "Failed to advertise", exception)
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to advertise: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
        
        // Start discovery
        nearbyConnectionsManager.startDiscovery(
            onSuccess = {
                Log.d(TAG, "Discovery started successfully")
                isScanning = true
                activity?.runOnUiThread {
                    updateScanningUI()
                    Toast.makeText(requireContext(), "Scanning for nearby users...", Toast.LENGTH_SHORT).show()
                }
            },
            onFailure = { exception ->
                Log.e(TAG, "Failed to discover", exception)
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to discover: ${exception.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
    
    private fun stopScanning() {
        nearbyConnectionsManager.stopAdvertising()
        nearbyConnectionsManager.stopDiscovery()
        isScanning = false
        isAdvertising = false
        updateScanningUI()
        nearbyUsers.clear()
        nearbyUsersAdapter.notifyDataSetChanged()
        updateEmptyStates()
    }
    
    private fun updateScanningUI() {
        if (isScanning || isAdvertising) {
            scanButton.isEnabled = false
            stopButton.isEnabled = true
            statusText.text = "🔍 Scanning for nearby users...\n(Make sure your friend is also scanning)"
            emptyStateLayout.visibility = View.GONE
        } else {
            scanButton.isEnabled = true
            stopButton.isEnabled = false
            statusText.text = "📱 Make sure both users:\n• Have Bluetooth ON\n• Have Location ON\n• Tap 'Start Scanning'"
            updateEmptyStates()
        }
    }
    
    private fun addNearbyUser(endpointId: String, userName: String) {
        if (nearbyUsers.none { it.endpointId == endpointId }) {
            nearbyUsers.add(NearbyUser(endpointId, userName))
            nearbyUsersAdapter.notifyItemInserted(nearbyUsers.size - 1)
            updateEmptyStates()
        }
    }
    
    private fun removeNearbyUser(endpointId: String) {
        val index = nearbyUsers.indexOfFirst { it.endpointId == endpointId }
        if (index >= 0) {
            nearbyUsers.removeAt(index)
            nearbyUsersAdapter.notifyItemRemoved(index)
            updateEmptyStates()
        }
    }
    
    private fun requestConnection(nearbyUser: NearbyUser) {
        val userName = AuthUtils.getUserName(requireContext()) ?: "AquaGlow User"
        nearbyConnectionsManager.requestConnection(
            endpointId = nearbyUser.endpointId,
            userName = userName,
            onSuccess = {
                Toast.makeText(requireContext(), "Connecting to ${nearbyUser.name}...", Toast.LENGTH_SHORT).show()
            },
            onFailure = { exception ->
                Toast.makeText(requireContext(), "Failed to connect: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    private fun showConnectionRequestDialog(endpointId: String, userName: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Connection Request")
            .setMessage("$userName wants to connect with you. Accept?")
            .setPositiveButton("Accept") { _, _ ->
                nearbyConnectionsManager.acceptConnection(endpointId)
            }
            .setNegativeButton("Reject") { _, _ ->
                nearbyConnectionsManager.rejectConnection(endpointId)
            }
            .setCancelable(false)
            .show()
    }
    
    private fun moveToConnected(endpointId: String) {
        // Remove from nearby list
        val index = nearbyUsers.indexOfFirst { it.endpointId == endpointId }
        if (index >= 0) {
            val nearbyUser = nearbyUsers.removeAt(index)
            nearbyUsersAdapter.notifyItemRemoved(index)
            
            // Add to connected list
            connectedUsers.add(ConnectedUser(endpointId, nearbyUser.name, null))
            connectedUsersAdapter.notifyItemInserted(connectedUsers.size - 1)
            updateEmptyStates()
        }
    }
    
    private fun updateConnectedUserData(endpointId: String, userData: NearbyConnectionsManager.NearbyUserData) {
        val index = connectedUsers.indexOfFirst { it.endpointId == endpointId }
        if (index >= 0) {
            connectedUsers[index] = connectedUsers[index].copy(data = userData)
            connectedUsersAdapter.notifyItemChanged(index)
        }
    }
    
    private fun removeConnectedUser(endpointId: String) {
        val index = connectedUsers.indexOfFirst { it.endpointId == endpointId }
        if (index >= 0) {
            connectedUsers.removeAt(index)
            connectedUsersAdapter.notifyItemRemoved(index)
            updateEmptyStates()
        }
    }
    
    private fun disconnectUser(connectedUser: ConnectedUser) {
        AlertDialog.Builder(requireContext())
            .setTitle("Disconnect")
            .setMessage("Disconnect from ${connectedUser.name}?")
            .setPositiveButton("Disconnect") { _, _ ->
                nearbyConnectionsManager.disconnect(connectedUser.endpointId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun addAsFriend(connectedUser: ConnectedUser) {
        connectedUser.data?.let { userData ->
            val friend = SocialManager.Friend(
                id = userData.userId,
                name = userData.userName,
                email = userData.userEmail,
                avatar = userData.avatar,
                isOnline = true,
                lastActive = System.currentTimeMillis(),
                totalScore = userData.totalScore,
                currentStreak = userData.currentStreak,
                achievements = userData.achievements
            )
            
            SocialManager.addFriend(requireContext(), friend)
            Toast.makeText(requireContext(), "${userData.userName} added as friend!", Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(requireContext(), "Waiting for user data...", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateEmptyStates() {
        nearbyEmptyText.visibility = if (nearbyUsers.isEmpty() && isScanning) View.VISIBLE else View.GONE
        connectedEmptyText.visibility = if (connectedUsers.isEmpty()) View.VISIBLE else View.GONE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        nearbyConnectionsManager.disconnectAll()
        nearbyConnectionsManager.stopAdvertising()
        nearbyConnectionsManager.stopDiscovery()
    }
}

/**
 * Adapter for nearby users list
 */
class NearbyUsersAdapter(
    private val users: List<NearbyFriendsFragment.NearbyUser>,
    private val onConnect: (NearbyFriendsFragment.NearbyUser) -> Unit
) : RecyclerView.Adapter<NearbyUsersAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.userNameText)
        val connectButton: MaterialButton = view.findViewById(R.id.connectButton)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nearby_user, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.nameText.text = "📱 ${user.name}"
        holder.connectButton.setOnClickListener {
            onConnect(user)
        }
    }
    
    override fun getItemCount() = users.size
}

/**
 * Adapter for connected users list
 */
class ConnectedUsersAdapter(
    private val users: List<NearbyFriendsFragment.ConnectedUser>,
    private val onDisconnect: (NearbyFriendsFragment.ConnectedUser) -> Unit,
    private val onAddFriend: (NearbyFriendsFragment.ConnectedUser) -> Unit
) : RecyclerView.Adapter<ConnectedUsersAdapter.ViewHolder>() {
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.connectedUserName)
        val statsText: TextView = view.findViewById(R.id.connectedUserStats)
        val addFriendButton: MaterialButton = view.findViewById(R.id.addFriendButton)
        val disconnectButton: MaterialButton = view.findViewById(R.id.disconnectButton)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_connected_user, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.nameText.text = "🟢 ${user.name}"
        
        user.data?.let { data ->
            holder.statsText.text = "Score: ${data.totalScore} | Streak: ${data.currentStreak} days | ${data.todayMood}"
        } ?: run {
            holder.statsText.text = "Loading stats..."
        }
        
        holder.addFriendButton.setOnClickListener {
            onAddFriend(user)
        }
        
        holder.disconnectButton.setOnClickListener {
            onDisconnect(user)
        }
    }
    
    override fun getItemCount() = users.size
}


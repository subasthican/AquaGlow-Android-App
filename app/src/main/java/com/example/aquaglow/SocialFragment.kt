package com.example.aquaglow

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.*

/**
 * SocialFragment displays friends, challenges, and social features
 */
class SocialFragment : Fragment() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var challengesRecyclerView: RecyclerView
    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var friendsCountText: TextView
    private lateinit var challengesCountText: TextView
    private lateinit var rankText: TextView
    private lateinit var addFriendButton: com.google.android.material.button.MaterialButton
    private lateinit var nearbyFriendsButton: com.google.android.material.button.MaterialButton
    private lateinit var emptyStateLayout: LinearLayout

    private lateinit var challengesAdapter: ChallengesAdapter
    private lateinit var friendsAdapter: FriendsAdapter
    private var challengesList = mutableListOf<SocialManager.Challenge>()
    private var friendsList = mutableListOf<SocialManager.Friend>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_social, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupRecyclerViews()
        setupClickListeners()
        loadData()
        setupGlowEffects(view)
    }

    private fun initializeViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        challengesRecyclerView = view.findViewById(R.id.challengesRecyclerView)
        friendsRecyclerView = view.findViewById(R.id.friendsRecyclerView)
        friendsCountText = view.findViewById(R.id.friendsCountText)
        challengesCountText = view.findViewById(R.id.challengesCountText)
        rankText = view.findViewById(R.id.rankText)
        addFriendButton = view.findViewById(R.id.addFriendButton)
        nearbyFriendsButton = view.findViewById(R.id.nearbyFriendsButton)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
    }

    private fun setupRecyclerViews() {
        challengesAdapter = ChallengesAdapter(challengesList) { challenge ->
            joinChallenge(challenge.id)
        }
        challengesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = challengesAdapter
        }

        friendsAdapter = FriendsAdapter(
            friendsList,
            onFriendClick = { friend ->
                // Handle friend click
            },
            onRemoveClick = { friend ->
                removeFriend(friend.id)
            },
            onChallengeClick = { friend ->
                challengeFriend(friend.id)
            }
        )
        friendsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = friendsAdapter
        }
    }

    private fun setupClickListeners() {
        // Setup toolbar back button
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        addFriendButton.setOnClickListener {
            showAddFriendDialog()
        }
        
        nearbyFriendsButton.setOnClickListener {
            val navController = findNavController()
            navController.navigate(R.id.nearbyFriendsFragment)
        }
    }

    private fun loadData() {
        loadChallenges()
        loadFriends()
        updateStats()
    }

    private fun loadChallenges() {
        challengesList.clear()
        challengesList.addAll(SocialManager.getActiveChallenges(requireContext()))
        challengesAdapter.notifyDataSetChanged()
    }

    private fun loadFriends() {
        friendsList.clear()
        friendsList.addAll(SocialManager.getAllFriends(requireContext()))
        friendsAdapter.notifyDataSetChanged()
        
        // Show/hide empty state
        emptyStateLayout.visibility = if (friendsList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateStats() {
        friendsCountText.text = friendsList.size.toString()
        challengesCountText.text = challengesList.size.toString()
        
        // Get user's rank (simplified - in real app, this would come from server)
        val leaderboard = SocialManager.getLeaderboard(requireContext())
        val userRank = leaderboard.find { it.userId == "current_user" }?.rank ?: 1
        rankText.text = "#$userRank"
    }

    private fun showAddFriendDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_friend, null)
        
        val nameInput = dialogView.findViewById<EditText>(R.id.friendNameInput)
        val emailInput = dialogView.findViewById<EditText>(R.id.friendEmailInput)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add Friend")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameInput.text.toString().trim()
                val email = emailInput.text.toString().trim()

                if (name.isNotEmpty() && email.isNotEmpty()) {
                    val newFriend = SocialManager.Friend(
                        id = System.currentTimeMillis().toString(),
                        name = name,
                        email = email,
                        avatar = "👤",
                        totalScore = (100..2000).random(),
                        currentStreak = (0..30).random(),
                        achievements = (0..12).random()
                    )
                    SocialManager.addFriend(requireContext(), newFriend)
                    loadFriends()
                    updateStats()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun joinChallenge(challengeId: String) {
        SocialManager.joinChallenge(requireContext(), challengeId, "current_user")
        loadChallenges()
        updateStats()
    }

    private fun removeFriend(friendId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove Friend")
            .setMessage("Are you sure you want to remove this friend?")
            .setPositiveButton("Remove") { _, _ ->
                SocialManager.removeFriend(requireContext(), friendId)
                loadFriends()
                updateStats()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun challengeFriend(friendId: String) {
        // Show challenge options
        val challenges = SocialManager.getChallengeSuggestions(requireContext())
        val challengeNames = challenges.map { it.title }.toTypedArray()
        
        AlertDialog.Builder(requireContext())
            .setTitle("Challenge Friend")
            .setItems(challengeNames) { _, which ->
                val selectedChallenge = challenges[which]
                SocialManager.createChallenge(requireContext(), selectedChallenge)
                loadChallenges()
                updateStats()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupGlowEffects(view: View) {
        // Add breathing effect to stats cards
        GlowAnimationUtils.createBreathingEffect(friendsCountText, 3000L)
        GlowAnimationUtils.createBreathingEffect(challengesCountText, 3000L)
        GlowAnimationUtils.createBreathingEffect(rankText, 3000L)
        
        // Add pulse effect to add friend button
        GlowAnimationUtils.createPulseEffect(addFriendButton, 2000L)
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}

/**
 * Adapter for displaying challenges
 */
class ChallengesAdapter(
    private val challenges: List<SocialManager.Challenge>,
    private val onJoinClick: (SocialManager.Challenge) -> Unit
) : RecyclerView.Adapter<ChallengesAdapter.ChallengeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_challenge, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        holder.bind(challenges[position], onJoinClick)
    }

    override fun getItemCount(): Int = challenges.size

    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: TextView = itemView.findViewById(R.id.challengeIcon)
        private val title: TextView = itemView.findViewById(R.id.challengeTitle)
        private val description: TextView = itemView.findViewById(R.id.challengeDescription)
        private val progress: android.widget.ProgressBar = itemView.findViewById(R.id.challengeProgress)
        private val progressText: TextView = itemView.findViewById(R.id.challengeProgressText)
        private val participants: TextView = itemView.findViewById(R.id.challengeParticipants)
        private val timeLeft: TextView = itemView.findViewById(R.id.challengeTimeLeft)
        private val joinButton: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.joinChallengeButton)

        fun bind(challenge: SocialManager.Challenge, onJoinClick: (SocialManager.Challenge) -> Unit) {
            icon.text = when (challenge.type) {
                SocialManager.ChallengeType.HABIT_STREAK -> "🔥"
                SocialManager.ChallengeType.MOOD_TRACKING -> "😊"
                SocialManager.ChallengeType.STEP_COUNT -> "🏃‍♂️"
                SocialManager.ChallengeType.HYDRATION -> "💧"
                SocialManager.ChallengeType.WELLNESS_SCORE -> "📊"
                SocialManager.ChallengeType.CUSTOM -> "🎯"
            }
            
            title.text = challenge.title
            description.text = challenge.description
            
            // Calculate progress (simplified)
            val progressPercentage = 25 // In real app, get actual progress
            progress.progress = progressPercentage
            progressText.text = "$progressPercentage%"
            
            participants.text = "👥 ${challenge.participants.size} participants"
            
            val daysLeft = ((challenge.endDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
            timeLeft.text = "⏰ $daysLeft days left"
            
            joinButton.setOnClickListener {
                onJoinClick(challenge)
            }
        }
    }
}

/**
 * Adapter for displaying friends
 */
class FriendsAdapter(
    private val friends: List<SocialManager.Friend>,
    private val onFriendClick: (SocialManager.Friend) -> Unit,
    private val onRemoveClick: (SocialManager.Friend) -> Unit,
    private val onChallengeClick: (SocialManager.Friend) -> Unit
) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(friends[position], onFriendClick, onRemoveClick, onChallengeClick)
    }

    override fun getItemCount(): Int = friends.size

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatar: TextView = itemView.findViewById(R.id.friendAvatar)
        private val name: TextView = itemView.findViewById(R.id.friendName)
        private val status: TextView = itemView.findViewById(R.id.friendStatus)
        private val score: TextView = itemView.findViewById(R.id.friendScore)
        private val streak: TextView = itemView.findViewById(R.id.friendStreak)
        private val challengeButton: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.challengeFriendButton)
        private val removeButton: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.removeFriendButton)

        fun bind(
            friend: SocialManager.Friend,
            onFriendClick: (SocialManager.Friend) -> Unit,
            onRemoveClick: (SocialManager.Friend) -> Unit,
            onChallengeClick: (SocialManager.Friend) -> Unit
        ) {
            avatar.text = friend.avatar
            name.text = friend.name
            status.text = if (friend.isOnline) "🟢" else "⚪"
            score.text = "Score: ${friend.totalScore}"
            streak.text = "🔥 ${friend.currentStreak} day streak"
            
            // Handle clicking on friend item
            itemView.setOnClickListener {
                onFriendClick(friend)
            }
            
            challengeButton.setOnClickListener {
                onChallengeClick(friend)
            }
            
            removeButton.setOnClickListener {
                onRemoveClick(friend)
            }
        }
    }
}

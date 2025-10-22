package com.example.aquaglow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

/**
 * GamesFragment displays mini-games and gamified wellness activities
 */
class GamesFragment : Fragment() {

    private lateinit var gamesRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView
    private lateinit var totalScoreText: TextView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var refreshButton: MaterialButton

    private lateinit var gamesAdapter: GamesAdapter
    private var gamesList = mutableListOf<GameManager.MiniGame>()
    private var totalScore = 0
    private var loadingIndicator: ProgressBar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_games, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackButton(view)

        initializeViews(view)
        setupRecyclerView()
        setupClickListeners()
        showLoading()
        loadGames()
        updateProgress()
        hideLoading()
        setupGlowEffects(view)
    }
    
    private fun showLoading() {
        loadingIndicator?.visibility = View.VISIBLE
        gamesRecyclerView.visibility = View.GONE
    }
    
    private fun hideLoading() {
        loadingIndicator?.visibility = View.GONE
        gamesRecyclerView.visibility = View.VISIBLE
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this fragment
        loadGames()
        updateProgress()
    }

    private fun setupBackButton(view: View) {
        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initializeViews(view: View) {
        gamesRecyclerView = view.findViewById(R.id.gamesRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        progressText = view.findViewById(R.id.progressText)
        totalScoreText = view.findViewById(R.id.totalScoreText)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        refreshButton = view.findViewById(R.id.refreshButton)
    }

    private fun setupRecyclerView() {
        gamesAdapter = GamesAdapter(gamesList) { game ->
            startGame(game)
        }
        gamesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = gamesAdapter
        }
    }

    private fun setupClickListeners() {
        refreshButton.setOnClickListener {
            showLoading()
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                loadGames()
                hideLoading()
            }, 500)
        }
    }

    private fun loadGames() {
        val allGames = GameManager.getAllGames(requireContext())
        gamesList.clear()
        gamesList.addAll(allGames)
        
        totalScore = GameManager.getTotalScore(requireContext())
        
        gamesAdapter.notifyDataSetChanged()
        updateProgress()
        updateEmptyState()
    }

    private fun updateProgress() {
        val completedGames = GameManager.getCompletedGamesCount(requireContext())
        val totalGames = gamesList.size
        val progress = if (totalGames > 0) (completedGames * 100) / totalGames else 0
        
        progressBar.progress = progress
        progressText.text = "$completedGames/$totalGames completed"
        totalScoreText.text = "Total Score: $totalScore points"
    }

    private fun updateEmptyState() {
        if (gamesList.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            gamesRecyclerView.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            gamesRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun startGame(game: GameManager.MiniGame) {
        val intent = when (game.id) {
            "reflex_tap" -> android.content.Intent(requireContext(), ReflexGameActivity::class.java)
            "blink_sequence" -> android.content.Intent(requireContext(), BlinkGameActivity::class.java)
            "breathing_circle" -> android.content.Intent(requireContext(), BreathingGameActivity::class.java)
            "memory_sequence" -> android.content.Intent(requireContext(), MemoryGameActivity::class.java)
            else -> android.content.Intent(requireContext(), PlaceholderGameActivity::class.java)
        }
        intent.putExtra("game_id", game.id)
        intent.putExtra("game_title", game.title)
        startActivity(intent)
    }

    private fun setupGlowEffects(view: View) {
        // Apply glow effects to buttons
        refreshButton.let { button ->
            button.setOnTouchListener { _, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        button.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                        true
                    }
                    android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                        button.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                        false
                    }
                    else -> false
                }
            }
        }
    }
}

/**
 * Adapter for games RecyclerView
 */
class GamesAdapter(
    private val games: List<GameManager.MiniGame>,
    private val onGameClick: (GameManager.MiniGame) -> Unit
) : RecyclerView.Adapter<GamesAdapter.GameViewHolder>() {

    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.gameTitle)
        val descriptionText: TextView = itemView.findViewById(R.id.gameDescription)
        val typeText: TextView = itemView.findViewById(R.id.gameDifficulty)
        val difficultyText: TextView = itemView.findViewById(R.id.gameDifficulty)
        val durationText: TextView = itemView.findViewById(R.id.gameDuration)
        val pointsText: TextView = itemView.findViewById(R.id.gamePoints)
        val playButton: MaterialButton = itemView.findViewById(R.id.playGameButton)
        val wellnessBenefit: TextView = itemView.findViewById(R.id.wellnessBenefit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]
        
        holder.titleText.text = game.title
        holder.descriptionText.text = game.description
        holder.typeText.text = game.type.name.replace("_", " ")
        holder.difficultyText.text = game.difficulty.name
        holder.durationText.text = "⏱️ ${game.duration}s"
        holder.pointsText.text = "⭐ ${game.points} pts"
        holder.wellnessBenefit.text = "💡 ${game.wellnessBenefit}"
        
        // Set click listener on the play button
        holder.playButton.setOnClickListener {
            onGameClick(game)
        }
        
        // Also allow clicking on the entire card
        holder.itemView.setOnClickListener {
            onGameClick(game)
        }
    }

    override fun getItemCount(): Int = games.size
}

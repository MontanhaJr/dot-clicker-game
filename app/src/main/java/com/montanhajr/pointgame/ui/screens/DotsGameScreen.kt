package com.montanhajr.pointgame.ui.screens

import android.app.Activity
import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.montanhajr.pointgame.R
import com.montanhajr.pointgame.logic.BillingManager
import com.montanhajr.pointgame.logic.CareerManager
import com.montanhajr.pointgame.logic.GameState
import com.montanhajr.pointgame.logic.StatisticsManager
import com.montanhajr.pointgame.models.*
import com.montanhajr.pointgame.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun DotsGameScreen(
    gameMode: GameMode,
    difficulty: Difficulty?,
    numPlayers: Int,
    playerNames: List<String>? = null,
    boardStyle: BoardStyle = BoardStyle.GALAXY,
    careerLevel: Int? = null,
    onBackToMenu: () -> Unit
) {
    val context = LocalContext.current
    val billingManager = remember { BillingManager(context) }
    val statsManager = remember { StatisticsManager(context) }
    val careerManager = remember { CareerManager(context) }
    
    var gameState by remember {
        mutableStateOf(
            GameState.createNew(
                isCpuGame = gameMode == GameMode.VS_CPU,
                difficulty = difficulty ?: Difficulty.MEDIUM,
                numPlayers = numPlayers,
                playerNames = playerNames,
                boardStyle = boardStyle,
                careerLevel = careerLevel
            )
        )
    }
    
    var stateHistory by remember { mutableStateOf(listOf<GameState>()) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }
    var showFallbackInterstitial by remember { mutableStateOf(false) }
    var showLevelCompleteDialog by remember { mutableStateOf(false) }
    var unlockedAchievementName by remember { mutableStateOf<String?>(null) }
    var matchStartTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    var showRewardFab by remember { mutableStateOf(false) }
    var rewardCountdown by remember { mutableIntStateOf(10) }
    
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val cardWidthPx = with(density) { 96.dp.roundToPx() }
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }

    val connectionSound = remember { MediaPlayer.create(context, R.raw.connection) }
    val playerPointSound = remember { MediaPlayer.create(context, R.raw.player_point) }
    val cpuPointSound = remember { MediaPlayer.create(context, R.raw.cpu_point) }

    val uiColors = remember(boardStyle) { getStyleUiColors(boardStyle) }

    LaunchedEffect(Unit) {
        AdManager.loadInterstitial(context)
        AdManager.loadRewardedAd(context)
        
        if (statsManager.isAdPending()) {
            val activity = context as? Activity
            if (activity != null) {
                AdManager.showInterstitial(
                    activity = activity,
                    onAdDismissed = { statsManager.setAdPending(false) },
                    onShowFallback = { showFallbackInterstitial = true }
                )
            }
        }
    }

    LaunchedEffect(gameState.lines.size) {
        if (!showRewardFab && !gameState.gameOver && gameState.currentPlayer == 1 && gameState.lines.isNotEmpty()) {
            if (Random.nextFloat() < 0.25f) {
                delay(1000)
                showRewardFab = true
                rewardCountdown = 10
            }
        }
    }

    LaunchedEffect(showRewardFab) {
        if (showRewardFab) {
            while (rewardCountdown > 0) {
                delay(1000)
                rewardCountdown--
            }
            showRewardFab = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            connectionSound?.release()
            playerPointSound?.release()
            cpuPointSound?.release()
        }
    }

    LaunchedEffect(gameState.gameOver) {
        if (gameState.gameOver) {
            val duration = System.currentTimeMillis() - matchStartTime
            val humanTriangles = if (gameMode == GameMode.VS_CPU) gameState.playerScores[0] else gameState.playerScores.sum()
            statsManager.addTriangles(humanTriangles)
            
            val newlyUnlocked = if (gameMode == GameMode.VS_CPU) {
                val result = when {
                    gameState.playerScores[0] > gameState.playerScores[1] -> StatisticsManager.MatchResult.WIN
                    gameState.playerScores[0] < gameState.playerScores[1] -> StatisticsManager.MatchResult.LOSS
                    else -> StatisticsManager.MatchResult.DRAW
                }
                
                if (result == StatisticsManager.MatchResult.WIN && gameState.careerLevel != null) {
                    if (gameState.careerLevel == careerManager.getCurrentLevel()) {
                        careerManager.completeLevel()
                        showLevelCompleteDialog = true
                    }
                }
                
                statsManager.recordMatch(gameState.difficulty, result, duration)
            } else {
                statsManager.recordMatch(null, StatisticsManager.MatchResult.DRAW, duration)
            }
            
            if (newlyUnlocked != null) {
                delay(1000)
                unlockedAchievementName = newlyUnlocked
            }
        }
    }

    LaunchedEffect(gameState.currentPlayer, gameState.gameOver) {
        if (gameState.gameOver) delay(500)
        val targetScroll = ((gameState.currentPlayer - 1) * cardWidthPx) + (cardWidthPx / 2) - (screenWidthPx / 2)
        scrollState.animateScrollTo(targetScroll.coerceAtLeast(0))
    }

    LaunchedEffect(gameState.currentPlayer, gameState.gameOver, gameState.lines.size) {
        if (gameMode == GameMode.VS_CPU && gameState.currentPlayer == 2 && !gameState.gameOver) {
            delay(800)
            val cpuMove = gameState.getCpuMove()
            if (cpuMove != null) {
                val previousTriangles = gameState.triangles.size
                stateHistory = stateHistory + gameState
                gameState = gameState.drawLine(cpuMove.first, cpuMove.second)
                scope.launch {
                    try {
                        if (gameState.triangles.size > previousTriangles) cpuPointSound?.start() else connectionSound?.start()
                    } catch (e: Exception) {}
                }
            }
        }
    }

    fun performRestart() {
        stateHistory = emptyList()
        gameState = GameState.createNew(
            isCpuGame = gameMode == GameMode.VS_CPU,
            difficulty = gameState.difficulty,
            numPlayers = numPlayers,
            playerNames = playerNames,
            boardStyle = boardStyle,
            careerLevel = gameState.careerLevel
        )
        matchStartTime = System.currentTimeMillis()
        showRestartDialog = false
        statsManager.setAdPending(false)
    }

    fun goToNextLevel() {
        val nextLevel = (gameState.careerLevel ?: 0) + 1
        stateHistory = emptyList()
        gameState = GameState.createNew(
            isCpuGame = true,
            difficulty = careerManager.getDifficultyForLevel(nextLevel),
            numPlayers = 2,
            playerNames = playerNames,
            boardStyle = boardStyle,
            numPointsParam = careerManager.getPointsCountForLevel(nextLevel),
            careerLevel = nextLevel
        )
        matchStartTime = System.currentTimeMillis()
        showLevelCompleteDialog = false
        statsManager.setAdPending(false)
    }

    fun handleUndo() {
        if (stateHistory.isNotEmpty()) {
            gameState = stateHistory.last()
            stateHistory = stateHistory.dropLast(1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BoardBackground(style = boardStyle)
        Column(modifier = Modifier.fillMaxSize()) {
            GameHeader(
                gameState = gameState,
                gameMode = gameMode,
                scrollState = scrollState,
                uiColors = uiColors,
                onNewGame = { if (gameState.gameOver) performRestart() else showRestartDialog = true },
                onShowRules = { showRulesDialog = true },
                onBackToMenu = onBackToMenu
            )
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                key(gameState) {
                    GameBoard(
                        gameState = gameState,
                        enabled = gameMode != GameMode.VS_CPU || gameState.currentPlayer == 1,
                        onLineDrawn = { startId, endId ->
                            val previousTriangles = gameState.triangles.size
                            stateHistory = stateHistory + gameState
                            gameState = gameState.drawLine(startId, endId)
                            scope.launch {
                                try {
                                    if (gameState.triangles.size > previousTriangles) playerPointSound?.start() else connectionSound?.start()
                                } catch (e: Exception) {}
                            }
                        }
                    )
                }

                RewardUndoFab(
                    visible = showRewardFab && !gameState.gameOver && stateHistory.isNotEmpty(),
                    countdown = rewardCountdown,
                    onUndo = {
                        val activity = context as? Activity
                        if (activity != null) {
                            showRewardFab = false
                            AdManager.showRewardedAd(
                                activity = activity,
                                onRewardEarned = { handleUndo() },
                                onAdFailed = { handleUndo() }
                            )
                        }
                    }
                )
            }
            AdBanner(onPremiumClick = { showPremiumDialog = true })
        }
    }

    GameDialogManager(
        showRules = showRulesDialog,
        showRestart = showRestartDialog,
        showPremium = showPremiumDialog,
        showLevelComplete = showLevelCompleteDialog,
        showFallbackInterstitial = showFallbackInterstitial,
        unlockedAchievement = unlockedAchievementName,
        careerLevel = gameState.careerLevel ?: 0,
        onDismissRules = { showRulesDialog = false },
        onConfirmRestart = { performRestart() },
        onDismissRestart = { showRestartDialog = false },
        onDismissPremium = { showPremiumDialog = false },
        onSubscribePremium = { activity -> billingManager.launchPurchaseFlow(activity); showPremiumDialog = false },
        onNextLevel = { goToNextLevel() },
        onBackToMenu = onBackToMenu,
        onDismissAchievement = { unlockedAchievementName = null },
        onDismissFallback = { showFallbackInterstitial = false; performRestart() },
        onPremiumFromFallback = { showFallbackInterstitial = false; showPremiumDialog = true }
    )
}

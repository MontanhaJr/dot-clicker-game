package com.montanhajr.pointgame.ui.screens

import android.app.Activity
import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.montanhajr.pointgame.BuildConfig
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
    gameType: GameType = GameType.TRIANGLES,
    numPlayers: Int,
    playerNames: List<String>? = null,
    boardStyle: BoardStyle = BoardStyle.DEFAULT_POP,
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
                careerLevel = careerLevel,
                gameType = gameType
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
    
    // Power-up UI states
    var showRewardFab by remember { mutableStateOf(false) }
    var rewardCountdown by remember { mutableIntStateOf(10) }
    var currentPowerUp by remember { mutableStateOf(PowerUpType.UNDO) }
    var inventoryPowerUp by remember { mutableStateOf<PowerUpType?>(null) }
    
    // Debug states
    var showDebugMenu by remember { mutableStateOf(false) }
    
    // Nova flag para evitar sobreposição durante a propaganda
    var isAdWatching by remember { mutableStateOf(false) }
    
    var showEagleEye by remember { mutableStateOf(false) }
    var showXRay by remember { mutableStateOf(false) }
    var isEraserActive by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val cardWidthPx = with(density) { 96.dp.roundToPx() }
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }

    val connectionSound = remember { MediaPlayer.create(context, R.raw.connection) }
    val playerPointSound = remember { MediaPlayer.create(context, R.raw.player_point) }
    val cpuPointSound = remember { MediaPlayer.create(context, R.raw.cpu_point) }

    val uiColors = remember(gameState.boardStyle) { getStyleUiColors(gameState.boardStyle) }

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

    LaunchedEffect(gameState.gameOver) {
        if (!gameState.gameOver) {
            val isDebug = BuildConfig.DEBUG
            while (true) {
                val minDelay = if (isDebug) 5000L else 15000L
                val maxDelay = if (isDebug) 10000L else 45000L
                val nextAttemptDelay = (minDelay..maxDelay).random()
                
                delay(nextAttemptDelay)
                
                if (inventoryPowerUp == null && !showRewardFab && !isAdWatching && !gameState.gameOver && AdManager.isNetworkAvailable(context)) {
                    val spawnChance = if (isDebug) 0.9f else 0.6f
                    if (Random.nextFloat() < spawnChance) {
                        currentPowerUp = PowerUpType.entries.toTypedArray().random()
                        showRewardFab = true
                        rewardCountdown = 10
                    }
                }
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
            val humanScore = if (gameMode == GameMode.VS_CPU) gameState.playerScores[0] else gameState.playerScores.sum()
            
            if (gameState.gameType == GameType.TRIANGLES) {
                statsManager.addTriangles(humanScore)
            } else {
                statsManager.addSquares(humanScore)
            }
            
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
                
                statsManager.recordMatch(
                    difficulty = gameState.difficulty,
                    result = result,
                    timeMs = duration,
                    isTraining = gameState.careerLevel == null
                )
            } else {
                statsManager.recordMatch(null, StatisticsManager.MatchResult.DRAW, duration, isTraining = false)
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

    LaunchedEffect(gameState.currentPlayer, gameState.gameOver, gameState.lines.size, gameState.protectionShieldTurns) {
        if (gameMode == GameMode.VS_CPU && gameState.currentPlayer == 2 && !gameState.gameOver) {
            // Lógica especial de Shield ativo para a CPU
            if (gameState.protectionShieldTurns > 0) {
                val allMoves = gameState.getAllValidMoves()
                val onlyScoringMoves = allMoves.all { gameState.moveCompletesTriangle(it) }
                
                if (onlyScoringMoves && allMoves.isNotEmpty()) {
                    delay(1000)
                    // Toast message hardcoded corrected below if needed, but Toast often uses context strings
                    gameState = gameState.skipCpuTurnDueToShield()
                    return@LaunchedEffect
                }
            }

            delay(800)
            val cpuMove = gameState.getCpuMove()
            if (cpuMove != null) {
                val previousScore = gameState.playerScores.sum()
                stateHistory = stateHistory + gameState
                gameState = gameState.drawLine(cpuMove.first, cpuMove.second)
                scope.launch {
                    try {
                        if (gameState.playerScores.sum() > previousScore) cpuPointSound?.start() else connectionSound?.start()
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
            careerLevel = gameState.careerLevel,
            gameType = gameState.gameType
        )
        matchStartTime = System.currentTimeMillis()
        showRestartDialog = false
        statsManager.setAdPending(false)
        showEagleEye = false
        showXRay = false
        isEraserActive = false
        showRewardFab = false
        inventoryPowerUp = null
        isAdWatching = false
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
            careerLevel = nextLevel,
            gameType = if (nextLevel % 2 != 0) GameType.TRIANGLES else GameType.SQUARES
        )
        matchStartTime = System.currentTimeMillis()
        showLevelCompleteDialog = false
        statsManager.setAdPending(false)
        showRewardFab = false
        inventoryPowerUp = null
        isAdWatching = false
    }

    fun handleUndo() {
        if (stateHistory.isNotEmpty()) {
            gameState = stateHistory.last()
            stateHistory = stateHistory.dropLast(1)
        }
    }

    fun activatePowerUp(type: PowerUpType) {
        when (type) {
            PowerUpType.UNDO -> handleUndo()
            PowerUpType.EAGLE_EYE -> {
                val completable = gameState.getAllCompletableLines()
                if (completable.isEmpty()) {
                    // This could be moved to strings if it's a user facing error
                    Toast.makeText(context, context.getString(R.string.no_moves_to_close), Toast.LENGTH_SHORT).show()
                } else {
                    scope.launch {
                        showEagleEye = true
                        delay(3000)
                        showEagleEye = false
                    }
                }
            }
            PowerUpType.XRAY_VISION -> scope.launch {
                showXRay = true
                delay(5000)
                showXRay = false
            }
            PowerUpType.AUTO_SNAP -> {
                val move = gameState.getAutoSnapMove()
                if (move != null) {
                    val previousScore = gameState.playerScores.sum()
                    stateHistory = stateHistory + gameState
                    gameState = gameState.drawLine(move.first, move.second)
                    scope.launch { if (gameState.playerScores.sum() > previousScore) playerPointSound?.start() else connectionSound?.start() }
                }
            }
            PowerUpType.DOUBLE_MOVE -> gameState = gameState.copy(doubleMoveActive = true)
            PowerUpType.FREEZE_CPU -> gameState = gameState.copy(freezeCpuTurns = 1)
            PowerUpType.ERASER -> isEraserActive = true
            PowerUpType.GOLDEN_TRIANGLE -> gameState = gameState.copy(scoreMultiplier = 3, multiplierTurns = 2)
            PowerUpType.PROTECTION_SHIELD -> gameState = gameState.copy(protectionShieldTurns = 2)
        }
        inventoryPowerUp = null // Remove do inventário após usar
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BoardBackground(style = gameState.boardStyle)
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
                key(gameState, showEagleEye, showXRay, isEraserActive) {
                    GameBoard(
                        gameState = gameState,
                        enabled = gameMode != GameMode.VS_CPU || gameState.currentPlayer == 1,
                        showEagleEye = showEagleEye,
                        showXRay = showXRay,
                        isEraserActive = isEraserActive,
                        onLineDrawn = { startId, endId ->
                            if (gameState.isValidMove(startId, endId)) {
                                val previousScore = gameState.playerScores.sum()
                                stateHistory = stateHistory + gameState
                                gameState = gameState.drawLine(startId, endId)
                                scope.launch {
                                    try {
                                        if (gameState.playerScores.sum() > previousScore) playerPointSound?.start() else connectionSound?.start()
                                    } catch (e: Exception) {}
                                }
                            }
                        },
                        onLineErased = { line ->
                            gameState = gameState.removeLine(line)
                            isEraserActive = false
                        }
                    )
                }

                RewardPowerUpFab(
                    visible = showRewardFab && !gameState.gameOver && (currentPowerUp != PowerUpType.UNDO || stateHistory.isNotEmpty()),
                    countdown = rewardCountdown,
                    powerUpType = currentPowerUp,
                    onActivate = { type ->
                        val activity = context as? Activity
                        if (activity != null) {
                            showRewardFab = false
                            isAdWatching = true // Marcamos que o usuário entrou na propaganda
                            AdManager.showRewardedAd(
                                activity = activity,
                                onRewardEarned = { 
                                    inventoryPowerUp = type
                                    isAdWatching = false // Propaganda concluída com sucesso
                                },
                                onAdFailed = { 
                                    isAdWatching = false // Permite novo sorteio se o ad falhar
                                }
                            )
                        }
                    }
                )

                InventoryPowerUpButton(
                    powerUpType = inventoryPowerUp,
                    onActivate = { inventoryPowerUp?.let { activatePowerUp(it) } }
                )
                
                // Botão de Debug de Power-ups (Apenas em Debug)
                if (BuildConfig.DEBUG) {
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                        IconButton(
                            onClick = { showDebugMenu = !showDebugMenu },
                            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = "Debug", tint = Color.Green)
                        }
                        
                        DropdownMenu(
                            expanded = showDebugMenu,
                            onDismissRequest = { showDebugMenu = false }
                        ) {
                            PowerUpType.entries.forEach { powerUp ->
                                DropdownMenuItem(
                                    text = { Text(powerUp.displayName) },
                                    onClick = {
                                        inventoryPowerUp = powerUp
                                        showDebugMenu = false
                                        // Hardcoded debug message is okay
                                        Toast.makeText(context, "Debug: ${powerUp.displayName} adicionado!", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
            AdBanner(onPremiumClick = { showPremiumDialog = true })
        }
    }

    GameDialogManager(
        gameState = gameState,
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

package com.montanhajr.pointgame.ui.screens

import android.app.Activity
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NavigateNext
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showRewardFab && !gameState.gameOver && stateHistory.isNotEmpty(),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { rewardCountdown / 10f },
                                    modifier = Modifier.size(64.dp),
                                    color = Color(0xFF00FFFF),
                                    strokeWidth = 4.dp,
                                    trackColor = Color.White.copy(alpha = 0.1f)
                                )
                                LargeFloatingActionButton(
                                    onClick = {
                                        val activity = context as? Activity
                                        if (activity != null) {
                                            showRewardFab = false
                                            AdManager.showRewardedAd(
                                                activity = activity,
                                                onRewardEarned = { handleUndo() },
                                                onAdFailed = { handleUndo() }
                                            )
                                        }
                                    },
                                    shape = CircleShape,
                                    containerColor = Color(0xFF00FFFF),
                                    contentColor = Color.Black,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.Default.AutoFixHigh, contentDescription = "Reward Undo")
                                }
                            }
                            Text(
                                text = "UNDO",
                                color = Color(0xFF00FFFF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
            AdBanner(onPremiumClick = { showPremiumDialog = true })
        }
    }

    if (showRulesDialog) RulesDialog(onDismiss = { showRulesDialog = false })
    if (showRestartDialog) RestartConfirmDialog(onConfirm = { performRestart() }, onDismiss = { showRestartDialog = false })
    if (showPremiumDialog) PremiumDialog(onDismiss = { showPremiumDialog = false }, onSubscribeClick = { activity -> billingManager.launchPurchaseFlow(activity); showPremiumDialog = false })
    
    if (unlockedAchievementName != null) {
        AchievementUnlockedDialog(achievementName = unlockedAchievementName!!, onDismiss = { unlockedAchievementName = null })
    }

    if (showLevelCompleteDialog) {
        LevelCompleteDialog(
            level = gameState.careerLevel ?: 0,
            onNextLevel = { goToNextLevel() },
            onBackToMap = onBackToMenu
        )
    }

    if (showFallbackInterstitial) {
        FallbackInterstitialDialog(onDismiss = { showFallbackInterstitial = false; performRestart() }, onPremiumClick = { showFallbackInterstitial = false; showPremiumDialog = true })
    }
}

@Composable
fun LevelCompleteDialog(level: Int, onNextLevel: () -> Unit, onBackToMap: () -> Unit) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1A1A2E),
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Nível $level Concluído!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Você dominou este desafio. Pronto para o próximo?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onNextLevel,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.NavigateNext, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("PRÓXIMO NÍVEL", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onBackToMap,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("VOLTAR PARA O MAPA", color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun RestartConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A2E),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray,
        title = { Text(text = stringResource(R.string.restart_title), fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = { Text(text = stringResource(R.string.restart_desc), fontSize = 16.sp) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.restart_confirm), color = Color(0xFFE91E63), fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = Color.Gray) } }
    )
}

@Composable
fun AchievementUnlockedDialog(achievementName: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A2E),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray,
        icon = {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(48.dp))
        },
        title = {
            Text(
                text = "Conquista Desbloqueada!",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = achievementName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD700),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Você desbloqueou um novo estilo de tabuleiro exclusivo!",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Incrível!", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun GameHeader(
    gameState: GameState,
    gameMode: GameMode,
    scrollState: ScrollState,
    uiColors: UiThemeColors,
    onNewGame: () -> Unit,
    onShowRules: () -> Unit,
    onBackToMenu: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth(), color = uiColors.headerBg, shadowElevation = 0.dp) {
        Column(modifier = Modifier.statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onBackToMenu, contentPadding = PaddingValues(horizontal = 12.dp), modifier = Modifier.height(36.dp), colors = ButtonDefaults.buttonColors(containerColor = if (uiColors.isDark) Color(0xFF303050).copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.2f))) { Text(stringResource(R.string.menu), fontSize = 14.sp, color = uiColors.text) }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = onShowRules, contentPadding = PaddingValues(horizontal = 12.dp), modifier = Modifier.height(36.dp), colors = ButtonDefaults.filledTonalButtonColors(containerColor = if (uiColors.isDark) Color(0xFF303050).copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.2f))) { Text(stringResource(R.string.rules), fontSize = 14.sp, color = uiColors.text) }
                    FilledTonalButton(onClick = onNewGame, contentPadding = PaddingValues(horizontal = 12.dp), modifier = Modifier.height(36.dp), colors = ButtonDefaults.filledTonalButtonColors(containerColor = if (uiColors.isDark) Color(0xFF303050).copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.2f))) { Text(stringResource(R.string.new_game), fontSize = 14.sp, color = uiColors.text) }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState).padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                gameState.playerNames.forEachIndexed { index, name ->
                    val isCpu = gameMode == GameMode.VS_CPU && index == 1
                    val playerColor = getStylePlayerColor(gameState.boardStyle, index + 1, gameMode == GameMode.VS_CPU)
                    PlayerScoreCompact(playerName = if (isCpu) "CPU" else name, score = gameState.playerScores[index], color = playerColor, isActive = gameState.currentPlayer == index + 1, uiColors = uiColors)
                }
            }
            if (gameState.careerLevel != null) {
                Text(
                    text = "Level ${gameState.careerLevel}",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = uiColors.text.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PlayerScoreCompact(playerName: String, score: Int, color: Color, isActive: Boolean, uiColors: UiThemeColors) {
    Surface(modifier = Modifier.width(90.dp), shape = MaterialTheme.shapes.small, color = if (isActive) color.copy(alpha = 0.2f) else Color.Transparent, border = if (isActive) BorderStroke(2.dp, color) else BorderStroke(1.dp, if (uiColors.isDark) Color.Gray.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(text = playerName, fontSize = 14.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, color = if (isActive) color else if (uiColors.isDark) Color.LightGray else Color.Gray, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = score.toString(), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun RulesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A2E),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray,
        title = { Text(text = stringResource(R.string.rules), fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = { Column(modifier = Modifier.verticalScroll(rememberScrollState())) { RuleItem(title = stringResource(R.string.objective_title), description = stringResource(R.string.objective_desc)); Spacer(modifier = Modifier.height(8.dp)); RuleItem(title = stringResource(R.string.turns_title), description = stringResource(R.string.turns_desc)); Spacer(modifier = Modifier.height(8.dp)); RuleItem(title = stringResource(R.string.prohibited_title), description = stringResource(R.string.prohibited_desc)) } },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.understood), color = Color(0xFF00FFFF)) } }
    )
}

@Composable
fun RuleItem(title: String, description: String) {
    Column {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF00FFFF))
        Text(text = description, fontSize = 13.sp, color = Color.LightGray)
    }
}

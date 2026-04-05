package com.montanhajr.pointgame.ui.screens

import android.app.Activity
import android.media.MediaPlayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.montanhajr.pointgame.R
import com.montanhajr.pointgame.logic.BillingManager
import com.montanhajr.pointgame.logic.GameState
import com.montanhajr.pointgame.logic.StatisticsManager
import com.montanhajr.pointgame.models.*
import com.montanhajr.pointgame.ui.components.AdBanner
import com.montanhajr.pointgame.ui.components.AdManager
import com.montanhajr.pointgame.ui.components.BoardBackground
import com.montanhajr.pointgame.ui.components.GameBoard
import com.montanhajr.pointgame.ui.components.PremiumDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DotsGameScreen(
    gameMode: GameMode,
    difficulty: Difficulty?,
    numPlayers: Int,
    playerNames: List<String>? = null,
    boardStyle: BoardStyle = BoardStyle.GALAXY,
    onBackToMenu: () -> Unit
) {
    val context = LocalContext.current
    val billingManager = remember { BillingManager(context) }
    val statsManager = remember { StatisticsManager(context) }
    
    var gameState by remember {
        mutableStateOf(
            GameState.createNew(
                isCpuGame = gameMode == GameMode.VS_CPU,
                difficulty = difficulty ?: Difficulty.MEDIUM,
                numPlayers = numPlayers,
                playerNames = playerNames,
                boardStyle = boardStyle
            )
        )
    }
    
    var showRulesDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }
    var restartCountWithoutAd by remember { mutableStateOf(0) }
    var matchStartTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }
    val cardWidthPx = with(density) { 96.dp.roundToPx() }

    val connectionSound = remember { MediaPlayer.create(context, R.raw.connection) }
    val playerPointSound = remember { MediaPlayer.create(context, R.raw.player_point) }
    val cpuPointSound = remember { MediaPlayer.create(context, R.raw.cpu_point) }

    val uiColors = remember(boardStyle) { getStyleUiColors(boardStyle) }

    LaunchedEffect(Unit) {
        AdManager.loadInterstitial(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            connectionSound?.release()
            playerPointSound?.release()
            cpuPointSound?.release()
        }
    }

    // Record stats when game ends
    LaunchedEffect(gameState.gameOver) {
        if (gameState.gameOver) {
            val duration = System.currentTimeMillis() - matchStartTime
            
            // OTIMIZAÇÃO: Record only triangles formed by HUMAN players (exclude CPU at index 1 in VS_CPU mode)
            val humanTriangles = if (gameMode == GameMode.VS_CPU) {
                gameState.playerScores[0] // Only P1
            } else {
                gameState.playerScores.sum() // All humans in multiplayer
            }
            statsManager.addTriangles(humanTriangles)
            
            // Record match result for CPU games
            if (gameMode == GameMode.VS_CPU) {
                val player1Score = gameState.playerScores[0]
                val cpuScore = gameState.playerScores[1]
                val result = when {
                    player1Score > cpuScore -> StatisticsManager.MatchResult.WIN
                    player1Score < cpuScore -> StatisticsManager.MatchResult.LOSS
                    else -> StatisticsManager.MatchResult.DRAW
                }
                statsManager.recordMatch(difficulty, result, duration)
            } else {
                // For non-CPU games, just record the time and a generic match count
                statsManager.recordMatch(null, StatisticsManager.MatchResult.DRAW, duration)
            }
        }
    }

    LaunchedEffect(gameState.currentPlayer, gameState.gameOver) {
        if (gameState.gameOver) delay(500)
        val targetScroll = ((gameState.currentPlayer - 1) * cardWidthPx) + (cardWidthPx / 2) - (screenWidthPx / 2)
        scrollState.animateScrollTo(targetScroll.coerceAtLeast(0))
    }

    LaunchedEffect(gameState.currentPlayer, gameState.gameOver, gameState.lines.size) {
        if (gameMode == GameMode.VS_CPU &&
            gameState.currentPlayer == 2 &&
            !gameState.gameOver) {
            delay(800)
            val cpuMove = gameState.getCpuMove()
            if (cpuMove != null) {
                val previousTriangles = gameState.triangles.size
                gameState = gameState.drawLine(cpuMove.first, cpuMove.second)

                scope.launch {
                    try {
                        if (gameState.triangles.size > previousTriangles) {
                            cpuPointSound?.start()
                        } else {
                            connectionSound?.start()
                        }
                    } catch (e: Exception) {}
                }
            }
        }
    }

    fun performRestart() {
        gameState = GameState.createNew(
            isCpuGame = gameMode == GameMode.VS_CPU,
            difficulty = difficulty ?: Difficulty.MEDIUM,
            numPlayers = numPlayers,
            playerNames = playerNames,
            boardStyle = boardStyle
        )
        matchStartTime = System.currentTimeMillis() // Reset timer
        showRestartDialog = false
    }

    fun handleRestartLogic() {
        val totalPoints = gameState.points.size
        val totalPossibleLines = (totalPoints * (totalPoints - 1)) / 2
        val remainingMoves = totalPossibleLines - gameState.lines.size
        
        val shouldShowAd = when {
            gameState.gameOver -> true
            remainingMoves < 3 -> true
            restartCountWithoutAd >= 2 -> {
                restartCountWithoutAd = 0
                true
            }
            else -> {
                restartCountWithoutAd++
                false
            }
        }

        val activity = context as? Activity
        
        if (shouldShowAd && activity != null) {
            AdManager.showInterstitial(activity) {
                performRestart()
            }
        } else {
            performRestart()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        BoardBackground(style = boardStyle)

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            GameHeader(
                gameState = gameState,
                gameMode = gameMode,
                scrollState = scrollState,
                uiColors = uiColors,
                onNewGame = {
                    if (gameState.gameOver) {
                        handleRestartLogic()
                    } else {
                        showRestartDialog = true
                    }
                },
                onShowRules = { showRulesDialog = true },
                onBackToMenu = onBackToMenu
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                GameBoard(
                    gameState = gameState,
                    enabled = gameMode != GameMode.VS_CPU || gameState.currentPlayer == 1,
                    onLineDrawn = { startId, endId ->
                        val previousTriangles = gameState.triangles.size
                        gameState = gameState.drawLine(startId, endId)

                        scope.launch {
                            try {
                                if (gameState.triangles.size > previousTriangles) {
                                    playerPointSound?.start()
                                } else {
                                    connectionSound?.start()
                                }
                            } catch (e: Exception) {}
                        }
                    }
                )
            }

            AdBanner(onPremiumClick = { showPremiumDialog = true })
        }
    }

    if (showRulesDialog) {
        RulesDialog(onDismiss = { showRulesDialog = false })
    }

    if (showRestartDialog) {
        RestartConfirmDialog(
            onConfirm = { handleRestartLogic() },
            onDismiss = { showRestartDialog = false }
        )
    }

    if (showPremiumDialog) {
        PremiumDialog(
            onDismiss = { showPremiumDialog = false },
            onSubscribeClick = { activity ->
                billingManager.launchPurchaseFlow(activity)
                showPremiumDialog = false
            }
        )
    }
}

@Composable
fun RestartConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A2E),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray,
        title = {
            Text(
                text = stringResource(R.string.restart_title),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                text = stringResource(R.string.restart_desc),
                fontSize = 16.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.restart_confirm), color = Color(0xFFE91E63), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = Color.Gray)
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = uiColors.headerBg,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBackToMenu,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (uiColors.isDark) Color(0xFF303050).copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.2f))
                ) {
                    Text(stringResource(R.string.menu), fontSize = 14.sp, color = uiColors.text)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onShowRules,
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = if (uiColors.isDark) Color(0xFF303050).copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.2f))
                    ) {
                        Text(stringResource(R.string.rules), fontSize = 14.sp, color = uiColors.text)
                    }
                    FilledTonalButton(
                        onClick = onNewGame,
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = if (uiColors.isDark) Color(0xFF303050).copy(alpha = 0.6f) else Color.Gray.copy(alpha = 0.2f))
                    ) {
                        Text(stringResource(R.string.new_game), fontSize = 14.sp, color = uiColors.text)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                gameState.playerNames.forEachIndexed { index, name ->
                    val isCpu = gameMode == GameMode.VS_CPU && index == 1
                    val playerColor = getStylePlayerColor(gameState.boardStyle, index + 1, gameMode == GameMode.VS_CPU)
                    
                    PlayerScoreCompact(
                        playerName = if (isCpu) "CPU" else name,
                        score = gameState.playerScores[index],
                        color = playerColor,
                        isActive = gameState.currentPlayer == index + 1,
                        uiColors = uiColors
                    )
                }
            }

            if (gameState.gameOver) {
                val winnerMessage = when {
                    (gameState.playerScores.maxOrNull() ?: 0) == 0 -> stringResource(R.string.draw)
                    gameState.isCpuGame && gameState.playerScores.indexOf(gameState.playerScores.maxOrNull()) == 1 -> stringResource(R.string.cpu_won)
                    else -> {
                        val winnerIndex = gameState.playerScores.indexOf(gameState.playerScores.maxOrNull())
                        stringResource(R.string.player_won, gameState.playerNames[winnerIndex])
                    }
                }
                Text(
                    text = winnerMessage,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (uiColors.isDark) Color(0xFF00FF00) else Color(0xFF008000),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun PlayerScoreCompact(playerName: String, score: Int, color: Color, isActive: Boolean, uiColors: UiThemeColors) {
    Surface(
        modifier = Modifier.width(90.dp),
        shape = MaterialTheme.shapes.small,
        color = if (isActive) color.copy(alpha = 0.2f) else Color.Transparent,
        border = if (isActive) BorderStroke(2.dp, color) else BorderStroke(1.dp, if (uiColors.isDark) Color.Gray.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = playerName,
                fontSize = 14.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (isActive) color else if (uiColors.isDark) Color.LightGray else Color.Gray,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = score.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
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
        title = {
            Text(
                text = stringResource(R.string.rules),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                RuleItem(title = stringResource(R.string.objective_title), description = stringResource(R.string.objective_desc))
                Spacer(modifier = Modifier.height(8.dp))
                RuleItem(title = stringResource(R.string.turns_title), description = stringResource(R.string.turns_desc))
                Spacer(modifier = Modifier.height(8.dp))
                RuleItem(title = stringResource(R.string.prohibited_title), description = stringResource(R.string.prohibited_desc))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { 
                Text(stringResource(R.string.understood), color = Color(0xFF00FFFF)) 
            }
        }
    )
}

@Composable
fun RuleItem(title: String, description: String) {
    Column {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF00FFFF))
        Text(text = description, fontSize = 13.sp, color = Color.LightGray)
    }
}

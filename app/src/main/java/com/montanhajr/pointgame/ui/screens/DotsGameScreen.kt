package com.montanhajr.pointgame.ui.screens

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
import com.montanhajr.pointgame.logic.GameState
import com.montanhajr.pointgame.models.Difficulty
import com.montanhajr.pointgame.models.GameMode
import com.montanhajr.pointgame.models.PlayerColors
import com.montanhajr.pointgame.ui.components.AdBanner
import com.montanhajr.pointgame.ui.components.GameBoard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DotsGameScreen(
    gameMode: GameMode,
    difficulty: Difficulty?,
    numPlayers: Int,
    playerNames: List<String>? = null,
    onBackToMenu: () -> Unit
) {
    var gameState by remember {
        mutableStateOf(
            GameState.createNew(
                isCpuGame = gameMode == GameMode.VS_CPU,
                difficulty = difficulty ?: Difficulty.MEDIUM,
                numPlayers = numPlayers,
                playerNames = playerNames
            )
        )
    }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }
    val cardWidthPx = with(density) { 96.dp.roundToPx() }

    val connectionSound = remember { MediaPlayer.create(context, R.raw.connection) }
    val playerPointSound = remember { MediaPlayer.create(context, R.raw.player_point) }
    val cpuPointSound = remember { MediaPlayer.create(context, R.raw.cpu_point) }

    DisposableEffect(Unit) {
        onDispose {
            connectionSound?.release()
            playerPointSound?.release()
            cpuPointSound?.release()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF110d1e))
            .statusBarsPadding()
    ) {
        GameHeader(
            gameState = gameState,
            gameMode = gameMode,
            scrollState = scrollState,
            onNewGame = {
                showRestartDialog = true
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

        // Banner fixo no rodapé
        AdBanner()
    }

    if (showRulesDialog) {
        RulesDialog(onDismiss = { showRulesDialog = false })
    }

    if (showRestartDialog) {
        RestartConfirmDialog(
            onConfirm = {
                gameState = GameState.createNew(
                    isCpuGame = gameMode == GameMode.VS_CPU,
                    difficulty = difficulty ?: Difficulty.MEDIUM,
                    numPlayers = numPlayers,
                    playerNames = playerNames
                )
                showRestartDialog = false
            },
            onDismiss = { showRestartDialog = false }
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
    onNewGame: () -> Unit,
    onShowRules: () -> Unit,
    onBackToMenu: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A2E),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF303050))
                ) {
                    Text(stringResource(R.string.menu), fontSize = 14.sp, color = Color.White)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = onShowRules,
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF303050))
                    ) {
                        Text(stringResource(R.string.rules), fontSize = 14.sp, color = Color.White)
                    }
                    FilledTonalButton(
                        onClick = onNewGame,
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFF303050))
                    ) {
                        Text(stringResource(R.string.new_game), fontSize = 14.sp, color = Color.White)
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
                    val playerColor = if (isCpu) Color(0xFFE0E0E0) else PlayerColors[index % PlayerColors.size]
                    
                    PlayerScoreCompact(
                        playerName = if (isCpu) "CPU" else name,
                        score = gameState.playerScores[index],
                        color = playerColor,
                        isActive = gameState.currentPlayer == index + 1
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
                    color = Color(0xFF00FF00),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun PlayerScoreCompact(playerName: String, score: Int, color: Color, isActive: Boolean) {
    Surface(
        modifier = Modifier.width(90.dp),
        shape = MaterialTheme.shapes.small,
        color = if (isActive) color.copy(alpha = 0.25f) else Color.Transparent,
        border = if (isActive) BorderStroke(2.dp, color) else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
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
                color = if (isActive) color else Color.LightGray,
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

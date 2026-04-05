package com.montanhajr.pointgame.ui.screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.montanhajr.pointgame.R
import com.montanhajr.pointgame.logic.BillingManager
import com.montanhajr.pointgame.models.BoardStyle
import com.montanhajr.pointgame.models.Difficulty
import com.montanhajr.pointgame.models.GameMode
import com.montanhajr.pointgame.models.PlayerColors
import com.montanhajr.pointgame.ui.components.GalaxyBackground
import com.montanhajr.pointgame.ui.components.PremiumDialog

@Composable
fun GameModeScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE) }
    val billingManager = remember { BillingManager(context) }
    val isPremium by billingManager.isPremium.collectAsState()
    
    var gameMode by remember { mutableStateOf<GameMode?>(null) }
    var difficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var numPlayers by remember { mutableIntStateOf(3) }
    var playerNames by remember { mutableStateOf<List<String>?>(null) }
    
    var selectedBoardStyle by remember { 
        val savedStyleName = prefs.getString("last_board_style", BoardStyle.GALAXY.name)
        mutableStateOf(try {
            BoardStyle.valueOf(savedStyleName ?: BoardStyle.GALAXY.name)
        } catch (e: Exception) {
            BoardStyle.GALAXY
        })
    }
    
    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showMultiplayerDialog by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }
    var showBoardStyleScreen by remember { mutableStateOf(false) }
    var startGame by remember { mutableStateOf(false) }

    if (showBoardStyleScreen) {
        BoardStyleScreen(
            currentStyle = selectedBoardStyle,
            isPremium = isPremium,
            onStyleSelected = { style ->
                selectedBoardStyle = style
                prefs.edit().putString("last_board_style", style.name).apply()
            },
            onSubscribeClick = { activity ->
                billingManager.launchPurchaseFlow(activity)
            },
            onBack = { showBoardStyleScreen = false }
        )
        return
    }

    Scaffold(
        floatingActionButton = {
            if (gameMode == null && !isPremium) {
                ExtendedFloatingActionButton(
                    onClick = { showPremiumDialog = true },
                    containerColor = Color(0xFFFFD700),
                    contentColor = Color(0xFF1A1A2E),
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    text = { Text(stringResource(R.string.premium_button), fontWeight = FontWeight.Bold) },
                    modifier = Modifier.padding(bottom = 16.dp, end = 8.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                gameMode == null -> {
                    GameModeSelection(
                        onTwoPlayers = { 
                            gameMode = GameMode.TWO_PLAYERS
                            playerNames = listOf("Player 1", "Player 2")
                            startGame = true
                         },
                        onVsCpu = { showDifficultyDialog = true },
                        onMultiplayer = { showMultiplayerDialog = true },
                        onOpenBoardStyles = { showBoardStyleScreen = true }
                    )
                }
                gameMode == GameMode.TWO_PLAYERS && startGame -> {
                    DotsGameScreen(
                        gameMode = GameMode.TWO_PLAYERS,
                        difficulty = null,
                        numPlayers = 2,
                        playerNames = playerNames,
                        boardStyle = selectedBoardStyle,
                        onBackToMenu = { 
                            gameMode = null
                            startGame = false
                            playerNames = null
                         }
                    )
                }
                gameMode == GameMode.VS_CPU && startGame -> {
                    DotsGameScreen(
                        gameMode = GameMode.VS_CPU,
                        difficulty = difficulty,
                        numPlayers = 2,
                        boardStyle = selectedBoardStyle,
                        onBackToMenu = {
                            gameMode = null
                            startGame = false
                        }
                    )
                }
                gameMode == GameMode.MULTIPLAYER && startGame -> {
                    DotsGameScreen(
                        gameMode = GameMode.MULTIPLAYER,
                        difficulty = null,
                        numPlayers = numPlayers,
                        playerNames = playerNames,
                        boardStyle = selectedBoardStyle,
                        onBackToMenu = {
                            gameMode = null
                            startGame = false
                            playerNames = null
                        }
                    )
                }
            }
        }
    }

    if (showDifficultyDialog) {
        DifficultyDialog(
            selectedDifficulty = difficulty,
            onDifficultyChanged = { difficulty = it },
            onStartGame = {
                gameMode = GameMode.VS_CPU
                startGame = true
                showDifficultyDialog = false
            },
            onDismiss = { showDifficultyDialog = false }
        )
    }

    if (showMultiplayerDialog) {
        MultiplayerDialog(
            selectedCount = numPlayers,
            onCountChanged = { numPlayers = it },
            onStartGame = { names ->
                playerNames = names
                gameMode = GameMode.MULTIPLAYER
                startGame = true
                showMultiplayerDialog = false
            },
            onDismiss = { showMultiplayerDialog = false }
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
fun GameModeSelection(
    onTwoPlayers: () -> Unit, 
    onVsCpu: () -> Unit, 
    onMultiplayer: () -> Unit,
    onOpenBoardStyles: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A))
    ) {
        GalaxyBackground()
        
        IconButton(
            onClick = onOpenBoardStyles,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
                .size(48.dp)
                .background(Color(0xFF303050).copy(alpha = 0.8f), MaterialTheme.shapes.medium)
        ) {
            Icon(
                imageVector = Icons.Default.Brush,
                contentDescription = "Board Styles",
                tint = Color.White
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            Text(
                text = stringResource(R.string.game_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.choose_game_mode),
                fontSize = 18.sp,
                color = Color.LightGray.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            MenuButton(
                text = stringResource(R.string.two_players),
                color = Color(0xFF2196F3),
                onClick = onTwoPlayers
            )

            Spacer(modifier = Modifier.height(24.dp))

            MenuButton(
                text = stringResource(R.string.multiplayer),
                color = Color(0xFF9C27B0),
                onClick = onMultiplayer
            )

            Spacer(modifier = Modifier.height(24.dp))

            MenuButton(
                text = stringResource(R.string.vs_cpu),
                color = Color(0xFFE91E63),
                onClick = onVsCpu
            )
            
            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}

@Composable
fun MenuButton(text: String, color: Color, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(2.dp, color.copy(alpha = 0.6f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White,
            containerColor = color.copy(alpha = 0.15f)
        )
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MultiplayerDialog(
    selectedCount: Int,
    onCountChanged: (Int) -> Unit,
    onStartGame: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var names by remember(selectedCount) { 
        mutableStateOf(List(selectedCount) { "" }) 
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A2E),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray,
        title = { 
            Text(
                text = if (step == 1) stringResource(R.string.how_many_players) else stringResource(R.string.player_names), 
                fontWeight = FontWeight.Bold 
            ) 
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (step == 1) {
                    Text(
                        text = stringResource(R.string.num_players, selectedCount),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00FFFF)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Slider(
                        value = selectedCount.toFloat(),
                        onValueChange = { onCountChanged(it.toInt()) },
                        valueRange = 3f..10f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00FFFF),
                            activeTrackColor = Color(0xFF00FFFF)
                        )
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (i in 0 until selectedCount) {
                            val placeholderChar = ('A'.toInt() + i).toChar().toString()
                            val placeholder = stringResource(R.string.player_label, placeholderChar)
                            OutlinedTextField(
                                value = names[i],
                                onValueChange = { newName ->
                                    val newList = names.toMutableList()
                                    newList[i] = newName
                                    names = newList
                                },
                                label = { Text(placeholder) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PlayerColors[i % PlayerColors.size],
                                    unfocusedBorderColor = PlayerColors[i % PlayerColors.size].copy(alpha = 0.5f),
                                    focusedLabelColor = PlayerColors[i % PlayerColors.size],
                                    unfocusedLabelColor = Color.Gray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step == 1) {
                        step = 2
                    } else {
                        val finalNames = names.mapIndexed { i, name ->
                            val placeholderChar = ('A'.toInt() + i).toChar().toString()
                            name.ifBlank { "Player $placeholderChar" }
                        }
                        onStartGame(finalNames)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(if (step == 1) stringResource(R.string.confirm) else stringResource(R.string.start_game), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (step == 2) step = 1 else onDismiss()
            }) {
                Text(if (step == 2) stringResource(R.string.back) else stringResource(R.string.cancel), color = Color.Gray)
            }
        }
    )
}

@Composable
fun DifficultyDialog(
    selectedDifficulty: Difficulty,
    onDifficultyChanged: (Difficulty) -> Unit,
    onStartGame: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A2E),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray,
        title = {
            Text(
                text = stringResource(R.string.choose_difficulty),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DifficultyOption(
                    title = stringResource(R.string.easy_title),
                    description = stringResource(R.string.easy_desc),
                    isSelected = selectedDifficulty == Difficulty.EASY,
                    onClick = { onDifficultyChanged(Difficulty.EASY) }
                )
                DifficultyOption(
                    title = stringResource(R.string.medium_title),
                    description = stringResource(R.string.medium_desc),
                    isSelected = selectedDifficulty == Difficulty.MEDIUM,
                    onClick = { onDifficultyChanged(Difficulty.MEDIUM) }
                )
                DifficultyOption(
                    title = stringResource(R.string.hard_title),
                    description = stringResource(R.string.hard_desc),
                    isSelected = selectedDifficulty == Difficulty.HARD,
                    onClick = { onDifficultyChanged(Difficulty.HARD) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onStartGame,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text(stringResource(R.string.play), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = Color.Gray)
            }
        }
    )
}

@Composable
fun DifficultyOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF00FFFF).copy(alpha = 0.1f) else Color(0xFF303050).copy(alpha = 0.5f)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FFFF)) else null
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isSelected) Color(0xFF00FFFF) else Color.White
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.LightGray
            )
        }
    }
}

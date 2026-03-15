package com.montanhajr.pointgame.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.montanhajr.pointgame.models.Difficulty
import com.montanhajr.pointgame.models.GameMode
import com.montanhajr.pointgame.models.PlayerColors
import com.montanhajr.pointgame.ui.components.GalaxyBackground

@Composable
fun GameModeScreen() {
    var gameMode by remember { mutableStateOf<GameMode?>(null) }
    var difficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var numPlayers by remember { mutableIntStateOf(3) }
    var playerNames by remember { mutableStateOf<List<String>?>(null) }
    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showMultiplayerDialog by remember { mutableStateOf(false) }
    var startGame by remember { mutableStateOf(false) }

    when {
        gameMode == null -> {
            GameModeSelection(
                onTwoPlayers = { 
                    gameMode = GameMode.TWO_PLAYERS
                    playerNames = listOf("Jogador 1", "Jogador 2")
                    startGame = true
                 },
                onVsCpu = { showDifficultyDialog = true },
                onMultiplayer = { showMultiplayerDialog = true }
            )
        }
        gameMode == GameMode.TWO_PLAYERS && startGame -> {
            DotsGameScreen(
                gameMode = GameMode.TWO_PLAYERS,
                difficulty = null,
                numPlayers = 2,
                playerNames = playerNames,
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
                onBackToMenu = {
                    gameMode = null
                    startGame = false
                    playerNames = null
                }
            )
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
}

@Composable
fun GameModeSelection(onTwoPlayers: () -> Unit, onVsCpu: () -> Unit, onMultiplayer: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F1A)), // Fundo escuro como a galáxia
        contentAlignment = Alignment.Center
    ) {
        GalaxyBackground()
        
        // Removi o Card (borda e fundo opaco) para um visual mais limpo e neon
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "CONNECT THE DOTS!",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                // Adicionando um leve efeito neon no título (simulado via cor vibrante)
            )

            Text(
                text = "Escolha o modo de jogo",
                fontSize = 18.sp,
                color = Color.LightGray.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botões com cores vibrantes e sem fundos pesados
            MenuButton(
                text = "👥 2 Jogadores",
                color = Color(0xFF2196F3),
                onClick = onTwoPlayers
            )

            MenuButton(
                text = "👨‍👩‍👧‍👦 Multijogadores (3-10)",
                color = Color(0xFF9C27B0),
                onClick = onMultiplayer
            )

            MenuButton(
                text = "🤖 Jogar vs CPU",
                color = Color(0xFFE91E63),
                onClick = onVsCpu
            )
        }
    }
}

@Composable
fun MenuButton(text: String, color: Color, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
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
                text = if (step == 1) "Quantos Jogadores?" else "Nomes dos Jogadores", 
                fontWeight = FontWeight.Bold 
            ) 
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (step == 1) {
                    Text(
                        text = "$selectedCount Jogadores",
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
                            val placeholder = "Jogador ${( 'A'.toInt() + i).toChar()}"
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
                            name.ifBlank { "Jogador ${('A'.toInt() + i).toChar()}" }
                        }
                        onStartGame(finalNames)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text(if (step == 1) "Confirmar" else "Iniciar Partida", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = {
                if (step == 2) step = 1 else onDismiss()
            }) {
                Text(if (step == 2) "Voltar" else "Cancelar", color = Color.Gray)
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
                text = "Escolha a Dificuldade",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DifficultyOption(
                    title = "😊 Fácil",
                    description = "CPU joga aleatoriamente",
                    isSelected = selectedDifficulty == Difficulty.EASY,
                    onClick = { onDifficultyChanged(Difficulty.EASY) }
                )
                DifficultyOption(
                    title = "🤔 Médio",
                    description = "CPU tenta fechar triângulos",
                    isSelected = selectedDifficulty == Difficulty.MEDIUM,
                    onClick = { onDifficultyChanged(Difficulty.MEDIUM) }
                )
                DifficultyOption(
                    title = "🧠 Difícil",
                    description = "CPU joga estrategicamente",
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
                    Text("Jogar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
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
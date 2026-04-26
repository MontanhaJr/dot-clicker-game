package com.montanhajr.pointgame.ui.screens

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.montanhajr.pointgame.R
import com.montanhajr.pointgame.logic.BillingManager
import com.montanhajr.pointgame.logic.CareerManager
import com.montanhajr.pointgame.logic.StatisticsManager
import com.montanhajr.pointgame.models.BoardStyle
import com.montanhajr.pointgame.models.Difficulty
import com.montanhajr.pointgame.models.GameMode
import com.montanhajr.pointgame.ui.components.AchievementDialog
import com.montanhajr.pointgame.ui.components.GalaxyBackground
import com.montanhajr.pointgame.ui.components.PremiumDialog
import com.montanhajr.pointgame.ui.theme.*

@Composable
fun GameModeScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE) }
    val billingManager = remember { BillingManager(context) }
    val statsManager = remember { StatisticsManager(context) }
    val careerManager = remember { CareerManager(context) }
    val isPremium by billingManager.isPremium.collectAsState()
    
    var gameMode by remember { mutableStateOf<GameMode?>(null) }
    var difficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var numPlayers by remember { mutableIntStateOf(2) }
    var playerNames by remember { mutableStateOf<List<String>?>(null) }
    var careerLevel by remember { mutableStateOf<Int?>(null) }
    
    var selectedBoardStyle by remember { 
        val savedStyleName = prefs.getString("last_board_style", BoardStyle.DEFAULT_POP.name)
        mutableStateOf(try {
            BoardStyle.valueOf(savedStyleName ?: BoardStyle.DEFAULT_POP.name)
        } catch (e: Exception) {
            BoardStyle.DEFAULT_POP
        })
    }
    
    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showMultiplayerDialog by remember { mutableStateOf(false) }
    var showPremiumDialog by remember { mutableStateOf(false) }
    var showBoardStyleScreen by remember { mutableStateOf(false) }
    var showStatsScreen by remember { mutableStateOf(false) }
    var showAchievementDialog by remember { mutableStateOf(false) }
    var showCareerScreen by remember { mutableStateOf(false) }
    var startGame by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            showStatsScreen -> {
                StatsScreen(onBack = { showStatsScreen = false })
            }
            showCareerScreen -> {
                CareerScreen(
                    onLevelSelected = { level ->
                        careerLevel = level
                        difficulty = careerManager.getDifficultyForLevel(level)
                        gameMode = GameMode.VS_CPU
                        numPlayers = 2
                        startGame = true
                        showCareerScreen = false
                    },
                    onBack = { showCareerScreen = false }
                )
            }
            showBoardStyleScreen -> {
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
            }
            gameMode != null && startGame -> {
                DotsGameScreen(
                    gameMode = gameMode!!,
                    difficulty = difficulty,
                    numPlayers = numPlayers,
                    playerNames = playerNames,
                    boardStyle = selectedBoardStyle,
                    careerLevel = careerLevel,
                    onBackToMenu = { 
                        if (careerLevel != null) {
                            showCareerScreen = true
                        }
                        gameMode = null
                        startGame = false
                        playerNames = null
                        careerLevel = null
                    }
                )
            }
            else -> {
                Scaffold(
                    containerColor = Color.Transparent,
                    floatingActionButton = {
                        if (!isPremium) {
                            ExtendedFloatingActionButton(
                                onClick = { showPremiumDialog = true },
                                containerColor = PopYellow,
                                contentColor = PopDarkBlue,
                                icon = { Icon(Icons.Default.Star, contentDescription = null) },
                                text = { Text(stringResource(R.string.premium_button), fontWeight = FontWeight.ExtraBold) },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.shadow(8.dp, RoundedCornerShape(20.dp))
                            )
                        }
                    }
                ) { _ ->
                    GameModeSelection(
                        onCareerMode = { showCareerScreen = true },
                        onTwoPlayers = { 
                            gameMode = GameMode.TWO_PLAYERS
                            numPlayers = 2
                            playerNames = listOf("Player 1", "Player 2")
                            startGame = true
                         },
                        onVsCpu = { showDifficultyDialog = true },
                        onMultiplayer = { 
                            numPlayers = 3
                            showMultiplayerDialog = true 
                        },
                        onOpenBoardStyles = { showBoardStyleScreen = true },
                        onOpenStats = { showStatsScreen = true },
                        onOpenAchievements = { showAchievementDialog = true }
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
                numPlayers = 2
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

    if (showAchievementDialog) {
        AchievementDialog(
            achievements = statsManager.getAchievements(),
            onDismiss = { showAchievementDialog = false }
        )
    }
}

@Composable
fun GameModeSelection(
    onCareerMode: () -> Unit,
    onTwoPlayers: () -> Unit, 
    onVsCpu: () -> Unit, 
    onMultiplayer: () -> Unit,
    onOpenBoardStyles: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenAchievements: () -> Unit
) {
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // App Background Image
        Image(
            painter = painterResource(id = R.drawable.app_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Mantemos o GalaxyBackground como uma sobreposição leve se desejar, 
        // ou podemos removê-lo. Vou mantê-lo com alpha baixo para dar um efeito Pop!
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo Dot Pop! Oficial
            Image(
                painter = painterResource(id = R.drawable.dotpop_logo_no_bg),
                contentDescription = "Dot Pop! Logo",
                modifier = Modifier
                    .size(240.dp)
                    .padding(top = 48.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(32.dp))

            PopMenuButton(
                text = stringResource(R.string.journey_mode),
                mainColor = PopYellow,
                secondaryColor = PopOrange,
                onClick = onCareerMode
            )

            Spacer(modifier = Modifier.height(16.dp))

            PopMenuButton(
                text = stringResource(R.string.two_players),
                mainColor = PopBlue,
                secondaryColor = PopCyan,
                onClick = onTwoPlayers
            )

            Spacer(modifier = Modifier.height(16.dp))

            PopMenuButton(
                text = stringResource(R.string.multiplayer),
                mainColor = PopGreen,
                secondaryColor = Color(0xFF1B5E20),
                onClick = onMultiplayer
            )

            Spacer(modifier = Modifier.height(16.dp))

            PopMenuButton(
                text = stringResource(R.string.training_mode),
                mainColor = PopRed,
                secondaryColor = Color(0xFF880E4F),
                onClick = onVsCpu
            )
            
            Spacer(modifier = Modifier.height(100.dp))
        }

        // Botoes fixos no topo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PopIconButton(icon = Icons.Default.Timeline, color = PopCyan, onClick = onOpenStats)
            PopIconButton(icon = Icons.Default.EmojiEvents, color = PopYellow, onClick = onOpenAchievements, size = 56.dp)
            PopIconButton(icon = Icons.Default.Brush, color = PopCyan, onClick = onOpenBoardStyles)
        }
    }
}

@Composable
fun PopMenuButton(text: String, mainColor: Color, secondaryColor: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .border(3.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = mainColor),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(mainColor, secondaryColor)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PopWhite,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PopIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit, size: androidx.compose.ui.unit.Dp = 48.dp) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = color,
        modifier = Modifier
            .size(size)
            .shadow(6.dp, CircleShape)
            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PopDarkBlue,
                modifier = Modifier.size(size * 0.5f)
            )
        }
    }
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
        containerColor = PopDeepBlue,
        titleContentColor = PopWhite,
        textContentColor = Color.LightGray,
        shape = RoundedCornerShape(32.dp),
        tonalElevation = 12.dp,
        title = {
            Text(
                text = stringResource(R.string.choose_difficulty),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DifficultyOption(
                    title = stringResource(R.string.easy_title),
                    isSelected = selectedDifficulty == Difficulty.EASY,
                    color = PopGreen,
                    onClick = { onDifficultyChanged(Difficulty.EASY) }
                )
                DifficultyOption(
                    title = stringResource(R.string.medium_title),
                    isSelected = selectedDifficulty == Difficulty.MEDIUM,
                    color = PopYellow,
                    onClick = { onDifficultyChanged(Difficulty.MEDIUM) }
                )
                DifficultyOption(
                    title = stringResource(R.string.hard_title),
                    isSelected = selectedDifficulty == Difficulty.HARD,
                    color = PopRed,
                    onClick = { onDifficultyChanged(Difficulty.HARD) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onStartGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PopBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.play).uppercase(), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun DifficultyOption(
    title: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) color else PopDarkBlue.copy(alpha = 0.5f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, PopWhite) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title.uppercase(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = if (isSelected) PopWhite else color
            )
        }
    }
}

@Composable
fun MultiplayerDialog(
    selectedCount: Int,
    onCountChanged: (Int) -> Unit,
    onStartGame: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PopDeepBlue,
        titleContentColor = PopWhite,
        textContentColor = Color.LightGray,
        shape = RoundedCornerShape(32.dp),
        title = {
            Text(
                text = stringResource(R.string.multiplayer),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.how_many_players), fontWeight = FontWeight.Bold)
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    (2..4).forEach { count ->
                        FilterChip(
                            selected = selectedCount == count,
                            onClick = { onCountChanged(count) },
                            label = { Text(count.toString(), fontWeight = FontWeight.ExtraBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PopBlue,
                                selectedLabelColor = PopWhite
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onStartGame(List(selectedCount) { "Player ${it + 1}" }) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PopGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.start_game).uppercase(), fontWeight = FontWeight.ExtraBold)
                }
            }
        },
        confirmButton = {}
    )
}

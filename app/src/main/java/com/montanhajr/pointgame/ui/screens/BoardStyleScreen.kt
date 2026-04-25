package com.montanhajr.pointgame.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.montanhajr.pointgame.BuildConfig
import com.montanhajr.pointgame.R
import com.montanhajr.pointgame.logic.StatisticsManager
import com.montanhajr.pointgame.models.BoardStyle
import com.montanhajr.pointgame.ui.components.BoardBackground
import com.montanhajr.pointgame.ui.components.PremiumDialog
import com.montanhajr.pointgame.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardStyleScreen(
    currentStyle: BoardStyle,
    isPremium: Boolean,
    onStyleSelected: (BoardStyle) -> Unit,
    onSubscribeClick: (Activity) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val statsManager = remember { StatisticsManager(context) }
    val isFounderUnlocked = remember { statsManager.isFounderUnlocked() }
    
    val isDebug = BuildConfig.DEBUG
    val canSelectAll = isDebug || isPremium
    var showPremiumDialog by remember { mutableStateOf(false) }

    BackHandler {
        onBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // App Background oficial
        Image(
            painter = painterResource(id = R.drawable.app_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Camada Pop!
        Box(modifier = Modifier.fillMaxSize().background(PopDarkBlue.copy(alpha = 0.6f)))

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "ESTILOS DE TABULEIRO", 
                            color = PopWhite, 
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PopWhite)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(BoardStyle.entries.toTypedArray()) { style ->
                        val isLocked = when (style) {
                            BoardStyle.DEFAULT_POP -> false
                            BoardStyle.FOUNDER_GOLD -> !isFounderUnlocked && !isDebug
                            else -> !canSelectAll
                        }
                        
                        val isSelected = style == currentStyle

                        PopStyleCard(
                            style = style,
                            isSelected = isSelected,
                            isLocked = isLocked,
                            isAchievement = style == BoardStyle.FOUNDER_GOLD,
                            onClick = { 
                                if (isLocked) {
                                    if (style != BoardStyle.FOUNDER_GOLD) {
                                        showPremiumDialog = true
                                    }
                                } else {
                                    onStyleSelected(style)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showPremiumDialog) {
        PremiumDialog(
            onDismiss = { showPremiumDialog = false },
            onSubscribeClick = { activity ->
                onSubscribeClick(activity)
                showPremiumDialog = false
            }
        )
    }
}

@Composable
fun PopStyleCard(
    style: BoardStyle,
    isSelected: Boolean,
    isLocked: Boolean,
    isAchievement: Boolean = false,
    onClick: () -> Unit
) {
    val styleName = when (style) {
        BoardStyle.DEFAULT_POP -> "PADRÃO DOT POP!"
        BoardStyle.GALAXY -> "GALÁXIA"
        BoardStyle.NEON_NIGHT -> "NEON NIGHT"
        BoardStyle.MINIMALIST_WHITE -> "MINIMALISTA"
        BoardStyle.RETRO_ARCADE -> "RETRO ARCADE"
        BoardStyle.PAPER_NOTEBOOK -> "CADERNO"
        BoardStyle.CHALKBOARD -> "LOUSA"
        BoardStyle.CYBERPUNK_GLITCH -> "CYBERPUNK"
        BoardStyle.ANCIENT_SCROLL -> "PERGAMINHO"
        BoardStyle.DEEP_SEA -> "OCEANO"
        BoardStyle.FOUNDER_GOLD -> "OURO FUNDADOR"
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .shadow(if (isSelected) 12.dp else 4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(4.dp, PopYellow) else null,
        colors = CardDefaults.cardColors(containerColor = PopDeepBlue)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Preview do tabuleiro original
            BoardBackground(style = style)
            
            if (isLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isAchievement) Icons.Default.Star else Icons.Default.Lock, 
                            contentDescription = null, 
                            tint = if (isAchievement) PopYellow else PopWhite, 
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isAchievement) "PLAY 10 GAMES" else "PREMIUM", 
                            color = if (isAchievement) PopYellow else PopCyan, 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Banner Inferior do Nome
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(PopDeepBlue.copy(alpha = 0.85f))
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = styleName,
                    color = PopWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }
            
            // Selo de Ativo
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                        .background(PopYellow, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "ATIVO", 
                        color = PopDarkBlue, 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

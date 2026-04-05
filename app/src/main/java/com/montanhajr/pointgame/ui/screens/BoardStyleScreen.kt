package com.montanhajr.pointgame.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.montanhajr.pointgame.BuildConfig
import com.montanhajr.pointgame.R
import com.montanhajr.pointgame.models.BoardStyle
import com.montanhajr.pointgame.ui.components.BoardBackground
import com.montanhajr.pointgame.ui.components.PremiumDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardStyleScreen(
    currentStyle: BoardStyle,
    isPremium: Boolean,
    onStyleSelected: (BoardStyle) -> Unit,
    onBack: () -> Unit
) {
    val isDebug = BuildConfig.DEBUG
    val canSelectAll = isDebug || isPremium
    var showPremiumDialog by remember { mutableStateOf(false) }

    // Trata o botão voltar do sistema
    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Board Styles", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A2E))
            )
        },
        containerColor = Color(0xFF0F0F1A)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(BoardStyle.values()) { style ->
                    val isLocked = !canSelectAll && style != BoardStyle.GALAXY
                    val isSelected = style == currentStyle

                    StyleCard(
                        style = style,
                        isSelected = isSelected,
                        isLocked = isLocked,
                        onClick = { 
                            if (isLocked) {
                                showPremiumDialog = true
                            } else {
                                onStyleSelected(style)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showPremiumDialog) {
        PremiumDialog(onDismiss = { showPremiumDialog = false })
    }
}

@Composable
fun StyleCard(
    style: BoardStyle,
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    val styleName = when (style) {
        BoardStyle.GALAXY -> "Galaxy (Default)"
        BoardStyle.NEON_NIGHT -> "Neon Night"
        BoardStyle.MINIMALIST_WHITE -> "Minimalist White"
        BoardStyle.RETRO_ARCADE -> "Retro Arcade"
        BoardStyle.PAPER_NOTEBOOK -> "Notebook"
        BoardStyle.CHALKBOARD -> "Chalkboard"
        BoardStyle.CYBERPUNK_GLITCH -> "Cyberpunk Glitch"
        BoardStyle.ANCIENT_SCROLL -> "Ancient Scroll"
        BoardStyle.DEEP_SEA -> "Deep Sea"
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFFFD700)) else null,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Preview do Background
            BoardBackground(style = style)
            
            // Overlay para bloqueado ou selecionado
            if (isLocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Text("PREMIUM", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            // Nome do estilo no rodapé
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(8.dp)
            ) {
                Text(
                    text = styleName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                        .background(Color(0xFFFFD700), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("ACTIVE", color = Color(0xFF1A1A2E), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

package com.montanhajr.pointgame.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.montanhajr.pointgame.R
import com.montanhajr.pointgame.logic.GameState
import com.montanhajr.pointgame.models.GameMode
import com.montanhajr.pointgame.models.UiThemeColors
import com.montanhajr.pointgame.models.getStylePlayerColor

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
            Text(text = score.toString(), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

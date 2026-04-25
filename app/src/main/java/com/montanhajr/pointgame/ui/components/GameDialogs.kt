package com.montanhajr.pointgame.ui.components

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.montanhajr.pointgame.R
import com.montanhajr.pointgame.logic.GameState
import com.montanhajr.pointgame.ui.theme.*

@Composable
fun GameDialogManager(
    gameState: GameState,
    showRules: Boolean,
    showRestart: Boolean,
    showPremium: Boolean,
    showLevelComplete: Boolean,
    showFallbackInterstitial: Boolean,
    unlockedAchievement: String?,
    careerLevel: Int,
    onDismissRules: () -> Unit,
    onConfirmRestart: () -> Unit,
    onDismissRestart: () -> Unit,
    onDismissPremium: () -> Unit,
    onSubscribePremium: (Activity) -> Unit,
    onNextLevel: () -> Unit,
    onBackToMenu: () -> Unit,
    onDismissAchievement: () -> Unit,
    onDismissFallback: () -> Unit,
    onPremiumFromFallback: () -> Unit
) {
    if (showRules) RulesDialog(onDismiss = onDismissRules)
    if (showRestart) RestartConfirmDialog(onConfirm = onConfirmRestart, onDismiss = onDismissRestart)
    if (showPremium) PremiumDialog(onDismiss = onDismissPremium, onSubscribeClick = onSubscribePremium)
    
    if (unlockedAchievement != null) {
        AchievementUnlockedDialog(achievementName = unlockedAchievement, onDismiss = onDismissAchievement)
    }

    if (showLevelComplete) {
        LevelCompleteDialog(
            level = careerLevel,
            onNextLevel = onNextLevel,
            onBackToMap = onBackToMenu
        )
    }

    if (gameState.gameOver && !showLevelComplete) {
        val isCpuWin = gameState.isCpuGame && gameState.getWinnerIndex() == 1
        
        if (gameState.careerLevel != null && isCpuWin) {
            DefeatDialog(
                level = gameState.careerLevel,
                onRetry = onConfirmRestart,
                onBackToMap = onBackToMenu
            )
        } else {
            GameOverDialog(
                message = gameState.getWinnerMessage(gameState.isCpuGame),
                onRestart = onConfirmRestart,
                onBackToMenu = onBackToMenu
            )
        }
    }

    if (showFallbackInterstitial) {
        FallbackInterstitialDialog(onDismiss = onDismissFallback, onPremiumClick = onPremiumFromFallback)
    }
}

@Composable
fun DefeatDialog(level: Int, onRetry: () -> Unit, onBackToMap: () -> Unit) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = PopDeepBlue,
            tonalElevation = 12.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            border = BorderStroke(2.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.SentimentVeryDissatisfied,
                    contentDescription = null,
                    tint = PopRed,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "NÃO FOI DESSA VEZ!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PopWhite,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "A CPU dominou o nível $level. Deseja tentar novamente?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PopRed),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("TENTAR NOVAMENTE", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onBackToMap,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    border = BorderStroke(2.dp, PopWhite.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = PopWhite, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("VOLTAR PARA O MAPA", color = PopWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GameOverDialog(message: String, onRestart: () -> Unit, onBackToMenu: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        containerColor = PopDeepBlue,
        titleContentColor = PopWhite,
        textContentColor = Color.LightGray,
        shape = RoundedCornerShape(32.dp),
        title = { Text(text = "FIM DE JOGO", fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), color = PopWhite) },
        text = { 
            Text(
                text = message.uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PopCyan,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = PopBlue),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("RECOMECAR", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onBackToMenu,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("VOLTAR AO MENU", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun LevelCompleteDialog(level: Int, onNextLevel: () -> Unit, onBackToMap: () -> Unit) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = PopDeepBlue,
            tonalElevation = 12.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            border = BorderStroke(2.dp, PopYellow.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = PopYellow,
                    modifier = Modifier.size(72.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "NÍVEL $level CONCLUÍDO!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PopWhite,
                    fontWeight = FontWeight.ExtraBold,
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
                    colors = ButtonDefaults.buttonColors(containerColor = PopGreen),
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
                    border = BorderStroke(2.dp, PopWhite.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = PopWhite, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("VOLTAR PARA O MAPA", color = PopWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RestartConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PopDeepBlue,
        titleContentColor = PopWhite,
        textContentColor = Color.LightGray,
        shape = RoundedCornerShape(28.dp),
        title = { Text(text = stringResource(R.string.restart_title), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
        text = { Text(text = stringResource(R.string.restart_desc), fontSize = 16.sp) },
        confirmButton = { 
            TextButton(onClick = onConfirm) { 
                Text(stringResource(R.string.restart_confirm).uppercase(), color = PopRed, fontWeight = FontWeight.ExtraBold) 
            } 
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { 
                Text(stringResource(R.string.cancel).uppercase(), color = PopWhite.copy(alpha = 0.6f), fontWeight = FontWeight.Bold) 
            } 
        }
    )
}

@Composable
fun AchievementUnlockedDialog(achievementName: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PopDeepBlue,
        titleContentColor = PopWhite,
        textContentColor = Color.LightGray,
        shape = RoundedCornerShape(32.dp),
        icon = {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = PopYellow, modifier = Modifier.size(56.dp))
        },
        title = {
            Text(
                text = "CONQUISTA!",
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = achievementName.uppercase(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PopYellow,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Você desbloqueou um novo estilo de tabuleiro exclusivo!",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = PopWhite.copy(alpha = 0.8f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PopGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("INCRÍVEL!", fontWeight = FontWeight.ExtraBold)
            }
        }
    )
}

@Composable
fun RulesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PopDeepBlue,
        titleContentColor = PopWhite,
        textContentColor = Color.LightGray,
        shape = RoundedCornerShape(28.dp),
        title = { Text(text = stringResource(R.string.rules).uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = PopCyan) },
        text = { 
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) { 
                PopRuleItem(title = stringResource(R.string.objective_title), description = stringResource(R.string.objective_desc))
                Spacer(modifier = Modifier.height(12.dp))
                PopRuleItem(title = stringResource(R.string.turns_title), description = stringResource(R.string.turns_desc))
                Spacer(modifier = Modifier.height(12.dp))
                PopRuleItem(title = stringResource(R.string.prohibited_title), description = stringResource(R.string.prohibited_desc)) 
            } 
        },
        confirmButton = { 
            Button(
                onClick = onDismiss, 
                colors = ButtonDefaults.buttonColors(containerColor = PopBlue),
                shape = RoundedCornerShape(12.dp)
            ) { 
                Text(stringResource(R.string.understood).uppercase(), fontWeight = FontWeight.ExtraBold) 
            } 
        }
    )
}

@Composable
private fun PopRuleItem(title: String, description: String) {
    Column {
        Text(text = title.uppercase(), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = PopYellow)
        Text(text = description, fontSize = 13.sp, color = PopWhite.copy(alpha = 0.7f))
    }
}

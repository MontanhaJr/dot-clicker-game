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

@Composable
fun GameDialogManager(
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

    if (showFallbackInterstitial) {
        FallbackInterstitialDialog(onDismiss = onDismissFallback, onPremiumClick = onPremiumFromFallback)
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
fun RulesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A2E),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray,
        title = { Text(text = stringResource(R.string.rules), fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = { 
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) { 
                RuleItem(title = stringResource(R.string.objective_title), description = stringResource(R.string.objective_desc))
                Spacer(modifier = Modifier.height(8.dp))
                RuleItem(title = stringResource(R.string.turns_title), description = stringResource(R.string.turns_desc))
                Spacer(modifier = Modifier.height(8.dp))
                RuleItem(title = stringResource(R.string.prohibited_title), description = stringResource(R.string.prohibited_desc)) 
            } 
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.understood), color = Color(0xFF00FFFF)) } }
    )
}

@Composable
private fun RuleItem(title: String, description: String) {
    Column {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF00FFFF))
        Text(text = description, fontSize = 13.sp, color = Color.LightGray)
    }
}

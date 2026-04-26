package com.montanhajr.pointgame.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.montanhajr.pointgame.models.PowerUpType
import com.montanhajr.pointgame.ui.theme.*

@Composable
fun RewardPowerUpFab(
    visible: Boolean,
    countdown: Int, // Mantemos para consistência de lógica, mas a animação será independente
    powerUpType: PowerUpType,
    onActivate: (PowerUpType) -> Unit
) {
    // Animatable permite um controle de progresso 100% contínuo (float)
    val progress = remember { Animatable(1f) }

    // Disparamos a animação visual sempre que o FAB se torna visível
    LaunchedEffect(visible) {
        if (visible) {
            progress.snapTo(1f) // Reseta para 100%
            progress.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 10000, // 10 segundos totais
                    easing = LinearEasing // Movimento constante e suave
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    // O indicador agora usa o valor do progress.value que atualiza a cada frame (60fps)
                    CircularProgressIndicator(
                        progress = { progress.value },
                        modifier = Modifier.size(64.dp),
                        color = PopCyan,
                        strokeWidth = 4.dp,
                        trackColor = PopWhite.copy(alpha = 0.1f)
                    )
                    
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { onActivate(powerUpType) },
                        shape = CircleShape,
                        color = PopCyan,
                        tonalElevation = 6.dp
                    ) {
                        Image(
                            painter = painterResource(id = powerUpType.iconRes),
                            contentDescription = powerUpType.displayName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Text(
                    text = powerUpType.displayName,
                    color = PopCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun InventoryPowerUpButton(
    powerUpType: PowerUpType?,
    onActivate: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        AnimatedVisibility(
            visible = powerUpType != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable { onActivate() },
                    shape = CircleShape,
                    color = PopCyan,
                    tonalElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(2.dp, PopWhite.copy(alpha = 0.5f))
                ) {
                    powerUpType?.let {
                        Image(
                            painter = painterResource(id = it.iconRes),
                            contentDescription = it.displayName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Text(
                    text = "USAR",
                    color = PopCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

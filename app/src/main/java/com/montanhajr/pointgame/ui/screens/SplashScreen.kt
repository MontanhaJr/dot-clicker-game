package com.montanhajr.pointgame.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.montanhajr.pointgame.R
import com.montanhajr.pointgame.logic.AppInitializer
import com.montanhajr.pointgame.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onLoadingComplete: () -> Unit) {
    val context = LocalContext.current
    val animatedProgress = remember { Animatable(0f) }
    var initProgress by remember { mutableStateOf(0f) }
    
    // OTIMIZAÇÃO SUPREMA: Começamos com a tela "preparada" para ser mostrada
    // mas só carregamos os elementos visuais após o sinal de prontidão do sistema.
    var isReadyToDrawContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Pequeno atraso de 50ms para garantir que a Splash Nativa já foi removida
        // pela MainActivity antes de carregar as imagens pesadas aqui.
        delay(50)
        isReadyToDrawContent = true
        
        val initJob = launch {
            AppInitializer.initialize(context) { progress ->
                initProgress = progress
            }
        }

        // Animação da barra (agora um pouco mais rápida para compensar a agilidade inicial)
        while (animatedProgress.value < 1f) {
            val target = if (initProgress > animatedProgress.value) initProgress else animatedProgress.value + 0.03f
            animatedProgress.animateTo(
                targetValue = target.coerceAtMost(1f),
                animationSpec = tween(durationMillis = 60, easing = LinearEasing)
            )
            if (animatedProgress.value >= 1f && initProgress >= 1f) break
            delay(20)
        }

        initJob.join()
        delay(100) 
        onLoadingComplete()
    }

    // Fundo sólido idêntico à Splash Nativa para transição imperceptível
    Box(modifier = Modifier.fillMaxSize().background(PopDarkBlue)) {
        if (isReadyToDrawContent) {
            Image(
                painter = painterResource(id = R.drawable.app_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(modifier = Modifier.fillMaxSize().background(PopDarkBlue.copy(alpha = 0.5f)))

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.dotpop_logo_no_bg),
                        contentDescription = "Dot Pop! Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                Column(
                    modifier = Modifier.width(160.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress.value)
                                .fillMaxHeight()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        colors = listOf(PopCyan, PopBlue)
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "INICIALIZANDO...",
                        color = PopCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }

        Text(
            text = "v1.4.4",
            color = PopWhite.copy(alpha = 0.3f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

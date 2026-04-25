package com.montanhajr.pointgame.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.montanhajr.pointgame.R
import com.montanhajr.pointgame.models.BoardStyle
import com.montanhajr.pointgame.ui.theme.PopDarkBlue
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun BoardBackground(style: BoardStyle) {
    when (style) {
        BoardStyle.DEFAULT_POP -> DefaultPopBackground()
        BoardStyle.GALAXY -> GalaxyBackground()
        BoardStyle.NEON_NIGHT -> NeonNightBackground()
        BoardStyle.MINIMALIST_WHITE -> MinimalistWhiteBackground()
        BoardStyle.RETRO_ARCADE -> RetroArcadeBackground()
        BoardStyle.PAPER_NOTEBOOK -> PaperNotebookBackground()
        BoardStyle.CHALKBOARD -> ChalkboardBackground()
        BoardStyle.CYBERPUNK_GLITCH -> CyberpunkGlitchBackground()
        BoardStyle.ANCIENT_SCROLL -> AncientScrollBackground()
        BoardStyle.DEEP_SEA -> DeepSeaBackground()
        BoardStyle.FOUNDER_GOLD -> FounderGoldBackground()
    }
}

@Composable
fun DefaultPopBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.app_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Adicionamos uma sobreposição para garantir legibilidade das linhas
        Box(modifier = Modifier.fillMaxSize().background(PopDarkBlue.copy(alpha = 0.7f)))
    }
}

@Composable
fun NeonNightBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "NeonTransition")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "NeonPulse"
    )

    Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFF000814))) {
        val gridStep = 80.dp.toPx()
        val glowColor = Color(0xFF00B4D8)
        
        for (y in 0..(size.height / gridStep).toInt()) {
            val yPos = y * gridStep
            drawLine(
                color = glowColor.copy(alpha = 0.15f * pulse),
                start = Offset(0f, yPos),
                end = Offset(size.width, yPos),
                strokeWidth = 2.dp.toPx()
            )
        }
        
        for (x in 0..(size.width / gridStep).toInt()) {
            val xPos = x * gridStep
            drawLine(
                color = glowColor.copy(alpha = 0.15f * pulse),
                start = Offset(xPos, 0f),
                end = Offset(xPos, size.height),
                strokeWidth = 2.dp.toPx()
            )
        }

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color(0xFF000814).copy(alpha = 0.8f)),
                center = center,
                radius = size.maxDimension / 1.5f
            )
        )
    }
}

@Composable
fun MinimalistWhiteBackground() {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA)))
}

@Composable
fun RetroArcadeBackground() {
    Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0D0D))) {
        val scanlineStep = 4.dp.toPx()
        for (y in 0..(size.height / scanlineStep).toInt()) {
            val yPos = y * scanlineStep
            drawLine(
                color = Color.Black.copy(alpha = 0.3f),
                start = Offset(0f, yPos),
                end = Offset(size.width, yPos),
                strokeWidth = 1.dp.toPx()
            )
        }

        val blockSize = 20.dp.toPx()
        for (x in 0..(size.width / blockSize).toInt()) {
            for (y in 0..(size.height / blockSize).toInt()) {
                if ((x + y) % 2 == 0) {
                    drawRect(
                        color = Color(0xFF1A1A2E),
                        topLeft = Offset(x * blockSize, y * blockSize),
                        size = androidx.compose.ui.geometry.Size(blockSize, blockSize)
                    )
                }
            }
        }

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                center = center,
                radius = size.maxDimension / 1.2f
            )
        )
    }
}

@Composable
fun PaperNotebookBackground() {
    Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFFFCF5E5))) {
        val strokeWidth = 1.dp.toPx()
        val step = 32.dp.toPx()
        
        for (y in 0..(size.height / step).toInt()) {
            val yPos = y * step + 40.dp.toPx()
            if (yPos < size.height) {
                drawLine(
                    color = Color(0xFFADD8E6).copy(alpha = 0.7f),
                    start = Offset(0f, yPos),
                    end = Offset(size.width, yPos),
                    strokeWidth = strokeWidth
                )
            }
        }
        
        drawLine(
            color = Color(0xFFFFB6C1),
            start = Offset(50.dp.toPx(), 0f),
            end = Offset(50.dp.toPx(), size.height),
            strokeWidth = 2.dp.toPx()
        )
    }
}

@Composable
fun ChalkboardBackground() {
    Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFF1B2621))) {
        for (i in 0..500) {
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = (1..5).random().toFloat(),
                center = Offset((0..size.width.toInt()).random().toFloat(), (0..size.height.toInt()).random().toFloat())
            )
        }
    }
}

@Composable
fun CyberpunkGlitchBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "GlitchTransition")
    val glitchAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlitchAlpha"
    )

    Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0510))) {
        drawRect(
            color = Color(0xFFBB00FF).copy(alpha = glitchAlpha),
            topLeft = Offset(0f, (0..size.height.toInt()).random().toFloat()),
            size = androidx.compose.ui.geometry.Size(size.width, (2..10).random().toFloat())
        )
        
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF1A0B2E), Color(0xFF0A0510))
            )
        )
    }
}

@Composable
fun AncientScrollBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.ancient_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                color = Color(0xFF2B1B17).copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun DeepSeaBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "SeaTransition")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WaveOffset"
    )

    Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFF001219))) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF005F73), Color(0xFF001219))
            )
        )
        
        for (i in 0..30) {
            val yPos = (size.height - ((i * 100 + waveOffset) % size.height))
            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                radius = 2.dp.toPx(),
                center = Offset((i * 50) % size.width, yPos)
            )
        }
    }
}

@Composable
fun FounderGoldBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.gold_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        val infiniteTransition = rememberInfiniteTransition(label = "GoldTransition")
        
        // Aumentado o range (-2f a 2f) e diminuído o tempo (3000ms) para ser mais rápido e ir até o final
        val shineProgress by infiniteTransition.animateFloat(
            initialValue = -2f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ShineProgress"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Reflexo de luz diagonal mais largo (color stops 0.3f a 0.7f)
            drawRect(
                brush = Brush.linearGradient(
                    0.0f to Color.Transparent,
                    0.3f to Color.White.copy(alpha = 0.02f),
                    0.5f to Color.White.copy(alpha = 0.15f),
                    0.7f to Color.White.copy(alpha = 0.02f),
                    1.0f to Color.Transparent,
                    start = Offset(size.width * shineProgress, 0f),
                    end = Offset(size.width * (shineProgress + 1f), size.height)
                )
            )
            
            // Partículas de "ouro"
            val random = Random(123)
            for (i in 0..30) {
                drawCircle(
                    color = Color(0xFFFFD700).copy(alpha = 0.15f),
                    radius = random.nextFloat() * 2.dp.toPx(),
                    center = Offset(random.nextFloat() * size.width, random.nextFloat() * size.height)
                )
            }
        }
    }
}

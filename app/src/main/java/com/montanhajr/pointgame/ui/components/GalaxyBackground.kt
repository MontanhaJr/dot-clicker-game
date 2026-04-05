package com.montanhajr.pointgame.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class Star(
    val xRel: Float,
    val yRel: Float,
    val radius: Float,
    val color: Color,
    val animDuration: Int,
    val animDelay: Int
)

@Composable
fun GalaxyBackground() {
    val starColors = listOf(
        Color(0xFFFFD700), // Gold
        Color(0xFFFF69B4), // Pink
        Color(0xFF00FFFF), // Cyan
        Color(0xFFADFF2F), // GreenYellow
        Color(0xFFFF4500)  // OrangeRed
    )

    val stars = remember {
        val random = Random(42)
        List(135) {
            Star(
                xRel = random.nextFloat(),
                yRel = random.nextFloat(),
                radius = random.nextFloat() * 2.5f + 1f,
                color = starColors[random.nextInt(starColors.size)],
                animDuration = random.nextInt(4000, 8000),
                animDelay = random.nextInt(0, 10000)
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "GalaxyStars")
    
    val timeFactor by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "TimeFactor"
    )

    // Fundo preto fixo para a galáxia
    Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A1A))) {
        stars.forEach { star ->
            val x = star.xRel * size.width
            val y = star.yRel * size.height
            
            val globalTime = (timeFactor * 20000).toInt()
            val starTime = (globalTime + star.animDelay) % star.animDuration
            val starProgress = starTime.toFloat() / star.animDuration
            
            val wave = kotlin.math.sin(starProgress * 2 * kotlin.math.PI).toFloat()
            val intensity = (wave + 1f) / 2f
            
            val currentAlpha = (0.15f + (intensity * 0.55f)).coerceIn(0f, 1f)
            val currentRadius = star.radius * (0.85f + (intensity * 0.3f))

            drawCircle(
                color = star.color.copy(alpha = currentAlpha),
                radius = currentRadius,
                center = Offset(x, y)
            )
            
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(star.color.copy(alpha = currentAlpha * 0.25f), Color.Transparent),
                    center = Offset(x, y),
                    radius = currentRadius * 5f
                ),
                radius = currentRadius * 5f,
                center = Offset(x, y)
            )
        }
    }
}

package com.montanhajr.pointgame.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.montanhajr.pointgame.logic.GameState
import com.montanhajr.pointgame.models.DotPoint
import com.montanhajr.pointgame.models.PlayerColors
import com.montanhajr.pointgame.models.Triangle
import kotlin.math.sqrt

@Composable
fun GameBoard(
    gameState: GameState,
    enabled: Boolean,
    onLineDrawn: (Int, Int) -> Unit
) {
    var dragStartId by remember { mutableStateOf<Int?>(null) }
    var currentDragPosition by remember { mutableStateOf<Offset?>(null) }
    var hoveredPointId by remember { mutableStateOf<Int?>(null) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val density = LocalDensity.current.density
        val canvasWidth = maxWidth.value * density
        val canvasHeight = maxHeight.value * density

        val scaledPoints = remember(canvasWidth, canvasHeight, gameState.points) {
            val paddingPixels = 150f
            val usableWidth = 900f - (2 * paddingPixels)
            val usableHeight = 1200f - (2 * paddingPixels)

            gameState.points.map { point ->
                point.copy(
                    position = Offset(
                        x = paddingPixels + (point.position.x - paddingPixels) * (canvasWidth - (2 * paddingPixels * canvasWidth / 900f)) / usableWidth,
                        y = paddingPixels + (point.position.y - paddingPixels) * (canvasHeight - (2 * paddingPixels * canvasHeight / 1200f)) / usableHeight
                    )
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .pointerInput(gameState.gameOver, enabled) {
                    if (!gameState.gameOver && enabled) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val pointId = getNearestPointId(offset, scaledPoints)
                                if (pointId != null) {
                                    dragStartId = pointId
                                    currentDragPosition = offset
                                }
                            },
                            onDrag = { change, _ ->
                                currentDragPosition = change.position
                                hoveredPointId = getNearestPointId(change.position, scaledPoints)
                            },
                            onDragEnd = {
                                if (dragStartId != null && hoveredPointId != null) {
                                    if (gameState.isValidMove(dragStartId!!, hoveredPointId!!)) {
                                        onLineDrawn(dragStartId!!, hoveredPointId!!)
                                    }
                                }
                                dragStartId = null
                                currentDragPosition = null
                                hoveredPointId = null
                            },
                            onDragCancel = {
                                dragStartId = null
                                currentDragPosition = null
                                hoveredPointId = null
                            }
                        )
                    }
                }
        ) {
            gameState.triangles.forEach { triangle ->
                val p1 = scaledPoints.find { it.id == triangle.p1Id }
                val p2 = scaledPoints.find { it.id == triangle.p2Id }
                val p3 = scaledPoints.find { it.id == triangle.p3Id }

                if (p1 != null && p2 != null && p3 != null) {
                    val color = PlayerColors[(triangle.owner - 1) % PlayerColors.size]
                    val triangleColor = color.copy(alpha = 0.15f)

                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(p1.position.x, p1.position.y)
                        lineTo(p2.position.x, p2.position.y)
                        lineTo(p3.position.x, p3.position.y)
                        close()
                    }

                    drawPath(path = path, color = triangleColor)

                    val centerX = (p1.position.x + p2.position.x + p3.position.x) / 3
                    val centerY = (p1.position.y + p2.position.y + p3.position.y) / 3

                    val letter = gameState.playerNames[triangle.owner - 1][0].toString()

                    drawContext.canvas.nativeCanvas.drawText(
                        letter,
                        centerX,
                        centerY + 20,
                        android.graphics.Paint().apply {
                            this.color = android.graphics.Color.argb(
                                (color.alpha * 255).toInt(),
                                (color.red * 255).toInt(),
                                (color.green * 255).toInt(),
                                (color.blue * 255).toInt()
                            )
                            textSize = 50f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                            // Efeito de brilho no texto
                            setShadowLayer(10f, 0f, 0f, android.graphics.Color.argb(
                                200,
                                (color.red * 255).toInt(),
                                (color.green * 255).toInt(),
                                (color.blue * 255).toInt()
                            ))
                        }
                    )
                }
            }

            gameState.lines.forEach { line ->
                val start = scaledPoints.find { it.id == line.startId }
                val end = scaledPoints.find { it.id == line.endId }

                if (start != null && end != null) {
                    val lineColor = PlayerColors[(line.player - 1) % PlayerColors.size]
                    
                    // Efeito Neon (camadas de linha)
                    // 1. Brilho externo (largo e muito transparente)
                    drawLine(
                        color = lineColor.copy(alpha = 0.2f),
                        start = start.position,
                        end = end.position,
                        strokeWidth = 24f,
                        cap = StrokeCap.Round
                    )
                    // 2. Brilho médio
                    drawLine(
                        color = lineColor.copy(alpha = 0.5f),
                        start = start.position,
                        end = end.position,
                        strokeWidth = 14f,
                        cap = StrokeCap.Round
                    )
                    // 3. Núcleo da linha (sólido)
                    drawLine(
                        color = lineColor,
                        start = start.position,
                        end = end.position,
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }
            }

            if (dragStartId != null && currentDragPosition != null && enabled) {
                val startPoint = scaledPoints.find { it.id == dragStartId }
                if (startPoint != null) {
                    val color = PlayerColors[(gameState.currentPlayer - 1) % PlayerColors.size]
                    val lineColor = color.copy(alpha = 0.5f)

                    if (hoveredPointId != null && gameState.isValidMove(dragStartId!!, hoveredPointId!!)) {
                        val endPoint = scaledPoints.find { it.id == hoveredPointId }
                        if (endPoint != null) {
                            drawLine(
                                color = color.copy(alpha = 0.8f),
                                start = startPoint.position,
                                end = endPoint.position,
                                strokeWidth = 10f,
                                cap = StrokeCap.Round
                            )
                        }
                    } else {
                        drawLine(
                            color = lineColor,
                            start = startPoint.position,
                            end = currentDragPosition!!,
                            strokeWidth = 8f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            scaledPoints.forEach { point ->
                val isDragStart = point.id == dragStartId
                val isHovered = point.id == hoveredPointId

                if (isHovered && dragStartId != null && enabled) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f),
                        radius = 40f,
                        center = point.position
                    )
                }

                // Estilo Neon para os pontos
                val pointColor = when {
                    isDragStart && enabled -> Color(0xFF00FF00) // Neon Green
                    isHovered && dragStartId != null && enabled -> Color(0xFF00FFFF) // Neon Cyan
                    else -> Color(0xFFCCCCCC)
                }

                if (isDragStart || isHovered) {
                    // Brilho do ponto selecionado
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(pointColor.copy(alpha = 0.6f), Color.Transparent),
                            center = point.position,
                            radius = 35f
                        ),
                        radius = 35f,
                        center = point.position
                    )
                }

                drawCircle(
                    color = pointColor,
                    radius = if (isDragStart && enabled) 16f else if (isHovered && dragStartId != null && enabled) 14f else 10f,
                    center = point.position
                )
                
                // Núcleo branco para pontos ativos
                if (isDragStart || isHovered) {
                    drawCircle(
                        color = Color.White,
                        radius = 6f,
                        center = point.position
                    )
                }
            }
        }
    }
}

private fun getNearestPointId(offset: Offset, points: List<DotPoint>): Int? {
    var nearestId: Int? = null
    var minDistance = Float.MAX_VALUE

    points.forEach { point ->
        val distance = sqrt(
            (offset.x - point.position.x) * (offset.x - point.position.x) +
                    (offset.y - point.position.y) * (offset.y - point.position.y)
        )

        if (distance < minDistance && distance < 100) {
            minDistance = distance
            nearestId = point.id
        }
    }

    return nearestId
}

package com.montanhajr.pointgame.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.montanhajr.pointgame.logic.GameState
import com.montanhajr.pointgame.models.DotPoint
import com.montanhajr.pointgame.models.PlayerColors
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

    val cpuColor = Color(0xFFE0E0E0)
    val density = LocalDensity.current
    val touchThreshold = with(density) { 44.dp.toPx() } // Área de toque maior para facilitar seleção

    fun getPlayerColor(playerIndex: Int): Color {
        return if (gameState.isCpuGame && playerIndex == 2) {
            cpuColor
        } else {
            PlayerColors[(playerIndex - 1) % PlayerColors.size]
        }
    }

    fun getPlayerInitial(playerIndex: Int): String {
        if (gameState.isCpuGame && playerIndex == 2) return "C"
        val name = gameState.playerNames.getOrNull(playerIndex - 1) ?: return "?"
        val prefixes = listOf("Player ", "Jogador ", "Spieler ", "Joueur ")
        for (prefix in prefixes) {
            if (name.startsWith(prefix, ignoreCase = true)) {
                val afterPrefix = name.substring(prefix.length).trim()
                if (afterPrefix.isNotEmpty()) return afterPrefix.take(1).uppercase()
            }
        }
        return name.take(1).uppercase()
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val canvasWidth = maxWidth.value * density.density
        val canvasHeight = maxHeight.value * density.density

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
                .padding(24.dp) // Reduzi um pouco o padding externo para dar mais espaço
                .pointerInput(gameState.gameOver, enabled) {
                    if (!gameState.gameOver && enabled) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val pointId = getNearestPointId(offset, scaledPoints, touchThreshold)
                                if (pointId != null) {
                                    dragStartId = pointId
                                    currentDragPosition = offset
                                }
                            },
                            onDrag = { change, _ ->
                                currentDragPosition = change.position
                                hoveredPointId = getNearestPointId(change.position, scaledPoints, touchThreshold)
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
            // Desenho dos triângulos
            gameState.triangles.forEach { triangle ->
                val p1 = scaledPoints.find { it.id == triangle.p1Id }
                val p2 = scaledPoints.find { it.id == triangle.p2Id }
                val p3 = scaledPoints.find { it.id == triangle.p3Id }

                if (p1 != null && p2 != null && p3 != null) {
                    val color = getPlayerColor(triangle.owner)
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

                    val letter = getPlayerInitial(triangle.owner)

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
                        }
                    )
                }
            }

            // Desenho das linhas existentes
            gameState.lines.forEach { line ->
                val start = scaledPoints.find { it.id == line.startId }
                val end = scaledPoints.find { it.id == line.endId }

                if (start != null && end != null) {
                    val lineColor = getPlayerColor(line.player)
                    
                    drawLine(
                        color = lineColor.copy(alpha = 0.2f),
                        start = start.position,
                        end = end.position,
                        strokeWidth = 24f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = lineColor.copy(alpha = 0.5f),
                        start = start.position,
                        end = end.position,
                        strokeWidth = 14f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = lineColor,
                        start = start.position,
                        end = end.position,
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // Desenho da linha de drag atual
            if (dragStartId != null && currentDragPosition != null && enabled) {
                val startPoint = scaledPoints.find { it.id == dragStartId }
                if (startPoint != null) {
                    val color = getPlayerColor(gameState.currentPlayer)
                    val isValidMove = hoveredPointId != null && gameState.isValidMove(dragStartId!!, hoveredPointId!!)
                    val isInvalidHover = hoveredPointId != null && !isValidMove
                    
                    if (hoveredPointId != null) {
                        val endPoint = scaledPoints.find { it.id == hoveredPointId }
                        if (endPoint != null) {
                            drawLine(
                                color = if (isValidMove) color.copy(alpha = 0.8f) else Color.Red,
                                start = startPoint.position,
                                end = endPoint.position,
                                strokeWidth = 10f,
                                cap = StrokeCap.Round
                            )
                        }
                    } else {
                        drawLine(
                            color = color.copy(alpha = 0.5f),
                            start = startPoint.position,
                            end = currentDragPosition!!,
                            strokeWidth = 8f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Desenho dos pontos
            scaledPoints.forEach { point ->
                val isDragStart = point.id == dragStartId
                val isHovered = point.id == hoveredPointId
                val isInvalidMove = dragStartId != null && isHovered && !gameState.isValidMove(dragStartId!!, hoveredPointId!!)

                val currentPlayerColor = getPlayerColor(gameState.currentPlayer)

                // Feedback visual de seleção
                if (isHovered && dragStartId != null && enabled) {
                    drawCircle(
                        color = if (isInvalidMove) Color.Red.copy(alpha = 0.3f) else currentPlayerColor.copy(alpha = 0.2f),
                        radius = 50f,
                        center = point.position
                    )
                }

                val pointColor = when {
                    isDragStart && enabled -> currentPlayerColor
                    isHovered && dragStartId != null && enabled -> {
                        if (isInvalidMove) Color.Red else currentPlayerColor.copy(alpha = 0.8f)
                    }
                    else -> Color(0xFFCCCCCC)
                }

                // Brilho ao redor do ponto selecionado/focado
                if (isDragStart || isHovered) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(pointColor.copy(alpha = 0.6f), Color.Transparent),
                            center = point.position,
                            radius = 45f
                        ),
                        radius = 45f,
                        center = point.position
                    )
                }

                // Ponto físico (Aumentado para facilitar visualização)
                drawCircle(
                    color = pointColor,
                    radius = if (isDragStart && enabled) 18f else if (isHovered && dragStartId != null && enabled) 16f else 12f,
                    center = point.position
                )
                
                if (isDragStart || isHovered) {
                    drawCircle(
                        color = Color.White,
                        radius = 8f,
                        center = point.position
                    )
                }
            }
        }
    }
}

private fun getNearestPointId(offset: Offset, points: List<DotPoint>, threshold: Float): Int? {
    var nearestId: Int? = null
    var minDistanceSq = Float.MAX_VALUE
    val thresholdSq = threshold * threshold

    points.forEach { point ->
        val dx = offset.x - point.position.x
        val dy = offset.y - point.position.y
        val distanceSq = dx * dx + dy * dy

        if (distanceSq < thresholdSq && distanceSq < minDistanceSq) {
            minDistanceSq = distanceSq
            nearestId = point.id
        }
    }

    return nearestId
}

package com.montanhajr.pointgame.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.montanhajr.pointgame.logic.GameState
import com.montanhajr.pointgame.models.BoardStyle
import com.montanhajr.pointgame.models.DotPoint
import com.montanhajr.pointgame.models.Line
import com.montanhajr.pointgame.models.getStylePlayerColor
import com.montanhajr.pointgame.models.getStyleUiColors
import kotlin.math.sqrt

@Composable
fun GameBoard(
    gameState: GameState,
    enabled: Boolean,
    showEagleEye: Boolean = false,
    showXRay: Boolean = false,
    isEraserActive: Boolean = false,
    onLineDrawn: (Int, Int) -> Unit,
    onLineErased: (Line) -> Unit = {}
) {
    var dragStartId by remember { mutableStateOf<Int?>(null) }
    var currentDragPosition by remember { mutableStateOf<Offset?>(null) }
    var hoveredPointId by remember { mutableStateOf<Int?>(null) }

    val density = LocalDensity.current
    val touchThreshold = with(density) { 44.dp.toPx() }
    val style = gameState.boardStyle
    val uiColors = remember(style) { getStyleUiColors(style) }

    val pointBaseColor = when (style) {
        BoardStyle.MINIMALIST_WHITE -> Color(0xFF333333)
        BoardStyle.PAPER_NOTEBOOK -> Color(0xFF333333)
        BoardStyle.RETRO_ARCADE -> Color(0xFF00FF00)
        BoardStyle.CHALKBOARD -> Color(0xFFEEEEEE)
        BoardStyle.CYBERPUNK_GLITCH -> Color(0xFF00FFFF)
        BoardStyle.ANCIENT_SCROLL -> Color(0xFF4A3728)
        BoardStyle.DEEP_SEA -> Color(0xFF94D2BD)
        BoardStyle.FOUNDER_GOLD -> Color(0xFF8B7310)
        else -> Color(0xFFCCCCCC)
    }

    fun getPlayerColor(playerIndex: Int): Color = getStylePlayerColor(style, playerIndex, gameState.isCpuGame)

    fun getPlayerInitial(playerIndex: Int): String {
        if (gameState.isCpuGame && playerIndex == 2) return "C"
        val name = gameState.playerNames.getOrNull(playerIndex - 1) ?: return "?"
        return name.take(1).uppercase()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = maxWidth.value * density.density
        val canvasHeight = maxHeight.value * density.density

        val scaledPoints = remember(canvasWidth, canvasHeight, gameState.points) {
            val paddingPx = 24.dp.value * density.density
            val usableWidth = canvasWidth - (paddingPx * 2)
            val usableHeight = canvasHeight - (paddingPx * 2)

            gameState.points.map { point ->
                point.copy(
                    position = Offset(
                        x = paddingPx + (point.position.x / 900f) * usableWidth,
                        y = paddingPx + (point.position.y / 1200f) * usableHeight
                    )
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .pointerInput(gameState.gameOver, enabled, isEraserActive) {
                    if (!gameState.gameOver && enabled) {
                        if (isEraserActive) {
                            detectTapGestures { offset ->
                                val cpuLines = gameState.lines.filter { it.player == 2 }
                                val nearestLine = cpuLines.find { line ->
                                    val start = scaledPoints.find { it.id == line.startId }?.position ?: return@find false
                                    val end = scaledPoints.find { it.id == line.endId }?.position ?: return@find false
                                    distanceToLine(offset, start, end) < 40f
                                }
                                nearestLine?.let { onLineErased(it) }
                            }
                        } else {
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
                }
        ) {
            // X-Ray Vision: Show all possible connections
            if (showXRay) {
                gameState.getAllPossibleConnections().forEach { (sId, eId) ->
                    val start = scaledPoints.find { it.id == sId }?.position ?: return@forEach
                    val end = scaledPoints.find { it.id == eId }?.position ?: return@forEach
                    drawLine(
                        color = uiColors.xRay.copy(alpha = 0.2f),
                        start = start,
                        end = end,
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                    )
                }
            }

            // Eagle Eye: Highlight completable triangles
            if (showEagleEye) {
                gameState.getAllCompletableLines().forEach { (sId, eId) ->
                    val start = scaledPoints.find { it.id == sId }?.position ?: return@forEach
                    val end = scaledPoints.find { it.id == eId }?.position ?: return@forEach
                    drawLine(
                        color = uiColors.eagleEye.copy(alpha = 0.6f),
                        start = start,
                        end = end,
                        strokeWidth = 10f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // Triângulos
            gameState.triangles.forEach { triangle ->
                val p1 = scaledPoints.find { it.id == triangle.p1Id }
                val p2 = scaledPoints.find { it.id == triangle.p2Id }
                val p3 = scaledPoints.find { it.id == triangle.p3Id }

                if (p1 != null && p2 != null && p3 != null) {
                    val color = getPlayerColor(triangle.owner)
                    val triangleColor = color.copy(alpha = 0.15f)
                    val path = Path().apply {
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
                        letter, centerX, centerY + 20,
                        android.graphics.Paint().apply {
                            this.color = android.graphics.Color.argb((color.alpha * 255).toInt(), (color.red * 255).toInt(), (color.green * 255).toInt(), (color.blue * 255).toInt())
                            textSize = 50f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                    )
                }
            }

            // Linhas
            gameState.lines.forEach { line ->
                val start = scaledPoints.find { it.id == line.startId }
                val end = scaledPoints.find { it.id == line.endId }
                if (start != null && end != null) {
                    val lineColor = getPlayerColor(line.player)
                    
                    if (style == BoardStyle.NEON_NIGHT) {
                        drawLine(color = lineColor.copy(alpha = 0.3f), start = start.position, end = end.position, strokeWidth = 20f, cap = StrokeCap.Round)
                    }

                    if (style == BoardStyle.FOUNDER_GOLD) {
                        val borderColor = if (line.player == 1) Color(0xFFFFFACD) else Color(0xFF222222)
                        drawLine(color = borderColor, start = start.position, end = end.position, strokeWidth = 10f, cap = StrokeCap.Round)
                        drawLine(color = lineColor, start = start.position, end = end.position, strokeWidth = 6f, cap = StrokeCap.Round)
                    } else if (style == BoardStyle.RETRO_ARCADE) {
                        drawLine(color = lineColor, start = start.position, end = end.position, strokeWidth = 8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                    } else {
                        drawLine(color = lineColor, start = start.position, end = end.position, strokeWidth = if (style == BoardStyle.PAPER_NOTEBOOK) 4f else 6f, cap = StrokeCap.Round)
                    }
                }
            }

            // Drag Preview
            if (dragStartId != null && currentDragPosition != null && enabled) {
                val startPoint = scaledPoints.find { it.id == dragStartId }
                if (startPoint != null) {
                    val color = getPlayerColor(gameState.currentPlayer)
                    val isValidHover = hoveredPointId != null && gameState.isValidMove(dragStartId!!, hoveredPointId!!)
                    val lineEnd = if (hoveredPointId != null) scaledPoints.find { it.id == hoveredPointId }?.position ?: currentDragPosition!! else currentDragPosition!!

                    if (style == BoardStyle.RETRO_ARCADE) {
                        drawLine(color = if (hoveredPointId != null && !isValidHover) Color.Red else color.copy(alpha = 0.6f), start = startPoint.position, end = lineEnd, strokeWidth = 8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                    } else {
                        drawLine(color = if (hoveredPointId != null && !isValidHover) Color.Red else color.copy(alpha = 0.5f), start = startPoint.position, end = lineEnd, strokeWidth = 8f, cap = StrokeCap.Round)
                    }
                }
            }

            // Pontos
            scaledPoints.forEach { point ->
                val isDragStart = point.id == dragStartId
                val isHovered = point.id == hoveredPointId
                val isInvalidMove = dragStartId != null && isHovered && !gameState.isValidMove(dragStartId!!, hoveredPointId!!)
                val currentPlayerColor = getPlayerColor(gameState.currentPlayer)

                val pointColor = when {
                    isDragStart && enabled -> currentPlayerColor
                    isHovered && dragStartId != null && enabled -> if (isInvalidMove) Color.Red else currentPlayerColor.copy(alpha = 0.8f)
                    else -> pointBaseColor
                }

                if (style == BoardStyle.RETRO_ARCADE) {
                    val size = if (isDragStart || isHovered) 24f else 16f
                    drawRect(color = pointColor, topLeft = Offset(point.position.x - size/2, point.position.y - size/2), size = androidx.compose.ui.geometry.Size(size, size))
                } else {
                    if (style == BoardStyle.NEON_NIGHT && (isDragStart || isHovered)) {
                        drawCircle(brush = Brush.radialGradient(colors = listOf(pointColor.copy(alpha = 0.5f), Color.Transparent), center = point.position, radius = 40f), radius = 40f, center = point.position)
                    }
                    drawCircle(color = pointColor, radius = if (isDragStart && enabled) 18f else if (isHovered && dragStartId != null && enabled) 16f else 12f, center = point.position)
                    if (isDragStart || isHovered) {
                        drawCircle(color = Color.White, radius = 8f, center = point.position)
                    }
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

private fun distanceToLine(p: Offset, s: Offset, e: Offset): Float {
    val l2 = (e.x - s.x) * (e.x - s.x) + (e.y - s.y) * (e.y - s.y)
    if (l2 == 0f) return sqrt((p.x - s.x) * (p.x - s.x) + (p.y - s.y) * (p.y - s.y))
    var t = ((p.x - s.x) * (e.x - s.x) + (p.y - s.y) * (e.y - s.y)) / l2
    t = t.coerceIn(0f, 1f)
    return sqrt((p.x - (s.x + t * (e.x - s.x))) * (p.x - (s.x + t * (e.x - s.x))) + (p.y - (s.y + t * (e.y - s.y))) * (p.y - (s.y + t * (e.y - s.y))))
}

package com.montanhajr.pointgame.logic

import androidx.compose.ui.geometry.Offset
import com.montanhajr.pointgame.models.*
import kotlin.math.sqrt
import kotlin.random.Random

data class GameState(
    val points: List<DotPoint>,
    val lines: List<Line> = emptyList(),
    val triangles: List<Triangle> = emptyList(),
    val currentPlayer: Int = 1,
    val playerScores: List<Int>,
    val playerNames: List<String>,
    val numPlayers: Int,
    val gameOver: Boolean = false,
    val isCpuGame: Boolean = false,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val boardStyle: BoardStyle = BoardStyle.GALAXY,
    val careerLevel: Int? = null
) {
    companion object {
        fun createNew(
            isCpuGame: Boolean = false,
            difficulty: Difficulty = Difficulty.MEDIUM,
            numPlayers: Int = 2,
            playerNames: List<String>? = null,
            boardStyle: BoardStyle = BoardStyle.GALAXY,
            numPointsParam: Int? = null,
            careerLevel: Int? = null
        ): GameState {
            val random = Random(System.currentTimeMillis())
            val numPoints = numPointsParam ?: (18 + random.nextInt(8))
            val points = mutableListOf<DotPoint>()
            val minDistance = 110f
            val canvasWidth = 900f
            val canvasHeight = 1200f
            val padding = 150f
            var attempts = 0
            val maxAttempts = 2000 // Aumentado para suportar mais pontos em níveis altos

            while (points.size < numPoints && attempts < maxAttempts) {
                val x = padding + random.nextFloat() * (canvasWidth - 2 * padding)
                val y = padding + random.nextFloat() * (canvasHeight - 2 * padding)
                val newPoint = Offset(x, y)

                val isFarEnough = points.all { existingPoint ->
                    val distance = sqrt(
                        (newPoint.x - existingPoint.position.x) * (newPoint.x - existingPoint.position.x) +
                                (newPoint.y - existingPoint.position.y) * (newPoint.y - existingPoint.position.y)
                    )
                    distance >= minDistance
                }

                if (isFarEnough) {
                    points.add(DotPoint(points.size, newPoint))
                }
                attempts++
            }

            val names = playerNames ?: List(numPlayers) { i -> if (isCpuGame && i == 1) "CPU" else "P${i + 1}" }
            val scores = List(numPlayers) { 0 }

            return GameState(
                points = points,
                playerScores = scores,
                playerNames = names,
                numPlayers = numPlayers,
                isCpuGame = isCpuGame,
                difficulty = difficulty,
                boardStyle = boardStyle,
                careerLevel = careerLevel
            )
        }
    }

    fun drawLine(startId: Int, endId: Int): GameState {
        if (!isValidMove(startId, endId)) return this

        val newLine = Line(startId, endId, currentPlayer)
        val newLines = lines + newLine
        
        val newlyFoundTriangles = findTrianglesForLine(newLine, newLines)
        val newTriangles = triangles + newlyFoundTriangles
        
        val trianglesCompleted = newlyFoundTriangles.isNotEmpty()
        
        val newScores = playerScores.toMutableList()
        if (trianglesCompleted) {
            newScores[currentPlayer - 1] += newlyFoundTriangles.size
        }

        val isGameOver = !hasValidMovesLeft(newLines, newTriangles)
        var nextPlayer = if (trianglesCompleted && !isGameOver) currentPlayer 
                        else if (currentPlayer >= numPlayers) 1 else currentPlayer + 1

        if (isGameOver) {
            val maxScore = newScores.maxOrNull() ?: 0
            val winnerIndex = newScores.indexOf(maxScore)
            if (winnerIndex != -1) {
                nextPlayer = winnerIndex + 1
            }
        }

        return copy(
            lines = newLines,
            triangles = newTriangles,
            currentPlayer = nextPlayer,
            playerScores = newScores,
            gameOver = isGameOver
        )
    }

    private fun findTrianglesForLine(lastLine: Line, allLines: List<Line>): List<Triangle> {
        val found = mutableListOf<Triangle>()
        val a = lastLine.startId
        val b = lastLine.endId
        
        for (p in points) {
            val c = p.id
            if (c == a || c == b) continue
            
            val hasAC = allLines.any { (it.startId == a && it.endId == c) || (it.startId == c && it.endId == a) }
            val hasBC = allLines.any { (it.startId == b && it.endId == c) || (it.startId == c && it.endId == b) }
            
            if (hasAC && hasBC) {
                if (!hasAnyPointInside(a, b, c) && !hasLinesCrossing(a, b, c, allLines)) {
                    found.add(Triangle(a, b, c, lastLine.player))
                }
            }
        }
        return found
    }

    fun getCpuMove(): Pair<Int, Int>? {
        return CpuIntelligence(this).getMove()
    }

    fun isValidMove(startId: Int, endId: Int, currentLines: List<Line> = lines): Boolean {
        if (startId == endId) return false
        if (currentLines.any { (it.startId == startId && it.endId == endId) || (it.startId == endId && it.endId == startId) }) return false

        val start = points.find { it.id == startId }?.position ?: return false
        val end = points.find { it.id == endId }?.position ?: return false

        if (currentLines.any { doLinesIntersect(start, end, points.find { p -> p.id == it.startId }?.position ?: Offset.Zero, points.find { p -> p.id == it.endId }?.position ?: Offset.Zero) }) return false
        if (points.any { it.id != startId && it.id != endId && isPointOnLineSegment(it.position, start, end) }) return false
        if (triangles.any { lineIntersectsTriangle(start, end, it) }) return false

        return true
    }

    private fun lineIntersectsTriangle(lineStart: Offset, lineEnd: Offset, triangle: Triangle): Boolean {
        val p1 = points.find { it.id == triangle.p1Id }?.position ?: return false
        val p2 = points.find { it.id == triangle.p2Id }?.position ?: return false
        val p3 = points.find { it.id == triangle.p3Id }?.position ?: return false
        val triangleIds = setOf(triangle.p1Id, triangle.p2Id, triangle.p3Id)
        val startId = points.find { it.position == lineStart }?.id
        val endId = points.find { it.position == lineEnd }?.id
        if (startId != null && endId != null && triangleIds.contains(startId) && triangleIds.contains(endId)) return false
        val edges = listOf(Pair(p1, p2), Pair(p2, p3), Pair(p3, p1))
        if (edges.any { doLinesIntersect(lineStart, lineEnd, it.first, it.second) }) return true
        if (isPointInsideTriangle(lineStart, p1, p2, p3) && isPointInsideTriangle(lineEnd, p1, p2, p3)) return true
        return false
    }

    internal fun hasAnyPointInside(p1Id: Int, p2Id: Int, p3Id: Int): Boolean {
        val p1 = points.find { it.id == p1Id }?.position ?: return false
        val p2 = points.find { it.id == p2Id }?.position ?: return false
        val p3 = points.find { it.id == p3Id }?.position ?: return false
        return points.any { it.id != p1Id && it.id != p2Id && it.id != p3Id && isPointInsideTriangle(it.position, p1, p2, p3) }
    }

    internal fun hasLinesCrossing(p1Id: Int, p2Id: Int, p3Id: Int, allLines: List<Line>): Boolean {
        val p1 = points.find { it.id == p1Id }?.position ?: return false
        val p2 = points.find { it.id == p2Id }?.position ?: return false
        val p3 = points.find { it.id == p3Id }?.position ?: return false
        val triangleEdges = listOf(Pair(p1, p2), Pair(p2, p3), Pair(p3, p1))
        val triangleIds = setOf(p1Id, p2Id, p3Id)
        return allLines.any { line ->
            if (triangleIds.contains(line.startId) && triangleIds.contains(line.endId)) return@any false
            val s = points.find { it.id == line.startId }?.position ?: return@any false
            val e = points.find { it.id == line.endId }?.position ?: return@any false
            triangleEdges.any { edge -> doLinesIntersect(s, e, edge.first, edge.second) } ||
            (isPointInsideTriangle(s, p1, p2, p3) && isPointInsideTriangle(e, p1, p2, p3))
        }
    }

    private fun isPointOnLineSegment(p: Offset, s: Offset, e: Offset): Boolean {
        val len = distance(s, e)
        if (len < 0.1f) return false
        val dist = kotlin.math.abs((e.y - s.y) * p.x - (e.x - s.x) * p.y + e.x * s.y - e.y * s.x) / len
        if (dist > 15f) return false
        val dot = (p.x - s.x) * (e.x - s.x) + (p.y - s.y) * (e.y - s.y)
        return dot > 0 && dot < len * len
    }

    private fun doLinesIntersect(p1: Offset, p2: Offset, p3: Offset, p4: Offset): Boolean {
        fun ccw(a: Offset, b: Offset, c: Offset) = (c.y - a.y) * (b.x - a.x) > (b.y - a.y) * (c.x - a.x)
        val intersect = ccw(p1, p3, p4) != ccw(p2, p3, p4) && ccw(p1, p2, p3) != ccw(p1, p2, p4)
        return if (intersect) distance(p1, p3) > 0.1f && distance(p1, p4) > 0.1f && distance(p2, p3) > 0.1f && distance(p2, p4) > 0.1f else false
    }

    private fun distance(p1: Offset, p2: Offset) = sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y))

    private fun isPointInsideTriangle(p: Offset, a: Offset, b: Offset, c: Offset): Boolean {
        val area = kotlin.math.abs((b.x - a.x) * (c.y - a.y) - (c.x - a.x) * (b.y - a.y))
        val a1 = kotlin.math.abs((a.x - p.x) * (b.y - p.y) - (b.x - p.x) * (a.y - p.y))
        val a2 = kotlin.math.abs((b.x - p.x) * (c.y - p.y) - (c.x - p.x) * (b.y - p.y))
        val a3 = kotlin.math.abs((c.x - p.x) * (a.y - p.y) - (a.x - p.x) * (c.y - p.y))
        return kotlin.math.abs(a1 + a2 + a3 - area) < 1f
    }

    private fun hasValidMovesLeft(currentLines: List<Line>, currentTriangles: List<Triangle>): Boolean {
        val testState = this.copy(lines = currentLines, triangles = currentTriangles)
        for (i in points.indices) {
            for (j in i + 1 until points.size) {
                if (testState.isValidMove(points[i].id, points[j].id)) return true
            }
        }
        return false
    }

    fun getWinnerMessage(isCpuGame: Boolean): String {
        val maxScore = playerScores.maxOrNull() ?: 0
        val winners = playerScores.indices.filter { playerScores[it] == maxScore }
        return when {
            winners.size > 1 -> "Empate! 🤝"
            isCpuGame && winners[0] == 1 -> "CPU venceu! 🤖"
            else -> "${playerNames[winners[0]]} venceu! 🎉"
        }
    }
}

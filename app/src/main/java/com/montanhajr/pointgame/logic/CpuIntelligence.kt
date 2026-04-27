package com.montanhajr.pointgame.logic

import com.montanhajr.pointgame.models.Difficulty
import com.montanhajr.pointgame.models.GameType
import com.montanhajr.pointgame.models.Line
import kotlin.random.Random

class CpuIntelligence(private val gameState: GameState) {

    fun getMove(): Pair<Int, Int>? {
        return when (gameState.difficulty) {
            Difficulty.EASY -> getCpuMoveEasy()
            Difficulty.MEDIUM -> getCpuMoveMedium()
            Difficulty.HARD -> getCpuMoveHard()
        }
    }

    private fun getCpuMoveEasy(): Pair<Int, Int>? {
        val allMoves = getAllValidMoves()
        return if (allMoves.isEmpty()) null else allMoves.random()
    }

    private fun getCpuMoveMedium(): Pair<Int, Int>? {
        if (Random.nextFloat() < 0.3f) return getCpuMoveEasy()
        return findScoringMove() ?: getCpuMoveEasy()
    }

    private fun getCpuMoveHard(): Pair<Int, Int>? {
        val allMoves = getAllValidMoves()
        if (allMoves.isEmpty()) return null

        // 1. Tentar completar um ponto (Triângulo ou Quadrado)
        var bestMove: Pair<Int, Int>? = null
        for (move in allMoves) {
            if (wouldMoveCompleteScore(move.first, move.second, gameState.lines)) {
                return move
            }
        }

        // 2. Bloquear oponente (se ele puder completar no próximo turno)
        for (move in allMoves) {
            if (wouldMoveCompleteScore(move.first, move.second, gameState.lines, playerForSim = 1)) {
                return move
            }
        }

        // 3. Filtrar movimentos perigosos
        val safeMoves = allMoves.filter { !isMoveDangerous(it) }

        if (safeMoves.isNotEmpty()) {
            return safeMoves.random()
        }

        // 4. Se todos forem perigosos, escolher o menos pior
        var minOpened = Int.MAX_VALUE
        var selectedMove = allMoves[0]
        
        for (move in allMoves) {
            val opened = countScoresOpenedByMove(move)
            if (opened < minOpened) {
                minOpened = opened
                selectedMove = move
            }
        }
        
        return selectedMove
    }

    private fun getAllValidMoves(): List<Pair<Int, Int>> {
        val moves = mutableListOf<Pair<Int, Int>>()
        val points = gameState.points
        for (i in points.indices) {
            val p1Id = points[i].id
            for (j in i + 1 until points.size) {
                val p2Id = points[j].id
                if (gameState.isValidMove(p1Id, p2Id)) {
                    moves.add(p1Id to p2Id)
                }
            }
        }
        return moves
    }

    private fun findScoringMove(): Pair<Int, Int>? {
        val allMoves = getAllValidMoves()
        for (move in allMoves) {
            if (wouldMoveCompleteScore(move.first, move.second, gameState.lines)) {
                return move
            }
        }
        return null
    }

    private fun wouldMoveCompleteScore(
        startId: Int, 
        endId: Int, 
        currentLines: List<Line>, 
        playerForSim: Int = gameState.currentPlayer
    ): Boolean {
        return if (gameState.gameType == GameType.TRIANGLES) {
            wouldMoveCompleteTriangle(startId, endId, currentLines, playerForSim)
        } else {
            wouldMoveCompleteSquare(startId, endId, currentLines, playerForSim)
        }
    }

    private fun wouldMoveCompleteTriangle(
        startId: Int, 
        endId: Int, 
        currentLines: List<Line>, 
        playerForSim: Int
    ): Boolean {
        val points = gameState.points
        for (p in points) {
            val cId = p.id
            if (cId == startId || cId == endId) continue
            
            val hasAC = currentLines.any { (it.startId == startId && it.endId == cId) || (it.startId == cId && it.endId == startId) }
            if (!hasAC) continue
            
            val hasBC = currentLines.any { (it.startId == endId && it.endId == cId) || (it.startId == cId && it.endId == endId) }
            if (hasBC) {
                if (!gameState.hasAnyPointInside(startId, endId, cId) && 
                    !gameState.hasLinesCrossing(startId, endId, cId, currentLines + Line(startId, endId, playerForSim))) {
                    return true
                }
            }
        }
        return false
    }

    private fun wouldMoveCompleteSquare(
        startId: Int, 
        endId: Int, 
        currentLines: List<Line>, 
        playerForSim: Int
    ): Boolean {
        val p1 = gameState.points.find { it.id == startId }?.position ?: return false
        val p2 = gameState.points.find { it.id == endId }?.position ?: return false
        
        // Em um grid, se desenharmos uma linha, um quadrado pode ser formado 'acima' ou 'abaixo' dela (ou esquerda/direita)
        // Procuramos por dois pontos que completem o quadrado com startId e endId.
        val points = gameState.points
        for (i in points.indices) {
            for (j in i + 1 until points.size) {
                val p3Id = points[i].id
                val p4Id = points[j].id
                if (p3Id == startId || p3Id == endId || p4Id == startId || p4Id == endId) continue
                
                val p3 = points[i].position
                val p4 = points[j].position
                
                // Verificar se formam um quadrado (mesma distância)
                val d12 = dist(p1, p2)
                val d13 = dist(p1, p3)
                val d24 = dist(p2, p4)
                val d34 = dist(p3, p4)
                
                if (kotlin.math.abs(d12 - d13) < 5f && kotlin.math.abs(d12 - d24) < 5f && kotlin.math.abs(d12 - d34) < 5f) {
                    val has13 = currentLines.any { (it.startId == startId && it.endId == p3Id) || (it.startId == p3Id && it.endId == startId) }
                    val has24 = currentLines.any { (it.startId == endId && it.endId == p4Id) || (it.startId == p4Id && it.endId == endId) }
                    val has34 = currentLines.any { (it.startId == p3Id && it.endId == p4Id) || (it.startId == p4Id && it.endId == p3Id) }
                    
                    if (has13 && has24 && has34) return true
                }
            }
        }
        return false
    }

    private fun dist(a: androidx.compose.ui.geometry.Offset, b: androidx.compose.ui.geometry.Offset): Float {
        return kotlin.math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y))
    }

    private fun isMoveDangerous(move: Pair<Int, Int>): Boolean {
        val (a, b) = move
        if (wouldMoveCompleteScore(a, b, gameState.lines)) return false

        val nextLines = gameState.lines + Line(a, b, gameState.currentPlayer)
        val allMoves = getAllValidMoves() // Simplificado para CPU não ficar lenta
        
        for (m in allMoves) {
            // Se o oponente (player 1) puder completar um score após esse movimento
            if (wouldMoveCompleteScore(m.first, m.second, nextLines, playerForSim = 1)) {
                return true
            }
        }
        return false
    }

    private fun countScoresOpenedByMove(move: Pair<Int, Int>): Int {
        val (a, b) = move
        var count = 0
        val nextLines = gameState.lines + Line(a, b, gameState.currentPlayer)
        val allMoves = getAllValidMoves()
        
        for (m in allMoves) {
            if (wouldMoveCompleteScore(m.first, m.second, nextLines, playerForSim = 1)) count++
        }
        return count
    }
}

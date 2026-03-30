package com.montanhajr.pointgame.logic

import com.montanhajr.pointgame.models.Difficulty
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
        return findTriangleCompletingMove() ?: getCpuMoveEasy()
    }

    private fun getCpuMoveHard(): Pair<Int, Int>? {
        val allMoves = getAllValidMoves()
        if (allMoves.isEmpty()) return null

        // 1. Tentar completar um triângulo (Prioridade máxima)
        // Otimização: Já temos wouldMoveCompleteTriangle que é relativamente rápido
        var bestMove: Pair<Int, Int>? = null
        for (move in allMoves) {
            if (wouldMoveCompleteTriangle(move.first, move.second, gameState.lines)) {
                return move
            }
        }

        // 2. Bloquear oponente (se ele puder completar um triângulo no próximo turno)
        // Simulando como se fosse o jogador 1
        for (move in allMoves) {
            if (wouldMoveCompleteTriangle(move.first, move.second, gameState.lines, playerForSim = 1)) {
                return move
            }
        }

        // 3. Filtrar movimentos perigosos e escolher o melhor
        // Movimento perigoso é aquele que permite ao oponente completar um triângulo
        val safeMoves = mutableListOf<Pair<Int, Int>>()
        for (move in allMoves) {
            if (!isMoveDangerous(move)) {
                safeMoves.add(move)
            }
        }

        if (safeMoves.isNotEmpty()) {
            // Escolhe um movimento seguro que maximize o potencial futuro (heurística simples)
            // Para economizar hardware, vamos evitar countPotentialTriangles que é O(N^3)
            // Em vez disso, apenas pegamos um aleatório dos seguros ou o primeiro.
            return safeMoves.random()
        }

        // 4. Se todos forem perigosos, escolher o que abre menos triângulos para o oponente
        var minOpened = Int.MAX_VALUE
        var selectedMove = allMoves[0]
        
        for (move in allMoves) {
            val opened = countTrianglesOpenedByMove(move)
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

    private fun findTriangleCompletingMove(): Pair<Int, Int>? {
        val points = gameState.points
        for (i in points.indices) {
            val p1Id = points[i].id
            for (j in i + 1 until points.size) {
                val p2Id = points[j].id
                if (gameState.isValidMove(p1Id, p2Id)) {
                    if (wouldMoveCompleteTriangle(p1Id, p2Id, gameState.lines)) {
                        return p1Id to p2Id
                    }
                }
            }
        }
        return null
    }

    private fun wouldMoveCompleteTriangle(
        startId: Int, 
        endId: Int, 
        currentLines: List<Line>, 
        playerForSim: Int = gameState.currentPlayer
    ): Boolean {
        val points = gameState.points
        // Otimização: usar um Set para busca rápida de linhas existentes
        // No entanto, para o número pequeno de linhas, List.any costuma ser rápido o suficiente.
        // Mas vamos otimizar criando uma representação de adjacência se necessário.
        
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

    private fun isMoveDangerous(move: Pair<Int, Int>): Boolean {
        val (a, b) = move
        // Se este movimento completa um triângulo para a CPU, não é perigoso para a CPU
        if (wouldMoveCompleteTriangle(a, b, gameState.lines)) return false

        val nextLines = gameState.lines + Line(a, b, gameState.currentPlayer)
        val points = gameState.points
        
        for (p in points) {
            val c = p.id
            if (c == a || c == b) continue
            
            val hasAC = nextLines.any { (it.startId == a && it.endId == c) || (it.startId == c && it.endId == a) }
            val hasBC = nextLines.any { (it.startId == b && it.endId == c) || (it.startId == c && it.endId == b) }
            
            if (hasAC && gameState.isValidMove(b, c, nextLines) && wouldMoveCompleteTriangle(b, c, nextLines, playerForSim = 1)) return true
            if (hasBC && gameState.isValidMove(a, c, nextLines) && wouldMoveCompleteTriangle(a, c, nextLines, playerForSim = 1)) return true
        }
        return false
    }

    private fun countTrianglesOpenedByMove(move: Pair<Int, Int>): Int {
        val (a, b) = move
        var count = 0
        val nextLines = gameState.lines + Line(a, b, gameState.currentPlayer)
        val points = gameState.points
        
        for (p in points) {
            val c = p.id
            if (c == a || c == b) continue
            val hasAC = nextLines.any { (it.startId == a && it.endId == c) || (it.startId == c && it.endId == a) }
            val hasBC = nextLines.any { (it.startId == b && it.endId == c) || (it.startId == c && it.endId == b) }
            
            if (hasAC && gameState.isValidMove(b, c, nextLines) && wouldMoveCompleteTriangle(b, c, nextLines, playerForSim = 1)) count++
            if (hasBC && gameState.isValidMove(a, c, nextLines) && wouldMoveCompleteTriangle(a, c, nextLines, playerForSim = 1)) count++
        }
        return count
    }
}

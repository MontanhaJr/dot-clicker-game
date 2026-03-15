package com.montanhajr.pointgame.models

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class DotPoint(val id: Int, val position: Offset)
data class Line(val startId: Int, val endId: Int, val player: Int)
data class Triangle(val p1Id: Int, val p2Id: Int, val p3Id: Int, val owner: Int)

enum class GameMode {
    TWO_PLAYERS,
    VS_CPU,
    MULTIPLAYER
}

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD
}

val PlayerColors = listOf(
    Color(0xFF2196F3), // Azul
    Color(0xFFE91E63), // Rosa
    Color(0xFF4CAF50), // Verde
    Color(0xFFFF9800), // Laranja
    Color(0xFF9C27B0), // Roxo
    Color(0xFF795548), // Marrom
    Color(0xFF00BCD4), // Ciano
    Color(0xFFCDDC39), // Lima
    Color(0xFF673AB7), // Roxo Escuro
    Color(0xFF607D8B)  // Azul Acinzentado
)

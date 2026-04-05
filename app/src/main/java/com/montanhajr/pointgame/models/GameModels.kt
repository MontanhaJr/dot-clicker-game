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

enum class BoardStyle {
    GALAXY,
    NEON_NIGHT,
    MINIMALIST_WHITE,
    RETRO_ARCADE,
    PAPER_NOTEBOOK,
    CHALKBOARD,
    CYBERPUNK_GLITCH,
    ANCIENT_SCROLL,
    DEEP_SEA,
    FOUNDER_GOLD // Estilo especial para pioneiros
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
    Color(0xFF607D8B)  // Azul Acinzetado
)

fun getStylePlayerColor(style: BoardStyle, playerIndex: Int, isCpuGame: Boolean): Color {
    val cpuColor = when (style) {
        BoardStyle.MINIMALIST_WHITE -> Color(0xFF666666)
        BoardStyle.PAPER_NOTEBOOK -> Color(0xFFFF0000)
        BoardStyle.RETRO_ARCADE -> Color(0xFFFF00FF)
        BoardStyle.CHALKBOARD -> Color(0xFFFFD700)
        BoardStyle.CYBERPUNK_GLITCH -> Color(0xFFFF0055)
        BoardStyle.ANCIENT_SCROLL -> Color(0xFF8B7310)
        BoardStyle.DEEP_SEA -> Color(0xFFFFB703)
        BoardStyle.FOUNDER_GOLD -> Color(0xFFE5E4E2) // Platina para CPU
        else -> Color(0xFFE0E0E0)
    }

    if (isCpuGame && playerIndex == 2) return cpuColor

    return when (style) {
        BoardStyle.PAPER_NOTEBOOK -> if (playerIndex == 1) Color(0xFF005BAC) else PlayerColors[(playerIndex - 1) % PlayerColors.size]
        BoardStyle.RETRO_ARCADE -> if (playerIndex == 1) Color(0xFF00FFFF) else PlayerColors[(playerIndex - 1) % PlayerColors.size]
        BoardStyle.CHALKBOARD -> if (playerIndex == 1) Color(0xFFFFFFFF) else PlayerColors[(playerIndex - 1) % PlayerColors.size]
        BoardStyle.CYBERPUNK_GLITCH -> if (playerIndex == 1) Color(0xFFBB00FF) else PlayerColors[(playerIndex - 1) % PlayerColors.size]
        BoardStyle.ANCIENT_SCROLL -> {
            when (playerIndex) {
                1 -> Color(0xFF3E2723)
                2 -> Color(0xFF8B7310)
                else -> PlayerColors[(playerIndex - 1) % PlayerColors.size]
            }
        }
        BoardStyle.DEEP_SEA -> if (playerIndex == 1) Color(0xFFE9D8A6) else PlayerColors[(playerIndex - 1) % PlayerColors.size]
        BoardStyle.FOUNDER_GOLD -> {
            when (playerIndex) {
                1 -> Color(0xFFFFD700) // Ouro
                2 -> Color(0xFFE5E4E2) // Prata/Platina
                else -> PlayerColors[(playerIndex - 1) % PlayerColors.size]
            }
        }
        else -> PlayerColors[(playerIndex - 1) % PlayerColors.size]
    }
}

fun getStyleUiColors(style: BoardStyle): UiThemeColors {
    return when (style) {
        BoardStyle.MINIMALIST_WHITE -> UiThemeColors(
            headerBg = Color(0xFFF0F0F0).copy(alpha = 0.7f),
            text = Color(0xFF1A1A2E),
            isDark = false
        )
        BoardStyle.PAPER_NOTEBOOK -> UiThemeColors(
            headerBg = Color(0xFFFCF5E5).copy(alpha = 0.7f),
            text = Color(0xFF333333),
            isDark = false
        )
        BoardStyle.ANCIENT_SCROLL -> UiThemeColors(
            headerBg = Color(0xFFD4B483).copy(alpha = 0.7f),
            text = Color(0xFF3E2723),
            isDark = false
        )
        BoardStyle.CHALKBOARD -> UiThemeColors(
            headerBg = Color(0xFF1B2621).copy(alpha = 0.7f),
            text = Color.White,
            isDark = true
        )
        BoardStyle.FOUNDER_GOLD -> UiThemeColors(
            headerBg = Color(0xFF1A1A1A).copy(alpha = 0.7f),
            text = Color(0xFFFFD700),
            isDark = true
        )
        else -> UiThemeColors(
            headerBg = Color(0xFF1A1A2E).copy(alpha = 0.7f),
            text = Color.White,
            isDark = true
        )
    }
}

data class UiThemeColors(
    val headerBg: Color,
    val text: Color,
    val isDark: Boolean
)

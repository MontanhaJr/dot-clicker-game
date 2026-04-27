package com.montanhajr.pointgame.models

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.montanhajr.pointgame.ui.theme.*

data class DotPoint(val id: Int, val position: Offset)
data class Line(val startId: Int, val endId: Int, val player: Int)
data class Triangle(val p1Id: Int, val p2Id: Int, val p3Id: Int, val owner: Int)
data class Square(val p1Id: Int, val p2Id: Int, val p3Id: Int, val p4Id: Int, val owner: Int)

enum class GameMode {
    TWO_PLAYERS,
    VS_CPU,
    MULTIPLAYER
}

enum class GameType {
    TRIANGLES,
    SQUARES
}

enum class Difficulty {
    EASY,
    MEDIUM,
    HARD
}

enum class BoardStyle {
    DEFAULT_POP,
    GALAXY,
    NEON_NIGHT,
    MINIMALIST_WHITE,
    RETRO_ARCADE,
    PAPER_NOTEBOOK,
    CHALKBOARD,
    CYBERPUNK_GLITCH,
    ANCIENT_SCROLL,
    DEEP_SEA,
    FOUNDER_GOLD
}

val PlayerColors = listOf(
    Color(0xFF2196F3), // Azul
    Color(0xFFE91E63), // Rosa
    PopGreen,          // Verde Pop
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
        BoardStyle.DEFAULT_POP -> PopRed
        BoardStyle.MINIMALIST_WHITE -> Color(0xFF666666)
        BoardStyle.PAPER_NOTEBOOK -> Color(0xFFFF0000)
        BoardStyle.RETRO_ARCADE -> Color(0xFFFF00FF)
        BoardStyle.CHALKBOARD -> Color(0xFFFFD700)
        BoardStyle.CYBERPUNK_GLITCH -> Color(0xFFFF0055)
        BoardStyle.ANCIENT_SCROLL -> Color(0xFF8B7310)
        BoardStyle.DEEP_SEA -> Color(0xFFFFB703)
        BoardStyle.FOUNDER_GOLD -> Color(0xFFD3D3D3)
        else -> Color(0xFFE0E0E0)
    }

    if (isCpuGame && playerIndex == 2) return cpuColor

    return when (style) {
        BoardStyle.DEFAULT_POP -> if (playerIndex == 1) PopGreen else if (playerIndex == 2) PopRed else PlayerColors[(playerIndex - 1) % PlayerColors.size]
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
                1 -> Color(0xFFB8860B)
                2 -> Color(0xFFD3D3D3)
                else -> PlayerColors[(playerIndex - 1) % PlayerColors.size]
            }
        }
        else -> PlayerColors[(playerIndex - 1) % PlayerColors.size]
    }
}

fun getStyleUiColors(style: BoardStyle): UiThemeColors {
    return when (style) {
        BoardStyle.DEFAULT_POP -> UiThemeColors(
            headerBg = PopDarkBlue.copy(alpha = 0.8f),
            text = PopWhite,
            isDark = true,
            eagleEye = PopYellow,
            xRay = PopCyan
        )
        BoardStyle.MINIMALIST_WHITE -> UiThemeColors(
            headerBg = Color(0xFFF0F0F0).copy(alpha = 0.7f),
            text = Color(0xFF1A1A2E),
            isDark = false,
            eagleEye = Color(0xFFFF5722),
            xRay = Color(0xFF2196F3)
        )
        BoardStyle.PAPER_NOTEBOOK -> UiThemeColors(
            headerBg = Color(0xFFFCF5E5).copy(alpha = 0.7f),
            text = Color(0xFF333333),
            isDark = false,
            eagleEye = Color(0xFFD32F2F),
            xRay = Color(0xFF1976D2)
        )
        BoardStyle.ANCIENT_SCROLL -> UiThemeColors(
            headerBg = Color(0xFFD4B483).copy(alpha = 0.7f),
            text = Color(0xFF3E2723),
            isDark = false,
            eagleEye = Color(0xFF5D4037),
            xRay = Color(0xFF0D47A1)
        )
        BoardStyle.CHALKBOARD -> UiThemeColors(
            headerBg = Color(0xFF1B2621).copy(alpha = 0.7f),
            text = Color.White,
            isDark = true,
            eagleEye = Color(0xFFFFEB3B),
            xRay = Color.White
        )
        BoardStyle.FOUNDER_GOLD -> UiThemeColors(
            headerBg = Color(0xFF1A1A1A).copy(alpha = 0.7f),
            text = Color(0xFFFFD700),
            isDark = true,
            eagleEye = Color(0xFF00E5FF),
            xRay = Color.White
        )
        BoardStyle.CYBERPUNK_GLITCH -> UiThemeColors(
            headerBg = Color(0xFF1A1A2E).copy(alpha = 0.7f),
            text = Color.White,
            isDark = true,
            eagleEye = Color(0xFF00FF00),
            xRay = Color(0xFFFF00FF)
        )
        BoardStyle.RETRO_ARCADE -> UiThemeColors(
            headerBg = Color(0xFF1A1A2E).copy(alpha = 0.7f),
            text = Color.White,
            isDark = true,
            eagleEye = Color(0xFFFFEB3B),
            xRay = Color(0xFF00FFFF)
        )
        else -> UiThemeColors(
            headerBg = Color(0xFF1A1A2E).copy(alpha = 0.7f),
            text = Color.White,
            isDark = true,
            eagleEye = Color(0xFF00FFFF),
            xRay = Color.White
        )
    }
}

data class UiThemeColors(
    val headerBg: Color,
    val text: Color,
    val isDark: Boolean,
    val eagleEye: Color,
    val xRay: Color
)

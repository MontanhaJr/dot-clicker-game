package com.montanhajr.pointgame.models

import com.montanhajr.pointgame.R

enum class PowerUpType(
    val displayName: String,
    val iconRes: Int,
    val description: String
) {
    UNDO("UNDO", R.drawable.undo_icon, "Desfaz a última jogada"),
    EAGLE_EYE("LUPA", R.drawable.magnifier_icon, "Revela triângulos fecháveis"),
    XRAY_VISION("RAIO-X", R.drawable.eye_icon, "Mostra todas as conexões"),
    AUTO_SNAP("IMÃ", R.drawable.magnetic_icon, "Fecha um triângulo aleatório"),
    DOUBLE_MOVE("DUPLO", R.drawable.double_move_icon, "Jogue duas vezes"),
    FREEZE_CPU("GELO", R.drawable.freeze_icon, "Congela a CPU por 1 turno"),
    ERASER("BORRACHA", R.drawable.erase_icon, "Apaga qualquer linha do tabuleiro"),
    GOLDEN_TRIANGLE("OURO", R.drawable.golden_tri_icon, "Triângulos valem 3x"),
    PROTECTION_SHIELD("ESCUDO", R.drawable.shield_icon, "CPU não pode pontuar")
}

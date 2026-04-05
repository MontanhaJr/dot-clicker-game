package com.montanhajr.pointgame.models

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val requiredProgress: Int,
    val isCompleted: Boolean
)

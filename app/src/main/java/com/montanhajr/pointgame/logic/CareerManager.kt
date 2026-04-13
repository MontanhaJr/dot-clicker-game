package com.montanhajr.pointgame.logic

import android.content.Context
import com.montanhajr.pointgame.models.Difficulty
import java.util.concurrent.TimeUnit

class CareerManager(context: Context) {
    private val prefs = context.getSharedPreferences("career_progression", Context.MODE_PRIVATE)

    fun getCurrentLevel(): Int = prefs.getInt("current_level", 1)

    fun completeLevel() {
        val nextLevel = getCurrentLevel() + 1
        prefs.edit().putInt("current_level", nextLevel).apply()
    }

    // Lógica de Dificuldade Progressiva
    fun getDifficultyForLevel(level: Int): Difficulty {
        return when {
            level <= 5 -> Difficulty.EASY
            level <= 15 -> Difficulty.MEDIUM
            else -> Difficulty.HARD
        }
    }

    fun getPointsCountForLevel(level: Int): Int {
        return (15 + (level / 2)).coerceAtMost(40)
    }

    // Lógica de Retenção Diária
    fun checkDailyLogin(): Int {
        val lastLogin = prefs.getLong("last_login_ts", 0L)
        val today = System.currentTimeMillis()
        val diff = today - lastLogin
        val daysDiff = TimeUnit.MILLISECONDS.toDays(diff)

        var streak = prefs.getInt("daily_streak", 0)

        if (daysDiff == 1L) {
            streak++
        } else if (daysDiff > 1L) {
            streak = 1
        } else if (lastLogin == 0L) {
            streak = 1
        }

        prefs.edit()
            .putLong("last_login_ts", today)
            .putInt("daily_streak", streak)
            .apply()

        return streak
    }
}

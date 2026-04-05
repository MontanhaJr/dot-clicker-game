package com.montanhajr.pointgame.logic

import android.content.Context
import com.montanhajr.pointgame.models.Achievement
import com.montanhajr.pointgame.models.Difficulty
import java.util.Calendar

class StatisticsManager(context: Context) {
    private val prefs = context.getSharedPreferences("game_stats", Context.MODE_PRIVATE)

    fun addTriangles(count: Int) {
        val current = prefs.getInt("total_triangles", 0)
        prefs.edit().putInt("total_triangles", current + count).apply()
    }

    fun recordMatch(difficulty: Difficulty?, result: MatchResult, timeMs: Long) {
        val editor = prefs.edit()
        
        if (difficulty != null) {
            val key = when (result) {
                MatchResult.WIN -> "wins_${difficulty.name.lowercase()}"
                MatchResult.LOSS -> "losses_${difficulty.name.lowercase()}"
                MatchResult.DRAW -> "draws_${difficulty.name.lowercase()}"
            }
            editor.putInt(key, prefs.getInt(key, 0) + 1)
        }

        val totalMatches = prefs.getLong("total_matches", 0) + 1
        editor.putLong("total_matches", totalMatches)
        editor.putLong("total_time_ms", prefs.getLong("total_time_ms", 0) + timeMs)
        
        // Lógica de Conquista: Founder
        checkFounderAchievement(totalMatches, editor)
        
        editor.apply()
    }

    private fun checkFounderAchievement(totalMatches: Long, editor: android.content.SharedPreferences.Editor) {
        if (prefs.getBoolean("achievement_founder", false)) return

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        // Se jogou 10 partidas durante o ano de lançamento (2025/2026 conforme o footer)
        if (currentYear <= 2026 && totalMatches >= 10) {
            editor.putBoolean("achievement_founder", true)
        }
    }

    fun isFounderUnlocked(): Boolean = prefs.getBoolean("achievement_founder", false)

    fun getAchievements(): List<Achievement> {
        val totalMatches = prefs.getLong("total_matches", 0).toInt()
        val isFounderCompleted = isFounderUnlocked()
        
        return listOf(
            Achievement(
                id = "founder",
                title = "Founder",
                description = "Play 10 matches during the launch year (until end of 2026) to unlock the Founder Gold style.",
                currentProgress = totalMatches.coerceAtMost(10),
                requiredProgress = 10,
                isCompleted = isFounderCompleted
            )
            // Futuras conquistas podem ser adicionadas aqui
        )
    }

    fun getStats(): GameStats {
        return GameStats(
            totalTriangles = prefs.getInt("total_triangles", 0),
            totalMatches = prefs.getLong("total_matches", 0),
            isFounderUnlocked = isFounderUnlocked(),
            easyStats = getDifficultyStats(Difficulty.EASY),
            mediumStats = getDifficultyStats(Difficulty.MEDIUM),
            hardStats = getDifficultyStats(Difficulty.HARD),
            avgTimeMs = if (prefs.getLong("total_matches", 0) > 0) {
                prefs.getLong("total_time_ms", 0) / prefs.getLong("total_matches", 0)
            } else 0L
        )
    }

    private fun getDifficultyStats(difficulty: Difficulty): DifficultyStats {
        val wins = prefs.getInt("wins_${difficulty.name.lowercase()}", 0)
        val losses = prefs.getInt("losses_${difficulty.name.lowercase()}", 0)
        val draws = prefs.getInt("draws_${difficulty.name.lowercase()}", 0)
        val total = wins + losses + draws
        val winRate = if (total > 0) (wins.toFloat() / total * 100).toInt() else 0
        return DifficultyStats(wins, losses, draws, winRate)
    }

    enum class MatchResult { WIN, LOSS, DRAW }

    data class GameStats(
        val totalTriangles: Int,
        val totalMatches: Long,
        val isFounderUnlocked: Boolean,
        val easyStats: DifficultyStats,
        val mediumStats: DifficultyStats,
        val hardStats: DifficultyStats,
        val avgTimeMs: Long
    )

    data class DifficultyStats(
        val wins: Int,
        val losses: Int,
        val draws: Int,
        val winRate: Int
    )
}

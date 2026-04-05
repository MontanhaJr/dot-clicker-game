package com.montanhajr.pointgame.logic

import android.content.Context
import com.montanhajr.pointgame.models.Difficulty

class StatisticsManager(context: Context) {
    private val prefs = context.getSharedPreferences("game_stats", Context.MODE_PRIVATE)

    fun addTriangles(count: Int) {
        val current = prefs.getInt("total_triangles", 0)
        prefs.edit().putInt("total_triangles", current + count).apply()
    }

    fun recordMatch(difficulty: Difficulty?, result: MatchResult, timeMs: Long) {
        val editor = prefs.edit()
        
        // Record difficulty specific stats for CPU games
        if (difficulty != null) {
            val key = when (result) {
                MatchResult.WIN -> "wins_${difficulty.name.lowercase()}"
                MatchResult.LOSS -> "losses_${difficulty.name.lowercase()}"
                MatchResult.DRAW -> "draws_${difficulty.name.lowercase()}"
            }
            editor.putInt(key, prefs.getInt(key, 0) + 1)
        }

        // Global stats for average time
        editor.putLong("total_matches", prefs.getLong("total_matches", 0) + 1)
        editor.putLong("total_time_ms", prefs.getLong("total_time_ms", 0) + timeMs)
        
        editor.apply()
    }

    fun getStats(): GameStats {
        return GameStats(
            totalTriangles = prefs.getInt("total_triangles", 0),
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

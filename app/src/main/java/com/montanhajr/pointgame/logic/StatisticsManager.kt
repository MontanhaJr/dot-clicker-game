package com.montanhajr.pointgame.logic

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.games.PlayGames
import com.montanhajr.pointgame.BuildConfig
import com.montanhajr.pointgame.models.Achievement
import com.montanhajr.pointgame.models.Difficulty
import java.util.Calendar

class StatisticsManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("game_stats", Context.MODE_PRIVATE)

    companion object {
        const val ACHIEVEMENT_FOUNDER_ID = BuildConfig.ACHIEVEMENT_FOUNDER_ID
    }

    fun addTriangles(count: Int) {
        val current = prefs.getInt("total_triangles", 0)
        prefs.edit().putInt("total_triangles", current + count).apply()
    }

    fun addSquares(count: Int) {
        val current = prefs.getInt("total_squares", 0)
        prefs.edit().putInt("total_squares", current + count).apply()
    }

    fun recordMatch(difficulty: Difficulty?, result: MatchResult, timeMs: Long, isTraining: Boolean = true): String? {
        val editor = prefs.edit()
        
        if (difficulty != null && isTraining) {
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
        
        val newlyUnlocked = checkLocalFounderAchievement(totalMatches, editor)
        syncMatchToGooglePlay(totalMatches)
        editor.putBoolean("ad_pending", true)
        editor.apply()
        
        return newlyUnlocked
    }

    private fun checkLocalFounderAchievement(totalMatches: Long, editor: android.content.SharedPreferences.Editor): String? {
        if (prefs.getBoolean("achievement_founder", false)) return null
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        if (currentYear <= 2026 && totalMatches >= 10) {
            editor.putBoolean("achievement_founder", true)
            return "Founder"
        }
        return null
    }

    private fun syncMatchToGooglePlay(totalMatches: Long) {
        val activity = context as? Activity ?: return
        try {
            PlayGames.getAchievementsClient(activity).setSteps(ACHIEVEMENT_FOUNDER_ID, totalMatches.toInt().coerceAtMost(10))
        } catch (e: Exception) {
            Log.e("StatsManager", "Play Games Sync failed: ${e.message}")
        }
    }

    fun syncFromCloud(onComplete: () -> Unit = {}) {
        val activity = context as? Activity ?: return
        PlayGames.getAchievementsClient(activity).load(true).addOnSuccessListener { annotatedData ->
            val buffer = annotatedData.get()
            if (buffer != null) {
                try {
                    val editor = prefs.edit()
                    for (ach in buffer) {
                        if (ach.achievementId == ACHIEVEMENT_FOUNDER_ID && ach.state == com.google.android.gms.games.achievement.Achievement.STATE_UNLOCKED) {
                            editor.putBoolean("achievement_founder", true)
                        }
                    }
                    editor.apply()
                } finally {
                    buffer.release()
                }
            }
            onComplete()
        }.addOnFailureListener {
            onComplete()
        }
    }

    fun isAdPending(): Boolean = prefs.getBoolean("ad_pending", false)
    fun setAdPending(pending: Boolean) { prefs.edit().putBoolean("ad_pending", pending).apply() }
    fun incrementRestartCount(): Int {
        val count = prefs.getInt("restart_count", 0) + 1
        prefs.edit().putInt("restart_count", count).apply()
        return count
    }
    fun resetRestartCount() { prefs.edit().putInt("restart_count", 0).apply() }
    fun isFounderUnlocked(): Boolean = prefs.getBoolean("achievement_founder", false)

    fun getAchievements(): List<Achievement> {
        val totalMatches = prefs.getLong("total_matches", 0).toInt()
        return listOf(
            Achievement(
                id = "founder",
                title = "Founder",
                description = "Play 10 matches during the launch year (until end of 2026) to unlock the Founder Gold style.",
                currentProgress = totalMatches.coerceAtMost(10),
                requiredProgress = 10,
                isCompleted = isFounderUnlocked()
            )
        )
    }

    fun getStats(): GameStats {
        val totalMatches = prefs.getLong("total_matches", 0)
        return GameStats(
            totalTriangles = prefs.getInt("total_triangles", 0),
            totalSquares = prefs.getInt("total_squares", 0),
            totalMatches = totalMatches,
            isFounderUnlocked = isFounderUnlocked(),
            easyStats = getDifficultyStats(Difficulty.EASY),
            mediumStats = getDifficultyStats(Difficulty.MEDIUM),
            hardStats = getDifficultyStats(Difficulty.HARD),
            avgTimeMs = if (totalMatches > 0) prefs.getLong("total_time_ms", 0) / totalMatches else 0L
        )
    }

    private fun getDifficultyStats(difficulty: Difficulty): DifficultyStats {
        val wins = prefs.getInt("wins_${difficulty.name.lowercase()}", 0)
        val losses = prefs.getInt("losses_${difficulty.name.lowercase()}", 0)
        val draws = prefs.getInt("draws_${difficulty.name.lowercase()}", 0)
        val total = wins + losses + draws
        return DifficultyStats(wins, losses, draws, if (total > 0) (wins.toFloat() / total * 100).toInt() else 0)
    }

    enum class MatchResult { WIN, LOSS, DRAW }
    data class GameStats(
        val totalTriangles: Int,
        val totalSquares: Int,
        val totalMatches: Long,
        val isFounderUnlocked: Boolean,
        val easyStats: DifficultyStats,
        val mediumStats: DifficultyStats,
        val hardStats: DifficultyStats,
        val avgTimeMs: Long
    )
    data class DifficultyStats(val wins: Int, val losses: Int, val draws: Int, val winRate: Int)
}

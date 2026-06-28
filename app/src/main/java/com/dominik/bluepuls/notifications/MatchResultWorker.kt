package com.dominik.bluepuls.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dominik.bluepuls.R
import com.dominik.bluepuls.di.ServiceLocator
import kotlinx.coroutines.flow.first

/**
 * Periodični radnik koji provjerava ima li NOVI rezultat zadnje utakmice
 * i, ako da, javi notifikaciju. O istom rezultatu javi samo jednom (DataStore).
 */
class MatchResultWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val matches = ServiceLocator.matchRepository.getDinamoMatches().data
            val lastPlayed = matches.filter { it.isPlayed }.maxByOrNull { it.date }
                ?: return Result.success()

            val resultKey = "${lastPlayed.id}:${lastPlayed.homeGoals}-${lastPlayed.awayGoals}"
            val lastSeen = ServiceLocator.resultPreferences.lastResult.first()

            when {
                // Prvi put - samo zapamti baseline (bez notifikacije).
                lastSeen == null -> ServiceLocator.resultPreferences.setLastResult(resultKey)
                // Novi rezultat - javi i zapamti.
                lastSeen != resultKey -> {
                    NotificationHelper.showMatchResult(
                        applicationContext,
                        applicationContext.getString(R.string.notif_result_title),
                        "${lastPlayed.homeTeam.name} ${lastPlayed.homeGoals}:${lastPlayed.awayGoals} ${lastPlayed.awayTeam.name}"
                    )
                    ServiceLocator.resultPreferences.setLastResult(resultKey)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

package com.dominik.bluepuls.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

/**
 * Zakazuje STVARNE podsjetnike za utakmice preko WorkManagera.
 * Notifikacija se javlja [REMINDER_LEAD_MS] prije početka utakmice
 * (radi i kad je app zatvorena, preživljava restart uređaja).
 */
object MatchReminderScheduler {

    private const val UNIQUE_PREFIX = "match_reminder_"
    const val REMINDER_LEAD_MS = 60 * 60 * 1000L // 1 sat prije početka

    /**
     * Zakazuje podsjetnik za [matchId] s početkom u [kickoffMillis].
     * [notificationId] je stabilan jedinstven ID te utakmice (vidi ReminderPreferences).
     * @return true ako je zakazano; false ako je utakmica već počela/prošla.
     */
    fun scheduleForMatch(
        context: Context,
        matchId: String,
        notificationId: Int,
        title: String,
        message: String,
        kickoffMillis: Long
    ): Boolean {
        val now = System.currentTimeMillis()
        if (kickoffMillis <= now) return false

        val delay = (kickoffMillis - REMINDER_LEAD_MS - now).coerceAtLeast(0)
        val request = OneTimeWorkRequestBuilder<MatchReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    MatchReminderWorker.KEY_NOTIFICATION_ID to notificationId,
                    MatchReminderWorker.KEY_TITLE to title,
                    MatchReminderWorker.KEY_MESSAGE to message
                )
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(UNIQUE_PREFIX + matchId, ExistingWorkPolicy.REPLACE, request)
        return true
    }

    fun cancel(context: Context, matchId: String) {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_PREFIX + matchId)
    }
}

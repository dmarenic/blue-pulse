package com.dominik.bluepuls.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dominik.bluepuls.R

/**
 * WorkManager radnik koji prikaže notifikaciju-podsjetnik za utakmicu.
 * Pokreće ga [MatchReminderScheduler]. Radi pouzdano i kad je app zatvorena.
 */
class MatchReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE)
            ?: applicationContext.getString(R.string.notif_title)
        val message = inputData.getString(KEY_MESSAGE).orEmpty()
        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, DEFAULT_NOTIFICATION_ID)

        NotificationHelper.showMatchReminder(applicationContext, notificationId, title, message)
        return Result.success()
    }

    companion object {
        const val KEY_NOTIFICATION_ID = "notification_id"
        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
        private const val DEFAULT_NOTIFICATION_ID = 3000
    }
}

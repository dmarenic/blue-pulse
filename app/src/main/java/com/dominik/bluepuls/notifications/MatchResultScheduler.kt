package com.dominik.bluepuls.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Zakazuje periodičnu provjeru rezultata utakmica (svakih 6 h, uz internet).
 * Poziva se jednom pri pokretanju aplikacije.
 */
object MatchResultScheduler {

    private const val UNIQUE_WORK = "match_result_check"

    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<MatchResultWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}

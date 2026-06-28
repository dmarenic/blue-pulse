package com.dominik.bluepuls.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dominik.bluepuls.R

/**
 * Pomoćnik za lokalne notifikacije (kanal + prikaz).
 * Provjerava POST_NOTIFICATIONS dozvolu (Android 13+) prije prikaza pa
 * nikad ne baca SecurityException.
 */
object NotificationHelper {

    const val CHANNEL_ID = "match_reminders"
    private const val RESULT_NOTIFICATION_ID = 2002

    fun ensureChannel(context: Context) {
        // minSdk je 26, no provjera je dobra praksa.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notif_channel_desc)
            }
            context.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    // Dozvolu provjeravamo ručno preko hasNotificationPermission() prije notify();
    // lint ne prati tu pomoćnu funkciju pa eksplicitno potiskujemo lažni MissingPermission.
    @SuppressLint("MissingPermission")
    fun showMatchReminder(context: Context, notificationId: Int, title: String, message: String) {
        ensureChannel(context)

        if (!hasNotificationPermission(context)) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    @SuppressLint("MissingPermission")
    fun showMatchResult(context: Context, title: String, message: String) {
        ensureChannel(context)
        if (!hasNotificationPermission(context)) return

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(RESULT_NOTIFICATION_ID, notification)
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}

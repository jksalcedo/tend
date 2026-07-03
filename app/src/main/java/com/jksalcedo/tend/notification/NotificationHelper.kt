package com.jksalcedo.tend.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jksalcedo.tend.MainActivity
import com.jksalcedo.tend.R

object NotificationHelper {

    const val CHANNEL_ID = "tend_reminders"
    private const val CHANNEL_NAME = "Check-in Reminders"
    private const val CHANNEL_DESC = "Reminds you when it's time to reach out to someone"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = CHANNEL_DESC }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun showReminders(context: Context, names: List<String>) {
        if (names.isEmpty()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, body) = when (names.size) {
            1 -> "Time to reach out 🌱" to "Check in with ${names[0]}"
            2 -> "Time to reach out 🌱" to "Check in with ${names[0]} and ${names[1]}"
            else -> "Time to reach out 🌱" to "Check in with ${names[0]}, ${names[1]}, and ${names.size - 2} more"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(1001, notification)
    }
}

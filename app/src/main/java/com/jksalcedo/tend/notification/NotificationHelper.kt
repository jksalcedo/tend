package com.jksalcedo.tend.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jksalcedo.tend.MainActivity
import com.jksalcedo.tend.R

object NotificationHelper {

    const val CHANNEL_ID = "tend_reminders"
    private const val CHANNEL_NAME = "Check-in Reminders"
    private const val CHANNEL_DESC = "Reminds you when it's time to reach out to someone"

    private const val NOTIF_ID_CHECKINS = 1001

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = CHANNEL_DESC }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showReminders(context: Context, names: List<String>) {
        if (names.isEmpty()) return
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val pendingIntent = mainPendingIntent(context, requestCode = 0)

        val (title, body) = when (names.size) {
            1 -> "Time to reach out 🌱" to "Check in with ${names[0]}"
            2 -> "Time to reach out 🌱" to "Check in with ${names[0]} and ${names[1]}"
            else -> "Time to reach out 🌱" to "Check in with ${names[0]}, ${names[1]}, and ${names.size - 2} more"
        }

        notify(context, NOTIF_ID_CHECKINS, title, body, pendingIntent)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showEventReminder(
        context: Context,
        personName: String,
        eventLabel: String,
        daysUntil: Int,
        notifId: Int,
        age: Int? = null
    ) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val pendingIntent = mainPendingIntent(context, requestCode = notifId)

        val title = when {
            eventLabel.contains("birthday", ignoreCase = true) -> "🎂 Upcoming birthday"
            eventLabel.contains("anniversary", ignoreCase = true) -> "💍 Upcoming anniversary"
            else -> "📅 Upcoming date"
        }

        val ageStr = if (age != null && age > 0) {
            val suffix = when {
                age % 100 in 11..13 -> "th"
                age % 10 == 1 -> "st"
                age % 10 == 2 -> "nd"
                age % 10 == 3 -> "rd"
                else -> "th"
            }
            " $age$suffix"
        } else ""

        val body = when (daysUntil) {
            0 -> "$personName's$ageStr ${eventLabel.lowercase()} is today!"
            1 -> "$personName's$ageStr ${eventLabel.lowercase()} is tomorrow"
            else -> "$personName's$ageStr ${eventLabel.lowercase()} is in $daysUntil days"
        }

        notify(context, notifId, title, body, pendingIntent)
    }

    private fun mainPendingIntent(context: Context, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun notify(
        context: Context,
        id: Int,
        title: String,
        body: String,
        pendingIntent: PendingIntent
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }
}

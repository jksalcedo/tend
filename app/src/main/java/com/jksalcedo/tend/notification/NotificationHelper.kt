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

    private const val NOTIF_ID_CHECKINS = 1001

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = context.getString(R.string.notification_channel_description) }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showReminders(context: Context, names: List<String>) {
        if (names.isEmpty()) return
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val pendingIntent = mainPendingIntent(context, requestCode = 0)

        val title = context.getString(R.string.notification_checkin_title)
        val body = when (names.size) {
            1 -> context.getString(R.string.notification_checkin_body_one, names[0])
            2 -> context.getString(R.string.notification_checkin_body_two, names[0], names[1])
            else -> context.getString(
                R.string.notification_checkin_body_many,
                names[0],
                names[1],
                names.size - 2
            )
        }

        notify(context, NOTIF_ID_CHECKINS, title, body, pendingIntent)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showEventReminder(
        context: Context,
        personName: String,
        eventLabel: String,
        daysUntil: Int,
        notifId: Int
    ) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return

        val pendingIntent = mainPendingIntent(context, requestCode = notifId)

        val title = when {
            eventLabel.contains("birthday", ignoreCase = true) ->
                context.getString(R.string.notification_birthday_title)
            eventLabel.contains("anniversary", ignoreCase = true) ->
                context.getString(R.string.notification_anniversary_title)
            else -> context.getString(R.string.notification_event_title)
        }

        val body = when (daysUntil) {
            0 -> context.getString(R.string.notification_event_body_today, personName, eventLabel)
            1 -> context.getString(R.string.notification_event_body_tomorrow, personName, eventLabel)
            else -> context.getString(
                R.string.notification_event_body_in_days,
                personName,
                eventLabel,
                daysUntil
            )
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

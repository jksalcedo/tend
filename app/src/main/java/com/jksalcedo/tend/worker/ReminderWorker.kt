package com.jksalcedo.tend.worker

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jksalcedo.tend.data.local.AppDatabase
import com.jksalcedo.tend.notification.NotificationHelper
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val db: AppDatabase by inject()

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        val people = db.checkInDao().getAllPeople().first()
        val now = System.currentTimeMillis()

        // Check-in reminders
        val overdueNames = people
            .filter { it.nextReminderAt <= now && !it.isArchived }
            .map { it.name }
        NotificationHelper.showReminders(applicationContext, overdueNames)

        // Important date reminders
        people.filter { !it.isArchived }.forEach { person ->
            person.events.forEach { event ->
                val nextOccurrence = com.jksalcedo.tend.utils.DateUtils.getNextOccurrence(event.date)
                val daysUntil = com.jksalcedo.tend.utils.DateUtils.daysUntil(nextOccurrence).toInt()
                if (daysUntil in 0..7) {
                    // Stable unique ID: base + low bits of (personId XOR eventId hash)
                    val notifId = 2000 + ((person.id xor event.id.hashCode().toLong()) and 0xFFF).toInt()
                    NotificationHelper.showEventReminder(
                        context = applicationContext,
                        personName = person.name,
                        eventLabel = event.label,
                        daysUntil = daysUntil,
                        notifId = notifId
                    )
                }
            }
        }

        return Result.success()
    }
}

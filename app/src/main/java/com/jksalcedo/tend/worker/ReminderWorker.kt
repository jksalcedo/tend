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
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        people.filter { !it.isArchived }.forEach { person ->
            person.events.forEach { event ->
                val daysUntil = daysUntilNextOccurrence(event.date, today)
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

    private fun daysUntilNextOccurrence(dateMs: Long, today: Calendar): Int {
        val eventCal = Calendar.getInstance().apply { timeInMillis = dateMs }

        val thisYear = Calendar.getInstance().apply {
            set(Calendar.YEAR, today.get(Calendar.YEAR))
            set(Calendar.MONTH, eventCal.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, eventCal.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If this year's occurrence has already passed, check next year
        if (thisYear.before(today)) {
            thisYear.add(Calendar.YEAR, 1)
        }

        return TimeUnit.MILLISECONDS.toDays(thisYear.timeInMillis - today.timeInMillis).toInt()
    }
}

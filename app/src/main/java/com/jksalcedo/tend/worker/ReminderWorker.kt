package com.jksalcedo.tend.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jksalcedo.tend.data.local.AppDatabase
import com.jksalcedo.tend.notification.NotificationHelper
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val db: AppDatabase by inject()

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        val due = db.checkInDao().getAllPeople().first()
            .filter { it.nextReminderAt <= now && !it.isArchived }
            .map { it.name }

        NotificationHelper.showReminders(applicationContext, due)
        return Result.success()
    }
}

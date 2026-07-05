package com.jksalcedo.tend

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.jksalcedo.tend.di.appModule
import com.jksalcedo.tend.notification.NotificationHelper
import com.jksalcedo.tend.worker.ReminderWorker
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class TendApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TendApp)
            modules(appModule)
        }
        NotificationHelper.createChannel(this)
        scheduleReminders()
    }

    private fun scheduleReminders() {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "tend_reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
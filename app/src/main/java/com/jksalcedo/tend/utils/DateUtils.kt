package com.jksalcedo.tend.utils

import java.util.Calendar
import java.util.concurrent.TimeUnit

object DateUtils {

    fun getNextOccurrence(dateMs: Long): Long {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
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

        if (thisYear.before(today)) {
            thisYear.add(Calendar.YEAR, 1)
        }

        return thisYear.timeInMillis
    }

    fun daysUntil(targetMs: Long): Long {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val target = Calendar.getInstance().apply {
            timeInMillis = targetMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        return TimeUnit.MILLISECONDS.toDays(target.timeInMillis - today.timeInMillis)
    }
}

package com.jksalcedo.tend.domain.usecase

import android.content.Context
import com.jksalcedo.tend.domain.model.NerdStats
import com.jksalcedo.tend.domain.repository.PersonRepository
import com.jksalcedo.tend.utils.DateUtils
import java.io.File
import java.util.concurrent.TimeUnit

class GetNerdStatsUseCase(
    private val context: Context,
    private val repository: PersonRepository
) {
    suspend operator fun invoke(): NerdStats {
        val allPeople = repository.getAllPeopleList()
        
        val activePeople = allPeople.filter { !it.isArchived }
        val archivedPeople = allPeople.filter { it.isArchived }
        
        val totalNotes = allPeople.sumOf { it.notes.size }
        val totalEvents = allPeople.sumOf { it.events.size }
        val totalSocialLinks = allPeople.sumOf { it.socialLinks.size }
        
        val frequencies = allPeople.map { it.frequencyDays }.filter { it > 0 }
        val avgFrequency = if (frequencies.isNotEmpty()) frequencies.average() else 0.0
        val shortestFrequency = frequencies.minOrNull() ?: 0
        val longestFrequency = frequencies.maxOrNull() ?: 0
        
        val now = System.currentTimeMillis()
        var overdue = 0
        var dueSoon = 0
        
        activePeople.forEach { person ->
            val checkInDays = TimeUnit.MILLISECONDS.toDays(person.nextReminderAt - now)
            val nextEventDays = person.events.map { event ->
                DateUtils.daysUntil(DateUtils.getNextOccurrence(event.date))
            }.minOrNull()
            
            val minDays = if (nextEventDays != null) {
                Math.min(Math.max(0, checkInDays), nextEventDays) // Only consider checkInDays 0+ when comparing with event
            } else {
                checkInDays
            }
            
            if (checkInDays < 0) overdue++
            else if (checkInDays <= 3L || (nextEventDays != null && nextEventDays <= 3L)) dueSoon++
        }
        
        // Get app version
        var appVersion = "Unknown"
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            appVersion = "${packageInfo.versionName} (${packageInfo.longVersionCode})"
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Get DB size
        val dbFile = context.getDatabasePath("tend_database")
        val dbSize = if (dbFile.exists()) dbFile.length() else 0L
        
        return NerdStats(
            appVersion = appVersion,
            databaseSizeBytes = dbSize,
            activeConnections = activePeople.size,
            archivedConnections = archivedPeople.size,
            totalNotes = totalNotes,
            totalEvents = totalEvents,
            totalSocialLinks = totalSocialLinks,
            averageCheckInFrequency = avgFrequency,
            shortestCheckInFrequency = shortestFrequency,
            longestCheckInFrequency = longestFrequency,
            overdueConnections = overdue,
            dueSoonConnections = dueSoon
        )
    }
}

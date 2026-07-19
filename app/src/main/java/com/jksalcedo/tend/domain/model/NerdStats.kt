package com.jksalcedo.tend.domain.model

data class NerdStats(
    val appVersion: String,
    val databaseSizeBytes: Long,
    val activeConnections: Int,
    val archivedConnections: Int,
    val totalNotes: Int,
    val totalEvents: Int,
    val totalSocialLinks: Int,
    val averageCheckInFrequency: Double,
    val shortestCheckInFrequency: Int,
    val longestCheckInFrequency: Int,
    val overdueConnections: Int,
    val dueSoonConnections: Int
) {
    val databaseSizeKb: Double
        get() = databaseSizeBytes / 1024.0
}

package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.UpcomingEventItem
import com.jksalcedo.tend.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class GetUpcomingEventsUseCase(
    private val personRepository: PersonRepository
) {
    operator fun invoke(daysAhead: Int = 60): Flow<List<UpcomingEventItem>> {
        return personRepository.getAllPeople().map { people ->
            val upcoming = mutableListOf<UpcomingEventItem>()
            val todayCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val todayMillis = todayCal.timeInMillis
            val endMillis = todayMillis + (daysAhead.toLong() * 24 * 60 * 60 * 1000L)

            val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

            for (person in people) {
                if (person.isArchived) continue
                for (event in person.events) {
                    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                        timeInMillis = event.date
                    }
                    val eventMonth = utcCal.get(Calendar.MONTH)
                    val eventDay = utcCal.get(Calendar.DAY_OF_MONTH)

                    val targetCal = Calendar.getInstance().apply {
                        timeInMillis = todayMillis
                        set(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.MONTH, eventMonth)
                        val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
                        set(Calendar.DAY_OF_MONTH, minOf(eventDay, maxDay))
                    }

                    if (targetCal.timeInMillis < todayMillis) {
                        targetCal.add(Calendar.YEAR, 1)
                    }

                    val nextOccurrence = targetCal.timeInMillis

                    if (nextOccurrence in todayMillis..endMillis) {
                        val diffMillis = nextOccurrence - todayMillis
                        val daysRemaining = (diffMillis / (24 * 60 * 60 * 1000L)).toInt()

                        upcoming.add(
                            UpcomingEventItem(
                                personId = person.id,
                                personName = person.name,
                                photoUri = person.photoUri,
                                localPhotoPath = person.localPhotoPath,
                                eventLabel = event.label,
                                eventType = event.type,
                                eventDate = nextOccurrence,
                                daysRemaining = daysRemaining,
                                formattedDate = dateFormat.format(Date(nextOccurrence)),
                                phoneNumber = person.phoneNumber,
                                email = person.email
                            )
                        )
                    }
                }
            }

            upcoming.sortedBy { it.daysRemaining }
        }
    }
}

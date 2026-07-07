package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.NativeContact
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.repository.PersonRepository
import java.util.concurrent.TimeUnit

class ImportContactsUseCase(
    private val personRepository: PersonRepository
) {
    suspend operator fun invoke(contacts: List<NativeContact>, frequencyDays: Int = 14) {
        val now = System.currentTimeMillis()
        val nextReminder = now + TimeUnit.DAYS.toMillis(frequencyDays.toLong())

        contacts.forEach { contact ->
            personRepository.insertPerson(
                Person(
                    name = contact.name,
                    photoUri = contact.photoUri,
                    phoneNumber = contact.phoneNumber,
                    email = contact.email,
                    frequencyDays = frequencyDays,
                    lastContactedAt = now,
                    nextReminderAt = nextReminder,
                    nativeLookupKey = contact.lookupKey,
                    nativeContactId = contact.contactId
                )
            )
        }
    }
}

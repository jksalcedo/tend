package com.jksalcedo.tend.data.repository

import android.content.Context
import com.jksalcedo.tend.data.local.CheckInDao
import com.jksalcedo.tend.data.local.entity.PersonEntity
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.repository.PersonRepository
import com.jksalcedo.tend.widget.TendWidgetProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PersonRepositoryImpl(
    private val dao: CheckInDao,
    private val context: Context
) : PersonRepository {

    override fun getAllPeople(): Flow<List<Person>> {
        return dao.getAllPeople().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getArchivedPeople(): Flow<List<Person>> {
        return dao.getArchivedPeople().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPersonById(id: Long): Person? {
        return dao.getPersonById(id)?.toDomain()
    }

    override fun observePersonById(id: Long): Flow<Person?> {
        return dao.observePersonById(id).map { it?.toDomain() }
    }

    override suspend fun insertPerson(person: Person) {
        dao.insertPerson(person.toEntity())
        TendWidgetProvider.triggerUpdate(context)
    }

    override suspend fun updatePerson(person: Person) {
        dao.updatePerson(person.toEntity())
        TendWidgetProvider.triggerUpdate(context)
    }

    override suspend fun deletePerson(id: Long) {
        dao.deletePerson(id)
        TendWidgetProvider.triggerUpdate(context)
    }

    override suspend fun getLinkedPeople(): List<Person> {
        return dao.getLinkedPeople().map { it.toDomain() }
    }

    override fun observeDuplicatesOf(lookupKey: String, excludeId: Long): Flow<List<Person>> {
        return dao.observeDuplicatesOf(lookupKey, excludeId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getEveryPerson(): List<Person> {
        return dao.getEveryPerson().map { it.toDomain() }
    }

    override suspend fun getAllPeopleList(): List<Person> {
        return dao.getAllPeopleList().map { it.toDomain() }
    }

    override suspend fun insertAll(people: List<Person>) {
        dao.insertAll(people.map { it.toEntity() })
        TendWidgetProvider.triggerUpdate(context)
    }

    override suspend fun deleteAllPeople() {
        dao.deleteAllPeople()
        TendWidgetProvider.triggerUpdate(context)
    }

    private fun PersonEntity.toDomain(): Person {
        return Person(
            id = id,
            name = name,
            photoUri = photoUri,
            phoneNumber = phoneNumber,
            email = email,
            events = events,
            notes = notes,
            socialLinks = socialLinks,
            frequencyDays = frequencyDays,
            lastContactedAt = lastContactedAt,
            nextReminderAt = nextReminderAt,
            isArchived = isArchived,
            nativeLookupKey = nativeLookupKey,
            nativeContactId = nativeContactId,
            isDeviceLinkBroken = isDeviceLinkBroken,
            localPhotoPath = localPhotoPath,
            tags = tags,
            reminderWindowDays = reminderWindowDays
        )
    }

    private fun Person.toEntity(): PersonEntity {
        return PersonEntity(
            id = id,
            name = name,
            photoUri = photoUri,
            phoneNumber = phoneNumber,
            email = email,
            events = events,
            notes = notes,
            socialLinks = socialLinks,
            frequencyDays = frequencyDays,
            lastContactedAt = lastContactedAt,
            nextReminderAt = nextReminderAt,
            isArchived = isArchived,
            nativeLookupKey = nativeLookupKey,
            nativeContactId = nativeContactId,
            isDeviceLinkBroken = isDeviceLinkBroken,
            localPhotoPath = localPhotoPath,
            tags = tags,
            reminderWindowDays = reminderWindowDays
        )
    }
}
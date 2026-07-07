package com.jksalcedo.tend.data.repository

import com.jksalcedo.tend.data.local.CheckInDao
import com.jksalcedo.tend.data.local.entity.PersonEntity
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
class PersonRepositoryImpl(
    private val dao: CheckInDao
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

    override suspend fun insertPerson(person: Person) {
        dao.insertPerson(person.toEntity())
    }

    override suspend fun updatePerson(person: Person) {
        dao.updatePerson(person.toEntity())
    }

    override suspend fun deletePerson(id: Long) {
        dao.deletePerson(id)
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
            nativeContactId = nativeContactId
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
            nativeContactId = nativeContactId
        )
    }
}

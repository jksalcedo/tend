package com.jksalcedo.crontact.data.repository

import com.jksalcedo.crontact.data.local.CheckInDao
import com.jksalcedo.crontact.data.local.entity.PersonEntity
import com.jksalcedo.crontact.domain.model.Person
import com.jksalcedo.crontact.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PersonRepositoryImpl @Inject constructor(
    private val dao: CheckInDao
) : PersonRepository {

    override fun getAllPeople(): Flow<List<Person>> {
        return dao.getAllPeople().map { entities ->
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
            notes = notes,
            cadenceDays = cadenceDays,
            lastContactedAt = lastContactedAt,
            nextReminderAt = nextReminderAt,
            isArchived = isArchived
        )
    }

    private fun Person.toEntity(): PersonEntity {
        return PersonEntity(
            id = id,
            name = name,
            photoUri = photoUri,
            notes = notes,
            cadenceDays = cadenceDays,
            lastContactedAt = lastContactedAt,
            nextReminderAt = nextReminderAt,
            isArchived = isArchived
        )
    }
}

package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.NativeContact
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.repository.ContactsRepository
import com.jksalcedo.tend.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeContactsRepository(
    private val importableContacts: List<NativeContact> = emptyList()
) : ContactsRepository {
    override suspend fun getImportableContacts(): List<NativeContact> = importableContacts
}

class FakePersonRepository : PersonRepository {
    private val people = MutableStateFlow<List<Person>>(emptyList())
    val insertedPeople: List<Person> get() = people.value

    override fun getAllPeople(): Flow<List<Person>> = people
    override fun getArchivedPeople(): Flow<List<Person>> = people

    override suspend fun getPersonById(id: Long): Person? =
        people.value.firstOrNull { it.id == id }

    override suspend fun insertPerson(person: Person) {
        val assignedId = (people.value.maxOfOrNull { it.id } ?: 0L) + 1
        people.value = people.value + person.copy(id = assignedId)
    }

    override suspend fun updatePerson(person: Person) {
        people.value = people.value.map { if (it.id == person.id) person else it }
    }

    override suspend fun deletePerson(id: Long) {
        people.value = people.value.filterNot { it.id == id }
    }
}

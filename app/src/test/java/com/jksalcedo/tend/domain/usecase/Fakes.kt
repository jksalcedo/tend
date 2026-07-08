package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.model.NativeContact
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.repository.ContactsRepository
import com.jksalcedo.tend.domain.repository.OnboardingRepository
import com.jksalcedo.tend.domain.repository.PersonRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeContactsRepository(
    private val importableContacts: List<NativeContact> = emptyList(),
    private val resolvedContacts: Map<String, NativeContact?> = emptyMap(),
    private val photosToCache: Map<Long, String?> = emptyMap(),
    private val createdContact: NativeContact? = null
) : ContactsRepository {
    val createContactCalls = mutableListOf<CreateContactCall>()
    val resolveCalls = mutableListOf<String>()
    val cachePhotoCalls = mutableListOf<Long>()

    data class CreateContactCall(
        val name: String,
        val phoneNumber: String?,
        val email: String?,
        val photoUri: String?
    )

    override suspend fun getImportableContacts(): List<NativeContact> = importableContacts

    override suspend fun resolveContact(lookupKey: String, cachedContactId: Long?): NativeContact? {
        resolveCalls.add(lookupKey)
        return resolvedContacts[lookupKey]
    }

    override suspend fun cachePhoto(contactId: Long): String? {
        cachePhotoCalls.add(contactId)
        return photosToCache[contactId]
    }

    override suspend fun createContact(
        name: String,
        phoneNumber: String?,
        email: String?,
        photoUri: String?
    ): NativeContact {
        createContactCalls.add(CreateContactCall(name, phoneNumber, email, photoUri))
        return createdContact ?: NativeContact(
            lookupKey = "new-lookup-key",
            contactId = 999L,
            name = name,
            phoneNumber = phoneNumber,
            email = email,
            photoUri = photoUri
        )
    }
}

class FakePersonRepository : PersonRepository {
    private val people = MutableStateFlow<List<Person>>(emptyList())
    val insertedPeople: List<Person> get() = people.value

    override fun getAllPeople(): Flow<List<Person>> = people
    override fun getArchivedPeople(): Flow<List<Person>> = people

    override suspend fun getPersonById(id: Long): Person? =
        people.value.firstOrNull { it.id == id }

    override fun observePersonById(id: Long): Flow<Person?> =
        people.map { list -> list.firstOrNull { it.id == id } }

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

    override suspend fun getLinkedPeople(): List<Person> =
        people.value.filter { it.nativeLookupKey != null }

    override fun observeDuplicatesOf(lookupKey: String, excludeId: Long): Flow<List<Person>> =
        people.map { list -> list.filter { it.nativeLookupKey == lookupKey && it.id != excludeId } }

    fun seed(person: Person): Person {
        val assignedId = (people.value.maxOfOrNull { it.id } ?: 0L) + 1
        val seeded = person.copy(id = assignedId)
        people.value = people.value + seeded
        return seeded
    }
}

class FakeOnboardingRepository(
    private var resolved: Boolean = false
) : OnboardingRepository {
    var markResolvedCallCount = 0
        private set

    override suspend fun isContactImportPromptResolved(): Boolean = resolved

    override suspend fun markContactImportPromptResolved() {
        markResolvedCallCount++
        resolved = true
    }
}

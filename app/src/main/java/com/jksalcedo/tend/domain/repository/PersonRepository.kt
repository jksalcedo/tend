package com.jksalcedo.tend.domain.repository

import com.jksalcedo.tend.domain.model.Person
import kotlinx.coroutines.flow.Flow

interface PersonRepository {
    fun getAllPeople(): Flow<List<Person>>
    fun getArchivedPeople(): Flow<List<Person>>
    suspend fun getPersonById(id: Long): Person?
    fun observePersonById(id: Long): Flow<Person?>
    suspend fun insertPerson(person: Person)
    suspend fun updatePerson(person: Person)
    suspend fun deletePerson(id: Long)
    suspend fun getLinkedPeople(): List<Person>
    fun observeDuplicatesOf(lookupKey: String, excludeId: Long): Flow<List<Person>>
}

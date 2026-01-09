package com.jksalcedo.crontact.domain.repository

import com.jksalcedo.crontact.domain.model.Person
import kotlinx.coroutines.flow.Flow

interface PersonRepository {
    fun getAllPeople(): Flow<List<Person>>
    suspend fun getPersonById(id: Long): Person?
    suspend fun insertPerson(person: Person)
    suspend fun updatePerson(person: Person)
    suspend fun deletePerson(id: Long)
}

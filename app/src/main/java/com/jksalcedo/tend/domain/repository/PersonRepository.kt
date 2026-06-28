package com.jksalcedo.tend.domain.repository

import com.jksalcedo.tend.domain.model.Person
import kotlinx.coroutines.flow.Flow

interface PersonRepository {
    fun getAllPeople(): Flow<List<Person>>
    suspend fun getPersonById(id: Long): Person?
    suspend fun insertPerson(person: Person)
    suspend fun updatePerson(person: Person)
    suspend fun deletePerson(id: Long)
}

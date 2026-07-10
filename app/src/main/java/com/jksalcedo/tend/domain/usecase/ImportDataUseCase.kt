package com.jksalcedo.tend.domain.usecase

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jksalcedo.tend.domain.model.Person
import com.jksalcedo.tend.domain.repository.PersonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class ImportDataUseCase(
    private val repository: PersonRepository
) {
    suspend operator fun invoke(inputStream: InputStream) {
        withContext(Dispatchers.IO) {
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Person>>() {}.type
            val people: List<Person> = Gson().fromJson(jsonString, listType)

            if (people.isNotEmpty()) {
                repository.deleteAllPeople()
                repository.insertAll(people)
            }
        }
    }
}

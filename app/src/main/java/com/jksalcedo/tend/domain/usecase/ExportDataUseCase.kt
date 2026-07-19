package com.jksalcedo.tend.domain.usecase

import com.google.gson.Gson
import com.jksalcedo.tend.domain.repository.PersonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream

class ExportDataUseCase(
    private val repository: PersonRepository
) {
    suspend operator fun invoke(outputStream: OutputStream) {
        withContext(Dispatchers.IO) {
            val allPeople = repository.getAllPeopleList()
            val jsonString = Gson().toJson(allPeople)

            outputStream.use { 
                it.write(jsonString.toByteArray())
            }
        }
    }
}
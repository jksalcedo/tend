package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.repository.PersonRepository

class UnlinkPersonUseCase(
    private val repository: PersonRepository
) {
    suspend operator fun invoke(id: Long) {
        val person = repository.getPersonById(id) ?: return
        repository.updatePerson(
            person.copy(
                nativeLookupKey = null,
                nativeContactId = null,
                isDeviceLinkBroken = false,
                duplicateOfPersonId = null
            )
        )
    }
}

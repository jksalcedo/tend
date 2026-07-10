package com.jksalcedo.tend.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveAllTagsUseCaseTest {

    @Test
    fun `returns every tag in the pool`() = runBlocking {
        val tagRepository = FakeTagRepository()
        tagRepository.seed("Family", "Friend")
        val useCase = ObserveAllTagsUseCase(tagRepository)

        val tags = useCase().first()

        assertEquals(listOf("Family", "Friend"), tags)
    }
}

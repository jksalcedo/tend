package com.jksalcedo.tend.domain.usecase

import com.jksalcedo.tend.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow

class ObserveAllTagsUseCase(
    private val tagRepository: TagRepository
) {
    operator fun invoke(): Flow<List<String>> = tagRepository.observeAllTags()
}

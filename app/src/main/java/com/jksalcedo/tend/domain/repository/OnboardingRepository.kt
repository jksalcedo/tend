package com.jksalcedo.tend.domain.repository

interface OnboardingRepository {
    suspend fun isContactImportPromptResolved(): Boolean
    suspend fun markContactImportPromptResolved()
}

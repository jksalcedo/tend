package com.jksalcedo.tend.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.jksalcedo.tend.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.first

class OnboardingRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : OnboardingRepository {

    override suspend fun isContactImportPromptResolved(): Boolean {
        return dataStore.data.first()[CONTACT_IMPORT_PROMPT_RESOLVED] ?: false
    }

    override suspend fun markContactImportPromptResolved() {
        dataStore.edit { it[CONTACT_IMPORT_PROMPT_RESOLVED] = true }
    }

    companion object {
        private val CONTACT_IMPORT_PROMPT_RESOLVED = booleanPreferencesKey("contact_import_prompt_resolved")
    }
}

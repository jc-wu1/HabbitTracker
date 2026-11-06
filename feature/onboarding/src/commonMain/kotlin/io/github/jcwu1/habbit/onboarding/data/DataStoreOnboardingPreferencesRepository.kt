package io.github.jcwu1.habbit.onboarding.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import io.github.jcwu1.habbit.onboarding.OnboardingPreferencesRepository
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Singleton

@Singleton
class DataStoreOnboardingPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : OnboardingPreferencesRepository {
    override val hasSeenOnboarding = dataStore.data.map { it[hasSeenKey] ?: false }

    override suspend fun markSeen() {
        dataStore.edit { it[hasSeenKey] = true }
    }

    private companion object {
        val hasSeenKey = booleanPreferencesKey("has_seen_onboarding")
    }
}

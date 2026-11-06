package io.github.jcwu1.habbit.onboarding

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import io.github.jcwu1.habbit.onboarding.data.DataStoreOnboardingPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingPreferencesRepositoryTest {
    @Test
    fun persistsTheExpectedBooleanAcrossStoreRecreation() = runTest {
        val directory = Files.createTempDirectory("habbit-onboarding").toFile()
        val file = directory.resolve("test.preferences_pb")
        val firstScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val secondScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val store = PreferenceDataStoreFactory.create(scope = firstScope) { file }
            val repository = DataStoreOnboardingPreferencesRepository(store)
            assertFalse(repository.hasSeenOnboarding.first())
            repository.markSeen()
            assertTrue(store.data.first()[booleanPreferencesKey("has_seen_onboarding")] == true)
            firstScope.coroutineContext[Job]!!.cancelAndJoin()

            val reopened = PreferenceDataStoreFactory.create(scope = secondScope) { file }
            assertTrue(DataStoreOnboardingPreferencesRepository(reopened).hasSeenOnboarding.first())
        } finally {
            firstScope.coroutineContext[Job]!!.cancelAndJoin()
            secondScope.coroutineContext[Job]!!.cancelAndJoin()
            directory.deleteRecursively()
        }
    }
}

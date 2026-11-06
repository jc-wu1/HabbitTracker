package io.github.jcwu1.habbit.startup

import io.github.jcwu1.habbit.onboarding.OnboardingPreferencesRepository

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    @Test
    fun waitsForReadAndForWriteBeforeExposingLoginDestination() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val read = CompletableDeferred<Boolean>()
        val write = CompletableDeferred<Unit>()
        var writes = 0
        val repository = object : OnboardingPreferencesRepository {
            override val hasSeenOnboarding: Flow<Boolean> = flow { emit(read.await()) }
            override suspend fun markSeen() { writes++; write.await() }
        }
        val vm = AppViewModel(repository)
        try {
            vm.load()
            runCurrent()
            assertNull(vm.state.value.hasSeenOnboarding)
            read.complete(false)
            advanceUntilIdle()
            assertEquals(false, vm.state.value.hasSeenOnboarding)
            vm.completeOnboarding()
            vm.completeOnboarding()
            runCurrent()
            assertEquals(1, writes)
            assertTrue(vm.state.value.isSaving)
            assertEquals(false, vm.state.value.hasSeenOnboarding)
            write.complete(Unit)
            advanceUntilIdle()
            assertEquals(true, vm.state.value.hasSeenOnboarding)
            assertFalse(vm.state.value.isSaving)
        } finally {
            vm.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun readFailureKeepsDestinationUnknownAndRetryLoadsReturningUser() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var fail = true
        val repository = object : OnboardingPreferencesRepository {
            override val hasSeenOnboarding: Flow<Boolean> = flow {
                if (fail) error("storage unavailable")
                emit(true)
            }
            override suspend fun markSeen() = error("returning user must not rewrite preferences")
        }
        val vm = AppViewModel(repository)
        try {
            vm.load()
            advanceUntilIdle()
            assertNull(vm.state.value.hasSeenOnboarding)
            assertTrue(vm.state.value.loadFailed)
            fail = false
            vm.load()
            advanceUntilIdle()
            assertEquals(true, vm.state.value.hasSeenOnboarding)
            assertFalse(vm.state.value.loadFailed)
        } finally {
            vm.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun failedWriteDoesNotLeaveOnboardingAndCanBeRetried() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var fail = true
        val repository = object : OnboardingPreferencesRepository {
            override val hasSeenOnboarding = kotlinx.coroutines.flow.flowOf(false)
            override suspend fun markSeen() { if (fail) error("disk full") }
        }
        val vm = AppViewModel(repository)
        try {
            vm.load()
            advanceUntilIdle()
            vm.completeOnboarding()
            advanceUntilIdle()
            assertEquals(false, vm.state.value.hasSeenOnboarding)
            assertTrue(vm.state.value.saveFailed)
            fail = false
            vm.completeOnboarding()
            advanceUntilIdle()
            assertEquals(true, vm.state.value.hasSeenOnboarding)
        } finally {
            vm.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }
}

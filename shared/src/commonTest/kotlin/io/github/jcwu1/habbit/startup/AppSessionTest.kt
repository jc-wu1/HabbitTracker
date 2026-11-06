package io.github.jcwu1.habbit.startup

import androidx.lifecycle.viewModelScope
import io.github.jcwu1.habbit.auth.SessionRepository
import io.github.jcwu1.habbit.onboarding.OnboardingPreferencesRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import kotlin.test.*
import kotlin.time.Clock

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AppSessionTest {
    @Test
    fun startupWaitsForTheSavedSessionBeforeChoosingADestination() = runTest {
        val read = CompletableDeferred<Long?>()
        withViewModel(object : SessionRepository {
            override suspend fun accessTokenExpiresAtMillis() = read.await()
        }) { vm ->
            vm.load()
            runCurrent()
            assertNull(vm.state.value.hasSeenOnboarding)
            read.complete(Clock.System.now().toEpochMilliseconds() + 60_000)
            runCurrent()
            assertEquals(true, vm.state.value.hasSeenOnboarding)
            assertTrue(vm.state.value.isSignedIn)
        }
    }

    @Test
    fun absentAndExpiredSessionsRequireLogin() = runTest {
        for (expiry in listOf(null, Clock.System.now().toEpochMilliseconds() - 1)) {
            withViewModel(FakeSessionRepository(expiry)) { vm ->
                vm.load()
                runCurrent()
                assertEquals(true, vm.state.value.hasSeenOnboarding)
                assertFalse(vm.state.value.isSignedIn)
            }
        }
    }

    @Test
    fun aSavedSessionUsesTheReturningUserFlowEvenWithoutTheOnboardingPreference() = runTest {
        for (offset in listOf(-1L, 60_000L)) {
            val expiry = Clock.System.now().toEpochMilliseconds() + offset
            withViewModel(FakeSessionRepository(expiry), hasSeenOnboarding = false) { vm ->
                vm.load()
                runCurrent()
                assertEquals(true, vm.state.value.hasSeenOnboarding)
                assertEquals(offset > 0, vm.state.value.isSignedIn)
            }
        }
    }

    @Test
    fun anActiveSessionSignsOutWhenItsAccessTokenExpires() = runTest {
        withViewModel(FakeSessionRepository(Clock.System.now().toEpochMilliseconds() + 60_000)) { vm ->
            vm.load()
            runCurrent()
            assertTrue(vm.state.value.isSignedIn)
            advanceTimeBy(61_000)
            runCurrent()
            assertFalse(vm.state.value.isSignedIn)
        }
    }

    @Test
    fun rechecksSessionAfterLoginAndWhenReturningToTheApp() = runTest {
        val sessions = FakeSessionRepository(null)
        withViewModel(sessions) { vm ->
            vm.load()
            runCurrent()
            assertFalse(vm.state.value.isSignedIn)
            sessions.expiry = Clock.System.now().toEpochMilliseconds() + 60_000
            vm.refreshSession()
            runCurrent()
            assertTrue(vm.state.value.isSignedIn)
            sessions.expiry = Clock.System.now().toEpochMilliseconds() - 1
            vm.refreshSession()
            runCurrent()
            assertFalse(vm.state.value.isSignedIn)
        }
    }

    @Test
    fun sessionReadFailureDoesNotExposeHomeAndCanBeRetried() = runTest {
        var fail = true
        withViewModel(object : SessionRepository {
            override suspend fun accessTokenExpiresAtMillis(): Long? {
                if (fail) error("storage unavailable")
                return Clock.System.now().toEpochMilliseconds() + 60_000
            }
        }) { vm ->
            vm.load()
            runCurrent()
            assertTrue(vm.state.value.loadFailed)
            assertNull(vm.state.value.hasSeenOnboarding)
            assertFalse(vm.state.value.isSignedIn)
            fail = false
            vm.load()
            runCurrent()
            assertFalse(vm.state.value.loadFailed)
            assertTrue(vm.state.value.isSignedIn)
        }
    }

    private suspend fun TestScope.withViewModel(
        sessions: SessionRepository,
        hasSeenOnboarding: Boolean = true,
        block: suspend TestScope.(AppViewModel) -> Unit,
    ) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val preferences = object : OnboardingPreferencesRepository {
            override val hasSeenOnboarding = flowOf(hasSeenOnboarding)
            override suspend fun markSeen() = Unit
        }
        val vm = AppViewModel(preferences, sessions)
        try {
            block(vm)
        } finally {
            vm.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }
}

private class FakeSessionRepository(var expiry: Long?) : SessionRepository {
    override suspend fun accessTokenExpiresAtMillis() = expiry
}

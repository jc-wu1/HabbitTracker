package io.github.jcwu1.habbit.auth

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    @Test
    fun validatesBeforeCallingRepositoryAndPreventsDuplicateSubmissions() = runTest {
        val response = CompletableDeferred<AuthResult>()
        var calls = 0
        val repository = object : AuthRepository {
            override suspend fun logIn(username: String, password: String): AuthResult {
                calls++
                assertEquals("emilys", username)
                assertEquals(" pw ", password)
                return response.await()
            }
            override suspend fun register(name: String, email: String, password: String) = AuthResult.NotConfigured
            override suspend fun continueWithGoogle() = AuthResult.NotConfigured
            override suspend fun resetPassword(email: String) = AuthResult.NotConfigured
        }
        withViewModel(repository) { vm ->
            vm.submit(register = false)
            runCurrent()
            assertEquals(FieldError.Required, vm.state.value.errors.username)
            assertEquals(FieldError.Required, vm.state.value.errors.password)
            assertEquals(0, calls)
            vm.updateUsername(" emilys ")
            vm.updatePassword(" pw ")
            assertFalse(vm.state.value.errors.hasErrors)
            vm.submit(register = false)
            vm.submit(register = false)
            runCurrent()
            assertTrue(vm.state.value.isLoading)
            assertEquals(1, calls)
            response.complete(AuthResult.NotConfigured)
            advanceUntilIdle()
            assertFalse(vm.state.value.isLoading)
            assertEquals(AuthMessage.NotConfigured, vm.state.value.message)
        }
    }

    @Test
    fun successfulLoginClearsTheSubmittedPassword() = runTest {
        withViewModel(LoginRepository { AuthResult.Success }) { vm ->
            vm.updateUsername("emilys")
            vm.updatePassword("pw")
            vm.togglePasswordVisibility()
            vm.submit(register = false)
            advanceUntilIdle()
            assertEquals("", vm.state.value.password)
            assertFalse(vm.state.value.passwordVisible)
            assertFalse(vm.state.value.isLoading)
            assertEquals(AuthMessage.SignedIn, vm.state.value.message)
        }
    }

    @Test
    fun rejectedCredentialsAndStorageFailureStayRetryable() = runTest {
        for ((result, message) in listOf(
            AuthResult.InvalidCredentials to AuthMessage.InvalidCredentials,
            AuthResult.StorageFailed to AuthMessage.StorageFailed,
        )) {
            withViewModel(LoginRepository { result }) { vm ->
                vm.updateUsername("emilys")
                vm.updatePassword("pw")
                vm.submit(register = false)
                advanceUntilIdle()
                assertEquals(message, vm.state.value.message)
                assertEquals("pw", vm.state.value.password)
                assertFalse(vm.state.value.isLoading)
                vm.dismissMessage()
                assertNull(vm.state.value.message)
            }
        }
    }

    @Test
    fun networkFailureResetsLoadingAndShowsAnError() = runTest {
        withViewModel(LoginRepository { error("offline") }) { vm ->
            vm.updateUsername("emilys")
            vm.updatePassword("pw")
            vm.submit(register = false)
            advanceUntilIdle()
            assertEquals(AuthMessage.Failed, vm.state.value.message)
            assertFalse(vm.state.value.isLoading)
        }
    }

    @Test
    fun cancellationDoesNotShowAFailureMessage() = runTest {
        withViewModel(LoginRepository { throw CancellationException("cancelled") }) { vm ->
            vm.updateUsername("emilys")
            vm.updatePassword("pw")
            vm.submit(register = false)
            advanceUntilIdle()
            assertNull(vm.state.value.message)
            assertFalse(vm.state.value.isLoading)
        }
    }

    private suspend fun TestScope.withViewModel(
        repository: AuthRepository,
        block: suspend (AuthViewModel) -> Unit,
    ) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val vm = AuthViewModel(repository)
        try {
            block(vm)
        } finally {
            vm.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }
}

private class LoginRepository(private val login: suspend () -> AuthResult) : AuthRepository {
    override suspend fun logIn(username: String, password: String) = login()
    override suspend fun register(name: String, email: String, password: String) = AuthResult.NotConfigured
    override suspend fun continueWithGoogle() = AuthResult.NotConfigured
    override suspend fun resetPassword(email: String) = AuthResult.NotConfigured
}

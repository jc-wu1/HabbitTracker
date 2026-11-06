package io.github.jcwu1.habbit.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

enum class AuthMessage { SignedIn, InvalidCredentials, StorageFailed, NotConfigured, Failed, TermsUnavailable, PrivacyUnavailable }

data class AuthUiState(
    val username: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val errors: AuthErrors = AuthErrors(),
    val isLoading: Boolean = false,
    val message: AuthMessage? = null,
)

@KoinViewModel
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(AuthUiState())
    val state = mutableState.asStateFlow()

    fun updateUsername(value: String) = mutableState.update {
        it.copy(username = value, errors = it.errors.copy(username = if (it.errors.username != null && value.isBlank()) FieldError.Required else null))
    }

    fun updateName(value: String) = mutableState.update {
        it.copy(name = value, errors = it.errors.copy(name = if (it.errors.name != null && value.isBlank()) FieldError.Required else null))
    }

    fun updateEmail(value: String) = mutableState.update {
        it.copy(email = value, errors = it.errors.copy(email = if (it.errors.email != null) validateEmail(value) else null))
    }

    fun updatePassword(value: String, register: Boolean = false) = mutableState.update {
        val error = if (it.errors.password != null) {
            if (register) validatePassword(value) else if (value.isBlank()) FieldError.Required else null
        } else null
        it.copy(password = value, errors = it.errors.copy(password = error))
    }

    fun togglePasswordVisibility() = mutableState.update { it.copy(passwordVisible = !it.passwordVisible) }
    fun showMessage(message: AuthMessage) = mutableState.update { it.copy(message = message) }
    fun dismissMessage() = mutableState.update { it.copy(message = null) }

    fun submit(register: Boolean) {
        val input = state.value
        if (input.isLoading) return
        val identifier = if (register) input.email else input.username
        val errors = validateAuth(input.name, identifier, input.password, register)
        mutableState.update { it.copy(errors = errors) }
        if (errors.hasErrors) return
        perform {
            if (register) repository.register(input.name.trim(), input.email.trim(), input.password)
            else repository.logIn(input.username.trim(), input.password)
        }
    }

    fun google() = perform { repository.continueWithGoogle() }

    fun forgotPassword() {
        if (!state.value.isLoading) showMessage(AuthMessage.NotConfigured)
    }

    private fun perform(call: suspend () -> AuthResult) {
        if (state.value.isLoading) return
        mutableState.update { it.copy(isLoading = true, message = null) }
        viewModelScope.launch {
            try {
                when (call()) {
                    AuthResult.Success -> mutableState.update {
                        it.copy(password = "", passwordVisible = false, message = AuthMessage.SignedIn)
                    }
                    AuthResult.InvalidCredentials -> showMessage(AuthMessage.InvalidCredentials)
                    AuthResult.StorageFailed -> showMessage(AuthMessage.StorageFailed)
                    AuthResult.NotConfigured -> showMessage(AuthMessage.NotConfigured)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                showMessage(AuthMessage.Failed)
            } finally {
                mutableState.update { it.copy(isLoading = false) }
            }
        }
    }
}

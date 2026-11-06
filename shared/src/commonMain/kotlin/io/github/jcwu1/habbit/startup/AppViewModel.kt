package io.github.jcwu1.habbit.startup

import io.github.jcwu1.habbit.onboarding.OnboardingPreferencesRepository
import io.github.jcwu1.habbit.auth.SessionRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

data class StartupState(
    val hasSeenOnboarding: Boolean? = null,
    val isSaving: Boolean = false,
    val isSignedIn: Boolean = false,
    val loadFailed: Boolean = false,
    val saveFailed: Boolean = false,
)

@KoinViewModel
class AppViewModel(
    private val preferences: OnboardingPreferencesRepository,
    private val sessions: SessionRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(StartupState())
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null
    private var refreshJob: Job? = null
    private var expiryJob: Job? = null

    fun load() {
        if (loadJob?.isActive == true || state.value.hasSeenOnboarding != null) return
        mutableState.update { it.copy(loadFailed = false) }
        loadJob = viewModelScope.launch {
            try {
                val seen = preferences.hasSeenOnboarding.first()
                val expiry = sessions.accessTokenExpiresAtMillis()
                applySession(expiry)
                mutableState.update { it.copy(hasSeenOnboarding = seen || expiry != null) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(loadFailed = true) }
            }
        }
    }

    fun refreshSession() {
        if (state.value.hasSeenOnboarding == null) {
            load()
            return
        }
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            try {
                applySession(sessions.accessTokenExpiresAtMillis())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                applySession(null)
            }
        }
    }

    private fun applySession(expiresAtMillis: Long?) {
        expiryJob?.cancel()
        val remaining = expiresAtMillis?.minus(Clock.System.now().toEpochMilliseconds()) ?: 0
        mutableState.update { it.copy(isSignedIn = remaining > 0) }
        if (remaining > 0) {
            expiryJob = viewModelScope.launch {
                delay(remaining)
                mutableState.update { it.copy(isSignedIn = false) }
            }
        }
    }

    fun dismissSaveError() = mutableState.update { it.copy(saveFailed = false) }

    fun completeOnboarding() {
        if (state.value.isSaving || state.value.hasSeenOnboarding != false) return
        mutableState.update { it.copy(isSaving = true, saveFailed = false) }
        viewModelScope.launch {
            try {
                preferences.markSeen()
                mutableState.update { it.copy(hasSeenOnboarding = true, isSaving = false) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(isSaving = false, saveFailed = true) }
            }
        }
    }
}

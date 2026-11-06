package io.github.jcwu1.habbit.startup

import io.github.jcwu1.habbit.onboarding.OnboardingPreferencesRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

data class StartupState(
    val hasSeenOnboarding: Boolean? = null,
    val isSaving: Boolean = false,
    val loadFailed: Boolean = false,
    val saveFailed: Boolean = false,
)

@KoinViewModel
class AppViewModel(private val preferences: OnboardingPreferencesRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(StartupState())
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null

    fun load() {
        if (loadJob?.isActive == true || state.value.hasSeenOnboarding != null) return
        mutableState.update { it.copy(loadFailed = false) }
        loadJob = viewModelScope.launch {
            try {
                val seen = preferences.hasSeenOnboarding.first()
                mutableState.update { it.copy(hasSeenOnboarding = seen) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(loadFailed = true) }
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

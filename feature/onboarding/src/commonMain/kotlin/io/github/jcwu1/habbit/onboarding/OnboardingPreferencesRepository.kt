package io.github.jcwu1.habbit.onboarding

import kotlinx.coroutines.flow.Flow

interface OnboardingPreferencesRepository {
    val hasSeenOnboarding: Flow<Boolean>
    suspend fun markSeen()
}

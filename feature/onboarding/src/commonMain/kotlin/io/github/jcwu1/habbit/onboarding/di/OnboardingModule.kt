package io.github.jcwu1.habbit.onboarding.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module(includes = [PreferencesPlatformModule::class])
@Configuration
@ComponentScan("io.github.jcwu1.habbit.onboarding")
class OnboardingModule

@Module
expect class PreferencesPlatformModule

internal const val preferencesFileName = "habbit.preferences_pb"

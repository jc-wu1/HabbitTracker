package io.github.jcwu1.habbit.onboarding.di

import androidx.datastore.core.DataStore
import androidx.datastore.core.okio.WebLocalStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
actual class PreferencesPlatformModule {
    @Singleton
    fun preferences(): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        storage = WebLocalStorage(PreferencesSerializer, preferencesFileName),
    )
}

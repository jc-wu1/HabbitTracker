package io.github.jcwu1.habbit.onboarding.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Singleton

@Module
actual class PreferencesPlatformModule {
    @Singleton
    fun preferences(@Provided context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.applicationContext.filesDir.resolve(preferencesFileName) }
}

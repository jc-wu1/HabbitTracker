package io.github.jcwu1.habbit.onboarding.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@Module
actual class PreferencesPlatformModule {
    @OptIn(ExperimentalForeignApi::class)
    @Singleton
    fun preferences(): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath {
        val directory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )
        (requireNotNull(directory?.path) + "/$preferencesFileName").toPath()
    }
}

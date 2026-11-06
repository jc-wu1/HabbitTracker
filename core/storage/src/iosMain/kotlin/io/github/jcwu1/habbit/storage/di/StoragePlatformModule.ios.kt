package io.github.jcwu1.habbit.storage.di

import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.KSafeConfig
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
actual class StoragePlatformModule {
    @Singleton
    fun secureStorage(): KSafe = KSafe(
        fileName = secureStoreName,
        config = KSafeConfig(appNamespace = appNamespace),
    )
}

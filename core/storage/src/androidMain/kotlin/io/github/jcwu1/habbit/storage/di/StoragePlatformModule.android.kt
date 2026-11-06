package io.github.jcwu1.habbit.storage.di

import android.content.Context
import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.KSafeConfig
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Singleton

@Module
actual class StoragePlatformModule {
    @Singleton
    fun secureStorage(@Provided context: Context): KSafe = KSafe(
        context = context.applicationContext,
        fileName = secureStoreName,
        config = KSafeConfig(appNamespace = appNamespace),
    )
}

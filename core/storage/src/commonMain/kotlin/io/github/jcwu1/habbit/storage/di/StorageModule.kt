package io.github.jcwu1.habbit.storage.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module(includes = [StoragePlatformModule::class])
@Configuration
@ComponentScan("io.github.jcwu1.habbit.storage")
class StorageModule

@Module
expect class StoragePlatformModule

internal const val secureStoreName = "habbit_session"
internal const val appNamespace = "io.github.jcwu1.habbit"

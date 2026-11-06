package io.github.jcwu1.habbit.network.di

import io.github.jcwu1.habbit.network.SharedHttpClient
import io.ktor.client.HttpClient
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Singleton

@Module
@Configuration
@ComponentScan("io.github.jcwu1.habbit.network")
class NetworkModule {
    @Singleton
    fun httpClient(): HttpClient = SharedHttpClient.instance
}

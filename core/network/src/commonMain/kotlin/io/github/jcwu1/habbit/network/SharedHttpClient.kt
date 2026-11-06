package io.github.jcwu1.habbit.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object SharedHttpClient {
    /**
     * App-lifetime client shared by all features. Do not close it after individual requests.
     * Uses OkHttp on Android, Darwin on iOS, and the JS engine on web targets.
     */
    val instance: HttpClient by lazy { createHttpClient() }
}

internal fun createHttpClient(engine: HttpClientEngine? = null): HttpClient {
    val configure: HttpClientConfig<*>.() -> Unit = {
        expectSuccess = true

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }
    }

    return if (engine == null) HttpClient(configure) else HttpClient(engine, configure)
}

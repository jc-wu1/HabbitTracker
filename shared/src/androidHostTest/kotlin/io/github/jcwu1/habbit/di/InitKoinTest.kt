package io.github.jcwu1.habbit.di

import io.github.jcwu1.habbit.network.SharedHttpClient
import io.ktor.client.HttpClient
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import kotlin.test.Test
import kotlin.test.assertSame

class InitKoinTest {
    @Test
    fun resolvesTheExistingSharedHttpClient() {
        initKoin()
        try {
            val koin = GlobalContext.get()
            val client = koin.get<HttpClient>()

            assertSame(SharedHttpClient.instance, client)
            assertSame(client, koin.get<HttpClient>())
        } finally {
            stopKoin()
        }
    }
}

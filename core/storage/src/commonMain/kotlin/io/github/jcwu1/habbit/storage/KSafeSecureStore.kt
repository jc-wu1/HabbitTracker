package io.github.jcwu1.habbit.storage

import eu.anifantakis.lib.ksafe.KSafe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import org.koin.core.annotation.Singleton

@Singleton
class KSafeSecureStore(private val ksafe: KSafe) : SecureStore {
    override suspend fun <T> get(key: String, defaultValue: T, serializer: KSerializer<T>): T =
        withContext(Dispatchers.Default) {
            awaitStorageReady(ksafe)
            ksafe.get(key, defaultValue, serializer)
        }

    override suspend fun <T> put(key: String, value: T, serializer: KSerializer<T>) {
        withContext(Dispatchers.Default) {
            awaitStorageReady(ksafe)
            ksafe.put(key, value, serializer, ksafe.defaultWriteMode)
        }
    }
}

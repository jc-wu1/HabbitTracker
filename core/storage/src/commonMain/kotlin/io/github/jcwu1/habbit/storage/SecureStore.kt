package io.github.jcwu1.habbit.storage

import kotlinx.serialization.KSerializer

interface SecureStore {
    suspend fun <T> get(key: String, defaultValue: T, serializer: KSerializer<T>): T
    suspend fun <T> put(key: String, value: T, serializer: KSerializer<T>)
}

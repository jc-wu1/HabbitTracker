package io.github.jcwu1.habbit.auth

interface SessionRepository {
    suspend fun accessTokenExpiresAtMillis(): Long?
}

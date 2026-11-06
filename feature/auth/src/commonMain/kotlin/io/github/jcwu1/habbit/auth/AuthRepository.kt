package io.github.jcwu1.habbit.auth

enum class AuthResult { Success, InvalidCredentials, StorageFailed, NotConfigured }

interface AuthRepository {
    suspend fun logIn(username: String, password: String): AuthResult
    suspend fun register(name: String, email: String, password: String): AuthResult
    suspend fun continueWithGoogle(): AuthResult
    suspend fun resetPassword(email: String): AuthResult
}

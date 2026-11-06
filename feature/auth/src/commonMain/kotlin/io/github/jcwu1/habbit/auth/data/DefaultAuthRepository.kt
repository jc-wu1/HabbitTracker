package io.github.jcwu1.habbit.auth.data

import io.github.jcwu1.habbit.auth.AuthRepository
import io.github.jcwu1.habbit.auth.AuthResult
import io.github.jcwu1.habbit.auth.SessionRepository
import io.github.jcwu1.habbit.storage.SecureStore
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import org.koin.core.annotation.Singleton
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.nullable

@Singleton
internal class DefaultAuthRepository(
    private val remote: AuthRemoteDataSource,
    private val store: SecureStore,
) : AuthRepository, SessionRepository {
    override suspend fun accessTokenExpiresAtMillis(): Long? {
        val session = try {
            store.get("auth_session", null, AuthSession.serializer().nullable)
        } catch (_: SerializationException) {
            return null
        }
        return session?.let { accessTokenExpiryMillis(it.accessToken) }
    }

    override suspend fun logIn(username: String, password: String): AuthResult {
        val session = try {
            remote.logIn(username, password)
        } catch (error: ClientRequestException) {
            if (error.response.status == HttpStatusCode.BadRequest ||
                error.response.status == HttpStatusCode.Unauthorized
            ) return AuthResult.InvalidCredentials
            throw error
        }
        require(session.accessToken.isNotBlank() && session.refreshToken.isNotBlank()) {
            "Login response did not contain both tokens"
        }
        return try {
            store.put("auth_session", session, AuthSession.serializer())
            AuthResult.Success
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            AuthResult.StorageFailed
        }
    }

    override suspend fun register(name: String, email: String, password: String) = unavailable()
    override suspend fun continueWithGoogle() = unavailable()
    override suspend fun resetPassword(email: String) = unavailable()

    private suspend fun unavailable(): AuthResult {
        delay(600)
        return AuthResult.NotConfigured
    }
}

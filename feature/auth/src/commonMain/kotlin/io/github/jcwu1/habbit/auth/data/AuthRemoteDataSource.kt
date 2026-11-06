package io.github.jcwu1.habbit.auth.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Singleton

@Singleton
internal class AuthRemoteDataSource(private val client: HttpClient) {
    suspend fun logIn(username: String, password: String): AuthSession =
        client.post("https://dummyjson.com/user/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(username, password, expiresInMins = 60))
        }.body()
}

@Serializable
private data class LoginRequest(val username: String, val password: String, val expiresInMins: Int)

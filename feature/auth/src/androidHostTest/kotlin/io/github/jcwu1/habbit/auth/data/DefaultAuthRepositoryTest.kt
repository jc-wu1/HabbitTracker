package io.github.jcwu1.habbit.auth.data

import io.github.jcwu1.habbit.auth.AuthResult
import io.github.jcwu1.habbit.storage.SecureStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.*
import kotlin.io.encoding.Base64

class DefaultAuthRepositoryTest {
    @Test
    fun restoresTheExistingSavedSessionWithoutCallingTheNetwork() = runTest {
        val payload = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
            .encode("""{"exp":1900000000}""".encodeToByteArray())
        val saved = sessionJson.replace("test-access-token", "header.$payload.signature")
        val store = FakeSecureStore().apply { values["auth_session"] = saved }
        client(MockEngine { error("restoring a session must not call the network") }).use { client ->
            val repository = DefaultAuthRepository(AuthRemoteDataSource(client), store)
            assertEquals(1_900_000_000_000L, repository.accessTokenExpiresAtMillis())
            assertEquals(saved, store.values["auth_session"])
        }
    }

    @Test
    fun missingMalformedAndTokenlessSavedSessionsCannotRestoreHome() = runTest {
        for (saved in listOf(null, sessionJson, "{}", "invalid-json", sessionJson.replace("test-access-token", ""))) {
            val store = FakeSecureStore().apply { if (saved != null) values["auth_session"] = saved }
            client(MockEngine { error("no network expected") }).use { client ->
                assertNull(DefaultAuthRepository(AuthRemoteDataSource(client), store).accessTokenExpiresAtMillis())
            }
        }
    }

    @Test
    fun postsCredentialsAndPersistsTheCompleteSession() = runTest {
        val engine = MockEngine { request ->
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("https://dummyjson.com/user/login", request.url.toString())
            val body = request.body as TextContent
            assertEquals(ContentType.Application.Json, body.contentType)
            val json = Json.parseToJsonElement(body.text).jsonObject
            assertEquals(setOf("username", "password", "expiresInMins"), json.keys)
            assertEquals("emilys", json["username"]?.jsonPrimitive?.content)
            assertEquals("request-only-password", json["password"]?.jsonPrimitive?.content)
            assertEquals("60", json["expiresInMins"]?.jsonPrimitive?.content)
            respond(sessionJson, headers = jsonHeaders)
        }
        client(engine).use { client ->
            val store = FakeSecureStore()
            val repository = DefaultAuthRepository(AuthRemoteDataSource(client), store)
            assertEquals(AuthResult.Success, repository.logIn("emilys", "request-only-password"))
            val saved = assertNotNull(store.values["auth_session"])
            assertEquals(Json.parseToJsonElement(sessionJson), Json.parseToJsonElement(saved))
            assertFalse(saved.contains("request-only-password"))
            assertEquals(setOf("auth_session"), store.values.keys)
        }
    }

    @Test
    fun successWaitsForTheSessionCommit() = runTest {
        val writeStarted = CompletableDeferred<Unit>()
        val commit = CompletableDeferred<Unit>()
        val store = FakeSecureStore {
            writeStarted.complete(Unit)
            commit.await()
        }
        client(MockEngine { respond(sessionJson, headers = jsonHeaders) }).use { client ->
            val result = async { DefaultAuthRepository(AuthRemoteDataSource(client), store).logIn("emilys", "pw") }
            try {
                withContext(Dispatchers.Default) {
                    withTimeout(5_000) { writeStarted.await() }
                }
                assertFalse(result.isCompleted)
                assertTrue(store.values.isEmpty())
            } finally {
                commit.complete(Unit)
            }
            assertEquals(AuthResult.Success, result.await())
            assertNotNull(store.values["auth_session"])
        }
    }

    @Test
    fun rejectedCredentialsPreserveTheExistingSession() = runTest {
        for (status in listOf(HttpStatusCode.BadRequest, HttpStatusCode.Unauthorized)) {
            val store = FakeSecureStore().apply { values["auth_session"] = sessionJson }
            client(MockEngine { respond("""{"message":"Invalid credentials"}""", status, jsonHeaders) }).use { client ->
                val result = DefaultAuthRepository(AuthRemoteDataSource(client), store).logIn("wrong", "wrong")
                assertEquals(AuthResult.InvalidCredentials, result)
                assertEquals(mapOf("auth_session" to sessionJson), store.values)
            }
        }
    }

    @Test
    fun storageFailureDoesNotReportSuccessOrOverwriteTheSession() = runTest {
        val store = FakeSecureStore { error("storage unavailable") }.apply { values["auth_session"] = sessionJson }
        client(MockEngine { respond(sessionJson, headers = jsonHeaders) }).use { client ->
            val result = DefaultAuthRepository(AuthRemoteDataSource(client), store).logIn("emilys", "pw")
            assertEquals(AuthResult.StorageFailed, result)
            assertEquals(mapOf("auth_session" to sessionJson), store.values)
        }
    }

    @Test
    fun malformedOrTokenlessResponsesNeverReplaceTheSession() = runTest {
        for (response in listOf("""{"id":1}""", sessionJson.replace("test-refresh-token", ""))) {
            val store = FakeSecureStore().apply { values["auth_session"] = sessionJson }
            client(MockEngine { respond(response, headers = jsonHeaders) }).use { client ->
                assertFails {
                    DefaultAuthRepository(AuthRemoteDataSource(client), store).logIn("emilys", "pw")
                }
                assertEquals(mapOf("auth_session" to sessionJson), store.values)
            }
        }
    }

    @Test
    fun serverFailureNeverWritesASession() = runTest {
        val store = FakeSecureStore()
        client(MockEngine { respond("Unavailable", HttpStatusCode.InternalServerError) }).use { client ->
            assertFails { DefaultAuthRepository(AuthRemoteDataSource(client), store).logIn("emilys", "pw") }
            assertTrue(store.values.isEmpty())
        }
    }

    @Test
    fun cancellationPropagatesInsteadOfBecomingAStorageError() = runTest {
        val store = FakeSecureStore { throw CancellationException("cancelled") }
        client(MockEngine { respond(sessionJson, headers = jsonHeaders) }).use { client ->
            assertFailsWith<CancellationException> {
                DefaultAuthRepository(AuthRemoteDataSource(client), store).logIn("emilys", "pw")
            }
            assertTrue(store.values.isEmpty())
        }
    }
}

private fun client(engine: MockEngine) = HttpClient(engine) {
    expectSuccess = true
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
}

private class FakeSecureStore(
    private val beforePut: suspend () -> Unit = {},
) : SecureStore {
    val values = mutableMapOf<String, String>()
    override suspend fun <T> get(key: String, defaultValue: T, serializer: KSerializer<T>): T =
        values[key]?.let { Json.decodeFromString(serializer, it) } ?: defaultValue

    override suspend fun <T> put(key: String, value: T, serializer: KSerializer<T>) {
        beforePut()
        values[key] = Json.encodeToString(serializer, value)
    }
}

private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
private const val sessionJson = """{
    "id":1,
    "username":"emilys",
    "email":"emily.johnson@x.dummyjson.com",
    "firstName":"Emily",
    "lastName":"Johnson",
    "gender":"female",
    "image":"https://dummyjson.com/icon/emilys/128",
    "accessToken":"test-access-token",
    "refreshToken":"test-refresh-token"
}"""

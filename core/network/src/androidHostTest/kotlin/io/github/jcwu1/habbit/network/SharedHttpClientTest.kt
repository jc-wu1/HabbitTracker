package io.github.jcwu1.habbit.network

import io.ktor.client.call.body
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SharedHttpClientTest {
    @Test
    fun decodesJsonWithUnknownFields() = runBlocking {
        val engine = MockEngine {
            respond(
                content = """{"name":"Read","serverOnly":true}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }

        engine.use {
            createHttpClient(engine).use { client ->
                val habit = client.get("https://example.com/habits/1").body<HabitPayload>()

                assertEquals(HabitPayload("Read"), habit)
            }
        }
    }

    @Test
    fun serializesJsonRequestBodies() = runBlocking {
        val engine = MockEngine { request ->
            assertEquals("""{"name":"Read"}""", (request.body as TextContent).text)
            respond("", HttpStatusCode.NoContent)
        }

        engine.use {
            createHttpClient(engine).use { client ->
                val response = client.post("https://example.com/habits") {
                    contentType(ContentType.Application.Json)
                    setBody(HabitPayload("Read"))
                }

                assertEquals(HttpStatusCode.NoContent, response.status)
            }
        }
    }

    @Test
    fun throwsForHttpErrors() = runBlocking {
        val engine = MockEngine {
            respond("Missing habit", HttpStatusCode.NotFound)
        }

        engine.use {
            createHttpClient(engine).use { client ->
                val error = assertFailsWith<ClientRequestException> {
                    client.get("https://example.com/habits/1")
                }

                assertEquals(HttpStatusCode.NotFound, error.response.status)
            }
        }
    }

}

@Serializable
private data class HabitPayload(val name: String)

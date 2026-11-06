package io.github.jcwu1.habbit.auth.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64

private val tokenJson = Json { ignoreUnknownKeys = true }
private val tokenBase64 = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)

// Reads the deadline for local navigation; authenticated APIs still validate the token on the server.
internal fun accessTokenExpiryMillis(token: String): Long? {
    val parts = token.split('.')
    if (parts.size != 3 || parts.any { it.isBlank() }) return null
    return try {
        val payload = tokenBase64.decode(parts[1]).decodeToString(throwOnInvalidSequence = true)
        val expiry = tokenJson.decodeFromString<AccessTokenClaims>(payload).exp
        expiry.takeIf { it in 0..Long.MAX_VALUE / 1_000 }?.times(1_000)
    } catch (_: Exception) {
        null
    }
}

@Serializable
private data class AccessTokenClaims(val exp: Long)

package io.github.jcwu1.habbit.auth.data

import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AccessTokenExpiryTest {
    @Test
    fun readsAnUnpaddedJwtExpiryAsEpochMilliseconds() {
        assertEquals(1_900_000_000_000L, accessTokenExpiryMillis(jwt("""{"sub":"1","exp":1900000000}""")))
    }

    @Test
    fun preservesExpiredDatesSoTheyCannotBeTreatedAsFreshSessions() {
        assertEquals(1_000L, accessTokenExpiryMillis(jwt("""{"exp":1}""")))
    }

    @Test
    fun rejectsMalformedMissingAndInvalidExpiryClaims() {
        for (token in listOf(
            "", "not-a-jwt", "header.%.signature",
            jwt("not-json"), jwt("""{}"""), jwt("""{"exp":"tomorrow"}"""),
            jwt("""{"exp":-1}"""), jwt("""{"exp":9223372036854775807}"""),
        )) {
            assertNull(accessTokenExpiryMillis(token), token)
        }
    }
}

private fun jwt(payload: String): String =
    "header." + Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).encode(payload.encodeToByteArray()) + ".signature"

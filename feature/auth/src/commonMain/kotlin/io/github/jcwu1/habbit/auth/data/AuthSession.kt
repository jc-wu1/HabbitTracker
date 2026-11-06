package io.github.jcwu1.habbit.auth.data

import kotlinx.serialization.Serializable

@Serializable
internal data class AuthSession(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val image: String,
    val accessToken: String,
    val refreshToken: String,
)

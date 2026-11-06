package io.github.jcwu1.habbit.auth

enum class FieldError { Required, InvalidEmail, WeakPassword }

data class AuthErrors(
    val username: FieldError? = null,
    val name: FieldError? = null,
    val email: FieldError? = null,
    val password: FieldError? = null,
) {
    val hasErrors: Boolean get() = username != null || name != null || email != null || password != null
}

fun validateEmail(email: String): FieldError? = when {
    email.isBlank() -> FieldError.Required
    !Regex("^[^\\s@]+@[^\\s@.]+(?:\\.[^\\s@.]+)+$").matches(email.trim()) -> FieldError.InvalidEmail
    else -> null
}

fun validatePassword(password: String): FieldError? = when {
    password.isBlank() -> FieldError.Required
    password.length < 8 || password.none { it.isDigit() } -> FieldError.WeakPassword
    else -> null
}

fun validateAuth(name: String, identifier: String, password: String, register: Boolean) = AuthErrors(
    username = if (!register && identifier.isBlank()) FieldError.Required else null,
    name = if (register && name.isBlank()) FieldError.Required else null,
    email = if (register) validateEmail(identifier) else null,
    password = if (register) validatePassword(password) else if (password.isBlank()) FieldError.Required else null,
)

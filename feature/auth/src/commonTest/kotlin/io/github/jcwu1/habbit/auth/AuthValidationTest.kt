package io.github.jcwu1.habbit.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthValidationTest {
    @Test
    fun emptyRegistrationFieldsHaveInlineErrors() {
        val errors = validateAuth("", "", "", register = true)
        assertEquals(FieldError.Required, errors.name)
        assertEquals(FieldError.Required, errors.email)
        assertEquals(FieldError.Required, errors.password)
    }

    @Test
    fun rejectsMalformedEmailAndPasswordsWithoutMinimumOrNumber() {
        for (email in listOf("hello", "a@", "@example.com", "a b@example.com", "a@example")) {
            assertEquals(FieldError.InvalidEmail, validateEmail(email))
        }
        for (password in listOf("short1", "longpassword")) {
            assertEquals(FieldError.WeakPassword, validatePassword(password))
        }
    }

    @Test
    fun loginAcceptsUsernameAndExistingPasswordWithoutSignupStrengthRules() {
        assertFalse(validateAuth("", "emilys", "pw", register = false).hasErrors)
    }

    @Test
    fun loginDoesNotRequireNameAndValidInputsPass() {
        assertFalse(validateAuth("", "learner@example.com", "language1", register = false).hasErrors)
        assertTrue(validateAuth("  ", "learner@example.com", "language1", register = true).hasErrors)
        assertFalse(validateAuth("Learner", "learner@example.com", "language1", register = true).hasErrors)
    }
}

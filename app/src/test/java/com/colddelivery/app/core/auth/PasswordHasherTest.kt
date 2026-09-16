package com.colddelivery.app.core.auth

import org.junit.Assert.*
import org.junit.Test

class PasswordHasherTest {
    private val testPassword = "TestPassword123!"
    @Test fun `hash is not plaintext and matches original`() {
        val first = PasswordHasher.create(testPassword)
        val second = PasswordHasher.create(testPassword)
        assertNotEquals(testPassword, first.hash)
        assertTrue(PasswordHasher.matches(testPassword, first.hash, first.salt, first.iterations))
        assertFalse(PasswordHasher.matches("wrong", first.hash, first.salt, first.iterations))
        assertNotEquals(first.hash, second.hash)
    }
}

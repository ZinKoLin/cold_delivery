package com.colddelivery.app.core.auth

import org.junit.Assert.*
import org.junit.Test

class PasswordHasherTest {
    @Test fun `hash is not plaintext and matches original`() {
        val first = PasswordHasher.create("cold2026")
        val second = PasswordHasher.create("cold2026")
        assertNotEquals("cold2026", first.hash)
        assertTrue(PasswordHasher.matches("cold2026", first.hash, first.salt, first.iterations))
        assertFalse(PasswordHasher.matches("wrong", first.hash, first.salt, first.iterations))
        assertNotEquals(first.hash, second.hash)
    }
}

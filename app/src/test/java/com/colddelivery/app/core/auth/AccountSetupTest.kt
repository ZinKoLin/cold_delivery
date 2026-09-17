package com.colddelivery.app.core.auth

import org.junit.Assert.*
import org.junit.Test

class AccountSetupTest {
    @Test fun `password validation rejects empty short and mismatched values`() {
        assertEquals(PasswordSetupError.EMPTY, validatePasswordSetup("", ""))
        assertEquals(PasswordSetupError.TOO_SHORT, validatePasswordSetup("short", "short"))
        assertEquals(PasswordSetupError.MISMATCH, validatePasswordSetup("long-enough", "different"))
        assertNull(validatePasswordSetup("long-enough", "long-enough"))
    }

    @Test fun `release setup is required only when no user exists`() {
        assertTrue(shouldShowFirstRunSetup(isDebug = false, userExists = false))
        assertFalse(shouldShowFirstRunSetup(isDebug = false, userExists = true))
        assertFalse(shouldShowFirstRunSetup(isDebug = true, userExists = false))
    }

    @Test fun `created password uses salted PBKDF2 storage`() {
        val password = "CreatedPassword123!"
        val stored = PasswordHasher.create(password)
        assertNotEquals(password, stored.hash)
        assertTrue(PasswordHasher.matches(password, stored.hash, stored.salt, stored.iterations))
        assertFalse(PasswordHasher.matches("wrong-password", stored.hash, stored.salt, stored.iterations))
        assertTrue(stored.salt.isNotBlank())
        assertEquals(PasswordHasher.DefaultIterations, stored.iterations)
    }
}
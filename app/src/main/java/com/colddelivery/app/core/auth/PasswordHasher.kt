package com.colddelivery.app.core.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    const val DefaultIterations = 210_000
    private const val SaltBytes = 16
    private const val KeyBits = 256
    data class Stored(val hash: String, val salt: String, val iterations: Int)

    fun create(password: String, iterations: Int = DefaultIterations): Stored {
        val salt = ByteArray(SaltBytes).also { SecureRandom().nextBytes(it) }
        return Stored(derive(password, salt, iterations), Base64.getEncoder().encodeToString(salt), iterations)
    }

    fun matches(password: String, storedHash: String, storedSalt: String, iterations: Int): Boolean {
        if (storedHash.isBlank() || storedSalt.isBlank() || iterations <= 0) return false
        val derived = derive(password, Base64.getDecoder().decode(storedSalt), iterations)
        return MessageDigest.isEqual(derived.toByteArray(Charsets.US_ASCII), storedHash.toByteArray(Charsets.US_ASCII))
    }

    private fun derive(password: String, salt: ByteArray, iterations: Int): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KeyBits)
        return try {
            val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            Base64.getEncoder().encodeToString(key)
        } finally { spec.clearPassword() }
    }
}

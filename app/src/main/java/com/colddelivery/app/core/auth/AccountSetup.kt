package com.colddelivery.app.core.auth

enum class PasswordSetupError { EMPTY, TOO_SHORT, MISMATCH }

fun validatePasswordSetup(password: String, confirmation: String): PasswordSetupError? = when {
    password.isEmpty() -> PasswordSetupError.EMPTY
    password.length < 8 -> PasswordSetupError.TOO_SHORT
    password != confirmation -> PasswordSetupError.MISMATCH
    else -> null
}

fun shouldShowFirstRunSetup(isDebug: Boolean, userExists: Boolean): Boolean = !isDebug && !userExists
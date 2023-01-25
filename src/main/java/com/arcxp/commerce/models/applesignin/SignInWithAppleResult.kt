package com.arcxp.commerce.models.applesignin

/**
 * @suppress
 */
sealed class SignInWithAppleResult {
    data class Success(val authorizationCode: String) : SignInWithAppleResult()

    data class Failure(val error: Throwable) : SignInWithAppleResult()

    object Cancel : SignInWithAppleResult()
}

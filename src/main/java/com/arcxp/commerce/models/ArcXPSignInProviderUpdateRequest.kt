package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPSignInProviderUpdateRequest(
    val apiKey: String,
    val endPoint: String,
    val publicKey: String,
    val pwMinLength: Int,
    val pwMaxLength: Int,
    val pwSpecialCharacters: Int,
    val pwLowerCase: Int,
    val pwUpperCase: Int,
    val pwNumbers: Int,
    val passwordMigrationType: String,
    val passwordMigrationAlgorithm: String,
    val passwordMigrationSalt: String,
    val userNameIsEmail: Boolean,
    val teamId: String,
    val keyId: String,
    val urlToReceiveAuthToken: String
) {
    companion object {
        enum class PasswordMigrationType {
            Reset, Sha2, Copy
        }
    }
}
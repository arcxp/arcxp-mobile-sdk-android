package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPSignInProvider(
    val createdOn: String,
    val createdBy: String,
    val modifiedOn: String,
    val modifiedBy: String,
    val deletedOn: String,
    val id: Int,
    val identityTypeId: Int,
    val apiKey: String,
    val publicKey: String,
    val endPoint: String,
    val pwMinLength: Int,
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
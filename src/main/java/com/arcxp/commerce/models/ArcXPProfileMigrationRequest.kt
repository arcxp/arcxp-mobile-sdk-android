package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPProfileMigrationRequest(
    val firstName: String,
    val lastName: String,
    val secondLastName: String,
    val displayName: String,
    val gender: String,
    val email: String,
    val picture: String,
    val birthYear: String,
    val birthMonth: String,
    val birthDay: String,
    val contacts: List<ArcXPContactRequest>,
    val addresses: List<ArcXPAddressRequest>,
    val attributes: List<ArcXPAttributeRequest>,
    val legacyId: String,
    val deletionRule: Int,
    val emailVerified: Boolean,
    val createdOn: String
)
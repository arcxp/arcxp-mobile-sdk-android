package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val secondLastName: String? = null,
    val displayName: String? = null,
    val gender: String? = null,
    val email: String,
    val picture: String? = null,
    val birthYear: String? = null,
    val birthMonth: String? = null,
    val birthDay: String? = null,
    val legacyId: String? = null,
    val deletionRule: Int? = null,
    val contacts: ArcXPContactRequest?  = null,
    val addresses: ArcXPAddressRequest?  = null,
    val attributes: ArcXPAttributeRequest?  = null
)
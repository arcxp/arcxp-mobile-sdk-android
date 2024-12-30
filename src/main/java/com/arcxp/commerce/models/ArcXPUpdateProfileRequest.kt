package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
class ArcXPUpdateProfileRequest(val firstName: String? = null,
                                val lastName: String? = null,
                                val secondLastName: String? = null,
                                val displayName: String? = null,
                                val gender: String? = null,
                                val email: String? = null,
                                val picture: String? = null,
                                val birthYear: String? = null,
                                val birthMonth: String? = null,
                                val birthDay: String? = null,
                                val legacyId: String? = null,
                                val contacts: List<ArcXPContactRequest>? = null,
                                val addresses: List<ArcXPAddressRequest>? = null,
                                val attributes: List<ArcXPAttributeRequest>? = null)
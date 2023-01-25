package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPFinalizePaymentRequest(
    val token:String?,
    val email: String?,
    val address: ArcXPAddressRequest?,
    val phone: String?,
    val browserInfo: String?,
    val firstName: String?,
    val lastName: String?
)
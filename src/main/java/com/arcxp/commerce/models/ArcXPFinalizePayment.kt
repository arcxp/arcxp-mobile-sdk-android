package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPFinalizePayment(
    val redirectURL: String?,
    val creditCardFirstSix: String?,
    val creditCardLastFour: String?,
    val cardHolderName: String?,
    val creditCardTypeID: Int?,
    val paymentProviderID: Int?,
    val token: String?,
    val expiration: String?,
    val identificationNumber: String?,
    val payerID: String?
)
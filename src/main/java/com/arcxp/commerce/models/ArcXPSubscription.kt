package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPSubscriptions(val response: List<SubscriptionSummary>)

@Keep
data class SubscriptionSummary(
    val paymentMethod: PaymentMethod?,
    val productName: String?,
    val sku: String?,
    val statusID: String,
    val subscriptionID: Number,
    val attributes: List<Map<String, String>>,
    val currentRetailCycleIDX: Int) {

    companion object {
        enum class SubscriptionStatus(val value: Int, val text: String) {
            ACTIVE(1, "Active"),
            TERMINATED(2, "Terminated"),
            CANCELED(3, "Canceled"),
            SUSPENDED(4, "Suspended"),
            GIFTED(5, "Gifted")
        }
    }
}

@Keep
data class SubscriptionAttribute(val key: String, val value: String)

@Keep
data class PaymentMethod(
    val cardHolderName:String?,
    val creditCardType: String?,
    val expiration: String?,
    val firstSix: String?,
    val lastFour: String?,
    val paymentMethodID: Number,
    val paymentPartner: String)
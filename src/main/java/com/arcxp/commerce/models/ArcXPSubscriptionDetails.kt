package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPSubscriptionDetails(val subscriptionID: String?,
                                    val sku: String?,
                                    val nextEventDateUTC: String?,
                                    val subscriptionType: Int?,
                                    val status: Int?,
                                    val events: List<Event?>?,
                                    val salesOrder: List<SalesOrder?>?,
                                    val paymentHistory: List<PaymentHistory?>?,
                                    val productName: String?,
                                    val priceCode: String?,
                                    val currentPaymentMethod: CurrentPaymentMethod?,
                                    val clientID: String?,
                                    val billingAddress: ArcXPAddress?,
                                    val subscriptionAttributes: List<SubscriptionAttributes?>?,
                                    val currentRetailCycleIDX: Int?,
                                    val upcomingPriceChanges: List<UpcomingPriceChanges?>?
)

/**
 * @suppress
 */
@Keep
data class Event(val eventDateUTC: String?,
                 val details: String?,
                 val eventType: String?,
                 val reasonCode: String?,
                 val csrClientId: String?)

/**
 * @suppress
 */
@Keep
data class SalesOrder(val id: String?,
                      val orderDateUTC: String?,
                      val orderNumber: String?,
                      val status: Int?,
                      val tax: Double?,
                      val shipping: Double?,
                      val total: Double?,
                      val currency: String?)

/**
 * @suppress
 */
@Keep
data class PaymentHistory(val sku: String?,
                          val currency: String?,
                          val total: Double?,
                          val tax: Double?,
                          val transactionDate: String?,
                          val periodFrom: String?,
                          val periodTo: String?)

/**
 * @suppress
 */
@Keep
data class CurrentPaymentMethod(val creditCardType: String?,
                                val firstSix: String?,
                                val lastFour: String?,
                                val expiration: String?,
                                val cardHolderName: String?,
                                val identificationNumber: String?,
                                val documentType: String?,
                                val paymentPartner: String?,
                                val paymentMethodID: Int?)

/**
 * @suppress
 */
@Keep
data class SubscriptionAttributes(val name: String?, val value: String?)

/**
 * @suppress
 */
@Keep
data class UpcomingPriceChanges(val effectiveDataUTC: String?,
                                val sku: String?,
                                val priceCode: String?,
                                val cycleIndex: Int?,
                                val id: Int?)

package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPCustomerOrder(val total: Double?,
                              val subtotal: Double?,
                              val tax: Double?,
                              val shipping: Double?,
                              val items: List<CartItem?>?,
                              val currency: String?,
                              val taxSupported: Boolean?,
                              val orderNumber: String?,
                              val status: String?,
                              val email: String?,
                              val phone: String?,
                              val subscriptionIDs: String?,
                              val firstName: String?,
                              val lastName: String?,
                              val secondLastName: String?,
                              val orderDateUTC: String?,
                              val tempPassword: String?,
                              val giftRedeemInfo: List<GiftRedeemInfo?>?,
                              val taxDelegated: Boolean?,
                              val paymentPending: Boolean?)

@Keep
data class CartItem(val sku: String?,
                    val quantity:Int?,
                    val shortDescription: String?,
                    val name: String?,
                    val price: Double?,
                    val tax: Double?,
                    val subtotal: Double?,
                    val total: Double?,
                    val priceCode: String?,
                    val gift: Boolean?)

@Keep
data class GiftRedeemInfo(val redeemCode: String?,
                          val sku: String?,
                          val priceCode: String?)
package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPCustomerOrderRequest(val email: String?,
                                     val phone: String?,
                                     val shippingAddress: ArcXPAddressRequest?,
                                     val billingAddress: ArcXPAddressRequest?,
                                     val firstName: String?,
                                     val lastName: String?,
                                     val secondLastName: String?)
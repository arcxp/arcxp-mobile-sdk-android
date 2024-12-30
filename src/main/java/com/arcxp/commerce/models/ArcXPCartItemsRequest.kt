package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPCartItemsRequest(val items: List<CartItem?>?,
                                 val billingAddress: ArcXPAddressRequest?)
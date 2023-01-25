package com.arcxp.commerce.models

import androidx.annotation.Keep


/**
 * @suppress
 */
@Keep
data class ArcXPCancelSubscriptionRequest(val reason: String = "User Requested")
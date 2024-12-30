package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPCancelSubscriptionRequest(val reason: String = "User Requested")
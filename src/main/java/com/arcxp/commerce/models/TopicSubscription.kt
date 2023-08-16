package com.arcxp.commerce.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TopicSubscription(val displayName: String, val name: String, var subscribed: Boolean)

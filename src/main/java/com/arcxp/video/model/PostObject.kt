package com.arcxp.video.model

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class PostObject(
    val manifestUrl: String,
    val trackingUrl: String
)
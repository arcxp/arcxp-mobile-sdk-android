package com.arc.arcvideo.model

import androidx.annotation.Keep

@Keep
data class PostObject(
    val manifestUrl: String,
    val trackingUrl: String
)
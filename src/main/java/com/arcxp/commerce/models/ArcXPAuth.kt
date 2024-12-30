package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
public data class ArcXPAuth(
    val uuid: String,
    val accessToken: String,
    val refreshToken: String,
    val dn: String,
    val un: String,
    val jti: String
)
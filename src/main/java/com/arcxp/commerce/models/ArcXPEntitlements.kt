package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPEntitlements(val skus: List<Sku>?, //V1 entitlements have SKUs
                             val zones: List<Int>?,  //V2 entitlements have zones
                             val edgescape: Edgescape?)

@Keep
data class Sku(val sku: String)

@Keep
data class Edgescape(val city: String?, val continent: String?, val georegion: String?, val dma: String?, val country_code: String?)
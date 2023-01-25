package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPEntitlements(val skus: List<Sku>, val edgescape: Edgescape?)

/**
 * @suppress
 */
@Keep
data class Sku(val sku: String)

/**
 * @suppress
 */
@Keep
data class Edgescape(val city: String?, val continent: String?, val georegion: String?, val dma: String?, val country_code: String?)
package com.arcxp.video.model

import com.squareup.moshi.Json

/**
 * @suppress
 */
public data class ArcTypeResponse(
        val type: String,
        val allow: Boolean,
        @Json(name = "_params")val params: TypeParams,
        @Json(name = "computed_location")val computedLocation: ComputedLocation
)
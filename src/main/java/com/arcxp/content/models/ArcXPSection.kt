package com.arcxp.content.models

import androidx.annotation.Keep
import com.squareup.moshi.Json

/**
 * Response service object for Section List/Navigation Call
 *
 * @property id
 * @property website
 * @property type
 * @property name
 * @property sections
 */
@Keep
data class ArcXPSection(
    @Json(name = "_id") val id: String,
    @Json(name = "_website") val website: String,
    @Json(name = "node_type") val type: String,
    val name: String,
    @Json(name = "navigation") val navigation: Navigation,
    @Json(name = "children") val sections: List<ArcXPSection>?
)

@Keep
data class Navigation(
    @Json(name = "nav_title") val nav_title: String?
)


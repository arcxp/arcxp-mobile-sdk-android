package com.arcxp.content.models

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response service object for Section List/Navigation Call
 *
 * @property id
 * @property website
 * @property type
 * @property name
 * @property children
 */
@Keep
@JsonClass(generateAdapter = true)
data class ArcXPSection(
    @Json(name = "_id") val id: String,
    @Json(name = "_website") val website: String,
    @Json(name = "node_type") val type: String,
    val name: String,
    val navigation: Navigation,
    val children: List<ArcXPSection>?,
    val parent: Map<String, String>?,
    val ancestors: Map<String, *>?,
    val site: SiteServiceSite?,
    val social: SiteServiceSocial?,
    //TODO there are some more fields here, can't find ANS entry though..
)


@Keep
@JsonClass(generateAdapter = true)
data class SiteServiceSite(
    @Json(name = "site_url") val siteUrl: String?,
    @Json(name = "site_about") val siteAbout: String?,
    @Json(name = "site_description") val siteDescription: String?,
    @Json(name = "pagebuilder_path_for_native_apps") val pageBuilderPathForNativeApps: String?,
    @Json(name = "site_taglined") val siteTagline: String?,
    @Json(name = "site_title") val siteTitle: String?,
    @Json(name = "sitesite_keywords_title") val siteKeywordsTitle: String?,
)

@Keep
@JsonClass(generateAdapter = true)
data class SiteServiceSocial(
    val rss: String?,
    val twitter: String?,
    val facebook: String?,
    val instagram: String?,
)

@Keep
@JsonClass(generateAdapter = true)
data class Navigation(
    @Json(name = "nav_title") val nav_title: String?
)


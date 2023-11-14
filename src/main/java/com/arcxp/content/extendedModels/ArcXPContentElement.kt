package com.arcxp.content.extendedModels

import androidx.annotation.Keep
import com.arcxp.ArcXPMobileSDK.imageUtils
import com.arcxp.content.models.*
import com.arcxp.commons.util.Utils.formatter
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@Keep
@JsonClass(generateAdapter = true)
data class ArcXPContentElement(
    val additional_properties: AdditionalProperties?,
    val created_date: String?,
    val display_date: String?,
    val first_publish_date: String?,
    val last_updated_date: String?,
    val owner: Owner?,
    val publish_date: Date?,
    val publishing: Publishing?,
    val revision: Revision?,
    val type: String,
    val version: String?,
    val _id: String,
    val website: String?,
    val address: Address?,
    val content_elements: List<ArcXPContentElement>?,
    val caption: String?,
    val credits: Credits?,
    val geo: Geo?,
    val height: Int?,
    val width: Int?,
    val licensable: Boolean?,
    val newKeywords: String?,
    val referent_properties: ReferentProperties?,
    val selectedGalleries: List<String>?,
    val subtitle: String?,
    val taxonomy: Taxonomy?,
    val url: String?,
    val copyright: String?,
    val description: Description?,
    val headlines: Headline?,
    val language: String?,
    val location: String?,
    @Json(name = "promo_items") val promoItem: PromoItem?,
    val video_type: String?,
    val canonical_url: String?,
    val subtype: String?,
    val content: String?,
    val embed_html: String?,
    val subheadlines: Subheadlines?,
    val streams: List<Streams>?,
    val duration: Long?,
    val auth: Map<String, String>?
)

fun ArcXPContentElement.author(): String {
    if (this.credits?.by?.isNotEmpty() == true) {
        return this.credits.by.get(0).name ?: ""
    }
    return ""
}

fun ArcXPContentElement.title() = this.headlines?.basic ?: ""

fun ArcXPContentElement.description() = this.description?.basic ?: ""

fun ArcXPContentElement.date() = this.publish_date?.let { formatter.format(it) } ?: ""

fun ArcXPContentElement.thumbnail() = this.promoItem?.let { promoItem ->
        imageUtils().thumbnail(promoItem)
    } ?: ""

fun ArcXPContentElement.fallback() = this.promoItem?.let { promoItem ->
        imageUtils().fallback(promoItem)
    } ?: ""

fun ArcXPContentElement.imageUrl(): String = this.promoItem?.basic?.let { promoItem ->
        imageUtils().imageUrl(promoItem)
    } ?: ""


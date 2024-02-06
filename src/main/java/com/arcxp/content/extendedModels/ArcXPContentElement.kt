package com.arcxp.content.extendedModels

import androidx.annotation.Keep
import androidx.media3.common.util.UnstableApi
import com.arcxp.ArcXPMobileSDK.imageUtils
import com.arcxp.commons.util.Utils
import com.arcxp.commons.util.Utils.formatter
import com.arcxp.content.models.AdditionalProperties
import com.arcxp.content.models.Address
import com.arcxp.content.models.Credits
import com.arcxp.content.models.Description
import com.arcxp.content.models.Geo
import com.arcxp.content.models.Headline
import com.arcxp.content.models.Owner
import com.arcxp.content.models.PromoItem
import com.arcxp.content.models.Publishing
import com.arcxp.content.models.ReferentProperties
import com.arcxp.content.models.Revision
import com.arcxp.content.models.Streams
import com.arcxp.content.models.Subheadlines
import com.arcxp.content.models.Taxonomy
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

@Keep
@JsonClass(generateAdapter = true)
data class ArcXPContentElement(
    val additional_properties: AdditionalProperties? = null,
    val created_date: String? = null,
    val display_date: String? = null,
    val first_publish_date: String? = null,
    val last_updated_date: String? = null,
    val owner: Owner? = null,
    val publish_date: Date? = null,
    val publishing: Publishing? = null,
    val revision: Revision? = null,
    val type: String,
    val version: String? = null,
    val _id: String,
    val website: String? = null,
    val address: Address? = null,
    val content_elements: List<ArcXPContentElement>? = null,
    val caption: String? = null,
    val credits: Credits? = null,
    val geo: Geo? = null,
    val height: Int? = null,
    val width: Int? = null,
    val licensable: Boolean? = null,
    val newKeywords: String? = null,
    val referent_properties: ReferentProperties? = null,
    val selectedGalleries: List<String>? = null,
    val subtitle: String? = null,
    val taxonomy: Taxonomy? = null,
    val url: String? = null,
    val copyright: String? = null,
    val description: Description? = null,
    val headlines: Headline? = null,
    val language: String? = null,
    val location: String? = null,
    @Json(name = "promo_items") val promoItem: PromoItem? = null,
    val video_type: String? = null,
    val canonical_url: String? = null,
    val subtype: String? = null,
    val content: String? = null,
    val embed_html: String? = null,
    val subheadlines: Subheadlines? = null,
    val streams: List<Streams>? = null,
    val duration: Long? = null,
    val auth: Map<String, String>? = null
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

@UnstableApi
fun ArcXPContentElement.thumbnail() = this.promoItem?.let { promoItem ->
        imageUtils().thumbnail(promoItem)
    } ?: ""

@UnstableApi
fun ArcXPContentElement.fallback() = this.promoItem?.let { promoItem ->
        imageUtils().fallback(promoItem)
    } ?: ""

@UnstableApi
fun ArcXPContentElement.imageUrl(): String = this.promoItem?.basic?.let { promoItem ->
        imageUtils().imageUrl(promoItem)
    } ?: ""

@UnstableApi
fun ArcXPContentElement.isVideo() = type == Utils.AnsTypes.VIDEO.type
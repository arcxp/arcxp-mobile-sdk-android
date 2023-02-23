package com.arcxp.content.extendedModels

import androidx.annotation.Keep
import com.arcxp.ArcXPMobileSDK.resizer
import com.arcxp.content.models.*
import com.arcxp.content.util.Constants
import com.arcxp.content.util.Constants.THUMBNAIL_RESIZE_URL_KEY
import com.arcxp.content.util.createFullImageUrl
import com.arcxp.content.util.formatter
import com.squareup.moshi.Json
import java.util.*

@Keep
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
    val duration: Long?
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

fun ArcXPContentElement.thumbnail() =
    if (type == "video") {
        promoItem?.basic?.url?.let { resizer().createThumbnail(it.substringAfter("=/")) } ?: ""
    } else {
        fallback()
    }

fun ArcXPContentElement.fallback() =
    promoItem?.let { promoItem ->
        if (type == "video") {
            promoItem.basic?.url ?: ""
        } else {
            (promoItem.basic?.additional_properties?.get(THUMBNAIL_RESIZE_URL_KEY) as? String
                ?: promoItem.lead_art?.additional_properties?.get(THUMBNAIL_RESIZE_URL_KEY) as? String
                ?: promoItem.lead_art?.promo_items?.basic?.additional_properties?.get(
                    THUMBNAIL_RESIZE_URL_KEY
                ) as? String)
                ?.let { createFullImageUrl(url = it) }
        }
    } ?: ""


fun ArcXPContentElement.imageUrl(): String {
    //whether in portrait or landscape, we don't want an image with resolution larger than the max screen dimension

    this.promoItem?.basic?.let {
        //if we don't have height/width we do not want to resize
        if (it.height != null && it.width != null) {
            //choose whether the maximum dimension is width or height
            val maxImageSize = Math.max(it.height, it.width)
            val maxScreenSize = resizer().getScreenSize()

            //we want to scale preserving aspect ratio on this dimension
            val maxIsHeight = maxImageSize == it.height!!

            ///if image is smaller than device we do not want to resize
            if (maxImageSize >= maxScreenSize) {
                val finalUrl = if (this.type == "video") {
                    it.url?.substringAfter("=/")
                } else {
                    (it.additional_properties?.get(Constants.RESIZE_URL_KEY) as? String)?.substringAfter(
                        "=/"
                    )
                } ?: ""
                if (finalUrl.isNotEmpty()) {
                    return if (maxIsHeight) {
                        resizer()
                            .resizeHeight(url = finalUrl, height = maxScreenSize)
                    } else {
                        resizer().resizeWidth(url = finalUrl, width = maxScreenSize)
                    }
                }

            } else return it.url ?: ""

        }
    }
    return ""
}

package com.arcxp.content.extendedModels

import androidx.annotation.Keep
import com.arcxp.content.ArcXPContentSDK
import com.arcxp.content.models.*
import com.arcxp.content.util.Constants.RESIZE_URL_KEY
import com.arcxp.content.util.Constants.THUMBNAIL_RESIZE_URL_KEY
import com.arcxp.content.util.createFullImageUrl
import com.arcxp.content.util.formatter
import com.squareup.moshi.Json
import java.lang.Math.max
import java.util.*

/**
 * Individual Response service object for Collection Call
 *
 * Collection call receives list with up to 20 of these
 *
 * @property id ANS ID
 * @property headlines headlines
 * @property description description
 * @property credits credits
 * @property promoItem promo Items, including picture
 * @property modified_on last modified data
 * @property publishedDate publish date
 */
@Keep
data class ArcXPCollection(
    @Json(name = "_id") val id: String,
    @Json(name = "headlines") val headlines: Headlines,
    val description: Description?,
    val credits: Credits?,
    val type: String,
    @Json(name = "promo_items") val promoItem: PromoItem?,
    @Json(name = "display_date") val modified_on: Date?,
    @Json(name = "publish_date") val publishedDate: Date?,
    val duration: Long?,
    val subheadlines: Subheadlines?
)


fun ArcXPCollection.date() = this.publishedDate?.let { formatter.format(it) } ?: ""

fun ArcXPCollection.title() = this.headlines.basic ?: ""

fun ArcXPCollection.description() = this.description?.basic ?: ""

fun ArcXPCollection.author(): String {
    if (this.credits?.by?.isNotEmpty() == true) {
        return (this.credits.by[0].name) ?: ""
    }
    return ""
}

fun ArcXPCollection.fallback() =
    this.promoItem?.let { promoItem ->
        if (this.type == "video") {
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

fun ArcXPCollection.thumbnail() =
    if (type == "video") {
        promoItem?.basic?.url?.let { ArcXPContentSDK.resizer().createThumbnail(it.substringAfter("=/")) } ?: ""
    } else {
        fallback()
    }


fun ArcXPCollection.imageUrl(): String {
    //whether in portrait or landscape, we don't want an image with resolution larger than the max screen dimension

    this.promoItem?.basic?.let {
        //if we don't have height/width we do not want to resize
        if (it.height != null && it.width != null) {
            //choose whether the maximum dimension is width or height
            val maxImageSize = max(it.height, it.width)
            val maxScreenSize = ArcXPContentSDK.resizer().getScreenSize()

            //we want to scale preserving aspect ratio on this dimension
            val maxIsHeight = maxImageSize == it.height!!

            ///if image is smaller than device we do not want to resize
            if (maxImageSize >= maxScreenSize) {
                val finalUrl = if (this.type == "video") {
                    it.url?.substringAfter("=/")
                } else {
                    (it.additional_properties?.get(RESIZE_URL_KEY) as? String)?.substringAfter(
                        "=/"
                    )
                } ?: ""
                if (finalUrl.isNotEmpty()) {
                    return if (maxIsHeight) {
                        ArcXPContentSDK.resizer()
                            .resizeHeight(url = finalUrl, height = maxScreenSize)
                    } else {
                        ArcXPContentSDK.resizer().resizeWidth(url = finalUrl, width = maxScreenSize)
                    }
                }

            } else return it.url ?: ""

        }
    }
    return ""
}
package com.arcxp.content.extendedModels

import androidx.annotation.Keep
import com.arcxp.ArcXPMobileSDK
import com.arcxp.ArcXPMobileSDK.imageUtils
import com.arcxp.content.models.*
import com.arcxp.commons.util.Constants.THUMBNAIL_RESIZE_URL_KEY
import com.arcxp.commons.util.Utils
import com.arcxp.commons.util.Utils.createFullImageUrl
import com.arcxp.commons.util.Utils.formatter
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
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
@JsonClass(generateAdapter = true)
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

fun ArcXPCollection.thumbnail() = this.promoItem?.let { promoItem ->
            imageUtils().thumbnail(promoItem)
} ?: ""

fun ArcXPCollection.fallback() = this.promoItem?.let { promoItem ->
        imageUtils().fallback(promoItem)
    } ?: ""

fun ArcXPCollection.imageUrl(): String = this.promoItem?.basic?.let { promoItem->
        imageUtils().imageUrl(promoItem)
    } ?: ""

fun ArcXPCollection.isVideo() = type == Utils.AnsTypes.VIDEO.type

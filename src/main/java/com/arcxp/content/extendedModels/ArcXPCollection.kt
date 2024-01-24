package com.arcxp.content.extendedModels

import com.arcxp.content.models.*
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
//@Keep
//@JsonClass(generateAdapter = true)
//data class ArcXPCollection(
//    @Json(name = "_id") val uuid: String,
//)


//fun ArcXPCollection.date() = this.publishedDate?.let { formatter.format(it) } ?: ""
//
//fun ArcXPCollection.title() = this.headlines.basic ?: ""
//
//fun ArcXPCollection.description() = this.description?.basic ?: ""
//
//fun ArcXPCollection.author(): String {
//    if (this.credits?.by?.isNotEmpty() == true) {
//        return (this.credits.by[0].name) ?: ""
//    }
//    return ""
//}
//
//fun ArcXPCollection.thumbnail() = this.promoItem?.let { promoItem ->
//            imageUtils().thumbnail(promoItem)
//} ?: ""
//
//fun ArcXPCollection.fallback() = this.promoItem?.let { promoItem ->
//        imageUtils().fallback(promoItem)
//    } ?: ""
//
//fun ArcXPCollection.imageUrl(): String = this.promoItem?.basic?.let { promoItem->
//        imageUtils().imageUrl(promoItem)
//    } ?: ""
//
//fun ArcXPCollection.isVideo() = type == Utils.AnsTypes.VIDEO.type

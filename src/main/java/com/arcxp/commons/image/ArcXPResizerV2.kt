package com.arcxp.commons.image

import com.arcxp.commons.util.Constants
import com.arcxp.content.models.Image
import com.arcxp.content.models.PromoItem
import com.arcxp.content.models.substringAfterLastUrlCharacter

internal class ArcXPResizerV2(val baseUrl: String) {

    fun isValid(image: Image): Boolean = getAuthKey(image.auth) != null
    fun isValid(item: PromoItem.PromoItemBasic): Boolean = getAuthKey(item.auth) != null

    fun createThumbnail(url: String) = resizeWidth(url, Constants.THUMBNAIL_SIZE)

    fun getV2Url(image: Image): String? {
        var filePart: String? = image.url?.let { url ->
            val afterLastSlash = url.substringAfterLast("/")
            afterLastSlash?.substringAfterLastUrlCharacter(".")
        } ?: return null

        val authKey = getAuthKey(image.auth)

        return "${baseUrl}/resizer/v2/${image._id}.$filePart?auth=$authKey"
    }

    fun getV2Url(item: PromoItem.PromoItemBasic): String? {
        var filePart: String? = item.url?.let { url ->
            val afterLastSlash = url.substringAfterLast("/")
            afterLastSlash?.substringAfterLastUrlCharacter(".")
        } ?: return null

        val authKey = getAuthKey(item.auth)

        return "${baseUrl}/resizer/v2/${item._id}.$filePart?auth=$authKey"
    }

    fun getAuthKey(auth: Map<String, String>?) =
        auth?.filter {
            it.key.toIntOrNull() != null
        }
        ?.maxByOrNull {
            it.key.toInt()
        }
        ?.value

    fun resizeWidth(image: Image, width: Int): String? {
        val url = getV2Url(image) ?: return null
        return resizeWidth(url, width)
    }

    fun resizeHeight(image: Image, height: Int): String? {
        val url = getV2Url(image) ?: return null
        return resizeHeight(url, height)
    }


    fun resizeWidth(url: String, width: Int) = "${url}&width=$width"

    fun resizeHeight(url: String, height: Int) = "${url}&height=$height"
}
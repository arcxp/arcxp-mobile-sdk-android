package com.arcxp.commons.image

import com.arcxp.commons.util.Constants
import com.arcxp.content.models.Image
import com.arcxp.content.models.PromoItem
import com.arcxp.content.models.substringAfterLastUrlCharacter

/**
 * ArcXPResizerV2 is responsible for handling image resizing operations within the ArcXP Commerce module.
 * It provides methods to validate images and promo items, generate URLs for resized images, and create thumbnails.
 *
 * The class defines the following operations:
 * - Validate images and promo items based on authentication keys
 * - Generate V2 URLs for images and promo items
 * - Resize images by width and height
 * - Create thumbnails for images
 *
 * Usage:
 * - Create an instance of ArcXPResizerV2 with the base URL for the resizer service.
 * - Use the provided methods to validate images, generate URLs, and resize images.
 *
 * Example:
 *
 * val resizer = ArcXPResizerV2(baseUrl = "https://example.com")
 * val thumbnailUrl = resizer.createThumbnail(imageUrl)
 * val resizedUrl = resizer.resizeWidth(image, 200)
 *
 * Note: Ensure that the base URL is properly configured before using ArcXPResizerV2.
 *
 * @method isValid Validate an image or promo item.
 * @method createThumbnail Create a thumbnail for an image.
 * @method getV2Url Generate a V2 URL for an image or promo item.
 * @method resizeWidth Resize an image by width.
 * @method resizeHeight Resize an image by height.
 * @method getAuthKey Retrieve the authentication key for an image or promo item.
 */
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
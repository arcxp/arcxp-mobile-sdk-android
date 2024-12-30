package com.arcxp.commons.util

import android.content.res.Resources
import com.arcxp.content.models.Image
import com.squareup.pollexor.Thumbor

/**
 * ArcXPResizerV1 is responsible for handling image resizing operations using the Thumbor service within the ArcXP Commerce module.
 * It provides methods to validate images, generate URLs for resized images, and create thumbnails.
 *
 * The class defines the following operations:
 * - Validate the resizer configuration
 * - Generate URLs for resized images
 * - Resize images by width and height
 * - Create thumbnails for images
 *
 * Usage:
 * - Create an instance of ArcXPResizerV1 with the base URL for the resizer service and the resizer key.
 * - Use the provided methods to validate images, generate URLs, and resize images.
 *
 * Example:
 *
 * val resizer = ArcXPResizerV1(baseUrl = "https://example.com", resizerKey = "your_resizer_key")
 * val thumbnailUrl = resizer.createThumbnail(imageUrl)
 * val resizedUrl = resizer.resizeWidth(imageUrl, 200)
 *
 * Note: Ensure that the base URL and resizer key are properly configured before using ArcXPResizerV1.
 *
 * @method isValid Validate the resizer configuration.
 * @method imageUrl Generate the appropriate URL for an image.
 * @method createThumbnail Create a thumbnail for an image.
 * @method resizeWidth Resize an image by width.
 * @method resizeHeight Resize an image by height.
 */
internal class ArcXPResizerV1(baseUrl: String, resizerKey: String) {
    private var resizer: Thumbor?
    init {
        resizer = if (resizerKey.isBlank()) {
            null
        } else Thumbor.create("$baseUrl/resizer", resizerKey)
    }

    fun isValid(): Boolean = resizer != null

    fun imageUrl(image: Image): String? {
        return (image.additional_properties?.get(Constants.RESIZE_URL_KEY) as? String)?.substringAfter("=/")
    }

    fun createThumbnail(url: String) = resize(url, Constants.THUMBNAIL_SIZE, 0)
    fun resizeWidth(url: String, width: Int) = resize(url = url, width = width, height = 0)
    fun resizeHeight(url: String, height: Int) = resize(url = url, width = Resources.getSystem().displayMetrics.widthPixels , height = height)

    private fun resize(url: String, width: Int, height: Int): String? {
        return if (resizer != null) {
            resizer!!
                .buildImage(url)
                .resize(width, height)
                .smart()
                .toUrl()
        } else {
            null
        }
    }
}
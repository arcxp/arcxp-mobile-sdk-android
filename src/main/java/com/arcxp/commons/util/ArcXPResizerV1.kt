package com.arcxp.commons.util

import android.content.res.Resources
import com.arcxp.content.models.Image
import com.squareup.pollexor.Thumbor

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
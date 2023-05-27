package com.arcxp.commons.util

import android.content.res.Resources
import com.arcxp.ArcXPMobileSDK.initErrorResizerKey
import com.arcxp.commons.util.DependencyFactory.createArcXPError
import com.squareup.pollexor.Thumbor
import kotlin.math.max

class ArcXPResizer(baseUrl: String, resizerKey: String) {
    private var resizer: Thumbor
    init {
        resizer = if (resizerKey.isBlank()) {
            throw createArcXPError(message = initErrorResizerKey)
        } else Thumbor.create("$baseUrl/resizer", resizerKey)
    }

    private val ourScreenSize = max(
        Resources.getSystem().displayMetrics.widthPixels,
        Resources.getSystem().displayMetrics.heightPixels
    )

    fun getScreenSize() = ourScreenSize

    fun createThumbnail(url: String) = resize(url, Constants.THUMBNAIL_SIZE, 0)
    fun resizeWidth(url: String, width: Int) = resize(url = url, width = width, height = 0)
    fun resizeHeight(url: String, height: Int) = resize(url = url, width = Resources.getSystem().displayMetrics.widthPixels , height = height)

    private fun resize(url: String, width: Int, height: Int): String = resizer
        .buildImage(url)
        .resize(width, height)
        .smart()
        .toUrl()
}
package com.arcxp.content

import android.app.Application
import android.content.res.Resources
import com.arcxp.content.util.Constants
import com.arcxp.sdk.R
import com.squareup.pollexor.Thumbor
import com.squareup.pollexor.ThumborUrlBuilder
import java.lang.Math.max

class ArcXPResizer(application: Application, baseUrl: String) {
    private val resizer: Thumbor = Thumbor.create("$baseUrl/resizer", application.getString(R.string.resizer_key))

    private val ourScreenSize = max(
        Resources.getSystem().displayMetrics.widthPixels,
        Resources.getSystem().displayMetrics.heightPixels
    )

    fun getScreenSize() = ourScreenSize

    fun createThumbnail(url: String) = resize(url, Constants.THUMBNAIL_SIZE, 0)
    fun resizeWidth(url: String, width: Int) = resize(url = url, width = width, height = 0)
    fun resizeHeight(url: String, height: Int) = resize(url = url, width =Resources.getSystem().displayMetrics.widthPixels , height = height)

    private fun resize(url: String, width: Int, height: Int): String = resizer
        .buildImage(url)
        .resize(width, height)
        .smart()
        .toUrl()
}

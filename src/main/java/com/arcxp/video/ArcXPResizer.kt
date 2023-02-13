package com.arcxp.video

import android.app.Application
import android.content.res.Resources
import com.arcxp.sdk.R
import com.arcxp.video.util.Constants
import com.squareup.pollexor.Thumbor

class ArcXPResizer(application: Application, baseUrl: String) {
    private val resizer: Thumbor = Thumbor.create("$baseUrl/resizer", application.getString(R.string.resizer_key))

    private val ourScreenSize = Math.max(
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
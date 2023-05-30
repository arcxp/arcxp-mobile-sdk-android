package com.arcxp.video

import android.content.Context
import com.arcxp.video.util.Utils

object VideoPackageUtils {

    fun createArcVideoManager(mContext: Context) =  ArcVideoManager(mContext, Utils())
    fun createArcMediaPlayerConfigBuilder() =  ArcMediaPlayerConfig.Builder()
}
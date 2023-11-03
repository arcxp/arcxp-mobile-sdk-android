package com.arcxp.video

import android.app.Application
import com.arcxp.ArcXPMobileSDK
import com.arcxp.video.util.Utils

object VideoPackageUtils {

    fun createArcVideoManager(mContext: Application) =  ArcVideoManager(mContext, Utils(mContext))
    fun createArcMediaPlayerConfigBuilder() =  ArcXPVideoConfig.Builder()
}
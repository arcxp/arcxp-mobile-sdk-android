package com.arcxp.video

import android.content.Context
import com.arcxp.ArcXPMobileSDK
import com.arcxp.video.util.Utils

object VideoPackageUtils {

    fun createArcVideoManager(mContext: Context) =  ArcVideoManager(mContext, Utils(), ArcXPMobileSDK.castManager())
    fun createArcMediaPlayerConfigBuilder() =  ArcMediaPlayerConfig.Builder()
}
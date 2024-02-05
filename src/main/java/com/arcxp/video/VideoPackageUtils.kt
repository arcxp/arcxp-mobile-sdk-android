package com.arcxp.video

import android.app.Application
import androidx.media3.common.util.UnstableApi
import com.arcxp.ArcXPMobileSDK
import com.arcxp.video.util.Utils

@UnstableApi
object VideoPackageUtils {

    fun createArcVideoManager(mContext: Application) =  ArcVideoManager(mContext, Utils(mContext))
    fun createArcMediaPlayerConfigBuilder() =  ArcXPVideoConfig.Builder()
}
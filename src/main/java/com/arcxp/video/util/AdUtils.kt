package com.arcxp.video.util

import com.arcxp.video.model.ArcAd

class AdUtils {

    companion object {
        @JvmStatic
        fun createArcAd(mCurrentAd: ArcAd): ArcAd? {
            return mCurrentAd.adId?.let { adId ->
                mCurrentAd.adDuration?.let { adDuration ->
                    mCurrentAd.adTitle?.let { adTitle ->
                        mCurrentAd.clickthroughUrl?.let { clickthroughUrl ->
                            ArcAd(adId, adDuration, adTitle, clickthroughUrl)
                        }
                    }
                }
            }
        }
    }
}
package com.arcxp.video.model

import androidx.annotation.Keep
import com.google.ads.interactivemedia.v3.api.Ad
import org.json.JSONObject

@Keep
class ArcAd(private val ad: Ad? = null) {

    var adId: String? = null
    var adDuration: Double? = null
    var adTitle: String? = null
    var companionAds: List<JSONObject>? = null
    var mediaFiles: MediaFiles? = null
    var clickthroughUrl: String? = null

    init {
        adId = ad?.adId
        adDuration = ad?.duration
        adTitle = ad?.title
    }

    constructor(id:String, duration:Double, title:String, total: Int, count: Int) : this(null) {
        adId = id
        adDuration = duration
        adTitle = title
    }

    constructor(id:String, duration:Double, title:String, url: String) : this(null) {
        adId = id
        adDuration = duration
        adTitle = title
        clickthroughUrl = url
    }

    constructor(id:String, duration:Double, title:String, url: String, totol: Int, count: Int) : this(null) {
        adId = id
        adDuration = duration
        adTitle = title
        clickthroughUrl = url
    }
}

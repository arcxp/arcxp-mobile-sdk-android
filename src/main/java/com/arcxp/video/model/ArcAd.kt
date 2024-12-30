package com.arcxp.video.model

import androidx.annotation.Keep
import com.google.ads.interactivemedia.v3.api.Ad
import org.json.JSONObject

/**
 * ArcAd is a model class that represents an advertisement within the ArcXP platform.
 * It encapsulates details about the ad such as its ID, duration, title, companion ads, media files, and clickthrough URL.
 *
 * The class defines the following properties:
 * - adId: The unique identifier for the ad.
 * - adDuration: The duration of the ad in seconds.
 * - adTitle: The title of the ad.
 * - companionAds: A list of companion ads represented as JSON objects.
 * - mediaFiles: An instance of MediaFiles containing media file information for the ad.
 * - clickthroughUrl: The URL to which the ad redirects when clicked.
 *
 * Usage:
 * - Create an instance of ArcAd to store and manage advertisement details.
 * - Use the provided constructors to initialize the ad with different sets of properties.
 *
 * Example:
 *
 * val arcAd = ArcAd("ad123", 30.0, "Sample Ad", "https://example.com")
 *
 * Note: Ensure that the ad properties are properly set before using them in the application.
 *
 * @property adId The unique identifier for the ad.
 * @property adDuration The duration of the ad in seconds.
 * @property adTitle The title of the ad.
 * @property companionAds A list of companion ads represented as JSON objects.
 * @property mediaFiles An instance of MediaFiles containing media file information for the ad.
 * @property clickthroughUrl The URL to which the ad redirects when clicked.
 */
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

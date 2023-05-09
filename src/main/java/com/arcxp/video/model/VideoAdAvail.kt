package com.arcxp.video.model

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import org.json.JSONObject

@Keep
@JsonClass(generateAdapter = true)
public data class VideoAdData(
        var manifestUrl: String? = null,
        var trackingUrl: String? = null,
        var adAvails : AvailList? = null,
        var error: Error? = null,
        var sessionId: String? = null
)
@Keep
@JsonClass(generateAdapter = true)
public data class Error(
        val message: String? = null
)
@Keep
@JsonClass(generateAdapter = true)
public data class AvailList(
        var avails : List<VideoAdAvail>? = null
)
@Keep
@JsonClass(generateAdapter = true)
public data class VideoAdAvail(
        val ads: List<AdInfo>,
        val availId: String,
        val duration: String,
        val durationInSeconds: Double,
        val startTime: String,
        val startTimeInSeconds: Double
) {
    fun getTotalTime() : Long {
        var time = 0.0
        for (adInfo in ads) {
            time += adInfo.durationInSeconds
        }
        return (time*1000).toLong()
    }
}
@Keep
@JsonClass(generateAdapter = true)
public data class Ads(
        val ads: List<AdInfo>
)
@Keep
@JsonClass(generateAdapter = true)
public data class AdInfo(
        val adId: String,
        val mediaFiles: MediaFiles?,
        val adTitle: String,
        val adVerifications: List<AdVerification>,
        val companionAd: List<JSONObject>?,
        val duration: String,
        val durationInSeconds: Double,
        val startTime: String,
        val startTimeInSeconds: Double,
        val trackingEvents: List<TrackingEvent>,
        val clickthroughUrl: String?
)
@Keep
@JsonClass(generateAdapter = true)
public data class MediaFiles(
        val mezzanine: String,
        val mediaFilesList: List<JSONObject>
)
@Keep
@JsonClass(generateAdapter = true)
public data class AdVerification(
        val javascriptResource: List<JavascriptResource>?,
        val vendor: String?,
        val verificationParameters: String?
)
@Keep
@JsonClass(generateAdapter = true)
public data class JavascriptResource(
        val apiFramework: String?,
        val uri: String?
)
@Keep
@JsonClass(generateAdapter = true)
public data class TrackingEvent(
        val beaconUrls: List<String>,
        val duration: String,
        val durationInSeconds: Double,
        val eventId: String,
        val eventType: String,
        val startTime: String,
        val startTimeInSeconds: Double
)
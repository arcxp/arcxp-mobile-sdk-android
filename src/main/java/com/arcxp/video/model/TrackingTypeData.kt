package com.arcxp.video.model

import androidx.annotation.Keep

@Keep
sealed class TrackingTypeData {
    data class TrackingVideoTypeData(var position: Long? = null,
                                     var percentage: Int? = null,
                                     var arcVideo: ArcVideo? = null,
                                     var sessionId: String? = null): TrackingTypeData()
    data class TrackingAdTypeData(
            var position: Long? = null,
            var arcAd: ArcAd? = null,
            var sessionId: String? = null,
            var data: Any? = null): TrackingTypeData()
    data class TrackingSourceTypeData(var position: Long? = null,
                                      var source: String? = null,
                                      var sessionId: String? = null): TrackingTypeData()
    data class TrackingErrorTypeData(var arcVideo: ArcVideo? = null,
                                     var sessionId: String? = null,
                                     var adData: VideoAdAvail? = null): TrackingTypeData()
}

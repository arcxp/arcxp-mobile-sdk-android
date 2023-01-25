package com.arc.arcvideo.model

/**
 * @suppress
 */
data class TrackingDataModel(val availId: String,
                            val timestamp: Long,
                            val adInfo: AdInfo,
                             var isLast: Boolean,
                             var handled: Boolean = false,
                             val count: Int,
                             val total: Int,
                            val trackingEvent: TrackingEvent) : Comparable<TrackingDataModel> {

    override fun compareTo(other: TrackingDataModel): Int {
        return (timestamp - other.timestamp) as Int
    }

}
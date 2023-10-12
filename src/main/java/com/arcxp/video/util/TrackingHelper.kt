package com.arcxp.video.util

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import com.arcxp.commons.util.Utils
import com.arcxp.video.ArcVideoManager
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.AdInfo
import com.arcxp.video.model.AdVerification
import com.arcxp.video.model.ArcAd
import com.arcxp.video.model.ArcVideo
import com.arcxp.video.model.AvailList
import com.arcxp.video.model.TrackingDataModel
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData
import com.arcxp.video.model.TrackingTypeData.TrackingAdTypeData
import com.arcxp.video.model.VideoAdAvail
import com.arcxp.video.service.AdUtils.Companion.callBeaconUrl
import com.arcxp.video.views.VideoFrameLayout

/**
 * @suppress
 */
public class TrackingHelper(
    private val videoId: String,
    private val videoManager: ArcVideoManager,
    private val config: ArcXPVideoConfig,
    private val mContext: Context,
    private val mLayout: VideoFrameLayout,
    private val mListener: VideoListener//TODO should we have videoListener and manager as dependency??!
) {

    private var mCurrentAd: ArcAd? = null

    private val eventList: ArrayList<TrackingDataModel> = ArrayList()
    private val eventsToRemove: ArrayList<TrackingDataModel> = ArrayList()

    private var currentAvail: String? = null
    private val handledAvails = HashMap<String, Int>()

    private var currentPosition: Long? = null

    private var lastTouchTime: Long = 0

    private var omidHelper: OmidHelper? = null
    private var palHelper: PalHelper? = null

    public fun initAdTracking(verifications: List<AdVerification>) {
        omidHelper?.init(verifications)
    }

    private fun clearAdTracking() {
        omidHelper?.clear()
    }

    public fun checkTracking(position: Long) {
        if (config.isLoggingEnabled) {
            //Log.d("ArcVideoSDK", "current timeline position = ${videoManager.currentTimelinePosition} position=$position eventsize=${eventList.size}")
        }
        try {
            currentPosition = position
            for (event in eventList) {
                if (!event.handled) {
                    if (event.timestamp <= position) { // && event.timestamp > position - event.adInfo.durationInSeconds) {
                        event.handled = true
                        handleMessage(event, position)
                        eventsToRemove.add(event)
                    }
                }
            }
            for (event in eventsToRemove) {
                eventList.remove(event)
            }
            eventsToRemove.clear()
        } catch (e: Exception) {

        }
    }

    private fun addEvent(event: TrackingDataModel) {
        eventList.add(event)
    }

    fun initVideo(descriptionUrl: String) {
        if (config.isEnableOmid) {
            omidHelper = OmidHelper(
                context = mContext,
                config = config,
                layout = mLayout,
                videoPlayer = videoManager.videoPlayer
            )
        }
        if (config.isEnablePAL) {
            palHelper = PalHelper(
                context = mContext,
                config = config,
                layout = mLayout,
                mListener = mListener
            )
        }
        palHelper?.initVideo(descriptionUrl)
    }

    fun onTouch(event: MotionEvent, position: Long) {
        if (mCurrentAd != null && !videoManager.isLive) {
            val arcAd = mCurrentAd?.adId?.let {
                mCurrentAd?.adDuration?.let { it1 ->
                    mCurrentAd?.adTitle?.let { it2 ->
                        mCurrentAd?.clickthroughUrl?.let { it3 ->
                            ArcAd(it, it1, it2, it3)
                        }
                    }
                }
            }

            val adData = TrackingAdTypeData(currentPosition, arcAd)
            if (config.isLoggingEnabled) Log.d(
                "ArcVideoSDK",
                "Ad onTouch() id=${mCurrentAd?.adId} title=${mCurrentAd?.adTitle}"
            )
            onVideoEvent(TrackingType.AD_CLICKTHROUGH, adData, position)
            omidHelper?.mediaEventsOnTouch()
            palHelper?.sendAdImpression()
            omidHelper?.adEventsImpressionOccurred()
            onVideoEvent(TrackingType.AD_CLICKED, adData, position)
        } else {
            val arcVideo = ArcVideo.Builder().setUuid(videoId).build()
            val data = TrackingTypeData.TrackingVideoTypeData(position, 0, arcVideo)
            onVideoEvent(TrackingType.ON_PLAYER_TOUCHED, data, position)
        }
        palHelper?.onTouch(event, mCurrentAd)
        lastTouchTime = Utils.currentTimeInMillis()
    }

    private var lastHandledEvent: TrackingType? = null

    private fun onVideoEvent(trackingType: TrackingType, value: TrackingTypeData?, position: Long) {
        if (lastHandledEvent == trackingType && (Utils.currentTimeInMillis() - lastTouchTime) < 500) {
            if (config.isLoggingEnabled) Log.e(
                "ArcVideoSDK",
                "Skipping event ${trackingType} at time $position, last event time was $lastTouchTime"
            )
        } else {
            mListener.onTrackingEvent(trackingType, value)
            lastHandledEvent = trackingType
        }
    }

    fun addEvents(avails: AvailList, position: Long) {
        try {
            if (avails.avails != null) {
                for (avail in avails.avails!!) {
                    //skip processing if the ad count has not changed
                    val availAdCount = handledAvails.get(avail.availId)
                    if (availAdCount == null) {
                        if (config.isLoggingEnabled) Log.d(
                            "ArcVideoSDK",
                            "Processing received avail id=${avail.availId} total ads=${avail.ads.size}"
                        )
                        createEvents(avail, position)
                    } else if (avail.ads.size != availAdCount) {
                        if (config.isLoggingEnabled) Log.d(
                            "ArcVideoSDK",
                            "Reprocessing received avail id=${avail.availId} total ads=${avail.ads.size}"
                        )
                        adjustEvents(avail, position)
                    }
                    if (config.isLoggingEnabled) Log.d(
                        "ArcVideoSDK",
                        "Received avail id=${avail.availId}.  Ad count did not change."
                    )
                    handledAvails[avail.availId] = avail.ads.size
                }
            }
        } catch (e: Exception) {

        }
    }

    private fun adjustEvents(avail: VideoAdAvail, position: Long) {
        removeEvents(avail.availId)
        createEvents(avail, position)
    }

    private var lastTrackingItem: TrackingDataModel? = null

    private fun createEvents(avail: VideoAdAvail, position: Long) {
        if (config.isLoggingEnabled) Log.d(
            "ArcVideoSDK",
            "Creating ad events for avail ${avail.availId}"
        )
        var adCount = -1
        for (adinfo in avail.ads) {
            adCount += 1
            if (config.isLoggingEnabled) Log.d(
                "ArcVideoSDK",
                "Creating events for ad ${adinfo.adId} at ${avail.startTime}"
            )
            if (!validateTrackingEvent(adinfo)) {
                val adData =
                    TrackingTypeData.TrackingErrorTypeData(null, videoManager.sessionId, avail)
                onVideoEvent(TrackingType.MALFORMED_AD_AVAIL, adData, position)
                if (config.isLoggingEnabled) Log.d(
                    "ArcVideoSDK",
                    "Skipping creating ad events for ad ${adinfo.adId}.  Not enough events."
                )
                continue
            }
            for (trackingEvent in adinfo.trackingEvents) {
                if ((trackingEvent.startTimeInSeconds * 1000).toLong() < position) {
                    continue
                }
                for (event in eventList) {
                    if (event.trackingEvent.eventType == trackingEvent.eventType && event.adInfo.adId == adinfo.adId) {
                        continue
                    }
                }
                when (trackingEvent.eventType) {
                    "start", "firstQuartile", "midpoint", "thirdQuartile", "complete", "clickThrough", "impression" -> {
                        var lastItem = false;
                        val trackingDataModel = TrackingDataModel(
                            avail.availId,
                            (trackingEvent.startTimeInSeconds * 1000).toLong(),
                            adinfo,
                            lastItem,
                            false,
                            adCount,
                            avail.ads.size,
                            trackingEvent
                        )
                        eventList.add(trackingDataModel)
                        if (lastTrackingItem == null ||
                            (lastTrackingItem != null && lastTrackingItem?.timestamp?.compareTo(
                                trackingDataModel.timestamp
                            )!! > 0)
                        ) {
                            lastTrackingItem = trackingDataModel
                        }

                        if (config.isLoggingEnabled) Log.d(
                            "ArcVideoSDK",
                            "Created event ${trackingEvent.eventType} id=${trackingEvent.eventId} at time ${(trackingEvent.startTimeInSeconds * 1000)} current time = ${videoManager.currentTimelinePosition}"
                        )
                    }
                }
            }
        }
        if (eventList.isNotEmpty()) {
            eventList.sortBy { it.timestamp }
            eventList.last().isLast = true
        }
        if (config.isLoggingEnabled) Log.d(
            "ArcVideoSDK",
            "Last event for avail is id=${eventList.last().trackingEvent.eventId} type=${eventList.last().trackingEvent.eventType} at ${eventList.last().timestamp}"
        )
    }

    private fun validateTrackingEvent(adInfo: AdInfo): Boolean {
        var count = 0
        for (event in adInfo.trackingEvents) {
            when (event.eventType) {
                "start", "firstQuartile", "midpoint", "thirdQuartile", "complete", "clickThrough" -> {
                    count++
                }
            }
        }
        if (count < 6) {
            if (config.isLoggingEnabled) Log.d(
                "ArcVideoSDK",
                "Only $count events in ad ${adInfo.adId}"
            )
            return false
        }
        return true

    }

    fun removeEvents() {
        if (currentAvail != null) {
            removeEvents(currentAvail!!)
        }
    }

    private fun removeEvents(avail: String) {
        for (event in eventList) {
            if (event.availId == avail || event.handled) {
                eventsToRemove.add(event)
            }
        }

        for (event in eventsToRemove) {
            eventList.remove(event)
        }
        eventsToRemove.clear()
    }

    private fun handleMessage(
        event: TrackingDataModel,
        position: Long
    ) { //adInfo: AdInfo, event: TrackingEvent, isLast: Boolean) {
        currentAvail = event.availId
        if (config.isLoggingEnabled) Log.d(
            "ArcVideoSDK",
            "Processing event ${event.trackingEvent.eventType} id=${event.adInfo.adId} at time=${event.timestamp}"
        )

        var type: TrackingType? = null
        when (event.trackingEvent.eventType) {
            "start" -> {
                type = TrackingType.MIDROLL_AD_STARTED
                mCurrentAd = ArcAd(
                    event.adInfo.adId,
                    event.adInfo.durationInSeconds,
                    event.adInfo.adTitle,
                    event.total,
                    event.count
                )
                if (event.adInfo.companionAd != null && !event.adInfo.companionAd.isEmpty()) {
                    mCurrentAd?.companionAds = event.adInfo.companionAd
                }
                if (event.adInfo.mediaFiles != null) {
                    mCurrentAd?.mediaFiles = event.adInfo.mediaFiles
                }
                //palHelper?.sendAdImpression()
                if (videoManager.isClosedCaptionTurnedOn) {
                    videoManager.enableClosedCaption(false)
                    videoManager.enableClosedCaption(true)
                }
                initAdTracking(event.adInfo.adVerifications)
                if (config.isLoggingEnabled) Log.e("ArcVideoSDK", "Media Event Start")
                omidHelper?.mediaEventsStart(
                    event.adInfo.durationInSeconds.toFloat() * 1000.0f,
                    1.0F
                )

            }

            "firstQuartile" -> {
                type = TrackingType.MIDROLL_AD_25
                if (config.isLoggingEnabled) Log.e("ArcVideoSDK", "Media Event First Quartile")
                omidHelper?.mediaEventsFirstQuartile()
            }

            "midpoint" -> {
                type = TrackingType.MIDROLL_AD_50
                if (config.isLoggingEnabled) Log.e("ArcVideoSDK", "Media Event Midpoint")
                omidHelper?.mediaEventsMidpoint()
            }

            "thirdQuartile" -> {
                type = TrackingType.MIDROLL_AD_75
                if (config.isLoggingEnabled) Log.e("ArcVideoSDK", "Media Event Third Quartile")
                omidHelper?.mediaEventsThirdQuartile()
            }

            "complete" -> {
                type = TrackingType.MIDROLL_AD_COMPLETED
                if (config.isLoggingEnabled) Log.e("ArcVideoSDK", "Media Event Complete")
                omidHelper?.mediaEventsComplete()
            }
//            "impression" -> {
//                type = TrackingType.AD_IMPRESSION
//                if (config.isLoggingEnabled) Log.e("ArcVideoSDK", "Media Event Impression")
//                omidHelper?.adEventsImpressionOccurred()
//            }
            "clickThrough" -> if (mCurrentAd != null) {
                type = TrackingType.AD_CLICKTHROUGH
                if (config.isLoggingEnabled) Log.e("ArcVideoSDK", "Media Event Clickthrough")
                mCurrentAd?.clickthroughUrl = event.trackingEvent.beaconUrls[0]
            }
        }
        if (type != null) {
            if (mCurrentAd == null) {
                mCurrentAd = ArcAd(
                    event.adInfo.adId,
                    event.adInfo.durationInSeconds,
                    event.adInfo.adTitle,
                    event.total,
                    event.count
                )
            }
            val adData = TrackingAdTypeData(position, mCurrentAd)
            adData.arcAd?.clickthroughUrl = event.trackingEvent.beaconUrls[0]

            if (config.isLoggingEnabled) Log.e("ArcVideoSDK", "Sending Media Event $type")

            onVideoEvent(type, adData, position)
            if (type == TrackingType.MIDROLL_AD_STARTED) {
                if (mCurrentAd?.companionAds != null) {
                    onVideoEvent(TrackingType.AD_COMPANION_INFO, adData, position)
                }
                if (mCurrentAd?.mediaFiles != null) {
                    onVideoEvent(TrackingType.AD_MEDIA_FILES, adData, position)
                }
            }
            for (url in event.trackingEvent.beaconUrls) {
                callBeaconUrl(url)
            }
            if (event.isLast) {
                mCurrentAd = null
                if (config.isLoggingEnabled) Log.d("ArcVideoSDK", "All midroll ads processed")
                onVideoEvent(TrackingType.ALL_MIDROLL_AD_COMPLETE, adData, position)
                clearAdTracking()
            }
        }
    }

    public fun pausePlay() {
        omidHelper?.mediaEventsPause()
    }

    public fun resumePlay() {
        omidHelper?.mediaEventsResume()
    }

    public fun fullscreen() {
        omidHelper?.mediaEventsFullscreen()
    }

    public fun normalScreen() {
        omidHelper?.mediaEventsNormalScreen()
    }

    public fun volumeChange(volume: Float) {
        omidHelper?.mediaEventsVolumeChange(volume)
    }

    public fun onPlaybackStart() {
        palHelper?.sendPlaybackStart()
    }

    public fun onPlaybackEnd() {
        palHelper?.sendPlaybackEnd()
    }

    public fun onDestroy() {
        omidHelper?.onDestroy()
    }

    @VisibleForTesting
    fun getEventList() = eventList

    @VisibleForTesting
    fun getOMidHelper() = omidHelper

    @VisibleForTesting
    fun getPalHelper() = palHelper

    @VisibleForTesting
    fun getMCurrentAd() = mCurrentAd

    @VisibleForTesting
    fun getLastTouchTime() = lastTouchTime

    companion object {
        @JvmStatic
        public fun runOmidTest(activity: Activity, omOutput: TextView) {
            try {
                val config = ArcXPVideoConfig.Builder().enableOpenMeasurement(true).build()
                val helper = OmidHelper(
                    activity.applicationContext,
                    config,
                    VideoFrameLayout(activity.applicationContext),
                    null
                )
                helper.runOmidTest(activity, omOutput)
            } catch (e: Exception) {
                Log.e("ArcVideoSDK", "Exception: ${e.message}")
            }
        }
    }
}
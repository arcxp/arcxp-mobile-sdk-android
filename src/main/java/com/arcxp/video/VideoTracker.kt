package com.arcxp.video

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData
import com.arcxp.video.util.TrackingHelper
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.cast.CastPlayer
import androidx.media3.common.util.UnstableApi
import rx.Observable
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

/**
 *  This class is used to track and report video progress.
 *
 *  ### How does it relate to other important Video SDK classes?
 *  This class is used by the video player class to track and report progress events for the
 *  currently playing video.  It also has methods that return progress values.
 *
 * @suppress
 */
@UnstableApi
class VideoTracker private constructor(private var listener: VideoListener?, private val trackingHelper: TrackingHelper, private val isLive: Boolean, val mContext: Activity) {

    /**
     * The video player to track
     */
    private var exoPlayer: WeakReference<Player>? = null

    /**
     * This keeps track of the highest percentage value that has been reported at any given point.
     * The purpose of keeping tracking of this is to ensure that a percentage value is not double
     * reported or a value that is less than the current value is not reported.
     */
    private var highestPercent: Double = 0.0

    /**
     * Instantiate the period object once at the creation of the video tracker object.  If this
     * object is instantiated within the method that uses it the determination of the timeline
     * will not work.
     */
    private val period = Timeline.Period()

    private var streamStarted = false

    /**
     * Observable that fires every second to check on progress.
     */
    private val observable: Observable<Unit> = Observable.fromCallable {
        mContext.runOnUiThread {
            trackProgress()
        }
    }
            .repeatWhen { o ->
                o.concatMap {
                    Observable.timer(REPEAT_DELAY_IN_SECONDS, TimeUnit.SECONDS)
                }
            }
            .doOnUnsubscribe {
                mContext.runOnUiThread {
                    trackProgress()
                }
            }

    /**
     * Called by the observable.  This method checks the progress of the video and fires off events
     * for start, 25%, 50%, and 75%
     */
    private fun trackProgress() {
        try {
            val position = getTimelinePosition()
            trackingHelper.checkTracking(position)

            if (((position/1000L) % 5) == 0L) {
                val isPlayingAd = exoPlayer?.get()?.isPlayingAd
                val duration = exoPlayer?.get()?.duration?.toDouble()
                val currentPosition = getCurrentPos()
                if (isPlayingAd == false && duration != null && currentPosition != null) {
                    var currentPercent = (currentPosition / duration) * 100
                    if (highestPercent == 0.0 && (isLive || currentPercent >= 0.0)  && !streamStarted) {
                        onVideoEvent(TrackingType.ON_PLAY_STARTED, TrackingTypeData.TrackingVideoTypeData(percentage = 0, position = position))
                        streamStarted = true
                        if (currentPercent == 0.0) currentPercent = 1.0
                    }
                    if (currentPercent >= 25 && highestPercent < 25) {
                        onVideoEvent(TrackingType.VIDEO_PERCENTAGE_WATCHED, TrackingTypeData.TrackingVideoTypeData(percentage = 25, position = position))
                    }
                    if (currentPercent >= 50 && highestPercent < 50) {
                        onVideoEvent(TrackingType.VIDEO_PERCENTAGE_WATCHED, TrackingTypeData.TrackingVideoTypeData(percentage = 50, position = position))
                    }
                    if (currentPercent >= 75 && highestPercent < 75) {
                        onVideoEvent(TrackingType.VIDEO_PERCENTAGE_WATCHED, TrackingTypeData.TrackingVideoTypeData(percentage = 75, position = position))
                    }
                    highestPercent = when {
                        currentPercent > highestPercent -> currentPercent
                        else -> highestPercent
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ArcVideoSDK", "Exception: ${e.message}", e)
        }
    }

    /**
     * Return the current position of the video playback
     */
    private fun getCurrentPos(): Double {
        var currentPos = 0.0
        (exoPlayer?.get() as? ExoPlayer)?.let {
            currentPos = it.currentPosition.toDouble()
        }
        (exoPlayer?.get() as? CastPlayer)?.let {
            Handler(Looper.getMainLooper()).post {
               currentPos = it.currentPosition.toDouble()
            }
        }
        return currentPos
    }

    /**
     * Report video events.  Do not report percentage events for live video streams as
     * it does not make any sense and they get reported at random times.
     */
    @Synchronized
    private fun onVideoEvent(trackingType: TrackingType, value: TrackingTypeData? = null) {
        if (trackingType == TrackingType.VIDEO_PERCENTAGE_WATCHED && isLive) {
            return
        }
        listener?.onTrackingEvent(trackingType, value)
    }

    /**
     * Return the observable
     */
    fun getObs(): Observable<Unit> {
        return observable
    }

    /**
     * Set the player
     */
    private fun setExoPlayer(exoPlayer: Player) {
        this.exoPlayer = WeakReference(exoPlayer)
        highestPercent = 0.0
    }

    /**
     * Set the listener
     */
    fun setListener(listener: VideoListener?) {
        this.listener = listener;
    }

    /**
     * Calculate and return the timeline position.  This takes into account the position within
     * the video window in order to accurately report positions for live videos.
     */
    private fun getTimelinePosition() : Long {
        var position: Long = 0
        try {
            (exoPlayer?.get() as? ExoPlayer)?.let { it ->
                    try {
                        position = it.currentPosition - it.currentTimeline.getPeriod(it.currentPeriodIndex, period).positionInWindowMs
                    } catch (e: ArrayIndexOutOfBoundsException) {
                        Log.e("ArcVideoSDK", "Exception", e)
                    }
            }
            (exoPlayer?.get() as? CastPlayer)?.let { it ->
                    try {
                        position = it.currentPosition - it.currentTimeline.getPeriod(it.currentPeriodIndex, period).positionInWindowMs
                    } catch (e: ArrayIndexOutOfBoundsException) {
                        Log.e("ArcVideoSDK", "Exception", e)
                    }
            }
        } catch (e: Exception) {
            Log.e("ArcVideoSDK", "Exception", e)
        }
        return position
    }

    /**
     * Reset everything before starting a new video.
     */
    public fun reset() {
        highestPercent = 0.0
    }

    companion object {
        private const val REPEAT_DELAY_IN_SECONDS = 1L

        /**
         * Returns a singleton instance of the VideoTracker object.
         * @param listener [VideoListener] object to report callbacks
         * @param exoPlayer ExoPlayer object
         * @param trackingHelper [TrackingHelper] object to handle events
         * @param isLive Boolean indicating a live video
         */
        @JvmStatic
        fun getInstance(listener: VideoListener?, exoPlayer: Player, trackingHelper: TrackingHelper, isLive: Boolean, context: Activity): VideoTracker =
            VideoTracker(listener, trackingHelper, isLive, context).also {
                it.setExoPlayer(exoPlayer)
                it.setListener(listener)
            }
    }
}
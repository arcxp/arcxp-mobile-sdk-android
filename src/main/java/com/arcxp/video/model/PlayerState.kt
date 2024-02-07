package com.arcxp.video.model

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.VideoTracker
import com.arcxp.video.listeners.ArcKeyListener
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.players.DefaultTrackFilter
import com.arcxp.video.util.Utils
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.cast.CastPlayer
import androidx.media3.exoplayer.ima.ImaAdsLoader
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import androidx.media3.datasource.DataSource
import rx.Subscription


internal class PlayerState(
    mAppContext: Activity,
    val mListener: VideoListener,
    val utils: Utils,
    val config: ArcXPVideoConfig,
) {
    var mLocalPlayer: ExoPlayer? = null
    var mLocalPlayerView: PlayerView? = null
    var mCastPlayer: CastPlayer? = null
    var mCastControlView: PlayerControlView? = null
    var mTrackSelector: DefaultTrackSelector? = utils.createDefaultTrackSelector()
    var mVideoId: String? = null
    var mIsFullScreen = false
    var mFullScreenDialog: Dialog? = null
    var mFullscreenOverlays: HashMap<String, View> = config.overlays
    var mVideo: ArcVideo? = null
    var mVideos: MutableList<ArcVideo>? = ArrayList()

    var title: TextView? = null

    var mShareUrl: String? = null
    var mHeadline: String? = null
    var mCurrentVolume = 0f
    var mVideoTracker: VideoTracker? = null
    var mAdsLoader: ImaAdsLoader? = null
    var mIsLive = false
    var ccButton: ImageButton? = null
    var videoTrackingSub: Subscription? = null
    var defaultTrackFilter = DefaultTrackFilter()
    var period = Timeline.Period()

    /* temporarily disabled controls during an ad */
    var disabledControlsForAd = false
    var castSubtitlesOn = false
    var castMuteOn = false
    var castFullScreenOn = false
    var currentVideoIndex = 0

    var mArcKeyListener: ArcKeyListener? = null

    var wasInFullScreenBeforePip = false

    var firstAdCompleted = false


    var adPlaying = false
    var adPaused = false
    val mMediaDataSourceFactory: DataSource.Factory =
        utils.createDefaultDataSourceFactory(mAppContext, config.userAgent)

    fun incrementVideoIndex(positive: Boolean): Int {
        if (positive) ++currentVideoIndex else --currentVideoIndex
        return currentVideoIndex
    }

    var currentPlayer: Player? = null
    var currentPlayView: View? = null
}
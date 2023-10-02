package com.arcxp.video.players

import android.app.Activity
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.util.Utils
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DataSource

class PlayerState(
    val mAppContext: Activity,
    val mListener: VideoListener,
    val utils: Utils,
    val config: ArcXPVideoConfig
) {

    var mLocalPlayer: ExoPlayer? = null
    var mLocalPlayerView: StyledPlayerView? = null
    var mCastPlayer: CastPlayer? = null
    var mCastControlView: PlayerControlView? = null
    var mTrackSelector: DefaultTrackSelector? = utils.createDefaultTrackSelector()

    
    
    
    val mMediaDataSourceFactory: DataSource.Factory =
        utils.createDefaultDataSourceFactory(mAppContext, config.userAgent)
}
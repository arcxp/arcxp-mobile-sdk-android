package com.arcxp.video.players

import android.app.Activity
import android.content.DialogInterface
import android.text.TextUtils
import android.util.Log
import androidx.core.content.ContextCompat
import com.arcxp.sdk.R
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.ArcVideoSDKErrorType
import com.arcxp.video.model.PlayerState
import com.arcxp.video.util.PrefManager
import com.arcxp.video.util.Utils
import com.arcxp.video.views.ArcTrackSelectionView
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.util.Util
import java.util.Objects

internal class CaptionsManager(
    private val playerState: PlayerState,
    private val utils: Utils,
    private val mConfig: ArcXPVideoConfig,
    private val mListener: VideoListener
) {

    private fun getTextRendererIndex(mappedTrackInfo: MappingTrackSelector.MappedTrackInfo): Int {
        try {
            val count = mappedTrackInfo.rendererCount
            for (i in 0 until count) {
                if (mappedTrackInfo.getRendererType(i) == C.TRACK_TYPE_TEXT) {
                    return i
                }
            }
        } catch (e: Exception) {
        }
        return -1
    }

    fun initVideoCaptions() {
        try {
            val captionsEnabled = PrefManager.getBoolean(
                Objects.requireNonNull(mConfig.activity),
                PrefManager.IS_CAPTIONS_ENABLED,
                false
            )
            if (playerState.mTrackSelector != null) {
                val mappedTrackInfo = playerState.mTrackSelector!!.currentMappedTrackInfo
                if (mappedTrackInfo != null) {
                    val textRendererIndex: Int =
                        getTextRendererIndex(mappedTrackInfo)
                    if (textRendererIndex != -1) {
                        val parametersBuilder = playerState.mTrackSelector!!.buildUponParameters()
                        if (captionsEnabled) {
                            val trackGroups = mappedTrackInfo.getTrackGroups(textRendererIndex)
                            val override = utils.createSelectionOverride(trackGroups.length - 1, 0)
                            parametersBuilder.setSelectionOverride(
                                textRendererIndex,
                                trackGroups,
                                override
                            )
                            parametersBuilder.setRendererDisabled(textRendererIndex, false)
                        } else {
                            parametersBuilder.clearSelectionOverrides(textRendererIndex)
                            parametersBuilder.setRendererDisabled(textRendererIndex, true)
                        }
                        playerState.mTrackSelector!!.setParameters(parametersBuilder)
                        if (!mConfig.isShowClosedCaptionTrackSelection) {
                            if (captionsEnabled) {
                                playerState.ccButton?.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        Objects.requireNonNull<Activity?>(mConfig.activity),
                                        R.drawable.CcDrawableButton
                                    )
                                )
                            } else {
                                playerState.ccButton?.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        Objects.requireNonNull<Activity?>(mConfig.activity),
                                        R.drawable.CcOffDrawableButton
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
        }
    }

    fun createMediaSourceWithCaptions(): MediaSource? {
        try {
            playerState.mVideo?.id.let { createMediaSource(utils.createMediaItem(it)) }
                ?.let { videoMediaSource ->

                    if (!TextUtils.isEmpty(playerState.mVideo!!.subtitleUrl)) {
                        val config = utils.createSubtitleConfig(
                            playerState.mVideo!!.id,
                            playerState.mVideo!!.subtitleUrl
                        )
                        val singleSampleSource =
                            utils.createSingleSampleMediaSourceFactory(playerState.mMediaDataSourceFactory)
                                .setTag(playerState.mVideo!!.id)
                                .createMediaSource(config, C.TIME_UNSET)
                        return utils.createMergingMediaSource(videoMediaSource, singleSampleSource)
                    }
                    return videoMediaSource
                }
        } catch (e: Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
        }
        return null
    }

    private fun createMediaSource(mediaItem: MediaItem): MediaSource? {
        return if (mediaItem.localConfiguration != null) {
            val mediaUri = mediaItem.localConfiguration!!.uri
            val type: @C.ContentType Int = Util.inferContentType(mediaUri)
            when (type) {
                C.CONTENT_TYPE_HLS -> HlsMediaSource.Factory(playerState.mMediaDataSourceFactory)
                    .createMediaSource(mediaItem)

                C.CONTENT_TYPE_SS -> SsMediaSource.Factory(playerState.mMediaDataSourceFactory)
                    .createMediaSource(mediaItem)

                C.CONTENT_TYPE_DASH -> DashMediaSource.Factory(playerState.mMediaDataSourceFactory)
                    .createMediaSource(mediaItem)

                C.CONTENT_TYPE_OTHER -> ProgressiveMediaSource.Factory(playerState.mMediaDataSourceFactory)
                    .createMediaSource(mediaItem)

                else -> null
            }
        } else {
            null
        }
    }

    fun showCaptionsSelectionDialog() {
        try {
            if (playerState.mTrackSelector != null) {
                val mappedTrackInfo = playerState.mTrackSelector!!.currentMappedTrackInfo
                if (mappedTrackInfo != null) {
                    val textRendererIndex: Int = getTextRendererIndex(mappedTrackInfo)
                    if (textRendererIndex != -1) {
                        val dialogPair = ArcTrackSelectionView.getDialog(
                            mConfig.activity,
                            mConfig.activity!!.getString(R.string.captions_dialog_title),//TODO clean up
                            playerState.mTrackSelector,
                            textRendererIndex,
                            playerState.defaultTrackFilter
                        )
                        dialogPair.second.setShowDisableOption(true)
                        dialogPair.second.setAllowAdaptiveSelections(false)
                        dialogPair.second.setShowDefault(false)
                        dialogPair.first.show()
                        dialogPair.first.setOnDismissListener {
                            // save the chosen option to preferences
                            val parameters =
                                playerState.mTrackSelector!!.parameters
                            val isDisabled =
                                parameters.getRendererDisabled(textRendererIndex)
                            setVideoCaptionsEnabled(!isDisabled)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
        }
    }

    private fun setVideoCaptionsEnabled(value: Boolean) {
        PrefManager.saveBoolean(
            Objects.requireNonNull<Activity>(mConfig.activity),
            PrefManager.IS_CAPTIONS_ENABLED,
            value
        )
        if (playerState.ccButton != null) {
            if (isVideoCaptionsEnabled()) {
                playerState.ccButton!!.setImageDrawable(
                    ContextCompat.getDrawable(
                        Objects.requireNonNull<Activity>(
                            mConfig.activity
                        ), R.drawable.CcDrawableButton
                    )
                )
            } else {
                playerState.ccButton!!.setImageDrawable(
                    ContextCompat.getDrawable(
                        Objects.requireNonNull<Activity>(
                            mConfig.activity
                        ), R.drawable.CcOffDrawableButton
                    )
                )
            }
        }
    }

    private fun isVideoCaptionsEnabled(): Boolean {
        try {
            val textRendererIndex =
                getTextRendererIndex(playerState.mTrackSelector!!.currentMappedTrackInfo!!)
            if (textRendererIndex != -1) {
                return !playerState.mTrackSelector!!.parameters.getRendererDisabled(
                    textRendererIndex
                )
            }
    } catch (e: Exception) {

    }
    return false
}

fun toggleClosedCaption() {
    if (playerState.mTrackSelector != null) {
        val mappedTrackInfo = playerState.mTrackSelector!!.currentMappedTrackInfo
        if (mappedTrackInfo != null) {
            val textRendererIndex = getTextRendererIndex(mappedTrackInfo)
            if (textRendererIndex != -1) {
                val parameters = playerState.mTrackSelector!!.parameters
                val show = !parameters.getRendererDisabled(textRendererIndex)
                val parametersBuilder = playerState.mTrackSelector!!.buildUponParameters()
                if (!show) {
                    if (playerState.config.isLoggingEnabled) {
                        Log.d("ArcVideoSDK", "Toggling CC on")
                    }
                    val trackGroups = mappedTrackInfo.getTrackGroups(textRendererIndex)
                    val override =
                        DefaultTrackSelector.SelectionOverride(trackGroups.length - 1, 0)
                    parametersBuilder.setSelectionOverride(
                        textRendererIndex,
                        trackGroups,
                        override
                    )
                    parametersBuilder.setRendererDisabled(textRendererIndex, false)
                } else {
                    if (playerState.config.isLoggingEnabled) {
                        Log.d("ArcVideoSDK", "Toggling CC off")
                    }
                    parametersBuilder.clearSelectionOverrides(textRendererIndex)
                    parametersBuilder.setRendererDisabled(textRendererIndex, true)
                }
                playerState.mTrackSelector!!.setParameters(parametersBuilder)
                setVideoCaptionsEnabled(!show)
                PrefManager.saveBoolean(
                    Objects.requireNonNull<Activity>(playerState.config.activity),
                    PrefManager.IS_CAPTIONS_ENABLED,
                    !show
                )
            }
        }
    }
}

private fun toggleClosedCaption(show: Boolean): Boolean {
    if (playerState.config.isLoggingEnabled) {
        val showString = if (show) "on" else "off"
        Log.d("ArcVideoSDK", "Call to toggle CC $showString")
    }
    try {
        if (playerState.mTrackSelector != null) {
            val mappedTrackInfo = playerState.mTrackSelector!!.currentMappedTrackInfo
            if (mappedTrackInfo != null) {
                val textRendererIndex = getTextRendererIndex(mappedTrackInfo)
                if (textRendererIndex != -1) {
                    val parametersBuilder = playerState.mTrackSelector!!.buildUponParameters()
                    if (show) {
                        val trackGroups = mappedTrackInfo.getTrackGroups(textRendererIndex)
                        val override =
                            DefaultTrackSelector.SelectionOverride(trackGroups.length - 1, 0)
                        parametersBuilder.setSelectionOverride(
                            textRendererIndex,
                            trackGroups,
                            override
                        )
                        parametersBuilder.setRendererDisabled(textRendererIndex, false)
                    } else {
                        parametersBuilder.clearSelectionOverrides(textRendererIndex)
                        parametersBuilder.setRendererDisabled(textRendererIndex, true)
                    }
                    playerState.mTrackSelector!!.setParameters(parametersBuilder)
                    setVideoCaptionsEnabled(show)
                    PrefManager.saveBoolean(
                        Objects.requireNonNull<Activity>(playerState.config.activity),
                        PrefManager.IS_CAPTIONS_ENABLED,
                        show
                    )
                    return true
                }
            }
        }
    } catch (e: Exception) {
        mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.message, e)
    }
    return false
}

/**
 * Returns a number of caption tracks that have a non-null language
 */
private fun hasAvailableSubtitlesTracks(
    mappedTrackInfo: MappingTrackSelector.MappedTrackInfo,
    textRendererIndex: Int
): Int {
    var result = 0
    try {
        val trackGroups = mappedTrackInfo.getTrackGroups(textRendererIndex)
        for (groupIndex in 0 until trackGroups.length) {
            val group = trackGroups[groupIndex]
            for (trackIndex in 0 until group.length) {
                val format = group.getFormat(trackIndex)
                if (playerState.defaultTrackFilter.filter(format, trackGroups)) {
                    result++
                }
            }
        }
    } catch (e: Exception) {
        if (playerState.config.isLoggingEnabled) {
            Log.d("ArcVideoSDK", "Exception thrown detecting CC tracks. " + e.message)
        }
    }
    return result
}

fun isClosedCaptionAvailable(): Boolean {
    return try {
        if (playerState.mTrackSelector != null) {
            val mappedTrackInfo = playerState.mTrackSelector!!.currentMappedTrackInfo
            return if (mappedTrackInfo != null) {
                val textRendererIndex = getTextRendererIndex(mappedTrackInfo)
                hasAvailableSubtitlesTracks(mappedTrackInfo, textRendererIndex) > 0
            } else {
                false
            }
        }
        false
    } catch (e: Exception) {
        false
    }
}

fun enableClosedCaption(enable: Boolean) = if (isClosedCaptionAvailable()) {
    toggleClosedCaption(enable)
} else false


/**
 * Enable/Disable captions rendering according to user preferences
 */
//    private fun initCaptions() {
//        val captionsEnabled: Boolean = isVideoCaptionsEnabled()
//        if (playerState.mTrackSelector != null) {
//            val mappedTrackInfo = playerState.mTrackSelector!!.currentMappedTrackInfo
//            if (mappedTrackInfo != null) {
//                val textRendererIndex: Int = getTextRendererIndex(mappedTrackInfo)
//                if (textRendererIndex != -1) {
//                    val parametersBuilder = playerState.mTrackSelector!!.buildUponParameters()
//                    parametersBuilder.setRendererDisabled(textRendererIndex, !captionsEnabled)
//                    playerState.mTrackSelector!!.setParameters(parametersBuilder)
//                }
//            }
//        }
//    } //TODO what does this do? we don't use it

}
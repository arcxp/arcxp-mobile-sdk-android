package com.arcxp.video.players

import android.annotation.SuppressLint
import android.app.Activity
import android.text.TextUtils
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import com.arcxp.sdk.R
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.ArcVideoSDKErrorType
import com.arcxp.video.model.PlayerState
import com.arcxp.video.util.PrefManager
import com.arcxp.video.util.Utils
import com.arcxp.video.views.ArcTrackSelectionView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.smoothstreaming.SsMediaSource
import androidx.media3.exoplayer.trackselection.MappingTrackSelector
import androidx.media3.common.util.Util
import java.util.Objects

/**
 * CaptionsManager is a class responsible for managing video captions within the ArcXP platform.
 * It handles the initialization, enabling, disabling, and selection of captions for video playback.
 * The class integrates with the video player to provide a seamless captioning experience.
 *
 * The class defines the following properties:
 * - playerState: An instance of PlayerState that holds the current state of the video player.
 * - utils: An instance of Utils for utility functions.
 * - mConfig: An instance of ArcXPVideoConfig containing configuration information for the video player.
 * - mListener: An instance of VideoListener for handling video-related events.
 *
 * Usage:
 * - Create an instance of CaptionsManager with the necessary parameters.
 * - Use the provided methods to manage video captions.
 *
 * Example:
 *
 * val captionsManager = CaptionsManager(playerState, utils, mConfig, mListener)
 * captionsManager.initVideoCaptions()
 *
 * Note: Ensure that all required properties are properly set before using the CaptionsManager instance.
 *
 * @property playerState An instance of PlayerState that holds the current state of the video player.
 * @property utils An instance of Utils for utility functions.
 * @property mConfig An instance of ArcXPVideoConfig containing configuration information for the video player.
 * @property mListener An instance of VideoListener for handling video-related events.
 * @method initVideoCaptions Initializes the video captions based on user preferences.
 * @method createMediaSourceWithCaptions Creates a media source with captions.
 * @method showCaptionsSelectionDialog Displays a dialog for selecting captions.
 * @method toggleClosedCaption Toggles the closed captions on or off.
 * @method isClosedCaptionAvailable Checks if closed captions are available.
 * @method enableClosedCaption Enables or disables closed captions.
 */
@SuppressLint("UnsafeOptInUsageError")
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
                        if (mConfig.isLoggingEnabled) {
                            Log.d("ArcVideoSDK", "Toggling CC on")
                        }
                        val trackGroups = mappedTrackInfo.getTrackGroups(textRendererIndex)
                        val override =
                            utils.createSelectionOverride(trackGroups.length - 1, 0)
                        parametersBuilder.setSelectionOverride(
                            textRendererIndex,
                            trackGroups,
                            override
                        )
                        parametersBuilder.setRendererDisabled(textRendererIndex, false)
                    } else {
                        if (mConfig.isLoggingEnabled) {
                            Log.d("ArcVideoSDK", "Toggling CC off")
                        }
                        parametersBuilder.clearSelectionOverrides(textRendererIndex)
                        parametersBuilder.setRendererDisabled(textRendererIndex, true)
                    }
                    playerState.mTrackSelector!!.setParameters(parametersBuilder)
                    setVideoCaptionsEnabled(!show)
                }
            }
        }
    }

    private fun toggleClosedCaption(show: Boolean): Boolean {
        if (mConfig.isLoggingEnabled) {
            val showString = if (show) "on" else "off"
            Log.d("ArcVideoSDK", "Call to toggle CC $showString")
        }
        try {
            val mappedTrackInfo = playerState.mTrackSelector!!.currentMappedTrackInfo!!
            val textRendererIndex = getTextRendererIndex(mappedTrackInfo)
            val parametersBuilder = playerState.mTrackSelector!!.buildUponParameters()
            if (show) {
                val trackGroups = mappedTrackInfo.getTrackGroups(textRendererIndex)
                val override =
                    utils.createSelectionOverride(trackGroups.length - 1, 0)
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
            return true
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
            if (mConfig.isLoggingEnabled) {
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
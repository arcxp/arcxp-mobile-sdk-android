package com.arc.arcvideo

import androidx.annotation.Keep
import com.arc.arcvideo.model.*

/**
 * This is the callback class that must be implemented to return video streams or errors to the
 * findByUuid() or findByUuids() methods in [ArcMediaClient]
 */
@Keep
interface ArcVideoStreamCallback {
    @Deprecated(message = "use onVideoStream for geo calls, and errors will go through onError")
    fun onVideoResponse(arcVideoResponse: ArcVideoResponse?) {}
    fun onVideoStream(videos : List<ArcVideoStream>?) {}
    fun onLiveVideos(videos : List<VideoVO>?) {}
    fun onVideoStreamVirtual(arcVideoStreamVirtualChannel : ArcVideoStreamVirtualChannel?) {}
    fun onError(type: ArcVideoSDKErrorType, message: String, value: Any?) {}
}

/**
 * This is the callback class that must be implemented to return playlists or errors to the
 * findByPlaylist() method in [ArcMediaClient]
 */
@Keep
interface ArcVideoPlaylistCallback {
    fun onVideoPlaylist(playlist : ArcVideoPlaylist?)
    fun onError(type: ArcVideoSDKErrorType, message: String, value: Any?)
}
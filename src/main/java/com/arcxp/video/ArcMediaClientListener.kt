package com.arcxp.video

import androidx.annotation.Keep
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.video.model.ArcVideoPlaylist
import com.arcxp.video.model.ArcVideoResponse
import com.arcxp.video.model.ArcVideoStream
import com.arcxp.video.model.ArcVideoStreamVirtualChannel
import com.arcxp.video.model.VideoVO

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
    fun onJsonResult(json: String) {}
    fun onError(type: ArcXPSDKErrorType, message: String, value: Any?) {}
}

/**
 * This is the callback class that must be implemented to return playlists or errors to the
 * findByPlaylist() method in [ArcMediaClient]
 */
@Keep
interface ArcVideoPlaylistCallback {
    fun onVideoPlaylist(playlist : ArcVideoPlaylist?)
    fun onError(type: ArcXPSDKErrorType, message: String, value: Any?)
    fun onJsonResult(json: String) {}
}
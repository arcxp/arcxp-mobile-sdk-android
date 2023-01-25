package com.arc.arcvideo.model

/**
 * @suppress
 */
public data class ArcVideoResponse(
        var arcTypeResponse: ArcTypeResponse?,
        var arcVideoStreams: List<ArcVideoStream>? = null
)
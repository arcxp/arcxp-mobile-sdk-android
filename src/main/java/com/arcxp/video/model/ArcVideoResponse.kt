package com.arcxp.video.model

/**
 * @suppress
 */
public data class ArcVideoResponse(
        var arcTypeResponse: ArcTypeResponse?,
        var arcVideoStreams: List<ArcVideoStream>? = null
)
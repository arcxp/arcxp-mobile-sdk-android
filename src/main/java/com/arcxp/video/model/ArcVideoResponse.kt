package com.arcxp.video.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
public data class ArcVideoResponse(
        var arcTypeResponse: ArcTypeResponse?,
        var arcVideoStreams: List<ArcVideoStream>? = null
)
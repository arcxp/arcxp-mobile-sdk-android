package com.arcxp.video.model

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
public data class ArcVideoPlaylist(
        val playlistName: String,
        val version: String,
        val playlistItems: List<ArcVideoStream>
)
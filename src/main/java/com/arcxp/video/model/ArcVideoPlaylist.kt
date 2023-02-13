package com.arcxp.video.model

import androidx.annotation.Keep

@Keep
public data class ArcVideoPlaylist(
        val playlistName: String,
        val version: String,
        val playlistItems: List<ArcVideoStream>
)
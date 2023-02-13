package com.arcxp.video.model

import androidx.annotation.Keep
import com.arcxp.video.ArcXPVideoSDK
import com.squareup.moshi.Json

@Keep
data class ArcVideoStreamVirtualChannel(
    val id: String,
    val name: String?,
    val description: String?,
    val websites: List<Website>?,
    val path: String?,
    val published: Boolean?,
    val state: String?,
    val url: String?,
    val encodingProfile: String?,
    val programs: List<Program>?,
    val adSettings: AdSettings?,
    val createdAt: String?,
    val modifiedAt: String?
)

@Keep
data class Website(
    val id: String?,
    val primary: Boolean,
    val sections: List<Section>?
)

@Keep
data class Section(
    val id: String?,
    val primary: Boolean
)

@Keep
data class AdSettings(
    val enabled: Boolean,
    val url: String?,
    val tag: String?,
    val encodingStatus: String?,
    val slate: Slate?
)

@Keep
data class Slate(
    val id: String?,
    val filename: String?,
    val key: String?,
    val bucket: String?,
    val url: String?,
    val duration: Int
)

@Keep data class Program(
    val id: String?,
    val ansId: String?,
    val name: String?,
    val description: String?,
    val url: String?,
    val imageUrl: String?,
    val duration: Double?
)



fun ArcVideoStreamVirtualChannel.thumbnail() =
    programs?.
    get(0)?.
    imageUrl?.
    let { ArcXPVideoSDK.resizer().createThumbnail(it.substringAfter("https://")) } ?: ""

fun ArcVideoStreamVirtualChannel.fallback() =
    programs?.
    get(0)?.
    imageUrl ?: ""
package com.arcxp.video.model

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class Restriction(val id: String,
                       val name: String,
                       val restrictions: List<RestrictionItem>)

@Keep
@JsonClass(generateAdapter = true)
data class RestrictionItem(val id: String,
                           val name: String,
                           val zips: List<String>,
                           val dmas: List<String>,
                           val countries: List<String>,
                           val startTime: Int,
                           val endTime: Int,
                           val action: String)

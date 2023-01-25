package com.arc.arcvideo.model

import androidx.annotation.Keep

@Keep
data class Restriction(val id: String,
                       val name: String,
                       val restrictions: List<RestrictionItem>)

@Keep
data class RestrictionItem(val id: String,
                           val name: String,
                           val zips: List<String>,
                           val dmas: List<String>,
                           val countries: List<String>,
                           val startTime: Int,
                           val endTime: Int,
                           val action: String)

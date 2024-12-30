package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPUserNote(
    val createdOn: String,
    val createdBy: String,
    val modifiedOn: String,
    val modifiedBy: String,
    val deletedOn: String,
    val id: Int,
    val note: String
)
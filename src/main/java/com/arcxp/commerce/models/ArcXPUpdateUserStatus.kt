package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPUpdateUserStatus(
    val createdOn: String,
    val createdBy: String,
    val modifiedOn: String,
    val modifiedBy: String,
    val deletedOn: String,
    val uuid: String,
    val status: String
)
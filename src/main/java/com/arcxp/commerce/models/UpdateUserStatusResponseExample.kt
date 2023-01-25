package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class UpdateUserStatusResponseExample(
    val createdOn: String,
    val createdBy: String,
    val modifiedOn: String,
    val deleteOn: String,
    val uuid: String,
    val status: String
)
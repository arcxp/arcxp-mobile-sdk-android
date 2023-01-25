package com.arcxp.commerce.models

import androidx.annotation.Keep

/**
 * @suppress
 */
@Keep
data class ArcXPMigrationResponseRecord(
    val email: String,
    val success: Boolean,
    val errorMessage: String,
    val result: String,
    val uuid: String
)
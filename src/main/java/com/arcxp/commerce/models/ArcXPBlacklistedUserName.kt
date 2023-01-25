package com.arcxp.commerce.models

import androidx.annotation.Keep


/**
 * @suppress
 */
@Keep
data class ArcXPBlacklistedUserName(
    val createdBy: Int,
    val createdOn: String,
    val deletedOn: String,
    val modifiedBy: Int,
    val modifiedOn: String,
    val createdName: String,
    val modifiedName: String,
    val externalID: Int,
    val id: Int,
    val signinProviderID: Int,
    val userName: String
)
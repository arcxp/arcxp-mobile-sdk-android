package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPProfileDeletionRule(
    val createdOn: String,
    val createdBy: String,
    val modifiedOn: String,
    val modifiedBy: String,
    val deletedOn: String,
    val id: Int,
    val name: String,
    val notificationTriggerDays: Int,
    val actionTriggerDays: Int,
    val notificationRecurrenceDays: Int,
    val notificationLimit: Int,
    val typeId: String
) {

    companion object {
        enum class Type {
            EMAIL_NEVER_VERIFIED, NO_SIGN_IN, LAST_LOGIN
        }
    }
}
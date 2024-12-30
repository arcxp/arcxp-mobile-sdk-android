package com.arcxp.commerce.models

import androidx.annotation.Keep

@Keep
data class ArcXPProfileDeletionRuleUpdateRequest(
    val name: String,
    val notificationTriggerDays: Int,
    val notificationRecurrenceDays: Int,
    val notificationLimit: Int
) {

}
package com.arcxp.commerce.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ArcXPActivePaywallRules(val response: List<ActivePaywallRule>)

@Keep
data class ActivePaywallRule(
    val id: Int,
    val conditions: HashMap<String, RuleCondition>?,
    val e: List<Any>,
    val cc: String?,
    val cl: String?,
    val rt: Int,
    val budget: RuleBudget
)

@Keep
data class RuleCondition(
    @SerializedName("in")
    val inOrOut: Boolean,
    val values: List<String>
)

@Keep
data class RuleBudget(
    val budgetType: String,
    val calendarType: String,
    val calendarWeekDay: String,
    val rollingType: String,
    val rollingDays: Int,
    val rollingHours: Int
)
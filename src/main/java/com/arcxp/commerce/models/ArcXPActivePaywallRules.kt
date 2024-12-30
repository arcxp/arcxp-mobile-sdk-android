package com.arcxp.commerce.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ArcXPActivePaywallRules(val response: List<ActivePaywallRule>)

@Keep
data class ActivePaywallRule(
    val id: Int,
    val conditions: HashMap<String, RuleCondition>?,  //Defined conditions for the rule
    val e: List<Any>?,  //List of entitlements
    val ent: List<Any>?, //List of V2 entitlements
    val cc: String?,
    val cl: String?,
    val rt: Int,  //Number of articles allowed per budget period
    val budget: RuleBudget
)

@Keep
data class RuleCondition(
    @SerializedName("in")
    val inOrOut: Boolean,  //Content criteria in or out value.  True = in
    val values: List<String>  //Content sections
)

@Keep
data class RuleBudget(
    val budgetType: String,  //Calendar or rolling
    val calendarType: String,  //Weekly or monthly
    val calendarWeekDay: String, //Day of week to reset budget
    val rollingType: String,  //Days or hours
    val rollingDays: Int,  //Number of days to reset budget
    val rollingHours: Int  //Number of hours to reset budget
)
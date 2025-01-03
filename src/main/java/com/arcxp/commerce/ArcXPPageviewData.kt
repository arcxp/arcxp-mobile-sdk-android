package com.arcxp.commerce

import androidx.annotation.Keep

/**
 * These classes are used by the paywall module to manage the rules and data for the paywall.
 */
@Keep
data class ArcXPPageviewData(val pageId: String, val conditions: HashMap<String, String>)

@Keep
data class ArcXPPageviewEvaluationResult(val pageId: String, val show: Boolean, val campaign: String? = null, val ruleId: Int? = null)

@Keep
abstract class ArcXPPageviewListener {
    open fun onInitializationResult(success: Boolean) {}
    open fun onEvaluationResult(response: ArcXPPageviewEvaluationResult) {}
}

@Keep
data class ArcXPRulesData(var rules: HashMap<Int, ArcXPRuleData>)

@Keep
data class ArcXPRuleData(var counter: Int,
                         var timestamp: Long,
                         var viewedPages: ArrayList<String>?,
                         var lastResetDay: Int)

@Keep
data class ArcXPRuleResetResult(val reset: Boolean, val ruleData: ArcXPRuleData)

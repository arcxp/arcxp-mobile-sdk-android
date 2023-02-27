package com.arcxp.commerce.paywall

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.arcxp.ArcXPMobileSDK.commerceConfig
import com.arcxp.commerce.*
import com.arcxp.commerce.apimanagers.*
import com.arcxp.commerce.models.*
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.Constants
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.util.*

/**
 * This class implements all paywall functionality.
 * The client code does not have any direct access to the methods
 * but must interact with the functionality through the [ArcXPCommerceManager].
 *
 * This class is instantiated with RetailApiManager and SalesApiManager instances.  These are used
 * by the initialization method to get the active paywall rules and the current users
 * entitlements (subscriptions).
 */
class PaywallManager(
    val context: Context,
    val retailApiManager: RetailApiManager?,
    val salesApiManager: SalesApiManager?
) {

    private var paywallRulesArcxp: ArcXPActivePaywallRules? = null
    private lateinit var entitlements: ArcXPEntitlements

    private var rulesData: ArcXPRulesData? = null

    private var currentTime: Long = System.currentTimeMillis()
    private var currentDate: Calendar = Calendar.getInstance()

    private var isLoggedIn = false

    private val sharedPreferences: SharedPreferences? =
        context.getSharedPreferences(Constants.PAYWALL_PREFERENCES, Context.MODE_PRIVATE)

    /**
     * Initialize the manager to prepare for a page evaluation.  This is called
     * each time new page is evaluated.  It loads the current paywall rules and the
     * users entitlements.  The client code can also pass in the users entitlements
     * in which case the method will not load those from the server.  This is optional by the
     * client code.
     *
     * @param entitlementsResponse Optional parameter to pass in the current user entitlements.
     * If null they will be loaded from the server.
     * @param listener [ArcXPPageviewListener] object that will report back success/failure of initialization
     */
    fun initialize(
        entitlementsResponse: ArcXPEntitlements? = null,
        passedInTime: Long? = null,
        loggedInState: Boolean,
        listener: ArcXPPageviewListener
    ) {

        if (passedInTime != null) {
            currentDate.timeInMillis = passedInTime
        }

        currentDate.set(Calendar.HOUR_OF_DAY, 0)
        currentDate.set(Calendar.MINUTE, 0)
        currentDate.set(Calendar.SECOND, 0)
        currentDate.set(Calendar.MILLISECOND, 0)
        currentTime = currentDate.timeInMillis

        val cstring = SimpleDateFormat("MM/dd/yyyy").format(currentDate.time)
        Log.e("TAG", "$cstring")

        isLoggedIn = loggedInState

        //Fetch ruleset
        retailApiManager?.getActivePaywallRules(object : ArcXPRetailListener() {
            override fun onGetActivePaywallRulesSuccess(responseArcxp: ArcXPActivePaywallRules) {
                paywallRulesArcxp = responseArcxp
                savePaywallRulesToPrefs()
                if (entitlementsResponse == null) {
                    //Fetch entitlements
                    salesApiManager?.getEntitlements(object : ArcXPSalesListener() {
                        override fun onGetEntitlementsSuccess(response: ArcXPEntitlements) {
                            entitlements = response
                            saveEntitlementsToPrefs()
                            loadRuleDataFromPrefs()

                            listener.onInitializationResult(true)
                        }

                        override fun onGetEntitlementsFailure(error: ArcXPException) {
                            listener.onInitializationResult(false)
                        }
                    })
                } else {
                    entitlements = entitlementsResponse
                    loadRuleDataFromPrefs()
                    listener.onInitializationResult(true)
                }
            }

            override fun onGetActivePaywallRulesFailure(error: ArcXPException) {
                if (commerceConfig().useCachedPaywall) {
                    loadPaywallFromPrefs()
                    loadEntitlementsFromPrefs()
                    loadRuleDataFromPrefs()
                    if (paywallRulesArcxp != null){
                        listener.onInitializationResult(true)
                    } else {
                        listener.onInitializationResult(false)
                    }

                } else {
                    listener.onInitializationResult(false)
                }
            }
        })
    }

    @VisibleForTesting
    fun setCurrentDate(time: Long) {
        currentDate.timeInMillis = time
        currentTime = currentDate.timeInMillis
    }

    /**
     * The rules data (last reset time, viewed pages, etc) are saved as a json string
     * in shared preferences and loaded at runtime.  This is the easiest way of storing
     * all of this information given it is repeated for multiple rules.
     */
    private fun loadRuleDataFromPrefs() {
        val json = sharedPreferences?.getString(Constants.PAYWALL_PREFS_RULES_DATA, null)
        rulesData = if (json == null || json == "null") {
            ArcXPRulesData(HashMap<Int, ArcXPRuleData>())
        } else {
            Gson().fromJson(json, ArcXPRulesData::class.java)
        }
    }

    private fun loadPaywallFromPrefs() {
        val json = sharedPreferences?.getString(Constants.PAYWALL_RULES, null)
        paywallRulesArcxp = if (json == null || json == "null") {
            null
        } else {
            Gson().fromJson(json, ArcXPActivePaywallRules::class.java)
        }
    }

    private fun loadEntitlementsFromPrefs() {
        val json = sharedPreferences?.getString(Constants.ENTITLEMENTS, null)
        entitlements = if (json == null || json == "null") {
            ArcXPEntitlements(listOf(), Edgescape(null, null, null, null, null))
        } else {
            Gson().fromJson(json, ArcXPEntitlements::class.java)
        }
    }

    /**
     * Save the rules data to shared preferences
     */
    private fun saveRulesToPrefs() {
        val json = Gson().toJson(rulesData)
        sharedPreferences?.edit()?.putString(Constants.PAYWALL_PREFS_RULES_DATA, json)?.commit()
    }

    /**
     * Save paywall rules to shared preferences
     */
    private fun savePaywallRulesToPrefs() {
        val json = Gson().toJson(paywallRulesArcxp)
        sharedPreferences?.edit()?.putString(Constants.PAYWALL_RULES, json)?.commit()
    }

    /**
     * Save entitlements to shared preferences
     */
    private fun saveEntitlementsToPrefs() {
        val json = Gson().toJson(entitlements)
        sharedPreferences?.edit()?.putString(Constants.ENTITLEMENTS, json)?.commit()
    }


    fun clearPaywallCache() {
        sharedPreferences?.edit()?.clear()?.apply()
    }

    fun getPaywallCache(): String? {
        return sharedPreferences?.getString(Constants.PAYWALL_PREFS_RULES_DATA, null)
    }

    /**
     * Evaluate a page using the paywall algorithm and the rules and entitlements loaded
     * during the initialize method.
     *
     * @param pageviewData [ArcXPPageviewData] object with the condition data for the page
     *
     * @return [ArcXPPageviewEvaluationResult] object.  show = true: Show page, show = false: Do not show page
     */
    fun evaluate(pageviewData: ArcXPPageviewData): ArcXPPageviewEvaluationResult {
        //The default values are to show the page and no campaign string
        //If any of the rules are tripped they will modify these values
        //If no rules are tripped then they will stay as the default
        var showPage = true
        var campaign: String? = null

        //Iterate through each of the rules returned by the server call getAllActivePaywallRules()
        //We will iterate through every rule but only update the return value based upon the first one
        //that trips.  This is because each rule may need to have its stored values updated (reset the counter,
        // update the counter, reset the timestamp, etc).
        paywallRulesArcxp?.response?.iterator()?.forEach {
            if (evaluateRule(it, pageviewData)) {
                //If true is returned then this rule has tripped
                //If this is the first rule to trip then update the return
                //values.  Otherwise we just ran the rule to update its own
                //counters
                if (showPage) {
                    showPage = false
                    campaign = it.cl
                }
            }
            //else rule does not apply so skip it
        }

        return ArcXPPageviewEvaluationResult(
            pageId = pageviewData.pageId,
            show = showPage,
            campaign = campaign
        )
    }

    /**
     *  Evaluate an individual rule to determine if it applies for a given page.
     *
     *  @param pageviewData [ArcXPPageviewData] object with the page data
     *
     *  @return true = rule applies, false = rule does not apply
     */
    private fun evaluateRule(rule: ActivePaywallRule, pageviewData: ArcXPPageviewData): Boolean {
        //Load the data for this rule, if it exists
        var ruleData = rulesData?.rules?.get(rule.id)
        if (ruleData == null) {
            ruleData = ArcXPRuleData(0, currentTime, ArrayList<String>(), 0)
            rulesData?.rules?.set(rule.id, ruleData)
        }

        //check entitlements
        if (evaluateEntitlements(rule.e)) {
            //check conditions
            if (evaluateConditions(rule.conditions, pageviewData.conditions)) {
                //check if the counter needs to be reset
                checkResetCounters(ruleData, rule.budget)
                //See if this page has not been viewed before
                if (checkNotViewed(ruleData, pageviewData.pageId)) {
                    //Check to see if we are over budget
                    if (checkOverBudget(ruleData, rule.rt)) {
                        return true
                    } else {
                        //Not over budget yet so add the page to the viewed pages list and update
                        //the counter
                        ruleData.counter++
                        ruleData.viewedPages?.add(pageviewData.pageId)
                    }
                }
            }
        }
        saveRulesToPrefs()
        return false
    }

    /**
     * Evaluate the entitlements for a rule to see if they apply
     *
     * @param entitlements List of entitlements from the rule
     * @param entitlementResponse [ArcXPEntitlements] object for the logged in user
     *
     * @return true = rule applies, false = rule does not apply
     */
    fun evaluateEntitlements(entitlements: List<Object>): Boolean {
        //Make sure we have entitlements
        if (entitlements.isNotEmpty()) {
            try {
                if (entitlements.size == 1) {
                    if (isLoggedIn) {
                        return !(entitlements[0] as Boolean)
                    } else {
                        return true
                    }
                } else {
                    val entitlementSkus = entitlements.subList(1, entitlements.size)

                    for (e in this.entitlements.skus) {
                        for (entitlementSku in entitlementSkus) {
                            if (e.sku.lowercase() == (entitlementSku as String).lowercase()) {
                                return !(entitlements[0] as Boolean)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                //If an exception is thrown then we know that something was screwed up in
                //the entitlements list so return false
                return true
            }
        }
        return true
    }

    /**
     * Evaluate the page view conditions to see if the rule applies
     *
     * @param ruleConditions Map of conditions for the rule
     * @param pageConditions Map of conditions for the page
     *
     * @return true if the rule applies, false if the rule does not apply
     */
    fun evaluateConditions(
        ruleConditions: HashMap<String, RuleCondition>?,
        pageConditions: HashMap<String, String>
    ): Boolean {
        var apply: Boolean? = null

        //Count the number of conditions that apply
        var conditionsCount = 0

        //For each condition in the rules
        if (ruleConditions != null) {
            for (condition in ruleConditions) {
                //Is this condition in or out?
                if (condition.value.inOrOut) {
                    conditionsCount++
                    //This rule is for IN conditions
                    //Check for a matching condition
                    if (pageConditions[condition.key] != null) {
                        if (condition.value.values.contains(pageConditions[condition.key])) {
                            //We are in IN so this rule may apply
                            apply = apply ?: true
                        } else {
                            //The condition is not in the pageConditions so this rule does not apply
                            apply = false
                        }
                    } else {
                        //If the condition doesn't exist then the rule does not apply
                        apply = false
                    }
                } else {
                    //This rule is for OUT conditions
                    //Check for a matching condition
                    if (pageConditions[condition.key] != null) {
                        //If we have a matching condition with the rule and the page but it is
                        //OUT then we must deduct it from the total condition count
                        conditionsCount++
                        if (condition.value.values.contains(pageConditions[condition.key])) {
                            //We are in OUT so this rule does not apply
                            apply = false
                        } else {
                            apply = apply ?: true
                        }
                    }
                }
            }
        }

        //If none of the conditions were triggered then this rule does not apply so return false
        //or if the number of total conditions in the rule does not match the number of conditions
        //on the page then this rule cannot apply
        if (apply == null || conditionsCount != pageConditions.size) {
            apply = false
        }
        return apply
    }

    /**
     * Make a call to see if the counters need to be reset.  If the result is true then
     * save the rules to shared preferences.
     *
     * @param ruleData The data that has been stored for this rule from shared preferences.
     * @pram ruleBudget [RuleBudget] object from the paywall rule.
     *
     * @return True = counter was reset, false = counter was not reset.
     */
    fun evaluateResetCounters(ruleData: ArcXPRuleData, ruleBudget: RuleBudget): Boolean {
        val result = checkResetCounters(ruleData, ruleBudget)
        if (result.reset) {
            saveRulesToPrefs()
        }
        return result.reset
    }

    /**
     * Check if the threshold has been reached to require the budget counter to be reset
     * and reset it if it is required.
     *
     * @param ruleData The data that has been stored for this rule from shared preferences.
     * @pram ruleBudget [RuleBudget] object from the paywall rule.
     *
     * @return [ArcXPRuleResetResult] object that contains if a reset is need and the rule.
     */
    fun checkResetCounters(ruleData: ArcXPRuleData, ruleBudget: RuleBudget): ArcXPRuleResetResult {
        //Flag to determine if we will need to reset the counter
        var reset = false

        when (ruleBudget.budgetType.toLowerCase()) {
            "calendar" -> {
                //The previous date is stored in milliseconds since epoch
                var storedDate = Calendar.getInstance()
                if (ruleData.timestamp > 0L) {
                    //If we have a stored value create a date from it
                    storedDate.timeInMillis = ruleData.timestamp
                }

                //These values will be used by both branches of the algorithm
                val storedYear = storedDate.get(Calendar.YEAR)
                val currentYear = currentDate.get(Calendar.YEAR)

                when (ruleBudget.calendarType.toLowerCase()) {
                    "weekly" -> {
                        var storedWeek = storedDate.get(Calendar.WEEK_OF_YEAR)
                        val currentWeek = currentDate.get(Calendar.WEEK_OF_YEAR)
                        val currentDay = currentDate.get(Calendar.DAY_OF_YEAR)

                        //If the stored week is one year prior to the current week then
                        //both will have the same week number and our math will not know they
                        //are different weeks so adjust the stored week integer
                        if (storedYear != currentYear && storedWeek >= currentWeek) {
                            storedWeek -= 52
                        }

                        //If our stored year and current year are the same then we can compare
                        //our two weeks.
                        //else
                        //If our stored year is from the previous year then we are in a new week
                        //and should reset
                        val resetDayOfWeekstr =
                            DayOfWeek.valueOf(ruleBudget.calendarWeekDay.uppercase())
                        var resetDayOfWeek = (resetDayOfWeekstr.ordinal + 1) % 7 + 1
                        val currentDayOfWeek = currentDate.get(Calendar.DAY_OF_WEEK)

                        //We are in a new week so make sure we have passed the reset day
                        if (storedWeek < currentWeek) {
                            //We are in a new week so see if we passed the reset day
                            if (resetDayOfWeek - currentDayOfWeek <= 0) {
                                if (ruleData.lastResetDay != currentDay) {
                                    reset = true
                                    ruleData.lastResetDay = currentDay
                                }
                            }
                            //We are in the same week so see if we have passed over or are at the reset day
                            //since our last reading
                        } else if (storedWeek == currentWeek) {
                            val storedDayOfWeek = storedDate.get(Calendar.DAY_OF_WEEK)

                            if ((storedDayOfWeek < resetDayOfWeek) && (currentDayOfWeek >= resetDayOfWeek)) {
                                if (ruleData.lastResetDay != currentDay) {
                                    reset = true
                                    ruleData.lastResetDay = currentDay
                                }
                            }
                        }
                    }
                    "monthly" -> {
                        val storedMonth = storedDate.get(Calendar.MONTH)
                        val currentMonth = currentDate.get(Calendar.MONTH)
                        val currentDay = currentDate.get(Calendar.DAY_OF_YEAR)

                        //If our stored year and current year are the same then we can compare
                        //the two months.  If we are no longer in the same month then we have
                        //are now in a new month and should reset
                        //else
                        //If our stored year is from the previous year then we have to be in
                        //a new month so we need to reset
                        //else
                        //we must be in the same month so reset flag remains false
                        if ((storedYear == currentYear && currentMonth > storedMonth) || (storedYear < currentYear)) {
                            if (ruleData.lastResetDay != currentDay) {
                                reset = true
                                ruleData.lastResetDay = currentDay
                            }
                        }
                    }
                }
            }
            "rolling" -> {
                val timeDelta = currentTime - ruleData.timestamp
                //Number of days and hours between the stored time and today
                val daysDelta = timeDelta / 86400000
                val hoursDelta = timeDelta / 3600000

                when (ruleBudget.rollingType.toLowerCase()) {
                    "days" -> {
                        //If we are over our reset point then set the reset flag
                        if (ruleBudget.rollingDays <= daysDelta) {
                            reset = true
                        }
                    }
                    "hours" -> {
                        //If we are over our reset point then set the reset flag
                        if (ruleBudget.rollingHours < hoursDelta) {
                            reset = true
                        }
                    }
                }
            }
        }
        if (reset) {
            ruleData.timestamp = currentTime
            ruleData.counter = 0
        }

        return ArcXPRuleResetResult(reset, ruleData)
    }

    /**
     * Check if the page has not been viewed as part of this rule.  Reverse logic
     * here in order to keep the if statements consistent in the parent method.
     *
     * @return true = page has not been viewed, false = page has been viewed
     */
    fun checkNotViewed(ruleData: ArcXPRuleData, pageId: String): Boolean {
        return !ruleData.viewedPages!!.contains(pageId)
    }

    /**
     * Check if this rules counter is over the budget
     */
    fun checkOverBudget(ruleData: ArcXPRuleData, budget: Int): Boolean {
        return (ruleData.counter >= budget)
    }

    @VisibleForTesting
    fun setEntitlements(e: ArcXPEntitlements) {
        this.entitlements = e
    }

    @VisibleForTesting
    fun setLoggedIn(l: Boolean) {
        this.isLoggedIn = l
    }

    companion object {
        public enum class DayOfWeekAdjusted(val value: String) {
            SUNDAY("sunday"), MONDAY("monday"), TUESDAY("tuesday"), WEDNESDAY("wednesday"), THURSDAY(
                "thursday"
            ),
            FRIDAY("friday"), SATURDAY("saturday")
        }
    }
}
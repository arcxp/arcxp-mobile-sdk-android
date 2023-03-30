package com.arcxp.commerce.paywall

import android.content.Context
import android.content.SharedPreferences
import com.arcxp.ArcXPMobileSDK
import com.arcxp.ArcXPMobileSDK.commerceConfig
import com.arcxp.commerce.ArcXPCommerceConfig
import com.arcxp.commerce.ArcXPPageviewListener
import com.arcxp.commerce.ArcXPRuleData
import com.arcxp.commerce.apimanagers.RetailApiManager
import com.arcxp.commerce.apimanagers.SalesApiManager
import com.arcxp.commerce.base.BaseUnitTest
import com.arcxp.commerce.callbacks.ArcXPRetailListener
import com.arcxp.commerce.callbacks.ArcXPSalesListener
import com.arcxp.commerce.di.configureTestAppComponent
import com.arcxp.commerce.models.ArcXPActivePaywallRules
import com.arcxp.commerce.models.ArcXPEntitlements
import com.arcxp.commerce.models.RuleBudget
import com.arcxp.commerce.models.RuleCondition
import com.arcxp.commerce.repositories.RetailRepository
import com.arcxp.commerce.repositories.SalesRepository
import com.arcxp.commerce.retrofit.RetailService
import com.arcxp.commerce.retrofit.SalesService
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.Constants
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Success
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.inject
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.*

class PaywallManagerTest : BaseUnitTest() {

    private lateinit var paywallRules: ArcXPActivePaywallRules
    private lateinit var entitlements: ArcXPEntitlements

    private lateinit var retailRepository: RetailRepository
    private lateinit var salesRepository: SalesRepository

    @MockK
    private lateinit var context: Context

    @RelaxedMockK
    private lateinit var retailApiManager: RetailApiManager

    @RelaxedMockK
    private lateinit var salesApiManager: SalesApiManager

    private val retailService: RetailService by inject(RetailService::class.java)
    private val salesService: SalesService by inject(SalesService::class.java)

    @RelaxedMockK
    private lateinit var sharedPreferences: SharedPreferences

    @RelaxedMockK
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor

    @RelaxedMockK
    private lateinit var listener: ArcXPPageviewListener

    @RelaxedMockK
    private lateinit var config: ArcXPCommerceConfig

    private val preference = Constants.PAYWALL_PREFERENCES

    @Before
    fun setup() {
        super.setUp()
        MockKAnnotations.init(this, relaxed = true)
        startKoin { modules(configureTestAppComponent(getMockWebServerUrl())) }
        every { sharedPreferences.edit() } returns sharedPreferencesEditor
        every {
            context.getSharedPreferences(
                preference,
                Context.MODE_PRIVATE
            )
        } returns sharedPreferences
        retailRepository = RetailRepository(retailService)
        salesRepository = SalesRepository(salesService)

        loadPaywallRules()

        loadSubscriptions()
    }


    private fun loadPaywallRules() = runTest {
        mockNetworkResponseWithFileContent(
            "paywall_active_rules.json",
            HttpURLConnection.HTTP_OK
        )

        val response: Either<Any?, ArcXPActivePaywallRules?> =
            retailRepository.getActivePaywallRules()
        val successResponse = response as Success<ArcXPActivePaywallRules>
        paywallRules = successResponse.success
    }

    private fun loadSubscriptions() = runTest {
        mockNetworkResponseWithFileContent(
            "sales_entitlements.json",
            HttpURLConnection.HTTP_OK
        )

        val response: Either<Any?, ArcXPEntitlements?> = salesRepository.getEntitlements()
        val successResponse = response as Success<ArcXPEntitlements>
        entitlements = successResponse.success
    }

    @Test
    fun `verify paywall rules were stored in shared prefs on successful response from getActivePaywallRules`() {
        val testObject = PaywallManager(context, retailApiManager, null)

        mockkObject(ArcXPMobileSDK)
        every { commerceConfig() } returns config
        val response = mockk<ArcXPActivePaywallRules>()
        val captureCallback = slot<ArcXPRetailListener>()

        testObject.initialize(listener = listener, loggedInState = false)
        verify(exactly = 1) {
            retailApiManager.getActivePaywallRules(capture(captureCallback))
        }
        captureCallback.captured.onGetActivePaywallRulesSuccess(response)
        verify(exactly = 1) {
            sharedPreferencesEditor.putString(Constants.PAYWALL_RULES, "{}")
        }
    }

    @Test
    fun `verify entitlements were stored in shared prefs on successful response from getEntitlements`() {
        val testObject = PaywallManager(context, retailApiManager, salesApiManager)

        mockkObject(ArcXPMobileSDK)
        every { commerceConfig() } returns config
        val response = mockk<ArcXPActivePaywallRules>()
        val salesResponse = mockk<ArcXPEntitlements>()
        val captureCallback = slot<ArcXPRetailListener>()
        val salesCallback = slot<ArcXPSalesListener>()

        testObject.initialize(
            listener = listener,
            entitlementsResponse = null,
            loggedInState = false
        )
        verify(exactly = 1) {
            retailApiManager.getActivePaywallRules(capture(captureCallback))
        }
        captureCallback.captured.onGetActivePaywallRulesSuccess(response)

        verify(exactly = 1) {
            sharedPreferencesEditor.putString(Constants.PAYWALL_RULES, "{}")
            salesApiManager.getEntitlements(capture(salesCallback))
        }

        salesCallback.captured.onGetEntitlementsSuccess(salesResponse)

        verify(exactly = 1) {
            sharedPreferencesEditor.putString(Constants.ENTITLEMENTS, "{}")
        }

    }

    @Test
    fun `verify paywall rules are retrieved from shared prefs on failed response from getActivePaywallRules`() {
        val testObject = PaywallManager(context, retailApiManager, null)

        mockkObject(ArcXPMobileSDK)
        every { commerceConfig() } returns config
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPRetailListener>()

        val activePaywallRulesJson = getJson("active_rule.json")
        val salesEntitlementsJson = getJson("sales_entitlements.json")

        every {
            config.useCachedPaywall
        } returns true

        every {
            sharedPreferences.getString(Constants.PAYWALL_RULES, null)
        } returns activePaywallRulesJson

        every {
            sharedPreferences.getString(Constants.ENTITLEMENTS, null)
        } returns salesEntitlementsJson

        testObject.initialize(listener = listener, loggedInState = false)
        verify(exactly = 1) {
            retailApiManager.getActivePaywallRules(capture(captureCallback))
        }
        captureCallback.captured.onGetActivePaywallRulesFailure(response)


        verify(exactly = 1) {
            sharedPreferences.getString(Constants.PAYWALL_RULES, null)
            sharedPreferences.getString(Constants.ENTITLEMENTS, null)
        }
    }

    @Test
    fun `verify when paywall rules are null from shared pref, listener returns false`() {
        val testObject = PaywallManager(context, retailApiManager, null)

        mockkObject(ArcXPMobileSDK)
        every { commerceConfig() } returns config
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPRetailListener>()

        every {
            config.useCachedPaywall
        } returns true

        every {
            sharedPreferences.getString(Constants.PAYWALL_RULES, null)
        } returns null

        every {
            sharedPreferences.getString(Constants.ENTITLEMENTS, null)
        } returns null

        testObject.initialize(listener = listener, loggedInState = false)
        verify(exactly = 1) {
            retailApiManager.getActivePaywallRules(capture(captureCallback))
        }
        captureCallback.captured.onGetActivePaywallRulesFailure(response)


        verify(exactly = 1) {
            listener.onInitializationResult(false)
        }
    }

    @Test
    fun `verify when entitlements and paywall are "null" from shared pref, listener returns false`() {
        val testObject = PaywallManager(context, retailApiManager, null)

        mockkObject(ArcXPMobileSDK)
        every { commerceConfig() } returns config
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPRetailListener>()

        every {
            config.useCachedPaywall
        } returns true

        every {
            sharedPreferences.getString(Constants.PAYWALL_RULES, null)
        } returns "null"

        every {
            sharedPreferences.getString(Constants.ENTITLEMENTS, null)
        } returns "null"

        testObject.initialize(listener = listener, loggedInState = false)
        verify(exactly = 1) {
            retailApiManager.getActivePaywallRules(capture(captureCallback))
        }
        captureCallback.captured.onGetActivePaywallRulesFailure(response)


        verify(exactly = 1) {
            listener.onInitializationResult(false)
        }
    }

    @Test
    fun `return false to Paywall listener when client turns off autoCachePaywallRules`() {
        val testObject = PaywallManager(context, retailApiManager, null)

        mockkObject(ArcXPMobileSDK)
        every { commerceConfig() } returns config
        val response = mockk<ArcXPException>()
        val captureCallback = slot<ArcXPRetailListener>()

        every {
            config.useCachedPaywall
        } returns false


        testObject.initialize(listener = listener, loggedInState = false)
        verify(exactly = 1) {
            retailApiManager.getActivePaywallRules(capture(captureCallback))
        }
        captureCallback.captured.onGetActivePaywallRulesFailure(response)


        verify(exactly = 1) {
            listener.onInitializationResult(false)
        }
    }

    @Test
    fun `getPaywallCache returns expected`() {
        val expected = "expected"
        val testObject = PaywallManager(context, retailApiManager, salesApiManager)
        every {
            sharedPreferences.getString(
                Constants.PAYWALL_PREFS_RULES_DATA,
                null
            )
        } returns expected

        val actual = testObject.getPaywallCache()

        assertEquals(expected, actual)
    }

    @Test
    fun `clearPaywallCache runs clear and applies to shared prefs`() {
        val testObject = PaywallManager(context, retailApiManager, salesApiManager)
        every {
            sharedPreferencesEditor.clear()
        } returns sharedPreferencesEditor

        testObject.clearPaywallCache()

        verify {
            sharedPreferencesEditor.clear()
            sharedPreferencesEditor.apply()
        }
    }


    @Test
    fun `test evaluate entitlements`() {
        val testObject = PaywallManager(context, retailApiManager, salesApiManager)
        testObject.setEntitlements(entitlements)
        val userEntitlements1 = arrayListOf<Object>(true as Object, "premium" as Object)
        var resulttype = testObject.evaluateEntitlements(userEntitlements1)
        assertFalse(resulttype)
        val userEntitlements2 = arrayListOf<Object>(true as Object, "guest" as Object)
        resulttype = testObject.evaluateEntitlements(userEntitlements2)
        assertTrue(resulttype)
        val userEntitlements3 = arrayListOf<Object>(false as Object, "premium" as Object)
        resulttype = testObject.evaluateEntitlements(userEntitlements3)
        assertTrue(resulttype)
    }

    @Test
    fun `test evaluate entitlements registered user`() {
        val testObject = PaywallManager(context, retailApiManager, salesApiManager)
        testObject.setLoggedIn(true)
        val userEntitlements1 = arrayListOf<Object>(true as Object)
        var resulttype = testObject.evaluateEntitlements(userEntitlements1)
        assertFalse(resulttype)
        testObject.setLoggedIn(false)
        resulttype = testObject.evaluateEntitlements(userEntitlements1)
        assertTrue(resulttype)
    }

    @Test
    fun `test evaluate conditions`() {
        val testObject = PaywallManager(context, retailApiManager, salesApiManager)
        val ruleCondition1 = RuleCondition(true, arrayListOf("mobile"))
        val ruleCondition2 = RuleCondition(false, arrayListOf("europe"))
        val ruleConditions = hashMapOf(
            Pair("deviceType", ruleCondition1),
            Pair("locations", ruleCondition2)
        )
        val pageCondition1 = hashMapOf(Pair<String, String>("deviceType", "mobile"))
        var resulttype = testObject.evaluateConditions(ruleConditions, pageCondition1)
        assertTrue(resulttype)
        val pageCondition2 = hashMapOf(
            Pair("deviceType", "mobile"),
            Pair("locations", "US")
        )
        resulttype = testObject.evaluateConditions(ruleConditions, pageCondition2)
        assertTrue(resulttype)
        val pageCondition3 = hashMapOf(
            Pair("deviceType", "mobile"),
            Pair("locations", "europe")
        )
        resulttype = testObject.evaluateConditions(ruleConditions, pageCondition3)
        assertFalse(resulttype)
        val pageCondition4 = hashMapOf(
            Pair("deviceType", "tablet"),
            Pair("locations", "US")
        )
        resulttype = testObject.evaluateConditions(ruleConditions, pageCondition4)
        assertFalse(resulttype)

        val ruleConditions2 = hashMapOf(
            Pair(
                "contentType", RuleCondition(
                    true,
                    listOf("story")
                )
            ), Pair("deviceType", RuleCondition(true, listOf("mobile")))
        )
        val pageCondition5 = hashMapOf(
            Pair("deviceType", "mobile"),
            Pair("contentType", "story")
        )
        resulttype = testObject.evaluateConditions(ruleConditions2, pageCondition5)
        assertTrue(resulttype)

        val ruleConditions3 = hashMapOf(
            Pair(
                "deviceType",
                RuleCondition(false, listOf("mobile"))
            )
        )
        val pageCondition6 = hashMapOf(Pair("deviceType", "tablet"))
        resulttype = testObject.evaluateConditions(ruleConditions3, pageCondition6)
        assertTrue(resulttype)
    }

    @Test
    fun `test counter reset rolling days`() {
        val testObject = PaywallManager(context, retailApiManager, salesApiManager)
        val testDate = Calendar.getInstance()
        testDate.time = SimpleDateFormat("MM/dd/yyyy").parse("07/10/2021")

        val today = Calendar.getInstance()
        today.time = SimpleDateFormat("MM/dd/yyyy").parse("07/20/2021")

        testObject.setCurrentDate(today.timeInMillis)
        var ruleData = ArcXPRuleData(
            counter = 10,
            timestamp = testDate.timeInMillis,  //ten days ago
            viewedPages = null, lastResetDay = 0
        )
        val ruleBudget1 = RuleBudget(
            budgetType = "Rolling", calendarType = "", calendarWeekDay = "",
            rollingType = "Days", rollingDays = 10, rollingHours = 0
        )
        var result = testObject.checkResetCounters(ruleData, ruleBudget1)
        assertTrue(result.reset)
        assertEquals(result.ruleData.counter, 0)

        ruleData = ArcXPRuleData(
            counter = 10,
            timestamp = testDate.timeInMillis,  //ten days ago
            viewedPages = null,
            lastResetDay = 0
        )

        val ruleBudget2 = RuleBudget(
            budgetType = "Rolling", calendarType = "", calendarWeekDay = "",
            rollingType = "Days", rollingDays = 11, rollingHours = 0
        )
        result = testObject.checkResetCounters(ruleData, ruleBudget2)
        assertFalse(result.reset)
        assertEquals(result.ruleData.counter, 10)
    }

    @Test
    fun `test counter reset rolling hours`() {
        val testObject = PaywallManager(context, retailApiManager, salesApiManager)
        val testDate = Calendar.getInstance()
        testDate.time = SimpleDateFormat("MM/dd/yyyy h:mm a").parse("07/22/2021 8:00 AM")

        val today = Calendar.getInstance()
        today.time = SimpleDateFormat("MM/dd/yyyy h:mm a").parse("07/23/2021 8:00 AM")

        testObject.setCurrentDate(today.timeInMillis)

        val ruleData = ArcXPRuleData(
            counter = 10,
            timestamp = testDate.timeInMillis,  //24 hours ago
            viewedPages = null, lastResetDay = 0
        )

        val ruleBudget3 = RuleBudget(
            budgetType = "Rolling", calendarType = "", calendarWeekDay = "",
            rollingType = "Hours", rollingDays = 0, rollingHours = 24
        )
        var result = testObject.checkResetCounters(ruleData, ruleBudget3)
        assertFalse(result.reset)
        assertEquals(result.ruleData.counter, 10)

        val ruleBudget4 = RuleBudget(
            budgetType = "Rolling", calendarType = "", calendarWeekDay = "",
            rollingType = "Hours", rollingDays = 0, rollingHours = 20
        )
        result = testObject.checkResetCounters(ruleData, ruleBudget4)
        assertTrue(result.reset)
        assertEquals(result.ruleData.counter, 0)
    }

    @Test
    fun `test counter reset calendar monthly`() {
        val testObject = PaywallManager(context, retailApiManager, salesApiManager)
        val testDate = Calendar.getInstance()
        testDate.time = SimpleDateFormat("MM/dd/yyyy").parse("07/22/2021")

        val today = Calendar.getInstance()
        today.time = SimpleDateFormat("MM/dd/yyyy").parse("08/01/2021")

        testObject.setCurrentDate(today.timeInMillis)

        var ruleData = ArcXPRuleData(
            counter = 10,
            timestamp = testDate.timeInMillis,
            viewedPages = null, lastResetDay = 0
        )

        val ruleBudget5 = RuleBudget(
            budgetType = "Calendar", calendarType = "Monthly", calendarWeekDay = "",
            rollingType = "", rollingDays = 0, rollingHours = 0
        )
        var result = testObject.checkResetCounters(ruleData, ruleBudget5)
        assertTrue(result.reset)
        assertEquals(result.ruleData.counter, 0)

        val testDate2 = Calendar.getInstance()
        testDate2.time = SimpleDateFormat("MM/dd/yyyy").parse("07/26/2021")

        today.time = SimpleDateFormat("MM/dd/yyyy").parse("07/01/2021")

        testObject.setCurrentDate(today.timeInMillis)

        ruleData = ArcXPRuleData(
            counter = 10,
            timestamp = testDate2.timeInMillis,  //32 days ago
            viewedPages = null, lastResetDay = 0
        )
        result = testObject.checkResetCounters(ruleData, ruleBudget5)
        assertFalse(result.reset)
        assertEquals(result.ruleData.counter, 10)

    }

    @Test
    fun `test counter reset calendar weekly`() {
        val testObject = PaywallManager(context, retailApiManager, salesApiManager)
        val testDate = Calendar.getInstance()
        testDate.time = SimpleDateFormat("MM/dd/yyyy").parse("07/26/2021")  //Monday
        testObject.setCurrentDate(testDate.timeInMillis)

        val storedDate = Calendar.getInstance()
        storedDate.time = SimpleDateFormat("MM/dd/yyyy").parse("07/26/2021") //Monday

        val ruleData = ArcXPRuleData(
            counter = 10,
            timestamp = storedDate.timeInMillis,  //5 days prior
            viewedPages = null, lastResetDay = 0
        )

        val ruleBudget1 = RuleBudget("Calendar", "Weekly", "Sunday", "", 0, 0)
        var result = testObject.checkResetCounters(ruleData, ruleBudget1)

        assertFalse(result.reset)
        assertEquals(result.ruleData.counter, 10)

        testDate.time = SimpleDateFormat("MM/dd/yyyy").parse("08/01/2021")  //Sunday
        testObject.setCurrentDate(testDate.timeInMillis)

        result = testObject.checkResetCounters(ruleData, ruleBudget1)

        assertTrue(result.reset)
        assertEquals(result.ruleData.counter, 0)
    }

    @Test
    fun `test not viewed`() {
        val testObject = PaywallManager(context, retailApiManager, salesApiManager)
        val ruleData = ArcXPRuleData(0, 0, arrayListOf("12345"), 0)
        var returnval = testObject.checkNotViewed(ruleData = ruleData, pageId = "12345")
        assertFalse(returnval)
        returnval = testObject.checkNotViewed(ruleData = ruleData, pageId = "23456")
        assertTrue(returnval)
    }

    @Test
    fun `test check budget`() {
        val testObject = PaywallManager(context, retailApiManager, salesApiManager)
        var ruleData = ArcXPRuleData(10, 0, null, 0)
        var returnval = testObject.checkOverBudget(ruleData = ruleData, budget = 10)
        assertTrue(returnval)
        ruleData = ArcXPRuleData(9, 0, null, 0)
        returnval = testObject.checkOverBudget(ruleData = ruleData, budget = 10)
        assertFalse(returnval)
    }
}
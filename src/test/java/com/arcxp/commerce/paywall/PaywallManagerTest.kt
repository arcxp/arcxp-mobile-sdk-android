package com.arcxp.commerce.paywall

import android.content.SharedPreferences
import com.arcxp.ArcXPMobileSDK
import com.arcxp.ArcXPMobileSDK.commerceConfig
import com.arcxp.commerce.ArcXPCommerceConfig
import com.arcxp.commerce.ArcXPPageviewData
import com.arcxp.commerce.ArcXPPageviewEvaluationResult
import com.arcxp.commerce.ArcXPPageviewListener
import com.arcxp.commerce.ArcXPRuleData
import com.arcxp.commerce.ArcXPRulesData
import com.arcxp.commerce.apimanagers.RetailApiManager
import com.arcxp.commerce.apimanagers.SalesApiManager
import com.arcxp.commerce.base.BaseUnitTest
import com.arcxp.commerce.callbacks.ArcXPRetailListener
import com.arcxp.commerce.callbacks.ArcXPSalesListener
import com.arcxp.commerce.di.configureTestAppComponent
import com.arcxp.commerce.models.ActivePaywallRule
import com.arcxp.commerce.models.ArcXPActivePaywallRules
import com.arcxp.commerce.models.ArcXPEntitlements
import com.arcxp.commerce.models.Edgescape
import com.arcxp.commerce.models.RuleBudget
import com.arcxp.commerce.models.RuleCondition
import com.arcxp.commerce.models.Sku
import com.arcxp.commerce.repositories.RetailRepository
import com.arcxp.commerce.repositories.SalesRepository
import com.arcxp.commerce.retrofit.RetailService
import com.arcxp.commerce.retrofit.SalesService
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.util.Constants
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.commons.util.DependencyFactory.createArcXPRulesData
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Success
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.java.KoinJavaComponent.inject
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.Calendar

class PaywallManagerTest : BaseUnitTest() {

    private lateinit var paywallRules: ArcXPActivePaywallRules
    private lateinit var entitlements: ArcXPEntitlements

    private lateinit var retailRepository: RetailRepository
    private lateinit var salesRepository: SalesRepository

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

    @RelaxedMockK
    private lateinit var error: ArcXPException

    @RelaxedMockK
    private lateinit var rulesData: ArcXPRulesData


    private lateinit var testObject: PaywallManager

    @Before
    fun setup() {
        super.setUp()
        MockKAnnotations.init(this, relaxed = true)
        startKoin { modules(configureTestAppComponent(getMockWebServerUrl())) }
        every { sharedPreferences.edit() } returns sharedPreferencesEditor
        retailRepository = RetailRepository(retailService)
        salesRepository = SalesRepository(salesService)
        mockkObject(DependencyFactory)
        every { createArcXPRulesData() } returns rulesData

        loadPaywallRules()

        loadSubscriptions()

        testObject = PaywallManager(
            retailApiManager = retailApiManager,
            salesApiManager = salesApiManager,
            sharedPreferences = sharedPreferences
        )
    }

    @After
    override fun tearDown() {
        stopKoin()
        clearAllMocks()
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
    fun `initialize set current time`() {
        mockkObject(ArcXPMobileSDK)
        every { commerceConfig() } returns config

        testObject.initialize(passedInTime = 1000L, listener = listener, loggedInState = false)

        assertEquals(testObject.getCurrentTime(), 1000L)
        assertEquals(testObject.getCurrentTimeFromDate(), 1000L)
    }

    @Test
    fun `verify paywall rules were stored in shared prefs on successful response from getActivePaywallRules`() {
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

        salesCallback.captured.onGetEntitlementsFailure(error)
        verify(exactly = 1) {
            listener.onInitializationResult(false)
        }

    }

    @Test
    fun `verify entitlement passed in during initialization`() {
        val entitlementsResponse = ArcXPEntitlements(
            skus = arrayListOf(Sku("abc")),
            edgescape = Edgescape(
                city = "",
                continent = "",
                georegion = "",
                dma = "",
                country_code = ""
            ),
        )

        mockkObject(ArcXPMobileSDK)
        every { commerceConfig() } returns config
        val response = mockk<ArcXPActivePaywallRules>()
        val captureCallback = slot<ArcXPRetailListener>()

        testObject.initialize(
            listener = listener,
            entitlementsResponse = entitlementsResponse,
            loggedInState = false
        )
        verify(exactly = 1) {
            retailApiManager.getActivePaywallRules(capture(captureCallback))
        }
        captureCallback.captured.onGetActivePaywallRulesSuccess(response)

        assertEquals(entitlementsResponse, testObject.getEntitlements())
        verify(exactly = 1) {
            testObject.loadRuleDataFromPrefs()
            listener.onInitializationResult(true)
        }
    }

    @Test
    fun `loadRuleDataFromPrefs returns empty rules`() {
        mockkObject(ArcXPMobileSDK)
        every { commerceConfig() } returns config

        every {
            sharedPreferences?.getString(Constants.PAYWALL_PREFS_RULES_DATA, null)
        } returns null

        testObject.initialize(listener = listener, loggedInState = false)

        testObject.loadRuleDataFromPrefs()

        assertEquals(testObject.getRulesData(), rulesData)
    }

    @Test
    fun `verify paywall rules are retrieved from shared prefs on failed response from getActivePaywallRules`() {
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
    fun `verify when entitlements and paywall are null from shared pref, listener returns false`() {
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
        testObject.setEntitlements(entitlements)
        val userEntitlements1 = arrayListOf<Any>(true as Any, "premium" as Any)
        var resulttype = testObject.evaluateEntitlements(userEntitlements1)
        assertFalse(resulttype)
        val userEntitlements2 = arrayListOf<Any>(true as Any, "guest" as Any)
        resulttype = testObject.evaluateEntitlements(userEntitlements2)
        assertTrue(resulttype)
        val userEntitlements3 = arrayListOf<Any>(false as Any, "premium" as Any)
        resulttype = testObject.evaluateEntitlements(userEntitlements3)
        assertTrue(resulttype)
    }

    @Test
    fun `test evaluate entitlements returns true with exception`() {
        testObject.setEntitlements(entitlements)
        testObject.setLoggedIn(true)
        val userEntitlements1 = arrayListOf("true" as Any) // no boolean for param 1, will hit exception
        assertTrue(testObject.evaluateEntitlements(userEntitlements1))
    }
    @Test
    fun `test evaluate entitlements returns false given extra skus (ignored any extras)`() {
        (entitlements.skus as ArrayList).add(Sku(sku = "sku2"))
        testObject.setEntitlements(entitlements)
        testObject.setLoggedIn(true)
        val userEntitlements1 = arrayListOf(true as Any, "premium" as Any)
        assertFalse(testObject.evaluateEntitlements(userEntitlements1))
    }

    @Test
    fun `test evaluate entitlements registered user`() {
        testObject.setLoggedIn(true)
        val userEntitlements1 = arrayListOf<Any>(true as Any)
        var resulttype = testObject.evaluateEntitlements(userEntitlements1)
        assertFalse(resulttype)
        testObject.setLoggedIn(false)
        resulttype = testObject.evaluateEntitlements(userEntitlements1)
        assertTrue(resulttype)
    }

    @Test
    fun `test evaluate conditions`() {
        val ruleCondition1 = RuleCondition(true, arrayListOf("mobile"))
        val ruleCondition2 = RuleCondition(false, arrayListOf("europe"))
        val ruleConditions = hashMapOf(
            Pair("deviceType", ruleCondition1),
            Pair("locations", ruleCondition2)
        )
        val pageCondition1 = hashMapOf(Pair<String, String>("deviceType", "mobile"))

        //Check the IN condition
        var resulttype = testObject.evaluateConditions(ruleConditions, pageCondition1)
        assertTrue(resulttype)

        //Check the OUT condition
        val pageCondition2 = hashMapOf(
            Pair("locations", "US")
        )
        resulttype = testObject.evaluateConditions(ruleConditions, pageCondition2)
        assertTrue(resulttype)

        //Check the IN false condition
        val pageCondition3 = hashMapOf(
            Pair("deviceType", "tablet")
        )
        resulttype = testObject.evaluateConditions(ruleConditions, pageCondition3)
        assertFalse(resulttype)

        //Check the OUT false condition
        val pageCondition4 = hashMapOf(
            Pair("locations", "europe")
        )
        resulttype = testObject.evaluateConditions(ruleConditions, pageCondition4)
        assertFalse(resulttype)

        //No rules
        resulttype = testObject.evaluateConditions(null, pageCondition4)
        assertFalse(resulttype)

        //No conditions
        resulttype = testObject.evaluateConditions(ruleConditions, hashMapOf())
        assertFalse(resulttype)
    }

    @Test
    fun `test evaluate geo conditions empty conditions`() {

        val geoCondition = Edgescape(city = "Denver", continent = "Europe", georegion = "region", dma = "dma", country_code = "FR")
        val ruleCondition = RuleCondition(true, arrayListOf("Denver"))

        //Check empty conditions
        var resulttype = testObject.evaluateGeoConditions(null, null)
        assertTrue(resulttype)
        resulttype = testObject.evaluateGeoConditions(hashMapOf(
            Pair("city", ruleCondition)), null)
        assertTrue(resulttype)
        resulttype = testObject.evaluateGeoConditions(hashMapOf(Pair("city", ruleCondition)), geoCondition)
        assertTrue(resulttype)

    }

    @Test
    fun `test evaluate geo conditions city pass`() {

        val ruleConditionIn = RuleCondition(true, arrayListOf("Denver"))
        val ruleConditionOut = RuleCondition(false, arrayListOf("Miami"))
        val geoCondition = Edgescape(city = "Denver", continent = "Europe", georegion = "region", dma = "dma", country_code = "FR")

        //Check IN/OUT city condition
        var resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("city", ruleConditionIn)),
            geoCondition)
        assertTrue(resulttype)
        resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("city", ruleConditionOut)),
            geoCondition)
        assertTrue(resulttype)
    }

    @Test
    fun `test evaluate geo conditions city fail`() {

        val geoCondition = Edgescape(city = "Miami", continent = "Africa", georegion = "no region", dma = "not dma", country_code = "AF")
        val geoConditionNull = Edgescape(city = null, continent = null, georegion = null, dma = null, country_code = null)
        val ruleConditionIn = RuleCondition(true, arrayListOf("Denver"))
        val ruleConditionOut = RuleCondition(false, arrayListOf("Miami"))

        //Check IN/OUT city fail condition
        var resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("city", ruleConditionOut)),
            geoCondition)
        assertFalse(resulttype)
        resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("city", ruleConditionIn)),
            geoCondition)
        assertFalse(resulttype)
        resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("city", ruleConditionIn)),
            geoConditionNull)
        assertFalse(resulttype)
    }

    @Test
    fun `test evaluate geo conditions continent pass`() {

        val geoCondition = Edgescape(city = "Denver", continent = "Europe", georegion = "region", dma = "dma", country_code = "FR")
        val ruleConditionIn = RuleCondition(true, arrayListOf("Europe"))
        val ruleConditionOut = RuleCondition(false, arrayListOf("Africa"))

        //Check IN/OUT continent condition
        var resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("continent", ruleConditionIn)),
            geoCondition)
        assertTrue(resulttype)
        resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("continent", ruleConditionOut)),
            geoCondition)
        assertTrue(resulttype)

    }

    @Test
    fun `test evaluate geo conditions continent fail`() {

        val geoCondition = Edgescape(city = "Miami", continent = "Africa", georegion = "no region", dma = "not dma", country_code = "AF")
        val geoConditionNull = Edgescape(city = null, continent = null, georegion = null, dma = null, country_code = null)
        val ruleConditionCity = RuleCondition(true, arrayListOf("Denver"))
        val ruleConditionContinentIn = RuleCondition(true, arrayListOf("Europe"))
        val ruleConditionContinentOut = RuleCondition(false, arrayListOf("Africa"))

        //Check IN/OUT continent fail condition
        var resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("continent", ruleConditionContinentOut)),
            geoCondition)
        assertFalse(resulttype)
        resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("continent", ruleConditionContinentIn)),
            geoCondition)
        assertFalse(resulttype)
        resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("continent", ruleConditionCity)),
            geoConditionNull)
        assertFalse(resulttype)
    }

    @Test
    fun `test evaluate geo conditions georegion pass`() {

        val geoCondition = Edgescape(city = "Denver", continent = "Europe", georegion = "region", dma = "dma", country_code = "FR")
        val ruleConditionIn = RuleCondition(true, arrayListOf("region"))
        val ruleConditionOut = RuleCondition(false, arrayListOf("no region"))

        //Check IN/OUT georegion condition
        var resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("georegion", ruleConditionIn)),
            geoCondition)
        assertTrue(resulttype)
        resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("georegion", ruleConditionOut)),
            geoCondition)
        assertTrue(resulttype)
    }

    @Test
    fun `test evaluate geo conditions georegion fail`() {

        val geoCondition = Edgescape(city = "Miami", continent = "Africa", georegion = "no region", dma = "not dma", country_code = "AF")
        val geoConditionNull = Edgescape(city = null, continent = null, georegion = null, dma = null, country_code = null)
        val ruleConditionCity = RuleCondition(true, arrayListOf("Denver"))
        val ruleConditionRegionIn = RuleCondition(true, arrayListOf("region"))
        val ruleConditionRegionOut = RuleCondition(false, arrayListOf("no region"))

        //Check IN/OUT georegion fail condition
        var resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("georegion", ruleConditionRegionOut)),
            geoCondition)
        assertFalse(resulttype)
        resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("georegion", ruleConditionRegionIn)),
            geoCondition)
        assertFalse(resulttype)
        resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("georegion", ruleConditionCity)),
            geoConditionNull)
        assertFalse(resulttype)
    }

    @Test
    fun `test evaluate geo conditions dma pass`() {

        val geoCondition = Edgescape(city = "Denver", continent = "Europe", georegion = "region", dma = "dma", country_code = "FR")
        val ruleConditionIn = RuleCondition(true, arrayListOf("dma"))
        val ruleConditionOut = RuleCondition(false, arrayListOf("not dma"))

        var //Check IN/OUT dma condition
                resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("dma", ruleConditionIn)),
            geoCondition)
        assertTrue(resulttype)
        resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("dma", ruleConditionOut)),
            geoCondition)
        assertTrue(resulttype)
    }

    @Test
    fun `test evaluate geo conditions dma fail`() {

        val ruleConditionNotDma = RuleCondition(true, arrayListOf("Denver"))
        val ruleConditionDmaIn = RuleCondition(true, arrayListOf("dma"))
        val ruleConditionDmaOut = RuleCondition(false, arrayListOf("not dma"))
        val geoCondition = Edgescape(city = "Miami", continent = "Africa", georegion = "no region", dma = "not dma", country_code = "AF")
        val geoConditionNull = Edgescape(city = null, continent = null, georegion = null, dma = null, country_code = null)

        //Check IN/OUT dma fail condition
        var resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("dma", ruleConditionDmaOut)),
            geoCondition)
        assertFalse(resulttype)
        resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("dma", ruleConditionDmaIn)),
            geoCondition)
        assertFalse(resulttype)
        resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("dma", ruleConditionNotDma)),
            geoConditionNull)
        assertFalse(resulttype)
    }

    @Test
    fun `test evaluate geo conditions country_code pass`() {

        val geoCondition = Edgescape(city = "Denver", continent = "Europe", georegion = "region", dma = "dma", country_code = "FR")
        val ruleConditionIn = RuleCondition(true, arrayListOf("FR"))
        val ruleConditionOut = RuleCondition(false, arrayListOf("AF"))

        //Check IN/OUT country_code condition
        var resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("country_code", ruleConditionIn)),
            geoCondition)
        assertTrue(resulttype)
        resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("country_code", ruleConditionOut)),
            geoCondition)
        assertTrue(resulttype)
    }

    @Test
    fun `test evaluate geo conditions country_code fail`() {

        val geoCondition = Edgescape(city = "Miami", continent = "Africa", georegion = "no region", dma = "not dma", country_code = "AF")
        val geoConditionNull = Edgescape(city = null, continent = null, georegion = null, dma = null, country_code = null)
        val ruleConditionCity = RuleCondition(true, arrayListOf("Denver"))
        val ruleConditionCCIn = RuleCondition(true, arrayListOf("FR"))
        val ruleConditionCCOut = RuleCondition(false, arrayListOf("AF"))

        //Check IN/OUT country_code fail condition
        var resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("country_code", ruleConditionCCOut)),
            geoCondition)
        assertFalse(resulttype)
        resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("country_code", ruleConditionCCIn)),
            geoCondition)
        assertFalse(resulttype)
        resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("country_code", ruleConditionCity)),
            geoConditionNull)
        assertFalse(resulttype)
    }

    @Test
    fun `test evaluate geo conditions fall through`() {

        val geoCondition = Edgescape(city = "Miami", continent = "Africa", georegion = "no region", dma = "not dma", country_code = "AF")
        val ruleCondition = RuleCondition(false, arrayListOf("AF"))

        var resulttype = testObject.evaluateGeoConditions(
            hashMapOf(
                Pair("condition", ruleCondition)),
            geoCondition)
        assertTrue(resulttype)
    }

    @Test
    fun `test evaluate returns show`() {
        val ruleCondition1 = RuleCondition(true, arrayListOf("mobile"))
        val pageCondition1 = hashMapOf(Pair<String, String>("deviceType", "mobile"))

        val rules = ArcXPActivePaywallRules(
            listOf(
                ActivePaywallRule(
                    id = 1,
                    conditions = hashMapOf(Pair("1", ruleCondition1)),
                    e = ArrayList(),
                    cc = "123",
                    cl = "123",
                    rt = 1,
                    budget = RuleBudget(
                        budgetType = "Rolling", calendarType = "", calendarWeekDay = "",
                        rollingType = "Days", rollingDays = 10, rollingHours = 0
                    )
                )
            )
        )

        testObject.setRules(rules)

        val pageViewData = ArcXPPageviewData("1", pageCondition1)
        val result = testObject.evaluate(pageViewData)
        assertEquals(
            result,
            ArcXPPageviewEvaluationResult(pageId = "1", show = true, campaign = null)
        )
    }

    @Test
    fun `test evaluate returns do not show`() {
        val ruleCondition1 = RuleCondition(true, arrayListOf("mobile"))
        val pageCondition1 = hashMapOf(Pair<String, String>("deviceType", "mobile"))

        val rules = ArcXPActivePaywallRules(
            listOf(
                ActivePaywallRule(
                    id = 1,
                    conditions = hashMapOf(Pair("deviceType", ruleCondition1)),
                    e = ArrayList(),
                    cc = "123",
                    cl = "123",
                    rt = 0,
                    budget = RuleBudget(
                        budgetType = "Rolling", calendarType = "", calendarWeekDay = "",
                        rollingType = "Days", rollingDays = 10, rollingHours = 0
                    )
                )
            )
        )

        testObject.setRules(rules)

        val pageViewData = ArcXPPageviewData("1", pageCondition1)
        val result = testObject.evaluate(pageViewData)
        assertEquals(
            result,
            ArcXPPageviewEvaluationResult(pageId = "1", show = false, campaign = "123")
        )
    }

    @Test
    fun `test evaluate returns show not over budget`() {
        val ruleCondition1 = RuleCondition(true, arrayListOf("mobile"))
        val pageCondition1 = hashMapOf(Pair<String, String>("deviceType", "mobile"))

        val rules = ArcXPActivePaywallRules(
            listOf(
                ActivePaywallRule(
                    id = 1,
                    conditions = hashMapOf(Pair("deviceType", ruleCondition1)),
                    e = ArrayList(),
                    cc = null,
                    cl = null,
                    rt = 1,
                    budget = RuleBudget(
                        budgetType = "Rolling", calendarType = "", calendarWeekDay = "",
                        rollingType = "Days", rollingDays = 10, rollingHours = 0
                    )
                )
            )
        )

        testObject.setRules(rules)

        val pageViewData = ArcXPPageviewData("1", pageCondition1)
        val result = testObject.evaluate(pageViewData)
        assertEquals(
            result,
            ArcXPPageviewEvaluationResult(pageId = "1", show = true, campaign = null)
        )
    }

    @Test
    fun `test evaluateResetCounters`() {
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
        var result = testObject.evaluateResetCounters(ruleData, ruleBudget1)
        assertTrue(result)

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
        result = testObject.evaluateResetCounters(ruleData, ruleBudget2)
        assertFalse(result)
    }


    @Test
    fun `test counter reset rolling days`() {
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
    fun `test counter reset calendar weekly same week`() {
        val testDate = Calendar.getInstance()
        testDate.time = SimpleDateFormat("MM/dd/yyyy").parse("07/29/2021")  //Monday
        testObject.setCurrentDate(testDate.timeInMillis)

        val storedDate = Calendar.getInstance()
        storedDate.time = SimpleDateFormat("MM/dd/yyyy").parse("07/26/2021") //Monday

        val ruleData = ArcXPRuleData(
            counter = 10,
            timestamp = storedDate.timeInMillis,  //5 days prior
            viewedPages = null, lastResetDay = 0
        )

        val ruleBudget1 = RuleBudget("Calendar", "Weekly", "Tuesday", "", 0, 0)
        var result = testObject.checkResetCounters(ruleData, ruleBudget1)

        assertTrue(result.reset)
        assertEquals(result.ruleData.counter, 0)

        testDate.time = SimpleDateFormat("MM/dd/yyyy").parse("08/01/2021")  //Sunday
        testObject.setCurrentDate(testDate.timeInMillis)

        result = testObject.checkResetCounters(ruleData, ruleBudget1)

        assertFalse(result.reset)
        assertEquals(result.ruleData.counter, 0)
    }

    @Test
    fun `test counter reset calendar weekly year is last year`() {
        val testDate = Calendar.getInstance()
        testDate.time = SimpleDateFormat("MM/dd/yyyy").parse("07/26/2021")  //Monday
        testObject.setCurrentDate(testDate.timeInMillis)

        val storedDate = Calendar.getInstance()
        storedDate.time = SimpleDateFormat("MM/dd/yyyy").parse("07/26/2022") //Monday

        val ruleData = ArcXPRuleData(
            counter = 10,
            timestamp = storedDate.timeInMillis,  //5 days prior
            viewedPages = null, lastResetDay = 0
        )

        val ruleBudget1 = RuleBudget("Calendar", "Weekly", "Sunday", "", 0, 0)
        var result = testObject.checkResetCounters(ruleData, ruleBudget1)

        assertTrue(result.reset)
        assertEquals(result.ruleData.counter, 0)

        testDate.time = SimpleDateFormat("MM/dd/yyyy").parse("08/01/2021")  //Sunday
        testObject.setCurrentDate(testDate.timeInMillis)

        result = testObject.checkResetCounters(ruleData, ruleBudget1)

        assertTrue(result.reset)
        assertEquals(result.ruleData.counter, 0)
    }

    @Test
    fun `test not viewed`() {
        val ruleData = ArcXPRuleData(0, 0, arrayListOf("12345"), 0)
        var returnval = testObject.checkNotViewed(ruleData = ruleData, pageId = "12345")
        assertFalse(returnval)
        returnval = testObject.checkNotViewed(ruleData = ruleData, pageId = "23456")
        assertTrue(returnval)
    }

    @Test
    fun `test check budget`() {
        var ruleData = ArcXPRuleData(10, 0, null, 0)
        var returnval = testObject.checkOverBudget(ruleData = ruleData, budget = 10)
        assertTrue(returnval)
        ruleData = ArcXPRuleData(9, 0, null, 0)
        returnval = testObject.checkOverBudget(ruleData = ruleData, budget = 10)
        assertFalse(returnval)
    }
}
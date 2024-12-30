//package com.arcxp.sdk
//
//import android.app.Application
//import androidx.test.espresso.IdlingRegistry
//import androidx.test.espresso.idling.CountingIdlingResource
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.platform.app.InstrumentationRegistry
//import com.arcxp.ArcXPMobileSDK
//import com.arcxp.commerce.ArcXPCommerceConfig
//import com.arcxp.commerce.ArcXPPageviewData
//import com.arcxp.commerce.ArcXPPageviewEvaluationResult
//import com.arcxp.commerce.ArcXPPageviewListener
//import com.arcxp.commerce.models.ArcXPEntitlements
//import com.arcxp.commerce.models.Sku
//import junit.framework.TestCase.assertEquals
//import junit.framework.TestCase.assertFalse
//import junit.framework.TestCase.assertTrue
//import okhttp3.mockwebserver.Dispatcher
//import okhttp3.mockwebserver.MockResponse
//import okhttp3.mockwebserver.MockWebServer
//import okhttp3.mockwebserver.RecordedRequest
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import java.io.BufferedReader
//import java.io.InputStreamReader
//
//@RunWith(AndroidJUnit4::class)
//class PaywallTests {
//
//    lateinit var mockWebServer: MockWebServer
//    lateinit var idlingResource: CountingIdlingResource
//    lateinit var mockResponse: MockResponse
//    lateinit var dispatcher: Dispatcher
//
//    val commerceUrl = "https://arcsales-arcsales-sandbox.api.cdn.arcpublishing.com"
//    val org = "arcsales"
//    val site = "arcsales"
//    val baseURL = "https://arcsales-arcsales-sandbox.web.arc-cdn.net"
//    val env = "sandbox"
//
//    @Before
//    fun setUp() {
//
//        idlingResource = CountingIdlingResource("PaywallTests")
//
//        val context =
//            (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application)
//
//        mockWebServer = MockWebServer()
//        mockResponse = MockResponse()
//            .setBody(loadJsonFromResources("paywall_active_rules.json"))
//            .setResponseCode(200)
//
//        dispatcher = object : Dispatcher() {
//            @Throws(InterruptedException::class)
//            override fun dispatch(request: RecordedRequest): MockResponse {
//                return when (request.path) {
//                    "/paywall/active" -> MockResponse().setResponseCode(200)
//                        .setBody(loadJsonFromResources("paywall_active_rules.json"))
//                    else -> {
//                        MockResponse().setResponseCode(400)
//                    }
//                }
//            }
//        }
//        mockWebServer.dispatcher = dispatcher
//        mockWebServer.start()
//
//        val baseUrl = mockWebServer.url("\\").toString()
//
//        context.apply {
//            val commerceConfig = ArcXPCommerceConfig.Builder()
//                .setContext(this)
//                .setBaseUrl(commerceUrl)
//                .setBaseSalesUrl(commerceUrl)
//                .setBaseRetailUrl(baseUrl)
//                //.setBaseRetailUrl(commerceUrl)
//                .setUserNameIsEmail(false)
//                .enableAutoCache(true)
//                .usePaywallCache(true)
//                .build()
//
//            ArcXPMobileSDK.initialize(
//                application = this,
//                site = site,
//                org = org,
//                environment = env,
//                commerceConfig = commerceConfig,
//                baseUrl = baseURL
//            )
//        }
//
//        IdlingRegistry.getInstance().register(idlingResource)
//    }
//
//    @Test
//    fun paywallEvaluateHitsBudget() {
//
//        mockWebServer.dispatcher = dispatcher
//        idlingResource.increment()
//        ArcXPMobileSDK.commerceManager().clearPaywallCache()
//
//        val pageData = ArcXPPageviewData("1",
//            hashMapOf(
//                Pair("deviceType", "mobile"),
//                Pair("contentType", "story")
//            )
//        )
//        val pageData2 = ArcXPPageviewData("2",
//            hashMapOf(
//                Pair("deviceType", "mobile"),
//                Pair("contentType", "story")
//            )
//        )
//        val pageData3 = ArcXPPageviewData("3",
//            hashMapOf(
//                Pair("deviceType", "mobile"),
//                Pair("contentType", "story")
//            )
//        )
//        val pageData4 = ArcXPPageviewData("4",
//            hashMapOf(
//                Pair("deviceType", "mobile"),
//                Pair("contentType", "story")
//            )
//        )
//        callPaywallEvaluatePage(pageData, entitlements,
//            object : ArcXPPageviewListener() {
//                override fun onEvaluationResult(result: ArcXPPageviewEvaluationResult) {
//                    assertTrue(result.show)
//
//                    callPaywallEvaluatePage(pageData2, entitlements,
//                        object : ArcXPPageviewListener() {
//                            override fun onEvaluationResult(result: ArcXPPageviewEvaluationResult) {
//                                assertTrue(result.show)
//
//                                callPaywallEvaluatePage(pageData3, entitlements,
//                                    object : ArcXPPageviewListener() {
//                                        override fun onEvaluationResult(result: ArcXPPageviewEvaluationResult) {
//                                            assertTrue(result.show)
//
//                                            callPaywallEvaluatePage(pageData4, entitlements,
//                                                object : ArcXPPageviewListener() {
//                                                    override fun onEvaluationResult(result: ArcXPPageviewEvaluationResult) {
//                                                        assertFalse(result.show)
//                                                        assertEquals(result.pageId, "4")
//                                                        assertEquals(result.campaign, "premium")
//                                                        assertEquals(result.ruleId, "888")
//                                                        idlingResource.decrement()
//                                                    }
//                                                }
//                                            )
//                                        }
//                                    }
//                                )
//                            }
//                        }
//                    )
//                }
//            }
//        )
//    }
//
//    @Test
//    fun paywallEvaluateReturnsShowLoggedIn() {
//
//        mockWebServer.dispatcher = dispatcher
//
//        ArcXPMobileSDK.commerceManager().clearPaywallCache()
//
//        ArcXPMobileSDK.commerceManager().login("abcdedf", "token", "token")
//
//       idlingResource.increment()
//        val entitlements = ArcXPEntitlements(listOf(Sku("123456")), null)
//        val pageData = ArcXPPageviewData("1",
//            hashMapOf(
//                Pair<String, String>("deviceType", "mobile"),
//                Pair("contentType", "story")
//            )
//        )
//        val pageData2 = ArcXPPageviewData("2",
//            hashMapOf(
//                Pair<String, String>("deviceType", "mobile"),
//                Pair("contentType", "story")
//            )
//        )
//        val pageData3 = ArcXPPageviewData("3",
//            hashMapOf(
//                Pair<String, String>("deviceType", "mobile"),
//                Pair("contentType", "story")
//            )
//        )
//        val pageData4 = ArcXPPageviewData("4",
//            hashMapOf(
//                Pair<String, String>("deviceType", "mobile"),
//                Pair("contentType", "story")
//            )
//        )
//
//        callPaywallEvaluatePage(pageData, entitlements,
//            object : ArcXPPageviewListener() {
//                override fun onEvaluationResult(result: ArcXPPageviewEvaluationResult) {
//                    assertTrue(result.show)
//
//                    callPaywallEvaluatePage(pageData2, entitlements,
//                        object : ArcXPPageviewListener() {
//                            override fun onEvaluationResult(result: ArcXPPageviewEvaluationResult) {
//                                assertTrue(result.show)
//
//                                callPaywallEvaluatePage(pageData3, entitlements,
//                                    object : ArcXPPageviewListener() {
//                                        override fun onEvaluationResult(result: ArcXPPageviewEvaluationResult) {
//                                            assertTrue(result.show)
//
//                                            callPaywallEvaluatePage(pageData4, entitlements,
//                                                object : ArcXPPageviewListener() {
//                                                    override fun onEvaluationResult(result: ArcXPPageviewEvaluationResult) {
//                                                        assertTrue(result.show)
//                                                        idlingResource.decrement()
//                                                    }
//                                                }
//                                            )
//                                        }
//                                    }
//                                )
//                            }
//                        }
//                    )
//                }
//            }
//        )
//    }
//
//    fun callPaywallEvaluatePage(pageData: ArcXPPageviewData, entitlements: ArcXPEntitlements?, listener: ArcXPPageviewListener) {
//        ArcXPMobileSDK.commerceManager().evaluatePage(
//            pageData,
//            entitlements,
//            null,
//            listener
//        )
//    }
//
//    @After
//    fun tearDown() {
//        if (::idlingResource.isInitialized) {
//            IdlingRegistry.getInstance().unregister(idlingResource)
//        }
//        mockWebServer.shutdown()
//        ArcXPMobileSDK.commerceManager().clearPaywallCache()
//        ArcXPMobileSDK.reset()
//    }
//
//    fun loadJsonFromResources(fileName: String): String {
//        val context = InstrumentationRegistry.getInstrumentation().targetContext
//        val inputStream = context.assets.open(fileName)
//        val reader = BufferedReader(InputStreamReader(inputStream))
//        val content = StringBuilder()
//        var line: String? = reader.readLine()
//        while (line != null) {
//            content.append(line)
//            line = reader.readLine()
//        }
//        reader.close()
//        return content.toString()
//    }
//}
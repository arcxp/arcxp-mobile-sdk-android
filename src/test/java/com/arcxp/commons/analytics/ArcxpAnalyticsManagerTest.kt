package com.arcxp.commons.analytics

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.arcxp.commons.models.EventType
import com.arcxp.commons.models.SdkName
import com.arcxp.commons.util.AnalyticsUtil
import com.arcxp.commons.util.BuildVersionProviderImpl
import com.arcxp.commons.util.Constants
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.commons.util.DependencyFactory.createBuildVersionProvider
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.*

class ArcxpAnalyticsManagerTest {

    @RelaxedMockK
    lateinit var application: Application

    @RelaxedMockK
    lateinit var shared: SharedPreferences

    @RelaxedMockK
    lateinit var sharedEditor: SharedPreferences.Editor

    @RelaxedMockK
    lateinit var buildVersionProvider: BuildVersionProviderImpl

    @RelaxedMockK
    lateinit var analyticsUtil: AnalyticsUtil

    @RelaxedMockK
    lateinit var calendar: Calendar

    private lateinit var testObject: ArcXPAnalyticsManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        mockkObject(DependencyFactory)

        every {
            application.getSharedPreferences(
                Constants.ANALYTICS,
                Context.MODE_PRIVATE
            )
        } returns shared
        every { shared.edit() } returns sharedEditor
        every { createBuildVersionProvider() } returns buildVersionProvider
        every { analyticsUtil.getCurrentLocale() } returns "US-US"
        every { analyticsUtil.deviceConnection() } returns "ONLINE"
        every { analyticsUtil.screenOrientation() } returns "portrait"
        every { buildVersionProvider.model() } returns "model"
        every { buildVersionProvider.manufacturer() } returns "manufacturer"
        every { buildVersionProvider.sdkInt() } returns 132
        every { calendar.time.time } returns 12345678

    }

    @After
    fun tearDown() {
        unmockkObject(DependencyFactory)
    }

    @Test
    fun `Id is null and sharedPref does not contain deviceID shared Pref`() {

        mockkStatic(UUID::class)

        every { shared.contains("deviceID") } returns false
        every { shared.getString("deviceID", null) } returns null
        every { UUID.randomUUID().toString() } returns "123-456-789"

        ArcXPAnalyticsManager(
            application = application,
            organization = "arctesting1",
            site = "config",
            environment = "sandbox",
            sdk_name = SdkName.VIDEO,
            buildVersionProvider = buildVersionProvider,
            analyticsUtil = analyticsUtil
        )

        verify(exactly = 1) {
            sharedEditor.putString(Constants.DEVICE_ID, "123-456-789")
        }
    }

    @Test
    fun `Id is not null and sharedPref does not contain deviceID shared Pref`() {

        every { shared.contains("deviceID") } returns true
        every { shared.getString("deviceID", null) } returns "123-456-789"

        val testObject2 = ArcXPAnalyticsManager(
            application = application,
            organization = "arctesting1",
            site = "config",
            environment = "sandbox",
            sdk_name = SdkName.VIDEO,
            buildVersionProvider = buildVersionProvider,
            analyticsUtil = analyticsUtil
        )

        assertEquals("123-456-789", testObject2.getDeviceId())
    }

//    @Test
//    fun `SDK has been installed`() {
//
//        val sdk_name= SdkName.VIDEO
//
//        every { shared.getBoolean(sdk_name.value, false) } returns true
//        every { shared.contains("deviceID") } returns true
//        every { shared.getString("deviceID", null) } returns "123-456-789"
//
//        val testObject2 = ArcXPAnalyticsManager(
//            application = application,
//            organization = "arctesting1",
//            site = "config",
//            environment = "sandbox",
//            sdk_name = SdkName.VIDEO,
//            buildVersionProvider = buildVersionProvider,
//            analyticsUtil = analyticsUtil
//        )
//
//        verify(exactly = 1) { testObject2.log(EventType.PING)}
//    }
// TODO so having issues with this test, unsure what exactly is going wrong but can't do verify on
//  something that isn't a mock, was passing individually but broke all tests passing
//  we can revisit when analytics code is uncommented as the log is a function that does a bunch
//  of stuff that needs to be tested as well

    @Test
    fun `Create json when pending analytics is null`() {

        testObject = ArcXPAnalyticsManager(
            application = application,
            organization = "arctesting1",
            site = "config",
            environment = "sandbox",
            sdk_name = SdkName.VIDEO,
            analyticsUtil = analyticsUtil,
            buildVersionProvider = buildVersionProvider
        )

        every { shared.getString(Constants.PENDING_ANALYTICS, null) } returns null

        assertEquals("json", testObject.createJson("json"))
    }


    @Test
    fun `Create json when pending analytics is not null`() {
        every { shared.getString(Constants.PENDING_ANALYTICS, null)?.isNotEmpty() } returns true
        every { shared.getString(Constants.PENDING_ANALYTICS, null) } returns "abc"

        testObject = ArcXPAnalyticsManager(
            application = application,
            organization = "arctesting1",
            site = "config",
            environment = "sandbox",
            sdk_name = SdkName.VIDEO,
            analyticsUtil = analyticsUtil,
            buildVersionProvider = buildVersionProvider
        )

        assertEquals("abcdef", testObject.createJson("def"))
    }

    @Test
    fun `Test buildAnalytics`() {

        every { shared.getString("deviceID", null) } returns "abc"

        val testObject2 = ArcXPAnalyticsManager(
            application = application,
            organization = "arctesting1",
            site = "config",
            environment = "sandbox",
            sdk_name = SdkName.VIDEO,
            buildVersionProvider = buildVersionProvider,
            analyticsUtil = analyticsUtil
        )
        val analytics = testObject2.buildAnalytics(EventType.PING)

//        assertNotNull(analytics)

        assertEquals(EventType.PING.value, analytics.event.event)
        assertEquals("abc", analytics.event.deviceUUID)
        assertEquals("arcxp-mobile-dev", analytics.source)
        assertEquals("arcxp-mobile", analytics.sourcetype)
        assertEquals("arcxp-mobile", analytics.index)

//        val event = ArcxpEventFields("abc","abc","abc","abc","abc","abc",
//            "abc","abc","abc","abc","abc","abc")

//        val test = ArcxpAnalytics(123456, "abc", "abc", "abc", event)
//
//        assertEquals(123456, test.timestamp)
//        assertEquals("abc", test.event.deviceUUID)
//        assertEquals("abc", test.event.org)
//        assertEquals("abc", test.event.site)
//        assertEquals("abc", test.event.environment)
//        assertEquals("abc", test.event.locale)
//        assertEquals("abc", test.event.platform)
//        assertEquals("abc", test.event.platformVersion)
//        assertEquals("abc", test.event.deviceModel)
//        assertEquals("abc", test.event.connectivityState)
//        assertEquals("abc", test.event.orientation)
//        assertEquals("abc", test.event.sdkName)
    }

//    @Test
//    fun `default parameters`() {
//        every { shared.getString("deviceID", null) } returns "abc"
//
//        val testObject2 = ArcXPAnalyticsManager(
//            application,
//            "arctesting1",
//            "config",
//            "sandbox",
//            SdkName.VIDEO
//        )
//
//        assertNotNull(testObject2)
//    }

//    @Test
//    fun `other tests`() {
//        val deviceOrientation = DeviceOrientation.PORTRAIT
//        assertEquals(DeviceOrientation.PORTRAIT.value, deviceOrientation.value)
//        assertEquals(DeviceOrientation.PORTRAIT.text, deviceOrientation.text)
//        assertEquals(DeviceOrientation.PORTRAIT, DeviceOrientation.from(1))
//        assertEquals(DeviceOrientation.LANDSCAPE, DeviceOrientation.from(2))
//
//        val connectivity = ConnectivityState.OFFLINE
//        assertEquals(ConnectivityState.OFFLINE.value, connectivity.value)
//    }

}
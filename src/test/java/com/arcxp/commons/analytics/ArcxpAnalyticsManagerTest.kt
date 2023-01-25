package com.arcxp.commons.analytics

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.arcxp.commons.models.*
import com.arcxp.commons.util.*
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.Assert.*

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

    private lateinit var testObject: ArcXPAnalyticsManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        mockkObject(ArcXPAnalytics)

        every { application.getSharedPreferences("analytics", Context.MODE_PRIVATE) } returns shared
        every { shared.edit() } returns sharedEditor
        every { ArcXPAnalytics.createAnalyticsUtil(application) } returns analyticsUtil
        every { ArcXPAnalytics.createBuildVersionProvider() } returns buildVersionProvider
        every { analyticsUtil.getCurrentLocale() } returns "US-US"
        every { analyticsUtil.deviceConnection() } returns "ONLINE"
        every { analyticsUtil.screenOrientation() } returns "portrait"
        every { buildVersionProvider.model() } returns "model"
        every { buildVersionProvider.manufacturer() } returns "manufacturer"
        every { buildVersionProvider.sdkInt() } returns "sdk"

        testObject = ArcXPAnalytics.createArcXPAnalyticsManager(
            application,
            "arctesting1",
            "config",
            "sandbox",
            SdkName.VIDEO
        )
    }

    @Test
    fun `Id is null and sharedPref does not contain deviceID shared Pref`() {

        mockkStatic(UUID::class)

        every { shared.contains("deviceID") } returns false
        every { shared.getString("deviceID", null) } returns null
        every { UUID.randomUUID().toString() } returns "123-456-789"

        ArcXPAnalytics.createArcXPAnalyticsManager(
            application,
            "arctesting1",
            "config",
            "sandbox",
            SdkName.VIDEO
        )

        verify(exactly = 1) {
            sharedEditor.putString(Constants.DEVICE_ID, "123-456-789")
        }
    }

    @Test
    fun `Id is not null and sharedPref does not contain deviceID shared Pref`() {

        every { shared.contains("deviceID") } returns true
        every { shared.getString("deviceID", null) } returns "123-456-789"

        val testObject2 = ArcXPAnalytics.createArcXPAnalyticsManager(
            application,
            "arctesting1",
            "config",
            "sandbox",
            SdkName.VIDEO
        )

        assertEquals("123-456-789", testObject2.getDeviceId())
    }

    @Test
    fun `SDK has been installed`() {

        val sdk_name= SdkName.VIDEO

        every { shared.getBoolean(sdk_name.value, false) } returns true
        every { shared.contains("deviceID") } returns true
        every { shared.getString("deviceID", null) } returns "123-456-789"

        val testObject2 = ArcXPAnalytics.createArcXPAnalyticsManager(
            application,
            "arctesting1",
            "config",
            "sandbox",
            SdkName.VIDEO
        )

        verify(exactly = 1) { testObject2.log(EventType.PING)}
    }

    @Test
    fun `Create json when pending analytics is null`() {

        every { shared.getString(Constants.PENDING_ANALYTICS, null) } returns null

        assertEquals("json", testObject.createJson("json"))
    }


    @Test
    fun `Create json when pending analytics is not null`() {
        every { shared.getString(Constants.PENDING_ANALYTICS, null)?.isNotEmpty() } returns true
        every { shared.getString(Constants.PENDING_ANALYTICS, null) } returns "abc"

        assertEquals("abcdef", testObject.createJson("def"))
    }

    @Test
    fun `Test buildAnalytics`() {

        every { shared.getString("deviceID", null) } returns "abc"

        val testObject2 = ArcXPAnalytics.createArcXPAnalyticsManager(
            application,
            "arctesting1",
            "config",
            "sandbox",
            SdkName.VIDEO
        )
        val analytics = testObject2.buildAnalytics(EventType.PING)

        assertNotNull(analytics)

        assertEquals(EventType.PING.value, analytics.event.event)
        assertEquals("abc", analytics.event.deviceUUID)
        assertEquals("arcxp-mobile-dev", analytics.source)
        assertEquals("arcxp-mobile", analytics.sourcetype)
        assertEquals("arcxp-mobile", analytics.index)

        val event = ArcxpEventFields("abc","abc","abc","abc","abc","abc",
            "abc","abc","abc","abc","abc","abc")

        val test = ArcxpAnalytics(123456, "abc", "abc", "abc", event)

        assertEquals(123456, test.timestamp)
        assertEquals("abc", test.event.deviceUUID)
        assertEquals("abc", test.event.org)
        assertEquals("abc", test.event.site)
        assertEquals("abc", test.event.environment)
        assertEquals("abc", test.event.locale)
        assertEquals("abc", test.event.platform)
        assertEquals("abc", test.event.platformVersion)
        assertEquals("abc", test.event.deviceModel)
        assertEquals("abc", test.event.connectivityState)
        assertEquals("abc", test.event.orientation)
        assertEquals("abc", test.event.sdkName)
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

    @Test
    fun `other tests`() {
        val deviceOrientation = DeviceOrientation.PORTRAIT
        assertEquals(DeviceOrientation.PORTRAIT.value, deviceOrientation.value)
        assertEquals(DeviceOrientation.PORTRAIT.text, deviceOrientation.text)
        assertEquals(DeviceOrientation.PORTRAIT, DeviceOrientation.from(1))
        assertEquals(DeviceOrientation.LANDSCAPE, DeviceOrientation.from(2))

        val connectivity = ConnectivityState.OFFLINE
        assertEquals(ConnectivityState.OFFLINE.value, connectivity.value)
    }

}
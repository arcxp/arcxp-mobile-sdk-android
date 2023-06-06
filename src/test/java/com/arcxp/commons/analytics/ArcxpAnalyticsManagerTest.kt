package com.arcxp.commons.analytics

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.arcxp.commons.models.ArcxpAnalytics
import com.arcxp.commons.models.ArcxpEventFields
import com.arcxp.commons.models.EventType
import com.arcxp.commons.models.SdkName
import com.arcxp.commons.retrofit.AnalyticsController
import com.arcxp.commons.service.AnalyticsService
import com.arcxp.commons.testutils.TestUtils.getJson
import com.arcxp.commons.util.*
import com.arcxp.commons.util.Constants.LAST_PING_TIME
import com.arcxp.commons.util.Constants.PENDING_ANALYTICS
import com.arcxp.commons.util.DependencyFactory.createIOScope
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class ArcxpAnalyticsManagerTest {

    @get:Rule
    var instantExecuteRule = InstantTaskExecutorRule()

    @RelaxedMockK
    private lateinit var application: Application

    @RelaxedMockK
    private lateinit var shared: SharedPreferences

    @RelaxedMockK
    private lateinit var sharedEditor: SharedPreferences.Editor

    @RelaxedMockK
    private lateinit var buildVersionProvider: BuildVersionProviderImpl

    @RelaxedMockK
    private lateinit var analyticsUtil: AnalyticsUtil

    @RelaxedMockK
    private lateinit var calendar: Calendar

    private val packageName = "package"

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockkObject(DependencyFactory)
        every { createIOScope() } returns CoroutineScope(context = Dispatchers.Unconfined + SupervisorJob())
        every {
            application.getSharedPreferences(
                Constants.ANALYTICS,
                Context.MODE_PRIVATE
            )
        } returns shared
        every {
            application.applicationContext.packageName
        } returns packageName
        every { shared.edit() } returns sharedEditor
        every { analyticsUtil.getCurrentLocale() } returns "US-US"
        every { analyticsUtil.deviceConnectionState() } returns "ONLINE"
        every { analyticsUtil.screenOrientation() } returns "portrait"
        every { buildVersionProvider.model() } returns "model"
        every { buildVersionProvider.manufacturer() } returns "manufacturer"
        every { buildVersionProvider.sdkInt() } returns 132
        every { buildVersionProvider.debug() } returns true
        every { calendar.time.time } returns 12345678
//        mockkObject(AnalyticsController)
//        every { AnalyticsController.getAnalyticsService(application = application) } returns analyticsService
        mockkObject(Utils)
        every { Utils.currentTimeInMillis()} returns 12345678
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `deviceId is created and added when prefs does not contain deviceID`() = runTest {
        mockkStatic(UUID::class)
        every { shared.getString("deviceID", null) } returns null
        every { UUID.randomUUID().toString() } returns "123-456-789"

        ArcXPAnalyticsManager(
            application = application,
            organization = "arctesting1",
            site = "config",
            environment = "sandbox",
            sdk_name = SdkName.VIDEO,
            sdk_version = "1234",
            buildVersionProvider = buildVersionProvider,
            analyticsUtil = analyticsUtil
        )

        verify(exactly = 1) {
            sharedEditor.putString(Constants.DEVICE_ID, "123-456-789")
        }
    }

    @Test
    fun `deviceId is used from prefs when sharedPref contains deviceID`() = runTest {

        every { shared.contains("deviceID") } returns true
        every { shared.getString("deviceID", null) } returns "123-456-7890"

        val testObject = ArcXPAnalyticsManager(
            application = application,
            organization = "arctesting1",
            site = "config",
            environment = "sandbox",
            sdk_name = SdkName.VIDEO,
            sdk_version = "1234",
            buildVersionProvider = buildVersionProvider,
            analyticsUtil = analyticsUtil
        )

        assertEquals("123-456-7890", testObject.getDeviceId())
    }

    @Test
    fun `Test checkLastPing time is not today`() = runTest {
        every { shared.getLong(LAST_PING_TIME, 0) } returns 0

        val testObject = ArcXPAnalyticsManager(
            application = application,
            organization = "arctesting1",
            site = "config",
            environment = "sandbox",
            sdk_name = SdkName.VIDEO,
            sdk_version = "1234",
            buildVersionProvider = buildVersionProvider,
            analyticsUtil = analyticsUtil
        )

        assertTrue(testObject.checkLastPing())
    }

    @Test
    fun `Test checkLastPing time is today`() = runTest {
        val currentTime = Calendar.getInstance().timeInMillis
        every { shared.getLong(LAST_PING_TIME, 0) } returns currentTime + 1000

        val testObject = ArcXPAnalyticsManager(
            application = application,
            organization = "arctesting1",
            site = "config",
            environment = "sandbox",
            sdk_name = SdkName.VIDEO,
            sdk_version = "1234",
            buildVersionProvider = buildVersionProvider,
            analyticsUtil = analyticsUtil
        )

        assertFalse(testObject.checkLastPing())
    }

    @Test
    fun `Test buildAnalytics`() = runTest {
        every { shared.getString("deviceID", null) } returns "abc"

        val testObject = ArcXPAnalyticsManager(
            application = application,
            organization = "arctesting1",
            site = "config",
            environment = "sandbox",
            sdk_name = SdkName.VIDEO,
            sdk_version = "1234",
            buildVersionProvider = buildVersionProvider,
            analyticsUtil = analyticsUtil
        )
        val analytics = testObject.buildAnalytics(EventType.PING)

        assertEquals(EventType.PING.value, analytics.event.event)
        assertEquals("abc", analytics.event.deviceUUID)
        assertEquals("sdk", analytics.source)
        assertEquals("debug", analytics.sourcetype)
        assertEquals("arcxp-mobile", analytics.index)

        val event = ArcxpEventFields(
            event = "abc",
            deviceUUID = "abc",
            sdkName = "abc",
            sdkVersion = "abc",
            org = "abc",
            site = "abc",
            environment = "abc",
            locale = "abc",
            platform = "abc",
            platformVersion = "abc",
            deviceModel = "abc",
            connectivityState = "abc",
            connectivityType = "abc",
            orientation = "abc",
            packageName = packageName
        )

        val test = ArcxpAnalytics(
            event = event,
            time = 123456,
            source = "abc",
            sourcetype = "abc",
            index = "abc"
        )

        assertEquals(123456, test.time)
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
        assertEquals("abc", test.event.sdkVersion)
        assertEquals(packageName, test.event.packageName)
    }

    @Test
    fun `Test buildAnalytics prod`() = runTest {
        every { shared.getString("deviceID", null) } returns "abc"
        every { buildVersionProvider.debug() } returns false
        val testObject = ArcXPAnalyticsManager(
            application = application,
            organization = "arctesting1",
            site = "config",
            environment = "sandbox",
            sdk_name = SdkName.VIDEO,
            sdk_version = "abc",
            buildVersionProvider = buildVersionProvider,
            analyticsUtil = analyticsUtil
        )
        val analytics = testObject.buildAnalytics(EventType.PING)

        assertEquals(EventType.PING.value, analytics.event.event)
        assertEquals("abc", analytics.event.deviceUUID)
        assertEquals("sdk", analytics.source)
        assertEquals("release", analytics.sourcetype)
        assertEquals("arcxp-mobile", analytics.index)

        val event = ArcxpEventFields(
            event = "abc",
            deviceUUID = "abc",
            sdkName = "abc",
            sdkVersion = "abc",
            org = "abc",
            site = "abc",
            environment = "abc",
            locale = "abc",
            platform = "abc",
            platformVersion = "abc",
            deviceModel = "abc",
            connectivityState = "abc",
            connectivityType = "abc",
            orientation = "abc",
            packageName = packageName
        )

        val test = ArcxpAnalytics(
            event = event,
            time = 123456,
            source = "abc",
            sourcetype = "abc",
            index = "abc"
        )

        assertEquals(123456, test.time)
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
        assertEquals(packageName, test.event.packageName)
    }

    @Test
    fun `Test buildFullAnalytics when offline analytics is not empty`() = runTest {
        val eventJson = getJson("analytics.json")
        every { shared.getString(PENDING_ANALYTICS, null) } returns eventJson
        every { shared.getString("deviceID", null) } returns "abc"

        val testObject = ArcXPAnalyticsManager(
            application = application,
            organization = "arctesting1",
            site = "config",
            environment = "sandbox",
            sdk_name = SdkName.VIDEO,
            sdk_version = "abc",
            buildVersionProvider = buildVersionProvider,
            analyticsUtil = analyticsUtil
        )
        val analytics = testObject.buildAnalytics(EventType.PING)
        val analyticsList = testObject.buildFullAnalytics(analytics)

        assertEquals(1, analyticsList.size)
    }
    @Test
    fun `Test buildFullAnalytics when offline analytics is empty`() = runTest {
        every { shared.getString(PENDING_ANALYTICS, null) } returns null
        every { shared.getString("deviceID", null) } returns "abc"

        val testObject = ArcXPAnalyticsManager(
            application = application,
            organization = "arctesting1",
            site = "config",
            environment = "sandbox",
            sdk_name = SdkName.VIDEO,
            sdk_version = "abc",
            buildVersionProvider = buildVersionProvider,
            analyticsUtil = analyticsUtil
        )
        val analytics = testObject.buildAnalytics(EventType.PING)
        val analyticsList = testObject.buildFullAnalytics(analytics)

        assertEquals(1, analyticsList.size)
    }
    @Test
    fun `Test buildFullAnalytics when exception with message`() = runTest {
        val message = "message"
        val exception = mockk<Exception>()
        every { exception.message } returns message
        every { shared.getString(PENDING_ANALYTICS, null) } throws exception
        mockkStatic(Log::class)


        val testObject = ArcXPAnalyticsManager(
            application = application,
            organization = "arctesting1",
            site = "config",
            environment = "sandbox",
            sdk_name = SdkName.VIDEO,
            sdk_version = "abc",
            buildVersionProvider = buildVersionProvider,
            analyticsUtil = analyticsUtil
        )
        clearAllMocks(answers = false)
        val analytics = testObject.buildAnalytics(EventType.PING)
        val analyticsList = testObject.buildFullAnalytics(analytics)

        assertEquals(1, analyticsList.size)
        verify(exactly = 1) {
            Log.e(any(), message)
        }
    }

    @Test
    fun `Test buildFullAnalytics when exception with no message`() = runTest {
        val exception = mockk<Exception>()
        every { exception.message } returns null
        every { shared.getString(PENDING_ANALYTICS, null) } throws exception
        mockkStatic(Log::class)


        val testObject = ArcXPAnalyticsManager(
            application = application,
            organization = "arctesting1",
            site = "config",
            environment = "sandbox",
            sdk_name = SdkName.VIDEO,
            sdk_version = "abc",
            buildVersionProvider = buildVersionProvider,
            analyticsUtil = analyticsUtil
        )
        clearAllMocks(answers = false)
        val analytics = testObject.buildAnalytics(EventType.PING)
        val analyticsList = testObject.buildFullAnalytics(analytics)

        assertEquals(1, analyticsList.size)
        verify(exactly = 1) {
            Log.e(any(), "unknown error")
        }
    }

//    @Test
//    fun `log install with failed call writes to pending`() = runTest {
//        mockkStatic(UUID::class)
//        every { shared.getString("deviceID", null) } returns null
//        every { UUID.randomUUID().toString() } returns "1111"
//        mockkObject(ConnectionUtil)
//        every { ConnectionUtil.isInternetAvailable(any()) } returns true
//        val eventJson = toJson(listOf(createTestEvent(), createTestEvent(), createTestEvent()))
//        every { shared.getBoolean(sdkName.value, false) } returns false
//        every { shared.getString(PENDING_ANALYTICS, null) } returns eventJson
//        every { sharedEditor.remove(any()) } returns sharedEditor
//        every { sharedEditor.putBoolean(any(), any()) } returns sharedEditor
//        coEvery {
//            analyticsService.postAnalytics(any())
//        } returns Response.error(401, "error".toResponseBody())
//
//        ArcXPAnalyticsManager(
//            application = application,
//            organization = "arctesting1",
//            site = "config",
//            environment = "sandbox",
//            sdk_name = SdkName.VIDEO,
//            sdk_version = "1234",
//            buildVersionProvider = buildVersionProvider,
//            analyticsUtil = analyticsUtil
//        )
//
//        val json = slot<String>()
//        coVerifySequence {
//            shared.edit()
//            shared.getString(Constants.DEVICE_ID, null)
//            sharedEditor.putString(Constants.DEVICE_ID, "1111")
//            shared.getBoolean(SdkName.VIDEO.value, false)
//            shared.getString(PENDING_ANALYTICS, null)
//            sharedEditor.remove(PENDING_ANALYTICS)
//            sharedEditor.apply()
//            sharedEditor.putString(PENDING_ANALYTICS, capture(json))
//            sharedEditor.putBoolean(SdkName.VIDEO.value, true)
//            sharedEditor.apply()
//        }
//        val capture = fromJsonList(json.captured, ArcxpAnalytics::class.java)!!
//        assertEquals(4, capture.size)
//
//    }
//
//    @Test
//    fun `log install with successful service call`() = runTest {
//        mockkStatic(UUID::class)
//        every { shared.getString("deviceID", null) } returns null
//        every { UUID.randomUUID().toString() } returns "1111"
//        mockkObject(ConnectionUtil)
//        every { ConnectionUtil.isInternetAvailable(any()) } returns true
//        val eventJson = toJson(listOf(createTestEvent(), createTestEvent(), createTestEvent()))
//        every { shared.getBoolean(sdkName.value, false) } returns false
//        every { shared.getString(PENDING_ANALYTICS, null) } returns eventJson
//        every { sharedEditor.remove(any()) } returns sharedEditor
//        every { sharedEditor.putBoolean(any(), any()) } returns sharedEditor
//        coEvery {
//            analyticsService.postAnalytics(any())
//        } returns Response.success(null)
//
//        ArcXPAnalyticsManager(
//            application = application,
//            organization = "arctesting1",
//            site = "config",
//            environment = "sandbox",
//            sdk_name = SdkName.VIDEO,
//            sdk_version = "abc",
//            buildVersionProvider = buildVersionProvider,
//            analyticsUtil = analyticsUtil
//        )
//
//        coVerifySequence {
//            shared.edit()
//            shared.getString(Constants.DEVICE_ID, null)
//            sharedEditor.putString(Constants.DEVICE_ID, "1111")
//            shared.getBoolean(SdkName.VIDEO.value, false)
//            shared.getString(PENDING_ANALYTICS, null)
//            sharedEditor.remove(PENDING_ANALYTICS)
//            sharedEditor.apply()
//            sharedEditor.putBoolean(SdkName.VIDEO.value, true)
//            sharedEditor.apply()
//        }
//    }
//
//    @Test
//    fun `log ping with checkPing true with successful service call`() = runTest {
//        mockkStatic(UUID::class)
//        every { shared.getString("deviceID", null) } returns null
//        every { UUID.randomUUID().toString() } returns "1111"
//        mockkObject(ConnectionUtil)
//        every { ConnectionUtil.isInternetAvailable(any()) } returns true
//        val eventJson = toJson(listOf(createTestEvent(), createTestEvent(), createTestEvent()))
//        every { shared.getBoolean(SdkName.VIDEO.value, false) } returns true
//        every { shared.getString(PENDING_ANALYTICS, null) } returns eventJson
//        every { sharedEditor.remove(any()) } returns sharedEditor
//        every { sharedEditor.putBoolean(any(), any()) } returns sharedEditor
//        coEvery {
//            analyticsService.postAnalytics(any())
//        } returns Response.success(null)
//
//        ArcXPAnalyticsManager(
//            application = application,
//            organization = "arctesting1",
//            site = "config",
//            environment = "sandbox",
//            sdk_name = SdkName.VIDEO,
//            sdk_version = "abc",
//            buildVersionProvider = buildVersionProvider,
//            analyticsUtil = analyticsUtil
//        )
//
//        coVerifySequence {
//            shared.edit()
//            shared.getString(Constants.DEVICE_ID, null)
//            sharedEditor.putString(Constants.DEVICE_ID, "1111")
//            shared.getBoolean(SdkName.VIDEO.value, false)
//            shared.getLong(LAST_PING_TIME, 0)
//            sharedEditor.putLong(LAST_PING_TIME, any())
//            shared.getString(PENDING_ANALYTICS, null)
//            sharedEditor.remove(PENDING_ANALYTICS)
//            sharedEditor.apply()
//        }
//    }
//
//    @Test
//    fun `log ping with checklastping false writes to pending`() = runTest {
//        mockkStatic(UUID::class)
//        every { shared.getString("deviceID", null) } returns null
//        every { UUID.randomUUID().toString() } returns "123-456-7891233"
//        mockkObject(ConnectionUtil)
//        every { ConnectionUtil.isInternetAvailable(any()) } returns false
//        val eventJson = toJson(listOf(createTestEvent(), createTestEvent(), createTestEvent()))
//        every { shared.getBoolean(SdkName.VIDEO.value, false) } returns true
//        every { shared.getString(PENDING_ANALYTICS, null) } returns eventJson
//        every { sharedEditor.remove(any()) } returns sharedEditor
//        every { sharedEditor.putBoolean(any(), any()) } returns sharedEditor
//        coEvery {
//            analyticsService.postAnalytics(any())
//        } returns Response.error(401, "error".toResponseBody())
//        every { shared.getLong(LAST_PING_TIME, 0) } returns 87000000
//
//        ArcXPAnalyticsManager(
//            application = application,
//            organization = "arctesting1",
//            site = "config",
//            environment = "sandbox",
//            sdk_name = SdkName.VIDEO,
//            sdk_version = "abc",
//            buildVersionProvider = buildVersionProvider,
//            analyticsUtil = analyticsUtil
//        )
//
//        val json = slot<String>()
//        coVerifySequence {
//            shared.edit()
//            shared.getString(Constants.DEVICE_ID, null)
//            sharedEditor.putString(Constants.DEVICE_ID, "123-456-7891233")
//            shared.getBoolean(SdkName.VIDEO.value, false)
//            shared.getLong(LAST_PING_TIME, 0)
//            sharedEditor.putLong(LAST_PING_TIME, any())
//            shared.getString(PENDING_ANALYTICS, null)
//            sharedEditor.remove(PENDING_ANALYTICS)
//            sharedEditor.apply()
//            sharedEditor.putString(PENDING_ANALYTICS, capture(json))
//        }
//        val capture = fromJsonList(json.captured, ArcxpAnalytics::class.java)!!
//        assertEquals(4, capture.size)
//
//    }
//
//    @Test
//    fun `log install while offline writes to pending`() = runTest {
//        mockkStatic(UUID::class)
//        every { shared.getString("deviceID", null) } returns null
//        every { UUID.randomUUID().toString() } returns "123-456-7891233"
//        mockkObject(ConnectionUtil)
//        every { ConnectionUtil.isInternetAvailable(any()) } returns false
//        val eventJson = toJson(listOf(createTestEvent(), createTestEvent(), createTestEvent()))
//        every { shared.getBoolean(sdkName.value, false) } returns false
//        every { shared.getString(PENDING_ANALYTICS, null) } returns eventJson
//        every { sharedEditor.remove(any()) } returns sharedEditor
//        every { sharedEditor.putBoolean(any(), any()) } returns sharedEditor
//        coEvery {
//            analyticsService.postAnalytics(any())
//        } returns Response.error(401, "error".toResponseBody())
//
//        ArcXPAnalyticsManager(
//            application = application,
//            organization = "arctesting1",
//            site = "config",
//            environment = "sandbox",
//            sdk_name = SdkName.VIDEO,
//            sdk_version = "abc",
//            buildVersionProvider = buildVersionProvider,
//            analyticsUtil = analyticsUtil
//        )
//
//        val json = slot<String>()
//
//        coVerifySequence {
//            shared.edit()
//            shared.getString(Constants.DEVICE_ID, null)
//            sharedEditor.putString(Constants.DEVICE_ID, "123-456-7891233")
//            shared.getBoolean(SdkName.VIDEO.value, false)
//            shared.getString(PENDING_ANALYTICS, null)
//            sharedEditor.remove(PENDING_ANALYTICS)
//            sharedEditor.apply()
//            sharedEditor.putString(PENDING_ANALYTICS, capture(json))
//            sharedEditor.putBoolean(SdkName.VIDEO.value, true)
//            sharedEditor.apply()
//        }
//
//        val capture = fromJsonList(json.captured, ArcxpAnalytics::class.java)!!
//        assertEquals(4, capture.size)
//
//    }
//
//    @Test
//    fun `log install exception to pending`() = runTest {
//        mockkStatic(UUID::class)
//        every { shared.getString("deviceID", null) } returns null
//        every { UUID.randomUUID().toString() } returns "123-456-78912"
//        mockkObject(ConnectionUtil)
//        every { ConnectionUtil.isInternetAvailable(any()) } throws Exception()
//        val eventJson = toJson(listOf(createTestEvent(), createTestEvent(), createTestEvent()))
//        every { shared.getBoolean(sdkName.value, false) } returns false
//        every { shared.getString(PENDING_ANALYTICS, null) } returns eventJson
//        every { sharedEditor.remove(any()) } returns sharedEditor
//        every { sharedEditor.putBoolean(any(), any()) } returns sharedEditor
//        coEvery {
//            analyticsService.postAnalytics(any())
//        } returns Response.error(401, "error".toResponseBody())
//
//        ArcXPAnalyticsManager(
//            application = application,
//            organization = "arctesting1",
//            site = "config",
//            environment = "sandbox",
//            sdk_name = SdkName.VIDEO,
//            sdk_version = "abc",
//            buildVersionProvider = buildVersionProvider,
//            analyticsUtil = analyticsUtil
//        )
//
//        coVerifySequence {
//            shared.edit()
//            shared.getString(Constants.DEVICE_ID, null)
//            sharedEditor.putString(Constants.DEVICE_ID, "123-456-78912")
//            shared.getBoolean(SdkName.VIDEO.value, false)
//            shared.getString(PENDING_ANALYTICS, null)
//            sharedEditor.remove(PENDING_ANALYTICS)
//            sharedEditor.apply()
//            sharedEditor.putBoolean(SdkName.VIDEO.value, true)
//            sharedEditor.apply()
//        }
//    }

    private fun createTestEvent() =
        ArcxpAnalytics(
            event = ArcxpEventFields(
                event = "event",
                deviceUUID = "id",
                sdkName = "name",
                sdkVersion = "abc",
                org = "organization",
                site = "site",
                environment = "environment",
                locale = "en_US",
                platform = "android",
                platformVersion = "version",
                deviceModel = "deviceModel",
                connectivityState = "connection",
                connectivityType = "type",
                orientation = "orientation",
                packageName = packageName
            ),
            time = Calendar.getInstance().time.time,
            source = "arcxp-mobile-dev",
            sourcetype = "arcxp-mobile",
            index = "arcxp-mobile"
        )
}
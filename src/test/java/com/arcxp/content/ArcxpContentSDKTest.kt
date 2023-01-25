package com.arcxp.content.sdk

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.arcxp.commons.analytics.ArcXPAnalyticsManager
import com.arcxp.commons.models.SdkName
import com.arcxp.commons.util.AnalyticsUtil
import com.arcxp.commons.util.ArcXPAnalytics
import com.arcxp.content.sdk.apimanagers.ContentApiManager
import com.arcxp.content.sdk.models.ArcXPContentException
import com.arcxp.content.sdk.util.BuildVersionProvider
import com.arcxp.content.sdk.util.BuildVersionProviderImpl
import com.arcxp.content.sdk.util.DependencyFactory
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test


class ArcxpContentSDKTest {
    @RelaxedMockK
    lateinit var application: Application

    @RelaxedMockK
    lateinit var arcxpContentManager: ArcXPContentManager

    @RelaxedMockK
    lateinit var arcXPLogger: ArcXPLogger

    @RelaxedMockK
    lateinit var arcXPResizer: ArcXPResizer

    @RelaxedMockK
    lateinit var buildVersionProvider: BuildVersionProvider

    @RelaxedMockK
    lateinit var contentApiManager: ContentApiManager

    @RelaxedMockK
    lateinit var context: Context

    @RelaxedMockK
    lateinit var analyticsBuildVersionProvider: com.arcxp.commons.util.BuildVersionProviderImpl

    @RelaxedMockK
    lateinit var analyticsUtil: AnalyticsUtil

    @RelaxedMockK
    lateinit var shared: SharedPreferences

    @RelaxedMockK
    lateinit var sharedEditor: SharedPreferences.Editor

    private lateinit var analyticsObject: ArcXPAnalyticsManager


    @Before
    fun setUp() {
        unmockkObject(ArcXPContentSDK)
        MockKAnnotations.init(this)
        mockkObject(DependencyFactory)
        every { DependencyFactory.createArcXPContentManager(application = application) } returns arcxpContentManager
        every {
            DependencyFactory.createArcXPLogger(
                application = application,
                organization = "org",
                environment = "env",
                site = "site"
            )
        } returns arcXPLogger
        every { DependencyFactory.createContentRepository(application = application) } returns mockk(
            relaxed = true
        )
        every { DependencyFactory.createArcXPResizer(application, "baseUrl") } returns arcXPResizer
        every { DependencyFactory.createContentApiManager() } returns contentApiManager

        every { application.getString(R.string.resizer_key) } returns "123"

        mockkObject(ArcXPAnalytics)
        every { application.getSharedPreferences("analytics", Context.MODE_PRIVATE) } returns shared
        every { shared.edit() } returns sharedEditor
        every { ArcXPAnalytics.createAnalyticsUtil(application) } returns analyticsUtil
        every { ArcXPAnalytics.createBuildVersionProvider() } returns analyticsBuildVersionProvider
        every { analyticsUtil.getCurrentLocale() } returns "US-US"
        every { analyticsUtil.deviceConnection() } returns "ONLINE"
        every { analyticsUtil.screenOrientation() } returns "portrait"
        every { analyticsBuildVersionProvider.model() } returns "model"
        every { analyticsBuildVersionProvider.manufacturer() } returns "manufacturer"
        every { analyticsBuildVersionProvider.sdkInt() } returns "sdk"

        analyticsObject = ArcXPAnalytics.createArcXPAnalyticsManager(
            application,
            "arctesting1",
            "config",
            "sandbox",
            SdkName.VIDEO
        )
    }

    @After
    fun tearDown() {
        ArcXPContentSDK.reset()
    }

    @Test
    fun `contentManager() throws exception if not initialized`() {
        assertThrows(
            "Failed Initialization: SDK uninitialized, please run initialize method first",
            ArcXPContentException::class.java
        ) {
            ArcXPContentSDK.contentManager()
        }
    }

    @Test
    fun `isInitialized() returns false if not initialized`() {
        val actual = ArcXPContentSDK.isInitialized()
        assertFalse(actual)
    }

    @Test
    fun `isInitialized() returns true if initialized`() {
        ArcXPContentSDK.initialize(application = application, config = arcxpContentConfig)
        val actual = ArcXPContentSDK.isInitialized()
        assertTrue(actual)
    }

    @Test
    fun `contentManager() returns content manager if initialized first`() {
        ArcXPContentSDK.initialize(application = application, config = arcxpContentConfig)
        assertEquals(arcxpContentManager, ArcXPContentSDK.contentManager())
    }


    @Test(expected = ArcXPContentException::class)
    fun `initialize throws exception if already initialized`() {
        ArcXPContentSDK.initialize(application, config = arcxpContentConfig)
        ArcXPContentSDK.initialize(application, config = arcxpContentConfig)

    }

    @Test
    fun `initialize sets arcxpContentConfig`() {

        ArcXPContentSDK.initialize(application, config = arcxpContentConfig)

        val actual = ArcXPContentSDK.arcxpContentConfig()

        assertEquals(arcxpContentConfig, actual)
    }

    @Test
    fun `arcxpContentConfig() throws exception when uninitialized`() {
        assertThrows(
            "Failed Retrieving Config: SDK uninitialized, please run initialize method first",
            ArcXPContentException::class.java
        ) {
            ArcXPContentSDK.arcxpContentConfig()
        }
    }

    @Test
    fun `logger() throws exception when uninitialized`() {
        assertThrows(
            "Failed Initialization: SDK uninitialized, please run initialize method first",
            ArcXPContentException::class.java
        ) {
            ArcXPContentSDK.logger()
        }
    }

    @Test
    fun `logger() returns logger if initialized first`() {
        ArcXPContentSDK.initialize(application = application, config = arcxpContentConfig)

        assertEquals(arcXPLogger, ArcXPContentSDK.logger())
    }

    @Test
    fun `analytics() throws exception when uninitialized`() {
        assertThrows(
            "Failed Initialization: SDK uninitialized, please run initialize method first",
            ArcXPContentException::class.java
        ) {
            ArcXPContentSDK.analytics()
        }
    }

//    @Test
//    fun `analytics() returns analytics if initialized first`() {
//        val expected = mockk<ArcXPAnalyticsManager2>()
//        every {
//            DependencyFactory.createArcXPAnalyticsManager(
//                application = application,
//                "org",
//                "site",
//                "env"
//            )
//        } returns expected
//
//        ArcXPContentSDK.initialize(application = application, config = arcxpContentConfig)
//
//        assertEquals(expected, ArcXPContentSDK.analytics())
//
//    }

    @Test
    fun `getVersion return value`() {
        every { context.getString(com.arcxp.content.sdk.R.string.content_sdk_version) } returns "123"

        assertEquals(ArcXPContentSDK.getVersion(context), "123")
    }

    @Test
    fun `initialize sets resizer`() {
        val expected = mockk<ArcXPResizer>()
        every {
            DependencyFactory.createArcXPResizer(application = application, baseUrl = "baseUrl")
        } returns expected

        ArcXPContentSDK.initialize(application, config = arcxpContentConfig)

        val actual = ArcXPContentSDK.resizer()

        assertEquals(expected, actual)
    }

    @Test
    fun `resizer() throws exception when uninitialized`() {
        assertThrows(
            "Failed Initialization: SDK uninitialized, please run initialize method first",
            ArcXPContentException::class.java
        ) {
            ArcXPContentSDK.resizer()
        }
    }

    private val arcxpContentConfig =
        ArcXPContentConfig
            .Builder()
//            .setUrlComponents(org = "org", site = "site", env = "env")
            .setSite("site")
            .setOrgName("org")
            .setEnvironment("env")
            .setNavigationEndpoint("endpoint")
            .setBaseUrl("baseUrl")
            .setCacheSize(100)
            .setCacheTimeUntilUpdate(5)
            .build()
}
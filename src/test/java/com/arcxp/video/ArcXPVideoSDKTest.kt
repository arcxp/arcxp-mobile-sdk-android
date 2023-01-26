package com.arcxp.video

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.arcxp.video.util.DependencyProvider
import com.arcxp.sdk.R
import com.arcxp.commons.analytics.ArcXPAnalyticsManager
import com.arcxp.commons.models.SdkName
import com.arcxp.commons.util.AnalyticsUtil
import com.arcxp.commons.util.ArcXPAnalytics
import com.arcxp.commons.util.BuildVersionProviderImpl
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ArcXPVideoSDKTest {

    @RelaxedMockK
    lateinit var context: Context

    @RelaxedMockK
    lateinit var mediaClient: ArcMediaClient

    @RelaxedMockK
    lateinit var application: Application

    @RelaxedMockK
    lateinit var arcXPResizer: ArcXPResizer

    @RelaxedMockK
    lateinit var buildVersionProvider: BuildVersionProviderImpl

    @RelaxedMockK
    lateinit var analyticsUtil: AnalyticsUtil

    @RelaxedMockK
    lateinit var shared: SharedPreferences

    @RelaxedMockK
    lateinit var sharedEditor: SharedPreferences.Editor

    private lateinit var analyticsObject: ArcXPAnalyticsManager


    @Before
    fun setUp() {
        unmockkObject(ArcXPVideoSDK)
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockkObject(ArcMediaClient)
        mockkObject(DependencyProvider)
        every { DependencyProvider.createArcXPResizer(application = application, baseUrl = "baseUrl") } returns arcXPResizer
        every { DependencyProvider.createMediaClient("org", "env")} returns mediaClient
        every { application.getString(R.string.resizer_key) } returns "123"

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

        analyticsObject = ArcXPAnalytics.createArcXPAnalyticsManager(
            application,
            "arctesting1",
            "config",
            "sandbox",
            SdkName.VIDEO
        )

        every { ArcXPAnalytics.createArcXPAnalyticsManager(application,
            "arctesting1",
            "config",
            "sandbox",
            SdkName.VIDEO)} returns analyticsObject
    }

    @After
    fun tearDown() {
        ArcXPVideoSDK.reset()
    }

    @Test
    fun `mediaClient() throws exception if not initialized`() {
        Assert.assertThrows(
            "Failed Initialization: SDK uninitialized, please run initialize method first",
            ArcException::class.java
        ) {
            ArcXPVideoSDK.mediaClient()
        }
    }

    @Test
    fun `mediaClient() returns media client if initialized first`() {
        ArcXPVideoSDK.initialize(application = application, baseUrl = "baseUrl", org = "org", env = "env", site = "site")
        Assert.assertEquals(mediaClient, ArcXPVideoSDK.mediaClient())
    }

    @Test
    fun `resizer() returns resizer if initialized first`() {
        ArcXPVideoSDK.initialize(application = application, baseUrl = "baseUrl", org = "org", env = "env", site = "site")
        Assert.assertEquals(arcXPResizer, ArcXPVideoSDK.resizer())
    }


    @Test(expected = ArcException::class)
    fun `initialize throws exception if already initialized`() {
        ArcXPVideoSDK.initialize(application = application, baseUrl = "baseUrl", org = "org", env = "env", site = "site")
        ArcXPVideoSDK.initialize(application = application, baseUrl = "baseUrl", org = "org", env = "env", site = "site")

    }

    @Test
    fun `initialize sets resizer`() {

        val expected = mockk<ArcXPResizer>()
        every {
            DependencyProvider.createArcXPResizer(application = application, baseUrl = "baseUrl")
        } returns expected

        ArcXPVideoSDK.initialize(application, baseUrl = "baseUrl", org= "org", env = "env", site = "site")

        val actual = ArcXPVideoSDK.resizer()

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `resizer() throws exception when uninitialized`() {
        Assert.assertThrows(
            "Failed Initialization: SDK uninitialized, please run initialize method first",
            ArcException::class.java
        ) {
            ArcXPVideoSDK.resizer()
        }
    }

    @Test
    fun `getVersion return value`() {
        every { context.getString(R.string.sdk_version) } returns "123"

        Assert.assertEquals(ArcXPVideoSDK.getVersion(context), "123")
    }


}
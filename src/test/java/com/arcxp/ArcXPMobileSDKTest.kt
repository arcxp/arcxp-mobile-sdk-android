package com.arcxp

import android.app.Application
import android.content.Context
import com.arcxp.commerce.ArcXPCommerceConfig
import com.arcxp.commerce.ArcXPCommerceManager
import com.arcxp.commons.analytics.ArcXPAnalyticsManager
import com.arcxp.commons.models.SdkName
import com.arcxp.commons.throwables.ArcXPError
import com.arcxp.commons.util.ArcXPLogger
import com.arcxp.commons.util.ArcXPResizer
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.commons.util.DependencyFactory.createArcXPAnalyticsManager
import com.arcxp.commons.util.DependencyFactory.createArcXPCommerceManager
import com.arcxp.commons.util.DependencyFactory.createArcXPContentManager
import com.arcxp.commons.util.DependencyFactory.createArcXPLogger
import com.arcxp.commons.util.DependencyFactory.createArcXPResizer
import com.arcxp.commons.util.DependencyFactory.createMediaClient
import com.arcxp.content.ArcXPContentConfig
import com.arcxp.content.ArcXPContentManager
import com.arcxp.sdk.R
import com.arcxp.video.ArcMediaClient
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ArcXPMobileSDKTest {

    @RelaxedMockK
    lateinit var application: Application

    @RelaxedMockK
    lateinit var mediaClient: ArcMediaClient

    @RelaxedMockK
    lateinit var resizer: ArcXPResizer

    @RelaxedMockK
    lateinit var logger: ArcXPLogger

    @RelaxedMockK
    lateinit var arcXPAnalyticsManager: ArcXPAnalyticsManager

    @RelaxedMockK
    lateinit var contentConfig: ArcXPContentConfig

    @RelaxedMockK
    lateinit var contentManager: ArcXPContentManager

    @RelaxedMockK
    lateinit var commerceConfig: ArcXPCommerceConfig

    @RelaxedMockK
    lateinit var commerceManager: ArcXPCommerceManager

    private val testSite = "site"
    private val testOrg = "org"
    private val testBaseurl = "baseurl"
    private val testEnv = "env"
    
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(DependencyFactory)
        every {
            createArcXPLogger(
                application = application,
                organization = testOrg,
                environment = testEnv,
                site = testSite
            )
        } returns logger
        every {
            createArcXPAnalyticsManager(
                application = application,
                organization = testOrg,
                site = testSite,
                environment = testEnv,
                sdk_name = SdkName.SINGLE,
                sdk_version = "abc"
            )
        } returns arcXPAnalyticsManager

        every {
            createArcXPResizer(
                application = application,
                baseUrl = testBaseurl
            )
        } returns resizer
        every { createMediaClient(orgName = testOrg, env = testEnv) } returns mediaClient
        every { createArcXPContentManager(application = application) } returns contentManager
        every {
            createArcXPCommerceManager(
                application = application,
                config = commerceConfig,
                clientCachedData = any()
            )
        } returns commerceManager

        every { application.getString(R.string.sdk_version) } returns "abc"
    }

    @After
    fun tearDown() {
        unmockkObject(DependencyFactory)
        ArcXPMobileSDK.reset()
    }

    @Test
    fun `initialize with blank url throws exception with expected message`() {
        val actual = assertFailsWith(
            exceptionClass = ArcXPError::class,
            block = {
                ArcXPMobileSDK.initialize(
                    application = application,
                    site = "",
                    org = "",
                    environment = "",
                    baseUrl = ""
                )
            }
        )

        assertEquals(ArcXPMobileSDK.initErrorBaseUrl, actual.message)
    }

    @Test
    fun `initialize with blank site throws exception with expected message`() {
        val actual = assertFailsWith(
            exceptionClass = ArcXPError::class,
            block = {
                ArcXPMobileSDK.initialize(
                    application = application,
                    site = "",
                    org = testOrg,
                    environment = "",
                    baseUrl = testBaseurl
                )
            }
        )

        assertEquals(ArcXPMobileSDK.initErrorOrgSite, actual.message)
    }

    @Test
    fun `initialize with blank org throws exception with expected message`() {
        val actual = assertFailsWith(
            exceptionClass = ArcXPError::class,
            block = {
                ArcXPMobileSDK.initialize(
                    application = application,
                    site = testSite,
                    org = "",
                    environment = "",
                    baseUrl = testBaseurl
                )
            }
        )

        assertEquals(ArcXPMobileSDK.initErrorOrgSite, actual.message)
    }

    @Test
    fun `initialize sets member variables`() {

        ArcXPMobileSDK.initialize(
            application = application,
            site = testSite,
            org = testOrg,
            environment = testEnv,
            baseUrl = testBaseurl
        )

        assertEquals(application, ArcXPMobileSDK.application())
        assertTrue(ArcXPMobileSDK.initialized)
        assertEquals(testOrg, ArcXPMobileSDK.organization)
        assertEquals(testSite, ArcXPMobileSDK.site)
        assertEquals(testEnv, ArcXPMobileSDK.environment)
        assertEquals(testBaseurl, ArcXPMobileSDK.baseUrl)
        assertEquals(logger, ArcXPMobileSDK.logger())
        assertEquals(resizer, ArcXPMobileSDK.resizer())
        assertEquals(arcXPAnalyticsManager, ArcXPMobileSDK.analytics())
        assertEquals(mediaClient, ArcXPMobileSDK.mediaClient())
        assertFalse(ArcXPMobileSDK.contentInitialized())
        assertFalse(ArcXPMobileSDK.commerceInitialized())
    }

    @Test
    fun `initialize content when given config`() {
        ArcXPMobileSDK.initialize(
            application = application,
            site = testSite,
            org = testOrg,
            environment = testEnv,
            baseUrl = testBaseurl,
            contentConfig = contentConfig
        )

        assertTrue(ArcXPMobileSDK.contentInitialized())
        assertEquals(contentConfig, ArcXPMobileSDK.contentConfig())
        assertEquals(contentManager, ArcXPMobileSDK.contentManager())
    }

    @Test
    fun `initialize commerce when given config`() {
        ArcXPMobileSDK.initialize(
            application = application,
            site = testSite,
            org = testOrg,
            environment = testEnv,
            baseUrl = testBaseurl,
            commerceConfig = commerceConfig
        )

        assertTrue(ArcXPMobileSDK.commerceInitialized())
        assertEquals(commerceConfig, ArcXPMobileSDK.commerceConfig())
        assertEquals(commerceManager, ArcXPMobileSDK.commerceManager())
    }

    @Test
    fun `initialize commerce when given client map passes to commerce manager`() {
        val map = mapOf("client" to "data")

        ArcXPMobileSDK.initialize(
            application = application,
            site = testSite,
            org = testOrg,
            environment = testEnv,
            baseUrl = testBaseurl,
            commerceConfig = commerceConfig,
            clientCachedData = map
        )

        verify(exactly = 1) {
            createArcXPCommerceManager(
                application = application,
                config = commerceConfig,
                clientCachedData = map
            )
        }
    }

    @Test
    fun `getVersion returns version string`() {
        val context = mockk<Context>()
        val version = "version"
        every { context.getString(R.string.sdk_version)} returns version

        assertEquals(version, ArcXPMobileSDK.getVersion(context = context))
    }

    @Test
    fun `resizer() returns failure when uninitialized`() {
        val actual = assertFailsWith(
            exceptionClass = ArcXPError::class,
            block = {
                ArcXPMobileSDK.resizer()
            }
        )

        assertFalse(ArcXPMobileSDK.initialized)
        assertEquals(ArcXPMobileSDK.initError, actual.message)
    }

    @Test
    fun `mediaClient() returns failure when uninitialized`() {
        val actual = assertFailsWith(
            exceptionClass = ArcXPError::class,
            block = {
                ArcXPMobileSDK.mediaClient()
            }
        )

        assertFalse(ArcXPMobileSDK.initialized)
        assertEquals(ArcXPMobileSDK.initError, actual.message)
    }

    @Test
    fun `logger() returns failure when uninitialized`() {
        val actual = assertFailsWith(
            exceptionClass = ArcXPError::class,
            block = {
                ArcXPMobileSDK.logger()
            }
        )

        assertFalse(ArcXPMobileSDK.initialized)
        assertEquals(ArcXPMobileSDK.initError, actual.message)
    }

    @Test
    fun `analytics() returns failure when uninitialized`() {
        val actual = assertFailsWith(
            exceptionClass = ArcXPError::class,
            block = {
                ArcXPMobileSDK.analytics()
            }
        )

        assertFalse(ArcXPMobileSDK.initialized)
        assertEquals(ArcXPMobileSDK.initError, actual.message)
    }

    @Test
    fun `contentManager() returns failure when uninitialized`() {
        val actual = assertFailsWith(
            exceptionClass = ArcXPError::class,
            block = {
                ArcXPMobileSDK.contentManager()
            }
        )

        assertFalse(ArcXPMobileSDK.initialized)
        assertEquals(ArcXPMobileSDK.initErrorContent, actual.message)
    }

    @Test
    fun `contentConfig() returns failure when uninitialized`() {
        val actual = assertFailsWith(
            exceptionClass = ArcXPError::class,
            block = {
                ArcXPMobileSDK.contentConfig()
            }
        )

        assertFalse(ArcXPMobileSDK.initialized)
        assertEquals(ArcXPMobileSDK.initErrorContent, actual.message)
    }

    @Test
    fun `commerceManager() returns failure when uninitialized`() {
        val actual = assertFailsWith(
            exceptionClass = ArcXPError::class,
            block = {
                ArcXPMobileSDK.commerceManager()
            }
        )

        assertEquals(ArcXPMobileSDK.initErrorCommerce, actual.message)
    }

    @Test
    fun `commerceConfig() returns failure when uninitialized`() {
        val actual = assertFailsWith(
            exceptionClass = ArcXPError::class,
            block = {
                ArcXPMobileSDK.commerceConfig()
            }
        )

        assertFalse(ArcXPMobileSDK.initialized)
        assertEquals(ArcXPMobileSDK.initErrorCommerce, actual.message)
    }

    @Test
    fun `application() returns failure when uninitialized`() {
        val actual = assertFailsWith(
            exceptionClass = ArcXPError::class,
            block = {
                ArcXPMobileSDK.application()
            }
        )

        assertFalse(ArcXPMobileSDK.initialized)
        assertEquals(ArcXPMobileSDK.initError, actual.message)
    }
}
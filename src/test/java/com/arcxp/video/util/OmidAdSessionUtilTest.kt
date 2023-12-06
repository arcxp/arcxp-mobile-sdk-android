package com.arcxp.video.util

import android.content.Context
import com.arcxp.commons.util.Constants
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.model.AdVerification
import com.arcxp.video.model.JavascriptResource
import com.iab.omid.library.washpost.Omid
import com.iab.omid.library.washpost.adsession.*
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.net.URL

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class OmidAdSessionUtilTest {

    @RelaxedMockK lateinit var context: Context
    @RelaxedMockK lateinit var config: ArcXPVideoConfig
    @RelaxedMockK lateinit var adSessionConfiguration: AdSessionConfiguration
    @RelaxedMockK lateinit var adSessionContext: AdSessionContext
    @RelaxedMockK lateinit var partner: Partner
    @RelaxedMockK lateinit var vParameters: VerificationScriptResource
    @RelaxedMockK lateinit var adSession: AdSession

    private val vastUrl: List<AdVerification> = listOf(AdVerification(listOf(JavascriptResource("omid", "http://www.javascriptresource.url")), "vendor", "params"))
    private val vastUrl2: List<AdVerification> = listOf(AdVerification(listOf(JavascriptResource("omid2", "http://www.javascriptresource.url")), "vendor", "params"))
    private val omidPartnerName = "omidPartnerName"
    private val omidVersionName = "omidVersionName"

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockkStatic(Omid::class)
        every { Omid.activate(context.applicationContext) } just Runs

        mockkStatic(AdSessionConfiguration::class)
        every {
            AdSessionConfiguration.createAdSessionConfiguration(
                CreativeType.VIDEO,
                ImpressionType.VIEWABLE,
                Owner.NATIVE,
                Owner.NATIVE,
                false
            )
        } returns adSessionConfiguration
        mockkStatic(Partner::class)
        every { Partner.createPartner(omidPartnerName, omidVersionName) } returns partner

        every { config.omidPartnerName } returns omidPartnerName
        every { config.omidVersionName } returns omidVersionName

        mockkStatic(VerificationScriptResource::class)

        every {
            VerificationScriptResource.createVerificationScriptResourceWithParameters(
                "vendor",
                URL("http://www.javascriptresource.url"),
                "params"
            )
        } returns vParameters
        mockkStatic(AdSessionContext::class)
        every {
            AdSessionContext.createNativeAdSessionContext(
                partner,
                Constants.OMIDJS,
                any(),
                null,
                null
            )
        } returns adSessionContext
        mockkStatic(AdSession::class)
        every { AdSession.createAdSession(any(), any()) } returns adSession

    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getNativeAdSession returns expected adSession`() {
        val actualAdSessionConfiguration = slot<AdSessionConfiguration>()
        val actualAdSessionContext = slot<AdSessionContext>()
        val actualVerificationScripts = slot<List<VerificationScriptResource>>()

        val result = OmidAdSessionUtil.getNativeAdSession(context, config, vastUrl)

        verify {
            AdSession.createAdSession(
                capture(actualAdSessionConfiguration),
                capture(actualAdSessionContext)
            )
        }
        verify {
            AdSessionContext.createNativeAdSessionContext(
                partner,
                Constants.OMIDJS,
                capture(actualVerificationScripts),
                null,
                null
            )
        }
        assertEquals(adSessionConfiguration, actualAdSessionConfiguration.captured)
        assertEquals(adSessionContext, actualAdSessionContext.captured)
        assertEquals(listOf(vParameters), actualVerificationScripts.captured)
        assertEquals(adSession, result)
    }

    @Test
    fun `getNativeAdSession returns expected adSession not omid`() {
        val actualAdSessionConfiguration = slot<AdSessionConfiguration>()
        val actualAdSessionContext = slot<AdSessionContext>()
        val actualVerificationScripts = slot<List<VerificationScriptResource>>()

        val result = OmidAdSessionUtil.getNativeAdSession(context, config, vastUrl2)

        verify {
            AdSession.createAdSession(
                capture(actualAdSessionConfiguration),
                capture(actualAdSessionContext)
            )
        }
        verify {
            AdSessionContext.createNativeAdSessionContext(
                partner,
                Constants.OMIDJS,
                capture(actualVerificationScripts),
                null,
                null
            )
        }
        assertEquals(adSessionConfiguration, actualAdSessionConfiguration.captured)
        assertEquals(adSessionContext, actualAdSessionContext.captured)
        assertEquals(adSession, result)
    }
}
package com.arcxp.commerce

import android.app.Application
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commons.throwables.ArcXPError
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.sdk.R
import io.mockk.MockKAnnotations
import io.mockk.every
import org.junit.Assert.assertThrows
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkObject
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArcXPCommerceConfigTest {

    @RelaxedMockK
    lateinit var context: Application

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkObject(DependencyFactory)

        mockkObject(ArcXPMobileSDK)
        every { ArcXPMobileSDK.application() } returns context
    }

    @Test
    fun `test build config`() {

        val config =
            ArcXPCommerceConfig.Builder()
                .setContext(context = context)
                .useLocalConfig(true)
                .setPwMinLength(1)
                .setPwSpecialCharacters(1)
                .setPwUppercase(1)
                .setPwLowercase(1)
                .setUserNameIsEmail(true)
                .setPwPwNumbers(1)
                .enableGoogleOneTap(true)
                .enableGoogleOneTapAutoLogin(true)
                .usePaywallCache(true)
                .build()

        assertTrue(config.useLocalConfig)
        assertTrue(config.userNameIsEmail)
        assertTrue(config.googleOneTapEnabled)
        assertTrue(config.googleOneTapAutoLoginEnabled)
        assertTrue(config.useCachedPaywall)
        assertEquals(1, config.pwMinLength)
        assertEquals(1, config.pwSpecialCharacters)
        assertEquals(1, config.pwUppercase)
        assertEquals(1, config.pwLowercase)
        assertEquals(1, config.pwPwNumbers)
    }

    @Test
    fun `test null context`() {
        try {
            ArcXPCommerceConfig.Builder()
                .build()
        } catch (e: ArcXPException) {
            assertEquals(context.getString(R.string.commerce_builder_missing_context), e.message)
            assertEquals(ArcXPSDKErrorType.CONFIG_ERROR, e.type)
        }
    }
}
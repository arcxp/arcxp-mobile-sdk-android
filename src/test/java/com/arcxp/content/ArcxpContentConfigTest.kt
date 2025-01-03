package com.arcxp.content

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commons.util.Constants
import com.arcxp.sdk.R
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ArcxpContentConfigTest {

    @get:Rule
    var rule = InstantTaskExecutorRule()

    private val preLoading = true
    private val navFailureMsg =
        "Failed Initialization: SDK Needs navigationEndpoint value for site service"

    @RelaxedMockK
    private lateinit var application: Application

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockkObject(ArcXPMobileSDK)
        every { ArcXPMobileSDK.application() } returns application
        every { application.getString(R.string.init_failure_navigation_endpoint) } returns navFailureMsg
    }

    @Test
    fun `build on success with individual setters`() {

        val expectedMaxCacheSize = 1023
        val expectedTimeUntilUpdate = 24687387

        val actual =
            ArcXPContentConfig
                .Builder()
                .setCacheSize(sizeInMB = expectedMaxCacheSize)
                .setPreloading(preLoading = preLoading)
                .setCacheTimeUntilUpdate(minutes = expectedTimeUntilUpdate)
                .build()
        assertEquals(expectedMaxCacheSize, actual.cacheSizeMB)
        assertEquals(preLoading, actual.preLoading)
        assertEquals(expectedTimeUntilUpdate, actual.cacheTimeUntilUpdateMinutes)
    }

    @Test
    fun `build on success with cache size below minimum uses minimum`() {

        val expectedMaxCacheSize = -876423
        val expectedTimeUntilUpdate = 24687387

        val actual =
            ArcXPContentConfig
                .Builder()
                .setCacheSize(sizeInMB = expectedMaxCacheSize)
                .setPreloading(preLoading = preLoading)
                .setCacheTimeUntilUpdate(minutes = expectedTimeUntilUpdate)
                .build()
        assertEquals(Constants.VALID_CACHE_SIZE_RANGE_MB.first, actual.cacheSizeMB)
        assertEquals(preLoading, actual.preLoading)
        assertEquals(expectedTimeUntilUpdate, actual.cacheTimeUntilUpdateMinutes)
    }

    @Test
    fun `build on success with cache size above maximum uses maximum`() {

        val expectedMaxCacheSize = 1025
        val expectedTimeUntilUpdate = 24687387

        val actual =
            ArcXPContentConfig
                .Builder()
                .setCacheSize(sizeInMB = expectedMaxCacheSize)
                .setPreloading(preLoading = preLoading)
                .setCacheTimeUntilUpdate(minutes = expectedTimeUntilUpdate)
                .build()
        assertEquals(Constants.VALID_CACHE_SIZE_RANGE_MB.last, actual.cacheSizeMB)
        assertEquals(preLoading, actual.preLoading)
        assertEquals(expectedTimeUntilUpdate, actual.cacheTimeUntilUpdateMinutes)
    }

    @Test
    fun `build when time to update is below min uses min`() {
        val testObject = ArcXPContentConfig
            .Builder()
            .setCacheTimeUntilUpdate(0)
            .build()


        assertEquals(Constants.CACHE_TIME_UNTIL_UPDATE_MIN, testObject.cacheTimeUntilUpdateMinutes)
    }
}
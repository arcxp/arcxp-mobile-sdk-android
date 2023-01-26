package com.arcxp.content

import com.arcxp.content.models.ArcXPContentException
import com.arcxp.content.util.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ArcxpContentConfigTest {


    private val endpoint = "endpoint"
    private val baseUrl = "url"
    private val org = "org"
    private val site = "site"
    private val env = "env"
    private val videoCollectionName = "video"
    private val preLoading = true

    @Test
    fun `build on success with setBaseUrl`() {
        val actual =
            ArcXPContentConfig
                .Builder()
                .setOrgName(name = org)
                .setEnvironment(env = env)
                .setSite(site = site)
                .setBaseUrl(url = baseUrl)
                .setNavigationEndpoint(endpoint = endpoint)
                .build()
        assertEquals(org, actual.orgName)
        assertEquals(env, actual.environment)
        assertEquals(baseUrl, actual.baseUrl)
        assertEquals(endpoint, actual.navigationEndpoint)
    }

    @Test
    fun `build on success with null environment`() {
        val actual =
            ArcXPContentConfig
                .Builder()
                .setOrgName(name = org)
                .setSite(site = site)
                .setBaseUrl(url = baseUrl)
                .setNavigationEndpoint(endpoint = endpoint)
                .build()
        assertEquals(org, actual.orgName)
        assertEquals("", actual.environment)
        assertEquals(baseUrl, actual.baseUrl)
        assertEquals(endpoint, actual.navigationEndpoint)
    }

    @Test
    fun `build on success with individual setters`() {

        val expectedMaxCacheSize = 1023
        val expectedTimeUntilUpdate = 24687387

        val actual =
            ArcXPContentConfig
                .Builder()
                .setOrgName(name = org)
                .setEnvironment(env = env)
                .setSite(site = site)
                .setBaseUrl(url = baseUrl)
                .setNavigationEndpoint(endpoint)
                .setCacheSize(sizeInMB = expectedMaxCacheSize)
                .setPreloading(preLoading = preLoading)
                .setVideoCollectionName(videoCollectionName = videoCollectionName)
                .setCacheTimeUntilUpdate(minutes = expectedTimeUntilUpdate)
                .build()
        assertEquals(org, actual.orgName)
        assertEquals(site, actual.siteName)
        assertEquals(env, actual.environment)
        assertEquals(endpoint, actual.navigationEndpoint)
        assertEquals(expectedMaxCacheSize, actual.cacheSizeMB)
        assertEquals(preLoading, actual.preLoading)
        assertEquals(videoCollectionName, actual.videoCollectionName)
        assertEquals(expectedTimeUntilUpdate, actual.cacheTimeUntilUpdateMinutes)
    }

    @Test
    fun `build on success with cache size below minimum uses minimum`() {

        val expectedMaxCacheSize = -876423
        val expectedTimeUntilUpdate = 24687387

        val actual =
            ArcXPContentConfig
                .Builder()
                .setOrgName(name = org)
                .setEnvironment(env = env)
                .setSite(site = site)
                .setBaseUrl(url = baseUrl)
                .setNavigationEndpoint(endpoint)
                .setCacheSize(sizeInMB = expectedMaxCacheSize)
                .setPreloading(preLoading = preLoading)
                .setCacheTimeUntilUpdate(minutes = expectedTimeUntilUpdate)
                .build()
        assertEquals(org, actual.orgName)
        assertEquals(site, actual.siteName)
        assertEquals(env, actual.environment)
        assertEquals(endpoint, actual.navigationEndpoint)
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
                .setOrgName(name = org)
                .setEnvironment(env = env)
                .setSite(site = site)
                .setBaseUrl(url = baseUrl)
                .setNavigationEndpoint(endpoint)
                .setCacheSize(sizeInMB = expectedMaxCacheSize)
                .setPreloading(preLoading = preLoading)
                .setCacheTimeUntilUpdate(minutes = expectedTimeUntilUpdate)
                .build()
        assertEquals(org, actual.orgName)
        assertEquals(site, actual.siteName)
        assertEquals(env, actual.environment)
        assertEquals(endpoint, actual.navigationEndpoint)
        assertEquals(Constants.VALID_CACHE_SIZE_RANGE_MB.last, actual.cacheSizeMB)
        assertEquals(preLoading, actual.preLoading)
        assertEquals(expectedTimeUntilUpdate, actual.cacheTimeUntilUpdateMinutes)
    }

    @Test
    fun `build when org is null`() {
        val result = assertThrows(
            ArcXPContentException::class.java
        ) {
            ArcXPContentConfig
                .Builder()
                .setBaseUrl(url = baseUrl)
                .setSite(site = site)
                .build()
        }
        assertEquals(
            "Failed Initialization: SDK needs values for org and site for analytics/logging",
            result.message
        )
    }

    @Test
    fun `build when base url is null`() {
        val result = assertThrows(
            ArcXPContentException::class.java
        ) {
            ArcXPContentConfig
                .Builder()
                .build()
        }
        assertEquals(
            "Failed Initialization: SDK needs values for baseUrl for backend communication",
            result.message
        )
    }

    @Test
    fun `build when base url is blank`() {
        val result = assertThrows(
            ArcXPContentException::class.java
        ) {
            ArcXPContentConfig
                .Builder()
                .setBaseUrl("  ")
                .build()
        }
        assertEquals(
            "Failed Initialization: SDK needs values for baseUrl for backend communication",
            result.message
        )
    }

    @Test
    fun `build when site is blank`() {
        val result = assertThrows(
            ArcXPContentException::class.java
        ) {
            ArcXPContentConfig
                .Builder()
                .setOrgName(name = org)
                .setEnvironment(env = "")
                .setSite(site = " ")
                .setBaseUrl(url = baseUrl)
                .build()
        }
        assertEquals(
            "Failed Initialization: SDK needs values for org and site for analytics/logging",
            result.message
        )
    }

    @Test
    fun `build when org is blank`() {
        val result = assertThrows(
            ArcXPContentException::class.java
        ) {
            ArcXPContentConfig
                .Builder()
                .setOrgName(name = "        ")
                .setSite(site = site)
                .setBaseUrl(url = baseUrl)
                .setEnvironment(env = env)
                .build()
        }
        assertEquals(
            "Failed Initialization: SDK needs values for org and site for analytics/logging",
            result.message
        )
    }

    @Test
    fun `build when site is null`() {
        val result = assertThrows(
            ArcXPContentException::class.java
        ) {
            ArcXPContentConfig
                .Builder()
                .setOrgName(name = org)
                .setBaseUrl(url = baseUrl)
                .build()
        }
        assertEquals(
            "Failed Initialization: SDK needs values for org and site for analytics/logging",
            result.message
        )
    }


    @Test
    fun `fail when navigationEndpoint is empty in build`() {
        val result = assertThrows(
            ArcXPContentException::class.java
        ) {
            ArcXPContentConfig.Builder()
                .setBaseUrl(url = "sales")
                .setOrgName(name = org)
                .setSite(site = site)
                .setNavigationEndpoint(endpoint = "")
                .build()
        }
        assertEquals(
            "Failed Initialization: SDK Needs navigationEndpoint value for site service",
            result.message
        )
    }

    @Test
    fun `throws error when navigationEndpoint is missing in build`() {
        val result = assertThrows(
            ArcXPContentException::class.java
        ) {
            ArcXPContentConfig.Builder()
                .setEnvironment(env = env)
                .setBaseUrl(url = "sales")
                .setOrgName(name = org)
                .setSite(site = site)
                .build()
        }
        assertEquals(
            "Failed Initialization: SDK Needs navigationEndpoint value for site service",
            result.message
        )
    }

    @Test
    fun `build when time to update is below min uses min`() {
        val testObject = ArcXPContentConfig
            .Builder()
            .setOrgName(name = org)
            .setSite(site = site)
            .setBaseUrl(url = baseUrl)
            .setNavigationEndpoint(endpoint)
            .setCacheTimeUntilUpdate(0)
            .setEnvironment(env = env)
            .build()


        assertEquals(Constants.CACHE_TIME_UNTIL_UPDATE_MIN, testObject.cacheTimeUntilUpdateMinutes)
    }

    @Test
    fun `contentBaseUrl returns expected`() {
        val actual = ArcXPContentConfig
            .Builder()
            .setBaseUrl("https://org-env.web.arc-cdn.net")
            .setOrgName(name = org)
            .setSite(site = site)
            .setEnvironment(env = env)
            .setNavigationEndpoint(endpoint)
            .build().baseUrl
        val expected = "https://org-env.web.arc-cdn.net"

        assertEquals(expected, actual)
    }

    @Test
    fun `baseUrl returns expected`() {
        val actual = ArcXPContentConfig
            .Builder()
            .setBaseUrl(url = baseUrl)
            .setOrgName(name = org)
            .setEnvironment(env = "")
            .setSite(site = site)
            .setNavigationEndpoint(endpoint)
            .build().baseUrl


        assertEquals(baseUrl, actual)
    }

    @Test
    fun `set base url`() {
        val actual = ArcXPContentConfig
            .Builder()
            .setBaseUrl(url = baseUrl)
            .setEnvironment(env = "")
            .setOrgName(name = org)
            .setSite(site = site)
            .setNavigationEndpoint(endpoint)
            .build().baseUrl


        assertEquals(baseUrl, actual)
    }
}
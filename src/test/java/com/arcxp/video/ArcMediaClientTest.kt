package com.arcxp.video

import android.app.Application
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commons.throwables.ArcXPError
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.sdk.R
import com.arcxp.video.api.VideoApiManager
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import kotlin.test.assertTrue

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ArcMediaClientTest {

    private val baseUrl = "baseurl-org"
    private val org = "org"
    private val env = "env"

    @RelaxedMockK
    private lateinit var listener: ArcVideoStreamCallback

    @RelaxedMockK
    private lateinit var application: Application

    @RelaxedMockK
    private lateinit var playlistListener: ArcVideoPlaylistCallback

    private val orgFailure = "org cannot be blank"
    private val baseUrlFailure = "baseUrl cannot be blank"

    @RelaxedMockK
    private lateinit var videoApiManager: VideoApiManager

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockkObject(DependencyFactory)
        every { DependencyFactory.createVideoApiManager(baseUrl = baseUrl) } returns videoApiManager
        every {
            DependencyFactory.createVideoApiManager(
                orgName = org,
                environmentName = env
            )
        } returns videoApiManager
        mockkObject(ArcXPMobileSDK)
        every {
            ArcXPMobileSDK.application()
        } returns mockk {
            every {
                getString(R.string.blank_baseurl_failure)
            } returns baseUrlFailure
            every {
                getString(R.string.org_failure)
            } returns orgFailure
        }
    }

    @After
    fun tearDown() {
        unmockkObject(DependencyFactory)
    }

    @Test(expected = ArcXPError::class)
    fun `initialize calls getInstance with blank baseurl, throws ArcXPError`() {
        try {
            ArcMediaClient(baseUrl = "")
        } catch (e: ArcXPError) {
            e.apply {
                assertTrue { type == ArcXPSDKErrorType.INIT_ERROR }
                assertTrue { message == baseUrlFailure }
            }
            throw e
        }

    }

    @Test(expected = ArcXPError::class)
    fun `instantiate calls getInstance with blank baseUrl, throws ArcXPError`() {
        ArcMediaClient(baseUrl = "")
    }

    @Test(expected = ArcXPError::class)
    fun `create(org, env) calls getInstance with blank org, throws ArcXPError`() {
        ArcMediaClient(orgName = "", serverEnvironment = "")
    }

    @Test
    fun `findByUuid calls through to api Manager`() {
        ArcMediaClient(baseUrl)
            .findByUuid(uuid = "uuid", listener = listener, shouldUseVirtualChannel = true)

        verify(exactly = 1) {
            videoApiManager.findByUuidApi(
                uuid = "uuid",
                listener = listener, shouldUseVirtualChannel = true
            )
        }
    }

    @Test
    fun `findByUuid calls through to api Manager with default no virtual channel`() {
        ArcMediaClient(baseUrl)
            .findByUuid(uuid = "uuid", listener = listener)

        verify(exactly = 1) {
            videoApiManager.findByUuidApi(
                uuid = "uuid",
                listener = listener, shouldUseVirtualChannel = false
            )
        }
    }

    @Test
    fun `findByUuidAsJson calls through to api Manager`() {
        ArcMediaClient(baseUrl)
            .findByUuidAsJson(uuid = "uuid", listener = listener, shouldUseVirtualChannel = true)

        verify(exactly = 1) {
            videoApiManager.findByUuidApiAsJson(
                uuid = "uuid",
                listener = listener, shouldUseVirtualChannel = true
            )
        }
    }

    @Test
    fun `findByUuidAsJson calls through to api Manager with default`() {
        ArcMediaClient(baseUrl)
            .findByUuidAsJson(uuid = "uuid", listener = listener)

        verify(exactly = 1) {
            videoApiManager.findByUuidApiAsJson(
                uuid = "uuid",
                listener = listener, shouldUseVirtualChannel = false
            )
        }
    }

    @Test
    fun `findByPlaylist calls through to api Manager`() {
        val name = "playlist name"
        val count = 3

        ArcMediaClient(baseUrl)
            .findByPlaylist(name, count, playlistListener)

        verify(exactly = 1) {
            videoApiManager.findByPlaylistApi(name, count, playlistListener)
        }
    }

    @Test
    fun `findByPlaylist calls through to api Manager using ArcMediaClient(org, env)`() {
        val name = "playlist name"
        val count = 3

        ArcMediaClient(orgName = org, serverEnvironment = env)
            .findByPlaylist(name, count, playlistListener)

        verify(exactly = 1) {
            videoApiManager.findByPlaylistApi(name, count, playlistListener)
        }
    }

    @Test
    fun `findByPlaylistAsJson calls through to api Manager`() {
        val name = "playlist name"
        val count = 3

        ArcMediaClient(baseUrl)
            .findByPlaylistAsJson(name, count, playlistListener)

        verify(exactly = 1) {
            videoApiManager.findByPlaylistApiAsJson(name, count, playlistListener)
        }
    }

    @Test
    fun `findByPlaylistAsJson calls through to api Manager using ArcMediaClient(org, env)`() {
        val name = "playlist name"
        val count = 3

        ArcMediaClient(orgName = org, serverEnvironment = env)
            .findByPlaylistAsJson(name, count, playlistListener)

        verify(exactly = 1) {
            videoApiManager.findByPlaylistApiAsJson(name, count, playlistListener)
        }
    }

    @Test
    fun `findLive calls through to api Manager`() {
        ArcMediaClient(orgName = org, serverEnvironment = env)
            .findLive(listener = listener)

        verify(exactly = 1) {
            videoApiManager.findLive(listener = listener)
        }
    }

    @Test
    fun `findLiveSuspend calls through to api Manager`() = runTest {
        ArcMediaClient(orgName = org, serverEnvironment = env)
            .findLiveSuspend()

        coVerify(exactly = 1) {
            videoApiManager.findLiveSuspend()
        }
    }

    @Test
    fun `findLiveAsJson calls through to api Manager`() {
        ArcMediaClient(orgName = org, serverEnvironment = env)
            .findLiveAsJson(listener = listener)

        verify(exactly = 1) {
            videoApiManager.findLiveAsJson(listener = listener)
        }
    }

    @Test
    fun `findLiveSuspendAsJson calls through to api Manager`() = runTest {
        ArcMediaClient(orgName = org, serverEnvironment = env)
            .findLiveSuspendAsJson()

        coVerify(exactly = 1) {
            videoApiManager.findLiveSuspendAsJson()
        }
    }
}
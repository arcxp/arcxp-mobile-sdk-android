package com.arcxp.video

import android.app.Application
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commons.throwables.ArcXPError
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.sdk.R
import com.arcxp.video.api.VideoApiManager
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
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
    fun `findByUuids (listener, vararg) calls through to api Manager`() {
        ArcMediaClient(baseUrl)
            .findByUuids(listener = listener, "uuid1", "uuid2", "uuid3")

        verify(exactly = 1) {
            videoApiManager.findByUuidsApi(
                listener = listener,
                uuids = listOf("uuid1", "uuid2", "uuid3")
            )
        }
    }

    @Test
    fun `findByUuids (vararg, listener) calls through to api Manager`() {
        ArcMediaClient(baseUrl)
            .findByUuids("uuid1", "uuid2", "uuid3", listener = listener)

        verify(exactly = 1) {
            videoApiManager.findByUuidsApi(
                listener = listener,
                uuids = listOf("uuid1", "uuid2", "uuid3")
            )
        }
    }

    @Test
    fun `findByUuids (list, listener) calls through to api Manager`() {
        val list = listOf("uuid1", "uuid2", "uuid3")

        ArcMediaClient(baseUrl)
            .findByUuids(uuids = list, listener = listener)

        verify(exactly = 1) {
            videoApiManager.findByUuidsApi(listener = listener, uuids = list)
        }
    }

    @Test
    fun `findByUuids (emptyList(), listener) calls through to api Manager`() {
        val list = listOf("uuid1", "uuid2", "uuid3")

        ArcMediaClient(baseUrl)
            .findByUuids(emptyList(), listener)

        verify(exactly = 1) {
            videoApiManager.findByUuidsApi(listener, emptyList())
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
    fun `findByUuidsAsJson (listener, vararg) calls through to api Manager`() {
        ArcMediaClient(baseUrl)
            .findByUuidsAsJson(listener, "uuid1", "uuid2", "uuid3")

        verify(exactly = 1) {
            videoApiManager.findByUuidsApiAsJson(
                listener = listener,
                uuids = listOf("uuid1", "uuid2", "uuid3")
            )
        }
    }

    @Test
    fun `findByUuidsAsJson (vararg, listener) calls through to api Manager`() {
        ArcMediaClient(baseUrl)
            .findByUuidsAsJson("uuid1", "uuid2", "uuid3", listener = listener)

        verify(exactly = 1) {
            videoApiManager.findByUuidsApiAsJson(
                listener = listener,
                uuids = listOf("uuid1", "uuid2", "uuid3")
            )
        }
    }

    @Test
    fun `findByUuidsAsJson (list, listener) calls through to api Manager`() {
        val list = listOf("uuid1", "uuid2", "uuid3")

        ArcMediaClient(baseUrl)
            .findByUuidsAsJson(uuids = list, listener = listener)

        verify(exactly = 1) {
            videoApiManager.findByUuidsApiAsJson(listener = listener, uuids = list)
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
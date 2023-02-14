package com.arcxp.video

import com.arcxp.video.ArcMediaClient.Companion.createClient
import com.arcxp.video.api.VideoApiManager
import com.arcxp.video.util.DependencyProvider
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.After
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ArcMediaClientTest {

    private val baseUrl = "baseurl-org"
    private val org = "org"
    private val env = "env"

    @RelaxedMockK
    private lateinit var listener: ArcVideoStreamCallback

    @RelaxedMockK
    private lateinit var playlistListener: ArcVideoPlaylistCallback

    @RelaxedMockK
    private lateinit var videoApiManager: VideoApiManager

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockkObject(DependencyProvider)
        every { DependencyProvider.createVideoApiManager(baseUrl = baseUrl) } returns videoApiManager
        every { DependencyProvider.createVideoApiManager(orgName = org, environmentName = env) } returns videoApiManager
    }

    @After
    fun tearDown() {
        unmockkObject(DependencyProvider)
    }

    @Test(expected = ArcException::class)
    fun `initialize calls getInstance with blank orgName, throws ArcException`() {
        ArcMediaClient.initialize("")
    }

    @Test(expected = ArcException::class)
    fun `instantiate calls getInstance with blank baseUrl, throws ArcException`() {
        ArcMediaClient.instantiate("")
    }

    @Test(expected = ArcException::class)
    fun `create(org, env) calls getInstance with blank org, throws ArcException`() {
        createClient(orgName = "", serverEnvironment = "")
    }

    @Test
    fun `findByUuid calls through to api Manager`() {
        createClient(baseUrl)
            .findByUuid("uuid", listener, shouldUseVirtualChannel = true)

        verify(exactly = 1) {
            videoApiManager.findByUuidApi("uuid", listener, shouldUseVirtualChannel = true)
        }
    }

    @Test
    fun `findByUuids (listener, vararg) calls through to api Manager`() {
        createClient(baseUrl)
            .findByUuids(listener, "uuid1","uuid2", "uuid3")

        verify(exactly = 1) {
            videoApiManager.findByUuidsApi(listener, listOf("uuid1","uuid2", "uuid3"))
        }
    }

    @Test
    fun `findByUuids (vararg, listener) calls through to api Manager`() {
        createClient(baseUrl)
            .findByUuids("uuid1", "uuid2", "uuid3", listener = listener)

        verify(exactly = 1) {
            videoApiManager.findByUuidsApi(listener, listOf("uuid1", "uuid2", "uuid3"))
        }
    }

    @Test
    fun `findByUuids (list, listener) calls through to api Manager`() {
        val list = listOf("uuid1", "uuid2", "uuid3")

        createClient(baseUrl)
            .findByUuids(list, listener)

        verify(exactly = 1) {
            videoApiManager.findByUuidsApi(listener, list)
        }
    }

    @Test
    fun `findByPlaylist calls through to api Manager`() {
        val name = "playlist name"
        val count = 3

        createClient(baseUrl)
            .findByPlaylist(name, count, playlistListener)

        verify(exactly = 1) {
            videoApiManager.findByPlaylistApi(name, count, playlistListener)
        }
    }

    @Test
    fun `findByPlaylist calls through to api Manager using createClient(org, env)`() {
        val name = "playlist name"
        val count = 3

        createClient(orgName = org, serverEnvironment = env)
            .findByPlaylist(name, count, playlistListener)

        verify(exactly = 1) {
            videoApiManager.findByPlaylistApi(name, count, playlistListener)
        }
    }

    @Test
    fun `findLive calls through to api Manager`() {
        createClient(orgName = org, serverEnvironment = env)
            .findLive(listener = listener)

        verify(exactly = 1) {
            videoApiManager.findLive(listener = listener)
        }
    }
}
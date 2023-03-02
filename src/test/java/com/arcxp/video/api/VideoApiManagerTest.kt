package com.arcxp.video.api

import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.testutils.TestUtils.createVideoStream
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.MoshiController.toJson
import com.arcxp.commons.util.Success
import com.arcxp.video.ArcVideoPlaylistCallback
import com.arcxp.video.ArcVideoStreamCallback
import com.arcxp.video.model.*
import com.arcxp.video.service.AkamaiService
import com.arcxp.video.service.ArcMediaClientService
import com.arcxp.video.service.VirtualChannelService
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Response.error
import retrofit2.Response.success
import retrofit2.mock.Calls
import java.io.IOException
import java.net.UnknownHostException


class VideoApiManagerTest {

    @RelaxedMockK
    private lateinit var listener: ArcVideoStreamCallback

    @RelaxedMockK
    private lateinit var playListListener: ArcVideoPlaylistCallback

    @MockK
    private lateinit var baseService: ArcMediaClientService

    @MockK
    private lateinit var akamaiService: AkamaiService

    @MockK
    private lateinit var virtualChannelService: VirtualChannelService

    private lateinit var testObject: VideoApiManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        testObject =
            VideoApiManager(
                orgName = "org",
                environmentName = "env",
                baseService = baseService,
                akamaiService = akamaiService,
                virtualChannelService = virtualChannelService
            )
    }

    @Test
    fun `findByUuid for base request is 401 unauthorized, and is handled by listener`() {
        every { akamaiService.findByUuid(uuid = "id") } answers {
            Calls.response(
                error(
                    401,
                    "".toResponseBody()
                )
            )
        }

        testObject.findByUuidApi(uuid = "id", listener = listener)

        verify(exactly = 1) {
            listener.onError(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = "401: Unauthorized",
                value = any()
            )
        }
    }

    @Test
    fun `findByUuid for base request is 404 Not Found, and is handled by listener`() {
        every { akamaiService.findByUuid(uuid = "id") } answers {
            Calls.response(
                error(
                    404,
                    "".toResponseBody()
                )
            )
        }

        testObject.findByUuidApi(uuid = "id", listener = listener)

        verify(exactly = 1) {
            listener.onError(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = "404: Not Found",
                value = any()
            )
        }
    }

    @Test
    fun `findByUuid for base request is 403 Forbidden, and is handled by listener`() {
        every { akamaiService.findByUuid(uuid = "id") } answers {
            Calls.response(
                error(
                    403,
                    "".toResponseBody()
                )
            )
        }

        testObject.findByUuidApi(uuid = "id", listener = listener)

        verify(exactly = 1) {
            listener.onError(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = "403: Forbidden",
                value = any()
            )
        }
    }

    @Test
    fun `findByUuid for base request is other error, and is handled by listener`() {
        every { akamaiService.findByUuid(uuid = "id") } answers {
            Calls.response(
                error(
                    405,
                    "".toResponseBody()
                )
            )
        }

        testObject.findByUuidApi(uuid = "id", listener = listener)

        verify(exactly = 1) {
            listener.onError(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = "405: Response.error()",
                value = any()
            )
        }
    }

    @Test
    fun `findByUuid for akamai restricted request returns expected data`() {
        val expectedList = listOf(createVideoStream())
        val expectedArcVideoResponse = ArcVideoResponse(null, expectedList)
        val expectedResponseBody = mockk<ResponseBody>(relaxed = true) {
            every { string() } returns toJson(expectedList)!!
        }

        every { akamaiService.findByUuid("id") } answers {
            Calls.response(
                expectedResponseBody
            )
        }

        testObject.findByUuidApi(uuid = "id", listener = listener)

        verify(exactly = 1) { listener.onVideoResponse(arcVideoResponse = expectedArcVideoResponse) }
    }

    @Test
    fun `findByUuid for akamaiService restricted request returns disallowed and is handled`() {
        val expectedArcTypeResponse = ArcTypeResponse(
            "geo-restriction",
            false,
            TypeParams(country = "country", zip = "zip", dma = "dma"),
            ComputedLocation(country = "Germany", zip = "zip", dma = "dma")
        )
        val expectedResponseBody = mockk<ResponseBody>(relaxed = true) {
            every { string() } returns toJson(expectedArcTypeResponse)!!
        }
        every { akamaiService.findByUuid("id") } answers {
            Calls.response(
                expectedResponseBody
            )
        }

        testObject.findByUuidApi(uuid = "id", listener = listener)

        verify(exactly = 1) {
            listener.onError(
                type = ArcXPSDKErrorType.SOURCE_ERROR,
                message = "This Geo-restricted content is not allowed in region: Germany",
                value = expectedArcTypeResponse
            )
        }
    }

    @Test
    fun `findByUuid for akamai restricted request returns bad result and is handled`() {
        val expectedResponseBody = mockk<ResponseBody>(relaxed = true) {
            every { string() } returns toJson("not json")!!
        }
        every { akamaiService.findByUuid("id") } answers {
            Calls.response(
                expectedResponseBody
            )
        }

        testObject.findByUuidApi(uuid = "id", listener = listener)

        verify(exactly = 1) {
            listener.onError(
                type = ArcXPSDKErrorType.SOURCE_ERROR,
                message = "Bad result from geo restricted video call to findByUuid()",
                value = any()
            )
        }
    }

    @Test
    fun `findByUuid for akamai restricted request has failure `() {
        val expected = IOException()
        every { virtualChannelService.findByUuidVirtual("id") } answers { Calls.failure(expected) }
        every { akamaiService.findByUuid("id") } answers {
            Calls.failure(
                expected
            )
        }

        testObject.findByUuidApi(uuid = "id", listener = listener)

        verify(exactly = 1) {
            listener.onError(
                type = ArcXPSDKErrorType.SOURCE_ERROR,
                message = "Error in geo restricted video call to findByUuid()",
                value = expected
            )
        }
    }

    @Test
    fun `findByUuid for akamai restricted request has error in call, handled by listener`() {
        val expected: Call<ResponseBody> = Calls.response(
            error(
                404,
                "".toResponseBody()
            )
        )
        every { akamaiService.findByUuid("id") } answers {
            expected
        }

        testObject.findByUuidApi(uuid = "id", listener = listener)

        verify(exactly = 1) {
            listener.onError(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = "404: Not Found",
                value = any()
            )
        }
    }

    @Test
    fun `findByUuid for virtual channel request returns expected data`() {
        val expected = mockk<ArcVideoStreamVirtualChannel>()
        every { virtualChannelService.findByUuidVirtual("id") } answers { Calls.response(expected) }

        testObject.findByUuidApi(uuid = "id", listener = listener, shouldUseVirtualChannel = true)

        verify(exactly = 1) { listener.onVideoStreamVirtual(arcVideoStreamVirtualChannel = expected) }
    }

    @Test
    fun `findByUuid for virtual channel request returns error and is handled`() {
        val expected: Call<ArcVideoStreamVirtualChannel> = Calls.response(
            error(
                404,
                "".toResponseBody()
            )
        )
        every { virtualChannelService.findByUuidVirtual("id") } answers { expected }

        testObject.findByUuidApi(uuid = "id", listener = listener, shouldUseVirtualChannel = true)

        verify(exactly = 1) {
            listener.onError(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = "404: Not Found",
                value = any()
            )
        }
    }

    @Test
    fun `findByUuid for virtual channel request returns handles failure`() {
        val expected = IOException()

        every { virtualChannelService.findByUuidVirtual("id") } answers { Calls.failure(expected) }

        testObject.findByUuidApi(uuid = "id", listener = listener, shouldUseVirtualChannel = true)

        verify(exactly = 1) {
            listener.onError(
                type = ArcXPSDKErrorType.SOURCE_ERROR,
                message = "Error in call to findByUuidVirtual()",
                value = expected
            )
        }
    }


    @Test
    fun `findByUuids returns expected data`() {
        val idList = listOf("id1", "id2", "id3")
        val expected =
            listOf(createVideoStream("id1"), createVideoStream("id2"), createVideoStream("id3"))
        every { akamaiService.findByUuids(uuids = idList) } answers {
            Calls.response(
                expected
            )
        }

        testObject.findByUuidsApi(listener = listener, listOf("id1", "id2", "id3"))

        verify(exactly = 1) { listener.onVideoStream(videos = expected) }
    }

    @Test
    fun `findByUuids request fails, and is handled by listener`() {
        val uuids = listOf("id1", "id2", "id3")
        val exception = IOException("message")
        every { akamaiService.findByUuids(uuids = uuids) } answers { Calls.failure(exception) }

        testObject.findByUuidsApi(listener = listener, uuids = uuids)

        verify(exactly = 1) {
            listener.onError(
                type = ArcXPSDKErrorType.SOURCE_ERROR,
                message = "Error in call to findByUuids()",
                value = exception
            )
        }
    }

    @Test
    fun `findByUuids has error, and is handled by listener`() {
        val uuids = listOf("id1", "id2", "id3")
        every { akamaiService.findByUuids(uuids = uuids) } answers {
            Calls.response(
                error(
                    401,
                    "".toResponseBody()
                )
            )
        }

        testObject.findByUuidsApi(listener = listener, uuids = uuids)

        verify(exactly = 1) {
            listener.onError(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = "401: Unauthorized",
                value = any()
            )
        }
    }

    @Test
    fun `findByPlaylist returns expected data`() {
        val expected = mockk<ArcVideoPlaylist>()
        every {
            akamaiService.findByPlaylist(
                name = "id",
                count = 1
            )
        } answers { Calls.response(expected) }

        testObject.findByPlaylistApi(name = "id", listener = playListListener, count = 1)

        verify(exactly = 1) { playListListener.onVideoPlaylist(playlist = expected) }
    }

    @Test
    fun `findByPlaylist request fails, and is handled by listener`() {
        val exception = IOException("message")
        every { akamaiService.findByPlaylist(name = "id", count = 1) } answers {
            Calls.failure(
                exception
            )
        }

        testObject.findByPlaylistApi(name = "id", listener = playListListener, count = 1)

        verify(exactly = 1) {
            playListListener.onError(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = "Error in call to findByPlaylist()",
                value = exception
            )
        }
    }

    @Test
    fun `findByPlaylist has error, and is handled by listener`() {
        every { akamaiService.findByPlaylist(name = "id", count = 1) } answers {
            Calls.response(
                error(401, "".toResponseBody())
            )
        }

        testObject.findByPlaylistApi(name = "id", listener = playListListener, count = 1)

        verify(exactly = 1) {
            playListListener.onError(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = "401: Unauthorized",
                value = any()
            )
        }
    }

    @Test
    fun `findLive Success passes result to listener`() {
        val expected = mockk<List<VideoVO>>()
        every { baseService.findLive() } answers {
            Calls.response(
                expected
            )
        }

        testObject.findLive(listener = listener)

        verify(exactly = 1) {
            listener.onLiveVideos(expected)
        }
    }

    @Test
    fun `findLive Unsuccessful response passes result to listener`() {
        val exception = IOException("message")
        every { baseService.findLive() } answers {
            Calls.failure(
                exception
            )
        }

        testObject.findLive(listener = listener)

        verify(exactly = 1) {
            listener.onError(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = "Error in call to findLive()",
                value = exception
            )
        }
    }

    @Test
    fun `findLive Failure passes result to listener`() {
        every { baseService.findLive() } answers {
            Calls.response(
                error(403, "".toResponseBody())
            )
        }

        testObject.findLive(listener = listener)

        verify(exactly = 1) {
            listener.onError(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = "403: Forbidden",
                value = any()
            )
        }
    }

    @Test
    fun `findLiveSuspend Success returns expected`() = runTest {
        val listItem = listOf(
            VideoVO(
                id = "id2134",
                adConfig = null,
                associatedContent = null,
                contentConfig = null,
                customFields = null,
                dummy = null,
                embedConfig = null,
                hideInPlaylist = null,
                imageResizerUrls = null,
                liveEventConfig = null,
                metaConfig = null,
                platform = null,
                playlistName = null,
                producerConfig = null,
                promoImage = null,
                sponsoredConfig = null,
                subtitlesConfig = null,
                syncContentEnabled = null,
                synchronizedToMethode = null,
                truthTellerEnabled = null,
                variantExclusions = null
            )
        )
        val expected = toJson(listItem)!!
        coEvery { baseService.findLiveSuspend() } coAnswers {
            success(expected.toResponseBody())
        }

        val actual = testObject.findLiveSuspend()

        assertEquals(listItem, (actual as Success).success)
    }

    @Test
    fun `findLiveSuspend returns on failure`() = runTest {
        coEvery { baseService.findLiveSuspend() } coAnswers {
            error(400, "".toResponseBody())
        }

        val actual = testObject.findLiveSuspend()

        assertEquals(ArcXPSDKErrorType.SERVER_ERROR, (actual as Failure).failure.type)
        assertEquals("Find Live Failed", actual.failure.message)
    }

    @Test
    fun `findLiveSuspend returns on exception`() = runTest {
        coEvery { baseService.findLiveSuspend() } throws UnknownHostException()

        val actual = testObject.findLiveSuspend()

        assertEquals(ArcXPSDKErrorType.SERVER_ERROR, (actual as Failure).failure.type)
        assertEquals("Find Live Exception", actual.failure.message)
    }

}


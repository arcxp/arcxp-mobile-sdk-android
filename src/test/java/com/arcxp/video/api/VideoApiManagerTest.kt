package com.arcxp.video.api

import android.app.Application
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commons.testutils.TestUtils.createVideoStream
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.MoshiController.toJson
import com.arcxp.commons.util.Success
import com.arcxp.sdk.R
import com.arcxp.video.ArcVideoPlaylistCallback
import com.arcxp.video.ArcVideoStreamCallback
import com.arcxp.video.model.ArcTypeResponse
import com.arcxp.video.model.ArcVideoPlaylist
import com.arcxp.video.model.ArcVideoResponse
import com.arcxp.video.model.ArcVideoStreamVirtualChannel
import com.arcxp.video.model.ComputedLocation
import com.arcxp.video.model.TypeParams
import com.arcxp.video.model.VideoVO
import com.arcxp.video.service.AkamaiService
import com.arcxp.video.service.ArcMediaClientService
import com.arcxp.video.service.VirtualChannelService
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
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
    private lateinit var application: Application

    @RelaxedMockK
    private lateinit var playListListener: ArcVideoPlaylistCallback

    @MockK
    private lateinit var baseService: ArcMediaClientService

    @MockK
    private lateinit var akamaiService: AkamaiService

    @MockK
    private lateinit var virtualChannelService: VirtualChannelService

    private val expectedCountry = "USA"

    private lateinit var testObject: VideoApiManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        mockkObject(ArcXPMobileSDK)
        every { ArcXPMobileSDK.application() } returns application
        every { application.getString(R.string.error_in_call_to_findbyuuidvirtual) } returns "Error in call to findByUuidVirtual()"
        every {
            application.getString(
                R.string.this_geo_restricted_content_is_not_allowed_in_region,
                expectedCountry
            )
        } returns "This Geo-restricted content is not allowed in region: $expectedCountry"
        every { application.getString(R.string.bad_result_from_geo_restricted_video_call_to_findbyuuid) } returns "Bad result from geo restricted video call to findByUuid()"
        every { application.getString(R.string.error_in_geo_restricted_video_call_to_findbyuuid) } returns "Error in geo restricted video call to findByUuid()"
        every { application.getString(R.string.error_in_call_to_findbyuuids) } returns "Error in call to findByUuids()"
        every { application.getString(R.string.error_in_call_to_findbyplaylist) } returns "Error in call to findByPlaylist()"
        every { application.getString(R.string.find_live_failed) } returns "Find Live Failed"
        every { application.getString(R.string.find_live_exception) } returns "Find Live Exception"
        every { application.getString(R.string.unauthorized) } returns "Unauthorized"
        every { application.getString(R.string.forbidden) } returns "Forbidden"
        every { application.getString(R.string.not_found) } returns "Not Found"
        every { application.getString(R.string.unknown_country) } returns "Unknown"

        testObject =
            VideoApiManager(
                orgName = "org",
                environmentName = "env",
                baseService = baseService,
                akamaiService = akamaiService,
                virtualChannelService = virtualChannelService
            )
    }

    @After
    fun tearDown() {
        clearAllMocks()
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
    fun `findByUuidAsJson for base request is 403 Forbidden, and is handled by listener`() {
        every { akamaiService.findByUuidAsJson(uuid = "id") } answers {
            Calls.response(
                error(
                    403,
                    "".toResponseBody()
                )
            )
        }

        testObject.findByUuidApiAsJson(uuid = "id", listener = listener)

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
    fun `findByUuidAsJson for akamai restricted request returns expected data`() {
        val expected = "json"

        every { akamaiService.findByUuidAsJson("id") } answers {
            Calls.response(
                expected.toResponseBody()
            )
        }

        testObject.findByUuidApiAsJson(uuid = "id", listener = listener)

        verify(exactly = 1) { listener.onJsonResult(json = expected) }
    }

    @Test
    fun `findByUuid for akamaiService restricted request returns disallowed and is handled`() {
        val expectedArcTypeResponse = ArcTypeResponse(
            "geo-restriction",
            false,
            TypeParams(country = "country", zip = "zip", dma = "dma"),
            ComputedLocation(country = expectedCountry, zip = "zip", dma = "dma")
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
                message = "This Geo-restricted content is not allowed in region: $expectedCountry",
                value = expectedArcTypeResponse
            )
        }
    }

    @Test
    fun `findByUuidAsJson for akamaiService restricted request returns disallowed and is handled`() {
        every {
            application.getString(
                R.string.this_geo_restricted_content_is_not_allowed_in_region, "Unknown"
            )
        } returns "This Geo-restricted content is not allowed in region: Unknown"
        val expectedArcTypeResponse = ArcTypeResponse(
            "geo-restriction",
            false,
            TypeParams(country = "country", zip = "zip", dma = "dma"),
            ComputedLocation(country = null, zip = "zip", dma = "dma")
        )
        val expectedResponseBody = "json"
        every { akamaiService.findByUuidAsJson("id") } answers {
            Calls.response(
                expectedResponseBody.toResponseBody()
            )
        }

        testObject.findByUuidApiAsJson(uuid = "id", listener = listener)

        verify(exactly = 1) {
            listener.onError(
                type = ArcXPSDKErrorType.SOURCE_ERROR,
                message = "This Geo-restricted content is not allowed in region: $expectedCountry",
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
    fun `findByUuidAsJson for akamai restricted request has failure `() {
        val expected = IOException()
        every { akamaiService.findByUuidAsJson("id") } answers {
            Calls.failure(
                expected
            )
        }

        testObject.findByUuidApiAsJson(uuid = "id", listener = listener)

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
    fun `findByUuidAsJson for virtual channel request returns handles failure`() {
        val expected = IOException()

        every { virtualChannelService.findByUuidVirtualAsJson("id") } answers {
            Calls.failure(
                expected
            )
        }

        testObject.findByUuidApiAsJson(
            uuid = "id",
            listener = listener,
            shouldUseVirtualChannel = true
        )

        verify(exactly = 1) {
            listener.onError(
                type = ArcXPSDKErrorType.SOURCE_ERROR,
                message = "Error in call to findByUuidVirtual()",
                value = expected
            )
        }
    }

    @Test
    fun `findByUuidAsJson for virtual channel request returns expected data`() {
        val expected = "json"
        every { virtualChannelService.findByUuidVirtualAsJson("id") } answers {
            Calls.response(
                expected.toResponseBody()
            )
        }

        testObject.findByUuidApiAsJson(
            uuid = "id",
            listener = listener,
            shouldUseVirtualChannel = true
        )

        verify(exactly = 1) { listener.onJsonResult(json = expected) }
    }

    @Test
    fun `findByUuidAsJson for virtual channel request returns error and is handled`() {
        val expected: Call<ResponseBody> = Calls.response(
            error(
                404,
                "".toResponseBody()
            )
        )
        every { virtualChannelService.findByUuidVirtualAsJson("id") } answers { expected }

        testObject.findByUuidApiAsJson(
            uuid = "id",
            listener = listener,
            shouldUseVirtualChannel = true
        )

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
        val exception = IOException()
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
    fun `findByUuidsAsJson returns expected data`() {
        val idList = listOf("id1", "id2", "id3")
        val expected = "json"
        every { akamaiService.findByUuidsAsJson(uuids = idList) } answers {
            Calls.response(
                expected.toResponseBody()
            )
        }

        testObject.findByUuidsApiAsJson(listener = listener, listOf("id1", "id2", "id3"))

        verify(exactly = 1) { listener.onJsonResult(json = expected) }
    }

    @Test
    fun `findByUuidsAsJson request fails, and is handled by listener`() {
        val uuids = listOf("id1", "id2", "id3")
        val exception = IOException()
        every { akamaiService.findByUuidsAsJson(uuids = uuids) } answers { Calls.failure(exception) }

        testObject.findByUuidsApiAsJson(listener = listener, uuids = uuids)

        verify(exactly = 1) {
            listener.onError(
                type = ArcXPSDKErrorType.SOURCE_ERROR,
                message = "Error in call to findByUuids()",
                value = exception
            )
        }
    }

    @Test
    fun `findByUuidsAsJson has error, and is handled by listener`() {
        val uuids = listOf("id1", "id2", "id3")
        every { akamaiService.findByUuidsAsJson(uuids = uuids) } answers {
            Calls.response(
                error(
                    401,
                    "".toResponseBody()
                )
            )
        }

        testObject.findByUuidsApiAsJson(listener = listener, uuids = uuids)

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
        val exception = IOException()
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
    fun `findByPlaylistAsJson returns expected data`() {
        val expected = "jay son"
        every {
            akamaiService.findByPlaylistAsJson(
                name = "id",
                count = 1
            )
        } answers { Calls.response(expected.toResponseBody()) }

        testObject.findByPlaylistApiAsJson(name = "id", listener = playListListener, count = 1)

        verify(exactly = 1) { playListListener.onJsonResult(json = expected) }
    }

    @Test
    fun `findByPlaylistAsJson request fails, and is handled by listener`() {
        val exception = IOException()
        every { akamaiService.findByPlaylistAsJson(name = "id", count = 1) } answers {
            Calls.failure(
                exception
            )
        }

        testObject.findByPlaylistApiAsJson(name = "id", listener = playListListener, count = 1)

        verify(exactly = 1) {
            playListListener.onError(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = "Error in call to findByPlaylist()",
                value = exception
            )
        }
    }

    @Test
    fun `findByPlaylistAsJson has error, and is handled by listener`() {
        every { akamaiService.findByPlaylistAsJson(name = "id", count = 1) } answers {
            Calls.response(
                error(401, "".toResponseBody())
            )
        }

        testObject.findByPlaylistApiAsJson(name = "id", listener = playListListener, count = 1)

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
        val exception = IOException()
        every { baseService.findLive() } answers {
            Calls.failure(
                exception
            )
        }

        testObject.findLive(listener = listener)

        verify(exactly = 1) {
            listener.onError(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = "Find Live Failed",
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
    fun `findLiveAsJson Success passes result to listener`() {
        val expected = "expected result json"
        every { baseService.findLiveAsJson() } answers {
            Calls.response(
                expected.toResponseBody()
            )
        }

        testObject.findLiveAsJson(listener = listener)

        verify(exactly = 1) {
            listener.onJsonResult(json = expected)
        }
    }

    @Test
    fun `findLiveAsJson Unsuccessful response passes result to listener`() {
        val exception = IOException()
        every { baseService.findLiveAsJson() } answers {
            Calls.failure(
                exception
            )
        }

        testObject.findLiveAsJson(listener = listener)

        verify(exactly = 1) {
            listener.onError(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = "Find Live Failed",
                value = exception
            )
        }
    }

    @Test
    fun `findLiveAsJson Failure passes result to listener`() {
        every { baseService.findLiveAsJson() } answers {
            Calls.response(
                error(403, "".toResponseBody())
            )
        }

        testObject.findLiveAsJson(listener = listener)

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

    @Test
    fun `findLiveSuspendAsJson Success returns expected`() = runTest {

        val expected = "expected json"
        coEvery { baseService.findLiveSuspendAsJson() } coAnswers {
            success(expected.toResponseBody())
        }

        val actual = (testObject.findLiveSuspendAsJson() as Success).success

        assertEquals(expected, actual)
    }

    @Test
    fun `findLiveSuspendAsJson returns on failure`() = runTest {
        coEvery { baseService.findLiveSuspendAsJson() } coAnswers {
            error(400, "".toResponseBody())
        }

        val actual = testObject.findLiveSuspendAsJson()

        assertEquals(ArcXPSDKErrorType.SERVER_ERROR, (actual as Failure).failure.type)
        assertEquals("Find Live Failed", actual.failure.message)
    }

}


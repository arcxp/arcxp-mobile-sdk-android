package com.arc.arcvideo.service

import android.net.Uri
import android.util.Log
import com.arc.arcvideo.ArcMediaPlayerConfig
import com.arc.arcvideo.model.ArcVideoStream
import com.arc.arcvideo.model.AvailList
import com.arc.arcvideo.model.PostObject
import com.arc.arcvideo.model.Stream
import com.arc.arcvideo.util.MoshiController.toJson
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkStatic
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Answers
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PowerMockIgnore
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets


@RunWith(PowerMockRunner::class)
@PrepareForTest(URL::class, DataOutputStream::class, AdUtils::class)
@PowerMockIgnore("kotlin.*")
class AdUtilsTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    lateinit var videoStream: ArcVideoStream

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun getVideoManifestReturnsExpectedVideoAdData() {
        val stream = mock(Stream::class.java)
        val streamUrl = mock(Uri::class.java)
        val url: URL =
            mock(URL::class.java, withSettings().defaultAnswer(Answers.RETURNS_SMART_NULLS))
        val sessionUrl: URL =
            mock(URL::class.java, withSettings().defaultAnswer(Answers.RETURNS_SMART_NULLS))
        val huc = mock(HttpURLConnection::class.java)
        val expectedResponse = PostObject("manifestUrl", "trackingUrl")
        val sessionUri = mock(Uri::class.java)
        val config =
            ArcMediaPlayerConfig.Builder().addAdParam("key", "value").setUserAgent("userAgent")
                .build()
        val expectedPostData =
            "{\"adsParams\":{\"key\":\"value\"}}".toByteArray(StandardCharsets.UTF_8)
        val expectedResponseJson = toJson(expectedResponse)
        val hucOutputStream = mock(DataOutputStream::class.java)
        val outputStream = mock(DataOutputStream::class.java)

        `when`(videoStream.additionalProperties?.advertising!!.enableAdInsertion).thenReturn(true)
        `when`(videoStream.additionalProperties?.advertising!!.adInsertionUrls!!.mt_master).thenReturn(
            "mt_master"
        )
        `when`(videoStream.additionalProperties?.advertising!!.adInsertionUrls!!.mt_session).thenReturn(
            "mt_session"
        )
        `when`(stream.url).thenReturn("streamUrl")
        `when`(streamUrl.path).thenReturn("/path/")
        `when`(sessionUri.getQueryParameter("aws.sessionId")).thenReturn("sessionId")
        `when`(sessionUrl.toString()).thenReturn("sessionUrlString")
        `when`(url.openConnection()).thenReturn(huc)
        `when`(url.protocol).thenReturn("https")
        `when`(url.host).thenReturn("some.host.com/")
        `when`(huc.responseCode).thenReturn(200)
        `when`(huc.inputStream).thenReturn(expectedResponseJson!!.byteInputStream())
        `when`(huc.outputStream).thenReturn(hucOutputStream)
        PowerMockito.whenNew(URL::class.java)
            .withArguments("mt_session/path/")
            .thenReturn(url)
        PowerMockito.whenNew(URL::class.java)
            .withArguments("https://some.host.com/manifestUrl")
            .thenReturn(sessionUrl)
        PowerMockito.whenNew(DataOutputStream::class.java)
            .withArguments(hucOutputStream)
            .thenReturn(outputStream)
        mockkStatic(Uri::class)
        every { Uri.parse("streamUrl") } returns streamUrl
        every { Uri.parse("sessionUrlString") } returns sessionUri

        val result = AdUtils.getVideoManifest(videoStream, stream, config)

        assertEquals("https://some.host.com/manifestUrl", result!!.manifestUrl)
        assertEquals("https://some.host.com/trackingUrl", result.trackingUrl)
        assertEquals("sessionId", result.sessionId)
        inOrder(url, huc, outputStream).run {
            verify(url).openConnection()
            verify(huc).requestMethod = "POST"
            verify(huc).setRequestProperty("User-Agent", "userAgent")
            verify(outputStream).write(expectedPostData)
            verify(outputStream).flush()
        }
    }

    @Test
    fun getVideoManifestReturnsError() {
        val stream = mock(Stream::class.java)
        val streamUrl = mock(Uri::class.java)
        val url: URL =
            mock(URL::class.java, withSettings().defaultAnswer(Answers.RETURNS_SMART_NULLS))
        val sessionUrl: URL =
            mock(URL::class.java, withSettings().defaultAnswer(Answers.RETURNS_SMART_NULLS))
        val huc = mock(HttpURLConnection::class.java)
        val expectedResponse = PostObject("manifestUrl", "trackingUrl")
        val sessionUri = mock(Uri::class.java)
        val config =
            ArcMediaPlayerConfig.Builder().addAdParam("key", "value").setUserAgent("userAgent")
                .build()
        val expectedPostData =
            "{\"adsParams\":{\"key\":\"value\"}}".toByteArray(StandardCharsets.UTF_8)
        val expectedResponseJson = toJson(expectedResponse)
        val hucOutputStream = mock(DataOutputStream::class.java)
        val outputStream = mock(DataOutputStream::class.java)

        `when`(videoStream.additionalProperties?.advertising!!.enableAdInsertion).thenReturn(true)
        `when`(videoStream.additionalProperties?.advertising!!.adInsertionUrls!!.mt_master).thenReturn(
            "mt_master"
        )
        `when`(videoStream.additionalProperties?.advertising!!.adInsertionUrls!!.mt_session).thenReturn(
            "mt_session"
        )
        `when`(stream.url).thenReturn("streamUrl")
        `when`(streamUrl.path).thenReturn("/path/")
        `when`(sessionUri.getQueryParameter("aws.sessionId")).thenReturn("sessionId")
        `when`(sessionUrl.toString()).thenReturn("sessionUrlString")
        `when`(url.openConnection()).thenThrow(MalformedURLException())
        `when`(url.protocol).thenReturn("https")
        `when`(url.host).thenReturn("some.host.com/")
        `when`(huc.responseCode).thenReturn(200)
        `when`(huc.inputStream).thenReturn(expectedResponseJson!!.byteInputStream())
        `when`(huc.outputStream).thenReturn(hucOutputStream)
        PowerMockito.whenNew(URL::class.java)
            .withArguments("mt_session/path/")
            .thenReturn(url)
        PowerMockito.whenNew(URL::class.java)
            .withArguments("https://some.host.com/manifestUrl")
            .thenReturn(sessionUrl)
        PowerMockito.whenNew(DataOutputStream::class.java)
            .withArguments(hucOutputStream)
            .thenReturn(outputStream)
        mockkStatic(Uri::class)
        every { Uri.parse("streamUrl") } returns streamUrl
        every { Uri.parse("sessionUrlString") } returns sessionUri

        val result = AdUtils.getVideoManifest(videoStream, stream, config)

        assertEquals("Exception during getVideoManifest(stream)", result!!.error!!.message)

    }

    @Test
    fun getVideoManifestWithUrlStringReturnsExpectedVideoAdData() {
        val urlString = "/v1/master"
        val stream = mock(Stream::class.java)
        val streamUrl = mock(Uri::class.java)
        val url: URL =
            mock(URL::class.java, withSettings().defaultAnswer(Answers.RETURNS_SMART_NULLS))
        val sessionUrl: URL =
            mock(URL::class.java, withSettings().defaultAnswer(Answers.RETURNS_SMART_NULLS))
        val huc = mock(HttpURLConnection::class.java)
        val expectedResponse = PostObject("manifestUrl", "trackingUrl")
        val sessionUri = mock(Uri::class.java)
        val config =
            ArcMediaPlayerConfig.Builder().addAdParam("key", "value").setUserAgent("userAgent")
                .build()
        val expectedPostData =
            "{\"adsParams\":{\"key\":\"value\"}}".toByteArray(StandardCharsets.UTF_8)
        val expectedResponseJson = toJson(expectedResponse)!!
        val hucOutputStream = mock(DataOutputStream::class.java)
        val outputStream = mock(DataOutputStream::class.java)

        `when`(videoStream.additionalProperties?.advertising!!.enableAdInsertion).thenReturn(true)
        `when`(videoStream.additionalProperties?.advertising!!.adInsertionUrls!!.mt_master).thenReturn(
            "mt_master"
        )
        `when`(videoStream.additionalProperties?.advertising!!.adInsertionUrls!!.mt_session).thenReturn(
            "mt_session"
        )
        `when`(stream.url).thenReturn("streamUrl")
        `when`(streamUrl.path).thenReturn("/path/")
        `when`(sessionUri.getQueryParameter("aws.sessionId")).thenReturn("sessionId")
        `when`(sessionUrl.toString()).thenReturn("sessionUrlString")
        `when`(url.openConnection()).thenReturn(huc)
        `when`(url.protocol).thenReturn("https")
        `when`(url.host).thenReturn("some.host.com/")
        `when`(huc.responseCode).thenReturn(200)
        `when`(huc.inputStream).thenReturn(expectedResponseJson.byteInputStream())
        `when`(huc.outputStream).thenReturn(hucOutputStream)
        PowerMockito.whenNew(URL::class.java)
            .withArguments("/v1/session")
            .thenReturn(url)
        PowerMockito.whenNew(URL::class.java)
            .withArguments("https://some.host.com/manifestUrl")
            .thenReturn(sessionUrl)
        PowerMockito.whenNew(DataOutputStream::class.java)
            .withArguments(hucOutputStream)
            .thenReturn(outputStream)
        mockkStatic(Uri::class)
        every { Uri.parse("streamUrl") } returns streamUrl
        every { Uri.parse("sessionUrlString") } returns sessionUri

        val result = AdUtils.getVideoManifest(urlString = urlString, config)

        assertEquals("https://some.host.com/manifestUrl", result!!.manifestUrl)
        assertEquals("https://some.host.com/trackingUrl", result.trackingUrl)
        assertEquals("sessionId", result.sessionId)
        inOrder(url, huc, outputStream).run {
            verify(url).openConnection()
            verify(huc).requestMethod = "POST"
            verify(huc).setRequestProperty("User-Agent", "userAgent")
            verify(outputStream).write(expectedPostData)
            verify(outputStream).flush()
        }

    }

    @Test
    fun getVideoManifestWithUrlStringReturnsError() {
        val urlString = "/v1/master"
        val stream = mock(Stream::class.java)
        val streamUrl = mock(Uri::class.java)
        val url: URL =
            mock(URL::class.java, withSettings().defaultAnswer(Answers.RETURNS_SMART_NULLS))
        val sessionUrl: URL =
            mock(URL::class.java, withSettings().defaultAnswer(Answers.RETURNS_SMART_NULLS))
        val huc = mock(HttpURLConnection::class.java)
        val expectedResponse = PostObject("manifestUrl", "trackingUrl")
        val sessionUri = mock(Uri::class.java)
        val config =
            ArcMediaPlayerConfig.Builder().addAdParam("key", "value").setUserAgent("userAgent")
                .build()
        val expectedPostData =
            "{\"adsParams\":{\"key\":\"value\"}}".toByteArray(StandardCharsets.UTF_8)
        val expectedResponseJson = toJson(expectedResponse)!!
        val hucOutputStream = mock(DataOutputStream::class.java)
        val outputStream = mock(DataOutputStream::class.java)

        `when`(videoStream.additionalProperties?.advertising!!.enableAdInsertion).thenReturn(true)
        `when`(videoStream.additionalProperties?.advertising!!.adInsertionUrls!!.mt_master).thenReturn(
            "mt_master"
        )
        `when`(videoStream.additionalProperties?.advertising!!.adInsertionUrls!!.mt_session).thenReturn(
            "mt_session"
        )
        `when`(stream.url).thenReturn("streamUrl")
        `when`(streamUrl.path).thenReturn("/path/")
        `when`(sessionUri.getQueryParameter("aws.sessionId")).thenReturn("sessionId")
        `when`(sessionUrl.toString()).thenReturn("sessionUrlString")
        `when`(url.openConnection()).thenThrow(MalformedURLException())
        `when`(url.protocol).thenReturn("https")
        `when`(url.host).thenReturn("some.host.com/")
        `when`(huc.responseCode).thenReturn(200)
        `when`(huc.inputStream).thenReturn(expectedResponseJson.byteInputStream())
        `when`(huc.outputStream).thenReturn(hucOutputStream)
        PowerMockito.whenNew(URL::class.java)
            .withArguments("/v1/session")
            .thenReturn(url)
        PowerMockito.whenNew(URL::class.java)
            .withArguments("https://some.host.com/manifestUrl")
            .thenReturn(sessionUrl)
        PowerMockito.whenNew(DataOutputStream::class.java)
            .withArguments(hucOutputStream)
            .thenReturn(outputStream)
        mockkStatic(Uri::class)
        every { Uri.parse("streamUrl") } returns streamUrl
        every { Uri.parse("sessionUrlString") } returns sessionUri


        val result = AdUtils.getVideoManifest(urlString = urlString, config)
        assertEquals("Exception during getVideoManifest(string)", result!!.error!!.message)

    }

    @Test
    fun `getVideoManifest returns error when required data is not present`() {
        val stream = mock(Stream::class.java)
        val config =
            ArcMediaPlayerConfig.Builder().addAdParam("key", "value").setUserAgent("userAgent")
                .build()
        `when`(videoStream.additionalProperties?.advertising!!.enableAdInsertion).thenReturn(null)

        val result = AdUtils.getVideoManifest(videoStream, stream, config)

        assertEquals("Error in ad insertion block", result!!.error!!.message)
    }

    @Test
    fun `getAvails returns avails from url text`() {
        val server = MockWebServer()
        val expectedAvails = AvailList(emptyList())
        val baseUrl = server.url("/")
        server.enqueue(
            MockResponse().setBody(toJson(expectedAvails)!!)
        )

        val actualAvails = AdUtils.getAvails(baseUrl.toString())

        assertEquals(expectedAvails, actualAvails)
    }

    @Test
    fun getAvailsLogsError() {
        val server = MockWebServer()
        val expectedAvails = "not an avail list"
        val baseUrl = server.url("/")
        mockkStatic(Log::class)
        server.enqueue(
            MockResponse().setBody(toJson(expectedAvails)!!)
        )

        val actualAvails = AdUtils.getAvails(baseUrl.toString())

        assertNull(actualAvails)
        io.mockk.verify {
            Log.e("ArcVideoSDK", "getAvails Exception")
        }
    }

//    @Test
//    fun `callBeaconUrlReturnsExpected`()  {
//        val server = MockWebServer()
//        val expectedResponse = "beacon url text"
//        val baseUrl = server.url("/")
//        server.enqueue(
//            MockResponse().setBody(expectedResponse))
//
//        val actualResponse = callBeaconUrl(baseUrl.toString())
//
//        assertEquals(expectedResponse, actualResponse)
//    } //we are discarding response, unsure how to test this

    @Test
    fun `getOMResponse returns text from url`() {
        val server = MockWebServer()
        val expectedResponse = "om response text"
        val baseUrl = server.url("/")
        server.enqueue(
            MockResponse().setBody(expectedResponse)
        )

        val actualResponse = AdUtils.getOMResponse(baseUrl.toString())

        assertEquals(expectedResponse, actualResponse)
    }
}
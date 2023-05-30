package com.arcxp.video.service

import android.net.Uri
import android.util.Log
import com.arcxp.commons.util.MoshiController.toJson
import com.arcxp.commons.util.Utils
import com.arcxp.video.ArcMediaPlayerConfig
import com.arcxp.video.model.ArcVideoStream
import com.arcxp.video.model.AvailList
import com.arcxp.video.model.PostObject
import com.arcxp.video.model.Stream
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import io.mockk.verifySequence
import junit.framework.TestCase.assertNull
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets


class AdUtilsTest {

    @RelaxedMockK
    private lateinit var videoStream: ArcVideoStream

    @RelaxedMockK
    private lateinit var dataOutputStream: DataOutputStream

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mockkObject(Utils)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `get video manifest returns expected video ad data`() {
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        val url = mockk<URL>(relaxed = true)
        val sessionUrl = mockk<URL>(relaxed = true)
        val huc = mockk<HttpURLConnection>(relaxed = true)
        val expectedResponse = PostObject("manifestUrl", "trackingUrl")
        val sessionUri = mockk<Uri>()
        val config =
            ArcMediaPlayerConfig.Builder().addAdParam("key", "value").setUserAgent("userAgent")
                .build()
        val expectedPostData =
            "{\"adsParams\":{\"key\":\"value\"}}".toByteArray(StandardCharsets.UTF_8)
        val expectedResponseJson = toJson(expectedResponse)
        val hucOutputStream = mock(DataOutputStream::class.java)
        val outputStream = mock(DataOutputStream::class.java)

        every { videoStream.additionalProperties?.advertising!!.enableAdInsertion } returns true
        every { videoStream.additionalProperties?.advertising!!.adInsertionUrls!!.mt_master } returns
                "mt_master"
        every { videoStream.additionalProperties?.advertising!!.adInsertionUrls!!.mt_session } returns
                "mt_session"

        every { stream.url} returns "streamUrl"
        every { streamUrl.path} returns "/path/"
        every { sessionUri.getQueryParameter("aws.sessionId")} returns "sessionId"
        every { sessionUrl.toString()} returns "sessionUrlString"
        every { url.openConnection() } returns huc
        every { url.protocol } returns "https"
        every { url.host } returns "some.host.com/"
        every { huc.responseCode } returns 200
        every { huc.inputStream } returns expectedResponseJson!!.byteInputStream()
        every { huc.outputStream } returns hucOutputStream
        mockkObject(Utils)
        every { Utils.createURL(spec = "mt_session/path/") } returns url
        every { Utils.createURL(spec = "https://some.host.com/manifestUrl") } returns sessionUrl
        every { Utils.createOutputStream(outputStream = outputStream) } returns dataOutputStream
        mockkStatic(Uri::class)
        every { Uri.parse("streamUrl") } returns streamUrl
        every { Uri.parse("sessionUrlString") } returns sessionUri

        val result = AdUtils.getVideoManifest(videoStream, stream, config)

        assertEquals("https://some.host.com/manifestUrl", result!!.manifestUrl)
        assertEquals("https://some.host.com/trackingUrl", result.trackingUrl)
        assertEquals("sessionId", result.sessionId)
        verifySequence {
            url.openConnection()
            huc.requestMethod = "POST"
            huc.setRequestProperty("User-Agent", "userAgent")
            huc.outputStream
            huc.inputStream
            url.protocol
            url.host
            url.protocol
            url.host
            outputStream.write(expectedPostData)
            outputStream.flush()
        }
    }

    @Test
    fun `get video manifest returns error`() {
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        val url = mockk<URL>(relaxed = true)
        val sessionUrl = mockk<URL>(relaxed = true)
        val huc = mockk<HttpURLConnection>(relaxed = true)
        val expectedResponse = PostObject("manifestUrl", "trackingUrl")
        val sessionUri = mockk<Uri>()
        val config =
            ArcMediaPlayerConfig.Builder().addAdParam("key", "value").setUserAgent("userAgent")
                .build()
//        val expectedPostData =
//            "{\"adsParams\":{\"key\":\"value\"}}".toByteArray(StandardCharsets.UTF_8)
        val expectedResponseJson = toJson(expectedResponse)
        val hucOutputStream = mockk<DataOutputStream>()
        val outputStream = mockk<DataOutputStream>()

        every {
            videoStream.additionalProperties?.advertising!!.enableAdInsertion
        } returns true
        every {
            videoStream.additionalProperties?.advertising!!.adInsertionUrls!!.mt_master
        } returns "mt_master"

        every {
            videoStream.additionalProperties?.advertising!!.adInsertionUrls!!.mt_session
        } returns "mt_session"

        every { stream.url} returns "streamUrl"
        every { streamUrl.path} returns "/path/"
        every { sessionUri.getQueryParameter("aws.sessionId")} returns "sessionId"
        every { sessionUrl.toString()} returns "sessionUrlString"
//        every { url.openConnection() } returns huc
        every { url.protocol } returns "https"
        every { url.host } returns "some.host.com/"
        every { url.openConnection() } throws MalformedURLException()
        every { huc.responseCode } returns 200
        every { huc.inputStream } returns expectedResponseJson!!.byteInputStream()
        every { huc.outputStream } returns hucOutputStream
        mockkObject(Utils)
        every { Utils.createURL(spec = "mt_session/path/") } returns url
        every { Utils.createURL(spec = "https://some.host.com/manifestUrl") } returns sessionUrl
        every { Utils.createOutputStream(outputStream = outputStream) } returns dataOutputStream
        mockkStatic(Uri::class)
        every { Uri.parse("streamUrl") } returns streamUrl
        every { Uri.parse("sessionUrlString") } returns sessionUri

        val result = AdUtils.getVideoManifest(videoStream, stream, config)

        assertEquals(
            "Exception during getVideoManifest(stream)",
            result?.error?.message
        )

    }

    @Test
    fun `get video manifest with url string returns expected video ad data`() {
        val urlString = "/v1/master"
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        val url = mockk<URL>(relaxed = true)
        val sessionUrl = mockk<URL>(relaxed = true)
        val huc = mockk<HttpURLConnection>(relaxed = true)
        val expectedResponse = PostObject("manifestUrl", "trackingUrl")
        val sessionUri = mockk<Uri>()
        val config =
            ArcMediaPlayerConfig.Builder().addAdParam("key", "value")
                .setUserAgent("userAgent")
                .build()
        val expectedPostData =
            "{\"adsParams\":{\"key\":\"value\"}}".toByteArray(StandardCharsets.UTF_8)
        val expectedResponseJson = toJson(expectedResponse)
        val hucOutputStream = mockk<DataOutputStream>()

        every {
            videoStream.additionalProperties?.advertising!!.enableAdInsertion
        } returns true
        every {
            videoStream.additionalProperties?.advertising!!.adInsertionUrls!!.mt_master
        } returns "mt_master"
        every {
            videoStream.additionalProperties?.advertising!!.adInsertionUrls!!.mt_session
        } returns "mt_session"
        every { stream.url} returns "streamUrl"
        every { streamUrl.path} returns "/path/"
        every { sessionUri.getQueryParameter("aws.sessionId")} returns "sessionId"
        every { sessionUrl.toString()} returns "sessionUrlString"
        every { url.openConnection() } returns huc
        every { url.protocol } returns "https"
        every { url.host } returns "some.host.com/"
        every { huc.responseCode } returns 200
        every { huc.inputStream } returns expectedResponseJson!!.byteInputStream()
        every { huc.outputStream } returns hucOutputStream
        mockkObject(Utils)
        every { Utils.createURL(spec = "/v1/session") } returns url
        every { Utils.createURL(spec = "https://some.host.com/manifestUrl") } returns sessionUrl
        every { Utils.createOutputStream(outputStream = hucOutputStream) } returns dataOutputStream
        mockkStatic(Uri::class)
        every { Uri.parse("streamUrl") } returns streamUrl
        every { Uri.parse("sessionUrlString") } returns sessionUri

        val result = AdUtils.getVideoManifest(urlString = urlString, config)

        assertEquals(
            "https://some.host.com/manifestUrl",
            result!!.manifestUrl
        )
        assertEquals(
            "https://some.host.com/trackingUrl",
            result.trackingUrl
        )
        assertEquals("sessionId", result.sessionId)
        verifySequence{
            url.openConnection()
            huc.requestMethod = "POST"
            huc.setRequestProperty("User-Agent", "userAgent")
            huc.outputStream
            dataOutputStream.write(expectedPostData)
            dataOutputStream.flush()
            huc.inputStream
            url.protocol
            url.host
            url.protocol
            url.host
        }

    }

    @Test
    fun `get video manifest with url string returns error`() {
        val urlString = "/v1/master"
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        val url = mockk<URL>(relaxed = true)
        val sessionUrl = mockk<URL>(relaxed = true)
        val huc = mockk<HttpURLConnection>(relaxed = true)
        val expectedResponse = PostObject("manifestUrl", "trackingUrl")
        val sessionUri = mockk<Uri>()
        val config =
            ArcMediaPlayerConfig.Builder().addAdParam("key", "value")
                .setUserAgent("userAgent")
                .build()
        val expectedResponseJson = toJson(expectedResponse)!!
        val hucOutputStream = mock(DataOutputStream::class.java)
        val outputStream = mock(DataOutputStream::class.java)

        every {
            videoStream.additionalProperties?.advertising!!.enableAdInsertion
        } returns true
        every {
            videoStream.additionalProperties?.advertising!!.adInsertionUrls!!.mt_master
        } returns "mt_master"

        every {
            videoStream.additionalProperties?.advertising!!.adInsertionUrls!!.mt_session
        } returns "mt_session"

        every { stream.url} returns "streamUrl"
        every { streamUrl.path} returns "/path/"
        every { sessionUri.getQueryParameter("aws.sessionId")} returns "sessionId"
        every { sessionUrl.toString()} returns "sessionUrlString"
        every { url.openConnection() } throws MalformedURLException()
        every { url.protocol} returns "https"
        every { url.host} returns "some.host.com/"
        every { huc.responseCode} returns 200
        every { huc.inputStream} returns expectedResponseJson.byteInputStream()
        every { huc.outputStream} returns hucOutputStream
        mockkObject(Utils)
        every { Utils.createURL(spec = "/v1/session") } returns url
        every { Utils.createURL(spec = "https://some.host.com/manifestUrl") } returns sessionUrl
        every { Utils.createOutputStream(outputStream = outputStream) } returns dataOutputStream
        mockkStatic(Uri::class)
        every { Uri.parse("streamUrl") } returns streamUrl
        every { Uri.parse("sessionUrlString") } returns sessionUri


        val result = AdUtils.getVideoManifest(
            urlString = urlString,
            config
        )
        assertEquals(
            "Exception during getVideoManifest(string)",
            result!!.error!!.message
        )

    }

    @Test
    fun `getVideoManifest returns error when required data is not present`() {
        val stream = mock(Stream::class.java)
        val config =
            ArcMediaPlayerConfig.Builder()
                .addAdParam("key", "value")
                .setUserAgent("userAgent")
                .build()
        every {
            videoStream.additionalProperties?.advertising!!.enableAdInsertion
        } returns null

        val result = AdUtils.getVideoManifest(
            videoStream,
            stream,
            config
        )

        assertEquals(
            "Error in ad insertion block",
            result!!.error!!.message
        )
    }

    @Test
    fun `getAvails returns avails from url text`() {
        val server = MockWebServer()
        val expectedAvails = AvailList(emptyList())
        val baseUrl = server.url("/")
        server.enqueue(
            MockResponse().setBody(toJson(expectedAvails)!!)
        )

        val actualAvails =
            AdUtils.getAvails(baseUrl.toString())

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

        val actualAvails =
            AdUtils.getAvails(baseUrl.toString())

        assertNull(actualAvails)
        verify {
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

        val actualResponse =
            AdUtils.getOMResponse(baseUrl.toString())

        assertEquals(expectedResponse, actualResponse)
    }
}
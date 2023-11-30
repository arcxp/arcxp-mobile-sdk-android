package com.arcxp.video.service

import android.net.Uri
import android.util.Log
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.commons.util.MoshiController
import com.arcxp.commons.util.MoshiController.toJson
import com.arcxp.commons.util.Utils
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.model.AdInsertionUrls
import com.arcxp.video.model.ArcVideoStream
import com.arcxp.video.model.AvailList
import com.arcxp.video.model.PostObject
import com.arcxp.video.model.Stream
import com.arcxp.video.service.AdUtils.Companion.callBeaconUrl
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import io.mockk.verifySequence
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTestOnTestScope
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.io.FileNotFoundException


@OptIn(ExperimentalCoroutinesApi::class)
class AdUtilsTest {

    @RelaxedMockK
    private lateinit var videoStream: ArcVideoStream

    @RelaxedMockK
    private lateinit var dataOutputStream: DataOutputStream

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mockkObject(Utils)
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 1
        every { Log.d(any(), any()) } returns 1
        mockkObject(DependencyFactory)
        every { DependencyFactory.ioDispatcher()} returns Dispatchers.Unconfined
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getVideoManifest(videoStream, stream, config) returns expected video ad data`() = runTest {
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        val url = mockk<URL>(relaxed = true)
        val sessionUrl = mockk<URL>(relaxed = true)
        val huc = mockk<HttpURLConnection>(relaxed = true)
        val expectedResponse = PostObject("manifestUrl", "trackingUrl")
        val sessionUri = mockk<Uri>()
        val config =
            ArcXPVideoConfig.Builder()
                .addAdParam("key", "value")
                .setUserAgent("userAgent")
                .enableLogging()
                .build()
        val expectedPostData =
            "{\"adsParams\":{\"key\":\"value\"}}".toByteArray(StandardCharsets.UTF_8)
        val expectedResponseJson = toJson(expectedResponse)
        val hucOutputStream = mock(DataOutputStream::class.java)
        val outputStream = mock(DataOutputStream::class.java)

        every { videoStream.additionalProperties?.advertising?.enableAdInsertion } returns true
        every { videoStream.additionalProperties?.advertising?.adInsertionUrls?.mt_master } returns
                "mt_master"
        every { videoStream.additionalProperties?.advertising?.adInsertionUrls?.mt_session } returns
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

        assertEquals("https://some.host.com/manifestUrl", result?.manifestUrl)
        assertEquals("https://some.host.com/trackingUrl", result?.trackingUrl)
        assertEquals("sessionId", result?.sessionId)
        verifySequence {
            Log.d("ArcVideoSDK", "Enable Ad Insertion = true.")
            Log.d("ArcVideoSDK", "mt_session = mt_session")
            Log.d("ArcVideoSDK", "Full URI=mt_session/path/")
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
            Log.d("ArcVideoSDK", "tracking url=https://some.host.com/trackingUrl \nmanifest url=https://some.host.com/manifestUrl.")
        }
    }

    @Test
    fun `getVideoManifest(videoStream, stream, config) returns expected video ad data, userAgent null`() = runTest {
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        val url = mockk<URL>(relaxed = true)
        val sessionUrl = mockk<URL>(relaxed = true)
        val huc = mockk<HttpURLConnection>(relaxed = true)
        val expectedResponse = PostObject("manifestUrl", "trackingUrl")
        val sessionUri = mockk<Uri>()
        val config =
            ArcXPVideoConfig.Builder()
                .addAdParam("key", "value")
                .build()
        val expectedPostData =
            "{\"adsParams\":{\"key\":\"value\"}}".toByteArray(StandardCharsets.UTF_8)
        val expectedResponseJson = toJson(expectedResponse)
        val hucOutputStream = mock(DataOutputStream::class.java)
        val outputStream = mock(DataOutputStream::class.java)

        every { videoStream.additionalProperties?.advertising?.enableAdInsertion } returns true
        every { videoStream.additionalProperties?.advertising?.adInsertionUrls?.mt_master } returns
                "mt_master"
        every { videoStream.additionalProperties?.advertising?.adInsertionUrls?.mt_session } returns
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

        assertEquals("https://some.host.com/manifestUrl", result?.manifestUrl)
        assertEquals("https://some.host.com/trackingUrl", result?.trackingUrl)
        assertEquals("sessionId", result?.sessionId)
        verifySequence {
            url.openConnection()
            huc.requestMethod = "POST"
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
    fun `getVideoManifest(videoStream, stream, config) returns expected video ad data, userAgent empty`() = runTest {
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        val url = mockk<URL>(relaxed = true)
        val sessionUrl = mockk<URL>(relaxed = true)
        val huc = mockk<HttpURLConnection>(relaxed = true)
        val expectedResponse = PostObject("manifestUrl", "trackingUrl")
        val sessionUri = mockk<Uri>()
        val config =
            ArcXPVideoConfig.Builder()
                .addAdParam("key", "value")
                .setUserAgent("")
                .build()
        val expectedPostData =
            "{\"adsParams\":{\"key\":\"value\"}}".toByteArray(StandardCharsets.UTF_8)
        val expectedResponseJson = toJson(expectedResponse)
        val hucOutputStream = mock(DataOutputStream::class.java)
        val outputStream = mock(DataOutputStream::class.java)

        every { videoStream.additionalProperties?.advertising?.enableAdInsertion } returns true
        every { videoStream.additionalProperties?.advertising?.adInsertionUrls?.mt_master } returns
                "mt_master"
        every { videoStream.additionalProperties?.advertising?.adInsertionUrls?.mt_session } returns
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

        assertEquals("https://some.host.com/manifestUrl", result?.manifestUrl)
        assertEquals("https://some.host.com/trackingUrl", result?.trackingUrl)
        assertEquals("sessionId", result?.sessionId)
        verifySequence {
            url.openConnection()
            huc.requestMethod = "POST"
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
    fun `getVideoManifest(videoStream, stream, config) returns expected video ad data, userAgent adParams empty`() = runTest {
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        val url = mockk<URL>(relaxed = true)
        val sessionUrl = mockk<URL>(relaxed = true)
        val huc = mockk<HttpURLConnection>(relaxed = true)
        val expectedResponse = PostObject("manifestUrl", "trackingUrl")
        val sessionUri = mockk<Uri>()
        val config =
            ArcXPVideoConfig.Builder().build()
        val expectedPostData =
            "{\"adsParams\":{\"key\":\"value\"}}".toByteArray(StandardCharsets.UTF_8)
        val expectedResponseJson = toJson(expectedResponse)
        val hucOutputStream = mock(DataOutputStream::class.java)
        val outputStream = mock(DataOutputStream::class.java)

        every { videoStream.additionalProperties?.advertising?.enableAdInsertion } returns true
        every { videoStream.additionalProperties?.advertising?.adInsertionUrls?.mt_master } returns
                "mt_master"
        every { videoStream.additionalProperties?.advertising?.adInsertionUrls?.mt_session } returns
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

        assertEquals("https://some.host.com/manifestUrl", result?.manifestUrl)
        assertEquals("https://some.host.com/trackingUrl", result?.trackingUrl)
        assertEquals("sessionId", result?.sessionId)
        verifySequence {
            url.openConnection()
            huc.requestMethod = "POST"
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
    fun `getVideoManifest(videoStream, stream, config) returns expected video ad data, adParams empty`() = runTest {
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        val url = mockk<URL>(relaxed = true)
        val sessionUrl = mockk<URL>(relaxed = true)
        val huc = mockk<HttpURLConnection>(relaxed = true)
        val expectedResponse = PostObject("manifestUrl", "trackingUrl")
        val sessionUri = mockk<Uri>()
        val config =
            ArcXPVideoConfig.Builder()
                .setUserAgent("userAgent")
                .build()
        val expectedPostData =
            "{\"adsParams\":{\"key\":\"value\"}}".toByteArray(StandardCharsets.UTF_8)
        val expectedResponseJson = toJson(expectedResponse)
        val hucOutputStream = mock(DataOutputStream::class.java)
        val outputStream = mock(DataOutputStream::class.java)

        every { videoStream.additionalProperties?.advertising?.enableAdInsertion } returns true
        every { videoStream.additionalProperties?.advertising?.adInsertionUrls?.mt_master } returns
                "mt_master"
        every { videoStream.additionalProperties?.advertising?.adInsertionUrls?.mt_session } returns
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

        assertEquals("https://some.host.com/manifestUrl", result?.manifestUrl)
        assertEquals("https://some.host.com/trackingUrl", result?.trackingUrl)
        assertEquals("sessionId", result?.sessionId)
        verifySequence {
            url.openConnection()
            huc.requestMethod = "POST"
            huc.setRequestProperty("User-Agent", "userAgent")
            huc.inputStream
            url.protocol
            url.host
            url.protocol
            url.host
            outputStream.write(expectedPostData)
            outputStream.flush()
        }
    }

//    @Test
//    fun `getVideoManifest(videoStream, stream, config) returns expected video ad data, postObject null`() = runTest {
//        val stream = mockk<Stream>()
//        val streamUrl = mockk<Uri>()
//        val url = mockk<URL>(relaxed = true)
//        val sessionUrl = mockk<URL>(relaxed = true)
//        val huc = mockk<HttpURLConnection>(relaxed = true)
//        val expectedResponse = PostObject("manifestUrl", "trackingUrl")
//        val sessionUri = mockk<Uri>()
//        val config =
//            ArcXPVideoConfig.Builder()
//                .setUserAgent("userAgent")
//                .enableLogging()
//                .build()
//        val expectedResponseJson = toJson(expectedResponse)
//        val hucOutputStream = mock(DataOutputStream::class.java)
//        val outputStream = mock(DataOutputStream::class.java)
//
//        every { videoStream.additionalProperties?.advertising?.enableAdInsertion } returns true
//        every { videoStream.additionalProperties?.advertising?.adInsertionUrls?.mt_master } returns
//                "mt_master"
//        every { videoStream.additionalProperties?.advertising?.adInsertionUrls?.mt_session } returns
//                "mt_session"
//
//        every { stream.url} returns "streamUrl"
//        every { streamUrl.path} returns "/path/"
//        every { sessionUri.getQueryParameter("aws.sessionId")} returns "sessionId"
//        every { sessionUrl.toString()} returns "sessionUrlString"
//        every { url.openConnection() } returns huc
//        every { url.protocol } returns "https"
//        every { url.host } returns "some.host.com/"
//        every { huc.responseCode } returns 200
//        every { huc.inputStream } returns expectedResponseJson!!.byteInputStream()
//        every { huc.outputStream } returns hucOutputStream
//        mockkObject(Utils)
//        every { Utils.createURL(spec = "mt_session/path/") } returns url
//        every { Utils.createURL(spec = "https://some.host.com/manifestUrl") } returns sessionUrl
//        every { Utils.createOutputStream(outputStream = outputStream) } returns dataOutputStream
//        mockkStatic(Uri::class)
//        every { Uri.parse("streamUrl") } returns streamUrl
//        every { Uri.parse("sessionUrlString") } returns sessionUri
//
//        mockkObject(MoshiController)
//        every { MoshiController.fromJson("{\"manifestUrl\":\"manifestUrl\",\"trackingUrl\":\"trackingUrl\"}", PostObject::class.java) } returns null
//
//        val result = AdUtils.getVideoManifest(videoStream, stream, config)
//
//        assertEquals("https://some.host.com/manifestUrl", result?.manifestUrl)
//        assertEquals("https://some.host.com/trackingUrl", result?.trackingUrl)
//        assertEquals("sessionId", result?.sessionId)
//    }

    @Test
    fun `getVideoManifest(videoStream, stream, config) returns error`() = runTest {
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        val url = mockk<URL>(relaxed = true)
        val sessionUrl = mockk<URL>(relaxed = true)
        val huc = mockk<HttpURLConnection>(relaxed = true)
        val expectedResponse = PostObject("manifestUrl", "trackingUrl")
        val sessionUri = mockk<Uri>()
        val config =
            ArcXPVideoConfig.Builder()
                .addAdParam("key", "value")
                .setUserAgent("userAgent")
                .build()
        val expectedResponseJson = toJson(expectedResponse)
        val hucOutputStream = mockk<DataOutputStream>()
        val outputStream = mockk<DataOutputStream>()

        every {
            videoStream.additionalProperties?.advertising?.enableAdInsertion
        } returns true
        every {
            videoStream.additionalProperties?.advertising?.adInsertionUrls?.mt_master
        } returns "mt_master"

        every {
            videoStream.additionalProperties?.advertising!!.adInsertionUrls?.mt_session
        } returns "mt_session"

        every { stream.url} returns "streamUrl"
        every { streamUrl.path} returns "/path/"
        every { sessionUri.getQueryParameter("aws.sessionId")} returns "sessionId"
        every { sessionUrl.toString()} returns "sessionUrlString"
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
    fun `getVideoManifest(videoStream, stream, config) outputStream throws exception`() = runTest {
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        val url = mockk<URL>(relaxed = true)
        val sessionUrl = mockk<URL>(relaxed = true)
        val huc = mockk<HttpURLConnection>(relaxed = true)
        val sessionUri = mockk<Uri>()
        val config =
            ArcXPVideoConfig.Builder().addAdParam("key", "value").setUserAgent("userAgent")
                .build()
        val hucOutputStream = mockk<DataOutputStream>()
        val outputStream = mockk<DataOutputStream>()

        every {
            videoStream.additionalProperties?.advertising?.enableAdInsertion
        } returns true
        every {
            videoStream.additionalProperties?.advertising?.adInsertionUrls?.mt_master
        } returns "mt_master"

        every {
            videoStream.additionalProperties?.advertising!!.adInsertionUrls?.mt_session
        } returns "mt_session"

        every { stream.url} returns "streamUrl"
        every { streamUrl.path} returns "/path/"
        every { sessionUri.getQueryParameter("aws.sessionId")} returns "sessionId"
        every { sessionUrl.toString()} returns "sessionUrlString"
        every { url.openConnection() } returns huc
        every { url.protocol } returns "https"
        every { url.host } returns "some.host.com/"
        every { huc.responseCode } returns 200

        every { huc.outputStream } returns hucOutputStream
        mockkObject(Utils)
        every { Utils.createURL(spec = "mt_session/path/") } returns url
        every { Utils.createURL(spec = "https://some.host.com/manifestUrl") } returns sessionUrl
        every { Utils.createOutputStream(outputStream = outputStream) } throws Exception()
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
    fun `getVideoManifest(videoStream, stream, config) inputStream throws exception`() = runTest {
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        val url = mockk<URL>(relaxed = true)
        val sessionUrl = mockk<URL>(relaxed = true)
        val huc = mockk<HttpURLConnection>(relaxed = true)
        val sessionUri = mockk<Uri>()
        val config =
            ArcXPVideoConfig.Builder().addAdParam("key", "value").setUserAgent("userAgent")
                .build()
        val hucOutputStream = mockk<DataOutputStream>()
        val outputStream = mockk<DataOutputStream>()

        every {
            videoStream.additionalProperties?.advertising?.enableAdInsertion
        } returns true
        every {
            videoStream.additionalProperties?.advertising?.adInsertionUrls?.mt_master
        } returns "mt_master"

        every {
            videoStream.additionalProperties?.advertising!!.adInsertionUrls?.mt_session
        } returns "mt_session"

        every { stream.url} returns "streamUrl"
        every { streamUrl.path} returns "/path/"
        every { sessionUri.getQueryParameter("aws.sessionId")} returns "sessionId"
        every { sessionUrl.toString()} returns "sessionUrlString"
        every { url.openConnection() } returns huc
        every { url.protocol } returns "https"
        every { url.host } returns "some.host.com/"
        every { huc.responseCode } returns 200

        coEvery { huc.inputStream } throws FileNotFoundException()

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
    fun `getVideoManifest(videoStream, stream, config) returns error, false enableAdInsertion`() = runTest {
        val stream = mockk<Stream>()
        val config =
            ArcXPVideoConfig.Builder().
            addAdParam("key", "value")
                .setUserAgent("userAgent")
                .enableLogging()
                .build()

        every {
            videoStream.additionalProperties?.advertising?.enableAdInsertion
        } returns false
        every {
            videoStream.additionalProperties?.advertising?.adInsertionUrls?.mt_master
        } returns "mt_master"

        every {
            videoStream.additionalProperties?.advertising?.adInsertionUrls?.mt_session
        } returns "mt_session"

        val result = AdUtils.getVideoManifest(videoStream, stream, config)

        assertEquals(
            "Error in ad insertion block",
            result?.error?.message
        )

        verify(exactly = 1) {
            Log.d("ArcVideoSDK", "Enable Ad Insertion = false.")
        }
    }

    @Test
    fun `getVideoManifest(videoStream, stream, config) returns error, false enableAdInsertion, null adInsertionUrls mt_master`() = runTest {
        val stream = mockk<Stream>()
        val config =
            ArcXPVideoConfig.Builder()
                .addAdParam("key", "value")
                .setUserAgent("userAgent")
                .enableLogging()
                .build()

        every {
            videoStream.additionalProperties?.advertising?.enableAdInsertion
        } returns false
        every {
            videoStream.additionalProperties?.advertising?.adInsertionUrls?.mt_master
        } returns null

        val result = AdUtils.getVideoManifest(videoStream, stream, config)

        assertEquals(
            "Error in ad insertion block",
            result?.error?.message
        )

        verify(exactly = 1) {
            Log.d("ArcVideoSDK", "Enable Ad Insertion = false.")
        }
    }

    @Test
    fun `getVideoManifest(videoStream, stream, config) returns error, null mt_master`() = runTest {
        val stream = mockk<Stream>()
        val config =
            ArcXPVideoConfig.Builder()
                .addAdParam("key", "value")
                .setUserAgent("userAgent")
                .build()

        every {
            videoStream.additionalProperties?.advertising?.enableAdInsertion
        } returns true
        every {
            videoStream.additionalProperties?.advertising?.adInsertionUrls?.mt_master
        } returns null

        val result = AdUtils.getVideoManifest(videoStream, stream, config)

        assertEquals(
            "Error in ad insertion block",
            result?.error?.message
        )
    }

    @Test
    fun `getVideoManifest(videoStream, stream, config) returns error, null adInsertionUrls mt_master`() = runTest {
        val stream = mockk<Stream>()
        val config =
            ArcXPVideoConfig.Builder()
                .addAdParam("key", "value")
                .setUserAgent("userAgent")
                .build()

        every {
            videoStream.additionalProperties?.advertising?.enableAdInsertion
        } returns true
        every {
            videoStream.additionalProperties?.advertising?.adInsertionUrls
        } returns AdInsertionUrls(null, "", "")

        val result = AdUtils.getVideoManifest(videoStream, stream, config)

        assertEquals(
            "Error in ad insertion block",
            result?.error?.message
        )
    }

    @Test
    fun `getVideoManifest(videoStream, stream, config) returns error, null advertising`() = runTest {
        val stream = mockk<Stream>()
        val config =
            ArcXPVideoConfig.Builder()
                .addAdParam("key", "value")
                .setUserAgent("userAgent")
                .enableLogging()
                .build()

        every {
            videoStream.additionalProperties?.advertising
        } returns null

        val result = AdUtils.getVideoManifest(videoStream, stream, config)

        assertEquals(
            "Error in ad insertion block",
            result?.error?.message
        )

        verify(exactly = 1) {
            Log.d("ArcVideoSDK", "Enable Ad Insertion = false.")
        }
    }

    @Test
    fun `getVideoManifest(videoStream, stream, config) returns error, null additionalProperties`() = runTest {
        val stream = mockk<Stream>()
        val config =
            ArcXPVideoConfig.Builder()
                .addAdParam("key", "value")
                .setUserAgent("userAgent")
                .enableLogging()
                .build()

        every {
            videoStream.additionalProperties
        } returns null

        val result = AdUtils.getVideoManifest(videoStream, stream, config)

        assertEquals(
            "Error in ad insertion block",
            result?.error?.message
        )

        verify(exactly = 1) {
            Log.d("ArcVideoSDK", "Enable Ad Insertion = false.")
        }
    }

    @Test
    fun `getVideoManifest(videoStream, stream, config) returns error, null adInsertionUrls`() = runTest {
        val stream = mockk<Stream>()
        val config =
            ArcXPVideoConfig.Builder()
                .addAdParam("key", "value")
                .setUserAgent("userAgent")
                .build()

        every {
            videoStream.additionalProperties?.advertising?.enableAdInsertion
        } returns true
        every {
            videoStream.additionalProperties?.advertising?.adInsertionUrls
        } returns null

        val result = AdUtils.getVideoManifest(videoStream, stream, config)

        assertEquals(
            "Error in ad insertion block",
            result?.error?.message
        )
    }

    @Test
    fun `getVideoManifest(urlString, config) returns expected video ad data`() = runTest {
        val urlString = "/v1/master"
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        val url = mockk<URL>(relaxed = true)
        val sessionUrl = mockk<URL>(relaxed = true)
        val huc = mockk<HttpURLConnection>(relaxed = true)
        val expectedResponse = PostObject("manifestUrl", "trackingUrl")
        val sessionUri = mockk<Uri>()
        val config =
            ArcXPVideoConfig.Builder().addAdParam("key", "value")
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
    fun `getVideoManifest(urlString, config) returns expected video ad data, postData null`() = runTest {
        val urlString = "/v1/master"
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        val url = mockk<URL>(relaxed = true)
        val sessionUrl = mockk<URL>(relaxed = true)
        val huc = mockk<HttpURLConnection>(relaxed = true)
        val expectedResponse = PostObject("manifestUrl", "trackingUrl")
        val sessionUri = mockk<Uri>()
        val config =
            ArcXPVideoConfig.Builder().addAdParam("key", "value")
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
    fun `getVideoManifest(urlString, config) returns error`() = runTest {
        val urlString = "/v1/master"
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        val url = mockk<URL>(relaxed = true)
        val sessionUrl = mockk<URL>(relaxed = true)
        val huc = mockk<HttpURLConnection>(relaxed = true)
        val expectedResponse = PostObject("manifestUrl", "trackingUrl")
        val sessionUri = mockk<Uri>()
        val config =
            ArcXPVideoConfig.Builder().addAdParam("key", "value")
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
    fun `getVideoManifest(videoStream, stream, config) returns error when required data is not present`() = runTest {
        val stream = mockk<Stream>()
        val config =
            ArcXPVideoConfig.Builder()
                .addAdParam("key", "value")
                .setUserAgent("userAgent")
                .build()
        every {
            videoStream.additionalProperties?.advertising?.enableAdInsertion
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
    fun `getServerSide ads succeeds`() = runTest {

        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        every {
            videoStream.additionalProperties?.advertising?.enableAdInsertion
        } returns true
        every {
            videoStream.additionalProperties?.advertising?.adInsertionUrls
        } returns AdInsertionUrls(mt_master = "mt_master", mt_root = "", mt_session = "")
        every {
            videoStream.additionalProperties?.advertising!!.adInsertionUrls!!.mt_master
        } returns "mt_master"
        every { stream.url} returns "streamUrl"
        every { streamUrl.path} returns "/path/"
        mockkStatic(Uri::class)
        every { Uri.parse("streamUrl") } returns streamUrl

        coEvery { Utils.createURLandReadText(spec = "mt_master/path/")} returns "something we discard"

        val result = AdUtils.enableServerSideAds(
            videoStream,
            stream
        )

        assertTrue(result)

        coVerify (exactly = 1) {
            Utils.createURLandReadText(spec = "mt_master/path/")
        }

    }

    @Test
    fun `getServerSide ads fails, null adInsertionUrls`() = runTest {
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        every {
            videoStream.additionalProperties?.advertising?.enableAdInsertion
        } returns true
        every {
            videoStream.additionalProperties?.advertising?.adInsertionUrls
        } returns null
        every { stream.url} returns "streamUrl"
        every { streamUrl.path} returns "/path/"
        mockkStatic(Uri::class)
        every { Uri.parse("streamUrl") } returns streamUrl

        val result = AdUtils.enableServerSideAds(
            videoStream,
            stream
        )

        assertFalse(result)

    }

    @Test
    fun `getServerSide ads fails, false enableAdInsertion`() = runTest {
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        every {
            videoStream.additionalProperties?.advertising?.enableAdInsertion
        } returns false
        every {
            videoStream.additionalProperties?.advertising?.adInsertionUrls
        } returns AdInsertionUrls(mt_master = "mt_master", mt_root = "", mt_session = "")
        every { stream.url} returns "streamUrl"
        every { streamUrl.path} returns "/path/"
        mockkStatic(Uri::class)
        every { Uri.parse("streamUrl") } returns streamUrl

        val result = AdUtils.enableServerSideAds(
            videoStream,
            stream
        )

        assertFalse(result)

    }

    @Test
    fun `getServerSide ads fails, false enableAdInsertion null adInsertionUrls`() = runTest {
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        every {
            videoStream.additionalProperties?.advertising?.enableAdInsertion
        } returns false
        every {
            videoStream.additionalProperties?.advertising?.adInsertionUrls
        } returns null
        every { stream.url} returns "streamUrl"
        every { streamUrl.path} returns "/path/"
        mockkStatic(Uri::class)
        every { Uri.parse("streamUrl") } returns streamUrl

        val result = AdUtils.enableServerSideAds(
            videoStream,
            stream
        )

        assertFalse(result)

    }

    @Test
    fun `getServerSide ads fails, null addtionalProperties`() = runTest {
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        every {
            videoStream.additionalProperties
        } returns null
        every { stream.url} returns "streamUrl"
        every { streamUrl.path} returns "/path/"
        mockkStatic(Uri::class)
        every { Uri.parse("streamUrl") } returns streamUrl

        val result = AdUtils.enableServerSideAds(
            videoStream,
            stream
        )

        assertFalse(result)

    }

    @Test
    fun `getServerSide ads fails, null advertising`() = runTest {
        val stream = mockk<Stream>()
        val streamUrl = mockk<Uri>()
        every {
            videoStream.additionalProperties?.advertising
        } returns null
        every { stream.url} returns "streamUrl"
        every { streamUrl.path} returns "/path/"
        mockkStatic(Uri::class)
        every { Uri.parse("streamUrl") } returns streamUrl

        val result = AdUtils.enableServerSideAds(
            videoStream,
            stream
        )

        assertFalse(result)

    }

    @Test
    fun `getAvails returns avails from url text`() = runTest {
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
    fun getAvailsLogsError() = runTest {
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

    @Test
    fun `getOMResponse returns text from url`() = runTest {
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

    @Test
    fun `callBeaconUrl calls endpoint`() = runTest {
        coEvery { Utils.createURLandReadText(spec = "url")} returns "something we discard"

        callBeaconUrl("url")

        coVerify (exactly = 1) {
            Utils.createURLandReadText(spec = "url")
        }

    }
}
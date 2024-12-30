package com.arcxp.video.service

import android.net.Uri
import android.util.Log
import com.arcxp.commons.util.Constants.SDK_TAG
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.commons.util.MoshiController.fromJson
import com.arcxp.commons.util.Utils
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.DataOutputStream
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * AdUtils is an internal utility class responsible for handling advertisement-related functionalities within the ArcXP platform.
 * It provides methods to enable server-side ads, retrieve video manifests, handle ad beacons, and create ad objects.
 * The class uses coroutines for asynchronous operations and integrates with various components such as ArcXPVideoConfig and ArcVideoStream.
 *
 * The class defines the following methods:
 * - enableServerSideAds: Enables server-side ads for a given video stream.
 * - getVideoManifest: Retrieves the video manifest for ad insertion.
 * - callPostAsync: Makes an asynchronous POST request to retrieve ad data.
 * - getAvails: Retrieves the list of available ads.
 * - callBeaconUrl: Sends a beacon request to a specified URL.
 * - createArcAd: Creates an ArcAd object from the given ad data.
 *
 * Usage:
 * - Use the provided static methods to manage ad-related functionalities.
 *
 * Example:
 *
 * val videoStream = ArcVideoStream(...)
 * val stream = Stream(...)
 * val config = ArcXPVideoConfig(...)
 * val adEnabled = AdUtils.enableServerSideAds(videoStream, stream)
 * val videoAdData = AdUtils.getVideoManifest(videoStream, stream, config)
 *
 * Note: This class is intended for internal use only and should not be exposed publicly.
 *
 * @method enableServerSideAds Enables server-side ads for a given video stream.
 * @method getVideoManifest Retrieves the video manifest for ad insertion.
 * @method callPostAsync Makes an asynchronous POST request to retrieve ad data.
 * @method getAvails Retrieves the list of available ads.
 * @method callBeaconUrl Sends a beacon request to a specified URL.
 * @method createArcAd Creates an ArcAd object from the given ad data.
 */
internal class AdUtils {

    companion object {

        private val mIoScope: CoroutineScope = DependencyFactory.createIOScope()

        @JvmStatic
        fun enableServerSideAds(videoStream: ArcVideoStream, stream: Stream): Boolean {
            if (videoStream.additionalProperties?.advertising?.enableAdInsertion == true
                && videoStream.additionalProperties.advertising.adInsertionUrls != null
            ) {
                val masterUri =
                    videoStream.additionalProperties.advertising.adInsertionUrls.mt_master
                val streamUrl = Uri.parse(stream.url)
                val fullUri = masterUri + streamUrl.path
                mIoScope.launch {
                    Utils.createURLandReadText(spec = fullUri)
                }
                return true
            }
            return false
        }

        @JvmStatic
        fun getVideoManifest(
            videoStream: ArcVideoStream,
            stream: Stream,
            config: ArcXPVideoConfig
        ): VideoAdData? {
            if (config.isLoggingEnabled) Log.d(
                SDK_TAG,
                "Enable Ad Insertion = ${videoStream.additionalProperties?.advertising?.enableAdInsertion ?: false}."
            )
            if (videoStream.additionalProperties?.advertising?.enableAdInsertion == true
                && videoStream.additionalProperties.advertising.adInsertionUrls != null
                && videoStream.additionalProperties.advertising.adInsertionUrls.mt_master != null
            ) {

                val masterUri =
                    videoStream.additionalProperties.advertising.adInsertionUrls.mt_session

                if (config.isLoggingEnabled) Log.d(SDK_TAG, "mt_session = $masterUri")
                val streamUrl = Uri.parse(stream.url)
                val fullUri = masterUri + streamUrl.path

                if (config.isLoggingEnabled) Log.d(SDK_TAG, "Full URI=$fullUri")

                val url = Utils.createURL(spec = fullUri)
                var postObject: PostObject
                try {
                    runBlocking {
                        postObject = callPostAsync(url, config).await()
                    }
                } catch (e: Exception) {
                    return VideoAdData(error = Error(message = "Exception during getVideoManifest(stream)"))
                }

                val trackingUrl = url.protocol + "://" + url.host + postObject.trackingUrl

                val manifestUrl = url.protocol + "://" + url.host + postObject.manifestUrl

                if (config.isLoggingEnabled) Log.d(
                    SDK_TAG,
                    "tracking url=$trackingUrl \nmanifest url=$manifestUrl."
                )

                val sessionUrl = Utils.createURL(spec = manifestUrl)
                val sessionUri = Uri.parse(sessionUrl.toString())
                val sessionId = sessionUri.getQueryParameter("aws.sessionId")

                return VideoAdData(
                    manifestUrl = manifestUrl,
                    trackingUrl = trackingUrl,
                    sessionId = sessionId
                )
            }
            return VideoAdData(error = Error(message = "Error in ad insertion block"))
        }

        @JvmStatic
        fun getVideoManifest(urlString: String, config: ArcXPVideoConfig): VideoAdData? {
            val newUrl = urlString.replace("/v1/master", "/v1/session")
            val url = Utils.createURL(spec = newUrl)
            var postObject: PostObject
            try {
                runBlocking {
                    postObject = callPostAsync(url, config).await()
                }
            } catch (e: Exception) {
                return VideoAdData(error = Error(message = "Exception during getVideoManifest(string)"))
            }

            val trackingUrl = url.protocol + "://" + url.host + postObject.trackingUrl

            val manifestUrl = url.protocol + "://" + url.host + postObject.manifestUrl

            val sessionUrl = Utils.createURL(spec = manifestUrl)
            val sessionUri = Uri.parse(sessionUrl.toString())
            val sessionId = sessionUri.getQueryParameter("aws.sessionId")

            return VideoAdData(
                manifestUrl = manifestUrl,
                trackingUrl = trackingUrl,
                sessionId = sessionId
            )
        }

        private fun callPostAsync(url: URL, config: ArcXPVideoConfig): Deferred<PostObject> =
            mIoScope.async {
                var data = "{\"adsParams\":{"
                if (!config.adParams.isEmpty()) {
                    for (param in config.adParams.entries) {
                        data += "\"" + param.key + "\":\"" + param.value + "\","
                    }
                    data = data.dropLast(1)
                    data += "}}"
                } else {
                    data = ""
                }
                var line: String? = null
                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    if (!config.userAgent.isNullOrBlank()) {
                        setRequestProperty("User-Agent", config.userAgent)
                    }

                    if (!data.isBlank()) {
                        val postData: ByteArray =
                            data.toString().toByteArray(StandardCharsets.UTF_8)
                        try {
                            val outputStream: DataOutputStream =
                                Utils.createOutputStream(this.outputStream)
                            outputStream.write(postData)
                            outputStream.flush()
                        } catch (exception: Exception) {

                        }
                    }

                    try {
                        inputStream.bufferedReader().use {
                            line = it.readLine()
                        }
                    } catch (e: FileNotFoundException) {
                    }
                }
                return@async fromJson(line!!, PostObject::class.java)!!
            }

        @JvmStatic
        fun getAvails(trackingUrl: String): AvailList? {
            var avails: AvailList? = null
            runBlocking {
                try {
                    avails = getAvailsAsync(trackingUrl).await()
                } catch (e: Exception) {
                    Log.e(SDK_TAG, "getAvails Exception")
                }
            }
            return avails
        }

        private fun getAvailsAsync(trackingUrl: String): Deferred<AvailList?> = mIoScope.async {
            var avails: AvailList?
            val line = Utils.createURL(spec = trackingUrl).readText()
            Log.e(SDK_TAG, "$line")
            avails = fromJson(line, AvailList::class.java)
            avails
        }

        @JvmStatic
        fun callBeaconUrl(url: String) {
            mIoScope.launch {
                Utils.createURLandReadText(spec = url)
            }
        }

        @JvmStatic
        fun createArcAd(mCurrentAd: ArcAd): ArcAd? {
            return mCurrentAd.adId?.let { adId ->
                mCurrentAd.adDuration?.let { adDuration ->
                    mCurrentAd.adTitle?.let { adTitle ->
                        mCurrentAd.clickthroughUrl?.let { clickthroughUrl ->
                            ArcAd(adId, adDuration, adTitle, clickthroughUrl)
                        }
                    }
                }
            }
        }
    }

}
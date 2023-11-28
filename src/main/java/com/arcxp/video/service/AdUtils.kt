package com.arcxp.video.service

import android.net.Uri
import android.util.Log
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
 * @suppress
 */
class AdUtils {

    companion object {
        private const val TAG = "ArcVideoSDK"

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
                enableServerSideAdsAsync(fullUri)
                return true
            }
            return false
        }

        private fun enableServerSideAdsAsync(urlString: String) =
            mIoScope.launch {
                Utils.createURLandReadText(spec = urlString)
            }

        @JvmStatic
        fun getVideoManifest(
            videoStream: ArcVideoStream,
            stream: Stream,
            config: ArcXPVideoConfig
        ): VideoAdData? {
            if (config.isLoggingEnabled) Log.d(
                TAG,
                "Enable Ad Insertion = ${videoStream.additionalProperties?.advertising?.enableAdInsertion}."
            )
            if (videoStream.additionalProperties?.advertising?.enableAdInsertion == true
                && videoStream.additionalProperties.advertising.adInsertionUrls != null
                && videoStream.additionalProperties.advertising.adInsertionUrls.mt_master != null
            ) {

                val masterUri =
                    videoStream.additionalProperties.advertising.adInsertionUrls.mt_session

                if (config.isLoggingEnabled) Log.d(TAG, "mt_session = $masterUri")
                val streamUrl = Uri.parse(stream.url)
                val fullUri = masterUri + streamUrl.path

                if (config.isLoggingEnabled) Log.d(TAG, "Full URI=$fullUri")

                val url = Utils.createURL(spec = fullUri)
                var postObject: PostObject? = null
                try {
                    runBlocking {
                        postObject = callPostAsync(url, config).await()
                    }
                } catch (e: Exception) {
                    return VideoAdData(error = Error(message = "Exception during getVideoManifest(stream)"))
                }

                val trackingUrl = url.protocol + "://" + url.host + postObject?.trackingUrl

                val manifestUrl = url.protocol + "://" + url.host + postObject?.manifestUrl

                if (config.isLoggingEnabled) Log.d(
                    TAG,
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
            var postObject: PostObject? = null
            try {
                runBlocking {
                    postObject = callPostAsync(url, config).await()
                }
            } catch (e: Exception) {
                return VideoAdData(error = Error(message = "Exception during getVideoManifest(string)"))
            }

            val trackingUrl = url.protocol + "://" + url.host + postObject?.trackingUrl

            val manifestUrl = url.protocol + "://" + url.host + postObject?.manifestUrl

            val sessionUrl = Utils.createURL(spec = manifestUrl)
            val sessionUri = Uri.parse(sessionUrl.toString())
            val sessionId = sessionUri.getQueryParameter("aws.sessionId")

            return VideoAdData(
                manifestUrl = manifestUrl,
                trackingUrl = trackingUrl,
                sessionId = sessionId
            )
        }

        private fun callPostAsync(url: URL, config: ArcXPVideoConfig): Deferred<PostObject?> =
            mIoScope.async {
                var postObject: PostObject? = null

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
                            val outputStream: DataOutputStream = Utils.createOutputStream(this.outputStream)
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
                postObject = fromJson(line!!, PostObject::class.java)
                postObject
            }

        @JvmStatic
        fun getAvails(trackingUrl: String): AvailList? {
            var avails: AvailList? = null
            runBlocking {
                try {
                    avails = getAvailsAsync(trackingUrl).await()
                } catch (e: Exception) {
                    Log.e(TAG, "getAvails Exception")
                }
            }
            return avails
        }

        private fun getAvailsAsync(trackingUrl: String): Deferred<AvailList?> = mIoScope.async {
            var avails: AvailList? = null
            val line = Utils.createURL(spec = trackingUrl).readText()
            Log.e(TAG, "$line")

            avails = fromJson(line, AvailList::class.java)

            avails
        }

        @JvmStatic
        fun callBeaconUrl(url: String) {
            callBeaconUrlAsync(url)
        }


        private fun callBeaconUrlAsync(urlstring: String) = mIoScope.launch {
            Utils.createURLandReadText(spec = urlstring)
        }

        @JvmStatic
        fun getOMResponse(url: String): String? {
            var response: String? = null
            runBlocking {
                response = getOMResponseAsync(url).await()
            }
            return response
        }

        private fun getOMResponseAsync(url: String): Deferred<String?> = mIoScope.async {
            val response = Utils.createURLandReadText(spec = url)
            response
        }

    }

}
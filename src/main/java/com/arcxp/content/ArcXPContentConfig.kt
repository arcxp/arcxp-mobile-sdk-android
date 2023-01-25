package com.arcxp.content

import androidx.annotation.Keep
import com.arcxp.content.sdk.models.ArcXPContentException
import com.arcxp.content.sdk.models.ArcXPContentSDKErrorType
import com.arcxp.content.sdk.util.Constants.DEFAULT_CACHE_SIZE_MB
import com.arcxp.content.sdk.util.Constants.DEFAULT_PRELOADING
import com.arcxp.content.sdk.util.Constants.CACHE_TIME_UNTIL_UPDATE_MIN
import com.arcxp.content.sdk.util.Constants.VALID_CACHE_SIZE_RANGE_MB

/**
 * ArcxpContentConfig Configuration file for ArcxpContentSDK
 *
 * @property orgName Organization Name
 * @property siteName Site Name
 * @property environment Environment Name
 * @property baseUrl User Provided Base URL to use instead of building from [orgName], [siteName] and [environment]
 * the accessor method [contentBaseUrl] will prefer this value if is non null
 * @property cacheTimeUntilUpdateMinutes Time in minutes until we request new data
 * @property cacheSizeMB Size in megabytes to restrict cache.
 * Defaults to [DEFAULT_CACHE_SIZE_MB] megabytes if not specified
 * value is constrained in range [VALID_CACHE_SIZE_RANGE_MB]
 * @property preLoading Should we bulk load results of collection calls
 */
@Keep
class ArcXPContentConfig private constructor(
    val orgName: String,
    val siteName: String,
    val environment: String,
    val baseUrl: String,
    val navigationEndpoint: String,
    val videoCollectionName: String,
    val cacheTimeUntilUpdateMinutes: Int?,
    val cacheSizeMB: Int,
    val preLoading: Boolean
) {

    class Builder {
        private var orgName: String? = null
        private var siteName: String? = null
        private var environment: String? = null
        private var baseUrl: String? = null
        private var navigationEndpoint: String? = null
        private var videoCollectionName: String? = null
        private var cacheSize: Int? = null
        private var cacheTimeUntilUpdate: Int? = null
        private var preLoading: Boolean? = null

        fun setOrgName(name: String): Builder {
            this.orgName = name
            return this
        }

        fun setEnvironment(env: String): Builder {
            this.environment = env
            return this
        }

        fun setSite(site: String): Builder {
            this.siteName = site
            return this
        }

        fun setBaseUrl(url: String): Builder {
            this.baseUrl = url
            return this
        }

        fun setNavigationEndpoint(endpoint: String): Builder {
            this.navigationEndpoint = endpoint
            return this
        }

        fun setVideoCollectionName(videoCollectionName: String): Builder {
            this.videoCollectionName = videoCollectionName
            return this
        }

        fun setCacheTimeUntilUpdate(minutes: Int): Builder {
            this.cacheTimeUntilUpdate = if(minutes > CACHE_TIME_UNTIL_UPDATE_MIN) minutes else CACHE_TIME_UNTIL_UPDATE_MIN
            return this
        }

        fun setCacheSize(sizeInMB: Int): Builder {
            this.cacheSize = sizeInMB.coerceIn(VALID_CACHE_SIZE_RANGE_MB)
            return this
        }

        fun setPreloading(preLoading: Boolean): Builder {
            this.preLoading = preLoading
            return this
        }

        /**
         * members environment, org, site must not be null
         * the remaining values are defaulted
         * @return [ArcXPContentConfig]
         */
        fun build(): ArcXPContentConfig {
            when {
                baseUrl.isNullOrBlank() -> {
                    throw ArcXPContentException(
                        ArcXPContentSDKErrorType.INIT_ERROR,
                        "Failed Initialization: SDK needs values for baseUrl for backend communication"
                    )
                }
                siteName.isNullOrBlank() or orgName.isNullOrBlank()-> {
                    throw ArcXPContentException(
                        ArcXPContentSDKErrorType.INIT_ERROR,
                        "Failed Initialization: SDK needs values for org and site for analytics/logging"
                    )
                }
                navigationEndpoint.isNullOrBlank() -> {
                    throw ArcXPContentException(ArcXPContentSDKErrorType.CONFIG_ERROR, "Failed Initialization: SDK Needs navigationEndpoint value for site service")
                }
                else -> return ArcXPContentConfig(
                    orgName = orgName!!,
                    siteName = siteName!!,
                    environment = environment ?: "",
                    baseUrl = baseUrl!!,
                    navigationEndpoint = navigationEndpoint!!,
                    cacheTimeUntilUpdateMinutes = cacheTimeUntilUpdate,
                    cacheSizeMB = cacheSize ?: DEFAULT_CACHE_SIZE_MB,
                    preLoading = preLoading ?: DEFAULT_PRELOADING,
                    videoCollectionName = videoCollectionName ?: ""
                )
            }
        }
    }
}
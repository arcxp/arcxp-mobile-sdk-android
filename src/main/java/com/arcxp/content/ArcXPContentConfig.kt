package com.arcxp.content

import androidx.annotation.Keep
import com.arcxp.ArcXPMobileSDK.application
import com.arcxp.ArcXPMobileSDK.baseUrl
import com.arcxp.ArcXPMobileSDK.environment
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.Constants.DEFAULT_CACHE_SIZE_MB
import com.arcxp.commons.util.Constants.DEFAULT_PRELOADING
import com.arcxp.commons.util.Constants.CACHE_TIME_UNTIL_UPDATE_MIN
import com.arcxp.commons.util.Constants.VALID_CACHE_SIZE_RANGE_MB
import com.arcxp.commons.util.DependencyFactory.createArcXPError
import com.arcxp.commons.util.DependencyFactory.createArcXPException
import com.arcxp.sdk.R

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
    val navigationEndpoint: String,
    val videoCollectionName: String,
    val cacheTimeUntilUpdateMinutes: Int?,
    val cacheSizeMB: Int,
    val preLoading: Boolean
) {

    class Builder {
        private var navigationEndpoint: String? = null
        private var videoCollectionName: String? = null
        private var cacheSize: Int? = null
        private var cacheTimeUntilUpdate: Int? = null
        private var preLoading: Boolean? = null

        fun setNavigationEndpoint(endpoint: String): Builder {
            this.navigationEndpoint = endpoint
            return this
        }

        fun setVideoCollectionName(videoCollectionName: String): Builder {
            this.videoCollectionName = videoCollectionName
            return this
        }

        fun setCacheTimeUntilUpdate(minutes: Int): Builder {
            this.cacheTimeUntilUpdate =
                if (minutes > CACHE_TIME_UNTIL_UPDATE_MIN) minutes else CACHE_TIME_UNTIL_UPDATE_MIN
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
                navigationEndpoint.isNullOrBlank() -> {
                    throw createArcXPError(
                        type = ArcXPSDKErrorType.CONFIG_ERROR,
                        message = application().getString(R.string.init_failure_navigation_endpoint)
                    )
                }
                else -> return ArcXPContentConfig(
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
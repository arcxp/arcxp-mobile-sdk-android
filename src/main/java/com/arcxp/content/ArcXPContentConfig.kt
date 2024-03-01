package com.arcxp.content

import androidx.annotation.Keep
import com.arcxp.commons.util.Constants.CACHE_TIME_UNTIL_UPDATE_MIN
import com.arcxp.commons.util.Constants.DEFAULT_CACHE_SIZE_MB
import com.arcxp.commons.util.Constants.DEFAULT_PRELOADING
import com.arcxp.commons.util.Constants.VALID_CACHE_SIZE_RANGE_MB

/**
 * ArcxpContentConfig Configuration file for ArcxpContentSDK
 *
 * @property cacheTimeUntilUpdateMinutes Time in minutes until we request new data
 * @property cacheSizeMB Size in megabytes to restrict cache.
 * Defaults to [DEFAULT_CACHE_SIZE_MB] megabytes if not specified
 * value is constrained in range [VALID_CACHE_SIZE_RANGE_MB]
 * @property preLoading Should we bulk load results of collection calls
 */
@Keep
class ArcXPContentConfig private constructor(
    val cacheTimeUntilUpdateMinutes: Int?,
    val cacheSizeMB: Int,
    val preLoading: Boolean
) {
    class Builder {

        private var cacheSize: Int? = null
        private var cacheTimeUntilUpdate: Int? = null
        private var preLoading: Boolean? = null

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
        fun build() = ArcXPContentConfig(
                    cacheTimeUntilUpdateMinutes = cacheTimeUntilUpdate,
                    cacheSizeMB = cacheSize ?: DEFAULT_CACHE_SIZE_MB,
                    preLoading = preLoading ?: DEFAULT_PRELOADING,
                )


    }
}
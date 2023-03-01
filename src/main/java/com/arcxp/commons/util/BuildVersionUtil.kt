package com.arcxp.commons.util

import android.os.Build
import com.arcxp.sdk.BuildConfig

/**
 * @suppress
 */
interface BuildVersionProvider {
    fun sdkInt(): Int
    fun manufacturer(): String
    fun model(): String

    fun debug(): Boolean
}

/**
 * @suppress
 */
class BuildVersionProviderImpl : BuildVersionProvider {
    override fun sdkInt(): Int {
        return Build.VERSION.SDK_INT
    }
    override fun manufacturer(): String = Build.MANUFACTURER
    override fun model(): String = Build.MODEL

    override fun debug(): Boolean = BuildConfig.DEBUG
}
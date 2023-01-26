package com.arcxp.commons.util

import android.os.Build

/**
 * @suppress
 */
interface BuildVersionProvider {
    fun sdkInt(): String
    fun manufacturer(): String
    fun model(): String
}

/**
 * @suppress
 */
class BuildVersionProviderImpl : BuildVersionProvider {
    override fun sdkInt(): String {
        return Build.VERSION.SDK_INT.toString()
    }
    override fun manufacturer(): String = Build.MANUFACTURER
    override fun model(): String = Build.MODEL
}
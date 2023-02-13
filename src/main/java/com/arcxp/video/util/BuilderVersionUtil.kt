package com.arcxp.video.util

import android.os.Build

/**
 * @suppress
 */
interface BuildVersionProvider {
    fun sdkInt(): Int
    fun manufacturer(): String
    fun model(): String
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
}
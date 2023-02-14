package com.arcxp.commerce.util

import com.arcxp.commerce.ArcXPCommerceSDKErrorType
import kotlinx.coroutines.Dispatchers

object DependencyProvider {
    fun ioDispatcher() = Dispatchers.IO

    fun createError(type: ArcXPCommerceSDKErrorType, message: String?, value: Any?) =
        ArcXPError(type = type, message = message ?: "", value = value)

}
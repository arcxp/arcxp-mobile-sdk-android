package com.arcxp.video.listeners

import androidx.annotation.Keep
import com.arcxp.commons.throwables.ArcXPSDKErrorType

@Keep
public interface ArcVideoSDKErrorListener {
    fun onError(errorType: ArcXPSDKErrorType, message: String, value: Any?)
}
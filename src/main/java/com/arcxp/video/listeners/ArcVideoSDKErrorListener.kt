package com.arcxp.video.listeners

import androidx.annotation.Keep
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.video.model.ArcVideoSDKErrorType

@Keep
public interface ArcVideoSDKErrorListener {
    fun onError(errorType: ArcVideoSDKErrorType, message: String, value: Any?)
}
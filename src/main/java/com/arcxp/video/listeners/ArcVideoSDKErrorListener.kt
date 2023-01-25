package com.arcxp.video.listeners

import androidx.annotation.Keep
import com.arc.arcvideo.model.ArcVideoSDKErrorType

@Keep
public interface ArcVideoSDKErrorListener {
    fun onError(errorType: ArcVideoSDKErrorType, message: String, value: Any?)
}
package com.arcxp.video.util

import android.util.Log
import com.arc.arcvideo.model.ArcVideoStream

//This extension allows us to use TAG in any class
val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }

fun Any.log(text: String) = Log.d(TAG, text)

fun ArcVideoStream.isLive() = "live" == videoType
package com.arcxp.video.cast

import android.content.Context
import androidx.mediarouter.app.MediaRouteActionProvider
import com.google.android.gms.cast.framework.CastSession

class ArcMediaRouteActionProvider(context: Context): MediaRouteActionProvider(context)

interface ArcCastSessionManagerListener {
    fun onSessionEnded(error: Int)
    fun onSessionResumed(wasSuspended: Boolean)
    fun onSessionStarted(sessionId: String)
    fun onSessionStarting()
    fun onSessionStartFailed(error: Int)
    fun onSessionEnding()
    fun onSessionResuming(sessionId: String)
    fun onSessionResumeFailed(error: Int)
    fun onSessionSuspended(reason: Int)
}
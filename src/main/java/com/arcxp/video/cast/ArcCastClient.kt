package com.arc.arcvideo.cast

import android.content.Context
import androidx.mediarouter.app.MediaRouteActionProvider
import com.google.android.gms.cast.framework.CastSession

/**
 * @suppress
 */
class ArcMediaRouteActionProvider(context: Context): MediaRouteActionProvider(context)

/**
 * @suppress
 */
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
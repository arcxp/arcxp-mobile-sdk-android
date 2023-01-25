package com.arcxp.video.listeners

import android.view.KeyEvent
import androidx.annotation.Keep

@Keep
interface ArcKeyListener {
    fun onKey(keyCode: Int, keyEvent: KeyEvent)
    fun onBackPressed()
}
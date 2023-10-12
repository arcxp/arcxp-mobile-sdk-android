package com.arcxp.video.players

import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.cast.ArcCastManager
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.util.TrackingHelper
import com.arcxp.video.util.Utils
import com.google.ads.interactivemedia.v3.api.AdEvent
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before

internal class PlayerListenerTest {

    @MockK
    private lateinit var trackingHelper: TrackingHelper
    @MockK
    private lateinit var playerState: PlayerState
    @MockK
    private lateinit var playerStateHelper: PlayerStateHelper
    @MockK
    private lateinit var mListener: VideoListener
    @MockK
    private lateinit var captionsManager: CaptionsManager
    @MockK
    private lateinit var mConfig: ArcXPVideoConfig
    @MockK
    private lateinit var arcCastManager: ArcCastManager
    @MockK
    private lateinit var utils: Utils
    @MockK
    private lateinit var adEventListener: AdEvent.AdEventListener
    @MockK
    private lateinit var videoPlayer: ArcVideoPlayer
    
    
    private lateinit var testObject: PlayerListener
    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
    }
}
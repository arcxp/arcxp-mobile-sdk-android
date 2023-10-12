package com.arcxp.video.players

import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.util.TrackingHelper
import com.arcxp.video.util.Utils
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before

internal class PlayerStateHelperTest {

    @MockK
    private lateinit var playerState: PlayerState

    @MockK
    private lateinit var trackingHelper: TrackingHelper

    @MockK
    private lateinit var utils: Utils

    @MockK
    private lateinit var mListener: VideoListener

    @MockK
    private lateinit var captionsManager: CaptionsManager

    private lateinit var testObject: PlayerStateHelper

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
    }
}
package com.arcxp.video.players

import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.util.Utils
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Before

internal class CaptionsManagerTest {

    @MockK
    private lateinit var playerState: PlayerState

    @MockK
    private lateinit var utils: Utils

    @MockK
    private lateinit var mConfig: ArcXPVideoConfig

    @MockK
    private lateinit var mListener: VideoListener


    private  lateinit var testObject: CaptionsManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
    }
}
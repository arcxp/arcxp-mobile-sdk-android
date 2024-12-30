package com.arcxp.video

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ArcXPVideoConfigTest {

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `addAdParam builder passes values to object correctly`() {
        val actual = ArcXPVideoConfig.Builder()
            .addAdParam("adParam1key", "adParam1Value")
            .addAdParam("adParam2key", "adParam2Value")
            .addAdParam("adParam3key", "adParam3Value")
            .build()

        assertEquals(3, actual.adParams.size)
        assertEquals("adParam1Value", actual.adParams["adParam1key"])
        assertEquals("adParam2Value", actual.adParams["adParam2key"])
        assertEquals("adParam3Value", actual.adParams["adParam3key"])
    }

    @Test
    fun `PreferredStreamType next cycles through correctly`() {
        assertTrue(
            ArcXPVideoConfig.PreferredStreamType.HLS.next()
                    == ArcXPVideoConfig.PreferredStreamType.TS
        )
        assertTrue(
            ArcXPVideoConfig.PreferredStreamType.TS.next()
                    == ArcXPVideoConfig.PreferredStreamType.MP4
        )
        assertTrue(
            ArcXPVideoConfig.PreferredStreamType.MP4.next()
                    == ArcXPVideoConfig.PreferredStreamType.GIF
        )
        assertTrue(
            ArcXPVideoConfig.PreferredStreamType.GIF.next()
                    == ArcXPVideoConfig.PreferredStreamType.GIFMP4
        )
        assertTrue(
            ArcXPVideoConfig.PreferredStreamType.GIFMP4.next()
                    == ArcXPVideoConfig.PreferredStreamType.HLS
        )

    }
}

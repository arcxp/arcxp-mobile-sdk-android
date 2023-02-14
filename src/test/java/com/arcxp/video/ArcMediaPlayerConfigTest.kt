package com.arcxp.video

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ArcMediaPlayerConfigTest {

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
        val actual = ArcMediaPlayerConfig.Builder()
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
            ArcMediaPlayerConfig.PreferredStreamType.HLS.next()
                    == ArcMediaPlayerConfig.PreferredStreamType.TS
        )
        assertTrue(
            ArcMediaPlayerConfig.PreferredStreamType.TS.next()
                    == ArcMediaPlayerConfig.PreferredStreamType.MP4
        )
        assertTrue(
            ArcMediaPlayerConfig.PreferredStreamType.MP4.next()
                    == ArcMediaPlayerConfig.PreferredStreamType.GIF
        )
        assertTrue(
            ArcMediaPlayerConfig.PreferredStreamType.GIF.next()
                    == ArcMediaPlayerConfig.PreferredStreamType.GIFMP4
        )
        assertTrue(
            ArcMediaPlayerConfig.PreferredStreamType.GIFMP4.next()
                    == ArcMediaPlayerConfig.PreferredStreamType.HLS
        )

    }
}

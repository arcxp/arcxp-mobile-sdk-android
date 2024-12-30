package com.arcxp.video.players

import com.arcxp.video.players.DefaultTrackFilter.ID_SUBTITLE_URL
import androidx.media3.common.Format
import androidx.media3.common.TrackGroup
import androidx.media3.exoplayer.source.TrackGroupArray
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


class DefaultTrackFilterTest {

    private lateinit var testObject: DefaultTrackFilter

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        testObject = DefaultTrackFilter()
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `filter given null format returns false`(){
        assertFalse(testObject.filter(null, mockk()))
    }

    @Test
    fun `filter given null format language returns false`(){
        val format = Format.Builder().setLanguage(null).build()

        assertFalse(testObject.filter(format, mockk()))
    }

    @Test
    fun `filter given non null format and format language, returns true when format id is not subtitle url`(){
        val format = Format.Builder().setLanguage("language").setId("not subtitle url").build()

        assertTrue(testObject.filter(format, mockk()))
    }
    @Test
    fun `filter returns true when format from a trackGroup is subtitle url`(){
        val format = Format.Builder()
                .setLanguage("language")
                .setId(ID_SUBTITLE_URL)
                .build()
        val anotherFormat = Format.Builder()
                .setLanguage("language")
                .setId(ID_SUBTITLE_URL)
                .build()
        val trackGroup = TrackGroup(anotherFormat)
        val trackGroups = TrackGroupArray(trackGroup)


        assertTrue(testObject.filter(format, trackGroups))
    }
    @Test
    fun `filter returns true when format from a trackGroup is not subtitle url, but language is null`(){
        val format = Format.Builder()
                .setLanguage("language")
                .setId(ID_SUBTITLE_URL)
                .build()
        val anotherFormat = Format.Builder()
                .setId("non subtitle")
                .build()
        val trackGroup = TrackGroup(anotherFormat)
        val trackGroups = TrackGroupArray(trackGroup)


        assertTrue(testObject.filter(format, trackGroups))
    }

    @Test
    fun `filter returns false when format from a trackGroup is not subtitle url, and language is non null`(){
        val format = Format.Builder()
                .setLanguage("language")
                .setId(ID_SUBTITLE_URL)
                .build()
        val anotherFormat = Format.Builder()
                .setId("non subtitle")
                .setLanguage("language")
                .build()
        val trackGroup = TrackGroup(anotherFormat)
        val trackGroups = TrackGroupArray(trackGroup)

        assertFalse(testObject.filter(format, trackGroups))
    }
}
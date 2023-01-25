package com.arc.arcvideo.util

import android.content.Context
import android.content.res.Resources
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.UnsupportedOperationException

class OmidJsLoaderTest {

    @RelaxedMockK lateinit var context: Context
    @RelaxedMockK lateinit var resources: Resources
    private val omSdkV1Json = "{\"type\": \"Video\",\"adConfigUrl\": \"\",\"adEnabled\": false }"
    private val inputStream: InputStream = ByteArrayInputStream(omSdkV1Json.toByteArray())


    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { context.resources } returns resources
    }

    @After
    fun tearDown() { clearAllMocks() }

    @Test
    fun `getOmidJs returns expected string `() {
        every { resources.openRawResource(any())} returns inputStream

        assertEquals(omSdkV1Json, OmidJsLoader.getOmidJs(context))
    }

    @Test
    fun `getOmidJs throws exception if omid resource not found`() {
        val mockInputStream = mockk<InputStream>()
        every { mockInputStream.available() } throws IOException()
        every { resources.openRawResource(any())} returns mockInputStream

        assertThrows("Yikes, omid resource not found", UnsupportedOperationException::class.java) {
            OmidJsLoader.getOmidJs(context)
        }
    }
}
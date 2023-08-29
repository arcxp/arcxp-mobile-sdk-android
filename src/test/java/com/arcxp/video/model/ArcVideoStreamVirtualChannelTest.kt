package com.arcxp.video.model

import android.app.Application
import com.arcxp.ArcXPMobileSDK
import com.arcxp.ArcXPMobileSDK.resizer
import com.arcxp.sdk.R
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArcVideoStreamVirtualChannelTest {

    @RelaxedMockK
    lateinit var application: Application


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(ArcXPMobileSDK)
        every { application.getString(R.string.resizer_key) } returns "resizer_key"
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `thumbnail in ArcVideoStreamVirtualChannel returns thumbnail url`() {
        val url = "url"
        every { resizer().createThumbnail(url) } returns "resizedToThumbnail"

        val testObject = createTestObject(null, listOf(Program(null, null, null, null, null, url, null)))

        val actual = testObject.thumbnail()

        assertEquals("resizedToThumbnail", actual)
    }

    @Test
    fun `thumbnail in ArcVideoStreamVirtualChannel returns empty string when programs is null`() {

        val testObject = createTestObject()

        val actual = testObject.thumbnail()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `thumbnail with ArcVideoStreamVirtualChannel, returns empty string when programs imageUrl is empty`() {

        val testObject = createTestObject(null, listOf(Program(null, null, null, null, null, null, null)))

        val actual = testObject.thumbnail()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `fallback with ArcVideoStreamVirtualChannel, returns original url string`() {
        val expected = "original url"
        val testObject = createTestObject(null, listOf(Program(null, null, null, null, null, expected, null)))

        val actual = testObject.fallback()

        assertEquals(expected, actual)
    }

    @Test
    fun `fallback with ArcVideoStreamVirtualChannel, promoImage is null and returns empty string`() {
        val testObject = createTestObject()

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `fallback with VideoVO, promoImage imageUrl is null and returns empty string`() {
        val testObject = createTestObject(null, listOf(Program(null, null, null, null, null, null, null)))

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }
}


private fun createTestObject(
    url: String? = null, programs: List<Program>? = null
) =
    ArcVideoStreamVirtualChannel(
        id = "id",
        null,
null,
null,
null,
null,
null,
url,
null,
programs,
null,
null,
null
)

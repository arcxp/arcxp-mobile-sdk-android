package com.arc.arcvideo.model

import android.app.Application
import com.arc.arcvideo.ArcXPVideoSDK
import com.arc.flagship.features.arcvideo.R
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ArcVideoStreamVirtualChannelTest {

    @RelaxedMockK
    lateinit var application: Application


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { application.getString(R.string.resizer_key) } returns "resizer_key"
    }

    @Test
    fun `thumbnail in ArcVideoStreamVirtualChannel returns thumbnail url`() {
        mockkObject(ArcXPVideoSDK)
        val url = "url"
        every { ArcXPVideoSDK.initialize(application, "base", "org", "env", "site") }
        every { ArcXPVideoSDK.resizer().createThumbnail(url) } returns "resizedToThumbnail"

        val testObject = createTestObject(null, listOf(Program(null, null, null, null, null, url, null)))

        val actual = testObject.thumbnail()

        Assert.assertEquals("resizedToThumbnail", actual)
    }

    @Test
    fun `thumbnail in ArcVideoStreamVirtualChannel returns empty string when programs is null`() {

        val testObject = createTestObject()

        val actual = testObject.thumbnail()

        Assert.assertTrue(actual.isEmpty())
    }

    @Test
    fun `thumbnail with ArcVideoStreamVirtualChannel, returns empty string when programs imageUrl is empty`() {

        val testObject = createTestObject(null, listOf(Program(null, null, null, null, null, null, null)))

        val actual = testObject.thumbnail()

        Assert.assertTrue(actual.isEmpty())
    }

    @Test
    fun `fallback with ArcVideoStreamVirtualChannel, returns original url string`() {
        mockkObject(ArcXPVideoSDK)
        val expected = "orginal url"
        every { ArcXPVideoSDK.initialize(application, "base", "org", "env", "site") }
        val testObject = createTestObject(null, listOf(Program(null, null, null, null, null, expected, null)))

        val actual = testObject.fallback()

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `fallback with ArcVideoStreamVirtualChannel, promoImage is null and returns empty string`() {
        val testObject = createTestObject()

        val actual = testObject.fallback()

        Assert.assertTrue(actual.isEmpty())
    }

    @Test
    fun `fallback with VideoVO, promoImage imageUrl is null and returns empty string`() {
        val testObject = createTestObject(null, listOf(Program(null, null, null, null, null, null, null)))

        val actual = testObject.fallback()

        Assert.assertTrue(actual.isEmpty())
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

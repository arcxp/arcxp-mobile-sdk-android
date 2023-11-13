package com.arcxp.video.model

import android.app.Application
import android.content.res.Resources
import com.arcxp.ArcXPMobileSDK
import com.arcxp.ArcXPMobileSDK.imageUtils
import com.arcxp.commons.image.CollectionImageUtil
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.sdk.R
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ArcVideoStreamVirtualChannelTest {

    @RelaxedMockK
    lateinit var application: Application

    @RelaxedMockK
    internal lateinit var imageUtils: CollectionImageUtil

    @RelaxedMockK
    lateinit var resources: Resources


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkObject(ArcXPMobileSDK)
        mockkStatic(Resources::class)
        mockkObject(CollectionImageUtil)
        every { Resources.getSystem() } returns resources
        every { application.getString(R.string.resizer_key) } returns "resizer_key"
        every { DependencyFactory.createImageUtil("", application) } returns imageUtils
        every { imageUtils() } returns imageUtils
        every { Resources.getSystem().displayMetrics.widthPixels } returns 100
        every { Resources.getSystem().displayMetrics.heightPixels } returns 100
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `thumbnail in ArcVideoStreamVirtualChannel returns thumbnail url`() {
        val url = "url"
        every { imageUtils.thumbnail(url) } returns "resizedToThumbnail"

        val testObject = createTestObject(null, listOf(Program(null, null, null, null, url = url, imageUrl = url, duration = null)))

        val actual = testObject.thumbnail()

        assertEquals("resizedToThumbnail", actual)
    }

    @Test
    fun `thumbnail in ArcVideoStreamVirtualChannel returns empty`() {
        val testObject = createTestObject(null, listOf(Program(null, null, null, null, url = null, imageUrl = null, duration = null)))

        val actual = testObject.thumbnail()

        assertEquals("", actual)
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

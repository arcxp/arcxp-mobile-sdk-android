package com.arcxp.video.model

import android.app.Application
import com.arcxp.ArcXPMobileSDK.resizer
import com.arcxp.sdk.R
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VideoVOTest {

    @RelaxedMockK
    lateinit var application: Application


    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { application.getString(R.string.resizer_key) } returns "resizer_key"
    }

    @Test
    fun `get thumbnail with video`() {
        val url = "url"
        every { resizer().createThumbnail(url) } returns "resizedToThumbnail"

        val testObject = createTestObject(
            VideoVO.PromoImage(
                image = VideoVO.PromoImage.Image(
                    null,
                    null,
                    null,
                    null,
                    url = url,
                    null
                )
            )
        )

        val actual = testObject.thumbnail()

        assertEquals("resizedToThumbnail", actual)
    }

    @Test
    fun `get thumbnail with VideoVO, promoItem is null returns empty string`() {

        val testObject = createTestObject(null)

        val actual = testObject.thumbnail()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `get thumbnail with VideoVO, promoItem image is null returns empty string`() {
        val testObject = createTestObject(VideoVO.PromoImage(null))

        val actual = testObject.thumbnail()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `get thumbnail with VideoVO, promoItem image url is null returns empty string`() {

        val testObject = createTestObject(
            VideoVO.PromoImage(
                VideoVO.PromoImage.Image(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            )
        )

        val actual = testObject.thumbnail()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `fallback with VideoVO, returns original url string`() {
        val expected = "orginal url"
        val testObject = createTestObject(
            VideoVO.PromoImage(
                VideoVO.PromoImage.Image(
                    null,
                    null,
                    null,
                    null,
                    expected,
                    null
                )
            )
        )

        val actual = testObject.fallback()

        assertEquals(expected, actual)
    }

    @Test
    fun `fallback with VideoVO, promoImage is null and returns empty string`() {
        val testObject = createTestObject(null)

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `fallback with VideoVO, promoImage image is null and returns empty string`() {
        val testObject = createTestObject(VideoVO.PromoImage(null))

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `fallback with VideoVO, promoImage image url is null and returns empty string`() {
        val testObject = createTestObject(
            VideoVO.PromoImage(
                VideoVO.PromoImage.Image(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            )
        )

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `fallback with VideoVO, url is null returns empty string`() {
        val testObject = createTestObject(
            VideoVO.PromoImage(
                VideoVO.PromoImage.Image(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
            )
        )

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }
}

private fun createTestObject(
    promoImage: VideoVO.PromoImage? = null
) = VideoVO(
    null, null, null, null, null, null, null,
    null, null, null, null, null, null, null,
    promoImage, null, null, null, null, null, null
)
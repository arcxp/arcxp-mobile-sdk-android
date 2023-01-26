package com.arcxp.video.model

import android.app.Application
import com.arcxp.video.ArcXPVideoSDK
import com.arcxp.sdk.R
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class VideoVOTest {

    @RelaxedMockK
    lateinit var application: Application


    @Before
    fun setUp(){
        MockKAnnotations.init(this)
        every { application.getString(R.string.resizer_key) } returns "resizer_key"
    }

    @Test
    fun `get thumbnail with video`() {
        mockkObject(ArcXPVideoSDK)
        val url = "url"
        every { ArcXPVideoSDK.initialize(application, "base", "org", "env", "site") }
        every { ArcXPVideoSDK.resizer().createThumbnail(url) } returns "resizedToThumbnail"

        val testObject = createTestObject(VideoVO.PromoImage(image = VideoVO.PromoImage.Image(null, null, null, null, url = url, null)))

        val actual = testObject.thumbnail()

        Assert.assertEquals("resizedToThumbnail", actual)
    }

    @Test
    fun `get thumbnail with VideoVO, promoItem is null returns emptry string`() {

        val testObject = createTestObject(null)

        val actual = testObject.thumbnail()

        Assert.assertTrue(actual.isEmpty())
    }

    @Test
    fun `get thumbnail with VideoVO, promoItem image is null returns emptry string`() {

        val testObject = createTestObject(VideoVO.PromoImage(null))

        val actual = testObject.thumbnail()

        Assert.assertTrue(actual.isEmpty())
    }

    @Test
    fun `get thumbnail with VideoVO, promoItem image url is null returns emptry string`() {

        val testObject = createTestObject(VideoVO.PromoImage(VideoVO.PromoImage.Image(null, null, null, null, null , null)))

        val actual = testObject.thumbnail()

        Assert.assertTrue(actual.isEmpty())
    }

    @Test
    fun `fallback with VideoVO, returns original url string`() {
        mockkObject(ArcXPVideoSDK)
        val expected = "orginal url"
        every { ArcXPVideoSDK.initialize(application, "base", "org", "env", "site") }
        val testObject = createTestObject(VideoVO.PromoImage(VideoVO.PromoImage.Image(null, null, null, null, expected, null)))

        val actual = testObject.fallback()

        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `fallback with VideoVO, promoImage is null and returns empty string`() {
        val testObject = createTestObject(null)

        val actual = testObject.fallback()

        Assert.assertTrue(actual.isEmpty())
    }

    @Test
    fun `fallback with VideoVO, promoImage image is null and returns empty string`() {
        val testObject = createTestObject(VideoVO.PromoImage(null))

        val actual = testObject.fallback()

        Assert.assertTrue(actual.isEmpty())
    }

    @Test
    fun `fallback with VideoVO, promoImage image url is null and returns empty string`() {
        val testObject = createTestObject(VideoVO.PromoImage(VideoVO.PromoImage.Image(null, null, null, null, null, null)))

        val actual = testObject.fallback()

        Assert.assertTrue(actual.isEmpty())
    }

    @Test
    fun `fallback with VideoVO, url is null returns empty string`() {
        val testObject = createTestObject(VideoVO.PromoImage(VideoVO.PromoImage.Image(null, null, null, null, null, null)))

        val actual = testObject.fallback()

        Assert.assertTrue(actual.isEmpty())
    }
}

private fun createTestObject(
    promoImage: VideoVO.PromoImage? = null
) =
    VideoVO(
        null, null, null, null, null, null, null,
        null, null, null, null, null, null, null,
       promoImage, null, null, null, null, null, null
    )
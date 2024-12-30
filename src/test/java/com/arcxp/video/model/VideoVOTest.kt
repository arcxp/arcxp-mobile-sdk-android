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

class VideoVOTest {

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
        every { Resources.getSystem() } returns resources
        every { DependencyFactory.createImageUtil("", application) } returns imageUtils
        every { imageUtils() } returns imageUtils
        every { Resources.getSystem().displayMetrics.widthPixels } returns 100
        every { Resources.getSystem().displayMetrics.heightPixels } returns 100
        every { application.getString(R.string.resizer_key) } returns "resizer_key"
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `get thumbnail with video`() {
        val url = "url"
        every { imageUtils.thumbnail(url) } returns "resizedToThumbnail"

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
    fun `get thumbnail with video returns empty`() {

        val testObject = createTestObject(
            VideoVO.PromoImage(
                image = VideoVO.PromoImage.Image(
                    null,
                    null,
                    null,
                    null,
                    url = null,
                    null
                )
            )
        )

        val actual = testObject.thumbnail()

        assertEquals("", actual)
    }

    @Test
    fun `get thumbnail with VideoVO, promoItem is null returns empty string`() {

        val testObject = createTestObject(null)

        val actual = testObject.thumbnail()

        assertTrue(actual!!.isEmpty())
    }

    @Test
    fun `get thumbnail with VideoVO, promoItem image is null returns empty string`() {
        val testObject = createTestObject(VideoVO.PromoImage(null))

        val actual = testObject.thumbnail()

        assertTrue(actual!!.isEmpty())
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

        assertTrue(actual!!.isEmpty())
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
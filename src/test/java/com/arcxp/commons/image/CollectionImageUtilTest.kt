package com.arcxp.commons.image

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commons.testutils.TestUtils.basic
import com.arcxp.commons.testutils.TestUtils.createImageObject
import com.arcxp.commons.testutils.TestUtils.lead_art
import com.arcxp.commons.util.ArcXPResizerV1
import com.arcxp.commons.util.Constants
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.content.models.PromoItem
import com.arcxp.sdk.R
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CollectionImageUtilsTest {

    @RelaxedMockK
    lateinit var application: Application

    @RelaxedMockK
    internal lateinit var resizerV2: ArcXPResizerV2

    @RelaxedMockK
    internal lateinit var resizerV1: ArcXPResizerV1

    internal lateinit var testObject: CollectionImageUtil

    @RelaxedMockK
    lateinit var resources: Resources

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockkObject(ArcXPMobileSDK)
        mockkObject(DependencyFactory)
        mockkStatic(Resources::class)
        mockkStatic(DisplayMetrics::class)
        val context = mockk<Context>()
        every {
            context.getString(R.string.resizer_key)
        } returns "abc"
        every { Resources.getSystem() } returns mockk {
            every { displayMetrics } returns DisplayMetrics().apply {
                widthPixels = 100
                heightPixels = 100
            }
        }
        every {
            DependencyFactory.createArcXPV1Resizer(
                baseUrl = "abc",
                resizerKey = context.getString(R.string.resizer_key)
            )
        } returns resizerV1
        every {
            DependencyFactory.createArcXPV2Resizer(
                baseUrl = "abc"
            )
        } returns resizerV2

        testObject = CollectionImageUtil("abc", application)

    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `imageUrl with image do not resize, url is null`() {
        val image = createImageObject(
            height = null,
            width = null
        )

        val actual = testObject.imageUrl(image)

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with image do not resize, url is not null`() {
        val url = "url"
        val image = createImageObject(
            height = null,
            width = null,
            url = url
        )

        val actual = testObject.imageUrl(image)

        assertEquals(url, actual)
    }

    @Test
    fun `imageUrl image is smaller than screen, url is not null`() {
        val url = "url"
        val image = createImageObject(
            height = 10,
            width = 10,
            url = url
        )

        val actual = testObject.imageUrl(image)

        assertEquals(url, actual)
    }

    @Test
    fun `imageUrl image is smaller than screen, url is null`() {
        val image = createImageObject(
            height = 10,
            width = 10,
            url = null
        )

        val actual = testObject.imageUrl(image)

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl image resize width with v2`() {
        val url = "url"
        val newUrl = "newUrl"
        val width = 100
        val image = createImageObject(
            height = 10,
            width = width,
            url = url
        )

        every { resizerV2.isValid(image) } returns true
        every { resizerV2.resizeWidth(image, width) } returns newUrl

        val actual = testObject.imageUrl(image)

        verify(exactly = 1) {
            resizerV2.resizeWidth(image, width)
        }

        assertEquals(newUrl, actual)
    }

    @Test
    fun `imageUrl image resize width with v2 resizeWidth returns null`() {
        val url = "url"
        val newUrl = "newUrl"
        val width = 100
        val image = createImageObject(
            height = 10,
            width = width,
            url = url
        )

        every { resizerV2.isValid(image) } returns true
        every { resizerV2.resizeWidth(image, width) } returns null

        val actual = testObject.imageUrl(image)

        verify(exactly = 1) {
            resizerV2.resizeWidth(image, width)
        }

        assertEquals("", actual)
    }

    @Test
    fun `imageUrl image resize height with v2`() {
        val url = "url"
        val newUrl = "newUrl"
        val height = 100
        val image = createImageObject(
            height = height,
            width = 10,
            url = url
        )

        every { resizerV2.isValid(image) } returns true
        every { resizerV2.resizeHeight(image, height) } returns newUrl

        val actual = testObject.imageUrl(image)

        verify(exactly = 1) {
            resizerV2.resizeHeight(image, height)
        }

        assertEquals(newUrl, actual)
    }

    @Test
    fun `imageUrl image resize height with v2 resizeHeight returns null`() {
        val url = "url"
        val newUrl = "newUrl"
        val height = 100
        val image = createImageObject(
            height = height,
            width = 10,
            url = url
        )

        every { resizerV2.isValid(image) } returns true
        every { resizerV2.resizeHeight(image, height) } returns null

        val actual = testObject.imageUrl(image)

        verify(exactly = 1) {
            resizerV2.resizeHeight(image, height)
        }

        assertEquals("", actual)
    }

    @Test
    fun `imageUrl image resize width with v1`() {
        val url = "=/url"
        val newUrl = "url"
        val width = 200
        val image = createImageObject(
            height = 10,
            width = width,
            url = url,
            additional_props = mapOf<String, String>("resizeUrl" to url)
        )

        every { resizerV1.isValid() } returns true
        every { resizerV1.resizeWidth(newUrl, 100) } returns newUrl

        val actual = testObject.imageUrl(image)

        verify(exactly = 1) {
            resizerV1.resizeWidth(newUrl, 100)
        }

        assertEquals(newUrl, actual)
    }

    @Test
    fun `imageUrl image resize width with v1 returns null`() {
        val url = "=/url"
        val newUrl = ""
        val width = 200
        val image = createImageObject(
            height = 10,
            width = width,
            url = url,
            additional_props = mapOf<String, String>("resizeUrl" to url)
        )

        every { resizerV1.isValid() } returns true
        every { resizerV1.resizeWidth("url", 100) } returns null

        val actual = testObject.imageUrl(image)

        assertEquals(newUrl, actual)
    }

    @Test
    fun `imageUrl image resize height with v1`() {
        val url = "=/url"
        val newUrl = "url"
        val height = 200
        val image = createImageObject(
            height = height,
            width = 10,
            url = url,
            additional_props = mapOf<String, String>("resizeUrl" to url)
        )

        every { resizerV1.isValid() } returns true
        every { resizerV1.resizeHeight(newUrl, 100) } returns newUrl

        val actual = testObject.imageUrl(image)

        verify(exactly = 1) {
            resizerV1.resizeHeight(newUrl, 100)
        }

        assertEquals(newUrl, actual)
    }

    @Test
    fun `imageUrl image v1 final url is empty`() {
        val url = "url"
        val height = 200
        val image = createImageObject(
            height = height,
            width = 10,
            url = url,
            additional_props = mapOf<String, String>("sometoken" to url)
        )

        every { resizerV1.isValid() } returns true

        val actual = testObject.imageUrl(image)

        assertEquals(url, actual)
    }

    @Test
    fun `imageUrl image resize height with v1 returns null`() {
        val url = "=/url"
        val newUrl = ""
        val height = 200
        val image = createImageObject(
            height = height,
            width = 10,
            url = url,
            additional_props = mapOf<String, String>("resizeUrl" to url)
        )

        every { resizerV1.isValid() } returns true
        every { resizerV1.resizeHeight("url", 100) } returns null

        val actual = testObject.imageUrl(image)

        assertEquals(newUrl, actual)
    }

    @Test
    fun `imageUrl image v1 no additional_properties returns empty`() {
        val url = "=/url"
        val newUrl = ""
        val height = 200
        val image = createImageObject(
            height = height,
            width = 10,
            url = null,
            additional_props = mapOf<String, String>("sometoken" to url)
        )

        every { resizerV1.isValid() } returns true

        val actual = testObject.imageUrl(image)

        assertEquals(newUrl, actual)
    }

    @Test
    fun `imageUrl image v1 returns url`() {
        val url = "url"
        val height = 200
        val image = createImageObject(
            height = height,
            width = 10,
            url = url
        )

        every { resizerV1.isValid() } returns true

        val actual = testObject.imageUrl(image)

        assertEquals(url, actual)
    }

    @Test
    fun `imageUrl image v1 false  returns url`() {
        val url = "url"
        val height = 200
        val image = createImageObject(
            height = height,
            width = 10,
            url = url
        )

        every { resizerV1.isValid() } returns false

        val actual = testObject.imageUrl(image)

        assertEquals(url, actual)
    }

    @Test
    fun `imageUrl with promoItem do not resize, url is null`() {
        val promoItem = basic(
            height = null,
            width = null
        )

        val actual = testObject.imageUrl(promoItem)

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with promoItem width null height is not`() {
        val promoItem = basic(
            height = null,
            width = 10
        )

        val actual = testObject.imageUrl(promoItem)

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with promoItem height null width is not`() {
        val promoItem = basic(
            height = 10,
            width = null
        )

        val actual = testObject.imageUrl(promoItem)

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with promoItem do not resize, url is not null`() {
        val url = "url"
        val image = basic(
            height = null,
            width = null,
            url = url
        )

        val actual = testObject.imageUrl(image)

        assertEquals(url, actual)
    }

    @Test
    fun `imageUrl promoItem is smaller than screen, url is not null`() {
        val url = "url"
        val image = basic(
            height = 10,
            width = 10,
            url = url
        )

        val actual = testObject.imageUrl(image)

        assertEquals(url, actual)
    }

    @Test
    fun `imageUrl promoItem is smaller than screen, url is null`() {
        val image = basic(
            height = 10,
            width = 10,
            url = null
        )

        val actual = testObject.imageUrl(image)

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl promoItem resize width with v2`() {
        val url = "url"
        val newUrl = "newUrl"
        val width = 100
        val image = basic(
            type = "image",
            height = 10,
            width = width,
            url = url
        )

        testObject = spyk(testObject)

        every { resizerV2.isValid(image) } returns true
        every { resizerV2.getV2Url(image) } returns url
        every { resizerV2.resizeWidth(url, width) } returns newUrl

        val actual = testObject.imageUrl(image)

        verify(exactly = 1) {
            resizerV2.resizeWidth(image.url!!, width)
        }

        assertEquals(newUrl, actual)
    }

    @Test
    fun `imageUrl promoItem resize height with v2`() {
        val url = "url"
        val newUrl = "newUrl"
        val height = 100
        val image = basic(
            type = "image",
            height = height,
            width = 10,
            url = url
        )

        testObject = spyk(testObject)

        every { resizerV2.isValid(image) } returns true
        every { resizerV2.getV2Url(image) } returns url
        every { resizerV2.resizeHeight(url, height) } returns newUrl

        val actual = testObject.imageUrl(image)

        verify(exactly = 1) {
            resizerV2.resizeHeight(image.url!!, height)
        }

        assertEquals(newUrl, actual)
    }

    @Test
    fun `imageUrl promoItem v2 invalid item type not image`() {
        val url = "url"
        val height = 100
        val image = basic(
            type = "notimage",
            height = height,
            width = 10,
            url = url
        )

        every { resizerV2.isValid(image) } returns false

        val actual = testObject.imageUrl(image)

        assertEquals(url, actual)
    }

    @Test
    fun `imageUrl promoItem v2 invalid item type image`() {
        val url = "url"
        val height = 100
        val image = basic(
            type = "image",
            height = height,
            width = 10,
            url = url
        )

        every { resizerV2.isValid(image) } returns false

        val actual = testObject.imageUrl(image)

        assertEquals(url, actual)
    }

    @Test
    fun `imageUrl promoItem resize width with v1 type = video`() {
        val url = "=/url"
        val newUrl = "url"
        val width = 200
        val image = basic(
            type = "video",
            height = 10,
            width = width,
            url = url,
        )

        every { resizerV1.isValid() } returns true
        every { resizerV1.resizeWidth(newUrl, 100) } returns newUrl

        val actual = testObject.imageUrl(image)

        verify(exactly = 1) {
            resizerV1.resizeWidth(newUrl, 100)
        }

        assertEquals(newUrl, actual)
    }

    @Test
    fun `imageUrl promoItem resize height with v1 type = video`() {
        val url = "=/url"
        val newUrl = "url"
        val height = 200
        val image = basic(
            type = "video",
            height = height,
            width = 10,
            url = url,
        )

        every { resizerV1.isValid() } returns true
        every { resizerV1.resizeHeight(newUrl, 100) } returns newUrl

        val actual = testObject.imageUrl(image)

        verify(exactly = 1) {
            resizerV1.resizeHeight(newUrl, 100)
        }

        assertEquals(newUrl, actual)
    }

    @Test
    fun `imageUrl promoItem v1 type = video url is null`() {
        val height = 200
        val image = basic(
            type = "video",
            height = height,
            width = 10,
            url = null,
        )

        every { resizerV1.isValid() } returns true

        val actual = testObject.imageUrl(image)

        assertEquals("", actual)
    }

    @Test
    fun `imageUrl promoItem resize width with v1 type != video`() {
        val url = "=/url"
        val newUrl = "url"
        val width = 200
        val image = basic(
            height = 10,
            width = width,
            url = url,
            additional_props = mapOf<String, String>("resizeUrl" to url)
        )

        every { resizerV1.isValid() } returns true
        every { resizerV1.resizeWidth(newUrl, 100) } returns newUrl

        val actual = testObject.imageUrl(image)

        verify(exactly = 1) {
            resizerV1.resizeWidth(newUrl, 100)
        }

        assertEquals(newUrl, actual)
    }

    @Test
    fun `imageUrl promoItem resize width returns null with v1 type != video`() {
        val url = "=/url"
        val newUrl = "url"
        val width = 200
        val image = basic(
            height = 10,
            width = width,
            url = url,
            additional_props = mapOf<String, String>("resizeUrl" to url)
        )

        every { resizerV1.isValid() } returns true
        every { resizerV1.resizeWidth(newUrl, 100) } returns null

        val actual = testObject.imageUrl(image)

        verify(exactly = 1) {
            resizerV1.resizeWidth(newUrl, 100)
        }

        assertEquals("", actual)
    }

    @Test
    fun `imageUrl promoItem resize height with v1 type != video`() {
        val url = "=/url"
        val newUrl = "url"
        val height = 200
        val image = basic(
            height = height,
            width = 10,
            url = url,
            additional_props = mapOf<String, String>("resizeUrl" to url)
        )

        every { resizerV1.isValid() } returns true
        every { resizerV1.resizeHeight(newUrl, 100) } returns newUrl

        val actual = testObject.imageUrl(image)

        verify(exactly = 1) {
            resizerV1.resizeHeight(newUrl, 100)
        }

        assertEquals(newUrl, actual)
    }

    @Test
    fun `imageUrl promoItem resize height returns null with v1 type != video`() {
        val url = "=/url"
        val newUrl = "url"
        val height = 200
        val image = basic(
            height = height,
            width = 10,
            url = url,
            additional_props = mapOf<String, String>("resizeUrl" to url)
        )

        every { resizerV1.isValid() } returns true
        every { resizerV1.resizeHeight(newUrl, 100) } returns null

        val actual = testObject.imageUrl(image)

        verify(exactly = 1) {
            resizerV1.resizeHeight(newUrl, 100)
        }

        assertEquals("", actual)
    }

    @Test
    fun `imageUrl promoItem resize height with v1 type != video returns empty`() {
        val url = "=/url"
        val newUrl = ""
        val height = 200
        val image = basic(
            height = height,
            width = 10,
            url = null,
            additional_props = mapOf<String, String>("sometoken" to url)
        )

        every { resizerV1.isValid() } returns true

        val actual = testObject.imageUrl(image)

        assertEquals(newUrl, actual)
    }

    @Test
    fun `imageUrl promoItem v1 returns url`() {
        val url = "url"
        val height = 200
        val image = createImageObject(
            height = height,
            width = 10,
            url = url
        )

        every { resizerV1.isValid() } returns true

        val actual = testObject.imageUrl(image)

        assertEquals(url, actual)
    }

    @Test
    fun `imageUrl promoItem v1 false returns url`() {
        val url = "url"
        val height = 200
        val image = basic(
            height = height,
            width = 10,
            url = url
        )

        every { resizerV1.isValid() } returns false

        val actual = testObject.imageUrl(image)

        assertEquals(url, actual)
    }

    @Test
    fun `imageUrl promoItem v1 returns empty`() {
        val url = ""
        val height = 200
        val image = createImageObject(
            height = height,
            width = 10,
            url = url
        )

        every { resizerV1.isValid() } returns true

        val actual = testObject.imageUrl(image)

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `thumbnail v2 returns url`() {
        val url = "url"

        val promoItem = PromoItem(
            basic(
                url = url,
                type = "image"
            ),
            lead_art = null
        )

        testObject = spyk(testObject)

        every { resizerV2.isValid(promoItem.basic!!) } returns true
        every { resizerV2.getV2Url(promoItem.basic!!) } returns url
        every { resizerV2.createThumbnail(url) } returns url

        val actual = testObject.thumbnail(promoItem)

        assertEquals(url, actual)
    }

    @Test
    fun `thumbnail v2 getV2Url returns null`() {
        val url = "url"

        val promoItem = PromoItem(
            basic(
                url = url,
                type = "image"
            ),
            lead_art = null
        )

        testObject = spyk(testObject)

        every { resizerV2.isValid(promoItem.basic!!) } returns true
        every { resizerV2.getV2Url(promoItem.basic!!) } returns null
        every { resizerV2.createThumbnail(url) } returns url

        val actual = testObject.thumbnail(promoItem)

        assertEquals("", actual)
    }

    @Test
    fun `thumbnail v2 image in lead_art returns url`() {
        val url = "url"

        val promoItem = PromoItem(
            basic = null,
            lead_art = basic(
                url = url,
                type = "image"
            )
        )

        every { resizerV2.isValid(promoItem.lead_art!!) } returns true
        every { resizerV2.getV2Url(promoItem.lead_art!!) } returns url
        every { resizerV2.createThumbnail(url) } returns url

        val actual = testObject.thumbnail(promoItem)

        assertEquals(url, actual)
    }

    @Test
    fun `thumbnail v2 image in lead_art getV2Url returns null`() {
        val url = "url"

        val promoItem = PromoItem(
            basic = null,
            lead_art = basic(
                url = url,
                type = "image"
            )
        )

        every { resizerV2.isValid(promoItem.lead_art!!) } returns true
        every { resizerV2.getV2Url(promoItem.lead_art!!) } returns null
        every { resizerV2.createThumbnail(url) } returns url

        val actual = testObject.thumbnail(promoItem)

        assertEquals("", actual)
    }

    @Test
    fun `thumbnail v2 type video returns url`() {
        val url = "url"
        val newUrl = "newUrl"
        val promoItem = PromoItem(
            basic(
                url = url,
                type = "video"
            ),
            lead_art = null
        )

        every { resizerV2.isValid(promoItem.basic!!) } returns true
        every { resizerV2.getV2Url(promoItem.basic!!) } returns url
        every { resizerV2.createThumbnail(url) } returns newUrl

        val actual = testObject.thumbnail(promoItem)

        assertEquals(newUrl, actual)
    }

    @Test
    fun `thumbnail v2 type video returns empty`() {
        val newUrl = ""
        val promoItem = PromoItem(
            basic(
                url = null,
                type = "video"
            ),
            lead_art = null
        )

        every { resizerV2.isValid(promoItem.basic!!) } returns true

        val actual = testObject.thumbnail(promoItem)

        assertEquals(newUrl, actual)
    }

    @Test
    fun `thumbnail v1 type video returns empty`() {
        val newUrl = ""
        val promoItem = PromoItem(
            basic(
                url = null,
                type = "video"
            ),
            lead_art = null
        )

        every { resizerV1.isValid() } returns true

        val actual = testObject.thumbnail(promoItem)

        assertEquals(newUrl, actual)
    }

    @Test
    fun `thumbnail v1 type video returns url`() {
        val newUrl = "=/url"
        val url = "url"
        val promoItem = PromoItem(
            basic(
                url = newUrl,
                type = "video"
            ),
            lead_art = null
        )

        every { resizerV1.isValid() } returns true
        every { resizerV1.createThumbnail(url) } returns url

        val actual = testObject.thumbnail(promoItem)

        assertEquals(url, actual)
    }

    @Test
    fun `thumbnail v1 invalid type video returns url`() {
        val newUrl = "url"
        val url = "url"
        val promoItem = PromoItem(
            basic(
                url = newUrl,
                type = "video"
            ),
            lead_art = null
        )

        every { resizerV1.isValid() } returns false

        val actual = testObject.thumbnail(promoItem)

        assertEquals(url, actual)
    }

    @Test
    fun `thumbnail promo item else condition`() {
        val newUrl = "url"
        var promoItem1 = PromoItem(
            basic(
                url = null,
                type = "image"
            ),
            lead_art = null
        )

        val actual = testObject.thumbnail(promoItem1)
        assertEquals("", actual)

        var promoItem2 = PromoItem(
            basic(
                url = newUrl,
                type = "image"
            ),
            lead_art = null
        )

        val actual2 = testObject.thumbnail(promoItem2)
        assertEquals(newUrl, actual2)

        var promoItem3 = PromoItem(
            basic = null,
            lead_art = basic(
                url = newUrl,
                type = "image"
            )
        )

        val actual3 = testObject.thumbnail(promoItem3)
        assertEquals(newUrl, actual3)

        var promoItem4 = PromoItem(
            basic = basic(
                additional_props = mapOf<String, String>(Constants.THUMBNAIL_RESIZE_URL_KEY to newUrl)
            ),
            lead_art = null
        )

        val actual4 = testObject.thumbnail(promoItem4)
        assertEquals("baseurl$newUrl", actual4)

        var promoItem5 = PromoItem(
            basic = basic(

            ),
            lead_art = lead_art(
                additional_props = mapOf<String, String>(Constants.THUMBNAIL_RESIZE_URL_KEY to newUrl)
            )
        )

        val actual5 = testObject.thumbnail(promoItem5)
        assertEquals("baseurl$newUrl", actual5)

        var promoItem6 = PromoItem(
            basic = basic(

            ),
            lead_art = lead_art(
                lead_art_additional_props = mapOf<String, String>(Constants.THUMBNAIL_RESIZE_URL_KEY to newUrl)
            )
        )

        val actual6 = testObject.thumbnail(promoItem6)
        assertEquals("baseurl$newUrl", actual6)

        var promoItem7 = PromoItem(
            basic = null,
            lead_art = null
        )

        val actual7 = testObject.thumbnail(promoItem7)
        assertEquals("", actual7)

        var promoItem8 = PromoItem(
            basic = null,
            lead_art = basic(
                promoItem = PromoItem(
                    basic = basic(
                        additional_props = mapOf<String, String>(Constants.THUMBNAIL_RESIZE_URL_KEY to newUrl)
                    ),
                    lead_art = null
                )
            )
        )

        val actual8 = testObject.thumbnail(promoItem8)
        assertEquals("baseurlurl", actual8)

        var promoItem9 = PromoItem(
            basic = null,
            lead_art = basic(
                promoItem = PromoItem(
                    basic = basic(
                        additional_props = null
                    ),
                    lead_art = null
                )
            )
        )

        val actual9 = testObject.thumbnail(promoItem9)
        assertEquals("", actual9)

        var promoItem10 = PromoItem(
            basic = null,
            lead_art = basic(
                promoItem = PromoItem(
                    basic = null,
                    lead_art = null
                )
            )
        )

        val actual10 = testObject.thumbnail(promoItem10)
        assertEquals("", actual10)

        var promoItem11 = PromoItem(
            basic = null,
            lead_art = basic(
                promoItem = null
            )
        )

        val actual11 = testObject.thumbnail(promoItem11)
        assertEquals("", actual11)

    }

    @Test
    fun `thumbnail string v1 returns empty`() {
        val url = "https://url"
        val newUrl = "url"

        every {resizerV1.isValid()} returns true
        every {resizerV1.createThumbnail(url)} returns newUrl

        val actual = testObject.thumbnail(url)

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `thumbnail string v1 createThumnail returns null`() {
        val url = "https://url"
        val newUrl = "url"

        every {resizerV1.isValid()} returns true
        every {resizerV1.createThumbnail(newUrl)} returns null

        val actual = testObject.thumbnail(url)

        assertEquals(url, actual)
    }

    @Test
    fun `thumbnail string v1 returns url`() {
        val url = "https://url"
        val newUrl = "url"

        every {resizerV1.isValid()} returns true
        every {resizerV1.createThumbnail(newUrl)} returns newUrl

        val actual = testObject.thumbnail(url)

        assertEquals(newUrl, actual)
    }

    @Test
    fun `thumbnail string returns string`() {
        val url = "url"
        val actual = testObject.thumbnail(url)
        assertEquals(url, actual)
    }

    @Test
    fun `fallback v2 returns url`() {
        val url = "url"

        val promoItem = PromoItem(
            basic(
                url = url,
                type = "image"
            ),
            lead_art = null
        )

        every { resizerV2.isValid(promoItem.basic!!) } returns true
        every { resizerV2.getV2Url(promoItem.basic!!) } returns url
        every { resizerV2.createThumbnail(url) } returns url

        val actual = testObject.fallback(promoItem)

        assertEquals(url, actual)
    }

    @Test
    fun `fallback v2 getV2Url returns null`() {
        val url = "url"

        val promoItem = PromoItem(
            basic(
                url = url,
                type = "image"
            ),
            lead_art = null
        )

        every { resizerV2.isValid(promoItem.basic!!) } returns true
        every { resizerV2.getV2Url(promoItem.basic!!) } returns null
        every { resizerV2.createThumbnail(url) } returns url

        val actual = testObject.fallback(promoItem)

        assertEquals("", actual)
    }

    @Test
    fun `fallback v2 image in lead_art returns url`() {
        val url = "url"

        val promoItem = PromoItem(
            basic = null,
            lead_art = basic(
                url = url,
                type = "image"
            )
        )

        every { resizerV2.isValid(promoItem.lead_art!!) } returns true
        every { resizerV2.getV2Url(promoItem.lead_art!!) } returns url
        every { resizerV2.createThumbnail(url) } returns url

        val actual = testObject.fallback(promoItem)

        assertEquals(url, actual)
    }

    @Test
    fun `fallback v2 image in lead_art getV2Url returns null`() {
        val url = "url"

        val promoItem = PromoItem(
            basic = null,
            lead_art = basic(
                url = url,
                type = "image"
            )
        )

        every { resizerV2.isValid(promoItem.lead_art!!) } returns true
        every { resizerV2.getV2Url(promoItem.lead_art!!) } returns null
        every { resizerV2.createThumbnail(url) } returns url

        val actual = testObject.fallback(promoItem)

        assertEquals("", actual)
    }

    @Test
    fun `fallback v2 type video returns url`() {
        val url = "url"
        val promoItem = PromoItem(
            basic(
                url = url,
                type = "video"
            ),
            lead_art = null
        )

        val actual = testObject.fallback(promoItem)

        assertEquals(url, actual)
    }

    @Test
    fun `fallback v2 type video returns empty`() {
        val url = ""
        val promoItem = PromoItem(
            basic(
                url = null,
                type = "video"
            ),
            lead_art = null
        )

        val actual = testObject.fallback(promoItem)

        assertEquals(url, actual)
    }

    @Test
    fun `fallback promo item else condition`() {
        val newUrl = "url"
        var promoItem1 = PromoItem(
            basic(
                url = null,
                type = "image"
            ),
            lead_art = null
        )

        val actual = testObject.fallback(promoItem1)
        assertEquals("", actual)

        var promoItem2 = PromoItem(
            basic(
                url = newUrl,
                type = "image"
            ),
            lead_art = null
        )

        val actual2 = testObject.fallback(promoItem2)
        assertEquals(newUrl, actual2)

        var promoItem3 = PromoItem(
            basic = null,
            lead_art = basic(
                url = newUrl,
                type = "image"
            )
        )

        val actual3 = testObject.fallback(promoItem3)
        assertEquals(newUrl, actual3)

        var promoItem4 = PromoItem(
            basic = basic(
                additional_props = mapOf<String, String>(Constants.THUMBNAIL_RESIZE_URL_KEY to newUrl)
            ),
            lead_art = null
        )

        val actual4 = testObject.fallback(promoItem4)
        assertEquals("baseurl$newUrl", actual4)

        var promoItem5 = PromoItem(
            basic = basic(

            ),
            lead_art = lead_art(
                additional_props = mapOf<String, String>(Constants.THUMBNAIL_RESIZE_URL_KEY to newUrl)
            )
        )

        val actual5 = testObject.fallback(promoItem5)
        assertEquals("baseurl$newUrl", actual5)

        var promoItem6 = PromoItem(
            basic = basic(

            ),
            lead_art = lead_art(
                lead_art_additional_props = mapOf<String, String>(Constants.THUMBNAIL_RESIZE_URL_KEY to newUrl)
            )
        )

        val actual6 = testObject.fallback(promoItem6)
        assertEquals("baseurl$newUrl", actual6)

        var promoItem7 = PromoItem(
            basic = null,
            lead_art = null
        )

        val actual7 = testObject.fallback(promoItem7)
        assertEquals("", actual7)

        var promoItem8 = PromoItem(
            basic = null,
            lead_art = basic(
                promoItem = PromoItem(
                    basic = basic(
                        additional_props = mapOf<String, String>(Constants.THUMBNAIL_RESIZE_URL_KEY to newUrl)
                    ),
                    lead_art = null
                )
            )
        )

        val actual8 = testObject.fallback(promoItem8)
        assertEquals("baseurlurl", actual8)

        var promoItem9 = PromoItem(
            basic = null,
            lead_art = basic(
                promoItem = PromoItem(
                    basic = basic(
                        additional_props = null
                    ),
                    lead_art = null
                )
            )
        )

        val actual9 = testObject.fallback(promoItem9)
        assertEquals("", actual9)

        var promoItem10 = PromoItem(
            basic = null,
            lead_art = basic(
                promoItem = PromoItem(
                    basic = null,
                    lead_art = null
                )
            )
        )

        val actual10 = testObject.fallback(promoItem10)
        assertEquals("", actual10)

        var promoItem11 = PromoItem(
            basic = null,
            lead_art = basic(
                promoItem = null
            )
        )

        val actual11 = testObject.fallback(promoItem11)
        assertEquals("", actual11)
    }
}
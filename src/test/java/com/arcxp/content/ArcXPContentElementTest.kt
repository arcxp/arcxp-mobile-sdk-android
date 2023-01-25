package com.arcxp.content

import com.arcxp.content.sdk.extendedModels.*
import com.arcxp.content.sdk.models.*
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.util.*

class ArcXPContentElementTest {

    private val resizedHeightURL = "resized url height"
    private val resizedWidthURL = "resized url width"

    @Test
    fun `fallback with video, uses url`() {
        val expected = "url"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns 1000
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = any(), height = any())
        } returns resizedHeightURL
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = any(), width = any())
        } returns resizedWidthURL

        val testObject = createTestObject(type = "video", url = expected)

        val actual = testObject.fallback()

        assertEquals(expected, actual)
    }

    @Test
    fun `fallback with video, url is null returns empty string`() {
        val testObject = createTestObject(type = "video")

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `fallback with video, returns empty string when promoItem is null`() {
        val testObject = createCollectionWithoutPromoItem("video", promoItem = null)

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `fallback with video, returns empty string when promoItem basic is null`() {
        val testObject =
            createCollectionWithoutPromoItem("video", promoItem = PromoItem(null, null))

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `fallback with image, returns url`() {
        val url = "thumbnailUrl"
        val expected = "baseUrl/$url"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.arcxpContentConfig().baseUrl } returns "baseUrl/"
        val testObject = createTestObject(
            url = url,
            additional_props = mapOf(Pair("thumbnailResizeUrl", "thumbnailUrl"))
        )

        val actual = testObject.fallback()

        assertEquals(expected, actual)
    }

    @Test
    fun `fallback with image, returns lead_art url`() {
        val url = "thumbnailUrl"
        val expected = "baseUrl/$url"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.arcxpContentConfig().baseUrl } returns "baseUrl/"
        val testObject = createTestObject(
            lead_art = lead_art(
                additional_props = mapOf(
                    Pair("thumbnailResizeUrl", "thumbnailUrl")
                )
            )
        )

        val actual = testObject.fallback()

        assertEquals(expected, actual)
    }

    @Test
    fun `fallback with image, returns lead_art promo_items url`() {
        val url = "thumbnailUrl"
        val expected = "baseUrl/$url"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.arcxpContentConfig().baseUrl } returns "baseUrl/"
        val testObject = createTestObject(
            lead_art = lead_art(
                lead_art_additional_props = mapOf(
                    Pair("thumbnailResizeUrl", "thumbnailUrl")
                )
            )
        )

        val actual = testObject.fallback()

        assertEquals(expected, actual)
    }

    @Test
    fun `title in element, gets headline from title extention`() {
        val expected = "This is a title"
        val headline = Headline(basic = expected, null, null, null, null, null)
        mockkObject(ArcXPContentSDK)

        val testObject = createTestObject(type = "video", title = headline)

        val actual = testObject.title()

        assertEquals(expected, actual)
    }

    @Test
    fun `title in element, returns empty string when headline is null`() {

        val testObject = createTestObject(type = "video")

        val actual = testObject.title()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `title in element, returns empty string when headline basic is null`() {
        val headline = Headline(null, null, null, null, null, null)
        val testObject = createTestObject(type = "video", title = headline)

        val actual = testObject.title()

        assertTrue(actual.isEmpty())
    }


    @Test
    fun `description in element, getting description from description extention`() {
        val expected = "This is a description"
        val description = Description(expected)

        val testObject = createTestObject(type = "video", description = description)

        val actual = testObject.description()

        assertEquals(expected, actual)
    }

    @Test
    fun `description in element, returns empty string when description is null`() {

        val testObject = createTestObject(type = "video")

        val actual = testObject.description()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `description in element, returns empty string when description basic is null`() {
        val testObject = createTestObject(
            type = "video",
            height = 1,
            width = 3000,
            description = Description(null)
        )

        val actual = testObject.description()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `image fallback - promoItem lead_art promoItem basic additional properties returns url`() {
        val testObject = createCollectionWithoutPromoItem(
            promoItem = PromoItem(
                null,
                lead_art = PromoItem.PromoItemBasic(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    promo_items = PromoItem(
                        basic = PromoItem.PromoItemBasic(
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            additional_properties = mapOf(
                                Pair(
                                    "thumbnailResizeUrl",
                                    "thumbnailUrl"
                                )
                            )
                        ), null
                    ),
                    null
                )
            )
        )
        val url = "thumbnailUrl"
        val expected = "baseUrl/$url"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.arcxpContentConfig().baseUrl } returns "baseUrl/"
        val actual = testObject.fallback()

        assertEquals(expected, actual)
    }

    @Test
    fun `image fallback - promoItem lead_art promoItem basic additional properties null`() {
        val testObject = createCollectionWithoutPromoItem(
            promoItem = PromoItem(
                null,
                lead_art = PromoItem.PromoItemBasic(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    promo_items = PromoItem(
                        basic = PromoItem.PromoItemBasic(
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            additional_properties = null
                        ), null
                    ),
                    null
                )
            )
        )

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `image fallback - promoItem lead_art promoItem basic null`() {
        val testObject = createCollectionWithoutPromoItem(
            promoItem = PromoItem(
                null,
                lead_art = PromoItem.PromoItemBasic(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    promo_items = PromoItem(basic = null, null),
                    null
                )
            )
        )

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `image fallback - promoItem lead_art promoItem null`() {
        val testObject = createCollectionWithoutPromoItem(
            promoItem = PromoItem(
                null,
                lead_art = PromoItem.PromoItemBasic(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    promo_items = null,
                    null
                )
            )
        )

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `image fallback - promoItem lead_art null`() {
        val testObject =
            createCollectionWithoutPromoItem(promoItem = PromoItem(null, lead_art = null))

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `image fallback - promoItem null`() {
        val testObject = createCollectionWithoutPromoItem()

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `image fallback finds url in promoItem additional_properties `() {
        val url = "thumbnailUrl"
        val expected = "baseUrl/$url"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.arcxpContentConfig().baseUrl } returns "baseUrl/"
        val testObject = createTestObject(
            width = 1,
            height = 1,
            url = url,
            additional_props = mapOf(Pair("thumbnailResizeUrl", "thumbnailUrl"))
        )

        val actual = testObject.fallback()

        assertEquals(expected, actual)
    }

    @Test
    fun `image fallback finds url in promoItem null `() {
        val testObject = createCollectionWithoutPromoItem()

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }


    @Test
    fun `get author with image, getting author from author extention`() {
        val expected = "Author"

        val testObject = createTestObject(
            height = 1,
            width = 3000,
            credits = Credits(
                by = listOf(
                    Credits.CreditsBy(
                        null,
                        null,
                        name = "Author",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    )
                ), photos_by = listOf()
            )
        )

        val actual = testObject.author()

        assertEquals(expected, actual)
    }

    @Test
    fun `get author with image, returns empty string when name is null`() {
        val testObject = createTestObject(
            height = 1,
            width = 3000,
            credits = Credits(
                by = listOf(
                    Credits.CreditsBy(
                        null,
                        null,
                        name = null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    )
                ), photos_by = listOf()
            )
        )

        val actual = testObject.author()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `get author with image, returns empty string when credits is null`() {

        val testObject = createTestObject(height = 1, width = 3000, credits = null)


        val actual = testObject.author()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `get author with image, returns empty string when credits by is null`() {
        val testObject = createTestObject(height = 1, width = 3000, credits = Credits(null, null))


        val actual = testObject.author()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `get author with image, returns empty string when credits by is empty`() {
        val testObject =
            createTestObject(height = 1, width = 3000, credits = Credits(listOf(), null))


        val actual = testObject.author()

        assertTrue(actual.isEmpty())
    }


    @Test
    fun `get author with image, returns empty string when credit is null`() {

        val testObject = createTestObject(height = 1, width = 3000)

        val actual = testObject.author()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `get author with image, gets empty value from author extention when there isn't an author`() {

        val testObject = createTestObject(height = 1, width = 3000)

        val actual = testObject.author()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `get thumbnail with image`() {
        val url = "thumbnailUrl"
        val expected = "baseUrl/$url"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.arcxpContentConfig().baseUrl } returns "baseUrl/"
        val testObject = createTestObject(
            height = 1,
            width = 3000,
            additional_props = mapOf(Pair("thumbnailResizeUrl", "thumbnailUrl")),
            credits = Credits(
                by = listOf(
                    Credits.CreditsBy(
                        null,
                        null,
                        name = "Author",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    )
                ), photos_by = listOf()
            )
        )

        val actual = testObject.thumbnail()

        assertEquals(expected, actual)
    }

    @Test
    fun `get thumbnail with video`() {
        val url = "thumbnailUrl"
        val expected = "resizedToThumbnail"
        mockkObject(ArcXPContentSDK)

        every { ArcXPContentSDK.resizer().createThumbnail(url) } returns expected

        val testObject = createTestObject(type = "video", url = url)

        val actual = testObject.thumbnail()

        assertEquals(expected, actual)
    }

    @Test
    fun `thumbnail with video returns empty string when PromoItem is null`() {

        val testObject = createCollectionWithoutPromoItem(type = "video")

        val actual = testObject.thumbnail()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `thumbnail with video returns empty string when PromoItem basic is null`() {

        val testObject =
            createCollectionWithoutPromoItem(type = "video", promoItem = PromoItem(null, null))

        val actual = testObject.thumbnail()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `thumbnail with video returns empty string when PromoItem basic url is null`() {

        val testObject = createTestObject(type = "video", basic = basic(url = null))

        val actual = testObject.thumbnail()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `format date with image returns empty when no value`() {
        val testObject = createTestObject(
            credits = Credits(
                by = listOf(
                    Credits.CreditsBy(
                        null,
                        null,
                        name = "Author",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    )
                ), photos_by = listOf()
            )
        )

        val actual = testObject.date()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `format date with image returns formatted date`() {
        val expected = "Oct 31, 2022"
        val published = Date.from(Instant.ofEpochSecond(1667241768))
        val testObject = createTestObject(
            published_date = published,
            credits = Credits(
                by = listOf(
                    Credits.CreditsBy(
                        null,
                        null,
                        name = "Author",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                    )
                ), photos_by = listOf()
            )
        )
        val actual = testObject.date()

        assertEquals(expected, actual)
    }

    @Test
    fun `imageUrl with video, height and width are null returns empty`() {
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns 1000
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = any(), height = any())
        } returns resizedHeightURL
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = any(), width = any())
        } returns resizedWidthURL

        val testObject = createTestObject(type = "video")

        val actual = testObject.imageUrl()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with video, imageHeight & imageWidth both have null promoItem property `() {
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns 1000
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = any(), height = any())
        } returns resizedHeightURL
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = any(), width = any())
        } returns resizedWidthURL

        val testObject = createCollectionWithoutPromoItem(type = "video")

        val actual = testObject.imageUrl()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with video, imageHeight & imageWidth both have null basic property `() {
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns 1000
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = any(), height = any())
        } returns resizedHeightURL
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = any(), width = any())
        } returns resizedWidthURL

        val testObject =
            createCollectionWithoutPromoItem(promoItem = PromoItem(basic = null, lead_art = null))

        val actual = testObject.imageUrl()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with image, height and width are null returns empty`() {
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns 1000
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = any(), height = any())
        } returns resizedHeightURL
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = any(), width = any())
        } returns resizedWidthURL

        val testObject = createTestObject()

        val actual = testObject.imageUrl()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with video, height is null returns empty`() {
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns 1000
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = any(), height = any())
        } returns resizedHeightURL
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = any(), width = any())
        } returns resizedWidthURL

        val testObject = createTestObject(type = "video", width = 1)

        val actual = testObject.imageUrl()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with video, width is null returns empty`() {
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns 1000
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = any(), height = any())
        } returns resizedHeightURL
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = any(), width = any())
        } returns resizedWidthURL

        val testObject = createTestObject(type = "video", height = 1)

        val actual = testObject.imageUrl()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with video, image is smaller than device does not resize url`() {
        val expected = "original url"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns 1000
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = any(), height = any())
        } returns resizedHeightURL
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = any(), width = any())
        } returns resizedWidthURL

        val testObject = createTestObject(type = "video", height = 1, width = 1, url = expected)

        val actual = testObject.imageUrl()

        assertEquals(expected, actual)
    }

    @Test
    fun `imageUrl with video, image is smaller than device does not resize url is null returns empty`() {
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns 1000
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = any(), height = any())
        } returns resizedHeightURL
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = any(), width = any())
        } returns resizedWidthURL

        val testObject = createTestObject(type = "video", height = 1, width = 1)

        val actual = testObject.imageUrl()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with image, image is smaller than device does not resize url`() {
        val expected = "original url"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns 1000
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = any(), height = any())
        } returns resizedHeightURL
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = any(), width = any())
        } returns resizedWidthURL

        val testObject = createTestObject(width = 1, height = 1, url = expected)

        val actual = testObject.imageUrl()

        assertEquals(expected, actual)
    }

    @Test
    fun `imageUrl with image, image is smaller than device but promoItem is null`() {
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns 1000
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = any(), height = any())
        } returns resizedHeightURL
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = any(), width = any())
        } returns resizedWidthURL

        val testObject = createCollectionWithoutPromoItem("video")

        val actual = testObject.imageUrl()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with video, image height is larger than device resizes to device size`() {
        val deviceSize = 100
        val inputUrl = "=/123"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns deviceSize
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = "123", height = deviceSize)
        } returns resizedHeightURL

        val testObject = createTestObject(type = "video", width = 1, height = 3000, url = inputUrl)

        val actual = testObject.imageUrl()

        assertEquals(resizedHeightURL, actual)
    }

    @Test
    fun `imageUrl with video, image height is larger than device, but promoItem url is missing`() {
        val deviceSize = 100
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns deviceSize
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = "123", height = deviceSize)
        } returns resizedHeightURL

        val testObject = createTestObject(type = "video", width = 1, height = 3000)

        val actual = testObject.imageUrl()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with image, image width is larger than device resizes to device size`() {
        val deviceSize = 100
        val inputUrl = "=/url"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns deviceSize
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = "url", width = deviceSize)
        } returns resizedWidthURL

        val testObject = createTestObject(
            width = 3000,
            height = 1,
            url = inputUrl,
            additional_props = mapOf(Pair("resizeUrl", "=/url"))
        )

        val actual = testObject.imageUrl()

        assertEquals(resizedWidthURL, actual)
    }

    @Test
    fun `imageUrl with video, image width is larger than device resizes to device size`() {
        val deviceSize = 100
        val inputUrl = "=/123"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns deviceSize
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = "123", width = deviceSize)
        } returns resizedWidthURL

        val testObject = createTestObject(type = "video", height = 1, width = 3000, url = inputUrl)

        val actual = testObject.imageUrl()

        assertEquals(resizedWidthURL, actual)
    }

    @Test
    fun `imageUrl with image, image width is larger than device but additional_properties are null`() {
        val deviceSize = 100
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns deviceSize
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = "123", width = deviceSize)
        } returns resizedWidthURL

        val testObject =
            createTestObject(height = 1, width = 3000, additional_props = null)

        val actual = testObject.imageUrl()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with image, image width is larger than device but resize url is null`() {
        val deviceSize = 100
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns deviceSize
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = "123", width = deviceSize)
        } returns resizedWidthURL

        val testObject = createTestObject(
            height = 1,
            width = 3000,
            additional_props = mapOf(Pair("a", "b"))
        )

        val actual = testObject.imageUrl()

        assertTrue(actual.isEmpty())
    }

    private fun createTestObject(
        type: String = "not a video",
        url: String? = null,
        width: Int? = null,
        height: Int? = null,
        title: Headline? = null,
        description: Description? = null,
        credits: Credits? = null,
        published_date: Date? = null,
        additional_props: Map<String, String>? = null,
        basic: PromoItem.PromoItemBasic? = null,
        lead_art: PromoItem.PromoItemBasic? = null
    ) =
        ArcXPContentElement(
            headlines = title,
            description = description,
            credits = credits,
            promoItem = PromoItem(
                basic(
                    width = width,
                    height = height,
                    url = url,
                    additional_props = additional_props
                ),
                lead_art = lead_art
            ),
            type = type,
            duration = null,
            subheadlines = null,
            additional_properties = null,
            created_date = null,
            display_date = null,
            first_publish_date = null,
            last_updated_date = null,
            owner = null,
            publish_date = published_date,
            publishing = null,
            revision = null,
            version = null,
            _id = "id",
            website = null,
            address = null,
            content_elements = null,
            caption = null,
            geo = null,
            height = null,
            width = null,
            licensable = null,
            newKeywords = null,
            referent_properties = null,
            selectedGalleries = null,
            subtitle = null,
            taxonomy = null,
            url = url,
            copyright = null,
            language = null,
            location = null,
            video_type = null,
            canonical_url = null,
            subtype = null,
            content = null,
            embed_html = null,
            streams = null
        )

    private fun basic(
        width: Int? = null,
        height: Int? = null,
        url: String? = null,
        additional_props: Map<String, String>? = null
    ): PromoItem.PromoItemBasic {
        return PromoItem.PromoItemBasic(
            _id = null,
            address = null,
            alignment = null,
            canonical_url = null,
            caption = null,
            channels = null,
            content = null,
            copyright = null,
            created_date = null,
            credits = null,
            description = null,
            display_date = null,
            editor_note = null,
            embed = null,
            first_publish_date = null,
            geo = null,
            headlines = null,
            height = height,
            width = width,
            last_updated_date = null,
            language = null,
            licensable = null,
            location = null,
            owner = null,
            publish_date = null,
            short_url = null,
            status = null,
            subheadlines = null,
            subtitle = null,
            subtype = null,
            taxonomy = null,
            type = null,
            url = url,
            version = null,
            promo_items = null,
            additional_properties = additional_props

        )
    }

    private fun lead_art(
        width: Int? = null,
        height: Int? = null,
        url: String? = null,
        additional_props: Map<String, String>? = null,
        lead_art_additional_props: Map<String, String>? = null
    ): PromoItem.PromoItemBasic {
        return PromoItem.PromoItemBasic(
            _id = null,
            address = null,
            alignment = null,
            canonical_url = null,
            caption = null,
            channels = null,
            content = null,
            copyright = null,
            created_date = null,
            credits = null,
            description = null,
            display_date = null,
            editor_note = null,
            embed = null,
            first_publish_date = null,
            geo = null,
            headlines = null,
            height = height,
            width = width,
            last_updated_date = null,
            language = null,
            licensable = null,
            location = null,
            owner = null,
            publish_date = null,
            short_url = null,
            status = null,
            subheadlines = null,
            subtitle = null,
            subtype = null,
            taxonomy = null,
            type = null,
            url = null,
            version = null,
            promo_items = PromoItem(
                basic(
                    width = width,
                    height = height,
                    additional_props = lead_art_additional_props
                ), null
            ),
            additional_properties = additional_props

        )
    }

    private fun createCollectionWithoutPromoItem(
        type: String = "nonVideo",
        url: String? = null,
        width: Int? = null,
        height: Int? = null,
        title: Headline? = null,
        description: Description? = null,
        credits: Credits? = null,
        published_date: Date? = null,
        additional_props: Map<String, String>? = null,
        promoItem: PromoItem? = null,
        basic: PromoItem.PromoItemBasic? = null,
        lead_art: PromoItem.PromoItemBasic? = null
    ) =
        ArcXPContentElement(
            null,
            null,
            null,
            null,
            null,
            null,
            publish_date = published_date,
            null,
            null,
            type = type,
            null,
            _id = "id",
            null,
            null,
            null,
            null,
            credits = credits,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            description = description,
            headlines = title,
            null,
            null,
            promoItem = promoItem,
            null,
            null,
            null,
            null,
            null,
            subheadlines = null,
            null,
            duration = null
        )
}
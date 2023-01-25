package com.arcxp.content

import com.arcxp.content.sdk.extendedModels.*
import com.arcxp.content.sdk.models.*
import com.arcxp.content.sdk.util.Constants.THUMBNAIL_RESIZE_URL_KEY
import io.mockk.every
import io.mockk.mockkObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.*

class ArcXPStoryTest {

    private val resizedHeightURL = "resized url height"
    private val resizedWidthURL = "resized url width"


    @Test
    fun `fallback in story, returns url from additional_properties`() {
        val url = "thumbnailUrl"
        val expected = "baseUrl/$url"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.arcxpContentConfig().baseUrl } returns "baseUrl/"
        val testObject = createTestObject(
            height = 1,
            width = 1,
            url = url,
            additional_props = mapOf(Pair("thumbnailResizeUrl", "thumbnailUrl"))
        )

        val actual = testObject.fallback()

        assertEquals(expected, actual)
    }

    @Test
    fun `imageUrl with story, imageHeight & imageWidth both have null promoItem property returns empty string `() {
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns 1000
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = any(), height = any())
        } returns resizedHeightURL
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = any(), width = any())
        } returns resizedWidthURL

        val testObject = createCollectionWithoutPromoItem(promoItem = null)

        val actual = testObject.imageUrl()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with video - url is null in promo basic `() {
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns 100
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = any(), height = any())
        } returns resizedHeightURL
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = any(), width = any())
        } returns resizedWidthURL

        val testObject = createVideo(width = 3000, height = 1, url = null)

        val actual = testObject.imageUrl()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with story, imageHeight & imageWidth both have null basic property `() {

        val testObject = createCollectionWithoutPromoItem(
            promoItem = PromoItem(basic = null, lead_art = null)
        )

        val actual = testObject.imageUrl()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `fallback with video returns url without resize`() {
        val expected = "url"

        val testObject = createVideo(url = expected)

        val actual = testObject.fallback()

        assertEquals(expected, actual)
    }

    @Test
    fun `fallback with video basic is null returns empty`() {
        val expected = "url"

        val testObject = createCollectionWithoutPromoItem(
            type = "video",
            promoItem = PromoItem(basic = null, lead_art = null)
        )

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `fallback with video basic url is null returns empty`() {
        val expected = "url"

        val testObject = createCollectionWithoutPromoItem(
            type = "video",
            promoItem = PromoItem(basic = basic(url = null), lead_art = null)
        )

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with story, additional properties are null `() {
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns 100
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = any(), height = any())
        } returns resizedHeightURL
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = any(), width = any())
        } returns resizedWidthURL

        val testObject = createTestObject(

            width = 3000,
            height = 1,
            url = null,
            additional_props = null

        )

        val actual = testObject.imageUrl()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with story, image is smaller than device size `() {
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns 100
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = any(), height = any())
        } returns resizedHeightURL
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = any(), width = any())
        } returns resizedWidthURL

        val testObject = createTestObject(

            width = 1,
            height = 1,
            url = null,
            additional_props = null

        )

        val actual = testObject.imageUrl()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with story, image height is null `() {
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns 100
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = any(), height = any())
        } returns resizedHeightURL
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = any(), width = any())
        } returns resizedWidthURL

        val testObject = createTestObject(

            width = 1,
            height = null,
            url = null,
            additional_props = null

        )

        val actual = testObject.imageUrl()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with story, image width is null `() {
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns 100
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = any(), height = any())
        } returns resizedHeightURL
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = any(), width = any())
        } returns resizedWidthURL

        val testObject = createTestObject(

            width = null,
            height = 1,
            url = null,
            additional_props = null

        )

        val actual = testObject.imageUrl()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with story, fails in glide and uses lead_art url for fallback`() {
        val url = "thumbnailUrl"
        val expected = "baseUrl/$url"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.arcxpContentConfig().baseUrl } returns "baseUrl/"
        val testObject = createTestObject(
            lead_art = lead_art(
                additional_props = mapOf(
                    Pair(
                        "thumbnailResizeUrl",
                        "thumbnailUrl"
                    )
                )
            )
        )

        val actual = testObject.fallback()

        assertEquals(expected, actual)
    }

    @Test
    fun `imageUrl with story, fails in glide and uses lead_art promo_items url for fallback`() {
        val url = "thumbnailUrl"
        val expected = "baseUrl/$url"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.arcxpContentConfig().baseUrl } returns "baseUrl/"
        val testObject = createTestObject(
            lead_art = lead_art(
                lead_art_additional_props = mapOf(
                    Pair(
                        "thumbnailResizeUrl",
                        "thumbnailUrl"
                    )
                )
            )
        )

        val actual = testObject.fallback()

        assertEquals(expected, actual)
    }

    @Test
    fun `imageUrl with story, returns empty string when there is no other url for fallback`() {
        val testObject = createTestObject()

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `image fallback - promoItem lead_art promoItem basic additional properties can't find thumbnail url`() {
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
                            additional_properties = mapOf(Pair("a", "b"))
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
    fun `image fallback - promoItem basic`() {
        val url = "thumbnailUrl"
        val expected = "baseUrl/$url"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.arcxpContentConfig().baseUrl } returns "baseUrl/"

        val testObject = createTestObject(
            additional_props = mapOf(Pair(THUMBNAIL_RESIZE_URL_KEY, url)),
            lead_art = null
        )

        val actual = testObject.fallback()

        assertEquals(expected, actual)
    }

    @Test
    fun `image fallback - promoItem basic with non string in map`() {
        val url = "thumbnailUrl"
        val expected = "baseUrl/$url"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.arcxpContentConfig().baseUrl } returns "baseUrl/"

        val testObject = createTestObject(
//            basic = basic(
//                additional_props = mapOf(
//                    Pair(
//                        THUMBNAIL_RESIZE_URL_KEY, 654
//                    )
//                )
//            ),
            additional_props = mapOf(
                Pair(
                    THUMBNAIL_RESIZE_URL_KEY, 76534
                )
            ),
            lead_art = null
        )

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `image fallback - promoItem basic with null in map`() {
        val url = "thumbnailUrl"
        val expected = "baseUrl/$url"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.arcxpContentConfig().baseUrl } returns "baseUrl/"

        val testObject = createTestObject(
//            basic = basic(
//                additional_props = mapOf(
//                    Pair(
//                        THUMBNAIL_RESIZE_URL_KEY, null
//                    )
//                )
//            ),
            additional_props = mapOf(
                Pair(
                    THUMBNAIL_RESIZE_URL_KEY, null
                )
            )
        )

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
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
    fun `image fallback - promoItem basic & lead_art null`() {
        val testObject = createCollectionWithoutPromoItem(
            promoItem = PromoItem(basic = null, lead_art = null)
        )

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
            url = url,
            additional_props = mapOf(Pair("thumbnailResizeUrl", "thumbnailUrl"))
        )


        val actual = testObject.fallback()

        assertEquals(expected, actual)
    }

    @Test
    fun `image fallback finds url in promoItem null `() {
        val testObject = createCollectionWithoutPromoItem("nonVideo")

        val actual = testObject.fallback()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with story, height and width are null returns empty`() {
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns 1000
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = any(), height = any())
        } returns resizedHeightURL
        every {
            ArcXPContentSDK.resizer().resizeWidth(url = any(), width = any())
        } returns resizedWidthURL

        val testObject =
            createCollectionWithoutPromoItem("nonVideo", promoItem = PromoItem(null, null))

        val actual = testObject.imageUrl()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `imageUrl with story, image is smaller than device does not resize url`() {
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
    fun `imageUrl with story, image width is larger than device resizes to device size`() {
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
    fun `imageUrl with story, image height is larger than device resizes to device size`() {
        val deviceSize = 100
        val inputUrl = "=/url"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns deviceSize
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = "url", height = deviceSize)
        } returns resizedHeightURL

        val testObject = createTestObject(
            width = 1,
            height = 3000,
            url = inputUrl,
            additional_props = mapOf(Pair("resizeUrl", "=/url"))
        )


        val actual = testObject.imageUrl()

        assertEquals(resizedHeightURL, actual)
    }

    @Test
    fun `imageUrl with story, video height is larger than device resizes to device size`() {
        val deviceSize = 100
        val inputUrl = "=/url"
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.resizer().getScreenSize() } returns deviceSize
        every {
            ArcXPContentSDK.resizer().resizeHeight(url = "url", height = deviceSize)
        } returns resizedHeightURL

        val testObject = createTestObject(type = "video", height = 3000, width = 1, url = "=/url")


        val actual = testObject.imageUrl()

        assertEquals(resizedHeightURL, actual)
    }


    @Test
    fun `title in story, returns title from title extention`() {
        val expected = "This is a title"
        val headline = Headline(basic = expected, null, null, null, null, null)

        val testObject = createTestObject(height = 1, width = 3000, title = headline)

        val actual = testObject.title()

        assertEquals(expected, actual)
    }

    @Test
    fun `title in story, returning empty string when headlines basic is null`() {
        val headline = Headline(basic = null, null, null, null, null, null)

        val testObject = createTestObject(height = 1, width = 3000, title = headline)

        val actual = testObject.title()

        assertTrue(actual.isEmpty())
    }


    @Test
    fun `title in  story, returns empty string when headline is null`() {
        val testObject = createTestObject(height = 1, width = 3000)

        val actual = testObject.title()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `description in story, returns description from description extention`() {
        val expected = "This is a description"
        val description = Description(expected)

        val testObject = createTestObject(height = 1, width = 3000, description = description)

        val actual = testObject.description()

        assertEquals(expected, actual)
    }

    @Test
    fun `description in story, returns empty string when description is null`() {

        val testObject = createTestObject(height = 1, width = 3000, description = null)

        val actual = testObject.description()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `description in story, returns empty string when description basic is null`() {
        val description = Description(null)
        val testObject = createTestObject(height = 1, width = 3000, description = description)

        val actual = testObject.description()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `subheadline in story, returns subheadlines from subheadlines extention`() {
        val expected = "This is subheadlines"
        val subheadlines = Subheadlines(expected)

        val testObject = createTestObject(height = 1, width = 3000, subheadlines = subheadlines)

        val actual = testObject.subheadlines()

        assertEquals(expected, actual)
    }

    @Test
    fun `subheadlines in story, returns empty string when subheadline is null`() {

        val testObject = createTestObject(height = 1, width = 3000, subheadlines = null)

        val actual = testObject.subheadlines()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `subheadlines in story, returns empty string when subheadline basic is null`() {
        val subheadlines = Subheadlines(null)
        val testObject = createTestObject(height = 1, width = 3000, subheadlines = subheadlines)

        val actual = testObject.subheadlines()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `author in story, returns author from author extention`() {
        val expected = "Author"

        val testObject = createTestObject(
            height = 1, width = 3000, credits = Credits(
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
    fun `author in story, returns empty string when name is null`() {

        val testObject = createTestObject(
            height = 1, width = 3000, credits = Credits(
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
    fun `author in story, returns empty string when credit is null`() {

        val testObject = createTestObject(height = 1, width = 3000)

        val actual = testObject.author()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `author in story, gets empty value from author extention when there isn't an author`() {

        val testObject = createTestObject(height = 1, width = 3000)

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
    fun `author in story, returns empty string when credits by is empty`() {
        val testObject =
            createTestObject(height = 1, width = 3000, credits = Credits(listOf(), null))


        val actual = testObject.author()

        assertTrue(actual.isEmpty())
    }


    @Test
    fun `format date with image returns empty when no value`() {
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

        val actual = testObject.date()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `format date with image returns formatted date`() {
        val expected = "Oct 31, 2022"
        val published = Date.from(Instant.ofEpochSecond(1667241768))
        val testObject = createTestObject(
            height = 1,
            width = 3000,
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
    fun `for coverage`() {
        val testObject = createCollectionWithoutPromoItem(
            publishing = ArcXPStory.Publishing(
                scheduled_operations = ArcXPStory.Publishing.ScheduledOperations(
                    publish_edition = listOf(
                        ArcXPStory.Publishing.ScheduledOperations.Edition(
                            operation = null,
                            operation_revision_id = null,
                            operation_edition = null,
                            operation_date = null,
                            additional_properties = null
                        )
                    ), unpublish_edition = null, additional_properties = null
                ), has_published_edition = true
            )
        )


        //scheduled_operations coverage
        testObject.publishing?.scheduled_operations?.publish_edition
        testObject.publishing?.scheduled_operations?.publish_edition?.get(0)?.operation
        testObject.publishing?.scheduled_operations?.publish_edition?.get(0)?.operation_revision_id
        testObject.publishing?.scheduled_operations?.publish_edition?.get(0)?.operation_edition
        testObject.publishing?.scheduled_operations?.publish_edition?.get(0)?.operation_date
        testObject.publishing?.scheduled_operations?.publish_edition?.get(0)?.additional_properties
        testObject.publishing?.scheduled_operations?.additional_properties
        testObject.publishing?.scheduled_operations?.unpublish_edition


        testObject.publishing?.has_published_edition


        //TBH I am not sure why we need to call the generated getters here and not in main class
        //this test is not useful except for coverage metrics

    }

    private fun createVideo(
        width: Int? = null,
        url: String? = null,
        height: Int? = 0,
        published_date: Date? = null,
        title: Headline? = null,
        description: Description? = null,
        additional_props: Map<String, String>? = null,
        lead_art: PromoItem.PromoItemBasic? = null
    ) =
        createTestObject(
            type = "video",
            height = height,
            width = width,
            url = url,
            published_date = published_date,
            title = title,
            description = description,
            additional_props = additional_props,
            lead_art = lead_art
        )

    private fun createTestObject(
        type: String = "non Video",
        url: String? = null,
        width: Int? = null,
        height: Int? = null,
        title: Headline? = null,
        description: Description? = null,
        subheadlines: Subheadlines? = null,
        credits: Credits? = null,
        published_date: Date? = null,
        additional_props: Map<String, *>? = null,
        lead_art: PromoItem.PromoItemBasic? = null
    ) =
        ArcXPStory(
            _id = "id",
            headlines = title,
            description = description,
            credits = credits,
            promoItem = PromoItem(
                basic = basic(
                    width = width,
                    height = height,
                    url = url,
                    additional_props = additional_props
                ),
                lead_art = lead_art
            ),
            type = type,
            duration = null,
            subheadlines = subheadlines,
            version = null,
            alignment = null,
            created_date = null,
            last_updated_date = null,
            publish_date = published_date,
            first_publish_date = null,
            display_date = null,
            geo = null,
            language = null,
            location = null,
            address = null,
            content_elements = null,
            related_content = null,
            publishing = ArcXPStory.Publishing(
                scheduled_operations = ArcXPStory.Publishing.ScheduledOperations(
                    publish_edition = null,
                    unpublish_edition = null,
                    additional_properties = null
                ), has_published_edition = null
            ),
            revision = null,
            website = null,
            websites = null,
            website_url = null,
            short_url = null,
            channels = null,
            owner = null,
            vanity_credits = null,
            editor_note = null,
            taxonomy = null,
            copyright = null,
            label = null,
            content = null,
            canonical_url = null,
            canonical_website = null,
            source = null,
            subtype = null,
            planning = null,
            pitches = null,
            syndication = null,
            distributor = null,
            tracking = null,
            comments = null,
            slug = null,
            content_restrictions = null,
            content_aliases = null,
            corrections = null,
            rendering_guides = null,
            status = null,
            workFlow = null,
            additional_properties = null
        )

    private fun basic(
        width: Int? = null,
        height: Int? = null,
        url: String? = null,
        additional_props: Map<String, *>? = null
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
        type: String = "type",
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
        lead_art: PromoItem.PromoItemBasic? = null,
        publishing: ArcXPStory.Publishing? = null
    ) =
        ArcXPStory(
            _id = "id",
            headlines = title,
            description = description,
            credits = credits,
            promoItem = promoItem,
            type = type,
            publish_date = published_date,
            duration = null,
            subheadlines = null,
            version = null,
            alignment = null,
            created_date = null,
            last_updated_date = null,
            first_publish_date = null,
            display_date = null,
            geo = null,
            language = null,
            location = null,
            address = null,
            content_elements = null,
            related_content = null,
            publishing = publishing,
            revision = null,
            website = null,
            websites = null,
            website_url = null,
            short_url = null,
            channels = null,
            owner = null,
            vanity_credits = null,
            editor_note = null,
            taxonomy = null,
            copyright = null,
            label = null,
            content = null,
            canonical_url = null,
            canonical_website = null,
            source = null,
            subtype = null,
            planning = null,
            pitches = null,
            syndication = null,
            distributor = null,
            tracking = null,
            comments = null,
            slug = null,
            content_restrictions = null,
            content_aliases = null,
            corrections = null,
            rendering_guides = null,
            status = null,
            workFlow = null,
            additional_properties = null,
        )
}
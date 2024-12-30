package com.arcxp.content

import android.app.Application
import android.content.res.Resources
import com.arcxp.ArcXPMobileSDK
import com.arcxp.ArcXPMobileSDK.imageUtils
import com.arcxp.commons.image.CollectionImageUtil
import com.arcxp.commons.testutils.TestUtils.basic
import com.arcxp.commons.testutils.TestUtils.createCollectionWithoutPromoItem
import com.arcxp.commons.testutils.TestUtils.createContentTestObject
import com.arcxp.commons.testutils.TestUtils.createContentTestObjectWithoutPromoItem
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.content.extendedModels.author
import com.arcxp.content.extendedModels.date
import com.arcxp.content.extendedModels.description
import com.arcxp.content.extendedModels.fallback
import com.arcxp.content.extendedModels.imageUrl
import com.arcxp.content.extendedModels.isVideo
import com.arcxp.content.extendedModels.thumbnail
import com.arcxp.content.extendedModels.title
import com.arcxp.content.models.Credits
import com.arcxp.content.models.Description
import com.arcxp.content.models.Headline
import com.arcxp.content.models.PromoItem
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.Date

class ArcXPContentElementTest {

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
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `thumbnail returns url`() {
        val promoItem = PromoItem(basic = basic(), lead_art = null)
        every { imageUtils.thumbnail(promoItem) } returns "resizedToThumbnail"

        val testObject = createContentTestObject(type = "video")

        val actual = testObject.thumbnail()

        assertEquals("resizedToThumbnail", actual)
    }

    @Test
    fun `thumbnail returns empty`() {

        val testObject = createContentTestObjectWithoutPromoItem(type = "video")

        val actual = testObject.thumbnail()

        assertEquals("", actual)
    }

    @Test
    fun `fallback returns url`() {
        val promoItem = PromoItem(basic = basic(), lead_art = null)
        every { imageUtils.fallback(promoItem) } returns "resizedToThumbnail"

        val testObject = createContentTestObject(type = "video")

        val actual = testObject.fallback()

        assertEquals("resizedToThumbnail", actual)
    }

    @Test
    fun `fallback returns empty`() {

        val testObject = createCollectionWithoutPromoItem(type = "video")

        val actual = testObject.fallback()

        assertEquals("", actual)
    }

    @Test
    fun `imageUrl returns url`() {
        val promoItem = PromoItem(basic = basic(), lead_art = null)
        every { imageUtils.imageUrl(promoItem.basic!!) } returns "resizedToThumbnail"

        val testObject = createContentTestObject(type = "video")

        val actual = testObject.imageUrl()

        assertEquals("resizedToThumbnail", actual)
    }

    @Test
    fun `imageUrl returns empty`() {

        val testObject = createCollectionWithoutPromoItem(type = "video")

        val actual = testObject.imageUrl()

        assertEquals("", actual)
    }

    @Test
    fun `title in element, gets headline from title extention`() {
        val expected = "This is a title"
        val headline = Headline(basic = expected, null, null, null, null, null)

        val testObject = createContentTestObject(type = "video", title = headline)

        val actual = testObject.title()

        assertEquals(expected, actual)
    }

    @Test
    fun `title in element, returns empty string when headline is null`() {
        val testObject = createContentTestObject(type = "video")

        val actual = testObject.title()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `title in element, returns empty string when headline basic is null`() {
        val headline = Headline(null, null, null, null, null, null)
        val testObject = createContentTestObject(type = "video", title = headline)

        val actual = testObject.title()

        assertTrue(actual.isEmpty())
    }


    @Test
    fun `description in element, getting description from description extention`() {
        val expected = "This is a description"
        val description = Description(expected)

        val testObject = createContentTestObject(type = "video", description = description)

        val actual = testObject.description()

        assertEquals(expected, actual)
    }

    @Test
    fun `description in element, returns empty string when description is null`() {
        val testObject = createContentTestObject(type = "video")

        val actual = testObject.description()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `description in element, returns empty string when description basic is null`() {
        val testObject = createContentTestObject(
            type = "video",
            height = 1,
            width = 3000,
            description = Description(null)
        )

        val actual = testObject.description()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `get author with image, returns empty string when name is null`() {
        val testObject = createContentTestObject(
            height = 1,
            width = 3000,
            credits = Credits(
                by = listOf(
                    Credits.CreditsBy(
                        _id = null,
                        image = null,
                        name = null,
                        org = null,
                        slug = null,
                        social_links = null,
                        type = null,
                        url = null,
                        version = null,
                        first_name = null,
                        middle_name = null,
                        last_name = null,
                        suffix = null,
                        byline = null,
                        location = null,
                        division = null,
                        email = null,
                        role = null,
                        expertise = null,
                        affiliation = null,
                        languages = null,
                        bio = null,
                        long_bio = null,
                        books = null,
                        education = null,
                        awards = null,
                        contributor = null,
                        subtype = null,
                        channels = null,
                        alignment = null,
                        additional_properties = null
                    )
                ), photos_by = listOf()
            )
        )

        val actual = testObject.author()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `get author with image, returns empty string when credits is null`() {

        val testObject = createContentTestObject(height = 1, width = 3000, credits = null)


        val actual = testObject.author()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `get author with image, returns empty string when credits by is null`() {
        val testObject = createContentTestObject(height = 1, width = 3000, credits = Credits(null, null))

        val actual = testObject.author()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `get author with image, returns empty string when credits by is empty`() {
        val testObject =
            createContentTestObject(height = 1, width = 3000, credits = Credits(listOf(), null))

        val actual = testObject.author()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `get author with image, returns empty string when credit is null`() {
        val testObject = createContentTestObject(height = 1, width = 3000)

        val actual = testObject.author()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `get author with image, gets empty value from author extention when there isn't an author`() {
        val testObject = createContentTestObject(height = 1, width = 3000)

        val actual = testObject.author()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `format date with image returns empty when no value`() {
        val testObject = createContentTestObject(
            credits = Credits(
                by = listOf(
                    Credits.CreditsBy(
                        _id = null,
                        image = null,
                        name = "Author",
                        org = null,
                        slug = null,
                        social_links = null,
                        type = null,
                        url = null,
                        version = null,
                        first_name = null,
                        middle_name = null,
                        last_name = null,
                        suffix = null,
                        byline = null,
                        location = null,
                        division = null,
                        email = null,
                        role = null,
                        expertise = null,
                        affiliation = null,
                        languages = null,
                        bio = null,
                        long_bio = null,
                        books = null,
                        education = null,
                        awards = null,
                        contributor = null,
                        subtype = null,
                        channels = null,
                        alignment = null,
                        additional_properties = null
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
        val testObject = createContentTestObject(
            published_date = published,
            credits = Credits(
                by = listOf(
                    Credits.CreditsBy(
                        _id = null,
                        image = null,
                        name = "Author",
                        org = null,
                        slug = null,
                        social_links = null,
                        type = null,
                        url = null,
                        version = null,
                        first_name = null,
                        middle_name = null,
                        last_name = null,
                        suffix = null,
                        byline = null,
                        location = null,
                        division = null,
                        email = null,
                        role = null,
                        expertise = null,
                        affiliation = null,
                        languages = null,
                        bio = null,
                        long_bio = null,
                        books = null,
                        education = null,
                        awards = null,
                        contributor = null,
                        subtype = null,
                        channels = null,
                        alignment = null,
                        additional_properties = null
                    )
                ), photos_by = listOf()
            )
        )
        val actual = testObject.date()

        assertEquals(expected, actual)
    }

    @Test
    fun `isVideo returns true if video`() {
        val testObject = createContentTestObject(type = "video")

        assertTrue(testObject.isVideo())
    }
    
}
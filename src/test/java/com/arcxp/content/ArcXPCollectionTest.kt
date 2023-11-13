package com.arcxp.content

import android.app.Application
import android.content.res.Resources
import com.arcxp.ArcXPMobileSDK
import com.arcxp.ArcXPMobileSDK.imageUtils
import com.arcxp.commons.image.CollectionImageUtil
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.content.extendedModels.ArcXPCollection
import com.arcxp.content.extendedModels.author
import com.arcxp.content.extendedModels.date
import com.arcxp.content.extendedModels.fallback
import com.arcxp.content.extendedModels.imageUrl
import com.arcxp.content.extendedModels.isVideo
import com.arcxp.content.extendedModels.thumbnail
import com.arcxp.content.models.Credits
import com.arcxp.content.models.Description
import com.arcxp.content.models.Headlines
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
import kotlin.test.assertFalse

class ArcXPCollectionTest {

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
        val url = "url"
        every { imageUtils.thumbnail(promoItem) } returns "resizedToThumbnail"

        val testObject = createTestObject(type = "video")

        val actual = testObject.thumbnail()

        assertEquals("resizedToThumbnail", actual)
    }

    @Test
    fun `thumbnail returns empty`() {

        val testObject = createCollectionWithoutPromoItem(type = "video")

        val actual = testObject.thumbnail()

        assertEquals("", actual)
    }

    @Test
    fun `fallback returns url`() {
        val promoItem = PromoItem(basic = basic(), lead_art = null)
        val url = "url"
        every { imageUtils.fallback(promoItem) } returns "resizedToThumbnail"

        val testObject = createTestObject(type = "video")

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

        val testObject = createTestObject(type = "video")

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
    fun `get author with image, getting author from author extension`() {
        val expected = "Author"

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
                ), photos_by = emptyList()
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
    fun `get author with image, gets empty value from author extension when there isn't an author`() {

        val testObject = createTestObject(height = 1, width = 3000)

        val actual = testObject.author()

        assertTrue(actual.isEmpty())
    }

    @Test
    fun `format date with image`() {
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
    fun `isVideo returns true if video`() {
        val testObject = createTestObject(type = "video")

        assertTrue(testObject.isVideo())
    }

    @Test
    fun `isVideo returns false if not video`() {
        val testObject = createTestObject(type = "story")

        assertFalse(testObject.isVideo())
    }

    private fun createTestObject(
        type: String = "not a video",
        url: String? = null,
        width: Int? = null,
        height: Int? = null,
        title: Headlines? = null,
        description: Description? = null,
        credits: Credits? = null,
        published_date: Date? = null,
        additional_props: Map<String, String>? = null,
        basic: PromoItem.PromoItemBasic? = null,
        lead_art: PromoItem.PromoItemBasic? = null
    ) =
        ArcXPCollection(
            id = "id",
            headlines = title ?: Headlines(null),
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
            modified_on = null,
            publishedDate = published_date,
            duration = null,
            subheadlines = null
        )

    private fun createCollectionWithoutPromoItem(
        type: String = "type",
        promoItem: PromoItem? = null,
        title: Headlines? = null,
        description: Description? = null,
        credits: Credits? = null,
        published_date: Date? = null
    ) =
        ArcXPCollection(
            id = "id",
            headlines = title ?: Headlines(null),
            description = description,
            credits = credits,
            promoItem = promoItem,
            type = type,
            modified_on = null,
            publishedDate = published_date,
            duration = null,
            subheadlines = null
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
            additional_properties = additional_props,
            auth = null
        )
    }

    private fun lead_art(
        width: Int? = null,
        height: Int? = null,
        additional_props: Map<String, String>? = null,
        lead_art_additional_props: Map<String, String>? = null,
        promo: PromoItem? = PromoItem(
            basic(
                width = width,
                height = height,
                additional_props = lead_art_additional_props
            ), null
        )
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
            promo_items = promo,
            additional_properties = additional_props,
            auth = null

        )
    }
}

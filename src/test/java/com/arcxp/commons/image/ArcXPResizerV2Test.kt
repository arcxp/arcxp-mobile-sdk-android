package com.arcxp.commons.image

import com.arcxp.commons.testutils.TestUtils.basic
import com.arcxp.commons.testutils.TestUtils.createImageObject
import com.arcxp.commons.util.Constants
import com.google.ads.interactivemedia.v3.internal.id
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull

class ArcXPResizerV2Test {

    internal lateinit var testObject: ArcXPResizerV2

    val baseUrl = "abc"
    val expected = "expected"

    @Before
    fun setup() {
        testObject = ArcXPResizerV2(baseUrl)
    }

    @Test
    fun `test isValid true`() {
        val image = createImageObject(
            auth = mapOf("1" to "abcdef")
        )

        val actual = testObject.isValid(image)

        assertTrue(actual)
    }

    @Test
    fun `test isValid promoItem false`() {
        val image = basic(
            auth = null
        )

        val actual = testObject.isValid(image)

        assertFalse(actual)
    }

    @Test
    fun `test isValid promoItem true`() {
        val image = basic(
            auth = mapOf("1" to "abcdef")
        )

        val actual = testObject.isValid(image)

        assertTrue(actual)
    }

    @Test
    fun `test isValid false`() {
        val image = createImageObject(
            auth = null
        )

        val actual = testObject.isValid(image)

        assertFalse(actual)
    }

    @Test
    fun `test getAuthKey returns null`() {
        val actual = testObject.getAuthKey(null)

        assertNull(actual)
    }

    @Test
    fun `test getAuthKey empty map returns null`() {
        assertNull(testObject.getAuthKey(emptyMap()))
    }

    @Test
    fun `test getAuthKey non integer keys map returns null`() {
        assertNull(testObject.getAuthKey(mapOf("asdf" to "abc")))
    }

    @Test
    fun `test getAuthKey min integer key map returns expected`() {
        assertEquals(expected, testObject.getAuthKey(mapOf(Int.MIN_VALUE.toString() to expected)))
    }

    @Test
    fun `test getAuthKey mix of keys map returns highest key`() {

        val actual = testObject.getAuthKey(mapOf("44asdf" to "abc", "2" to "def", "3" to expected))

        assertEquals(expected, actual)
    }

    @Test
    fun `test getAuthKey some matching keys`() {

        val actual = testObject.getAuthKey(mapOf("44asdf" to "abc", "3" to "def", "3" to expected))

        assertEquals(expected, actual)
    }

    @Test
    fun `test getAuthKey all matching keys`() {

        val actual = testObject.getAuthKey(mapOf("3" to "abc", "3" to "def", "3" to expected))

        assertEquals(expected, actual)
    }

    @Test
    fun `test getAuthKey returns key`() {
        val image = createImageObject(
            auth = mapOf("1" to "abc", "2" to "def", "3" to "ghi")
        )

        val actual = testObject.getAuthKey(image.auth)

        assertEquals(actual, "ghi")
    }

    @Test
    fun `getV2Url image returns null`() {
        val image = createImageObject(
            url = "abc",
            auth = mapOf("1" to "abc", "2" to "def", "3" to "ghi")
        )
        val actual = testObject.getV2Url(image)

        assertEquals(null, actual)
    }

    @Test
    fun `getV2Url promo item returns url`() {
        val item = basic(
            id = "id",
            url = "/abc.def",
            auth = mapOf("1" to "abc", "2" to "def", "3" to "ghi")
        )
        val url = "abc/resizer/v2/id.def?auth=ghi"

        val actual = testObject.getV2Url(item)

        assertEquals(url, actual)
    }

    @Test
    fun `resizeWidth image returns url`() {
        val image = createImageObject(
            url = "abc.def",
            auth = mapOf("1" to "abc", "2" to "def", "3" to "ghi")
        )
        val url = "abc/resizer/v2/id.def?auth=ghi&width=10"

        val actual = testObject.resizeWidth(image, 10)

        assertEquals(url, actual)
    }

    @Test
    fun `resizeWidth image returns null`() {
        val image = createImageObject(
            url = null,
            auth = mapOf("1" to "abc", "2" to "def", "3" to "ghi")
        )
        val actual = testObject.resizeWidth(image, 10)

        assertEquals(null, actual)
    }

    @Test
    fun `resizeWidth image bad url returns null`() {
        val image = createImageObject(
            url = "abc/abc",
            auth = mapOf("1" to "abc", "2" to "def", "3" to "ghi")
        )
        val actual = testObject.resizeWidth(image, 10)

        assertEquals(null, actual)
    }

    @Test
    fun `resizeHeight image returns url`() {
        val image = createImageObject(
            url = "abc.def",
            auth = mapOf("1" to "abc", "2" to "def", "3" to "ghi")
        )
        val url = "abc/resizer/v2/id.def?auth=ghi&height=10"

        val actual = testObject.resizeHeight(image, 10)

        assertEquals(url, actual)
    }

    @Test
    fun `resizeHeight image returns null`() {
        val image = createImageObject(
            url = null,
            auth = mapOf("1" to "abc", "2" to "def", "3" to "ghi")
        )
        val actual = testObject.resizeHeight(image, 10)

        assertEquals(null, actual)
    }

    @Test
    fun `resizeWidth url returns url`() {
        //assume a valid V2 url with auth is passed in
        val url = "abc.def"
        val newUrl = "$url&width=10"

        val actual = testObject.resizeWidth(url, 10)

        assertEquals(newUrl, actual)
    }

    @Test
    fun `resizeHeight url returns url`() {
        //assume a valid V2 url with auth is passed in
        val url = "abc.def"
        val newUrl = "$url&height=10"

        val actual = testObject.resizeHeight(url, 10)

        assertEquals(newUrl, actual)
    }

    @Test
    fun `createThumnail returns url`() {
        val url = "abc"
        val newUrl = "$url&width=${Constants.THUMBNAIL_SIZE}"

        val actual = testObject.createThumbnail(url)

        assertEquals(newUrl, actual)
    }

    @Test
    fun `getV2Url with null url`() {
        val expected = null
        val image = createImageObject(
            auth = mapOf("1" to "abcdef")
        )
        val actual = testObject.getV2Url(image = image)

        assertEquals(expected, actual)
    }

    @Test
    fun `getV2Url with url with no extension`() {
        val expected = null
        val image = createImageObject(
            auth = mapOf("1" to "abcdef"),
            url = "http://example.com/image/abc"
        )
        val actual = testObject.getV2Url(image = image)

        assertEquals(expected, actual)
    }

    @Test
    fun `getV2Url with url with no extension no slash`() {
        val expected = null
        val image = createImageObject(
            auth = mapOf("1" to "abcdef"),
            url = "abc"
        )
        val actual = testObject.getV2Url(image = image)

        assertEquals(expected, actual)
    }

    @Test
    fun `getV2Url with url with no extension has slash`() {
        val expected = null
        val image = createImageObject(
            auth = mapOf("1" to "abcdef"),
            url = "/abc"
        )
        val actual = testObject.getV2Url(image = image)

        assertEquals(expected, actual)
    }

    @Test
    fun `getV2Url with url with extension and slash`() {
        val expected = "abc/resizer/v2/id.def?auth=abcdef"
        val image = createImageObject(
            auth = mapOf("1" to "abcdef"),
            url = "/abc.def"
        )
        val actual = testObject.getV2Url(image = image)

        assertEquals(expected, actual)
    }

    @Test
    fun `getV2Url with url with no extension no slash has dot`() {
        val expected = "abc/resizer/v2/id.def?auth=abcdef"
        val image = createImageObject(
            auth = mapOf("1" to "abcdef"),
            url = "abc.def"
        )
        val actual = testObject.getV2Url(image = image)

        assertEquals(expected, actual)
    }

    @Test
    fun `getV2Url with url with stuff after extension`() {
        val expected = "abc/resizer/v2/id.jpg?param=value?auth=abcdef"
        val image = createImageObject(
            auth = mapOf("1" to "abcdef"),
            url = "https://arcsales-arcsales-sandbox./resizer/aibwh3iEM=/d30t.net/11-01-2023/t_c9887file_1920x1080_5400_v4_.jpg?param=value"
        )
        val actual = testObject.getV2Url(image = image)

        assertEquals(expected, actual)
    }

    @Test
    fun `getV2Url with url with extension`() {
        val expected = "abc/resizer/v2/id.jpg?auth=abcdef"
        val image = createImageObject(
            auth = mapOf("1" to "abcdef"),
            url = "https://arcsales-arcsales/resizer/aibwa2iV1uc3_xTdWV.net/11-01-2023/t_c9887f7c8e1_name_file_1920x1080_5400_v4_.jpg"
        )
        val actual = testObject.getV2Url(image = image)

        assertEquals(expected, actual)
    }

    @Test
    fun `getV2Url promoItem with null url`() {
        val expected = null
        val image = basic(
            auth = mapOf("1" to "abcdef")
        )
        val actual = testObject.getV2Url(item = image)

        assertEquals(expected, actual)
    }

    @Test
    fun `getV2Url promoItem with url with no extension`() {
        val expected = null
        val image = basic(
            auth = mapOf("1" to "abcdef"),
            url = "http://example.com/image/abc"
        )
        val actual = testObject.getV2Url(item = image)

        assertEquals(expected, actual)
    }

    @Test
    fun `getV2Url promoItem with url with no extension no slash`() {
        val expected = null
        val image = basic(
            auth = mapOf("1" to "abcdef"),
            url = "abc"
        )
        val actual = testObject.getV2Url(item = image)

        assertEquals(expected, actual)
    }

    @Test
    fun `getV2Url promoItem with url with no extension no slash has dot`() {
        val expected = "abc/resizer/v2/id.def?auth=abcdef"
        val image = basic(
            id = "id",
            auth = mapOf("1" to "abcdef"),
            url = "abc.def"
        )
        val actual = testObject.getV2Url(item = image)

        assertEquals(expected, actual)
    }

    @Test
    fun `getV2Url promoItem with url with stuff after extension`() {
        val expected = "abc/resizer/v2/id.jpg?param=value?auth=abcdef"
        val image = basic(
            id = "id",
            auth = mapOf("1" to "abcdef"),
            url = "https://arcsales-arcsales-sandbox./resizer/aibwh3iEM=/d30t.net/11-01-2023/t_c9887file_1920x1080_5400_v4_.jpg?param=value"
        )
        val actual = testObject.getV2Url(item = image)

        assertEquals(expected, actual)
    }

    @Test
    fun `getV2Url promoItem with url with extension`() {
        val expected = "abc/resizer/v2/id.jpg?auth=abcdef"
        val image = basic(
            id = "id",
            auth = mapOf("1" to "abcdef"),
            url = "https://arcsales-arcsales/resizer/aibwa2iV1uc3_xTdWV.net/11-01-2023/t_c9887f7c8e1_name_file_1920x1080_5400_v4_.jpg"
        )
        val actual = testObject.getV2Url(item = image)

        assertEquals(expected, actual)
    }
}
package com.arcxp.content.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commons.testutils.CoroutineTestRule
import com.arcxp.commons.testutils.TestUtils.createContentElement
import com.arcxp.commons.testutils.TestUtils.getJson
import com.arcxp.commons.util.MoshiController.toJson
import com.arcxp.commons.util.Utils
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DatabaseTest {


    // so this works, but doesn't count coverage on jacoco
    // the sql queries are compile time checked, so dunno
    // if we really need to test this interface
    // the tests are valid, so
    // i'll leave this in case we need an example mechanism to test db
    // but feel it doesn't have a lot of worth right now
    // this would be useful on a UI test or something maybe?


    private val testDispatcher = TestCoroutineDispatcher()

    private lateinit var testObject: ContentSDKDao
    private lateinit var db: Database

    @RelaxedMockK
    private lateinit var expectedDate: Date

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, Database::class.java
        )
            .setTransactionExecutor(testDispatcher.asExecutor())
            .setQueryExecutor(testDispatcher.asExecutor())
            .allowMainThreadQueries()
            .build()
        testObject = db.sdkDao()
        mockkObject(ArcXPMobileSDK)
        every { ArcXPMobileSDK.contentConfig().cacheTimeUntilUpdateMinutes } returns 5
        mockkObject(Utils)
        coEvery { Utils.createDate(any()) } returns expectedDate
        coEvery { Utils.createDate() } returns expectedDate

    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
        clearAllMocks()
    }

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @Test
    fun `insert And Get Collection`() = runTest {
        val uuid = "123"
        val expectedIndexValue = 12345
        val expectedCollection = CollectionItem(
            indexValue = expectedIndexValue,
            contentAlias = "contentAlias",
            uuid = uuid,
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val expectedJson = getJson("story1.json")
        testObject.insertCollectionItem(expectedCollection)
        testObject.insertJsonItem(
            JsonItem(
                uuid = uuid,
                jsonResponse = expectedJson,
                expiresAt = expectedDate
            )
        )

        val actual = testObject.getCollectionIndexedJson(
            collectionAlias = "contentAlias",
            from = 0,
            size = 1
        )
        assertEquals(1, actual.size)
        assertEquals(expectedJson, actual[0].jsonResponse)
        assertEquals(expectedIndexValue, actual[0].indexValue)

    }

    @Test
    fun `insert And Get Json`() = runTest {

        val expected = JsonItem("id", "response", expectedDate, expectedDate)
        testObject.insertJsonItem(expected)

        val actual = testObject.getJsonById("id")

        assertEquals(expected, actual)
    }

    @Test
    fun `item coverage from entity kt`() {
        val item = JsonItem(expiresAt = mockk(), uuid = "", jsonResponse = "")
        val expiration = (item as BaseItem).expiresAt
    }

    @Test
    fun `get section headers overwrites previous entry and returns expected value`() = runTest {

        val expected = SectionHeaderItem(
            sectionHeaderResponse = "response1",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val expected2 = SectionHeaderItem(
            sectionHeaderResponse = "response2",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        testObject.insertNavigation(sectionHeaderItem = expected)
        testObject.insertNavigation(sectionHeaderItem = expected2)

        val actual = testObject.getSectionList()?.sectionHeaderResponse
        assertEquals(expected2.sectionHeaderResponse, actual)
    }

    @Test
    fun `delete json item by id performs single deletion`() = runTest {


        val jsonItem1 = JsonItem(
            uuid = "id1",
            jsonResponse = "response",
            expiresAt = expectedDate,
            createdAt = expectedDate
        )
        val jsonItem2 = JsonItem(
            uuid = "id2",
            jsonResponse = "response",
            expiresAt = expectedDate,
            createdAt = expectedDate
        )
        val jsonItem3 = JsonItem(
            uuid = "id3",
            jsonResponse = "response",
            expiresAt = expectedDate,
            createdAt = expectedDate
        )
        val jsonItem4 = JsonItem(
            uuid = "id4",
            jsonResponse = "response",
            expiresAt = expectedDate,
            createdAt = expectedDate
        )
        val jsonItem5 = JsonItem(
            uuid = "id5",
            jsonResponse = "response",
            expiresAt = expectedDate,
            createdAt = expectedDate
        )
        val jsonItem6 = JsonItem(
            uuid = "id6",
            jsonResponse = "response",
            expiresAt = expectedDate,
            createdAt = expectedDate
        )
        testObject.insertJsonItem(jsonItem1)
        testObject.insertJsonItem(jsonItem2)
        testObject.insertJsonItem(jsonItem3)
        testObject.insertJsonItem(jsonItem4)
        testObject.insertJsonItem(jsonItem5)
        testObject.insertJsonItem(jsonItem6)


        assertEquals(6, testObject.countJsonItems())

        testObject.deleteJsonItemById(uuid = "id4")

        assertEquals(5, testObject.countJsonItems())

        assertNull(testObject.getJsonById("id4"))

    }

    @Test
    fun `delete oldest json item deletes only the oldest json item`() = runTest {
        val expectedDate = mockk<Date>()
        every { expectedDate.time } returns 123L
        val oldestDate = mockk<Date>()
        every { oldestDate.time } returns 1L
        val jsonItem1 = JsonItem(
            uuid = "id1",
            jsonResponse = "response",
            expiresAt = expectedDate,
            createdAt = expectedDate
        )
        val jsonItem2 = JsonItem(
            uuid = "id2",
            jsonResponse = "response",
            expiresAt = expectedDate,
            createdAt = expectedDate
        )
        val jsonItem3 = JsonItem(
            uuid = "id3",
            jsonResponse = "response",
            expiresAt = expectedDate,
            createdAt = expectedDate
        )
        val jsonItem4 = JsonItem(
            uuid = "id4",
            jsonResponse = "response",
            expiresAt = expectedDate,
            createdAt = expectedDate
        )
        val jsonItem5 = JsonItem(
            uuid = "id5",
            jsonResponse = "response",
            expiresAt = expectedDate,
            createdAt = oldestDate
        )
        val jsonItem6 = JsonItem(
            uuid = "id6",
            jsonResponse = "response",
            expiresAt = expectedDate,
            createdAt = expectedDate
        )
        testObject.insertJsonItem(jsonItem1)
        testObject.insertJsonItem(jsonItem2)
        testObject.insertJsonItem(jsonItem3)
        testObject.insertJsonItem(jsonItem4)
        testObject.insertJsonItem(jsonItem5)
        testObject.insertJsonItem(jsonItem6)


        assertEquals(6, testObject.countJsonItems())

        testObject.deleteOldestJsonItem()

        assertEquals(5, testObject.countJsonItems())

        assertNull(testObject.getJsonById("id5"))

    }

    @Test
    fun `get Collection returns only items within specified range`() = runTest {

        val collectionItem0 = CollectionItem(
            indexValue = 0,
            uuid = "0",
            contentAlias = "contentAlias",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val json0 = toJson(createContentElement(id = "0"))!!
        val jsonItem0 = JsonItem(
            uuid = "0",
            jsonResponse = json0,
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val expected0 =
            ContentSDKDao.IndexedJsonItem(indexValue = 0, toJson(createContentElement(id = "0"))!!)
        val collectionItem1 = CollectionItem(
            indexValue = 1,
            uuid = "1",
            contentAlias = "contentAlias",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val json1 = toJson(createContentElement(id = "1"))!!
        val jsonItem1 = JsonItem(
            uuid = "1",
            jsonResponse = json1,
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val expected1 =
            ContentSDKDao.IndexedJsonItem(indexValue = 1, toJson(createContentElement(id = "1"))!!)
        val collectionItem2 = CollectionItem(
            indexValue = 2,
            uuid = "2",
            contentAlias = "contentAlias",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val json2 = toJson(createContentElement(id = "2"))!!
        val jsonItem2 = JsonItem(
            uuid = "2",
            jsonResponse = json2,
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val expected2 =
            ContentSDKDao.IndexedJsonItem(indexValue = 2, toJson(createContentElement(id = "2"))!!)

        val collectionItem3 = CollectionItem(
            indexValue = 3,
            uuid = "3",
            contentAlias = "contentAlias",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val json3 = toJson(createContentElement(id = "3"))!!
        val jsonItem3 = JsonItem(
            uuid = "3",
            jsonResponse = json3,
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val expected3 =
            ContentSDKDao.IndexedJsonItem(indexValue = 3, toJson(createContentElement(id = "3"))!!)

        val collectionItem4 = CollectionItem(
            indexValue = 4,
            uuid = "4",
            contentAlias = "contentAlias",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val json4 = toJson(createContentElement(id = "4"))!!
        val jsonItem4 = JsonItem(
            uuid = "4",
            jsonResponse = json4,
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val expected4 =
            ContentSDKDao.IndexedJsonItem(indexValue = 4, toJson(createContentElement(id = "4"))!!)

        val collectionItem5 = CollectionItem(
            indexValue = 5,
            uuid = "5",
            contentAlias = "contentAlias",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val json5 = toJson(createContentElement(id = "5"))!!
        val jsonItem5 = JsonItem(
            uuid = "5",
            jsonResponse = json5,
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val expected5 =
            ContentSDKDao.IndexedJsonItem(indexValue = 5, toJson(createContentElement(id = "5"))!!)

        val collectionItem6 = CollectionItem(
            indexValue = 6,
            uuid = "6",
            contentAlias = "contentAlias",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val json6 = toJson(createContentElement(id = "6"))!!
        val jsonItem6 = JsonItem(
            uuid = "6",
            jsonResponse = json6,
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val expected6 =
            ContentSDKDao.IndexedJsonItem(indexValue = 6, toJson(createContentElement(id = "6"))!!)

        testObject.insertCollectionItem(collectionItem0)
        testObject.insertCollectionItem(collectionItem1)
        testObject.insertCollectionItem(collectionItem2)
        testObject.insertCollectionItem(collectionItem3)
        testObject.insertCollectionItem(collectionItem4)
        testObject.insertCollectionItem(collectionItem5)
        testObject.insertCollectionItem(collectionItem6)
        testObject.insertJsonItem(jsonItem0)
        testObject.insertJsonItem(jsonItem1)
        testObject.insertJsonItem(jsonItem2)
        testObject.insertJsonItem(jsonItem3)
        testObject.insertJsonItem(jsonItem4)
        testObject.insertJsonItem(jsonItem5)
        testObject.insertJsonItem(jsonItem6)

        val actual = testObject.getCollectionIndexedJson(
            collectionAlias = "contentAlias",
            from = 1,
            size = 5
        )


        assertEquals(5, actual.size)


        assertFalse(actual.contains(expected0))
        assertTrue(actual.contains(expected1))
        assertTrue(actual.contains(expected2))
        assertTrue(actual.contains(expected3))
        assertTrue(actual.contains(expected4))
        assertTrue(actual.contains(expected5))
        assertFalse(actual.contains(expected6))
    }

    @Test
    fun `get Collections returns all collection items`() = runTest {
        val collectionItem0 = CollectionItem(
            indexValue = 0,
            contentAlias = "contentAlias0",
            createdAt = expectedDate,
            expiresAt = expectedDate,
            uuid = "123"
        )
        val collectionItem1 = CollectionItem(
            indexValue = 1,
            contentAlias = "contentAlias1",
            createdAt = expectedDate,
            expiresAt = expectedDate,
            uuid = "123"
        )
        val collectionItem2 = CollectionItem(
            indexValue = 2,
            contentAlias = "contentAlias2",
            createdAt = expectedDate,
            expiresAt = expectedDate,
            uuid = "123"
        )
        val collectionItem3 = CollectionItem(
            indexValue = 3,
            contentAlias = "contentAlias3",
            createdAt = expectedDate,
            expiresAt = expectedDate,
            uuid = "123"
        )
        val collectionItem4 = CollectionItem(
            indexValue = 4,
            contentAlias = "contentAlias4",
            createdAt = expectedDate,
            expiresAt = expectedDate,
            uuid = "123"
        )
        val collectionItem5 = CollectionItem(
            indexValue = 5,
            contentAlias = "contentAlias5",
            createdAt = expectedDate,
            expiresAt = expectedDate,
            uuid = "123"
        )
        val collectionItem6 = CollectionItem(
            indexValue = 6,
            contentAlias = "contentAlias6",
            createdAt = expectedDate,
            expiresAt = expectedDate,
            uuid = "123"
        )
        testObject.insertCollectionItem(collectionItem0)
        testObject.insertCollectionItem(collectionItem1)
        testObject.insertCollectionItem(collectionItem2)
        testObject.insertCollectionItem(collectionItem3)
        testObject.insertCollectionItem(collectionItem4)
        testObject.insertCollectionItem(collectionItem5)
        testObject.insertCollectionItem(collectionItem6)

        val actual = testObject.getCollections()

        assertEquals(7, actual.size)

        assertTrue(actual.contains(collectionItem0))
        assertTrue(actual.contains(collectionItem1))
        assertTrue(actual.contains(collectionItem2))
        assertTrue(actual.contains(collectionItem3))
        assertTrue(actual.contains(collectionItem4))
        assertTrue(actual.contains(collectionItem5))
        assertTrue(actual.contains(collectionItem6))
    }

    @Test
    fun `getCollectionExpiration returns oldest date items in collection`() = runTest {
        mockkObject(Utils) // un mock date converter
        val expectedDate1 = Date()
        val oldestExpectedDate = Date()
        val oldestDate = Date()
        val expected = 1L
        oldestDate.time = 0L
        oldestExpectedDate.time = expected

        val collectionItem0 = CollectionItem(
            indexValue = 0,
            contentAlias = "contentAlias",
            createdAt = expectedDate1,
            expiresAt = expectedDate1,
            uuid = "123"
        )
        val collectionItem1 = CollectionItem(
            indexValue = 1,
            contentAlias = "contentAlias",
            createdAt = expectedDate1,
            expiresAt = expectedDate1,
            uuid = "123"
        )
        val collectionItem2 = CollectionItem(
            indexValue = 2,
            contentAlias = "contentAlias",
            createdAt = expectedDate1,
            expiresAt = expectedDate1,
            uuid = "123"
        )
        val collectionItem3 = CollectionItem(
            indexValue = 3,
            contentAlias = "contentAlias",
            createdAt = expectedDate1,
            expiresAt = oldestExpectedDate,
            uuid = "123"
        )
        val collectionItem4 = CollectionItem(
            indexValue = 4,
            contentAlias = "contentAlias",
            createdAt = expectedDate1,
            expiresAt = expectedDate1,
            uuid = "123"
        )
        val collectionItem5 = CollectionItem(
            indexValue = 5,
            contentAlias = "contentAlias",
            createdAt = expectedDate1,
            expiresAt = expectedDate1,
            uuid = "123"
        )
        val collectionItem6 = CollectionItem(
            indexValue = 6,
            contentAlias = "contentAlias6",
            createdAt = expectedDate1,
            expiresAt = expectedDate1,
            uuid = "123"
        )
        val expectedJson = getJson("story1.json")
        testObject.insertCollectionItem(collectionItem0)
        testObject.insertCollectionItem(collectionItem1)
        testObject.insertCollectionItem(collectionItem2)
        testObject.insertCollectionItem(collectionItem3)
        testObject.insertCollectionItem(collectionItem4)
        testObject.insertCollectionItem(collectionItem5)
//        testObject.insertCollectionItem(collectionItem6)
        testObject.insertJsonItem(
            JsonItem(
                uuid = "123",
                jsonResponse = expectedJson,
                expiresAt = expectedDate1
            )
        )
        val collections = testObject.getCollections()
        val db = testObject.getCollectionIndexedJson(collectionAlias = "contentAlias", size = 99, from = 0)

        val actual = testObject.getCollectionExpiration("contentAlias")
        collections.size
        db.size
        oldestDate.time
        oldestExpectedDate.time
        assertEquals(expected, actual?.time)
    }

    @Test
    fun `deleteCollectionItemByContentAlias deletes collections with specified content alias only`() =
        runTest {

            val collectionItem0 = CollectionItem(
                indexValue = 0,
                contentAlias = "contentAliasToDelete",
                createdAt = expectedDate,
                expiresAt = expectedDate,
                uuid = "123"
            )
            val collectionItem1 = CollectionItem(
                indexValue = 1,
                contentAlias = "contentAlias1",
                createdAt = expectedDate,
                expiresAt = expectedDate,
                uuid = "123"
            )
            val collectionItem2 = CollectionItem(
                indexValue = 2,
                contentAlias = "contentAlias2",
                createdAt = expectedDate,
                expiresAt = expectedDate,
                uuid = "123"
            )
            val collectionItem3 = CollectionItem(
                indexValue = 3,
                contentAlias = "contentAliasToDelete",
                createdAt = expectedDate,
                expiresAt = expectedDate,
                uuid = "123"
            )
            val collectionItem4 = CollectionItem(
                indexValue = 4,
                contentAlias = "contentAlias4",
                createdAt = expectedDate,
                expiresAt = expectedDate,
                uuid = "123"
            )
            val collectionItem5 = CollectionItem(
                indexValue = 5,
                contentAlias = "contentAliasToDelete",
                createdAt = expectedDate,
                expiresAt = expectedDate,
                uuid = "123"
            )
            val collectionItem6 = CollectionItem(
                indexValue = 6,
                contentAlias = "contentAlias6",
                createdAt = expectedDate,
                expiresAt = expectedDate,
                uuid = "123"
            )
            testObject.insertCollectionItem(collectionItem0)
            testObject.insertCollectionItem(collectionItem1)
            testObject.insertCollectionItem(collectionItem2)
            testObject.insertCollectionItem(collectionItem3)
            testObject.insertCollectionItem(collectionItem4)
            testObject.insertCollectionItem(collectionItem5)
            testObject.insertCollectionItem(collectionItem6)


            testObject.deleteCollectionItemByContentAlias("contentAliasToDelete")
            val actual = testObject.getCollections()

            assertEquals(4, actual.size)

            assertFalse(actual.contains(collectionItem0))
            assertTrue(actual.contains(collectionItem1))
            assertTrue(actual.contains(collectionItem2))
            assertFalse(actual.contains(collectionItem3))
            assertTrue(actual.contains(collectionItem4))
            assertFalse(actual.contains(collectionItem5))
            assertTrue(actual.contains(collectionItem6))
        }

    @Test
    fun `delete Collection Item By ContentAlias and index deletes collection with specified content alias and index only`() =
        runTest {

            val collectionItem0 = CollectionItem(
                indexValue = 0,
                contentAlias = "contentAlias0",
                createdAt = expectedDate,
                expiresAt = expectedDate,
                uuid = "123"
            )
            val collectionItem1 = CollectionItem(
                indexValue = 1,
                contentAlias = "contentAlias1",
                createdAt = expectedDate,
                expiresAt = expectedDate,
                uuid = "123"
            )
            val collectionItem2 = CollectionItem(
                indexValue = 2,
                contentAlias = "contentAlias2",
                createdAt = expectedDate,
                expiresAt = expectedDate,
                uuid = "123"
            )
            val collectionItem3 = CollectionItem(
                indexValue = 3,
                contentAlias = "contentAlias3",
                createdAt = expectedDate,
                expiresAt = expectedDate,
                uuid = "123"
            )
            val collectionItem4 = CollectionItem(
                indexValue = 4,
                contentAlias = "contentAlias4",
                createdAt = expectedDate,
                expiresAt = expectedDate,
                uuid = "123"
            )
            val collectionItem5 = CollectionItem(
                indexValue = 5,
                contentAlias = "contentAliasToDelete",
                createdAt = expectedDate,
                expiresAt = expectedDate,
                uuid = "123"
            )
            val collectionItem6 = CollectionItem(
                indexValue = 6,
                contentAlias = "contentAlias6",
                createdAt = expectedDate,
                expiresAt = expectedDate,
                uuid = "123"
            )
            testObject.insertCollectionItem(collectionItem0)
            testObject.insertCollectionItem(collectionItem1)
            testObject.insertCollectionItem(collectionItem2)
            testObject.insertCollectionItem(collectionItem3)
            testObject.insertCollectionItem(collectionItem4)
            testObject.insertCollectionItem(collectionItem5)
            testObject.insertCollectionItem(collectionItem6)


            testObject.deleteCollectionItemByIndex(contentAlias = "contentAliasToDelete", indexValue = 5)
            val actual = testObject.getCollections()

            assertEquals(6, actual.size)

            assertTrue(actual.contains(collectionItem0))
            assertTrue(actual.contains(collectionItem1))
            assertTrue(actual.contains(collectionItem2))
            assertTrue(actual.contains(collectionItem3))
            assertTrue(actual.contains(collectionItem4))
            assertFalse(actual.contains(collectionItem5))
            assertTrue(actual.contains(collectionItem6))
        }
}
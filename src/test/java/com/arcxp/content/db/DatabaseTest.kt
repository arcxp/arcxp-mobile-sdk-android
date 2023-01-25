package com.arcxp.content.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.arcxp.content.sdk.testUtils.CoroutineTestRule
import io.mockk.MockKAnnotations
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DatabaseTest {

//    @get:Rule
//    var coroutinesTestRule = CoroutineTestRule()

    // so this works, but doesn't count coverage on jacoco
    // the sql queries are compile time checked, so dunno
    // if we really need to test this interface
    // the tests are valid, so
    // i'll leave this in case we need an example mechanism to test db
    // but feel it doesn't have a lot of worth right now
    // this would be useful on a UI test or something maybe?


    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    private lateinit var testObject: ContentSDKDao
    private lateinit var db: Database

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), Database::class.java
        )
            .setTransactionExecutor(testDispatcher.asExecutor())
            .setQueryExecutor(testDispatcher.asExecutor())
            .allowMainThreadQueries()
            .build()
        testObject = db.sdkDao()

    }

    @After
    fun tearDown() {
        db.close()

    }

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @Test
    fun `insert And Get Collection`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val expectedDate = Date()
        val expected = CollectionItem(
            indexValue = 1,
            contentAlias = "contentAlias",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        testObject.insertCollectionItem(expected)

        val actual = testObject.getCollectionById(id = "contentAlias", from = 0, size = 1)

        assertEquals(listOf(expected), actual)
    }

    @Test
    fun `insert And Get Json`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val expectedDate = Date()
        val expected = JsonItem("id", "response", expectedDate, expectedDate)
        testObject.insertJsonItem(expected)

        val actual = testObject.getJsonById("id")

        assertEquals(expected, actual)
    }

    @Test
    fun `item coverage from entity kt`() {
        val item = JsonItem(expiresAt = mockk(), id = "", jsonResponse = "")
        val expiration = (item as BaseItem).expiresAt
    }

    @Test
    fun `get section headers overwrites previous entry and returns expected value`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val expectedDate = Date()
        val expected = SectionHeaderItem(sectionHeaderResponse = "response1", createdAt = expectedDate, expiresAt = expectedDate)
        val expected2 = SectionHeaderItem(sectionHeaderResponse = "response2", createdAt = expectedDate, expiresAt = expectedDate)
        testObject.insertNavigation(sectionHeaderItem = expected)
        testObject.insertNavigation(sectionHeaderItem = expected2)

        val actual = testObject.getSectionList()
        assertEquals(expected2, actual)
    }

    @Test
    fun `delete json item by id performs single deletion`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val expectedDate = Date()

        val jsonItem1 = JsonItem(id = "id1", jsonResponse = "response", expiresAt = expectedDate, createdAt = expectedDate)
        val jsonItem2 = JsonItem(id = "id2", jsonResponse = "response", expiresAt = expectedDate, createdAt = expectedDate)
        val jsonItem3 = JsonItem(id = "id3", jsonResponse = "response", expiresAt = expectedDate, createdAt = expectedDate)
        val jsonItem4 = JsonItem(id = "id4", jsonResponse = "response", expiresAt = expectedDate, createdAt = expectedDate)
        val jsonItem5 = JsonItem(id = "id5", jsonResponse = "response", expiresAt = expectedDate, createdAt = expectedDate)
        val jsonItem6 = JsonItem(id = "id6", jsonResponse = "response", expiresAt = expectedDate, createdAt = expectedDate)
        testObject.insertJsonItem(jsonItem1)
        testObject.insertJsonItem(jsonItem2)
        testObject.insertJsonItem(jsonItem3)
        testObject.insertJsonItem(jsonItem4)
        testObject.insertJsonItem(jsonItem5)
        testObject.insertJsonItem(jsonItem6)


        assertEquals(6, testObject.countJsonItems())

        testObject.deleteJsonItemById(id = "id4")

        assertEquals(5, testObject.countJsonItems())

        assertNull(testObject.getJsonById("id4"))

    }

    @Test
    fun `delete oldest json item deletes only the oldest json item`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val expectedDate = Date()
        val calendar = Calendar.getInstance()
        calendar.time = expectedDate
        calendar.add(Calendar.HOUR, -1)
        val oldestDate = calendar.time
        val jsonItem1 = JsonItem(id = "id1", jsonResponse = "response", expiresAt = expectedDate, createdAt = expectedDate)
        val jsonItem2 = JsonItem(id = "id2", jsonResponse = "response", expiresAt = expectedDate, createdAt = expectedDate)
        val jsonItem3 = JsonItem(id = "id3", jsonResponse = "response", expiresAt = expectedDate, createdAt = expectedDate)
        val jsonItem4 = JsonItem(id = "id4", jsonResponse = "response", expiresAt = expectedDate, createdAt = expectedDate)
        val jsonItem5 = JsonItem(id = "id5", jsonResponse = "response", expiresAt = expectedDate, createdAt = oldestDate)
        val jsonItem6 = JsonItem(id = "id6", jsonResponse = "response", expiresAt = expectedDate, createdAt = expectedDate)
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
    fun `get Collection returns only items within specified range`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val expectedDate = Date()
        val collectionItem0 = CollectionItem(
            indexValue = 0,
            contentAlias = "contentAlias",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem1 = CollectionItem(
            indexValue = 1,
            contentAlias = "contentAlias",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem2 = CollectionItem(
            indexValue = 2,
            contentAlias = "contentAlias",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem3= CollectionItem(
            indexValue = 3,
            contentAlias = "contentAlias",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem4 = CollectionItem(
            indexValue = 4,
            contentAlias = "contentAlias",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem5 = CollectionItem(
            indexValue = 5,
            contentAlias = "contentAlias",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem6 = CollectionItem(
            indexValue = 6,
            contentAlias = "contentAlias",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        testObject.insertCollectionItem(collectionItem0)
        testObject.insertCollectionItem(collectionItem1)
        testObject.insertCollectionItem(collectionItem2)
        testObject.insertCollectionItem(collectionItem3)
        testObject.insertCollectionItem(collectionItem4)
        testObject.insertCollectionItem(collectionItem5)
        testObject.insertCollectionItem(collectionItem6)

        val actual = testObject.getCollectionById(id = "contentAlias", from = 1, size = 5)

        assertEquals(5, actual!!.size)

        assertFalse(actual.contains(collectionItem0))
        assertTrue(actual.contains(collectionItem1))
        assertTrue(actual.contains(collectionItem2))
        assertTrue(actual.contains(collectionItem3))
        assertTrue(actual.contains(collectionItem4))
        assertTrue(actual.contains(collectionItem5))
        assertFalse(actual.contains(collectionItem6))
    }

    @Test
    fun `get Collections returns all collection items`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val expectedDate = Date()
        val collectionItem0 = CollectionItem(
            indexValue = 0,
            contentAlias = "contentAlias0",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem1 = CollectionItem(
            indexValue = 1,
            contentAlias = "contentAlias1",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem2 = CollectionItem(
            indexValue = 2,
            contentAlias = "contentAlias2",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem3= CollectionItem(
            indexValue = 3,
            contentAlias = "contentAlias3",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem4 = CollectionItem(
            indexValue = 4,
            contentAlias = "contentAlias4",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem5 = CollectionItem(
            indexValue = 5,
            contentAlias = "contentAlias5",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem6 = CollectionItem(
            indexValue = 6,
            contentAlias = "contentAlias6",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
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
    fun `deleteCollectionItemByContentAlias deletes collections with specified content alias only`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val expectedDate = Date()
        val collectionItem0 = CollectionItem(
            indexValue = 0,
            contentAlias = "contentAliasToDelete",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem1 = CollectionItem(
            indexValue = 1,
            contentAlias = "contentAlias1",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem2 = CollectionItem(
            indexValue = 2,
            contentAlias = "contentAlias2",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem3= CollectionItem(
            indexValue = 3,
            contentAlias = "contentAliasToDelete",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem4 = CollectionItem(
            indexValue = 4,
            contentAlias = "contentAlias4",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem5 = CollectionItem(
            indexValue = 5,
            contentAlias = "contentAliasToDelete",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem6 = CollectionItem(
            indexValue = 6,
            contentAlias = "contentAlias6",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
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
    fun `delete Collection Item By ContentAlias and index deletes collection with specified content alias and index only`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val expectedDate = Date()
        val collectionItem0 = CollectionItem(
            indexValue = 0,
            contentAlias = "contentAlias0",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem1 = CollectionItem(
            indexValue = 1,
            contentAlias = "contentAlias1",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem2 = CollectionItem(
            indexValue = 2,
            contentAlias = "contentAlias2",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem3= CollectionItem(
            indexValue = 3,
            contentAlias = "contentAlias3",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem4 = CollectionItem(
            indexValue = 4,
            contentAlias = "contentAlias4",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem5 = CollectionItem(
            indexValue = 5,
            contentAlias = "contentAliasToDelete",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        val collectionItem6 = CollectionItem(
            indexValue = 6,
            contentAlias = "contentAlias6",
            collectionResponse = "response",
            createdAt = expectedDate,
            expiresAt = expectedDate
        )
        testObject.insertCollectionItem(collectionItem0)
        testObject.insertCollectionItem(collectionItem1)
        testObject.insertCollectionItem(collectionItem2)
        testObject.insertCollectionItem(collectionItem3)
        testObject.insertCollectionItem(collectionItem4)
        testObject.insertCollectionItem(collectionItem5)
        testObject.insertCollectionItem(collectionItem6)


        testObject.deleteCollectionItemByIndex(id = "contentAliasToDelete", index = 5)
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
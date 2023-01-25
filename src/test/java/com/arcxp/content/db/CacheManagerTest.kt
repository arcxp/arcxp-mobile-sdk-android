package com.arcxp.content.sdk.db

import android.app.Application
import androidx.sqlite.db.SimpleSQLiteQuery
import com.arcxp.content.sdk.ArcXPContentSDK
import com.arcxp.content.sdk.testUtils.CoroutineTestRule
import com.arcxp.content.sdk.util.DependencyFactory
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CacheManagerTest {

    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @MockK
    lateinit var database: Database

    @MockK
    lateinit var application: Application

    @RelaxedMockK
    lateinit var dao: ContentSDKDao

    @MockK
    lateinit var vacQuery: SimpleSQLiteQuery

    @MockK
    lateinit var checkPointQuery: SimpleSQLiteQuery

    private lateinit var testObject: CacheManager
    private val expectedMaxCacheSize = 120//mb .. translates to 125829120 bytes
    private val expectedVideoCollectionName = "video"

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockkObject(DependencyFactory)
        every { DependencyFactory.createIOScope() } returns CoroutineScope(context = Dispatchers.Unconfined + SupervisorJob())
        every { DependencyFactory.vacuumQuery() } returns vacQuery
        every { DependencyFactory.checkPointQuery() } returns checkPointQuery
        mockkObject(ArcXPContentSDK)
        every { ArcXPContentSDK.arcxpContentConfig().cacheSizeMB } returns expectedMaxCacheSize
        every { ArcXPContentSDK.arcxpContentConfig().videoCollectionName } returns expectedVideoCollectionName

        every { database.sdkDao() } returns dao

        testObject = CacheManager(application = application, database = database)
    }

    @After
    fun tearDown() {
        unmockkObject(ArcXPContentSDK)
    }

    @Test
    fun `getCollectionById calls dao`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        testObject.getCollectionById(id = "id103", from = 23, size = 56)
        coVerify(exactly = 1) {
            dao.getCollectionById(id = "id103", from = 23, size = 56)
        }
    }

    @Test
    fun `getCollections calls dao`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        testObject.getCollections()
        coVerify(exactly = 1) {
            dao.getCollections()
        }
    }

    @Test
    fun `getSectionHeaders calls dao`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        testObject.getSectionList()
        coVerify(exactly = 1) {
            dao.getSectionList()
        }
    }

    @Test
    fun `insertSectionHeaders calls dao`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val expected: SectionHeaderItem = mockk()
        testObject.insertNavigation(sectionHeaderItem = expected)
        coVerify(exactly = 1) {
            dao.insertNavigation(sectionHeaderItem = expected)
        }
    }

    @Test
    fun `getJsonById calls dao`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        testObject.getJsonById(id = "id103")
        coVerify(exactly = 1) {
            dao.getJsonById(id = "id103")
        }
    }

    @Test
    fun `insert Json Item when items run out`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {

            with(application) {
                every { getDatabasePath("database").length() } returns 125829120
                every { getDatabasePath("database-shm").length() } returns 1
                every { getDatabasePath("database-wal").length() } returns 2
                every { getDatabasePath("database-journal").length() } returns 3
            }
            val expected: JsonItem = mockk()
            coEvery { dao.countJsonItems() } returnsMany listOf(3, 2, 1, 0)
            testObject.insertJsonItem(expected)
            coVerifySequence {
                dao.insertJsonItem(jsonItem = expected)
                dao.walCheckPoint(supportSQLiteQuery = checkPointQuery)
                dao.countJsonItems()
                dao.deleteOldestJsonItem()
                dao.walCheckPoint(supportSQLiteQuery = checkPointQuery)
                dao.countJsonItems()
                dao.deleteOldestJsonItem()
                dao.walCheckPoint(supportSQLiteQuery = checkPointQuery)
                dao.countJsonItems()
                dao.deleteOldestJsonItem()
                dao.walCheckPoint(supportSQLiteQuery = checkPointQuery)
                dao.countJsonItems()
            }
        }

    @Test
    fun `insert Json Item when space run out`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            with(application) {
                every { getDatabasePath("database").length() } returnsMany listOf(125829120, 999)
                every { getDatabasePath("database-shm").length() } returns 1
                every { getDatabasePath("database-wal").length() } returns 2
                every { getDatabasePath("database-journal").length() } returns 3
            }
            val expected: JsonItem = mockk()
            coEvery { dao.countJsonItems() } returnsMany listOf(3, 2, 1, 0)

            testObject.insertJsonItem(expected)

            coVerifySequence {
                dao.insertJsonItem(jsonItem = expected)
                dao.walCheckPoint(supportSQLiteQuery = checkPointQuery)
                dao.countJsonItems()
                dao.deleteOldestJsonItem()
                dao.walCheckPoint(supportSQLiteQuery = checkPointQuery)
                dao.countJsonItems()
            }
        }

    @Test
    fun `insertCollectionItem calls dao`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        val expected: CollectionItem = mockk()

        testObject.insertCollectionItem(collectionItem = expected)

        coVerify {
            dao.insertCollectionItem(collectionItem = expected)
        }
    }

    @Test
    fun `deleteCollectionItemById calls dao`() = coroutinesTestRule.testDispatcher.runBlockingTest {
        testObject.deleteCollectionItemByContentAlias(id = "eye-dee")

        coVerify(exactly = 1) { dao.deleteCollectionItemByContentAlias(contentAlias = "eye-dee") }
    }

    @Test
    fun `jsonCount returns value from dao`() {
        val expected = 10101
        every { dao.countJsonItems() } returns expected

        val actual = testObject.jsonCount()

        assertEquals(expected, actual)
    }

    @Test
    fun `vac calls dao`() {
        testObject.vac()

        coVerify(exactly = 1) { dao.vacuumDb(supportSQLiteQuery = vacQuery) }
    }

    @Test
    fun `minimize Collections removes stale ids`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            val collectionItem1 = CollectionItem(
                indexValue = 11,
                contentAlias = "111",
                collectionResponse = "response1",
                createdAt = mockk(),
                expiresAt = mockk()
            )
            val collectionItem2 = CollectionItem(
                indexValue = 22,
                contentAlias = "222",
                collectionResponse = "response2",
                createdAt = mockk(),
                expiresAt = mockk()
            )
            val collectionItem3 = CollectionItem(
                indexValue = 33,
                contentAlias = "333",
                collectionResponse = "response3",
                createdAt = mockk(),
                expiresAt = mockk()
            )
            val collectionItem4 = CollectionItem(
                indexValue = 44,
                contentAlias = "444",
                collectionResponse = "response4",
                createdAt = mockk(),
                expiresAt = mockk()
            )
            coEvery { dao.getCollections() } returns listOf(
                collectionItem1,
                collectionItem2,
                collectionItem3,
                collectionItem4
            )

            testObject.minimizeCollections(setOf("1", "2", "3", "444"))

            coVerify(exactly = 1) { dao.deleteCollectionItemByContentAlias(contentAlias = "111") }
            coVerify(exactly = 1) { dao.deleteCollectionItemByContentAlias(contentAlias = "222") }
            coVerify(exactly = 1) { dao.deleteCollectionItemByContentAlias(contentAlias = "333") }
            coVerify(exactly = 0) { dao.deleteCollectionItemByContentAlias(contentAlias = "444") }
        }

    @Test
    fun `minimize Collections removes stale ids but ignores videos`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            val collectionItem1 = CollectionItem(
                indexValue = 11,
                contentAlias = "111",
                collectionResponse = "response1",
                createdAt = mockk(),
                expiresAt = mockk()
            )
            val collectionItem2 = CollectionItem(
                indexValue = 22,
                contentAlias = "222",
                collectionResponse = "response2",
                createdAt = mockk(),
                expiresAt = mockk()
            )
            val collectionItem3 = CollectionItem(
                indexValue = 33,
                contentAlias = "333",
                collectionResponse = "response3",
                createdAt = mockk(),
                expiresAt = mockk()
            )
            val collectionItem4 = CollectionItem(
                indexValue = 44,
                contentAlias = "444",
                collectionResponse = "response4",
                createdAt = mockk(),
                expiresAt = mockk()
            )
            val collectionItem5 = CollectionItem(
                indexValue = 55,
                contentAlias = expectedVideoCollectionName,
                collectionResponse = "response5",
                createdAt = mockk(),
                expiresAt = mockk()
            )
            val collectionItem6 = CollectionItem(
                indexValue = 66,
                contentAlias = expectedVideoCollectionName,
                collectionResponse = "response6",
                createdAt = mockk(),
                expiresAt = mockk()
            )
            coEvery { dao.getCollections() } returns listOf(
                collectionItem1,
                collectionItem2,
                collectionItem3,
                collectionItem4,
                collectionItem5,
                collectionItem6
            )

            testObject.minimizeCollections(setOf("1", "2", "3", "444"))

            coVerify(exactly = 1) { dao.deleteCollectionItemByContentAlias(contentAlias = "111") }
            coVerify(exactly = 1) { dao.deleteCollectionItemByContentAlias(contentAlias = "222") }
            coVerify(exactly = 1) { dao.deleteCollectionItemByContentAlias(contentAlias = "333") }
            coVerify(exactly = 0) { dao.deleteCollectionItemByContentAlias(contentAlias = "444") }
            coVerify(exactly = 0) { dao.deleteCollectionItemByContentAlias(contentAlias = expectedVideoCollectionName) }
        }

    @Test
    fun `minimize Collections when nothing in cache`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            coEvery { dao.getCollections() } returns listOf(null)

            testObject.minimizeCollections(setOf("1", "2", "3"))

            coVerify(exactly = 0) { dao.deleteCollectionItemByContentAlias(contentAlias = any()) }
        }

    @Test
    fun `getInternalId from collection item for coverage`() {
        val collectionItem = CollectionItem(
            indexValue = 0,
            contentAlias = "contentAlias",
            collectionResponse = "",
            expiresAt = mockk()
        )

        assertEquals("contentAlias-0", collectionItem.internalId)
    }

    @Test
    fun `deleteCollectionItemByIndex passes through to dao`() =
        coroutinesTestRule.testDispatcher.runBlockingTest {

            testObject.deleteCollectionItemByIndex(id = "id", index = 213)

            coVerify(exactly = 1) { dao.deleteCollectionItemByIndex(id = "id", index = 213) }
        }
}
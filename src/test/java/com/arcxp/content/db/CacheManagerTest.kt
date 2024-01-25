package com.arcxp.content.db

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.sqlite.db.SimpleSQLiteQuery
import com.arcxp.ArcXPMobileSDK
import com.arcxp.ArcXPMobileSDK.contentConfig
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.commons.util.DependencyFactory.createIOScope
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CacheManagerTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

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
        mockkObject(ArcXPMobileSDK)
        mockkObject(DependencyFactory)
        mockkObject(com.arcxp.commons.util.DependencyFactory)
        every { createIOScope() } returns CoroutineScope(context = Dispatchers.Unconfined + SupervisorJob())
        every { DependencyFactory.vacuumQuery() } returns vacQuery
        every { DependencyFactory.checkPointQuery() } returns checkPointQuery
        every { contentConfig().cacheSizeMB } returns expectedMaxCacheSize
        every { contentConfig().videoCollectionName } returns expectedVideoCollectionName

        every { database.sdkDao() } returns dao

        testObject = CacheManager(application = application, database = database)
    }

    @Test
    fun `getCollectionById calls dao`() = runTest {
        testObject.getCollection(collectionAlias = "id103", from = 23, size = 56)
        coVerify(exactly = 1) {
            dao.getCollectionIndexedJson(collectionAlias = "id103", from = 23, size = 56)
        }
    }

    @Test
    fun `getCollections calls dao`() = runTest {
        testObject.getCollections()
        coVerify(exactly = 1) {
            dao.getCollections()
        }
    }

    @Test
    fun `getSectionHeaders calls dao`() = runTest {
        testObject.getSectionList()
        coVerify(exactly = 1) {
            dao.getSectionList()
        }
    }

    @Test
    fun `insertSectionHeaders calls dao`() = runTest {
        val expected: SectionHeaderItem = mockk()
        testObject.insertNavigation(sectionHeaderItem = expected)
        coVerify(exactly = 1) {
            dao.insertNavigation(sectionHeaderItem = expected)
        }
    }

    @Test
    fun `getJsonById calls dao`() = runTest {
        testObject.getJsonById(uuid = "id103")
        coVerify(exactly = 1) {
            dao.getJsonById(uuid = "id103")
        }
    }

    @Test
    fun `insert Json Item when items run out`() = runTest {

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
    fun `insert Json Item when space run out`() = runTest {
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
    fun `insertCollectionItem calls dao`() = runTest {
        val expected: CollectionItem = mockk()

        testObject.insertCollectionItem(collectionItem = expected)

        coVerify {
            dao.insertCollectionItem(collectionItem = expected)
        }
    }

    @Test
    fun `deleteCollectionItemById calls dao`() = runTest {
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
    fun `minimize Collections removes stale ids`() = runTest {
        val collectionItem1 = CollectionItem(
            indexValue = 11,
            contentAlias = "111",
            createdAt = mockk(),
            expiresAt = mockk(),
            uuid = "123"
        )
        val collectionItem2 = CollectionItem(
            indexValue = 22,
            contentAlias = "222",
            createdAt = mockk(),
            expiresAt = mockk(),
            uuid = "123"
        )
        val collectionItem3 = CollectionItem(
            indexValue = 33,
            contentAlias = "333",
            createdAt = mockk(),
            expiresAt = mockk(),
            uuid = "123"
        )
        val collectionItem4 = CollectionItem(
            indexValue = 44,
            contentAlias = "444",
            createdAt = mockk(),
            expiresAt = mockk(),
            uuid = "123"
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
    fun `minimize Collections removes stale ids but ignores videos`() = runTest {
        val collectionItem1 = CollectionItem(
            indexValue = 11,
            contentAlias = "111",
            createdAt = mockk(),
            expiresAt = mockk(),
            uuid = "123"
        )
        val collectionItem2 = CollectionItem(
            indexValue = 22,
            contentAlias = "222",
            createdAt = mockk(),
            expiresAt = mockk(),
            uuid = "123"
        )
        val collectionItem3 = CollectionItem(
            indexValue = 33,
            contentAlias = "333",
            createdAt = mockk(),
            expiresAt = mockk(),
            uuid = "123"
        )
        val collectionItem4 = CollectionItem(
            indexValue = 44,
            contentAlias = "444",
            createdAt = mockk(),
            expiresAt = mockk(),
            uuid = "123"
        )
        val collectionItem5 = CollectionItem(
            indexValue = 55,
            contentAlias = expectedVideoCollectionName,
            createdAt = mockk(),
            expiresAt = mockk(),
            uuid = "123"
        )
        val collectionItem6 = CollectionItem(
            indexValue = 66,
            contentAlias = expectedVideoCollectionName,
            createdAt = mockk(),
            expiresAt = mockk(),
            uuid = "123"
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
    fun `minimize Collections when nothing in cache`() = runTest {
        coEvery { dao.getCollections() } returns listOf(null)

        testObject.minimizeCollections(setOf("1", "2", "3"))

        coVerify(exactly = 0) { dao.deleteCollectionItemByContentAlias(contentAlias = any()) }
    }

    @Test
    fun `getInternalId from collection item for coverage`() {
        val collectionItem = CollectionItem(
            indexValue = 0,
            contentAlias = "contentAlias",
            expiresAt = mockk(),
            uuid = "123"
        )

        assertEquals("contentAlias-0", collectionItem.internalId)
    }

    @Test
    fun `deleteCollectionItemByIndex passes through to dao`() = runTest {

        testObject.deleteCollectionItemByIndex(contentAlias = "id", indexValue = 213)

        coVerify(exactly = 1) { dao.deleteCollectionItemByIndex(contentAlias = "id", indexValue = 213) }
    }
}
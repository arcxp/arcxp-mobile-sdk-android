package com.arcxp.content.db

import android.app.Application
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.sqlite.db.SimpleSQLiteQuery
import com.arcxp.ArcXPMobileSDK
import com.arcxp.ArcXPMobileSDK.contentConfig
import com.arcxp.commons.testutils.TestUtils.getJson
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.commons.util.DependencyFactory.createIOScope
import com.arcxp.commons.util.MoshiController
import com.arcxp.commons.util.Utils.constructJsonArray
import com.arcxp.content.extendedModels.ArcXPContentElement
import com.arcxp.sdk.R
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

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

    private val siteServiceHierarchy = "siteServiceHierarchy"

    private lateinit var testObject: CacheManager
    private val expectedMaxCacheSize = 120//mb .. translates to 125829120 bytes

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockkObject(ArcXPMobileSDK)
        mockkObject(DependencyFactory)
        every { createIOScope() } returns CoroutineScope(context = Dispatchers.Unconfined + SupervisorJob())
        every { DependencyFactory.vacuumQuery() } returns vacQuery
        every { DependencyFactory.checkPointQuery() } returns checkPointQuery
        every { contentConfig().cacheSizeMB } returns expectedMaxCacheSize

        every { database.sdkDao() } returns dao

        testObject = CacheManager(application = application, database = database)
    }

    @Test
    fun `getCollection calls dao and maps json to content objects`() = runTest {
        val index1 = 23
        val index2 = 24
        val storyJson1 = getJson("story1.json")
        val storyJson2 = getJson("story2.json")
        val story1 = MoshiController.fromJson(
            storyJson1,
            ArcXPContentElement::class.java
        )!!
        val story2 = MoshiController.fromJson(
            storyJson2,
            ArcXPContentElement::class.java
        )!!
        coEvery {
            dao.getCollectionIndexedJson(collectionAlias = "id103", from = 23, size = 56)
        } returns listOf(
            ContentSDKDao.IndexedJsonItem(indexValue = index1, jsonResponse = storyJson1),
            ContentSDKDao.IndexedJsonItem(indexValue = index2, jsonResponse = storyJson2),
        )
        val expected = mapOf(index1 to story1, index2 to story2)

        val actual = testObject.getCollection(collectionAlias = "id103", from = 23, size = 56)

        assertEquals(expected, actual)
    }

    @Test
    fun `getCollection calls dao and maps json to content objects except for failed items`() =
        runTest {
            coEvery {
                application.getString(
                    R.string.get_collection_deserialization_failure_message,
                    any()
                )
            } returns "expected"
            mockkStatic(Log::class)
            every { Log.e(any(), any(), any()) } returns 1
            val index1 = 23
            val index2 = 24
            val storyJson1 = getJson("story1.json")
            val story1 = MoshiController.fromJson(
                storyJson1,
                ArcXPContentElement::class.java
            )!!
            coEvery {
                dao.getCollectionIndexedJson(collectionAlias = "id103", from = 23, size = 56)
            } returns listOf(
                ContentSDKDao.IndexedJsonItem(indexValue = index1, jsonResponse = storyJson1),
                ContentSDKDao.IndexedJsonItem(indexValue = index2, jsonResponse = "invalid Json"),
            )
            val expected = mapOf(index1 to story1)

            val actual = testObject.getCollection(collectionAlias = "id103", from = 23, size = 56)

            assertEquals(expected, actual)
            coVerify(exactly = 1) { Log.e("CacheManager", "expected", any()) }

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
        testObject.getSectionList(siteServiceHierarchy = siteServiceHierarchy)
        coVerify(exactly = 1) {
            dao.getSectionList(siteServiceHierarchy = siteServiceHierarchy)
        }
    }

    @Test
    fun `insertSectionHeaders calls dao`() = runTest {
        val expected: SectionHeaderItem = mockk()
        testObject.insertNavigation(sectionHeaderItem = expected)
        coVerify(exactly = 1) {
            dao.insertSectionList(sectionHeaderItem = expected)
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
        coEvery { dao.countItems() } returnsMany listOf(3, 2, 1, 0)

        testObject.insert(jsonItem = expected)

        coVerifyOrder {
            dao.insertJsonItem(jsonItem = expected)
            dao.walCheckPoint(supportSQLiteQuery = checkPointQuery)
            dao.countItems()
            dao.deleteOldestCollectionItem()
            dao.deleteOldestJsonItem()
            dao.walCheckPoint(supportSQLiteQuery = checkPointQuery)
            dao.countItems()
            dao.deleteOldestCollectionItem()
            dao.deleteOldestJsonItem()
            dao.walCheckPoint(supportSQLiteQuery = checkPointQuery)
            dao.countItems()
            dao.deleteOldestCollectionItem()
            dao.deleteOldestJsonItem()
            dao.walCheckPoint(supportSQLiteQuery = checkPointQuery)
            dao.countItems()
        }
    }

    @Test
    fun `insert Json Item when space runs out`() = runTest {
        with(application) {
            every { getDatabasePath("database").length() } returnsMany listOf(125829120, 999)
            every { getDatabasePath("database-shm").length() } returns 1
            every { getDatabasePath("database-wal").length() } returns 2
            every { getDatabasePath("database-journal").length() } returns 3
        }
        val expected: JsonItem = mockk()
        coEvery { dao.countItems() } returnsMany listOf(3, 2, 1, 0)

        testObject.insert(jsonItem = expected)

        coVerifyOrder {
            dao.insertJsonItem(jsonItem = expected)
            dao.walCheckPoint(supportSQLiteQuery = checkPointQuery)
            dao.countItems()
            dao.deleteOldestCollectionItem()
            dao.deleteOldestJsonItem()
            dao.walCheckPoint(supportSQLiteQuery = checkPointQuery)
            dao.countItems()
        }
    }

    @Test
    fun `insert Item when space runs out`() = runTest {
        with(application) {
            every { getDatabasePath("database").length() } returnsMany listOf(125829120, 999)
            every { getDatabasePath("database-shm").length() } returns 1
            every { getDatabasePath("database-wal").length() } returns 2
            every { getDatabasePath("database-journal").length() } returns 3
        }
        val expected: JsonItem = mockk()
        val collectionItem: CollectionItem = mockk()
        coEvery { dao.countItems() } returnsMany listOf(3, 2, 1, 0)

        testObject.insert(jsonItem = expected, collectionItem = collectionItem)

        coVerifyOrder {
            dao.insertCollectionItem(collectionItem = collectionItem)
            dao.insertJsonItem(jsonItem = expected)
            dao.walCheckPoint(supportSQLiteQuery = checkPointQuery)
            dao.countItems()
            dao.deleteOldestCollectionItem()
            dao.deleteOldestJsonItem()
            dao.walCheckPoint(supportSQLiteQuery = checkPointQuery)
            dao.countItems()
        }
    }

    @Test
    fun `init calls vac`() = runTest {
        coVerifySequence{ dao.vacuumDb(supportSQLiteQuery = vacQuery) }
    }

    @Test
    fun `getCollectionExpiration calls dao`() = runTest {
        val collectionAlias = "collectionAlias"
        val expected: Date = mockk()
        coEvery {
            dao.getCollectionExpiration(collectionAlias)
        } returns expected

        val actual = testObject.getCollectionExpiration(collectionAlias = collectionAlias)

        assertEquals(expected, actual)
    }

    @Test
    fun `getCollectionAsJson calls dao and returns mapped result`() = runTest {
        val collectionAlias = "collectionAlias"
        val story1json = getJson("story1.json")
        val story2json = getJson("story2.json")
        val expectedDbResult = listOf(
            ContentSDKDao.IndexedJsonItem(1, story1json),
            ContentSDKDao.IndexedJsonItem(2, story2json)
        )
        val expected = constructJsonArray(listOf(story1json, story2json))
        coEvery {
            dao.getCollectionIndexedJson(collectionAlias, from = 0, size = 10)
        } returns expectedDbResult

        val actual =
            testObject.getCollectionAsJson(collectionAlias = collectionAlias, from = 0, size = 10)

        assertEquals(expected, actual)
    }

    @Test
    fun `getCollectionAsJson calls dao and returns empty string when db result is empty`() = runTest {
        val collectionAlias = "collectionAlias"
        coEvery {
            dao.getCollectionIndexedJson(collectionAlias, from = 0, size = 10)
        } returns emptyList()

        val actual =
            testObject.getCollectionAsJson(collectionAlias = collectionAlias, from = 0, size = 10)

        assertEquals("", actual)
    }

    @Test
    fun `delete Collection calls dao`() = runTest {
        testObject.deleteCollection(collectionAlias = "collectionAlias")
        coVerify(exactly = 1) { dao.deleteCollection(collectionAlias = "/collectionAlias") }
    }

    @Test
    fun `delete item calls dao`() = runTest {
        testObject.deleteItem(uuid = "uuid")
        coVerify(exactly = 1) {
            dao.deleteJsonItem(uuid = "uuid") }
    }

    @Test
    fun `purgeAll calls dao`() = runTest {
        testObject.deleteAll()
        coVerifyOrder {
            dao.deleteJsonTable()
            dao.deleteCollectionTable()
            dao.deleteSectionHeaderTable()
        }
    }
}
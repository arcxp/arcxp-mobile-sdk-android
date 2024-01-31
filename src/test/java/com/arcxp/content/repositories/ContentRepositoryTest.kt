package com.arcxp.content.repositories

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.arcxp.ArcXPMobileSDK
import com.arcxp.ArcXPMobileSDK.contentConfig
import com.arcxp.commons.testutils.TestUtils.getJson
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.Constants.DEFAULT_PAGINATION_SIZE
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.commons.util.DependencyFactory.createIOScope
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.MoshiController.fromJson
import com.arcxp.commons.util.Success
import com.arcxp.commons.util.Utils
import com.arcxp.content.apimanagers.ContentApiManager
import com.arcxp.content.db.CacheManager
import com.arcxp.content.db.CollectionItem
import com.arcxp.content.db.JsonItem
import com.arcxp.content.db.SectionHeaderItem
import com.arcxp.content.extendedModels.ArcXPContentElement
import com.arcxp.content.extendedModels.ArcXPStory
import com.arcxp.content.models.*
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*


class ContentRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @RelaxedMockK
    private lateinit var contentApiManager: ContentApiManager

    @RelaxedMockK
    private lateinit var cacheManager: CacheManager

    @RelaxedMockK
    private lateinit var arcxpContentCallback: ArcXPContentCallback

    private val id = "id"
    private val expectedJson = "expectedJson"
    private val keywords = "keywords"
    private val expectedError =
        ArcXPException(type = ArcXPSDKErrorType.SERVER_ERROR, message = "our error")
    private val expectedFailure = Failure(failure = expectedError)

    private lateinit var testObject: ContentRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockkObject(ArcXPMobileSDK)
        coEvery { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        coEvery { contentConfig().preLoading } returns true
        mockkObject(DependencyFactory)
        coEvery { DependencyFactory.createContentApiManager() } returns contentApiManager
        coEvery { createIOScope() } returns CoroutineScope(context = Dispatchers.Unconfined + SupervisorJob())

        testObject = ContentRepository(cacheManager = cacheManager)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

//    @Test
//    fun `getSectionListSuspend call removes any non-current ids from collection table`() = runTest {
//
//        //these will 'exist' in db, but not be in current response so they will be purged
//        val collectionItem1 = CollectionItem(
//            indexValue = 1,
//            collectionAlias = "111",
//            createdAt = mockk(),
//            expiresAt = mockk(),
//            uuid = "123"
//        )
//        val collectionItem2 = CollectionItem(
//            indexValue = 2,
//            collectionAlias = "222",
//            createdAt = mockk(),
//            expiresAt = mockk(),
//            uuid = "123"
//        )
//        val collectionItem3 = CollectionItem(
//            indexValue = 3,
//            collectionAlias = "333",
//            createdAt = mockk(),
//            expiresAt = mockk(),
//            uuid = "123"
//        )
//        coEvery { cacheManager.getCollections() } returns listOf(
//            collectionItem1,
//            collectionItem2,
//            collectionItem3
//        )
//        coEvery { cacheManager.getSectionList() } returns null
//        coEvery { contentApiManager.getSectionList() } returns Success(
//            Pair(
//                sectionListJson,
//                mockk()
//            )
//        )
//
//        testObject.getSectionList(shouldIgnoreCache = false)
//
//        coVerify(exactly = 1) {
//            cacheManager.minimizeCollections(
//                newCollectionAliases = setOf(
//                    "mobile-politics",
//                    "mobile-entertainment",
//                    "mobile-sports",
//                    "mobile-tech",
//                    "mobile-topstories"
//                )
//            )
//        }
//    } ///TODO do we need this?

    @Test
    fun `doCollectionApiCallSuspend inserts json results from response into db `() = runTest {
        val collectionJson = getJson("collectionFull.json")
        val contentElementList = fromJson(
            collectionJson,
            Array<ArcXPContentElement>::class.java
        )!!.toList()
        val map = mapOf(0 to contentElementList[0], 1 to contentElementList[1], 2 to contentElementList[2])
        val expected = Success(map)
        coEvery {
            cacheManager.getCollection(
                collectionAlias = any(),
                from = any(),
                size = any()
            )
        } returns emptyMap()
        coEvery { cacheManager.getJsonById(uuid = any()) } returns null
        coEvery { cacheManager.getCollectionExpiration(id) } returns Date().apply { time = 0 }
        coEvery {
            contentApiManager.getCollection(
                collectionAlias = id,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0,
                full = true
            )
        } returns Success(Pair(collectionJson, mockk()))

        val actual = testObject.getCollection(
            collectionAlias = id,
            shouldIgnoreCache = false,
            size = DEFAULT_PAGINATION_SIZE,
            from = 0
        )
        assertEquals(expected, actual)

        val jsonItemSlot = mutableListOf<JsonItem>()
        val collectionItemSlot = mutableListOf<CollectionItem>()
        coVerify(exactly = 3) { cacheManager.insert(capture(collectionItemSlot), capture(jsonItemSlot)) }
        val actual0 = fromJson(jsonItemSlot[0].jsonResponse, ArcXPContentElement::class.java)!!
        val actual1 = fromJson(jsonItemSlot[1].jsonResponse, ArcXPContentElement::class.java)!!
        val actual2 = fromJson(jsonItemSlot[2].jsonResponse, ArcXPContentElement::class.java)!!

        assertEquals("SBMBP2IX35CVLCNR6BQGSXGQVA", jsonItemSlot[0].uuid)
        assertEquals(contentElementList[0], actual0)

        assertEquals("RICKZKE4U5AF5GX7OLA6MWGOFY", jsonItemSlot[1].uuid)
        assertEquals(contentElementList[1], actual1)

        assertEquals("SIWW3GLZERCIFC7F7RERLBNILQ", jsonItemSlot[2].uuid)
        assertEquals(contentElementList[2], actual2)
    }

    @Test
    fun `doCollectionApiCallSuspend does not make story call when preLoading is false`() = runTest {
        coEvery { contentConfig().preLoading } returns false
        coEvery {
            cacheManager.getCollection(
                collectionAlias =  any(),
                from = any(),
                size = any()
            )
        } returns emptyMap()
        coEvery { cacheManager.getJsonById(uuid = any()) } returns null
        coEvery {
            contentApiManager.getCollection(
                collectionAlias =  id,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } returns Success(Pair(collectionListJson, Date()))
        testObject.getCollection(
            collectionAlias =  id,
            shouldIgnoreCache = false,
            size = DEFAULT_PAGINATION_SIZE,
            from = 0
        )
        coVerify { contentApiManager.getContent(any()) wasNot called }
    }

    @Test
    fun `preLoadDb loads result into db`() {
        val expectedExpiration = mockk<Date>()
        coEvery { cacheManager.getJsonById(uuid = id) } returns null
        coEvery { contentApiManager.getContent(id = id) } returns Success(
            Pair(
                storyJson,
                expectedExpiration
            )
        )

        testObject.preLoadDb(id = id, listener = arcxpContentCallback)

        coVerify(exactly = 0) {
            arcxpContentCallback.onGetContentSuccess(response = any())
        }

        val dbInsertionSlot = slot<JsonItem>()
        coVerify(exactly = 1) { cacheManager.insert(jsonItem = capture(dbInsertionSlot)) }

        assertEquals(
            storyJson,
            dbInsertionSlot.captured.jsonResponse
        )
        assertEquals(
            expectedExpiration,
            dbInsertionSlot.captured.expiresAt
        )
        assertEquals(id, dbInsertionSlot.captured.uuid)
    }

    @Test
    fun `preLoadDb on error notifies listener`() = runTest {
        val expectedError = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = "Get Story Deserialization Error"
        )
        val expected = Failure(expectedError)
        coEvery { cacheManager.getJsonById(uuid = id) } returns null
        coEvery { contentApiManager.getContent(id = id) } returns expected

        testObject.preLoadDb(id = id, listener = arcxpContentCallback)

        coVerify(exactly = 0) {
            arcxpContentCallback.onGetContentSuccess(response = any())
        }
        coVerify(exactly = 1) { arcxpContentCallback.onError(error = expectedError) }
    }

    @Test
    fun `preLoadDb on error with null listener`() = runTest {
        val expectedError = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = "Get Story Deserialization Error"
        )
        val expected = Failure(expectedError)
        coEvery { cacheManager.getJsonById(uuid = id) } returns null
        coEvery { contentApiManager.getContent(id = id) } returns expected

        testObject.preLoadDb(id = id)

        coVerify(exactly = 0) {
            arcxpContentCallback.onGetContentSuccess(response = any())
            arcxpContentCallback.onError(error = any())
        }
    }

    @Test
    fun `getSectionListSuspend returns db result (shouldIgnore False, stale False)`() = runTest {
        val timeUntilUpdateMinutes = 5
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns timeUntilUpdateMinutes

        val expirationDate = Calendar.getInstance()
        expirationDate.set(3022, Calendar.FEBRUARY, 8, 12, 0, 0)
        val expectedList = fromJson(
            sectionListJson,
            Array<ArcXPSection>::class.java
        )!!.toList()
        val expected = Success(success = expectedList)
        coEvery { cacheManager.getSectionList() } returns SectionHeaderItem(
            sectionHeaderResponse = sectionListJson,
            expiresAt = expirationDate.time
        )

        val actual = testObject.getSectionList(shouldIgnoreCache = false)

        assertEquals(expected, actual)
    }

    @Test
    fun `getSectionListSuspend returns api result (shouldIgnore true)`() = runTest {
        val expected = Success(
            success = fromJson(
                sectionListJson,
                Array<ArcXPSection>::class.java
            )!!.toList()
        )
        coEvery { contentApiManager.getSectionList() } returns Success(
            Pair(
                sectionListJson,
                Date()
            )
        )

        val actual = testObject.getSectionList(shouldIgnoreCache = true)

        assertEquals(expected, actual)
        coVerify { cacheManager wasNot called }
    }

    @Test
    fun `getSectionListSuspend returns api result and inserts into db (shouldIgnore false, stale true(not in db))`() =
        runTest {
            val expectedList = fromJson(
                sectionListJson,
                Array<ArcXPSection>::class.java
            )!!.toList()
            val expected = Success(
                success = expectedList
            )

            coEvery { cacheManager.getSectionList() } returns null
            coEvery { contentApiManager.getSectionList() } returns Success(
                Pair(
                    sectionListJson,
                    Date()
                )
            )

            val actual = testObject.getSectionList(shouldIgnoreCache = false)

            assertEquals(expected, actual)
            val dbInsertionSlot = slot<SectionHeaderItem>()
            coVerify(exactly = 1) {
                cacheManager.insertNavigation(
                    sectionHeaderItem = capture(
                        dbInsertionSlot
                    )
                )
            }
            assertEquals(
                expectedList,
                fromJson(
                    dbInsertionSlot.captured.sectionHeaderResponse,
                    Array<ArcXPSection>::class.java
                )!!.toList()
            )
            assertEquals(1, dbInsertionSlot.captured.id) //we overwrite 1 for each section list
        }

    @Test
    fun `getSectionListSuspend returns api result (shouldIgnore false, stale true(in db))`() =
        runTest {
            val timeUntilUpdateHours = 5
            every { contentConfig().cacheTimeUntilUpdateMinutes } returns timeUntilUpdateHours

            val cacheDate = Calendar.getInstance()
            cacheDate.set(2022, Calendar.FEBRUARY, 8, 11, 0, 0)
            val mockCurrentDate = Calendar.getInstance()
            mockCurrentDate.set(2022, Calendar.FEBRUARY, 8, 17, 0, 0)
            // mock date should be stale
            mockkObject(Utils)
            every { Utils.currentTime() } returns mockCurrentDate.time

            val expectedList = fromJson(
                sectionListJson2,
                Array<ArcXPSection>::class.java
            )!!.toList()
            val expected = Success(success = expectedList)
            coEvery { cacheManager.getSectionList() } returns SectionHeaderItem(
                sectionHeaderResponse = sectionListJson,
                createdAt = cacheDate.time,
                expiresAt = mockk()
            )
            coEvery { contentApiManager.getSectionList() } returns Success(
                Pair(
                    sectionListJson2,
                    Date()
                )
            )

            val actual = testObject.getSectionList(
                shouldIgnoreCache = false
            )

            assertEquals(expected, actual)
            val dbInsertionSlot = slot<SectionHeaderItem>()
            coVerify(exactly = 1) { cacheManager.insertNavigation(capture(dbInsertionSlot)) }
            assertEquals(
                expectedList,
                fromJson(
                    dbInsertionSlot.captured.sectionHeaderResponse,
                    Array<ArcXPSection>::class.java
                )!!.toList()
            )
            assertEquals(1, dbInsertionSlot.captured.id) //we overwrite 1 for each section list
        }

    @Test
    fun `getSectionListSuspend returns stale db result when api call fails (shouldIgnore false, stale true(in db), api fail)`() =
        runTest {
            val timeUntilUpdateHours = 5
            every { contentConfig().cacheTimeUntilUpdateMinutes } returns timeUntilUpdateHours

            val cacheDate = Calendar.getInstance()
            cacheDate.set(2022, Calendar.FEBRUARY, 8, 11, 0, 0)
            val mockCurrentDate = Calendar.getInstance()
            mockCurrentDate.set(2022, Calendar.FEBRUARY, 8, 17, 0, 0)
            // mock date should be stale
            mockkObject(Utils)
            every { Utils.currentTime() } returns mockCurrentDate.time

            val expectedList = fromJson(
                sectionListJson,
                Array<ArcXPSection>::class.java
            )!!.toList()
            val expected = Success(
                success = expectedList
            )
            coEvery { cacheManager.getSectionList() } returns SectionHeaderItem(
                sectionHeaderResponse = sectionListJson,
                createdAt = cacheDate.time,
                expiresAt = mockk()
            )
            coEvery { contentApiManager.getSectionList() } returns Failure(
                ArcXPException(
                    type = ArcXPSDKErrorType.SERVER_ERROR,
                    message = "our error message"
                )
            )


            val actual = testObject.getSectionList(
                shouldIgnoreCache = false
            )

            assertEquals(expected, actual)
        }

    @Test
    fun `getSectionListSuspend failure from api`() = runTest {
        val expectedError = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = "Failed to load navigation"
        )
        val expected = Failure(expectedError)
        coEvery {
            cacheManager.getSectionList()
        } returns null
        coEvery {
            contentApiManager.getSectionList()
        } returns expected

        val actual = testObject.getSectionList(shouldIgnoreCache = false)

        assertEquals(expected, actual)
    }

    @Test
    fun `getSectionListSuspend deserialization error from api`() = runTest {
        val json = "not Valid Json List"
        val expectedResponse = Success(Pair(json, Date()))
        val expectedError = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = "Navigation Deserialization Error"
        )
        val expected = Failure(expectedError)
        coEvery {
            cacheManager.getSectionList()
        } returns null
        coEvery {
            contentApiManager.getSectionList()
        } returns expectedResponse

        val actual = testObject.getSectionList(shouldIgnoreCache = false)

        assertEquals(expected, actual)
    }

    @Test
    fun `getContentSuspend returns db result (shouldIgnore False, stale False)`() = runTest {
        val timeUntilUpdateMinutes = 5
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns timeUntilUpdateMinutes

        val expirationDate = Calendar.getInstance()
        expirationDate.set(3022, Calendar.FEBRUARY, 8, 12, 0, 0)
        val expectedJson = fromJson(storyJson, ArcXPContentElement::class.java)!!
        val expected = Success(success = expectedJson)
        coEvery { cacheManager.getJsonById(uuid = id) } returns JsonItem(
            uuid = id,
            jsonResponse = storyJson,
            expiresAt = expirationDate.time
        )

        val actual = testObject.getContent(uuid = id, shouldIgnoreCache = false)

        assertEquals(expected, actual)
    }

    @Test
    fun `getContentSuspend returns api result (shouldIgnore true)`() = runTest {
        val expectedContent = fromJson(storyJson, ArcXPContentElement::class.java)!!
        val expected = Success(success = expectedContent)
        coEvery {
            contentApiManager.getContent(id = id)
        } returns Success(success = Pair(storyJson, Date()))

        val actual = testObject.getContent(
            uuid = id,
            shouldIgnoreCache = true
        )

        assertEquals(expected, actual)
        coVerify { cacheManager wasNot called }
    }

    @Test
    fun `getContentSuspend returns api result (shouldIgnore false, stale true(not in db))`() =
        runTest {
            val expected = fromJson(storyJson, ArcXPContentElement::class.java)!!
            coEvery { cacheManager.getJsonById(uuid = id) } returns null
            coEvery { contentApiManager.getContent(id = id) } returns Success(
                Pair(
                    storyJson,
                    Date()
                )
            )

            testObject.getContent(uuid = id, shouldIgnoreCache = false)

            coVerify(exactly = 0) {
                arcxpContentCallback.onGetContentSuccess(response = any())
            }

            val dbInsertionSlot = slot<JsonItem>()
            coVerify(exactly = 1) { cacheManager.insert(jsonItem = capture(dbInsertionSlot)) }

            assertEquals(
                expected,
                fromJson(dbInsertionSlot.captured.jsonResponse, ArcXPContentElement::class.java)
            )
            assertEquals(id, dbInsertionSlot.captured.uuid)
        }

    @Test
    fun `getContentSuspend returns api result (shouldIgnore false, stale true(in db))`() = runTest {
        val timeUntilUpdateHours = 5
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns timeUntilUpdateHours

        val cacheDate = Calendar.getInstance()
        cacheDate.set(2022, Calendar.FEBRUARY, 8, 11, 0, 0)

        val mockCurrentDate = Calendar.getInstance()
        mockCurrentDate.set(2022, Calendar.FEBRUARY, 8, 17, 0, 0)
        // mock date should be stale
        mockkObject(Utils)
        every { Utils.currentTime() } returns mockCurrentDate.time

        val expectedContent = fromJson(storyJson, ArcXPContentElement::class.java)!!
        val expected = Success(success = expectedContent)
        coEvery { cacheManager.getJsonById(uuid = id) } returns JsonItem(
            uuid = id,
            jsonResponse = storyJson,
            createdAt = mockk(),
            expiresAt = cacheDate.time
        )
        coEvery { contentApiManager.getContent(id = id) } returns Success(
            Pair(
                storyJson,
                Date()
            )
        )


        val actual = testObject.getContent(
            uuid = id,
            shouldIgnoreCache = false
        )
        assertEquals(expected, actual)
        val dbInsertionSlot = slot<JsonItem>()
        coVerify(exactly = 1) { cacheManager.insert(jsonItem = capture(dbInsertionSlot)) }
        assertEquals(
            expectedContent,
            fromJson(dbInsertionSlot.captured.jsonResponse, ArcXPContentElement::class.java)
        )
        assertEquals(id, dbInsertionSlot.captured.uuid)
    }

    @Test
    fun `getContentSuspend returns stale db entry if api call fails (shouldIgnore false, stale true(in db), api fail)`() =
        runTest {
            val timeUntilUpdateHours = 5
            every { contentConfig().cacheTimeUntilUpdateMinutes } returns timeUntilUpdateHours

            val cacheDate = Calendar.getInstance()
            cacheDate.set(2022, Calendar.FEBRUARY, 8, 11, 0, 0)

            val mockCurrentDate = Calendar.getInstance()
            mockCurrentDate.set(2022, Calendar.FEBRUARY, 8, 17, 0, 0)
            // mock date should be stale
            mockkObject(Utils)
            every { Utils.currentTime() } returns mockCurrentDate.time

            val expectedContent = fromJson(storyJson, ArcXPContentElement::class.java)!!
            val expected = Success(success = expectedContent)
            coEvery { cacheManager.getJsonById(uuid = id) } returns JsonItem(
                uuid = id,
                jsonResponse = storyJson,
                createdAt = mockk(),
                expiresAt = cacheDate.time
            )
            coEvery { contentApiManager.getContent(id = id) } returns
                    Failure(
                        ArcXPException(
                            type = ArcXPSDKErrorType.SERVER_ERROR,
                            message = "api error"
                        )
                    )


            val actual = testObject.getContent(
                uuid = id,
                shouldIgnoreCache = false
            )
            assertEquals(expected, actual)
        }

    @Test
    fun `getContentSuspend failure from api`() = runTest {
        val expectedError = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = "I AM ERROR"
        )
        val expected = Failure(expectedError)
        coEvery {
            cacheManager.getJsonById(uuid = id)
        } returns null
        coEvery {
            contentApiManager.getContent(
                id = id
            )
        } returns expected

        val actual = testObject.getContent(
            uuid = id,
            shouldIgnoreCache = false
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `getContentSuspend deserialization error from api`() = runTest {
        val json = "not Valid Json"
        val expectedResponse = Success(Pair(json, Date()))
        val expectedError = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = "Get Content Deserialization Error"
        )
        val expected = Failure(expectedError)
        coEvery {
            cacheManager.getJsonById(uuid = id)
        } returns null
        coEvery {
            contentApiManager.getContent(
                id = id
            )
        } returns expectedResponse

        val actual = testObject.getContent(
            uuid = id,
            shouldIgnoreCache = false
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `getStory returns db result (shouldIgnore False, stale False)`() = runTest {
        val timeUntilUpdateMinutes = 5
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns timeUntilUpdateMinutes

        val expirationDate = Calendar.getInstance()
        expirationDate.set(3022, Calendar.FEBRUARY, 8, 12, 0, 0)
        val expectedJson = fromJson(storyJson, ArcXPStory::class.java)!!
        val expected = Success(success = expectedJson)
        coEvery { cacheManager.getJsonById(uuid = id) } returns JsonItem(
            uuid = id,
            jsonResponse = storyJson,
            expiresAt = expirationDate.time
        )

        val actual = testObject.getStory(uuid = id, shouldIgnoreCache = false)

        assertEquals(expected, actual)
    }

    @Test
    fun `getStory detects no content elements in db result and calls api`() = runTest {
        val timeUntilUpdateMinutes = 5
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns timeUntilUpdateMinutes

        val expirationDate = Calendar.getInstance()
        expirationDate.set(3022, Calendar.FEBRUARY, 8, 12, 0, 0)
        val expectedCachedJson = getJson("storyNoContentElements.json")
        val expectedUpdatedApiResponse = Success(success = Pair(storyJson1, expirationDate.time))
        val story = fromJson(storyJson1, ArcXPStory::class.java)!!
        val expected = Success(success = story)
        coEvery { cacheManager.getJsonById(uuid = id) } returns JsonItem(
            uuid = id,
            jsonResponse = expectedCachedJson,
            expiresAt = expirationDate.time
        )
        coEvery {
            contentApiManager.getContent(id = id)
        } returns expectedUpdatedApiResponse

        val actual = testObject.getStory(uuid = id, shouldIgnoreCache = false)

        assertEquals(expected, actual)
    }

    @Test
    fun `getStory detects empty content elements in db result and calls api`() = runTest {
        val timeUntilUpdateMinutes = 5
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns timeUntilUpdateMinutes

        val expirationDate = Calendar.getInstance()
        expirationDate.set(3022, Calendar.FEBRUARY, 8, 12, 0, 0)
        val expectedCachedJson = getJson("storyEmptyContentElements.json")
        val expectedUpdatedApiResponse = Success(success = Pair(storyJson1, expirationDate.time))
        val story = fromJson(storyJson1, ArcXPStory::class.java)!!
        val expected = Success(success = story)
        coEvery { cacheManager.getJsonById(uuid = id) } returns JsonItem(
            uuid = id,
            jsonResponse = expectedCachedJson,
            expiresAt = expirationDate.time
        )
        coEvery {
            contentApiManager.getContent(id = id)
        } returns expectedUpdatedApiResponse

        val actual = testObject.getStory(uuid = id, shouldIgnoreCache = false)

        assertEquals(expected, actual)
    }

    @Test
    fun `getStory returns api result (shouldIgnore true)`() = runTest {
        val expectedContent = fromJson(storyJson, ArcXPStory::class.java)!!
        val expected = Success(success = expectedContent)
        coEvery {
            contentApiManager.getContent(id = id)
        } returns Success(success = Pair(storyJson, Date()))

        val actual = testObject.getStory(
            uuid = id,
            shouldIgnoreCache = true
        )

        assertEquals(expected, actual)
        coVerify { cacheManager wasNot called }
    }

    @Test
    fun `getStory returns api result (shouldIgnore false, stale true(not in db))`() = runTest {
        val expected = fromJson(storyJson, ArcXPStory::class.java)!!
        coEvery { cacheManager.getJsonById(uuid = id) } returns null
        coEvery { contentApiManager.getContent(id = id) } returns Success(
            Pair(
                storyJson,
                Date()
            )
        )

        testObject.getStory(uuid = id, shouldIgnoreCache = false)

        coVerify(exactly = 0) {
            arcxpContentCallback.onGetContentSuccess(response = any())
        }

        val dbInsertionSlot = slot<JsonItem>()
        coVerify(exactly = 1) { cacheManager.insert(jsonItem = capture(dbInsertionSlot)) }

        assertEquals(
            expected,
            fromJson(dbInsertionSlot.captured.jsonResponse, ArcXPStory::class.java)
        )
        assertEquals(id, dbInsertionSlot.captured.uuid)
    }

    @Test
    fun `getStory returns api result (shouldIgnore false, stale true(in db))`() = runTest {
        val timeUntilUpdateHours = 5
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns timeUntilUpdateHours

        val cacheDate = Calendar.getInstance()
        cacheDate.set(2022, Calendar.FEBRUARY, 8, 11, 0, 0)

        val mockCurrentDate = Calendar.getInstance()
        mockCurrentDate.set(2022, Calendar.FEBRUARY, 8, 17, 0, 0)
        // mock date should be stale
        mockkObject(Utils)
        every { Utils.currentTime() } returns mockCurrentDate.time

        val expectedContent = fromJson(storyJson, ArcXPStory::class.java)!!
        val expected = Success(success = expectedContent)
        coEvery { cacheManager.getJsonById(uuid = id) } returns JsonItem(
            uuid = id,
            jsonResponse = storyJson,
            createdAt = mockk(),
            expiresAt = cacheDate.time
        )
        coEvery { contentApiManager.getContent(id = id) } returns Success(
            Pair(
                storyJson,
                Date()
            )
        )


        val actual = testObject.getStory(
            uuid = id,
            shouldIgnoreCache = false
        )
        assertEquals(expected, actual)
        val dbInsertionSlot = slot<JsonItem>()
        coVerify(exactly = 1) { cacheManager.insert(jsonItem = capture(dbInsertionSlot)) }
        assertEquals(
            expectedContent,
            fromJson(dbInsertionSlot.captured.jsonResponse, ArcXPStory::class.java)
        )
        assertEquals(id, dbInsertionSlot.captured.uuid)
    }

    @Test
    fun `getStory returns stale db entry if api call fails (shouldIgnore false, stale true(in db), api fail)`() =
        runTest {
            val timeUntilUpdateHours = 5
            every { contentConfig().cacheTimeUntilUpdateMinutes } returns timeUntilUpdateHours

            val cacheDate = Calendar.getInstance()
            cacheDate.set(2022, Calendar.FEBRUARY, 8, 11, 0, 0)

            val mockCurrentDate = Calendar.getInstance()
            mockCurrentDate.set(2022, Calendar.FEBRUARY, 8, 17, 0, 0)
            // mock date should be stale
            mockkObject(Utils)
            every { Utils.currentTime() } returns mockCurrentDate.time

            val expectedContent = fromJson(storyJson, ArcXPStory::class.java)!!
            val expected = Success(success = expectedContent)
            coEvery { cacheManager.getJsonById(uuid = id) } returns JsonItem(
                uuid = id,
                jsonResponse = storyJson,
                createdAt = mockk(),
                expiresAt = cacheDate.time
            )
            coEvery { contentApiManager.getContent(id = id) } returns
                    Failure(
                        ArcXPException(
                            type = ArcXPSDKErrorType.SERVER_ERROR,
                            message = "api error"
                        )
                    )


            val actual = testObject.getStory(
                uuid = id,
                shouldIgnoreCache = false
            )
            assertEquals(expected, actual)
        }

    @Test
    fun `getStory failure from api`() = runTest {
        val expectedError = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = "I AM ERROR"
        )
        val expected = Failure(expectedError)
        coEvery {
            cacheManager.getJsonById(uuid = id)
        } returns null
        coEvery {
            contentApiManager.getContent(
                id = id
            )
        } returns expected

        val actual = testObject.getStory(
            uuid = id,
            shouldIgnoreCache = false
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `getStory deserialization error from api`() = runTest {
        val json = "not Valid Json"
        val expectedResponse = Success(Pair(json, Date()))
        val expectedError = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = "Get Story Deserialization Error"
        )
        val expected = Failure(expectedError)
        coEvery {
            cacheManager.getJsonById(uuid = id)
        } returns null
        coEvery {
            contentApiManager.getContent(
                id = id
            )
        } returns expectedResponse

        val actual = testObject.getStory(
            uuid = id,
            shouldIgnoreCache = false
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `getCollection returns db result (shouldIgnore False, stale False)`() = runTest {
        val timeUntilUpdateMinutes = 5
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns timeUntilUpdateMinutes

        val expirationDate = Calendar.getInstance()
        expirationDate.set(3022, Calendar.FEBRUARY, 8, 12, 0, 0)
        val item = fromJson(
            collectionJson0,
            ArcXPContentElement::class.java
        )!!
        val expectedMap = HashMap<Int, ArcXPContentElement>()
        expectedMap[0] = item
        val expected = Success(success = expectedMap)
        coEvery { cacheManager.getCollectionExpiration(id) } returns expirationDate.time
        coEvery {
            cacheManager.getCollection(
                collectionAlias = id,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } returns mapOf(
            0 to fromJson(collectionJson0, ArcXPContentElement::class.java)!!
        )

        val actual = testObject.getCollection(
            collectionAlias = id,
            shouldIgnoreCache = false,
            size = DEFAULT_PAGINATION_SIZE,
            from = 0
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `getCollection returns api result (shouldIgnore true)`() = runTest {
        val list = fromJson(
            collectionListJson,
            Array<ArcXPContentElement>::class.java
        )!!.toList()
        val map = HashMap<Int, ArcXPContentElement>()
        list.forEachIndexed { index, arcXPContentElement -> map[index] = arcXPContentElement }  //TODO what are we using list for
        val expected = Success(map)
        coEvery {
            contentApiManager.getCollection(
                collectionAlias = id,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0,
                full = true
            )
        } returns Success(Pair(collectionListJson, Date()))

        val actual = testObject.getCollection(
            collectionAlias = id,
            shouldIgnoreCache = true,
            size = DEFAULT_PAGINATION_SIZE,
            from = 0
        )


        assertEquals(expected, actual)
        coVerify { cacheManager wasNot called }
    }

    @Test
    fun `getCollection with empty result from db, inserts into db`() = runTest {
        val collectionJson = getJson("collectionFull.json")
        val collectionList = fromJson(
            collectionJson,
            Array<ArcXPContentElement>::class.java
        )!!.toList()
        val map = mapOf(0 to collectionList[0], 1 to collectionList[1], 2 to collectionList[2])
        val expected = Success(map)
        coEvery {
            cacheManager.getCollection(
                collectionAlias = id,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns emptyMap()
        coEvery {
            contentApiManager.getCollection(
                collectionAlias = id,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0,
                full = true
            )
        } returns Success(Pair(collectionJson, Date()))

        val actual = testObject.getCollection(
            collectionAlias =  id,
            shouldIgnoreCache = false,
            size = DEFAULT_PAGINATION_SIZE,
            from = 0
        )

        assertEquals(expected, actual)
        val collectionInsertionSlot = mutableListOf<CollectionItem>()
        val jsonInsertionSlot = mutableListOf<JsonItem>()
        coVerify(exactly = 3) { cacheManager.insert(capture(collectionInsertionSlot), capture(jsonInsertionSlot)) }

        assertEquals(
            collectionList[0],
            fromJson(jsonInsertionSlot[0].jsonResponse, ArcXPContentElement::class.java)
        )
        assertEquals(
            collectionList[1],
            fromJson(jsonInsertionSlot[1].jsonResponse, ArcXPContentElement::class.java)
        )
        assertEquals(
            collectionList[2],
            fromJson(jsonInsertionSlot[2].jsonResponse, ArcXPContentElement::class.java)
        )
        assertEquals(id, collectionInsertionSlot[0].collectionAlias)
        assertEquals(id, collectionInsertionSlot[1].collectionAlias)
        assertEquals(id, collectionInsertionSlot[2].collectionAlias)
        assertEquals(0, collectionInsertionSlot[0].indexValue)
        assertEquals(1, collectionInsertionSlot[1].indexValue)
        assertEquals(2, collectionInsertionSlot[2].indexValue)
    }

    @Test
    fun `getCollection returns api result (shouldIgnore false, stale true(in db))`() =
        runTest {
            val timeUntilUpdateMinutes = 5
            every { contentConfig().cacheTimeUntilUpdateMinutes } returns timeUntilUpdateMinutes

            val cacheDate = Calendar.getInstance()
            cacheDate.set(2022, Calendar.FEBRUARY, 8, 11, 0, 0)

            val mockCurrentDate = Calendar.getInstance()
            mockCurrentDate.set(2022, Calendar.FEBRUARY, 8, 17, 0, 0)
            // mock date should be stale
            mockkObject(Utils)
            every { Utils.currentTime() } returns mockCurrentDate.time
            val item0 = fromJson(
                collectionJson0,
                ArcXPContentElement::class.java
            )!!
            val item1 = fromJson(
                collectionJson1,
                ArcXPContentElement::class.java
            )!!
            val item2 = fromJson(
                collectionJson2,
                ArcXPContentElement::class.java
            )!!
            val expectedMap = HashMap<Int, ArcXPContentElement>()
            expectedMap[0] = item0
            expectedMap[1] = item1
            expectedMap[2] = item2
            val expected = Success(success = expectedMap)
            coEvery {
                cacheManager.getCollection(
                    collectionAlias =  "collectionAlias",
                    from = 0,
                    size = DEFAULT_PAGINATION_SIZE
                )
            } returns mapOf(
                0 to fromJson(collectionJson0, ArcXPContentElement::class.java)!!,
                1 to fromJson(collectionJson1, ArcXPContentElement::class.java)!!,
                2 to fromJson(collectionJson2, ArcXPContentElement::class.java)!!,
            )
            coEvery { cacheManager.getJsonById(uuid = "5QOT2SC6CNDHHDAH3RG3FVND6M") } returns JsonItem(
                uuid = "5QOT2SC6CNDHHDAH3RG3FVND6M",
                jsonResponse = storyJson0,
                expiresAt = Date()
            )
            coEvery { cacheManager.getJsonById(uuid = "TQAJOBEGYJAQTBK4LHNFDRCWY4") } returns JsonItem(
                uuid = "TQAJOBEGYJAQTBK4LHNFDRCWY4",
                jsonResponse = storyJson1,
                expiresAt = Date()
            )
            coEvery { cacheManager.getJsonById(uuid = "KUD5XN7BMFHY7FKPT23WJ5TXQI") } returns JsonItem(
                uuid = "KUD5XN7BMFHY7FKPT23WJ5TXQI",
                jsonResponse = storyJson2,
                expiresAt = Date()
            )
            coEvery {
                contentApiManager.getContent(id = "5QOT2SC6CNDHHDAH3RG3FVND6M")
            } returns Success(Pair(storyJson0, Date()))
            coEvery {
                contentApiManager.getContent(id = "TQAJOBEGYJAQTBK4LHNFDRCWY4")
            } returns Success(Pair(storyJson1, Date()))
            coEvery {
                contentApiManager.getContent(id = "KUD5XN7BMFHY7FKPT23WJ5TXQI")
            } returns Success(Pair(storyJson2, Date()))
            coEvery {
                contentApiManager.getCollection(
                    collectionAlias =  "collectionAlias",
                    size = DEFAULT_PAGINATION_SIZE,
                    from = 0,
                    full = true
                )
            } returns Success(Pair(collectionListJson, Date()))

            val actual = testObject.getCollection(
                collectionAlias =  "collectionAlias",
                shouldIgnoreCache = false,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0
            )

            assertEquals(expected, actual)
            val collectionInsertionSlot = mutableListOf<CollectionItem>()
            val jsonInsertionSlot = mutableListOf<JsonItem>()
            coVerify(exactly = 3) { cacheManager.insert(capture(collectionInsertionSlot), capture(jsonInsertionSlot)) }
            assertEquals(
                fromJson(collectionJson0, ArcXPContentElement::class.java),
                fromJson(jsonInsertionSlot[0].jsonResponse, ArcXPContentElement::class.java)
            )
            assertEquals(
                fromJson(collectionJson1, ArcXPContentElement::class.java),
                fromJson(jsonInsertionSlot[1].jsonResponse, ArcXPContentElement::class.java)
            )
            assertEquals(
                fromJson(collectionJson2, ArcXPContentElement::class.java),
                fromJson(jsonInsertionSlot[2].jsonResponse, ArcXPContentElement::class.java)
            )
            assertEquals("collectionAlias", collectionInsertionSlot[0].collectionAlias)
            assertEquals("collectionAlias", collectionInsertionSlot[1].collectionAlias)
            assertEquals("collectionAlias", collectionInsertionSlot[2].collectionAlias)
            assertEquals(0, collectionInsertionSlot[0].indexValue)
            assertEquals(1, collectionInsertionSlot[1].indexValue)
            assertEquals(2, collectionInsertionSlot[2].indexValue)
        }

    @Test
    fun `getCollection returns api result with null date (shouldIgnore false, stale true(in db))`() =
        runTest {
            val timeUntilUpdateMinutes = 5
            every { contentConfig().cacheTimeUntilUpdateMinutes } returns timeUntilUpdateMinutes

            val cacheDate = Calendar.getInstance()
            cacheDate.set(2022, Calendar.FEBRUARY, 8, 11, 0, 0)

            val mockCurrentDate = Calendar.getInstance()
            mockCurrentDate.set(2022, Calendar.FEBRUARY, 8, 17, 0, 0)
            // mock date should be stale
            mockkObject(Utils)
            every { Utils.currentTime() } returns mockCurrentDate.time
            val item0 = fromJson(
                collectionJson0,
                ArcXPContentElement::class.java
            )!!
            val item1 = fromJson(
                collectionJson1,
                ArcXPContentElement::class.java
            )!!
            val item2 = fromJson(
                collectionJson2,
                ArcXPContentElement::class.java
            )!!
            val expectedMap = HashMap<Int, ArcXPContentElement>()
            expectedMap[0] = item0
            expectedMap[1] = item1
            expectedMap[2] = item2
            val expected = Success(success = expectedMap)
            coEvery {
                cacheManager.getCollection(
                    collectionAlias =  "collectionAlias",
                    from = 0,
                    size = DEFAULT_PAGINATION_SIZE
                )
            } returns mapOf(
                0 to fromJson(collectionJson0, ArcXPContentElement::class.java)!!,
                1 to fromJson(collectionJson1, ArcXPContentElement::class.java)!!,
                2 to fromJson(collectionJson2, ArcXPContentElement::class.java)!!,
            )
            coEvery { cacheManager.getJsonById(uuid = "5QOT2SC6CNDHHDAH3RG3FVND6M") } returns JsonItem(
                uuid = "5QOT2SC6CNDHHDAH3RG3FVND6M",
                jsonResponse = storyJson0,
                expiresAt = Date()
            )
            coEvery { cacheManager.getJsonById(uuid = "TQAJOBEGYJAQTBK4LHNFDRCWY4") } returns JsonItem(
                uuid = "TQAJOBEGYJAQTBK4LHNFDRCWY4",
                jsonResponse = storyJson1,
                expiresAt = Date()
            )
            coEvery { cacheManager.getJsonById(uuid = "KUD5XN7BMFHY7FKPT23WJ5TXQI") } returns JsonItem(
                uuid = "KUD5XN7BMFHY7FKPT23WJ5TXQI",
                jsonResponse = storyJson2,
                expiresAt = Date()
            )
            coEvery {
                contentApiManager.getContent(id = "5QOT2SC6CNDHHDAH3RG3FVND6M")
            } returns Success(Pair(storyJson0, Date()))
            coEvery {
                contentApiManager.getContent(id = "TQAJOBEGYJAQTBK4LHNFDRCWY4")
            } returns Success(Pair(storyJson1, Date()))
            coEvery {
                contentApiManager.getContent(id = "KUD5XN7BMFHY7FKPT23WJ5TXQI")
            } returns Success(Pair(storyJson2, Date()))
            coEvery {
                contentApiManager.getCollection(
                    collectionAlias =  "collectionAlias",
                    size = DEFAULT_PAGINATION_SIZE,
                    from = 0,
                    full = true
                )
            } returns Success(Pair(collectionListJson, Date()))
            coEvery { cacheManager.getCollectionExpiration("collectionAlias") } returns null

            val actual = testObject.getCollection(
                collectionAlias =  "collectionAlias",
                shouldIgnoreCache = false,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0
            )

            assertEquals(expected, actual)
            val collectionInsertionSlot = mutableListOf<CollectionItem>()
            val jsonInsertionSlot = mutableListOf<JsonItem>()
            coVerify(exactly = 3) { cacheManager.insert(capture(collectionInsertionSlot), capture(jsonInsertionSlot)) }
            assertEquals(
                fromJson(collectionJson0, ArcXPContentElement::class.java),
                fromJson(jsonInsertionSlot[0].jsonResponse, ArcXPContentElement::class.java)
            )
            assertEquals(
                fromJson(collectionJson1, ArcXPContentElement::class.java),
                fromJson(jsonInsertionSlot[1].jsonResponse, ArcXPContentElement::class.java)
            )
            assertEquals(
                fromJson(collectionJson2, ArcXPContentElement::class.java),
                fromJson(jsonInsertionSlot[2].jsonResponse, ArcXPContentElement::class.java)
            )
            assertEquals("collectionAlias", collectionInsertionSlot[0].collectionAlias)
            assertEquals("collectionAlias", collectionInsertionSlot[1].collectionAlias)
            assertEquals("collectionAlias", collectionInsertionSlot[2].collectionAlias)
            assertEquals(0, collectionInsertionSlot[0].indexValue)
            assertEquals(1, collectionInsertionSlot[1].indexValue)
            assertEquals(2, collectionInsertionSlot[2].indexValue)
        }

    @Test
    fun `getCollection returns stale db result when api fails (shouldIgnore false, stale true(in db), api fail)`() =
        runTest {
            val timeUntilUpdateMinutes = 5
            coEvery { contentConfig().cacheTimeUntilUpdateMinutes } returns timeUntilUpdateMinutes

            val cacheDate = Calendar.getInstance()
            cacheDate.set(2022, Calendar.FEBRUARY, 8, 11, 0, 0)

            val mockCurrentDate = Calendar.getInstance()
            mockCurrentDate.set(2022, Calendar.FEBRUARY, 8, 17, 0, 0)
            // mock date should be stale
            mockkObject(Utils)
            every { Utils.currentTime() } returns mockCurrentDate.time
            val item0 = fromJson(
                collectionJson0,
                ArcXPContentElement::class.java
            )!!
            val item1 = fromJson(
                collectionJson1,
                ArcXPContentElement::class.java
            )!!
            val item2 = fromJson(
                collectionJson2,
                ArcXPContentElement::class.java
            )!!
            val expectedMap = HashMap<Int, ArcXPContentElement>()
            expectedMap[0] = item0
            expectedMap[1] = item1
            expectedMap[2] = item2
            val expected = Success(success = expectedMap)

            coEvery {
                cacheManager.getCollection(
                    collectionAlias =  "collectionAlias",
                    from = 0,
                    size = DEFAULT_PAGINATION_SIZE
                )
            } returns mapOf(
                0 to item0,
                1 to item1,
                2 to item2,
            )
            coEvery { cacheManager.getJsonById(uuid = "5QOT2SC6CNDHHDAH3RG3FVND6M") } returns JsonItem(
                uuid = "5QOT2SC6CNDHHDAH3RG3FVND6M",
                jsonResponse = storyJson0,
                expiresAt = Date()
            )
            coEvery { cacheManager.getJsonById(uuid = "TQAJOBEGYJAQTBK4LHNFDRCWY4") } returns JsonItem(
                uuid = "TQAJOBEGYJAQTBK4LHNFDRCWY4",
                jsonResponse = storyJson1,
                expiresAt = Date()
            )
            coEvery { cacheManager.getJsonById(uuid = "KUD5XN7BMFHY7FKPT23WJ5TXQI") } returns JsonItem(
                uuid = "KUD5XN7BMFHY7FKPT23WJ5TXQI",
                jsonResponse = storyJson2,
                expiresAt = Date()
            )
            coEvery {
                contentApiManager.getContent(id = "5QOT2SC6CNDHHDAH3RG3FVND6M")
            } returns Success(Pair(storyJson0, Date()))
            coEvery {
                contentApiManager.getContent(id = "TQAJOBEGYJAQTBK4LHNFDRCWY4")
            } returns Success(Pair(storyJson1, Date()))
            coEvery {
                contentApiManager.getContent(id = "KUD5XN7BMFHY7FKPT23WJ5TXQI")
            } returns Success(Pair(storyJson2, Date()))
            coEvery {
                contentApiManager.getCollection(
                    collectionAlias =  "collectionAlias",
                    size = DEFAULT_PAGINATION_SIZE,
                    from = 0,
                    full = true
                )
            } returns Failure(
                ArcXPException(
                    type = ArcXPSDKErrorType.SERVER_ERROR,
                    message = "error"
                )
            )

            val actual = testObject.getCollection(
                collectionAlias =  "collectionAlias",
                shouldIgnoreCache = false,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0
            )

            assertEquals(expected, actual)
        }

    @Test
    fun `getCollection returns api result (shouldIgnore false, stale true(not in db))`() =
        runTest {
            val collectionJson = getJson("collectionFull.json")
            val collectionList = fromJson(
                collectionJson,
                Array<ArcXPContentElement>::class.java
            )!!.toList()
            val map = mapOf(0 to collectionList[0], 1 to collectionList[1], 2 to collectionList[2])
            val expected = Success(map)
            coEvery {
                cacheManager.getCollection(
                    collectionAlias =  id,
                    from = 0,
                    size = DEFAULT_PAGINATION_SIZE
                )
            } returns emptyMap()
            coEvery {
                contentApiManager.getCollection(
                    collectionAlias =  id,
                    size = DEFAULT_PAGINATION_SIZE,
                    from = 0,
                    full = true
                )
            } returns Success(Pair(collectionJson, Date()))

            val actual = testObject.getCollection(
                collectionAlias =  id,
                shouldIgnoreCache = false,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0
            )

            assertEquals(expected, actual)

            val collectionInsertionSlot = mutableListOf<CollectionItem>()
            val jsonInsertionSlot = mutableListOf<JsonItem>()
            coVerify(exactly = 3) { cacheManager.insert(collectionItem = capture(collectionInsertionSlot), jsonItem = capture(jsonInsertionSlot)) }

            assertEquals(
                collectionList[0],
                fromJson(jsonInsertionSlot[0].jsonResponse, ArcXPContentElement::class.java)
            )

            assertEquals(
                collectionList[1],
                fromJson(jsonInsertionSlot[1].jsonResponse, ArcXPContentElement::class.java)
            )

            assertEquals(
                collectionList[2],
                fromJson(jsonInsertionSlot[2].jsonResponse, ArcXPContentElement::class.java)
            )
            assertEquals(id, collectionInsertionSlot[0].collectionAlias)
            assertEquals(id, collectionInsertionSlot[1].collectionAlias)
            assertEquals(id, collectionInsertionSlot[2].collectionAlias)
            assertEquals(0, collectionInsertionSlot[0].indexValue)
            assertEquals(1, collectionInsertionSlot[1].indexValue)
            assertEquals(2, collectionInsertionSlot[2].indexValue)
        }

    @Test
    fun `getCollection failure from api`() = runTest {
        val expectedError = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = "Get Collection result was Empty"
        )
        val expected = Failure(expectedError)
        coEvery {
            cacheManager.getCollection(
                collectionAlias =  id,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns emptyMap()
        coEvery {
            contentApiManager.getCollection(
                collectionAlias =  id,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE,
                full = true
            )
        } returns expected

        val actual = testObject.getCollection(
            collectionAlias =  id,
            shouldIgnoreCache = false,
            size = DEFAULT_PAGINATION_SIZE,
            from = 0
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `getCollection success from api, but list was empty`() = runTest {
        val json = "[]"
        val expectedResponse = Success(Pair(json, Date()))
        val expectedError = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = "Get Collection result was Empty"
        )
        val expected = Failure(expectedError)
        coEvery {
            cacheManager.getCollection(
                collectionAlias =  id,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns emptyMap()
        coEvery {
            contentApiManager.getCollection(
                collectionAlias =  id,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE,
                full = true
            )
        } returns expectedResponse

        val actual = testObject.getCollection(
            collectionAlias =  id,
            shouldIgnoreCache = false,
            size = DEFAULT_PAGINATION_SIZE,
            from = 0
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `getCollection success from api, but list had deserialization error`() = runTest {
        val json = "not Valid Json List"
        val expectedResponse = Success(Pair(json, Date()))
        val expectedError = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = "Get Collection Deserialization Error"
        )
        val expected = Failure(expectedError)
        coEvery {
            cacheManager.getCollection(
                collectionAlias =  id,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns emptyMap()
        coEvery {
            contentApiManager.getCollection(
                collectionAlias =  id,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE,
                full = true
            )
        } returns expectedResponse

        val actual = testObject.getCollection(
            collectionAlias =  id,
            shouldIgnoreCache = false,
            size = DEFAULT_PAGINATION_SIZE,
            from = 0
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `searchSuspend success returns api result`() = runTest {
        val keywords = "keywords"
        val from = 99
        val size = 3
        val expectedResponse = mockk<Map<Int, ArcXPContentElement>>()
        val expected = Success(expectedResponse)
        coEvery {
            contentApiManager.search(
                searchTerm = keywords,
                from = from,
                size = size
            )
        } returns expected

        val actual =
            testObject.searchSuspend(searchTerm = keywords, from = from, size = size)

        assertEquals(expected, actual)
    }

    @Test
    fun `searchSuspend failure returns api result`() = runTest {
        coEvery {
            contentApiManager.search(
                searchTerm = keywords,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns expectedFailure

        val actual = testObject.searchSuspend(searchTerm = keywords)

        assertEquals(expectedFailure, actual)
    }

    @Test
    fun `searchAsJsonSuspend success returns api result`() = runTest {
        val keywords = "keywords"
        val from = 99
        val size = 3
        val expectedResponse = "json"
        val expected = Success(expectedResponse)
        coEvery {
            contentApiManager.searchAsJson(
                searchTerm = keywords,
                from = from,
                size = size
            )
        } returns expected

        val actual =
            testObject.searchAsJsonSuspend(searchTerm = keywords, from = from, size = size)

        assertEquals(expected, actual)
    }

    @Test
    fun `searchAsJsonSuspend failure returns api result`() = runTest {
        coEvery {
            contentApiManager.searchAsJson(
                searchTerm = keywords,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns expectedFailure

        val actual = testObject.searchAsJsonSuspend(searchTerm = keywords)

        assertEquals(expectedFailure, actual)
    }

    @Test
    fun `searchVideosByKeywordsSuspend success returns api result`() = runTest {
        val keywords = "keywords"
        val from = 99
        val size = 3
        val expectedResponse = mockk<Map<Int, ArcXPContentElement>>()
        val expected = Success(expectedResponse)
        coEvery {
            contentApiManager.searchVideos(
                searchTerm = keywords,
                from = from,
                size = size
            )
        } returns expected

        val actual =
            testObject.searchVideosSuspend(searchTerm = keywords, from = from, size = size)

        assertEquals(expected, actual)
    }

    @Test
    fun `searchVideosByKeywordsSuspend failure returns api result`() = runTest {
        coEvery {
            contentApiManager.searchVideos(
                searchTerm = keywords,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns expectedFailure

        val actual = testObject.searchVideosSuspend(searchTerm = keywords)

        assertEquals(expectedFailure, actual)
    }

    @Test
    fun `getCollectionJsonSuspend on success`() = runTest {
        val expectedResponse = Success(success = Pair(expectedJson, Date()))
        val expected = Success(success = expectedJson)
        coEvery {
            contentApiManager.getCollection(collectionAlias = id, from = 837, size = 983)
        } returns expectedResponse

        val actual = testObject.getCollectionAsJson(id = id, from = 837, size = 983)

        assertEquals(expected, actual)
    }

    @Test
    fun `getCollectionJsonSuspend on failure`() = runTest {
        coEvery {
            contentApiManager.getCollection(any(), any(), any())
        } returns expectedFailure

        val actual = testObject.getCollectionAsJson(id = id, from = 837, size = 983)

        assertEquals(expectedFailure, actual)
    }

    @Test
    fun `getContentAsJson on success`() = runTest {
        val expectedResponse = Success(success = Pair(expectedJson, Date()))
        val expected = Success(success = expectedJson)
        coEvery {
            contentApiManager.getContent(any())
        } returns expectedResponse

        val actual = testObject.getContentAsJson(id = id)

        assertEquals(expected, actual)
    }

    @Test
    fun `getContentAsJson on failure`() = runTest {
        val expected = Failure(expectedError)
        coEvery {
            contentApiManager.getContent(any())
        } returns expected

        val actual = testObject.getContentAsJson(id = id)


        assertEquals(expected, actual)
    }

    @Test
    fun `getSectionListAsJson on success`() = runTest {
        val expectedResult = Success(Pair(expectedJson, Date()))
        val expected = Success(expectedJson)
        coEvery {
            contentApiManager.getSectionList()
        } returns expectedResult

        val actual = testObject.getSectionListAsJson()

        assertEquals(expected, actual)
    }

    @Test
    fun `getSectionListAsJson on failure`() = runTest {
        val expected = Failure(expectedError)
        coEvery {
            contentApiManager.getSectionList()
        } returns expected

        val actual = testObject.getSectionListAsJson()

        assertEquals(expected, actual)
    }

    @Test
    fun `delete collection calls cache manager`()= runTest {
        testObject.deleteCollection(collectionAlias = "alias")
        coVerifySequence {
            cacheManager.deleteCollection(collectionAlias = "alias")
        }
    }

    @Test
    fun `delete item calls cache manager`() = runTest {
        testObject.deleteItem(uuid = "id")
        coVerifySequence {
            cacheManager.deleteItem(uuid = "id")
        }
    }

    @Test
    fun `delete cache calls cache manager`() = runTest {
        testObject.deleteCache()
        coVerifySequence {
            cacheManager.deleteAll()
        }
    }



    private val storyJson0 =
        "{\"_id\":\"id\",\"content_elements\":[{\"_id\":\"5QOT2SC6CNDHHDAH3RG3FVND6M\",\"additional_properties\":{\"comments\":[]},\"content\":\"ánteponánt audiebamus porrecta reperiri usus. Certissimam commenticiam liberemus sed sermone. Cónscientia de difficilem exquisitis multa. Appetendum comparavérit definitionem iuberet, máximám médiocrém pertinacia quidam scaevola.\",\"type\":\"text\"},{\"_id\":\"4RKAFSAGANF3TD723V7DGHD6YM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Certáe divelli ferántur graecos, honoris imperdiet quaeritur quoniam reprehensione. Aptiór confidam córpórisque eram fames, fecisse ibidem reperiuntur tibi ullum. Admonere affluérét elitr magna. Aiunt aut democriti earum equidem, finibus illám interiret posse praetereat, quidam testibus.\",\"type\":\"text\"},{\"_id\":\"KP4E3KKS2BFKJNK4M75ZQZPY44\",\"additional_properties\":{\"comments\":[]},\"content\":\"Apeirian dare ei nihil percipit quibusdam repetitis solent vituperandae. Acuti aliquos bonorum eárumque, imitarentur libenter ménté quaestio, quoddám ratione tranquillat. Declinatio dicitis invidus legum opés, praésént pugnáre scelerisque utuntur. Arbitramur controversia esse fecerint, optari permagna temeritas. Augeri detractis duo intus labórum, máiorá óptimus.\",\"type\":\"text\"},{\"_id\":\"4O5XWH6Y7BEUVKJPZSKJDZ6CWM\",\"additional_properties\":{\"comments\":[]},\"content_elements\":[{\"_id\":\"RBFL3XWDMBFP3CF5NNI23YSXJU\",\"additional_properties\":{\"comments\":[]},\"content\":\"The lazy fox jumped over the brown dog.\",\"type\":\"text\"}],\"subtype\":\"blockquote\",\"type\":\"quote\"},{\"_id\":\"Y76ZEHFBJVATTG6XJ22MEATE5E\",\"additional_properties\":{\"comments\":[]},\"content\":\"Aliqua artis civibus erróribus expeteremus explicatam fábulis gravissimis hendrerit levius libido maluisset tuum. Chrysippo consistát constituámus epicureis, inesse iudicatum parendum quem ullamco. Diceret discidia illustriora improborum inquam insidiarum insipientiam interrogare quamquam repetitis senserit temperantia tráctátos vetuit. Aspernatur fana honestum iudicem, mei quicquam.\",\"type\":\"text\"},{\"_id\":\"BUPS2MFH2FG7NNYKR24UQ267B4\",\"additional_properties\":{\"comments\":[]},\"content\":\"Aliquid ceterórum chrysippe explicabo maximeque nonne parta quoddam recta. Dicitur industriae iusteque liberae operosam, persecuti tólerabiles vos. Accedere contemnit deseruisse dómus mundus, náturá sequitur summam. Futurové iucunditaté noluisse quis. ánteponánt erigimur faciendi inermis malivoli, molestiae mollitia patiatur scipio vidisse. Aristótelem dicenda effluere intellegámus neque scribéntur sentiamus tradere.\",\"type\":\"text\"},{\"_id\":\"Q5XSDPJWSFDIHIN4QWJTQWLE2M\",\"additional_properties\":{\"comments\":[],\"galleries\":[],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"4390327218_334de2dce7_o.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"owner\":\"robin.giannattasio@washpost.com\",\"proxyUrl\":\"/resizer/r1VGI4K5B5nJNipJyzHF71I9dO0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/r1VGI4K5B5nJNipJyzHF71I9dO0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"thumbnailResizeUrl\":\"/resizer/OaQLWMRK0YqYdVwAenZpc_c7MO0\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"version\":0},\"address\":{},\"created_date\":\"2019-02-21T20:10:51Z\",\"credits\":{},\"height\":2328,\"last_updated_date\":\"2019-02-21T20:10:51Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Poe in the Snow\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/r1VGI4K5B5nJNipJyzHF71I9dO0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"version\":\"0.10.3\",\"width\":3612},{\"_id\":\"JHLMYCTDTNGNVCRNKEQVKXIIAE\",\"additional_properties\":{\"comments\":[]},\"content\":\"Consequentis contineri dedocendi depulsa, docui eventurum illa iucunditas, iucundo licet montes num privámur reperiri sequi. Convéniré ero explicatis incidant instituit liquidae óptimus sentiunt spernat telos. Convallis impetus liberabuntur quaeso tueri. Comprobavit fabellas philosopho sapienti. Alienum defatigatio exhorrescere futuros gymnásiá, privatió stultorum torquatum.\",\"type\":\"text\"},{\"_id\":\"CYPEAYOB2JAHVHZBEMNC2HQQXM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Coerceri displicet fore urna. éxquisitis ferri navigandi officia, totam videntur. ácutum adquiescere declinátio dicas doming, error graecam meque motu.\",\"type\":\"text\"},{\"_id\":\"POQ5CYZ4VBE6ZG473UBZKWSRYM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Genuit lineam lucilius pacem, porta provocarem quamvis. Libidinum pertinaces scelerisque triarium. Discordia existimant filio illó, magnus numerándá quondám reprimique, splendore sua. Albucius collegi electis événiunt, éxcruciant expectátá hórtensió insidiarum, omnino operam ópinemur primó uberiora. Didicisse diligamus ii plusque ponatur timentis. Ait a\",\"type\":\"text\"},{\"_id\":\"YDHIVLXHS5HXLBZHU364O454ZI\",\"additional_properties\":{\"comments\":[]},\"content\":\"Lorem ipsum Dolor\",\"type\":\"header\"},{\"_id\":\"WNHHI5L4VBB77P6FN6A3VE6WOQ\",\"additional_properties\":{\"comments\":[]},\"content\":\"tomi discidia firmam fruentem, héndrérit multos régula statim.\",\"type\":\"text\"},{\"_id\":\"U4DLNKJ3SBBAPNSHPHJ4UBX67M\",\"additional_properties\":{\"comments\":[]},\"content\":\"Adhuc ámici ángore fugit graecis istae manilium minime parum reprehensiónes respondendum retinent turpis vidérér. ártes comit dolorum intervenire iustius nivém novi potione quidém reiciendis responsum satisfacit. Cognitione collégi défuturum dicemus eruditi illustrióra intellegaturque intellegimus ista maledici praeter religionis tractavissent. Aliquó allicit cónducunt cónsequuntur dediti fabulis iustitia iusto late periculis praéséntium scriptum suas sumus videntur. Attulit depravate magnum male referuntur, répériétur vitium. Est foré locatus oratoribus quadam quoqué sit velim vivéndo.\",\"type\":\"text\"},{\"_id\":\"G5WKDE5NEVBQ5AAMOPSWWJFFPA\",\"additional_properties\":{\"comments\":[],\"galleries\":[],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_6132.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"owner\":\"wyatt.johnston@washpost.com\",\"proxyUrl\":\"/resizer/FSqRLYAb2Uj_Q0s7Lfe-2vB5XJI\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/FSqRLYAb2Uj_Q0s7Lfe-2vB5XJI\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"thumbnailResizeUrl\":\"/resizer/P3NAaW-gRaAkJRV4HvvA9GJqiK0\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"version\":0},\"address\":{},\"created_date\":\"2019-02-21T21:55:52Z\",\"credits\":{},\"height\":1386,\"last_updated_date\":\"2019-02-21T21:55:52Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Sleepy Merlin\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/FSqRLYAb2Uj_Q0s7Lfe-2vB5XJI\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"version\":\"0.10.3\",\"width\":1040},{\"_id\":\"K7KTX2LXTFA7RIQOHHEZM4EZRM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Aristoteli cyrenaicisque delectatum facer, maledici materia mauris molestie notae pórrecta putamus sóluta tibi vehicula. Amicitiam consetetur contentus cyrenaicos eláboráre éxcépturi industria iudicant liberalitati obcaecati personae potest praeterita sinit. Civibus contra efficitur eirmod, facérém ista iustó liberos, nostrum quarum. Dóctióres mundi quibus tránsferrem. Altéra conducunt dico dissensió dissentiet, domesticarum habitassé inesse legum próbantur, suspicor talem tempóribus vestrae. Adiit fabulas fictaé incursióne infánti, legendam meque praeclarorum reiciendis rem sentiri stabilém venandi véstibulum video.\",\"type\":\"text\"},{\"_id\":\"X4TPTSYPBNHLFDS4Z7B72QDTP4\",\"additional_properties\":{\"comments\":[]},\"content\":\"I can haz cheeseburger\",\"type\":\"interstitial_link\",\"url\":\"https://icanhas.cheezburger.com/\"},{\"_id\":\"BSC7BCWPRNF2HIMZHQMD6N4VKM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Autém bibendum deorsus dialéctica haé, ipsi ómnia peccandi pervenias possum quámvis soliditátem témporé. Caeco collegi infinito instituendarum mollit paratus perpauca pleniórem praeclara quaerenda quálisque torquatum utraque veritatis. Assecutus compositis desistunt effluere expetitur faciunt intus iracundia libidinosarum sálutátus stabiliqué. Aequi carum comprehenderit delectat disputando, genuit pronuntiaret. ádest amori effluere locum novi, sapien traditur vituperari. Continent emolumento inanes possunt. Adhibuit innumerabiles perpetuam philosopho solitudo utrum.\",\"type\":\"text\"},{\"_id\":\"MZK7XM3IEFG5FEZSLA6TZ3PSZM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Cupio desperantes gloriosis latinis phasellus. Epicurus equos foris iucunda legantur perciperet. Acri alienus amicitiae fuscé gratia, honestum liberae póssum posuere quibus quocircá sophocles vivere. Conciliant delectet invidus nostram recta sero solitudo.\",\"type\":\"text\"},{\"_id\":\"33HATHXZGBDQDHWXYBHEVZUMGU\",\"additional_properties\":{\"comments\":[]},\"content\":\"Distinguique exercitationem fastidii finitas quoddam repugnantibus. Arridéns attento nimium óblivióne pártá, pérspécta quoquo. Declinatio faucibus leo ómittendis torquentur, utuntur vacuitate vituperátum. Conclusionemque dissentiás ferant iustioribus, multa partes putámus. Aequi cómmódó fit honestatis manum. Adhibenda eám efficiát éruditioném iucunditatem, mel negent parvos physicis platone respirare sedatio s\",\"type\":\"text\"},{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"additional_properties\":{\"comments\":[],\"has_published_copy\":true,\"owner\":\"matthew.nelson@washpost.com\",\"published\":true,\"roles\":[],\"version\":4},\"canonical_url\":\"/2021/08/16/cats-of-arc/\",\"content_elements\":[{\"_id\":\"BNPYOMX7LBCT3FXRYQGRWBBNAM\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_5752.JPG\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"published\":true,\"resizeUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"thumbnailResizeUrl\":\"/resizer/Z_PxsFTrQjjQYOmtz8nEg1Gt8r4\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:51:24Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":2123,\"last_updated_date\":\"2019-02-21T16:51:26Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"michelle cat\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":\"0.10.3\",\"width\":2438},{\"_id\":\"TONB3GC7SBAQDGACCGA5UPF5UA\",\"additional_properties\":{\"galleries\":[],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_2775.JPG\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/Tc6HUQgIzIa9_YHTb4mudOjw2H0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"published\":true,\"resizeUrl\":\"/resizer/Tc6HUQgIzIa9_YHTb4mudOjw2H0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"thumbnailResizeUrl\":\"/resizer/vsJZ9jjBKXS9sX-43csyKDJp81k\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"version\":0},\"address\":{},\"credits\":{},\"height\":4032,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"monte\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/Tc6HUQgIzIa9_YHTb4mudOjw2H0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"version\":\"0.10.3\",\"width\":3024},{\"_id\":\"5OPJFEJNWBGUTMA3R3Y6JAVW5I\",\"additional_properties\":{\"galleries\":[],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"Image from iOS (1).jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/IWiMhBwHYFP3m4iVDRAW9gP6QYg\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/IWiMhBwHYFP3m4iVDRAW9gP6QYg\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"thumbnailResizeUrl\":\"/resizer/YsGB2Y9sxAsMHulL8sAShSnoat4\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"version\":0},\"address\":{},\"credits\":{},\"height\":4032,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Cat\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/IWiMhBwHYFP3m4iVDRAW9gP6QYg\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"version\":\"0.10.3\",\"width\":3024},{\"_id\":\"C5XUI2AH2VDLDBKJRFNB73XYWM\",\"additional_properties\":{\"galleries\":[],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"V__30CF.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/GcRh0kB9tA6szaWFMtzNSB9cbPY\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/GcRh0kB9tA6szaWFMtzNSB9cbPY\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"thumbnailResizeUrl\":\"/resizer/5PrVpqMPBCKnRZ0kjr2t8Dy2zRU\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"version\":0},\"address\":{},\"credits\":{\"by\":[{\"name\":\"Picasa\",\"type\":\"author\"}]},\"height\":1600,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Turtle kitty\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/GcRh0kB9tA6szaWFMtzNSB9cbPY\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"version\":\"0.10.3\",\"width\":1195},{\"_id\":\"I5VLXXVEAFATRPO4PQ33WT6LAM\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"image_from_ios (1).jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"owner\":\"william.cook@washpost.com\",\"proxyUrl\":\"/resizer/g7-tSY8LEWts3Ixa_ulb8qBLxgk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/g7-tSY8LEWts3Ixa_ulb8qBLxgk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"thumbnailResizeUrl\":\"/resizer/1ZuGZuUcS8Tczr4Dxog3eIcNgUo\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:11:26Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":842,\"last_updated_date\":\"2019-02-21T16:11:32Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Miki Naps in Box\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/g7-tSY8LEWts3Ixa_ulb8qBLxgk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"version\":\"0.10.3\",\"width\":1122},{\"_id\":\"YX3I23GKNZD6RLN52XJ2PKF7LU\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[\"\",\"cat\",\"nap\",\"oreo\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"image_from_ios.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"owner\":\"william.cook@washpost.com\",\"proxyUrl\":\"/resizer/TCgU6F7dkvA_Ujg-HXhmMnNlieU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/TCgU6F7dkvA_Ujg-HXhmMnNlieU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"thumbnailResizeUrl\":\"/resizer/HT6WwVB2t1F_rbFg6PiQNtYAIS4\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:12:27Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":2268,\"last_updated_date\":\"2019-02-21T16:12:29Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Oreo Naptime\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/TCgU6F7dkvA_Ujg-HXhmMnNlieU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"version\":\"0.10.3\",\"width\":3024},{\"_id\":\"JTRWKF4N5BH3ZLMIIQ7USR4YFU\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_5590.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"owner\":\"melissa.depuydt@washpost.com\",\"proxyUrl\":\"/resizer/S-gcvu7Op2hVdOHJeTrFrHihjOU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/S-gcvu7Op2hVdOHJeTrFrHihjOU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"thumbnailResizeUrl\":\"/resizer/be0FWBcr0Op25mqvJx6pyLMq0LM\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:08:51Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":1651,\"last_updated_date\":\"2019-02-21T16:08:57Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Albus\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/S-gcvu7Op2hVdOHJeTrFrHihjOU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"version\":\"0.10.3\",\"width\":2201},{\"_id\":\"RIINOEOPJNB53KFHGEVZOYGHBQ\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_6959.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"owner\":\"melissa.depuydt@washpost.com\",\"proxyUrl\":\"/resizer/7s5HZuPDaSPriMsZGiKNkBqv93U\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/7s5HZuPDaSPriMsZGiKNkBqv93U\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"thumbnailResizeUrl\":\"/resizer/ThWn4tXKvbW6Sr-ferBkBhAD-EE\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:06:43Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":2268,\"last_updated_date\":\"2019-02-21T16:06:50Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"he fits, he sits\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/7s5HZuPDaSPriMsZGiKNkBqv93U\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"version\":\"0.10.3\",\"width\":3024},{\"_id\":\"PXSGHHECONEXHODQ6NVDZXVPZM\",\"additional_properties\":{\"galleries\":[],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_20171021_132711.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"owner\":\"susan.tyler@washpost.com\",\"proxyUrl\":\"/resizer/uyxUae4e-3YfUPDA0PBeN4ewhzk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/uyxUae4e-3YfUPDA0PBeN4ewhzk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"thumbnailResizeUrl\":\"/resizer/KjsHvYmMU4YBqp8ZFEDIO93dt8U\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"version\":0},\"address\":{},\"credits\":{},\"height\":3024,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Thriller\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/uyxUae4e-3YfUPDA0PBeN4ewhzk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"version\":\"0.10.3\",\"width\":4032},{\"_id\":\"55VYTIER35C5DIPJ5GYUR6KROE\",\"additional_properties\":{\"galleries\":[],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"the gambler.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"owner\":\"eric.carlisle@washpost.com\",\"proxyUrl\":\"/resizer/Fdw_t2G3kSEjvym1y4GPE3amFck\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/Fdw_t2G3kSEjvym1y4GPE3amFck\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"thumbnailResizeUrl\":\"/resizer/RQQhSg2Utzw6aiCjYfPzw6uHS3w\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"version\":0},\"address\":{},\"credits\":{},\"height\":898,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"The Gambler\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/Fdw_t2G3kSEjvym1y4GPE3amFck\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"version\":\"0.10.3\",\"width\":1198},{\"_id\":\"43Z756DP3RCCPHPYEERIGOXFFQ\",\"additional_properties\":{\"galleries\":[],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"secretgarden.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"owner\":\"joseph.gilbert@washpost.com\",\"proxyUrl\":\"/resizer/J0C0ik_P50egpt45xMKOi0SEG-g\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/J0C0ik_P50egpt45xMKOi0SEG-g\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"thumbnailResizeUrl\":\"/resizer/Or14R0bFgX2fOwlm2Oot_umjsEs\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"version\":0},\"address\":{},\"credits\":{},\"height\":1202,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Wildcat spotted!\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/J0C0ik_P50egpt45xMKOi0SEG-g\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"version\":\"0.10.3\",\"width\":675}],\"created_date\":\"2019-02-21T16:52:57Z\",\"credits\":{\"by\":[]},\"description\":{\"basic\":\"This is a gallery of Arc Cats.\"},\"display_date\":\"2019-02-21T16:52:57Z\",\"first_publish_date\":\"2019-02-21T16:52:57Z\",\"headlines\":{\"basic\":\"Cats of Arc\"},\"last_updated_date\":\"2021-08-16T17:24:59Z\",\"owner\":{\"id\":\"sandbox.corecomponents\"},\"promo_items\":{\"basic\":{\"_id\":\"BNPYOMX7LBCT3FXRYQGRWBBNAM\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_5752.JPG\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"published\":true,\"resizeUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"thumbnailResizeUrl\":\"/resizer/Z_PxsFTrQjjQYOmtz8nEg1Gt8r4\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:51:24Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":2123,\"last_updated_date\":\"2019-02-21T16:51:26Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"michelle cat\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":\"0.10.3\",\"width\":2438}},\"publish_date\":\"2021-08-13T15:38:49.268Z\",\"taxonomy\":{},\"type\":\"gallery\",\"version\":\"0.10.3\"},{\"_id\":\"2QT3ZMWN4ZADRCDYJ3XJXPTTUA\",\"additional_properties\":{\"comments\":[]},\"content\":\"uavitate tránsferám. Adamare consequi duxit finis, iisque intelleges summum.\",\"type\":\"text\"},{\"_id\":\"NJ4YQBFHXJAF5P7AXHAIXPBNAE\",\"additional_properties\":{\"comments\":[]},\"content\":\"Errore explicátis nonne pueri tárentinis. Curiosi déducérét gymnasia sollicitudin, soluta sunt. Culpá divitiarum dixit erunt ignavia, irridente miseram perfecto praécéptricé reque tincidunt vacuitate. Cógnósci cónfórmavit fugiendum omnino pellentesque. Béstiaé conversa fortasse gravitér hausta humili odio pérvénias stabilem suscipiantur.\",\"type\":\"text\"},{\"_id\":\"O5ICDWUHIFGMFCUCUUUGQ34KXQ\",\"subtype\":\"instagram\",\"type\":\"oembed_response\"},{\"_id\":\"5LJTL7KP7REQPIVF7BKEYHO2KY\",\"additional_properties\":{\"comments\":[]},\"content\":\"Detracto existimavit iriure posuere pugnare, putamus sapientium summum suum. Curabitur désidératuram dicenda dico, epicurus hostis intervenire mihi, omnesque práeclárorum praétéréa sévéra unum. Assentiar compréhénsam debilitati regione successerit, tempore tractavissent. Accusantibus conscientiam declinabunt dicas dixeris epicurei érat filio sinit studia suscipere. Defuturum exeduntur quia réquiréré. Aiunt dicit erudito éxpréssas graecis, imperitorum iracundia lóquerer partes praestabiliórem salutatus triárium.\",\"type\":\"text\"},{\"_id\":\"YYYCTF2AWVHLRJ7IZWIFH3FUPA\",\"additional_properties\":{\"comments\":[]},\"content\":\"benivole cóerceri cónficiuntur curá de\",\"type\":\"text\"},{\"_id\":\"CTIQTHIPYNGV5LGWU63REPDAHE\",\"additional_properties\":{\"comments\":[]},\"content\":\"Benivole coerceri donsifes\",\"type\":\"header\"},{\"_id\":\"OGOXMZ3T2NB6BEVNF2VPXRI2NE\",\"additional_properties\":{\"comments\":[]},\"content\":\"Dissident fáutrices firme hóneste libidinibus modératio morati mors saluto sévéra usus. Adversa cognitionem dicitur earumque, musicis nominis ornatus polyaeno, summumque tamquam variis. Conspiratione efflorescere elegantis medium, molestia nonné quos triarium ullo vigiliae vita.\",\"type\":\"text\"},{\"_id\":\"OZAYPLA5JFCFNFKCPKZLJOQ2GE\",\"subtype\":\"twitter\",\"type\":\"oembed_response\"},{\"_id\":\"I77Z64ZLKJAOLPZDTMF6JHQ5SY\",\"additional_properties\":{\"comments\":[]},\"content\":\"Aégritudo contereret diesque dulce eam, liberatione male putas sóciósqu. Confectum graeco patriam regione. Agam bene benivole cóerceri cónficiuntur curá defenditur dominationis sententiae stoici tenetur voluptatis. Amicum ánimus assentior circumcisaque ignoránt iste quicquid rebus triari.\",\"type\":\"text\"},{\"_id\":\"IHGAJZ3YUZDC5PWY3DYCFEFZCI\",\"additional_properties\":{\"comments\":[]},\"content_elements\":[{\"_id\":\"WBLMU57SU5F4PKVV3BZNNKZSUA\",\"additional_properties\":{\"comments\":[]},\"content\":\"Time spent with cats is never wasted. \",\"type\":\"text\"}],\"subtype\":\"pullquote\",\"type\":\"quote\"},{\"_id\":\"FGXFXPO42ZCRVHRRF3T5YQQENI\",\"additional_properties\":{\"comments\":[]},\"content\":\"Albucius class clita conspectum erudito impediri occultarum pleniorem quondam. Adhibenda careret cetero consistat cupidatat desiderent fortitudinem honéstatis inciderit quanti salutandi teneam. Eá epicureis huius optimi viris. Inprobitas iudicio justo loquuntur optime petentium plérisqué pondere suscipere. Brevi confirmaré cyrenaicos dápibus erant fecerit i indicavérunt nonumy plusque porttitor probet servire tranquillae.\",\"type\":\"text\"},{\"_id\":\"4J2FFQ23MJAH3C27Y6RZ4TK2MQ\",\"additional_properties\":{\"comments\":[]},\"content\":\"\\u003cdiv style\\u003d\\\"text-align:center;margin: 0 auto;padding: 25px; background: teal; color: white;\\\"\\u003e\\n    I am some raw html.\\n\\u003c/div\\u003e\",\"type\":\"raw_html\"},{\"_id\":\"OJXUZVPUQZAAVHMQWDLSZTCECI\",\"additional_properties\":{\"comments\":[]},\"content\":\"Beatus dominátionis errátá iudicant miserius negent praesertim sine turpis. Arcu consistat dicenda efficiatur, excepteur gubergren ignoraré laboribus négant refórmidans solet sublatum suscipit. Affirmatis arbitratu attulit class cógnitiónem, cónficiuntur nemini práetore qualisqué salutandi semel. Albuci cibo dicés habemus morborum. Affécti cuius disputata dolorum efficeretur, epicuri neglegentur quamque quapropter quodsi voce. Copiosae dediti doctiorés eademque gymnasia hábere legam malorum ócculta perturbári studiis tali tu. Argumentandum chrysippi democritum inflammat inpendente magnitudinem nacti pertinerent placatae porro repudiare scaevola tantam términatas vita.\",\"type\":\"text\"},{\"_id\":\"PYKACTM63FADDD5ST3H6FTKXXI\",\"type\":\"reference\"},{\"_id\":\"7LHUQWE7LVGX3MCVJG76YVCGFU\",\"additional_properties\":{\"comments\":[]},\"content\":\"Collegisti comparat cyrenaicisque dicás eventurum, individuá irure maioribus mors optimus, quosdám terrore. Contenta cu epicuro epicurus éxpéctat, inimicus málunt mediocris odit tali. Cadere dixeris igitur justo lectus, liberemus maluisti omnia perturbatur quia referuntur severitatem virtutibus. Adest aptius conficiuntqué expressas facillimis, legum nisi póetarum reperiri sagittis tuó. Adiit certámen delectari didicerimus expeteretur honestum humili legere moderatio pertinerent répériri vitam.\",\"type\":\"text\"},{\"_id\":\"5BNVGZNIKRBV7F7KC4I66627WA\",\"additional_properties\":{\"comments\":[]},\"content\":\"Doggo ipsum you are doin me a concern wow such tempt snoot big ol, heckin angery woofer wow such tempt clouds heckin, wow such tempt adorable doggo. Wow such tempt vvv doggorino yapper woofer, you are doin me a concern blop boof, heck wow very biscit bork. Vvv blop heckin good boys you are doing me a frighten shibe wow very biscit, shoober wow such tempt ruff floofs. Floofs waggy wags h*ck doggo puggorino pupper, woofer he made many woofs the neighborhood pupper. smol borking doggo with a long snoot for pats. The neighborhood pupper smol borking doggo with a long snoot for pats such treat extremely cuuuuuute the neighborhood pupper doggo wrinkler long bois, big ol pats fluffer boof porgo.\",\"type\":\"text\"},{\"_id\":\"WGY7KDSWDJF2TLRPWG2464AKF4\",\"additional_properties\":{\"comments\":[]},\"type\":\"list\"},{\"_id\":\"23R6XJRWNNAGPDTSTK54UPTNZ4\",\"additional_properties\":{\"comments\":[]},\"content\":\"Extremely cuuuuuute very hand that feed shibe woofer vvv, shoober. Wow such tempt most angery pupper I have ever seen you are doing me a frighten length boy borking doggo corgo long woofer the neighborhood pupper, smol long water shoob maximum borkdrive blep pats. Puggo shoob corgo blop doggorino, heck boofers. Mlem long doggo dat tungg tho borkf porgo borking doggo, wrinkler boof long woofer you are doing me the shock. You are doing me the shock long water shoob pupperino vvv, porgo shoob long bois, most angery pupper I have ever seen snoot. Puggo wrinkler extremely cuuuuuute doggorino, shoob noodle horse. Blop sub woofer you are doin me a concern puggorino doggorino, dat tungg tho boof. Shibe much ruin diet maximum borkdrive heckin good boys and girls many pats shibe shoober sub woofer, such treat very jealous pupper very good spot the neighborhood pupper heckin good boys the neighborhood pupper. Blep pupperino you are doing me a frighten shoober super chub, wow such tempt heckin good boys and girls. Very good spot porgo you are doing me a frighten extremely cuuuuuute, big ol pupper\",\"type\":\"text\"},{\"_id\":\"4ACL3OFVL5H5VCPDRT7IT6PZP4\",\"additional_properties\":{\"comments\":[]},\"type\":\"list\"},{\"_id\":\"K3KO7KD54BD5VERQTXKGO6Y6F4\",\"additional_properties\":{\"comments\":[]},\"content\":\"Sub woofer big ol maximum borkdrive aqua doggo mlem, maximum borkdrive I am bekom fat. long woofer what a nice floof. heck stop it fren. Corgo lotsa pats maximum borkdrive, blop. Lotsa pats maximum borkdrive blop fluffer shooberino, long doggo yapper he made many woofs long bois, very jealous pupper pats lotsa pats. Long water shoob I am bekom fat you are doing me a frighten heckin you are doin me a concern corgo borkdrive, clouds pupper heckin doge very hand that feed shibe. Big ol fluffer doing me a frighten most angery pupper I have ever seen thicc length boy dat tungg tho very jealous pupper porgo, floofs doggorino waggy wags most angery pupper I have ever seen ruff blop. You are doin me a concern the neighborhood pupper sub woofer very jealous pupper fluffer heck fat boi, wow very biscit heckin angery woofer borkf stop it fren yapper. Heck tungg very good spot porgo lotsa pats, long doggo pupper porgo.\",\"type\":\"text\"},{\"_id\":\"ATW7HPGNENCMZA277OTSUIMWAI\",\"additional_properties\":{\"comments\":[]},\"type\":\"table\"},{\"_id\":\"Y2NMVDMLAJBBZGOSPYMH7V34Z4\",\"additional_properties\":{\"comments\":[]},\"type\":\"list\"},{\"_id\":\"HEKD4R6EFRCV7JYKGZFNIG57T4\",\"additional_properties\":{\"comments\":[]},\"content\":\"\\u003cbr/\\u003e\",\"type\":\"text\"},{\"_id\":\"OOITKXLHK5H2BDLJOQYPJNGWKI\",\"additional_properties\":{\"comments\":[]},\"content\":\"\\u003cbr/\\u003e\",\"type\":\"text\"}],\"credits\":{\"by\":[{\"name\":\"Matt Nelson\",\"type\":\"author\"}]},\"headlines\":{\"basic\":\"Kitchen Sink Article 2: The Gazette\"},\"publish_date\":\"2021-08-13T15:38:49.268Z\",\"type\":\"story\",\"version\":\"0.10.7\",\"website_url\":\"/2019/03/06/kitchen-sink-article-2-the-gazette/\"}"
    private val storyJson1 =
        "{\"_id\":\"id\",\"content_elements\":[{\"_id\":\"TQAJOBEGYJAQTBK4LHNFDRCWY4\",\"additional_properties\":{\"comments\":[]},\"content\":\"ánteponánt audiebamus porrecta reperiri usus. Certissimam commenticiam liberemus sed sermone. Cónscientia de difficilem exquisitis multa. Appetendum comparavérit definitionem iuberet, máximám médiocrém pertinacia quidam scaevola.\",\"type\":\"text\"},{\"_id\":\"4RKAFSAGANF3TD723V7DGHD6YM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Certáe divelli ferántur graecos, honoris imperdiet quaeritur quoniam reprehensione. Aptiór confidam córpórisque eram fames, fecisse ibidem reperiuntur tibi ullum. Admonere affluérét elitr magna. Aiunt aut democriti earum equidem, finibus illám interiret posse praetereat, quidam testibus.\",\"type\":\"text\"},{\"_id\":\"KP4E3KKS2BFKJNK4M75ZQZPY44\",\"additional_properties\":{\"comments\":[]},\"content\":\"Apeirian dare ei nihil percipit quibusdam repetitis solent vituperandae. Acuti aliquos bonorum eárumque, imitarentur libenter ménté quaestio, quoddám ratione tranquillat. Declinatio dicitis invidus legum opés, praésént pugnáre scelerisque utuntur. Arbitramur controversia esse fecerint, optari permagna temeritas. Augeri detractis duo intus labórum, máiorá óptimus.\",\"type\":\"text\"},{\"_id\":\"4O5XWH6Y7BEUVKJPZSKJDZ6CWM\",\"additional_properties\":{\"comments\":[]},\"content_elements\":[{\"_id\":\"RBFL3XWDMBFP3CF5NNI23YSXJU\",\"additional_properties\":{\"comments\":[]},\"content\":\"The lazy fox jumped over the brown dog.\",\"type\":\"text\"}],\"subtype\":\"blockquote\",\"type\":\"quote\"},{\"_id\":\"Y76ZEHFBJVATTG6XJ22MEATE5E\",\"additional_properties\":{\"comments\":[]},\"content\":\"Aliqua artis civibus erróribus expeteremus explicatam fábulis gravissimis hendrerit levius libido maluisset tuum. Chrysippo consistát constituámus epicureis, inesse iudicatum parendum quem ullamco. Diceret discidia illustriora improborum inquam insidiarum insipientiam interrogare quamquam repetitis senserit temperantia tráctátos vetuit. Aspernatur fana honestum iudicem, mei quicquam.\",\"type\":\"text\"},{\"_id\":\"BUPS2MFH2FG7NNYKR24UQ267B4\",\"additional_properties\":{\"comments\":[]},\"content\":\"Aliquid ceterórum chrysippe explicabo maximeque nonne parta quoddam recta. Dicitur industriae iusteque liberae operosam, persecuti tólerabiles vos. Accedere contemnit deseruisse dómus mundus, náturá sequitur summam. Futurové iucunditaté noluisse quis. ánteponánt erigimur faciendi inermis malivoli, molestiae mollitia patiatur scipio vidisse. Aristótelem dicenda effluere intellegámus neque scribéntur sentiamus tradere.\",\"type\":\"text\"},{\"_id\":\"Q5XSDPJWSFDIHIN4QWJTQWLE2M\",\"additional_properties\":{\"comments\":[],\"galleries\":[],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"4390327218_334de2dce7_o.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"owner\":\"robin.giannattasio@washpost.com\",\"proxyUrl\":\"/resizer/r1VGI4K5B5nJNipJyzHF71I9dO0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/r1VGI4K5B5nJNipJyzHF71I9dO0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"thumbnailResizeUrl\":\"/resizer/OaQLWMRK0YqYdVwAenZpc_c7MO0\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"version\":0},\"address\":{},\"created_date\":\"2019-02-21T20:10:51Z\",\"credits\":{},\"height\":2328,\"last_updated_date\":\"2019-02-21T20:10:51Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Poe in the Snow\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/r1VGI4K5B5nJNipJyzHF71I9dO0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"version\":\"0.10.3\",\"width\":3612},{\"_id\":\"JHLMYCTDTNGNVCRNKEQVKXIIAE\",\"additional_properties\":{\"comments\":[]},\"content\":\"Consequentis contineri dedocendi depulsa, docui eventurum illa iucunditas, iucundo licet montes num privámur reperiri sequi. Convéniré ero explicatis incidant instituit liquidae óptimus sentiunt spernat telos. Convallis impetus liberabuntur quaeso tueri. Comprobavit fabellas philosopho sapienti. Alienum defatigatio exhorrescere futuros gymnásiá, privatió stultorum torquatum.\",\"type\":\"text\"},{\"_id\":\"CYPEAYOB2JAHVHZBEMNC2HQQXM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Coerceri displicet fore urna. éxquisitis ferri navigandi officia, totam videntur. ácutum adquiescere declinátio dicas doming, error graecam meque motu.\",\"type\":\"text\"},{\"_id\":\"POQ5CYZ4VBE6ZG473UBZKWSRYM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Genuit lineam lucilius pacem, porta provocarem quamvis. Libidinum pertinaces scelerisque triarium. Discordia existimant filio illó, magnus numerándá quondám reprimique, splendore sua. Albucius collegi electis événiunt, éxcruciant expectátá hórtensió insidiarum, omnino operam ópinemur primó uberiora. Didicisse diligamus ii plusque ponatur timentis. Ait a\",\"type\":\"text\"},{\"_id\":\"YDHIVLXHS5HXLBZHU364O454ZI\",\"additional_properties\":{\"comments\":[]},\"content\":\"Lorem ipsum Dolor\",\"type\":\"header\"},{\"_id\":\"WNHHI5L4VBB77P6FN6A3VE6WOQ\",\"additional_properties\":{\"comments\":[]},\"content\":\"tomi discidia firmam fruentem, héndrérit multos régula statim.\",\"type\":\"text\"},{\"_id\":\"U4DLNKJ3SBBAPNSHPHJ4UBX67M\",\"additional_properties\":{\"comments\":[]},\"content\":\"Adhuc ámici ángore fugit graecis istae manilium minime parum reprehensiónes respondendum retinent turpis vidérér. ártes comit dolorum intervenire iustius nivém novi potione quidém reiciendis responsum satisfacit. Cognitione collégi défuturum dicemus eruditi illustrióra intellegaturque intellegimus ista maledici praeter religionis tractavissent. Aliquó allicit cónducunt cónsequuntur dediti fabulis iustitia iusto late periculis praéséntium scriptum suas sumus videntur. Attulit depravate magnum male referuntur, répériétur vitium. Est foré locatus oratoribus quadam quoqué sit velim vivéndo.\",\"type\":\"text\"},{\"_id\":\"G5WKDE5NEVBQ5AAMOPSWWJFFPA\",\"additional_properties\":{\"comments\":[],\"galleries\":[],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_6132.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"owner\":\"wyatt.johnston@washpost.com\",\"proxyUrl\":\"/resizer/FSqRLYAb2Uj_Q0s7Lfe-2vB5XJI\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/FSqRLYAb2Uj_Q0s7Lfe-2vB5XJI\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"thumbnailResizeUrl\":\"/resizer/P3NAaW-gRaAkJRV4HvvA9GJqiK0\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"version\":0},\"address\":{},\"created_date\":\"2019-02-21T21:55:52Z\",\"credits\":{},\"height\":1386,\"last_updated_date\":\"2019-02-21T21:55:52Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Sleepy Merlin\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/FSqRLYAb2Uj_Q0s7Lfe-2vB5XJI\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"version\":\"0.10.3\",\"width\":1040},{\"_id\":\"K7KTX2LXTFA7RIQOHHEZM4EZRM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Aristoteli cyrenaicisque delectatum facer, maledici materia mauris molestie notae pórrecta putamus sóluta tibi vehicula. Amicitiam consetetur contentus cyrenaicos eláboráre éxcépturi industria iudicant liberalitati obcaecati personae potest praeterita sinit. Civibus contra efficitur eirmod, facérém ista iustó liberos, nostrum quarum. Dóctióres mundi quibus tránsferrem. Altéra conducunt dico dissensió dissentiet, domesticarum habitassé inesse legum próbantur, suspicor talem tempóribus vestrae. Adiit fabulas fictaé incursióne infánti, legendam meque praeclarorum reiciendis rem sentiri stabilém venandi véstibulum video.\",\"type\":\"text\"},{\"_id\":\"X4TPTSYPBNHLFDS4Z7B72QDTP4\",\"additional_properties\":{\"comments\":[]},\"content\":\"I can haz cheeseburger\",\"type\":\"interstitial_link\",\"url\":\"https://icanhas.cheezburger.com/\"},{\"_id\":\"BSC7BCWPRNF2HIMZHQMD6N4VKM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Autém bibendum deorsus dialéctica haé, ipsi ómnia peccandi pervenias possum quámvis soliditátem témporé. Caeco collegi infinito instituendarum mollit paratus perpauca pleniórem praeclara quaerenda quálisque torquatum utraque veritatis. Assecutus compositis desistunt effluere expetitur faciunt intus iracundia libidinosarum sálutátus stabiliqué. Aequi carum comprehenderit delectat disputando, genuit pronuntiaret. ádest amori effluere locum novi, sapien traditur vituperari. Continent emolumento inanes possunt. Adhibuit innumerabiles perpetuam philosopho solitudo utrum.\",\"type\":\"text\"},{\"_id\":\"MZK7XM3IEFG5FEZSLA6TZ3PSZM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Cupio desperantes gloriosis latinis phasellus. Epicurus equos foris iucunda legantur perciperet. Acri alienus amicitiae fuscé gratia, honestum liberae póssum posuere quibus quocircá sophocles vivere. Conciliant delectet invidus nostram recta sero solitudo.\",\"type\":\"text\"},{\"_id\":\"33HATHXZGBDQDHWXYBHEVZUMGU\",\"additional_properties\":{\"comments\":[]},\"content\":\"Distinguique exercitationem fastidii finitas quoddam repugnantibus. Arridéns attento nimium óblivióne pártá, pérspécta quoquo. Declinatio faucibus leo ómittendis torquentur, utuntur vacuitate vituperátum. Conclusionemque dissentiás ferant iustioribus, multa partes putámus. Aequi cómmódó fit honestatis manum. Adhibenda eám efficiát éruditioném iucunditatem, mel negent parvos physicis platone respirare sedatio s\",\"type\":\"text\"},{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"additional_properties\":{\"comments\":[],\"has_published_copy\":true,\"owner\":\"matthew.nelson@washpost.com\",\"published\":true,\"roles\":[],\"version\":4},\"canonical_url\":\"/2021/08/16/cats-of-arc/\",\"content_elements\":[{\"_id\":\"BNPYOMX7LBCT3FXRYQGRWBBNAM\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_5752.JPG\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"published\":true,\"resizeUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"thumbnailResizeUrl\":\"/resizer/Z_PxsFTrQjjQYOmtz8nEg1Gt8r4\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:51:24Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":2123,\"last_updated_date\":\"2019-02-21T16:51:26Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"michelle cat\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":\"0.10.3\",\"width\":2438},{\"_id\":\"TONB3GC7SBAQDGACCGA5UPF5UA\",\"additional_properties\":{\"galleries\":[],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_2775.JPG\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/Tc6HUQgIzIa9_YHTb4mudOjw2H0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"published\":true,\"resizeUrl\":\"/resizer/Tc6HUQgIzIa9_YHTb4mudOjw2H0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"thumbnailResizeUrl\":\"/resizer/vsJZ9jjBKXS9sX-43csyKDJp81k\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"version\":0},\"address\":{},\"credits\":{},\"height\":4032,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"monte\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/Tc6HUQgIzIa9_YHTb4mudOjw2H0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"version\":\"0.10.3\",\"width\":3024},{\"_id\":\"5OPJFEJNWBGUTMA3R3Y6JAVW5I\",\"additional_properties\":{\"galleries\":[],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"Image from iOS (1).jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/IWiMhBwHYFP3m4iVDRAW9gP6QYg\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/IWiMhBwHYFP3m4iVDRAW9gP6QYg\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"thumbnailResizeUrl\":\"/resizer/YsGB2Y9sxAsMHulL8sAShSnoat4\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"version\":0},\"address\":{},\"credits\":{},\"height\":4032,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Cat\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/IWiMhBwHYFP3m4iVDRAW9gP6QYg\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"version\":\"0.10.3\",\"width\":3024},{\"_id\":\"C5XUI2AH2VDLDBKJRFNB73XYWM\",\"additional_properties\":{\"galleries\":[],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"V__30CF.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/GcRh0kB9tA6szaWFMtzNSB9cbPY\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/GcRh0kB9tA6szaWFMtzNSB9cbPY\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"thumbnailResizeUrl\":\"/resizer/5PrVpqMPBCKnRZ0kjr2t8Dy2zRU\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"version\":0},\"address\":{},\"credits\":{\"by\":[{\"name\":\"Picasa\",\"type\":\"author\"}]},\"height\":1600,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Turtle kitty\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/GcRh0kB9tA6szaWFMtzNSB9cbPY\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"version\":\"0.10.3\",\"width\":1195},{\"_id\":\"I5VLXXVEAFATRPO4PQ33WT6LAM\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"image_from_ios (1).jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"owner\":\"william.cook@washpost.com\",\"proxyUrl\":\"/resizer/g7-tSY8LEWts3Ixa_ulb8qBLxgk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/g7-tSY8LEWts3Ixa_ulb8qBLxgk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"thumbnailResizeUrl\":\"/resizer/1ZuGZuUcS8Tczr4Dxog3eIcNgUo\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:11:26Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":842,\"last_updated_date\":\"2019-02-21T16:11:32Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Miki Naps in Box\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/g7-tSY8LEWts3Ixa_ulb8qBLxgk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"version\":\"0.10.3\",\"width\":1122},{\"_id\":\"YX3I23GKNZD6RLN52XJ2PKF7LU\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[\"\",\"cat\",\"nap\",\"oreo\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"image_from_ios.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"owner\":\"william.cook@washpost.com\",\"proxyUrl\":\"/resizer/TCgU6F7dkvA_Ujg-HXhmMnNlieU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/TCgU6F7dkvA_Ujg-HXhmMnNlieU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"thumbnailResizeUrl\":\"/resizer/HT6WwVB2t1F_rbFg6PiQNtYAIS4\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:12:27Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":2268,\"last_updated_date\":\"2019-02-21T16:12:29Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Oreo Naptime\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/TCgU6F7dkvA_Ujg-HXhmMnNlieU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"version\":\"0.10.3\",\"width\":3024},{\"_id\":\"JTRWKF4N5BH3ZLMIIQ7USR4YFU\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_5590.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"owner\":\"melissa.depuydt@washpost.com\",\"proxyUrl\":\"/resizer/S-gcvu7Op2hVdOHJeTrFrHihjOU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/S-gcvu7Op2hVdOHJeTrFrHihjOU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"thumbnailResizeUrl\":\"/resizer/be0FWBcr0Op25mqvJx6pyLMq0LM\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:08:51Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":1651,\"last_updated_date\":\"2019-02-21T16:08:57Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Albus\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/S-gcvu7Op2hVdOHJeTrFrHihjOU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"version\":\"0.10.3\",\"width\":2201},{\"_id\":\"RIINOEOPJNB53KFHGEVZOYGHBQ\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_6959.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"owner\":\"melissa.depuydt@washpost.com\",\"proxyUrl\":\"/resizer/7s5HZuPDaSPriMsZGiKNkBqv93U\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/7s5HZuPDaSPriMsZGiKNkBqv93U\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"thumbnailResizeUrl\":\"/resizer/ThWn4tXKvbW6Sr-ferBkBhAD-EE\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:06:43Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":2268,\"last_updated_date\":\"2019-02-21T16:06:50Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"he fits, he sits\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/7s5HZuPDaSPriMsZGiKNkBqv93U\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"version\":\"0.10.3\",\"width\":3024},{\"_id\":\"PXSGHHECONEXHODQ6NVDZXVPZM\",\"additional_properties\":{\"galleries\":[],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_20171021_132711.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"owner\":\"susan.tyler@washpost.com\",\"proxyUrl\":\"/resizer/uyxUae4e-3YfUPDA0PBeN4ewhzk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/uyxUae4e-3YfUPDA0PBeN4ewhzk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"thumbnailResizeUrl\":\"/resizer/KjsHvYmMU4YBqp8ZFEDIO93dt8U\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"version\":0},\"address\":{},\"credits\":{},\"height\":3024,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Thriller\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/uyxUae4e-3YfUPDA0PBeN4ewhzk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"version\":\"0.10.3\",\"width\":4032},{\"_id\":\"55VYTIER35C5DIPJ5GYUR6KROE\",\"additional_properties\":{\"galleries\":[],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"the gambler.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"owner\":\"eric.carlisle@washpost.com\",\"proxyUrl\":\"/resizer/Fdw_t2G3kSEjvym1y4GPE3amFck\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/Fdw_t2G3kSEjvym1y4GPE3amFck\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"thumbnailResizeUrl\":\"/resizer/RQQhSg2Utzw6aiCjYfPzw6uHS3w\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"version\":0},\"address\":{},\"credits\":{},\"height\":898,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"The Gambler\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/Fdw_t2G3kSEjvym1y4GPE3amFck\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"version\":\"0.10.3\",\"width\":1198},{\"_id\":\"43Z756DP3RCCPHPYEERIGOXFFQ\",\"additional_properties\":{\"galleries\":[],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"secretgarden.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"owner\":\"joseph.gilbert@washpost.com\",\"proxyUrl\":\"/resizer/J0C0ik_P50egpt45xMKOi0SEG-g\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/J0C0ik_P50egpt45xMKOi0SEG-g\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"thumbnailResizeUrl\":\"/resizer/Or14R0bFgX2fOwlm2Oot_umjsEs\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"version\":0},\"address\":{},\"credits\":{},\"height\":1202,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Wildcat spotted!\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/J0C0ik_P50egpt45xMKOi0SEG-g\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"version\":\"0.10.3\",\"width\":675}],\"created_date\":\"2019-02-21T16:52:57Z\",\"credits\":{\"by\":[]},\"description\":{\"basic\":\"This is a gallery of Arc Cats.\"},\"display_date\":\"2019-02-21T16:52:57Z\",\"first_publish_date\":\"2019-02-21T16:52:57Z\",\"headlines\":{\"basic\":\"Cats of Arc\"},\"last_updated_date\":\"2021-08-16T17:24:59Z\",\"owner\":{\"id\":\"sandbox.corecomponents\"},\"promo_items\":{\"basic\":{\"_id\":\"BNPYOMX7LBCT3FXRYQGRWBBNAM\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_5752.JPG\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"published\":true,\"resizeUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"thumbnailResizeUrl\":\"/resizer/Z_PxsFTrQjjQYOmtz8nEg1Gt8r4\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:51:24Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":2123,\"last_updated_date\":\"2019-02-21T16:51:26Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"michelle cat\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":\"0.10.3\",\"width\":2438}},\"publish_date\":\"2021-08-13T15:38:49.268Z\",\"taxonomy\":{},\"type\":\"gallery\",\"version\":\"0.10.3\"},{\"_id\":\"2QT3ZMWN4ZADRCDYJ3XJXPTTUA\",\"additional_properties\":{\"comments\":[]},\"content\":\"uavitate tránsferám. Adamare consequi duxit finis, iisque intelleges summum.\",\"type\":\"text\"},{\"_id\":\"NJ4YQBFHXJAF5P7AXHAIXPBNAE\",\"additional_properties\":{\"comments\":[]},\"content\":\"Errore explicátis nonne pueri tárentinis. Curiosi déducérét gymnasia sollicitudin, soluta sunt. Culpá divitiarum dixit erunt ignavia, irridente miseram perfecto praécéptricé reque tincidunt vacuitate. Cógnósci cónfórmavit fugiendum omnino pellentesque. Béstiaé conversa fortasse gravitér hausta humili odio pérvénias stabilem suscipiantur.\",\"type\":\"text\"},{\"_id\":\"O5ICDWUHIFGMFCUCUUUGQ34KXQ\",\"subtype\":\"instagram\",\"type\":\"oembed_response\"},{\"_id\":\"5LJTL7KP7REQPIVF7BKEYHO2KY\",\"additional_properties\":{\"comments\":[]},\"content\":\"Detracto existimavit iriure posuere pugnare, putamus sapientium summum suum. Curabitur désidératuram dicenda dico, epicurus hostis intervenire mihi, omnesque práeclárorum praétéréa sévéra unum. Assentiar compréhénsam debilitati regione successerit, tempore tractavissent. Accusantibus conscientiam declinabunt dicas dixeris epicurei érat filio sinit studia suscipere. Defuturum exeduntur quia réquiréré. Aiunt dicit erudito éxpréssas graecis, imperitorum iracundia lóquerer partes praestabiliórem salutatus triárium.\",\"type\":\"text\"},{\"_id\":\"YYYCTF2AWVHLRJ7IZWIFH3FUPA\",\"additional_properties\":{\"comments\":[]},\"content\":\"benivole cóerceri cónficiuntur curá de\",\"type\":\"text\"},{\"_id\":\"CTIQTHIPYNGV5LGWU63REPDAHE\",\"additional_properties\":{\"comments\":[]},\"content\":\"Benivole coerceri donsifes\",\"type\":\"header\"},{\"_id\":\"OGOXMZ3T2NB6BEVNF2VPXRI2NE\",\"additional_properties\":{\"comments\":[]},\"content\":\"Dissident fáutrices firme hóneste libidinibus modératio morati mors saluto sévéra usus. Adversa cognitionem dicitur earumque, musicis nominis ornatus polyaeno, summumque tamquam variis. Conspiratione efflorescere elegantis medium, molestia nonné quos triarium ullo vigiliae vita.\",\"type\":\"text\"},{\"_id\":\"OZAYPLA5JFCFNFKCPKZLJOQ2GE\",\"subtype\":\"twitter\",\"type\":\"oembed_response\"},{\"_id\":\"I77Z64ZLKJAOLPZDTMF6JHQ5SY\",\"additional_properties\":{\"comments\":[]},\"content\":\"Aégritudo contereret diesque dulce eam, liberatione male putas sóciósqu. Confectum graeco patriam regione. Agam bene benivole cóerceri cónficiuntur curá defenditur dominationis sententiae stoici tenetur voluptatis. Amicum ánimus assentior circumcisaque ignoránt iste quicquid rebus triari.\",\"type\":\"text\"},{\"_id\":\"IHGAJZ3YUZDC5PWY3DYCFEFZCI\",\"additional_properties\":{\"comments\":[]},\"content_elements\":[{\"_id\":\"WBLMU57SU5F4PKVV3BZNNKZSUA\",\"additional_properties\":{\"comments\":[]},\"content\":\"Time spent with cats is never wasted. \",\"type\":\"text\"}],\"subtype\":\"pullquote\",\"type\":\"quote\"},{\"_id\":\"FGXFXPO42ZCRVHRRF3T5YQQENI\",\"additional_properties\":{\"comments\":[]},\"content\":\"Albucius class clita conspectum erudito impediri occultarum pleniorem quondam. Adhibenda careret cetero consistat cupidatat desiderent fortitudinem honéstatis inciderit quanti salutandi teneam. Eá epicureis huius optimi viris. Inprobitas iudicio justo loquuntur optime petentium plérisqué pondere suscipere. Brevi confirmaré cyrenaicos dápibus erant fecerit i indicavérunt nonumy plusque porttitor probet servire tranquillae.\",\"type\":\"text\"},{\"_id\":\"4J2FFQ23MJAH3C27Y6RZ4TK2MQ\",\"additional_properties\":{\"comments\":[]},\"content\":\"\\u003cdiv style\\u003d\\\"text-align:center;margin: 0 auto;padding: 25px; background: teal; color: white;\\\"\\u003e\\n    I am some raw html.\\n\\u003c/div\\u003e\",\"type\":\"raw_html\"},{\"_id\":\"OJXUZVPUQZAAVHMQWDLSZTCECI\",\"additional_properties\":{\"comments\":[]},\"content\":\"Beatus dominátionis errátá iudicant miserius negent praesertim sine turpis. Arcu consistat dicenda efficiatur, excepteur gubergren ignoraré laboribus négant refórmidans solet sublatum suscipit. Affirmatis arbitratu attulit class cógnitiónem, cónficiuntur nemini práetore qualisqué salutandi semel. Albuci cibo dicés habemus morborum. Affécti cuius disputata dolorum efficeretur, epicuri neglegentur quamque quapropter quodsi voce. Copiosae dediti doctiorés eademque gymnasia hábere legam malorum ócculta perturbári studiis tali tu. Argumentandum chrysippi democritum inflammat inpendente magnitudinem nacti pertinerent placatae porro repudiare scaevola tantam términatas vita.\",\"type\":\"text\"},{\"_id\":\"PYKACTM63FADDD5ST3H6FTKXXI\",\"type\":\"reference\"},{\"_id\":\"7LHUQWE7LVGX3MCVJG76YVCGFU\",\"additional_properties\":{\"comments\":[]},\"content\":\"Collegisti comparat cyrenaicisque dicás eventurum, individuá irure maioribus mors optimus, quosdám terrore. Contenta cu epicuro epicurus éxpéctat, inimicus málunt mediocris odit tali. Cadere dixeris igitur justo lectus, liberemus maluisti omnia perturbatur quia referuntur severitatem virtutibus. Adest aptius conficiuntqué expressas facillimis, legum nisi póetarum reperiri sagittis tuó. Adiit certámen delectari didicerimus expeteretur honestum humili legere moderatio pertinerent répériri vitam.\",\"type\":\"text\"},{\"_id\":\"5BNVGZNIKRBV7F7KC4I66627WA\",\"additional_properties\":{\"comments\":[]},\"content\":\"Doggo ipsum you are doin me a concern wow such tempt snoot big ol, heckin angery woofer wow such tempt clouds heckin, wow such tempt adorable doggo. Wow such tempt vvv doggorino yapper woofer, you are doin me a concern blop boof, heck wow very biscit bork. Vvv blop heckin good boys you are doing me a frighten shibe wow very biscit, shoober wow such tempt ruff floofs. Floofs waggy wags h*ck doggo puggorino pupper, woofer he made many woofs the neighborhood pupper. smol borking doggo with a long snoot for pats. The neighborhood pupper smol borking doggo with a long snoot for pats such treat extremely cuuuuuute the neighborhood pupper doggo wrinkler long bois, big ol pats fluffer boof porgo.\",\"type\":\"text\"},{\"_id\":\"WGY7KDSWDJF2TLRPWG2464AKF4\",\"additional_properties\":{\"comments\":[]},\"type\":\"list\"},{\"_id\":\"23R6XJRWNNAGPDTSTK54UPTNZ4\",\"additional_properties\":{\"comments\":[]},\"content\":\"Extremely cuuuuuute very hand that feed shibe woofer vvv, shoober. Wow such tempt most angery pupper I have ever seen you are doing me a frighten length boy borking doggo corgo long woofer the neighborhood pupper, smol long water shoob maximum borkdrive blep pats. Puggo shoob corgo blop doggorino, heck boofers. Mlem long doggo dat tungg tho borkf porgo borking doggo, wrinkler boof long woofer you are doing me the shock. You are doing me the shock long water shoob pupperino vvv, porgo shoob long bois, most angery pupper I have ever seen snoot. Puggo wrinkler extremely cuuuuuute doggorino, shoob noodle horse. Blop sub woofer you are doin me a concern puggorino doggorino, dat tungg tho boof. Shibe much ruin diet maximum borkdrive heckin good boys and girls many pats shibe shoober sub woofer, such treat very jealous pupper very good spot the neighborhood pupper heckin good boys the neighborhood pupper. Blep pupperino you are doing me a frighten shoober super chub, wow such tempt heckin good boys and girls. Very good spot porgo you are doing me a frighten extremely cuuuuuute, big ol pupper\",\"type\":\"text\"},{\"_id\":\"4ACL3OFVL5H5VCPDRT7IT6PZP4\",\"additional_properties\":{\"comments\":[]},\"type\":\"list\"},{\"_id\":\"K3KO7KD54BD5VERQTXKGO6Y6F4\",\"additional_properties\":{\"comments\":[]},\"content\":\"Sub woofer big ol maximum borkdrive aqua doggo mlem, maximum borkdrive I am bekom fat. long woofer what a nice floof. heck stop it fren. Corgo lotsa pats maximum borkdrive, blop. Lotsa pats maximum borkdrive blop fluffer shooberino, long doggo yapper he made many woofs long bois, very jealous pupper pats lotsa pats. Long water shoob I am bekom fat you are doing me a frighten heckin you are doin me a concern corgo borkdrive, clouds pupper heckin doge very hand that feed shibe. Big ol fluffer doing me a frighten most angery pupper I have ever seen thicc length boy dat tungg tho very jealous pupper porgo, floofs doggorino waggy wags most angery pupper I have ever seen ruff blop. You are doin me a concern the neighborhood pupper sub woofer very jealous pupper fluffer heck fat boi, wow very biscit heckin angery woofer borkf stop it fren yapper. Heck tungg very good spot porgo lotsa pats, long doggo pupper porgo.\",\"type\":\"text\"},{\"_id\":\"ATW7HPGNENCMZA277OTSUIMWAI\",\"additional_properties\":{\"comments\":[]},\"type\":\"table\"},{\"_id\":\"Y2NMVDMLAJBBZGOSPYMH7V34Z4\",\"additional_properties\":{\"comments\":[]},\"type\":\"list\"},{\"_id\":\"HEKD4R6EFRCV7JYKGZFNIG57T4\",\"additional_properties\":{\"comments\":[]},\"content\":\"\\u003cbr/\\u003e\",\"type\":\"text\"},{\"_id\":\"OOITKXLHK5H2BDLJOQYPJNGWKI\",\"additional_properties\":{\"comments\":[]},\"content\":\"\\u003cbr/\\u003e\",\"type\":\"text\"}],\"credits\":{\"by\":[{\"name\":\"Matt Nelson\",\"type\":\"author\"}]},\"headlines\":{\"basic\":\"Kitchen Sink Article 2: The Gazette\"},\"publish_date\":\"2021-08-13T15:38:49.268Z\",\"type\":\"story\",\"version\":\"0.10.7\",\"website_url\":\"/2019/03/06/kitchen-sink-article-2-the-gazette/\"}"
    private val storyJson2 =
        "{\"_id\":\"id\",\"content_elements\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"additional_properties\":{\"comments\":[]},\"content\":\"ánteponánt audiebamus porrecta reperiri usus. Certissimam commenticiam liberemus sed sermone. Cónscientia de difficilem exquisitis multa. Appetendum comparavérit definitionem iuberet, máximám médiocrém pertinacia quidam scaevola.\",\"type\":\"text\"},{\"_id\":\"4RKAFSAGANF3TD723V7DGHD6YM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Certáe divelli ferántur graecos, honoris imperdiet quaeritur quoniam reprehensione. Aptiór confidam córpórisque eram fames, fecisse ibidem reperiuntur tibi ullum. Admonere affluérét elitr magna. Aiunt aut democriti earum equidem, finibus illám interiret posse praetereat, quidam testibus.\",\"type\":\"text\"},{\"_id\":\"KP4E3KKS2BFKJNK4M75ZQZPY44\",\"additional_properties\":{\"comments\":[]},\"content\":\"Apeirian dare ei nihil percipit quibusdam repetitis solent vituperandae. Acuti aliquos bonorum eárumque, imitarentur libenter ménté quaestio, quoddám ratione tranquillat. Declinatio dicitis invidus legum opés, praésént pugnáre scelerisque utuntur. Arbitramur controversia esse fecerint, optari permagna temeritas. Augeri detractis duo intus labórum, máiorá óptimus.\",\"type\":\"text\"},{\"_id\":\"4O5XWH6Y7BEUVKJPZSKJDZ6CWM\",\"additional_properties\":{\"comments\":[]},\"content_elements\":[{\"_id\":\"RBFL3XWDMBFP3CF5NNI23YSXJU\",\"additional_properties\":{\"comments\":[]},\"content\":\"The lazy fox jumped over the brown dog.\",\"type\":\"text\"}],\"subtype\":\"blockquote\",\"type\":\"quote\"},{\"_id\":\"Y76ZEHFBJVATTG6XJ22MEATE5E\",\"additional_properties\":{\"comments\":[]},\"content\":\"Aliqua artis civibus erróribus expeteremus explicatam fábulis gravissimis hendrerit levius libido maluisset tuum. Chrysippo consistát constituámus epicureis, inesse iudicatum parendum quem ullamco. Diceret discidia illustriora improborum inquam insidiarum insipientiam interrogare quamquam repetitis senserit temperantia tráctátos vetuit. Aspernatur fana honestum iudicem, mei quicquam.\",\"type\":\"text\"},{\"_id\":\"BUPS2MFH2FG7NNYKR24UQ267B4\",\"additional_properties\":{\"comments\":[]},\"content\":\"Aliquid ceterórum chrysippe explicabo maximeque nonne parta quoddam recta. Dicitur industriae iusteque liberae operosam, persecuti tólerabiles vos. Accedere contemnit deseruisse dómus mundus, náturá sequitur summam. Futurové iucunditaté noluisse quis. ánteponánt erigimur faciendi inermis malivoli, molestiae mollitia patiatur scipio vidisse. Aristótelem dicenda effluere intellegámus neque scribéntur sentiamus tradere.\",\"type\":\"text\"},{\"_id\":\"Q5XSDPJWSFDIHIN4QWJTQWLE2M\",\"additional_properties\":{\"comments\":[],\"galleries\":[],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"4390327218_334de2dce7_o.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"owner\":\"robin.giannattasio@washpost.com\",\"proxyUrl\":\"/resizer/r1VGI4K5B5nJNipJyzHF71I9dO0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/r1VGI4K5B5nJNipJyzHF71I9dO0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"thumbnailResizeUrl\":\"/resizer/OaQLWMRK0YqYdVwAenZpc_c7MO0\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"version\":0},\"address\":{},\"created_date\":\"2019-02-21T20:10:51Z\",\"credits\":{},\"height\":2328,\"last_updated_date\":\"2019-02-21T20:10:51Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Poe in the Snow\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/r1VGI4K5B5nJNipJyzHF71I9dO0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"version\":\"0.10.3\",\"width\":3612},{\"_id\":\"JHLMYCTDTNGNVCRNKEQVKXIIAE\",\"additional_properties\":{\"comments\":[]},\"content\":\"Consequentis contineri dedocendi depulsa, docui eventurum illa iucunditas, iucundo licet montes num privámur reperiri sequi. Convéniré ero explicatis incidant instituit liquidae óptimus sentiunt spernat telos. Convallis impetus liberabuntur quaeso tueri. Comprobavit fabellas philosopho sapienti. Alienum defatigatio exhorrescere futuros gymnásiá, privatió stultorum torquatum.\",\"type\":\"text\"},{\"_id\":\"CYPEAYOB2JAHVHZBEMNC2HQQXM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Coerceri displicet fore urna. éxquisitis ferri navigandi officia, totam videntur. ácutum adquiescere declinátio dicas doming, error graecam meque motu.\",\"type\":\"text\"},{\"_id\":\"POQ5CYZ4VBE6ZG473UBZKWSRYM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Genuit lineam lucilius pacem, porta provocarem quamvis. Libidinum pertinaces scelerisque triarium. Discordia existimant filio illó, magnus numerándá quondám reprimique, splendore sua. Albucius collegi electis événiunt, éxcruciant expectátá hórtensió insidiarum, omnino operam ópinemur primó uberiora. Didicisse diligamus ii plusque ponatur timentis. Ait a\",\"type\":\"text\"},{\"_id\":\"YDHIVLXHS5HXLBZHU364O454ZI\",\"additional_properties\":{\"comments\":[]},\"content\":\"Lorem ipsum Dolor\",\"type\":\"header\"},{\"_id\":\"WNHHI5L4VBB77P6FN6A3VE6WOQ\",\"additional_properties\":{\"comments\":[]},\"content\":\"tomi discidia firmam fruentem, héndrérit multos régula statim.\",\"type\":\"text\"},{\"_id\":\"U4DLNKJ3SBBAPNSHPHJ4UBX67M\",\"additional_properties\":{\"comments\":[]},\"content\":\"Adhuc ámici ángore fugit graecis istae manilium minime parum reprehensiónes respondendum retinent turpis vidérér. ártes comit dolorum intervenire iustius nivém novi potione quidém reiciendis responsum satisfacit. Cognitione collégi défuturum dicemus eruditi illustrióra intellegaturque intellegimus ista maledici praeter religionis tractavissent. Aliquó allicit cónducunt cónsequuntur dediti fabulis iustitia iusto late periculis praéséntium scriptum suas sumus videntur. Attulit depravate magnum male referuntur, répériétur vitium. Est foré locatus oratoribus quadam quoqué sit velim vivéndo.\",\"type\":\"text\"},{\"_id\":\"G5WKDE5NEVBQ5AAMOPSWWJFFPA\",\"additional_properties\":{\"comments\":[],\"galleries\":[],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_6132.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"owner\":\"wyatt.johnston@washpost.com\",\"proxyUrl\":\"/resizer/FSqRLYAb2Uj_Q0s7Lfe-2vB5XJI\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/FSqRLYAb2Uj_Q0s7Lfe-2vB5XJI\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"thumbnailResizeUrl\":\"/resizer/P3NAaW-gRaAkJRV4HvvA9GJqiK0\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"version\":0},\"address\":{},\"created_date\":\"2019-02-21T21:55:52Z\",\"credits\":{},\"height\":1386,\"last_updated_date\":\"2019-02-21T21:55:52Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Sleepy Merlin\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/FSqRLYAb2Uj_Q0s7Lfe-2vB5XJI\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"version\":\"0.10.3\",\"width\":1040},{\"_id\":\"K7KTX2LXTFA7RIQOHHEZM4EZRM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Aristoteli cyrenaicisque delectatum facer, maledici materia mauris molestie notae pórrecta putamus sóluta tibi vehicula. Amicitiam consetetur contentus cyrenaicos eláboráre éxcépturi industria iudicant liberalitati obcaecati personae potest praeterita sinit. Civibus contra efficitur eirmod, facérém ista iustó liberos, nostrum quarum. Dóctióres mundi quibus tránsferrem. Altéra conducunt dico dissensió dissentiet, domesticarum habitassé inesse legum próbantur, suspicor talem tempóribus vestrae. Adiit fabulas fictaé incursióne infánti, legendam meque praeclarorum reiciendis rem sentiri stabilém venandi véstibulum video.\",\"type\":\"text\"},{\"_id\":\"X4TPTSYPBNHLFDS4Z7B72QDTP4\",\"additional_properties\":{\"comments\":[]},\"content\":\"I can haz cheeseburger\",\"type\":\"interstitial_link\",\"url\":\"https://icanhas.cheezburger.com/\"},{\"_id\":\"BSC7BCWPRNF2HIMZHQMD6N4VKM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Autém bibendum deorsus dialéctica haé, ipsi ómnia peccandi pervenias possum quámvis soliditátem témporé. Caeco collegi infinito instituendarum mollit paratus perpauca pleniórem praeclara quaerenda quálisque torquatum utraque veritatis. Assecutus compositis desistunt effluere expetitur faciunt intus iracundia libidinosarum sálutátus stabiliqué. Aequi carum comprehenderit delectat disputando, genuit pronuntiaret. ádest amori effluere locum novi, sapien traditur vituperari. Continent emolumento inanes possunt. Adhibuit innumerabiles perpetuam philosopho solitudo utrum.\",\"type\":\"text\"},{\"_id\":\"MZK7XM3IEFG5FEZSLA6TZ3PSZM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Cupio desperantes gloriosis latinis phasellus. Epicurus equos foris iucunda legantur perciperet. Acri alienus amicitiae fuscé gratia, honestum liberae póssum posuere quibus quocircá sophocles vivere. Conciliant delectet invidus nostram recta sero solitudo.\",\"type\":\"text\"},{\"_id\":\"33HATHXZGBDQDHWXYBHEVZUMGU\",\"additional_properties\":{\"comments\":[]},\"content\":\"Distinguique exercitationem fastidii finitas quoddam repugnantibus. Arridéns attento nimium óblivióne pártá, pérspécta quoquo. Declinatio faucibus leo ómittendis torquentur, utuntur vacuitate vituperátum. Conclusionemque dissentiás ferant iustioribus, multa partes putámus. Aequi cómmódó fit honestatis manum. Adhibenda eám efficiát éruditioném iucunditatem, mel negent parvos physicis platone respirare sedatio s\",\"type\":\"text\"},{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"additional_properties\":{\"comments\":[],\"has_published_copy\":true,\"owner\":\"matthew.nelson@washpost.com\",\"published\":true,\"roles\":[],\"version\":4},\"canonical_url\":\"/2021/08/16/cats-of-arc/\",\"content_elements\":[{\"_id\":\"BNPYOMX7LBCT3FXRYQGRWBBNAM\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_5752.JPG\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"published\":true,\"resizeUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"thumbnailResizeUrl\":\"/resizer/Z_PxsFTrQjjQYOmtz8nEg1Gt8r4\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:51:24Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":2123,\"last_updated_date\":\"2019-02-21T16:51:26Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"michelle cat\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":\"0.10.3\",\"width\":2438},{\"_id\":\"TONB3GC7SBAQDGACCGA5UPF5UA\",\"additional_properties\":{\"galleries\":[],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_2775.JPG\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/Tc6HUQgIzIa9_YHTb4mudOjw2H0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"published\":true,\"resizeUrl\":\"/resizer/Tc6HUQgIzIa9_YHTb4mudOjw2H0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"thumbnailResizeUrl\":\"/resizer/vsJZ9jjBKXS9sX-43csyKDJp81k\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"version\":0},\"address\":{},\"credits\":{},\"height\":4032,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"monte\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/Tc6HUQgIzIa9_YHTb4mudOjw2H0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"version\":\"0.10.3\",\"width\":3024},{\"_id\":\"5OPJFEJNWBGUTMA3R3Y6JAVW5I\",\"additional_properties\":{\"galleries\":[],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"Image from iOS (1).jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/IWiMhBwHYFP3m4iVDRAW9gP6QYg\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/IWiMhBwHYFP3m4iVDRAW9gP6QYg\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"thumbnailResizeUrl\":\"/resizer/YsGB2Y9sxAsMHulL8sAShSnoat4\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"version\":0},\"address\":{},\"credits\":{},\"height\":4032,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Cat\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/IWiMhBwHYFP3m4iVDRAW9gP6QYg\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"version\":\"0.10.3\",\"width\":3024},{\"_id\":\"C5XUI2AH2VDLDBKJRFNB73XYWM\",\"additional_properties\":{\"galleries\":[],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"V__30CF.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/GcRh0kB9tA6szaWFMtzNSB9cbPY\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/GcRh0kB9tA6szaWFMtzNSB9cbPY\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"thumbnailResizeUrl\":\"/resizer/5PrVpqMPBCKnRZ0kjr2t8Dy2zRU\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"version\":0},\"address\":{},\"credits\":{\"by\":[{\"name\":\"Picasa\",\"type\":\"author\"}]},\"height\":1600,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Turtle kitty\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/GcRh0kB9tA6szaWFMtzNSB9cbPY\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"version\":\"0.10.3\",\"width\":1195},{\"_id\":\"I5VLXXVEAFATRPO4PQ33WT6LAM\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"image_from_ios (1).jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"owner\":\"william.cook@washpost.com\",\"proxyUrl\":\"/resizer/g7-tSY8LEWts3Ixa_ulb8qBLxgk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/g7-tSY8LEWts3Ixa_ulb8qBLxgk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"thumbnailResizeUrl\":\"/resizer/1ZuGZuUcS8Tczr4Dxog3eIcNgUo\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:11:26Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":842,\"last_updated_date\":\"2019-02-21T16:11:32Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Miki Naps in Box\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/g7-tSY8LEWts3Ixa_ulb8qBLxgk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"version\":\"0.10.3\",\"width\":1122},{\"_id\":\"YX3I23GKNZD6RLN52XJ2PKF7LU\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[\"\",\"cat\",\"nap\",\"oreo\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"image_from_ios.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"owner\":\"william.cook@washpost.com\",\"proxyUrl\":\"/resizer/TCgU6F7dkvA_Ujg-HXhmMnNlieU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/TCgU6F7dkvA_Ujg-HXhmMnNlieU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"thumbnailResizeUrl\":\"/resizer/HT6WwVB2t1F_rbFg6PiQNtYAIS4\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:12:27Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":2268,\"last_updated_date\":\"2019-02-21T16:12:29Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Oreo Naptime\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/TCgU6F7dkvA_Ujg-HXhmMnNlieU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"version\":\"0.10.3\",\"width\":3024},{\"_id\":\"JTRWKF4N5BH3ZLMIIQ7USR4YFU\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_5590.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"owner\":\"melissa.depuydt@washpost.com\",\"proxyUrl\":\"/resizer/S-gcvu7Op2hVdOHJeTrFrHihjOU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/S-gcvu7Op2hVdOHJeTrFrHihjOU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"thumbnailResizeUrl\":\"/resizer/be0FWBcr0Op25mqvJx6pyLMq0LM\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:08:51Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":1651,\"last_updated_date\":\"2019-02-21T16:08:57Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Albus\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/S-gcvu7Op2hVdOHJeTrFrHihjOU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"version\":\"0.10.3\",\"width\":2201},{\"_id\":\"RIINOEOPJNB53KFHGEVZOYGHBQ\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_6959.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"owner\":\"melissa.depuydt@washpost.com\",\"proxyUrl\":\"/resizer/7s5HZuPDaSPriMsZGiKNkBqv93U\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/7s5HZuPDaSPriMsZGiKNkBqv93U\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"thumbnailResizeUrl\":\"/resizer/ThWn4tXKvbW6Sr-ferBkBhAD-EE\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:06:43Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":2268,\"last_updated_date\":\"2019-02-21T16:06:50Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"he fits, he sits\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/7s5HZuPDaSPriMsZGiKNkBqv93U\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"version\":\"0.10.3\",\"width\":3024},{\"_id\":\"PXSGHHECONEXHODQ6NVDZXVPZM\",\"additional_properties\":{\"galleries\":[],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_20171021_132711.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"owner\":\"susan.tyler@washpost.com\",\"proxyUrl\":\"/resizer/uyxUae4e-3YfUPDA0PBeN4ewhzk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/uyxUae4e-3YfUPDA0PBeN4ewhzk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"thumbnailResizeUrl\":\"/resizer/KjsHvYmMU4YBqp8ZFEDIO93dt8U\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"version\":0},\"address\":{},\"credits\":{},\"height\":3024,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Thriller\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/uyxUae4e-3YfUPDA0PBeN4ewhzk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"version\":\"0.10.3\",\"width\":4032},{\"_id\":\"55VYTIER35C5DIPJ5GYUR6KROE\",\"additional_properties\":{\"galleries\":[],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"the gambler.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"owner\":\"eric.carlisle@washpost.com\",\"proxyUrl\":\"/resizer/Fdw_t2G3kSEjvym1y4GPE3amFck\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/Fdw_t2G3kSEjvym1y4GPE3amFck\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"thumbnailResizeUrl\":\"/resizer/RQQhSg2Utzw6aiCjYfPzw6uHS3w\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"version\":0},\"address\":{},\"credits\":{},\"height\":898,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"The Gambler\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/Fdw_t2G3kSEjvym1y4GPE3amFck\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"version\":\"0.10.3\",\"width\":1198},{\"_id\":\"43Z756DP3RCCPHPYEERIGOXFFQ\",\"additional_properties\":{\"galleries\":[],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"secretgarden.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"owner\":\"joseph.gilbert@washpost.com\",\"proxyUrl\":\"/resizer/J0C0ik_P50egpt45xMKOi0SEG-g\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/J0C0ik_P50egpt45xMKOi0SEG-g\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"thumbnailResizeUrl\":\"/resizer/Or14R0bFgX2fOwlm2Oot_umjsEs\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"version\":0},\"address\":{},\"credits\":{},\"height\":1202,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Wildcat spotted!\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/J0C0ik_P50egpt45xMKOi0SEG-g\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"version\":\"0.10.3\",\"width\":675}],\"created_date\":\"2019-02-21T16:52:57Z\",\"credits\":{\"by\":[]},\"description\":{\"basic\":\"This is a gallery of Arc Cats.\"},\"display_date\":\"2019-02-21T16:52:57Z\",\"first_publish_date\":\"2019-02-21T16:52:57Z\",\"headlines\":{\"basic\":\"Cats of Arc\"},\"last_updated_date\":\"2021-08-16T17:24:59Z\",\"owner\":{\"id\":\"sandbox.corecomponents\"},\"promo_items\":{\"basic\":{\"_id\":\"BNPYOMX7LBCT3FXRYQGRWBBNAM\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_5752.JPG\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"published\":true,\"resizeUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"thumbnailResizeUrl\":\"/resizer/Z_PxsFTrQjjQYOmtz8nEg1Gt8r4\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:51:24Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":2123,\"last_updated_date\":\"2019-02-21T16:51:26Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"michelle cat\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":\"0.10.3\",\"width\":2438}},\"publish_date\":\"2021-08-13T15:38:49.268Z\",\"taxonomy\":{},\"type\":\"gallery\",\"version\":\"0.10.3\"},{\"_id\":\"2QT3ZMWN4ZADRCDYJ3XJXPTTUA\",\"additional_properties\":{\"comments\":[]},\"content\":\"uavitate tránsferám. Adamare consequi duxit finis, iisque intelleges summum.\",\"type\":\"text\"},{\"_id\":\"NJ4YQBFHXJAF5P7AXHAIXPBNAE\",\"additional_properties\":{\"comments\":[]},\"content\":\"Errore explicátis nonne pueri tárentinis. Curiosi déducérét gymnasia sollicitudin, soluta sunt. Culpá divitiarum dixit erunt ignavia, irridente miseram perfecto praécéptricé reque tincidunt vacuitate. Cógnósci cónfórmavit fugiendum omnino pellentesque. Béstiaé conversa fortasse gravitér hausta humili odio pérvénias stabilem suscipiantur.\",\"type\":\"text\"},{\"_id\":\"O5ICDWUHIFGMFCUCUUUGQ34KXQ\",\"subtype\":\"instagram\",\"type\":\"oembed_response\"},{\"_id\":\"5LJTL7KP7REQPIVF7BKEYHO2KY\",\"additional_properties\":{\"comments\":[]},\"content\":\"Detracto existimavit iriure posuere pugnare, putamus sapientium summum suum. Curabitur désidératuram dicenda dico, epicurus hostis intervenire mihi, omnesque práeclárorum praétéréa sévéra unum. Assentiar compréhénsam debilitati regione successerit, tempore tractavissent. Accusantibus conscientiam declinabunt dicas dixeris epicurei érat filio sinit studia suscipere. Defuturum exeduntur quia réquiréré. Aiunt dicit erudito éxpréssas graecis, imperitorum iracundia lóquerer partes praestabiliórem salutatus triárium.\",\"type\":\"text\"},{\"_id\":\"YYYCTF2AWVHLRJ7IZWIFH3FUPA\",\"additional_properties\":{\"comments\":[]},\"content\":\"benivole cóerceri cónficiuntur curá de\",\"type\":\"text\"},{\"_id\":\"CTIQTHIPYNGV5LGWU63REPDAHE\",\"additional_properties\":{\"comments\":[]},\"content\":\"Benivole coerceri donsifes\",\"type\":\"header\"},{\"_id\":\"OGOXMZ3T2NB6BEVNF2VPXRI2NE\",\"additional_properties\":{\"comments\":[]},\"content\":\"Dissident fáutrices firme hóneste libidinibus modératio morati mors saluto sévéra usus. Adversa cognitionem dicitur earumque, musicis nominis ornatus polyaeno, summumque tamquam variis. Conspiratione efflorescere elegantis medium, molestia nonné quos triarium ullo vigiliae vita.\",\"type\":\"text\"},{\"_id\":\"OZAYPLA5JFCFNFKCPKZLJOQ2GE\",\"subtype\":\"twitter\",\"type\":\"oembed_response\"},{\"_id\":\"I77Z64ZLKJAOLPZDTMF6JHQ5SY\",\"additional_properties\":{\"comments\":[]},\"content\":\"Aégritudo contereret diesque dulce eam, liberatione male putas sóciósqu. Confectum graeco patriam regione. Agam bene benivole cóerceri cónficiuntur curá defenditur dominationis sententiae stoici tenetur voluptatis. Amicum ánimus assentior circumcisaque ignoránt iste quicquid rebus triari.\",\"type\":\"text\"},{\"_id\":\"IHGAJZ3YUZDC5PWY3DYCFEFZCI\",\"additional_properties\":{\"comments\":[]},\"content_elements\":[{\"_id\":\"WBLMU57SU5F4PKVV3BZNNKZSUA\",\"additional_properties\":{\"comments\":[]},\"content\":\"Time spent with cats is never wasted. \",\"type\":\"text\"}],\"subtype\":\"pullquote\",\"type\":\"quote\"},{\"_id\":\"FGXFXPO42ZCRVHRRF3T5YQQENI\",\"additional_properties\":{\"comments\":[]},\"content\":\"Albucius class clita conspectum erudito impediri occultarum pleniorem quondam. Adhibenda careret cetero consistat cupidatat desiderent fortitudinem honéstatis inciderit quanti salutandi teneam. Eá epicureis huius optimi viris. Inprobitas iudicio justo loquuntur optime petentium plérisqué pondere suscipere. Brevi confirmaré cyrenaicos dápibus erant fecerit i indicavérunt nonumy plusque porttitor probet servire tranquillae.\",\"type\":\"text\"},{\"_id\":\"4J2FFQ23MJAH3C27Y6RZ4TK2MQ\",\"additional_properties\":{\"comments\":[]},\"content\":\"\\u003cdiv style\\u003d\\\"text-align:center;margin: 0 auto;padding: 25px; background: teal; color: white;\\\"\\u003e\\n    I am some raw html.\\n\\u003c/div\\u003e\",\"type\":\"raw_html\"},{\"_id\":\"OJXUZVPUQZAAVHMQWDLSZTCECI\",\"additional_properties\":{\"comments\":[]},\"content\":\"Beatus dominátionis errátá iudicant miserius negent praesertim sine turpis. Arcu consistat dicenda efficiatur, excepteur gubergren ignoraré laboribus négant refórmidans solet sublatum suscipit. Affirmatis arbitratu attulit class cógnitiónem, cónficiuntur nemini práetore qualisqué salutandi semel. Albuci cibo dicés habemus morborum. Affécti cuius disputata dolorum efficeretur, epicuri neglegentur quamque quapropter quodsi voce. Copiosae dediti doctiorés eademque gymnasia hábere legam malorum ócculta perturbári studiis tali tu. Argumentandum chrysippi democritum inflammat inpendente magnitudinem nacti pertinerent placatae porro repudiare scaevola tantam términatas vita.\",\"type\":\"text\"},{\"_id\":\"PYKACTM63FADDD5ST3H6FTKXXI\",\"type\":\"reference\"},{\"_id\":\"7LHUQWE7LVGX3MCVJG76YVCGFU\",\"additional_properties\":{\"comments\":[]},\"content\":\"Collegisti comparat cyrenaicisque dicás eventurum, individuá irure maioribus mors optimus, quosdám terrore. Contenta cu epicuro epicurus éxpéctat, inimicus málunt mediocris odit tali. Cadere dixeris igitur justo lectus, liberemus maluisti omnia perturbatur quia referuntur severitatem virtutibus. Adest aptius conficiuntqué expressas facillimis, legum nisi póetarum reperiri sagittis tuó. Adiit certámen delectari didicerimus expeteretur honestum humili legere moderatio pertinerent répériri vitam.\",\"type\":\"text\"},{\"_id\":\"5BNVGZNIKRBV7F7KC4I66627WA\",\"additional_properties\":{\"comments\":[]},\"content\":\"Doggo ipsum you are doin me a concern wow such tempt snoot big ol, heckin angery woofer wow such tempt clouds heckin, wow such tempt adorable doggo. Wow such tempt vvv doggorino yapper woofer, you are doin me a concern blop boof, heck wow very biscit bork. Vvv blop heckin good boys you are doing me a frighten shibe wow very biscit, shoober wow such tempt ruff floofs. Floofs waggy wags h*ck doggo puggorino pupper, woofer he made many woofs the neighborhood pupper. smol borking doggo with a long snoot for pats. The neighborhood pupper smol borking doggo with a long snoot for pats such treat extremely cuuuuuute the neighborhood pupper doggo wrinkler long bois, big ol pats fluffer boof porgo.\",\"type\":\"text\"},{\"_id\":\"WGY7KDSWDJF2TLRPWG2464AKF4\",\"additional_properties\":{\"comments\":[]},\"type\":\"list\"},{\"_id\":\"23R6XJRWNNAGPDTSTK54UPTNZ4\",\"additional_properties\":{\"comments\":[]},\"content\":\"Extremely cuuuuuute very hand that feed shibe woofer vvv, shoober. Wow such tempt most angery pupper I have ever seen you are doing me a frighten length boy borking doggo corgo long woofer the neighborhood pupper, smol long water shoob maximum borkdrive blep pats. Puggo shoob corgo blop doggorino, heck boofers. Mlem long doggo dat tungg tho borkf porgo borking doggo, wrinkler boof long woofer you are doing me the shock. You are doing me the shock long water shoob pupperino vvv, porgo shoob long bois, most angery pupper I have ever seen snoot. Puggo wrinkler extremely cuuuuuute doggorino, shoob noodle horse. Blop sub woofer you are doin me a concern puggorino doggorino, dat tungg tho boof. Shibe much ruin diet maximum borkdrive heckin good boys and girls many pats shibe shoober sub woofer, such treat very jealous pupper very good spot the neighborhood pupper heckin good boys the neighborhood pupper. Blep pupperino you are doing me a frighten shoober super chub, wow such tempt heckin good boys and girls. Very good spot porgo you are doing me a frighten extremely cuuuuuute, big ol pupper\",\"type\":\"text\"},{\"_id\":\"4ACL3OFVL5H5VCPDRT7IT6PZP4\",\"additional_properties\":{\"comments\":[]},\"type\":\"list\"},{\"_id\":\"K3KO7KD54BD5VERQTXKGO6Y6F4\",\"additional_properties\":{\"comments\":[]},\"content\":\"Sub woofer big ol maximum borkdrive aqua doggo mlem, maximum borkdrive I am bekom fat. long woofer what a nice floof. heck stop it fren. Corgo lotsa pats maximum borkdrive, blop. Lotsa pats maximum borkdrive blop fluffer shooberino, long doggo yapper he made many woofs long bois, very jealous pupper pats lotsa pats. Long water shoob I am bekom fat you are doing me a frighten heckin you are doin me a concern corgo borkdrive, clouds pupper heckin doge very hand that feed shibe. Big ol fluffer doing me a frighten most angery pupper I have ever seen thicc length boy dat tungg tho very jealous pupper porgo, floofs doggorino waggy wags most angery pupper I have ever seen ruff blop. You are doin me a concern the neighborhood pupper sub woofer very jealous pupper fluffer heck fat boi, wow very biscit heckin angery woofer borkf stop it fren yapper. Heck tungg very good spot porgo lotsa pats, long doggo pupper porgo.\",\"type\":\"text\"},{\"_id\":\"ATW7HPGNENCMZA277OTSUIMWAI\",\"additional_properties\":{\"comments\":[]},\"type\":\"table\"},{\"_id\":\"Y2NMVDMLAJBBZGOSPYMH7V34Z4\",\"additional_properties\":{\"comments\":[]},\"type\":\"list\"},{\"_id\":\"HEKD4R6EFRCV7JYKGZFNIG57T4\",\"additional_properties\":{\"comments\":[]},\"content\":\"\\u003cbr/\\u003e\",\"type\":\"text\"},{\"_id\":\"OOITKXLHK5H2BDLJOQYPJNGWKI\",\"additional_properties\":{\"comments\":[]},\"content\":\"\\u003cbr/\\u003e\",\"type\":\"text\"}],\"credits\":{\"by\":[{\"name\":\"Matt Nelson\",\"type\":\"author\"}]},\"headlines\":{\"basic\":\"Kitchen Sink Article 2: The Gazette\"},\"publish_date\":\"2021-08-13T15:38:49.268Z\",\"type\":\"story\",\"version\":\"0.10.7\",\"website_url\":\"/2019/03/06/kitchen-sink-article-2-the-gazette/\"}"
    private val storyJson =
        "{\"_id\":\"id\",\"content_elements\":[{\"_id\":\"R56P5STCTZB5XEUKISHI4PI2LI\",\"additional_properties\":{\"comments\":[]},\"content\":\"ánteponánt audiebamus porrecta reperiri usus. Certissimam commenticiam liberemus sed sermone. Cónscientia de difficilem exquisitis multa. Appetendum comparavérit definitionem iuberet, máximám médiocrém pertinacia quidam scaevola.\",\"type\":\"text\"},{\"_id\":\"4RKAFSAGANF3TD723V7DGHD6YM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Certáe divelli ferántur graecos, honoris imperdiet quaeritur quoniam reprehensione. Aptiór confidam córpórisque eram fames, fecisse ibidem reperiuntur tibi ullum. Admonere affluérét elitr magna. Aiunt aut democriti earum equidem, finibus illám interiret posse praetereat, quidam testibus.\",\"type\":\"text\"},{\"_id\":\"KP4E3KKS2BFKJNK4M75ZQZPY44\",\"additional_properties\":{\"comments\":[]},\"content\":\"Apeirian dare ei nihil percipit quibusdam repetitis solent vituperandae. Acuti aliquos bonorum eárumque, imitarentur libenter ménté quaestio, quoddám ratione tranquillat. Declinatio dicitis invidus legum opés, praésént pugnáre scelerisque utuntur. Arbitramur controversia esse fecerint, optari permagna temeritas. Augeri detractis duo intus labórum, máiorá óptimus.\",\"type\":\"text\"},{\"_id\":\"4O5XWH6Y7BEUVKJPZSKJDZ6CWM\",\"additional_properties\":{\"comments\":[]},\"content_elements\":[{\"_id\":\"RBFL3XWDMBFP3CF5NNI23YSXJU\",\"additional_properties\":{\"comments\":[]},\"content\":\"The lazy fox jumped over the brown dog.\",\"type\":\"text\"}],\"subtype\":\"blockquote\",\"type\":\"quote\"},{\"_id\":\"Y76ZEHFBJVATTG6XJ22MEATE5E\",\"additional_properties\":{\"comments\":[]},\"content\":\"Aliqua artis civibus erróribus expeteremus explicatam fábulis gravissimis hendrerit levius libido maluisset tuum. Chrysippo consistát constituámus epicureis, inesse iudicatum parendum quem ullamco. Diceret discidia illustriora improborum inquam insidiarum insipientiam interrogare quamquam repetitis senserit temperantia tráctátos vetuit. Aspernatur fana honestum iudicem, mei quicquam.\",\"type\":\"text\"},{\"_id\":\"BUPS2MFH2FG7NNYKR24UQ267B4\",\"additional_properties\":{\"comments\":[]},\"content\":\"Aliquid ceterórum chrysippe explicabo maximeque nonne parta quoddam recta. Dicitur industriae iusteque liberae operosam, persecuti tólerabiles vos. Accedere contemnit deseruisse dómus mundus, náturá sequitur summam. Futurové iucunditaté noluisse quis. ánteponánt erigimur faciendi inermis malivoli, molestiae mollitia patiatur scipio vidisse. Aristótelem dicenda effluere intellegámus neque scribéntur sentiamus tradere.\",\"type\":\"text\"},{\"_id\":\"Q5XSDPJWSFDIHIN4QWJTQWLE2M\",\"additional_properties\":{\"comments\":[],\"galleries\":[],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"4390327218_334de2dce7_o.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"owner\":\"robin.giannattasio@washpost.com\",\"proxyUrl\":\"/resizer/r1VGI4K5B5nJNipJyzHF71I9dO0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/r1VGI4K5B5nJNipJyzHF71I9dO0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"thumbnailResizeUrl\":\"/resizer/OaQLWMRK0YqYdVwAenZpc_c7MO0\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"version\":0},\"address\":{},\"created_date\":\"2019-02-21T20:10:51Z\",\"credits\":{},\"height\":2328,\"last_updated_date\":\"2019-02-21T20:10:51Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Poe in the Snow\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/r1VGI4K5B5nJNipJyzHF71I9dO0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/Q5XSDPJWSFDIHIN4QWJTQWLE2M.jpg\",\"version\":\"0.10.3\",\"width\":3612},{\"_id\":\"JHLMYCTDTNGNVCRNKEQVKXIIAE\",\"additional_properties\":{\"comments\":[]},\"content\":\"Consequentis contineri dedocendi depulsa, docui eventurum illa iucunditas, iucundo licet montes num privámur reperiri sequi. Convéniré ero explicatis incidant instituit liquidae óptimus sentiunt spernat telos. Convallis impetus liberabuntur quaeso tueri. Comprobavit fabellas philosopho sapienti. Alienum defatigatio exhorrescere futuros gymnásiá, privatió stultorum torquatum.\",\"type\":\"text\"},{\"_id\":\"CYPEAYOB2JAHVHZBEMNC2HQQXM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Coerceri displicet fore urna. éxquisitis ferri navigandi officia, totam videntur. ácutum adquiescere declinátio dicas doming, error graecam meque motu.\",\"type\":\"text\"},{\"_id\":\"POQ5CYZ4VBE6ZG473UBZKWSRYM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Genuit lineam lucilius pacem, porta provocarem quamvis. Libidinum pertinaces scelerisque triarium. Discordia existimant filio illó, magnus numerándá quondám reprimique, splendore sua. Albucius collegi electis événiunt, éxcruciant expectátá hórtensió insidiarum, omnino operam ópinemur primó uberiora. Didicisse diligamus ii plusque ponatur timentis. Ait a\",\"type\":\"text\"},{\"_id\":\"YDHIVLXHS5HXLBZHU364O454ZI\",\"additional_properties\":{\"comments\":[]},\"content\":\"Lorem ipsum Dolor\",\"type\":\"header\"},{\"_id\":\"WNHHI5L4VBB77P6FN6A3VE6WOQ\",\"additional_properties\":{\"comments\":[]},\"content\":\"tomi discidia firmam fruentem, héndrérit multos régula statim.\",\"type\":\"text\"},{\"_id\":\"U4DLNKJ3SBBAPNSHPHJ4UBX67M\",\"additional_properties\":{\"comments\":[]},\"content\":\"Adhuc ámici ángore fugit graecis istae manilium minime parum reprehensiónes respondendum retinent turpis vidérér. ártes comit dolorum intervenire iustius nivém novi potione quidém reiciendis responsum satisfacit. Cognitione collégi défuturum dicemus eruditi illustrióra intellegaturque intellegimus ista maledici praeter religionis tractavissent. Aliquó allicit cónducunt cónsequuntur dediti fabulis iustitia iusto late periculis praéséntium scriptum suas sumus videntur. Attulit depravate magnum male referuntur, répériétur vitium. Est foré locatus oratoribus quadam quoqué sit velim vivéndo.\",\"type\":\"text\"},{\"_id\":\"G5WKDE5NEVBQ5AAMOPSWWJFFPA\",\"additional_properties\":{\"comments\":[],\"galleries\":[],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_6132.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"owner\":\"wyatt.johnston@washpost.com\",\"proxyUrl\":\"/resizer/FSqRLYAb2Uj_Q0s7Lfe-2vB5XJI\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/FSqRLYAb2Uj_Q0s7Lfe-2vB5XJI\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"thumbnailResizeUrl\":\"/resizer/P3NAaW-gRaAkJRV4HvvA9GJqiK0\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"version\":0},\"address\":{},\"created_date\":\"2019-02-21T21:55:52Z\",\"credits\":{},\"height\":1386,\"last_updated_date\":\"2019-02-21T21:55:52Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Sleepy Merlin\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/FSqRLYAb2Uj_Q0s7Lfe-2vB5XJI\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/G5WKDE5NEVBQ5AAMOPSWWJFFPA.jpg\",\"version\":\"0.10.3\",\"width\":1040},{\"_id\":\"K7KTX2LXTFA7RIQOHHEZM4EZRM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Aristoteli cyrenaicisque delectatum facer, maledici materia mauris molestie notae pórrecta putamus sóluta tibi vehicula. Amicitiam consetetur contentus cyrenaicos eláboráre éxcépturi industria iudicant liberalitati obcaecati personae potest praeterita sinit. Civibus contra efficitur eirmod, facérém ista iustó liberos, nostrum quarum. Dóctióres mundi quibus tránsferrem. Altéra conducunt dico dissensió dissentiet, domesticarum habitassé inesse legum próbantur, suspicor talem tempóribus vestrae. Adiit fabulas fictaé incursióne infánti, legendam meque praeclarorum reiciendis rem sentiri stabilém venandi véstibulum video.\",\"type\":\"text\"},{\"_id\":\"X4TPTSYPBNHLFDS4Z7B72QDTP4\",\"additional_properties\":{\"comments\":[]},\"content\":\"I can haz cheeseburger\",\"type\":\"interstitial_link\",\"url\":\"https://icanhas.cheezburger.com/\"},{\"_id\":\"BSC7BCWPRNF2HIMZHQMD6N4VKM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Autém bibendum deorsus dialéctica haé, ipsi ómnia peccandi pervenias possum quámvis soliditátem témporé. Caeco collegi infinito instituendarum mollit paratus perpauca pleniórem praeclara quaerenda quálisque torquatum utraque veritatis. Assecutus compositis desistunt effluere expetitur faciunt intus iracundia libidinosarum sálutátus stabiliqué. Aequi carum comprehenderit delectat disputando, genuit pronuntiaret. ádest amori effluere locum novi, sapien traditur vituperari. Continent emolumento inanes possunt. Adhibuit innumerabiles perpetuam philosopho solitudo utrum.\",\"type\":\"text\"},{\"_id\":\"MZK7XM3IEFG5FEZSLA6TZ3PSZM\",\"additional_properties\":{\"comments\":[]},\"content\":\"Cupio desperantes gloriosis latinis phasellus. Epicurus equos foris iucunda legantur perciperet. Acri alienus amicitiae fuscé gratia, honestum liberae póssum posuere quibus quocircá sophocles vivere. Conciliant delectet invidus nostram recta sero solitudo.\",\"type\":\"text\"},{\"_id\":\"33HATHXZGBDQDHWXYBHEVZUMGU\",\"additional_properties\":{\"comments\":[]},\"content\":\"Distinguique exercitationem fastidii finitas quoddam repugnantibus. Arridéns attento nimium óblivióne pártá, pérspécta quoquo. Declinatio faucibus leo ómittendis torquentur, utuntur vacuitate vituperátum. Conclusionemque dissentiás ferant iustioribus, multa partes putámus. Aequi cómmódó fit honestatis manum. Adhibenda eám efficiát éruditioném iucunditatem, mel negent parvos physicis platone respirare sedatio s\",\"type\":\"text\"},{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"additional_properties\":{\"comments\":[],\"has_published_copy\":true,\"owner\":\"matthew.nelson@washpost.com\",\"published\":true,\"roles\":[],\"version\":4},\"canonical_url\":\"/2021/08/16/cats-of-arc/\",\"content_elements\":[{\"_id\":\"BNPYOMX7LBCT3FXRYQGRWBBNAM\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_5752.JPG\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"published\":true,\"resizeUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"thumbnailResizeUrl\":\"/resizer/Z_PxsFTrQjjQYOmtz8nEg1Gt8r4\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:51:24Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":2123,\"last_updated_date\":\"2019-02-21T16:51:26Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"michelle cat\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":\"0.10.3\",\"width\":2438},{\"_id\":\"TONB3GC7SBAQDGACCGA5UPF5UA\",\"additional_properties\":{\"galleries\":[],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_2775.JPG\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/Tc6HUQgIzIa9_YHTb4mudOjw2H0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"published\":true,\"resizeUrl\":\"/resizer/Tc6HUQgIzIa9_YHTb4mudOjw2H0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"thumbnailResizeUrl\":\"/resizer/vsJZ9jjBKXS9sX-43csyKDJp81k\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"version\":0},\"address\":{},\"credits\":{},\"height\":4032,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"monte\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/Tc6HUQgIzIa9_YHTb4mudOjw2H0\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/TONB3GC7SBAQDGACCGA5UPF5UA.JPG\",\"version\":\"0.10.3\",\"width\":3024},{\"_id\":\"5OPJFEJNWBGUTMA3R3Y6JAVW5I\",\"additional_properties\":{\"galleries\":[],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"Image from iOS (1).jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/IWiMhBwHYFP3m4iVDRAW9gP6QYg\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/IWiMhBwHYFP3m4iVDRAW9gP6QYg\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"thumbnailResizeUrl\":\"/resizer/YsGB2Y9sxAsMHulL8sAShSnoat4\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"version\":0},\"address\":{},\"credits\":{},\"height\":4032,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Cat\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/IWiMhBwHYFP3m4iVDRAW9gP6QYg\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/5OPJFEJNWBGUTMA3R3Y6JAVW5I.jpg\",\"version\":\"0.10.3\",\"width\":3024},{\"_id\":\"C5XUI2AH2VDLDBKJRFNB73XYWM\",\"additional_properties\":{\"galleries\":[],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"V__30CF.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/GcRh0kB9tA6szaWFMtzNSB9cbPY\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/GcRh0kB9tA6szaWFMtzNSB9cbPY\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"thumbnailResizeUrl\":\"/resizer/5PrVpqMPBCKnRZ0kjr2t8Dy2zRU\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"version\":0},\"address\":{},\"credits\":{\"by\":[{\"name\":\"Picasa\",\"type\":\"author\"}]},\"height\":1600,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Turtle kitty\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/GcRh0kB9tA6szaWFMtzNSB9cbPY\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/C5XUI2AH2VDLDBKJRFNB73XYWM.jpg\",\"version\":\"0.10.3\",\"width\":1195},{\"_id\":\"I5VLXXVEAFATRPO4PQ33WT6LAM\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"image_from_ios (1).jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"owner\":\"william.cook@washpost.com\",\"proxyUrl\":\"/resizer/g7-tSY8LEWts3Ixa_ulb8qBLxgk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/g7-tSY8LEWts3Ixa_ulb8qBLxgk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"thumbnailResizeUrl\":\"/resizer/1ZuGZuUcS8Tczr4Dxog3eIcNgUo\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:11:26Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":842,\"last_updated_date\":\"2019-02-21T16:11:32Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Miki Naps in Box\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/g7-tSY8LEWts3Ixa_ulb8qBLxgk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/I5VLXXVEAFATRPO4PQ33WT6LAM.jpg\",\"version\":\"0.10.3\",\"width\":1122},{\"_id\":\"YX3I23GKNZD6RLN52XJ2PKF7LU\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[\"\",\"cat\",\"nap\",\"oreo\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"image_from_ios.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"owner\":\"william.cook@washpost.com\",\"proxyUrl\":\"/resizer/TCgU6F7dkvA_Ujg-HXhmMnNlieU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/TCgU6F7dkvA_Ujg-HXhmMnNlieU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"thumbnailResizeUrl\":\"/resizer/HT6WwVB2t1F_rbFg6PiQNtYAIS4\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:12:27Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":2268,\"last_updated_date\":\"2019-02-21T16:12:29Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Oreo Naptime\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/TCgU6F7dkvA_Ujg-HXhmMnNlieU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/YX3I23GKNZD6RLN52XJ2PKF7LU.jpg\",\"version\":\"0.10.3\",\"width\":3024},{\"_id\":\"JTRWKF4N5BH3ZLMIIQ7USR4YFU\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_5590.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"owner\":\"melissa.depuydt@washpost.com\",\"proxyUrl\":\"/resizer/S-gcvu7Op2hVdOHJeTrFrHihjOU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/S-gcvu7Op2hVdOHJeTrFrHihjOU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"thumbnailResizeUrl\":\"/resizer/be0FWBcr0Op25mqvJx6pyLMq0LM\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:08:51Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":1651,\"last_updated_date\":\"2019-02-21T16:08:57Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Albus\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/S-gcvu7Op2hVdOHJeTrFrHihjOU\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/JTRWKF4N5BH3ZLMIIQ7USR4YFU.jpg\",\"version\":\"0.10.3\",\"width\":2201},{\"_id\":\"RIINOEOPJNB53KFHGEVZOYGHBQ\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_6959.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"owner\":\"melissa.depuydt@washpost.com\",\"proxyUrl\":\"/resizer/7s5HZuPDaSPriMsZGiKNkBqv93U\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/7s5HZuPDaSPriMsZGiKNkBqv93U\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"thumbnailResizeUrl\":\"/resizer/ThWn4tXKvbW6Sr-ferBkBhAD-EE\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:06:43Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":2268,\"last_updated_date\":\"2019-02-21T16:06:50Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"he fits, he sits\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/7s5HZuPDaSPriMsZGiKNkBqv93U\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/RIINOEOPJNB53KFHGEVZOYGHBQ.jpg\",\"version\":\"0.10.3\",\"width\":3024},{\"_id\":\"PXSGHHECONEXHODQ6NVDZXVPZM\",\"additional_properties\":{\"galleries\":[],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_20171021_132711.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"owner\":\"susan.tyler@washpost.com\",\"proxyUrl\":\"/resizer/uyxUae4e-3YfUPDA0PBeN4ewhzk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/uyxUae4e-3YfUPDA0PBeN4ewhzk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"thumbnailResizeUrl\":\"/resizer/KjsHvYmMU4YBqp8ZFEDIO93dt8U\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"version\":0},\"address\":{},\"credits\":{},\"height\":3024,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Thriller\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/uyxUae4e-3YfUPDA0PBeN4ewhzk\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/PXSGHHECONEXHODQ6NVDZXVPZM.jpg\",\"version\":\"0.10.3\",\"width\":4032},{\"_id\":\"55VYTIER35C5DIPJ5GYUR6KROE\",\"additional_properties\":{\"galleries\":[],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"the gambler.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"owner\":\"eric.carlisle@washpost.com\",\"proxyUrl\":\"/resizer/Fdw_t2G3kSEjvym1y4GPE3amFck\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/Fdw_t2G3kSEjvym1y4GPE3amFck\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"thumbnailResizeUrl\":\"/resizer/RQQhSg2Utzw6aiCjYfPzw6uHS3w\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"version\":0},\"address\":{},\"credits\":{},\"height\":898,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"The Gambler\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/Fdw_t2G3kSEjvym1y4GPE3amFck\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/55VYTIER35C5DIPJ5GYUR6KROE.jpg\",\"version\":\"0.10.3\",\"width\":1198},{\"_id\":\"43Z756DP3RCCPHPYEERIGOXFFQ\",\"additional_properties\":{\"galleries\":[],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"secretgarden.jpg\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"owner\":\"joseph.gilbert@washpost.com\",\"proxyUrl\":\"/resizer/J0C0ik_P50egpt45xMKOi0SEG-g\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"published\":true,\"resizeUrl\":\"/resizer/J0C0ik_P50egpt45xMKOi0SEG-g\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"thumbnailResizeUrl\":\"/resizer/Or14R0bFgX2fOwlm2Oot_umjsEs\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"version\":0},\"address\":{},\"credits\":{},\"height\":1202,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Wildcat spotted!\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com//resizer/J0C0ik_P50egpt45xMKOi0SEG-g\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/43Z756DP3RCCPHPYEERIGOXFFQ.jpg\",\"version\":\"0.10.3\",\"width\":675}],\"created_date\":\"2019-02-21T16:52:57Z\",\"credits\":{\"by\":[]},\"description\":{\"basic\":\"This is a gallery of Arc Cats.\"},\"display_date\":\"2019-02-21T16:52:57Z\",\"first_publish_date\":\"2019-02-21T16:52:57Z\",\"headlines\":{\"basic\":\"Cats of Arc\"},\"last_updated_date\":\"2021-08-16T17:24:59Z\",\"owner\":{\"id\":\"sandbox.corecomponents\"},\"promo_items\":{\"basic\":{\"_id\":\"BNPYOMX7LBCT3FXRYQGRWBBNAM\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_5752.JPG\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"published\":true,\"resizeUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"thumbnailResizeUrl\":\"/resizer/Z_PxsFTrQjjQYOmtz8nEg1Gt8r4\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:51:24Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":2123,\"last_updated_date\":\"2019-02-21T16:51:26Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"michelle cat\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":\"0.10.3\",\"width\":2438}},\"publish_date\":\"2021-08-13T15:38:49.268Z\",\"taxonomy\":{},\"type\":\"gallery\",\"version\":\"0.10.3\"},{\"_id\":\"2QT3ZMWN4ZADRCDYJ3XJXPTTUA\",\"additional_properties\":{\"comments\":[]},\"content\":\"uavitate tránsferám. Adamare consequi duxit finis, iisque intelleges summum.\",\"type\":\"text\"},{\"_id\":\"NJ4YQBFHXJAF5P7AXHAIXPBNAE\",\"additional_properties\":{\"comments\":[]},\"content\":\"Errore explicátis nonne pueri tárentinis. Curiosi déducérét gymnasia sollicitudin, soluta sunt. Culpá divitiarum dixit erunt ignavia, irridente miseram perfecto praécéptricé reque tincidunt vacuitate. Cógnósci cónfórmavit fugiendum omnino pellentesque. Béstiaé conversa fortasse gravitér hausta humili odio pérvénias stabilem suscipiantur.\",\"type\":\"text\"},{\"_id\":\"O5ICDWUHIFGMFCUCUUUGQ34KXQ\",\"subtype\":\"instagram\",\"type\":\"oembed_response\"},{\"_id\":\"5LJTL7KP7REQPIVF7BKEYHO2KY\",\"additional_properties\":{\"comments\":[]},\"content\":\"Detracto existimavit iriure posuere pugnare, putamus sapientium summum suum. Curabitur désidératuram dicenda dico, epicurus hostis intervenire mihi, omnesque práeclárorum praétéréa sévéra unum. Assentiar compréhénsam debilitati regione successerit, tempore tractavissent. Accusantibus conscientiam declinabunt dicas dixeris epicurei érat filio sinit studia suscipere. Defuturum exeduntur quia réquiréré. Aiunt dicit erudito éxpréssas graecis, imperitorum iracundia lóquerer partes praestabiliórem salutatus triárium.\",\"type\":\"text\"},{\"_id\":\"YYYCTF2AWVHLRJ7IZWIFH3FUPA\",\"additional_properties\":{\"comments\":[]},\"content\":\"benivole cóerceri cónficiuntur curá de\",\"type\":\"text\"},{\"_id\":\"CTIQTHIPYNGV5LGWU63REPDAHE\",\"additional_properties\":{\"comments\":[]},\"content\":\"Benivole coerceri donsifes\",\"type\":\"header\"},{\"_id\":\"OGOXMZ3T2NB6BEVNF2VPXRI2NE\",\"additional_properties\":{\"comments\":[]},\"content\":\"Dissident fáutrices firme hóneste libidinibus modératio morati mors saluto sévéra usus. Adversa cognitionem dicitur earumque, musicis nominis ornatus polyaeno, summumque tamquam variis. Conspiratione efflorescere elegantis medium, molestia nonné quos triarium ullo vigiliae vita.\",\"type\":\"text\"},{\"_id\":\"OZAYPLA5JFCFNFKCPKZLJOQ2GE\",\"subtype\":\"twitter\",\"type\":\"oembed_response\"},{\"_id\":\"I77Z64ZLKJAOLPZDTMF6JHQ5SY\",\"additional_properties\":{\"comments\":[]},\"content\":\"Aégritudo contereret diesque dulce eam, liberatione male putas sóciósqu. Confectum graeco patriam regione. Agam bene benivole cóerceri cónficiuntur curá defenditur dominationis sententiae stoici tenetur voluptatis. Amicum ánimus assentior circumcisaque ignoránt iste quicquid rebus triari.\",\"type\":\"text\"},{\"_id\":\"IHGAJZ3YUZDC5PWY3DYCFEFZCI\",\"additional_properties\":{\"comments\":[]},\"content_elements\":[{\"_id\":\"WBLMU57SU5F4PKVV3BZNNKZSUA\",\"additional_properties\":{\"comments\":[]},\"content\":\"Time spent with cats is never wasted. \",\"type\":\"text\"}],\"subtype\":\"pullquote\",\"type\":\"quote\"},{\"_id\":\"FGXFXPO42ZCRVHRRF3T5YQQENI\",\"additional_properties\":{\"comments\":[]},\"content\":\"Albucius class clita conspectum erudito impediri occultarum pleniorem quondam. Adhibenda careret cetero consistat cupidatat desiderent fortitudinem honéstatis inciderit quanti salutandi teneam. Eá epicureis huius optimi viris. Inprobitas iudicio justo loquuntur optime petentium plérisqué pondere suscipere. Brevi confirmaré cyrenaicos dápibus erant fecerit i indicavérunt nonumy plusque porttitor probet servire tranquillae.\",\"type\":\"text\"},{\"_id\":\"4J2FFQ23MJAH3C27Y6RZ4TK2MQ\",\"additional_properties\":{\"comments\":[]},\"content\":\"\\u003cdiv style\\u003d\\\"text-align:center;margin: 0 auto;padding: 25px; background: teal; color: white;\\\"\\u003e\\n    I am some raw html.\\n\\u003c/div\\u003e\",\"type\":\"raw_html\"},{\"_id\":\"OJXUZVPUQZAAVHMQWDLSZTCECI\",\"additional_properties\":{\"comments\":[]},\"content\":\"Beatus dominátionis errátá iudicant miserius negent praesertim sine turpis. Arcu consistat dicenda efficiatur, excepteur gubergren ignoraré laboribus négant refórmidans solet sublatum suscipit. Affirmatis arbitratu attulit class cógnitiónem, cónficiuntur nemini práetore qualisqué salutandi semel. Albuci cibo dicés habemus morborum. Affécti cuius disputata dolorum efficeretur, epicuri neglegentur quamque quapropter quodsi voce. Copiosae dediti doctiorés eademque gymnasia hábere legam malorum ócculta perturbári studiis tali tu. Argumentandum chrysippi democritum inflammat inpendente magnitudinem nacti pertinerent placatae porro repudiare scaevola tantam términatas vita.\",\"type\":\"text\"},{\"_id\":\"PYKACTM63FADDD5ST3H6FTKXXI\",\"type\":\"reference\"},{\"_id\":\"7LHUQWE7LVGX3MCVJG76YVCGFU\",\"additional_properties\":{\"comments\":[]},\"content\":\"Collegisti comparat cyrenaicisque dicás eventurum, individuá irure maioribus mors optimus, quosdám terrore. Contenta cu epicuro epicurus éxpéctat, inimicus málunt mediocris odit tali. Cadere dixeris igitur justo lectus, liberemus maluisti omnia perturbatur quia referuntur severitatem virtutibus. Adest aptius conficiuntqué expressas facillimis, legum nisi póetarum reperiri sagittis tuó. Adiit certámen delectari didicerimus expeteretur honestum humili legere moderatio pertinerent répériri vitam.\",\"type\":\"text\"},{\"_id\":\"5BNVGZNIKRBV7F7KC4I66627WA\",\"additional_properties\":{\"comments\":[]},\"content\":\"Doggo ipsum you are doin me a concern wow such tempt snoot big ol, heckin angery woofer wow such tempt clouds heckin, wow such tempt adorable doggo. Wow such tempt vvv doggorino yapper woofer, you are doin me a concern blop boof, heck wow very biscit bork. Vvv blop heckin good boys you are doing me a frighten shibe wow very biscit, shoober wow such tempt ruff floofs. Floofs waggy wags h*ck doggo puggorino pupper, woofer he made many woofs the neighborhood pupper. smol borking doggo with a long snoot for pats. The neighborhood pupper smol borking doggo with a long snoot for pats such treat extremely cuuuuuute the neighborhood pupper doggo wrinkler long bois, big ol pats fluffer boof porgo.\",\"type\":\"text\"},{\"_id\":\"WGY7KDSWDJF2TLRPWG2464AKF4\",\"additional_properties\":{\"comments\":[]},\"type\":\"list\"},{\"_id\":\"23R6XJRWNNAGPDTSTK54UPTNZ4\",\"additional_properties\":{\"comments\":[]},\"content\":\"Extremely cuuuuuute very hand that feed shibe woofer vvv, shoober. Wow such tempt most angery pupper I have ever seen you are doing me a frighten length boy borking doggo corgo long woofer the neighborhood pupper, smol long water shoob maximum borkdrive blep pats. Puggo shoob corgo blop doggorino, heck boofers. Mlem long doggo dat tungg tho borkf porgo borking doggo, wrinkler boof long woofer you are doing me the shock. You are doing me the shock long water shoob pupperino vvv, porgo shoob long bois, most angery pupper I have ever seen snoot. Puggo wrinkler extremely cuuuuuute doggorino, shoob noodle horse. Blop sub woofer you are doin me a concern puggorino doggorino, dat tungg tho boof. Shibe much ruin diet maximum borkdrive heckin good boys and girls many pats shibe shoober sub woofer, such treat very jealous pupper very good spot the neighborhood pupper heckin good boys the neighborhood pupper. Blep pupperino you are doing me a frighten shoober super chub, wow such tempt heckin good boys and girls. Very good spot porgo you are doing me a frighten extremely cuuuuuute, big ol pupper\",\"type\":\"text\"},{\"_id\":\"4ACL3OFVL5H5VCPDRT7IT6PZP4\",\"additional_properties\":{\"comments\":[]},\"type\":\"list\"},{\"_id\":\"K3KO7KD54BD5VERQTXKGO6Y6F4\",\"additional_properties\":{\"comments\":[]},\"content\":\"Sub woofer big ol maximum borkdrive aqua doggo mlem, maximum borkdrive I am bekom fat. long woofer what a nice floof. heck stop it fren. Corgo lotsa pats maximum borkdrive, blop. Lotsa pats maximum borkdrive blop fluffer shooberino, long doggo yapper he made many woofs long bois, very jealous pupper pats lotsa pats. Long water shoob I am bekom fat you are doing me a frighten heckin you are doin me a concern corgo borkdrive, clouds pupper heckin doge very hand that feed shibe. Big ol fluffer doing me a frighten most angery pupper I have ever seen thicc length boy dat tungg tho very jealous pupper porgo, floofs doggorino waggy wags most angery pupper I have ever seen ruff blop. You are doin me a concern the neighborhood pupper sub woofer very jealous pupper fluffer heck fat boi, wow very biscit heckin angery woofer borkf stop it fren yapper. Heck tungg very good spot porgo lotsa pats, long doggo pupper porgo.\",\"type\":\"text\"},{\"_id\":\"ATW7HPGNENCMZA277OTSUIMWAI\",\"additional_properties\":{\"comments\":[]},\"type\":\"table\"},{\"_id\":\"Y2NMVDMLAJBBZGOSPYMH7V34Z4\",\"additional_properties\":{\"comments\":[]},\"type\":\"list\"},{\"_id\":\"HEKD4R6EFRCV7JYKGZFNIG57T4\",\"additional_properties\":{\"comments\":[]},\"content\":\"\\u003cbr/\\u003e\",\"type\":\"text\"},{\"_id\":\"OOITKXLHK5H2BDLJOQYPJNGWKI\",\"additional_properties\":{\"comments\":[]},\"content\":\"\\u003cbr/\\u003e\",\"type\":\"text\"}],\"credits\":{\"by\":[{\"name\":\"Matt Nelson\",\"type\":\"author\"}]},\"headlines\":{\"basic\":\"Kitchen Sink Article 2: The Gazette\"},\"publish_date\":\"2021-08-13T15:38:49.268Z\",\"type\":\"story\",\"version\":\"0.10.7\",\"website_url\":\"/2019/03/06/kitchen-sink-article-2-the-gazette/\"}"
    private val collectionListJson =
        "[{\"credits\":{\"by\":[]},\"type\":\"story\",\"description\":{\"basic\":\"\"},\"headlines\":{\"basic\":\"Story with lead art with custom embed subtype video\"},\"_id\":\"5QOT2SC6CNDHHDAH3RG3FVND6M\",\"promo_items\":{\"basic\":{\"_id\":\"CEQW6QLR4SDVNPFUSVSXYJEXEM\",\"type\":\"reference\"}},\"publish_date\":\"2021-08-13T15:38:49.268Z\"},{\"credits\":{\"by\":[]},\"type\":\"story\",\"description\":{\"basic\":\"Test gallery for the Gallery resolver\"},\"headlines\":{\"basic\":\"Beltran Test Gallery\"},\"_id\":\"TQAJOBEGYJAQTBK4LHNFDRCWY4\",\"promo_items\":{\"basic\":{\"_id\":\"APKWVSVIVFGRPC6MAGKWZAKFUA\",\"additional_properties\":{\"galleries\":[],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"2113505b-4651-4346-8b78-b94360d9eaf1.JPG\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/APKWVSVIVFGRPC6MAGKWZAKFUA.JPG\",\"owner\":\"beltran.caliz@washpost.com\",\"proxyUrl\":\"/resizer/j2EbAFyOBLYKdJbk5ws2rRKHiPE\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/APKWVSVIVFGRPC6MAGKWZAKFUA.JPG\",\"published\":true,\"resizeUrl\":\"/resizer/j2EbAFyOBLYKdJbk5ws2rRKHiPE\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/APKWVSVIVFGRPC6MAGKWZAKFUA.JPG\",\"thumbnailResizeUrl\":\"/resizer/Z_T5a4FOI4oSUgt_5OvhIIKLeLg\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/APKWVSVIVFGRPC6MAGKWZAKFUA.JPG\",\"version\":0},\"address\":{},\"caption\":\"Comino\\u0027s island in Malta\",\"credits\":{},\"height\":1024,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Malta\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com/resizer/j2EbAFyOBLYKdJbk5ws2rRKHiPE\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/APKWVSVIVFGRPC6MAGKWZAKFUA.JPG\",\"version\":\"0.10.3\",\"width\":768}},\"publish_date\":\"2021-08-13T15:38:49.268Z\"},\n" +
                "{\"credits\":{\"by\":[]},\"description\":{\"basic\":\"This is a gallery of Arc Cats.\"},\"headlines\":{\"basic\":\"Cats of Arc\"},\"type\":\"story\",\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"promo_items\":{\"basic\":{\"_id\":\"BNPYOMX7LBCT3FXRYQGRWBBNAM\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_5752.JPG\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"published\":true,\"resizeUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"thumbnailResizeUrl\":\"/resizer/Z_PxsFTrQjjQYOmtz8nEg1Gt8r4\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:51:24Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":2123,\"last_updated_date\":\"2019-02-21T16:51:26Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"michelle cat\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":\"0.10.3\",\"width\":2438}},\"publish_date\":\"2021-08-13T15:38:49.268Z\"}]"
    private val collectionJson0 =
        "{\"credits\":{\"by\":[]},\"type\":\"story\",\"description\":{\"basic\":\"\"},\"headlines\":{\"basic\":\"Story with lead art with custom embed subtype video\"},\"_id\":\"5QOT2SC6CNDHHDAH3RG3FVND6M\",\"promo_items\":{\"basic\":{\"_id\":\"CEQW6QLR4SDVNPFUSVSXYJEXEM\",\"type\":\"reference\"}},\"publish_date\":\"2021-08-13T15:38:49.268Z\"}"
    private val collectionJson1 =
        "{\"credits\":{\"by\":[]},\"type\":\"story\",\"description\":{\"basic\":\"Test gallery for the Gallery resolver\"},\"headlines\":{\"basic\":\"Beltran Test Gallery\"},\"_id\":\"TQAJOBEGYJAQTBK4LHNFDRCWY4\",\"promo_items\":{\"basic\":{\"_id\":\"APKWVSVIVFGRPC6MAGKWZAKFUA\",\"additional_properties\":{\"galleries\":[],\"keywords\":[\"\"],\"mime_type\":\"image/jpeg\",\"originalName\":\"2113505b-4651-4346-8b78-b94360d9eaf1.JPG\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/APKWVSVIVFGRPC6MAGKWZAKFUA.JPG\",\"owner\":\"beltran.caliz@washpost.com\",\"proxyUrl\":\"/resizer/j2EbAFyOBLYKdJbk5ws2rRKHiPE\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/APKWVSVIVFGRPC6MAGKWZAKFUA.JPG\",\"published\":true,\"resizeUrl\":\"/resizer/j2EbAFyOBLYKdJbk5ws2rRKHiPE\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/APKWVSVIVFGRPC6MAGKWZAKFUA.JPG\",\"thumbnailResizeUrl\":\"/resizer/Z_T5a4FOI4oSUgt_5OvhIIKLeLg\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/APKWVSVIVFGRPC6MAGKWZAKFUA.JPG\",\"version\":0},\"address\":{},\"caption\":\"Comino\\u0027s island in Malta\",\"credits\":{},\"height\":1024,\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"Malta\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com/resizer/j2EbAFyOBLYKdJbk5ws2rRKHiPE\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/APKWVSVIVFGRPC6MAGKWZAKFUA.JPG\",\"version\":\"0.10.3\",\"width\":768}},\"publish_date\":\"2021-08-13T15:38:49.268Z\"}"
    private val collectionJson2 =
        "{\"credits\":{\"by\":[]},\"type\":\"story\",\"description\":{\"basic\":\"This is a gallery of Arc Cats.\"},\"headlines\":{\"basic\":\"Cats of Arc\"},\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"promo_items\":{\"basic\":{\"_id\":\"BNPYOMX7LBCT3FXRYQGRWBBNAM\",\"additional_properties\":{\"galleries\":[{\"_id\":\"KUD5XN7BMFHY7FKPT23WJ5TXQI\",\"headlines\":{\"basic\":\"Cats of Arc\"}}],\"keywords\":[],\"mime_type\":\"image/jpeg\",\"originalName\":\"IMG_5752.JPG\",\"originalUrl\":\"https://cloudfront-us-east-1.images.arcpublishing.com/sandbox.corecomponents/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"owner\":\"matthew.nelson@washpost.com\",\"proxyUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"published\":true,\"resizeUrl\":\"/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"thumbnailResizeUrl\":\"/resizer/Z_PxsFTrQjjQYOmtz8nEg1Gt8r4\\u003d/300x0/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":1},\"address\":{},\"created_date\":\"2019-02-21T16:51:24Z\",\"credits\":{\"by\":[]},\"geo\":{},\"height\":2123,\"last_updated_date\":\"2019-02-21T16:51:26Z\",\"licensable\":false,\"owner\":{\"id\":\"sandbox.corecomponents\"},\"subtitle\":\"michelle cat\",\"taxonomy\":{},\"type\":\"image\",\"url\":\"https://corecomponents-the-gazette-prod.cdn.arcpublishing.com/resizer/1GrgTi1dYh1aMQ3COizjcc2EGOA\\u003d/arc-anglerfish-arc2-sandbox-sandbox-corecomponents/public/BNPYOMX7LBCT3FXRYQGRWBBNAM.JPG\",\"version\":\"0.10.3\",\"width\":2438}},\"publish_date\":\"2021-08-13T15:38:49.268Z\"}"

    private val sectionListJson =
        "[{\"_id\":\"/mobile-topstories\",\"site_topper\":{\"site_logo_image\":null},\"social\":{\"rss\":null,\"twitter\":null,\"facebook\":null,\"instagram\":null},\"site\":{\"site_url\":null,\"site_about\":null,\"site_description\":\"Top story collection to power the ArcXP mobile SDK\",\"pagebuilder_path_for_native_apps\":null,\"site_tagline\":null,\"site_title\":\"Mobile - Top Stories\",\"site_keywords\":null},\"navigation\":{\"nav_title\":\"Top Stories\"},\"name\":\"Mobile - Top Stories\",\"_website\":\"arcsales\",\"parent\":{\"default\":\"/\",\"mobile-nav\":\"/\"},\"ancestors\":{\"default\":[],\"mobile-nav\":[\"/\"]},\"_admin\":{\"alias_ids\":[\"/mobile-topstories\"]},\"inactive\":false,\"node_type\":\"section\",\"order\":{\"mobile-nav\":1001},\"children\":[]},{\"_id\":\"/mobile-politics\",\"site_topper\":{\"site_logo_image\":null},\"social\":{\"rss\":null,\"twitter\":null,\"facebook\":null,\"instagram\":null},\"site\":{\"site_url\":null,\"site_about\":null,\"site_description\":\"Politics collection to power the ArcXP mobile SDK\",\"pagebuilder_path_for_native_apps\":null,\"site_tagline\":null,\"site_title\":\"Mobile - Politics\",\"site_keywords\":\"politics\"},\"navigation\":{\"nav_title\":\"Politics\"},\"_admin\":{\"alias_ids\":[\"/mobile-politics\"]},\"_website\":\"arcsales\",\"name\":\"Mobile - Politics\",\"order\":{\"mobile-nav\":1002},\"parent\":{\"default\":\"/\",\"mobile-nav\":\"/\"},\"ancestors\":{\"default\":[],\"mobile-nav\":[\"/\"]},\"inactive\":false,\"node_type\":\"section\",\"children\":[]},{\"_id\":\"/mobile-tech\",\"site_topper\":{\"site_logo_image\":null},\"social\":{\"rss\":null,\"twitter\":null,\"facebook\":null,\"instagram\":null},\"site\":{\"site_url\":null,\"site_about\":null,\"site_description\":\"Tech collection to power the ArcXP mobile SDK\",\"pagebuilder_path_for_native_apps\":null,\"site_tagline\":null,\"site_title\":\"Mobile - Tech\",\"site_keywords\":null},\"navigation\":{\"nav_title\":\"Tech\"},\"_admin\":{\"alias_ids\":[\"/mobile-tech\"]},\"_website\":\"arcsales\",\"name\":\"Mobile - Tech\",\"parent\":{\"default\":\"/\",\"mobile-nav\":\"/\"},\"ancestors\":{\"default\":[],\"mobile-nav\":[\"/\"]},\"inactive\":false,\"node_type\":\"section\",\"order\":{\"mobile-nav\":1003},\"children\":[]},{\"_id\":\"/mobile-sports\",\"site_topper\":{\"site_logo_image\":null},\"social\":{\"rss\":null,\"twitter\":null,\"facebook\":null,\"instagram\":null},\"site\":{\"site_url\":null,\"site_about\":null,\"site_description\":\"Sports collection to power the ArcXP mobile SDK\",\"pagebuilder_path_for_native_apps\":null,\"site_tagline\":null,\"site_title\":\"Mobile - Sports\",\"site_keywords\":\"sports\"},\"navigation\":{\"nav_title\":\"Sports\"},\"_admin\":{\"alias_ids\":[\"/mobile-sports\"]},\"_website\":\"arcsales\",\"name\":\"Mobile - Sports\",\"parent\":{\"default\":\"/\",\"mobile-nav\":\"/\"},\"ancestors\":{\"default\":[],\"mobile-nav\":[\"/\"]},\"inactive\":false,\"node_type\":\"section\",\"order\":{\"mobile-nav\":1004},\"children\":[]},{\"_id\":\"/mobile-entertainment\",\"site_topper\":{\"site_logo_image\":null},\"social\":{\"rss\":null,\"twitter\":null,\"facebook\":null,\"instagram\":null},\"site\":{\"site_url\":null,\"site_about\":null,\"site_description\":\"Entertainment collection to power the ArcXP mobile SDK\",\"pagebuilder_path_for_native_apps\":null,\"site_tagline\":null,\"site_title\":\"Mobile - Entertainment\",\"site_keywords\":\"entertainment\"},\"navigation\":{\"nav_title\":\"Entertainment\"},\"_admin\":{\"alias_ids\":[\"/mobile-entertainment\"]},\"_website\":\"arcsales\",\"name\":\"Mobile - Entertainment\",\"parent\":{\"default\":\"/\",\"mobile-nav\":\"/\"},\"ancestors\":{\"default\":[],\"mobile-nav\":[\"/\"]},\"inactive\":false,\"node_type\":\"section\",\"order\":{\"mobile-nav\":1005},\"children\":[]}]"
    private val sectionListJson2 =
        "[{\"_id\":\"/mobile-topstories2\",\"site_topper\":{\"site_logo_image\":null},\"social\":{\"rss\":null,\"twitter\":null,\"facebook\":null,\"instagram\":null},\"site\":{\"site_url\":null,\"site_about\":null,\"site_description\":\"Top story collection to power the ArcXP mobile SDK\",\"pagebuilder_path_for_native_apps\":null,\"site_tagline\":null,\"site_title\":\"Mobile - Top Stories\",\"site_keywords\":null},\"navigation\":{\"nav_title\":\"Top Stories\"},\"name\":\"Mobile - Top Stories\",\"_website\":\"arcsales\",\"parent\":{\"default\":\"/\",\"mobile-nav\":\"/\"},\"ancestors\":{\"default\":[],\"mobile-nav\":[\"/\"]},\"_admin\":{\"alias_ids\":[\"/mobile-topstories\"]},\"inactive\":false,\"node_type\":\"section\",\"order\":{\"mobile-nav\":1001},\"children\":[]},{\"_id\":\"/mobile-politics\",\"site_topper\":{\"site_logo_image\":null},\"social\":{\"rss\":null,\"twitter\":null,\"facebook\":null,\"instagram\":null},\"site\":{\"site_url\":null,\"site_about\":null,\"site_description\":\"Politics collection to power the ArcXP mobile SDK\",\"pagebuilder_path_for_native_apps\":null,\"site_tagline\":null,\"site_title\":\"Mobile - Politics\",\"site_keywords\":\"politics\"},\"navigation\":{\"nav_title\":\"Politics\"},\"_admin\":{\"alias_ids\":[\"/mobile-politics\"]},\"_website\":\"arcsales\",\"name\":\"Mobile - Politics\",\"order\":{\"mobile-nav\":1002},\"parent\":{\"default\":\"/\",\"mobile-nav\":\"/\"},\"ancestors\":{\"default\":[],\"mobile-nav\":[\"/\"]},\"inactive\":false,\"node_type\":\"section\",\"children\":[]},{\"_id\":\"/mobile-tech\",\"site_topper\":{\"site_logo_image\":null},\"social\":{\"rss\":null,\"twitter\":null,\"facebook\":null,\"instagram\":null},\"site\":{\"site_url\":null,\"site_about\":null,\"site_description\":\"Tech collection to power the ArcXP mobile SDK\",\"pagebuilder_path_for_native_apps\":null,\"site_tagline\":null,\"site_title\":\"Mobile - Tech\",\"site_keywords\":null},\"navigation\":{\"nav_title\":\"Tech\"},\"_admin\":{\"alias_ids\":[\"/mobile-tech\"]},\"_website\":\"arcsales\",\"name\":\"Mobile - Tech\",\"parent\":{\"default\":\"/\",\"mobile-nav\":\"/\"},\"ancestors\":{\"default\":[],\"mobile-nav\":[\"/\"]},\"inactive\":false,\"node_type\":\"section\",\"order\":{\"mobile-nav\":1003},\"children\":[]},{\"_id\":\"/mobile-sports\",\"site_topper\":{\"site_logo_image\":null},\"social\":{\"rss\":null,\"twitter\":null,\"facebook\":null,\"instagram\":null},\"site\":{\"site_url\":null,\"site_about\":null,\"site_description\":\"Sports collection to power the ArcXP mobile SDK\",\"pagebuilder_path_for_native_apps\":null,\"site_tagline\":null,\"site_title\":\"Mobile - Sports\",\"site_keywords\":\"sports\"},\"navigation\":{\"nav_title\":\"Sports\"},\"_admin\":{\"alias_ids\":[\"/mobile-sports\"]},\"_website\":\"arcsales\",\"name\":\"Mobile - Sports\",\"parent\":{\"default\":\"/\",\"mobile-nav\":\"/\"},\"ancestors\":{\"default\":[],\"mobile-nav\":[\"/\"]},\"inactive\":false,\"node_type\":\"section\",\"order\":{\"mobile-nav\":1004},\"children\":[]},{\"_id\":\"/mobile-entertainment\",\"site_topper\":{\"site_logo_image\":null},\"social\":{\"rss\":null,\"twitter\":null,\"facebook\":null,\"instagram\":null},\"site\":{\"site_url\":null,\"site_about\":null,\"site_description\":\"Entertainment collection to power the ArcXP mobile SDK\",\"pagebuilder_path_for_native_apps\":null,\"site_tagline\":null,\"site_title\":\"Mobile - Entertainment\",\"site_keywords\":\"entertainment\"},\"navigation\":{\"nav_title\":\"Entertainment\"},\"_admin\":{\"alias_ids\":[\"/mobile-entertainment\"]},\"_website\":\"arcsales\",\"name\":\"Mobile - Entertainment\",\"parent\":{\"default\":\"/\",\"mobile-nav\":\"/\"},\"ancestors\":{\"default\":[],\"mobile-nav\":[\"/\"]},\"inactive\":false,\"node_type\":\"section\",\"order\":{\"mobile-nav\":1005},\"children\":[]}]"
}
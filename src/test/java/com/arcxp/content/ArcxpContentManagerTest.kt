package com.arcxp.content.repositories

import android.app.Application
import android.provider.Settings
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commons.analytics.ArcXPAnalyticsManager
import com.arcxp.commons.testutils.TestUtils
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.AnalyticsUtil
import com.arcxp.commons.util.BuildVersionProviderImpl
import com.arcxp.commons.util.Constants
import com.arcxp.commons.util.Constants.DEFAULT_PAGINATION_SIZE
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.commons.util.DependencyFactory.createArcXPException
import com.arcxp.commons.util.DependencyFactory.createBuildVersionProvider
import com.arcxp.commons.util.Either
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.MoshiController
import com.arcxp.commons.util.Success
import com.arcxp.commons.util.Utils
import com.arcxp.content.ArcXPContentConfig
import com.arcxp.content.ArcXPContentManager
import com.arcxp.content.extendedModels.ArcXPContentElement
import com.arcxp.content.extendedModels.ArcXPStory
import com.arcxp.content.models.ArcXPContentCallback
import com.arcxp.content.models.ArcXPSection
import com.arcxp.content.util.AuthManager
import com.arcxp.sdk.R
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Calendar


class ArcxpContentManagerTest {

    @get:Rule
    var rule = InstantTaskExecutorRule()

    @RelaxedMockK
    lateinit var contentRepository: ContentRepository

    @RelaxedMockK
    lateinit var application: Application

    @RelaxedMockK
    lateinit var arcxpContentCallback: ArcXPContentCallback

    @RelaxedMockK
    lateinit var analyticsBuildVersionProvider: BuildVersionProviderImpl

    @RelaxedMockK
    lateinit var analyticsUtil: AnalyticsUtil

    @RelaxedMockK
    lateinit var contentConfig: ArcXPContentConfig

    @RelaxedMockK
    lateinit var arcXPAnalyticsManager: ArcXPAnalyticsManager

    @RelaxedMockK
    lateinit var collectionLiveData: MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>

    @RelaxedMockK
    lateinit var jsonLiveData: MutableLiveData<Either<ArcXPException, String>>

    @RelaxedMockK
    lateinit var contentLiveData: MutableLiveData<Either<ArcXPException, ArcXPContentElement>>

    @RelaxedMockK
    lateinit var storyLiveData: MutableLiveData<Either<ArcXPException, ArcXPStory>>

    @RelaxedMockK
    lateinit var searchLiveData: MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>

    @RelaxedMockK
    lateinit var sectionListLiveData: MutableLiveData<Either<ArcXPException, List<ArcXPSection>>>

    private val id = "id"
    private val keywords = "keywords"
    private val json = "json"
    private val sectionsError = "Failed to load sections"

    private lateinit var testObject: ArcXPContentManager

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        mockkStatic(Calendar::class)
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { application.getString(R.string.bearer_token) } returns "bearer token"
        mockkObject(AuthManager)
        mockkObject(DependencyFactory)
        mockkStatic(Settings.Secure::class)
        every { DependencyFactory.ioDispatcher() } returns Dispatchers.Unconfined

        testObject =
            ArcXPContentManager(
                contentRepository = contentRepository,
                application = application,
                arcXPAnalyticsManager = arcXPAnalyticsManager,
                contentConfig = contentConfig,
                _contentLiveData = contentLiveData,
                _storyLiveData = storyLiveData,
                _collectionLiveData = collectionLiveData,
                _sectionListLiveData = sectionListLiveData,
                _searchLiveData = searchLiveData,
                _jsonLiveData = jsonLiveData,
            )
    }

    fun init() {
        coEvery { Log.d(any(), any()) } returns 0
        coEvery {
            Settings.Secure.getString(
                application.contentResolver,
                Settings.Secure.ANDROID_ID
            )
        } returns "1234"
        coEvery { createBuildVersionProvider() } returns analyticsBuildVersionProvider
        coEvery { analyticsUtil.getCurrentLocale() } returns "US-US"
        coEvery { analyticsUtil.deviceConnectionState() } returns "ONLINE"
        coEvery { analyticsUtil.screenOrientation() } returns "portrait"
        coEvery { analyticsBuildVersionProvider.model() } returns "model"
        coEvery { analyticsBuildVersionProvider.manufacturer() } returns "manufacturer"
        coEvery { analyticsBuildVersionProvider.sdkInt() } returns 123
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `creating instance sets token in AuthManager`() {
        verify(exactly = 1) { AuthManager.accessToken = "bearer token" }
    }

    @Test
    fun `getCollection success passes result to listener`() = runTest {
        init()
        val expected = mockk<HashMap<Int, ArcXPContentElement>>()
        coEvery {
            contentRepository.getCollection(
                collectionAlias = id,
                shouldIgnoreCache = false,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0,
                full = true
            )
        } returns Success(success = expected)

        testObject.getCollection(collectionAlias = id, listener = arcxpContentCallback, preLoading = true)

        coVerify(exactly = 1) { arcxpContentCallback.onGetCollectionSuccess(response = expected) }
    }

    @Test
    fun `getCollectionAsJson success passes result to listener`() = runTest {
        init()
        val expected = Success(success = json)
        coEvery {
            contentRepository.getCollectionAsJson(
                collectionAlias = id,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE,
                full = true
            )
        } returns expected

        testObject.getCollectionAsJson(collectionAlias = id, listener = arcxpContentCallback, preLoading = true)

        coVerify(exactly = 1) { arcxpContentCallback.onGetJsonSuccess(response = json) }
    }

    @Test
    fun `getCollection success passes result to livedata`() = runTest {
        init()
        val expected = HashMap<Int, ArcXPContentElement>()
        coEvery {
            contentRepository.getCollection(
                collectionAlias = id,
                shouldIgnoreCache = false,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } returns Success(success = expected)
        testObject.getCollection(collectionAlias = id)

        coVerify(exactly = 1) { collectionLiveData.postValue(Success(expected)) }
    }

    @Test
    fun `getContentSuspend returns value from repository`() = runTest {
        init()
        val expected = Failure(
            ArcXPException(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = "our error"
            )
        )
        coEvery {
            contentRepository.getContent(uuid = id, shouldIgnoreCache = false)
        } returns expected
        val actual = testObject.getContentSuspend(id = id)

        assertEquals(expected, actual)
    }

    @Test
    fun `getSectionListSuspend returns value from repository`() = runTest {
        init()
        val expected = Failure(
            ArcXPException(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = "our error"
            )
        )
        coEvery {
            contentRepository.getSectionList(shouldIgnoreCache = false)
        } returns expected
        val actual = testObject.getSectionListSuspend()

        assertEquals(expected, actual)
    }

    @Test
    fun `getCollectionAsJson success passes result to livedata`() = runTest {
        init()
        val expected = Success(success = json)
        coEvery {
            contentRepository.getCollectionAsJson(
                collectionAlias = id,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns expected
        testObject.getCollectionAsJson(collectionAlias = id)

        coVerify(exactly = 1) { jsonLiveData.postValue(expected) }
    }

    @Test
    fun `getCollection passes shouldIgnoreCache when populated`() = runTest {
        init()
        val expectedResult = HashMap<Int, ArcXPContentElement>()
        val expected = Success(success = expectedResult)
        coEvery {
            contentRepository.getCollection(
                collectionAlias = id,
                shouldIgnoreCache = true,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } returns expected
        testObject.getCollection(
            collectionAlias = id,
            listener = arcxpContentCallback,
            shouldIgnoreCache = true
        )

        coVerify {
            collectionLiveData.postValue(expected)
        }
    }

    @Test
    fun `getCollection failure passes error result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        coEvery {
            contentRepository.getCollection(
                collectionAlias = id,
                shouldIgnoreCache = false,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } returns Failure(failure = expected)

        testObject.getCollection(collectionAlias = id, listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onError(error = expected) }
    }

    @Test
    fun `getCollectionAsJson failure passes error result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        coEvery {
            contentRepository.getCollectionAsJson(
                collectionAlias = id,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Failure(failure = expected)

        testObject.getCollectionAsJson(collectionAlias = id, listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onError(error = expected) }
    }

    @Test
    fun `getCollection failure passes error result to livedata`() = runTest {
        init()
        val expectedError = mockk<ArcXPException>()
        val expected = Failure(failure = expectedError)
        coEvery {
            contentRepository.getCollection(
                collectionAlias = id,
                shouldIgnoreCache = false,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } returns expected
        testObject.getCollection(collectionAlias = id)

        coVerify(exactly = 1) { collectionLiveData.postValue(expected) }
    }

    @Test
    fun `getCollectionAsJson failure passes error result to livedata`() = runTest {
        init()
        val expectedError = mockk<ArcXPException>()
        val expected = Failure(failure = expectedError)
        coEvery {
            contentRepository.getCollectionAsJson(
                collectionAlias = id,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns expected
        testObject.getCollectionAsJson(collectionAlias = id)

        coVerify(exactly = 1) { jsonLiveData.postValue(expected) }
    }

    @Test
    fun `getVideoCollection calls getCollection with video collection name`() = runTest {
        init()
        val expectedVideoCollectionName = "video"
        coEvery { contentConfig.videoCollectionName } returns expectedVideoCollectionName
        coEvery {
            contentRepository.getCollection(
                collectionAlias = expectedVideoCollectionName,
                shouldIgnoreCache = true,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Failure(failure = mockk())
        testObject = spyk(testObject)

        testObject.getVideoCollection(shouldIgnoreCache = true)

        coVerify(exactly = 1) {
            testObject.getCollection(
                collectionAlias = expectedVideoCollectionName,
                listener = null,
                shouldIgnoreCache = true,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        }
    }

    @Test
    fun `getVideoCollection with defaults calls getCollection with defaults`() = runTest {
        init()
        val expectedVideoCollectionName = "video"
        mockkObject(ArcXPMobileSDK)
        coEvery { contentConfig.videoCollectionName } returns expectedVideoCollectionName
        coEvery {
            contentRepository.getCollection(
                collectionAlias = expectedVideoCollectionName,
                shouldIgnoreCache = false,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Failure(failure = mockk())
        testObject = spyk(testObject)

        testObject.getVideoCollection()

        coVerify(exactly = 1) {
            testObject.getCollection(
                collectionAlias = expectedVideoCollectionName,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0,
                shouldIgnoreCache = false,
                listener = null
            )
        }
    }

    @Test
    fun `search(list) success passes result to listener`() = runTest {
        init()
        val query = listOf("keyword1", "keyword2", "keyword3")
        val expectedModifiedQuery = "keyword1,keyword2,keyword3"
        val expected = mockk<Map<Int, ArcXPContentElement>>()

        coEvery {
            contentRepository.searchSuspend(
                searchTerm = expectedModifiedQuery,
                from = any(),
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Success(success = expected)

        testObject.search(searchTerms = query, listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onSearchSuccess(response = expected) }
    }

    @Test
    fun `search(list) success passes result to livedata`() = runTest {
        init()
        val query = listOf("keyword1", "keyword2", "keyword3")
        val expectedModifiedQuery = "keyword1,keyword2,keyword3"
        val expected = mockk<Map<Int, ArcXPContentElement>>()
        coEvery {
            contentRepository.searchSuspend(
                searchTerm = expectedModifiedQuery,
                from = any(),
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Success(success = expected)
        testObject.search(searchTerms = query, listener = arcxpContentCallback)

        coVerify(exactly = 1) { searchLiveData.postValue(Success(expected)) }
    }

    @Test
    fun `searchAsJson(list) success`() = runTest {
        init()
        val query = listOf("keyword1", "keyword2", "keyword3")
        val expectedModifiedQuery = "keyword1,keyword2,keyword3"
        val expected = "search json"

        coEvery {
            contentRepository.searchAsJsonSuspend(
                searchTerm = expectedModifiedQuery,
                from = any(),
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Success(success = expected)

        testObject.searchAsJson(searchTerms = query, listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onGetJsonSuccess(response = expected) }
        coVerify(exactly = 1) { jsonLiveData.postValue(Success(expected)) }
    }

    @Test
    fun `searchAsJsonSuspend(list) success`() = runTest {
        init()
        val query = listOf("keyword1", "keyword2", "keyword3")
        val expectedModifiedQuery = "keyword1,keyword2,keyword3"
        val expected = "search json"

        coEvery {
            contentRepository.searchAsJsonSuspend(
                searchTerm = expectedModifiedQuery,
                from = any(),
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Success(success = expected)

        val actual = testObject.searchAsJsonSuspend(searchTerms = query)

        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `searchAsJsonSuspend(string) success`() = runTest {
        init()
        val query = "keyword1,keyword2,keyword3"
        val expected = "search json"

        coEvery {
            contentRepository.searchAsJsonSuspend(
                searchTerm = query,
                from = any(),
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Success(success = expected)

        val actual = testObject.searchAsJsonSuspend(searchTerm = query)

        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `searchAsJson(string) live data success`() = runTest {
        init()
        val query = "keyword1,keyword2,keyword3"
        val expected = "search json"

        coEvery {
            contentRepository.searchAsJsonSuspend(
                searchTerm = query,
                from = any(),
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Success(success = expected)

        testObject.searchAsJson(searchTerm = query)

        coVerify(exactly = 1) { jsonLiveData.postValue(Success(expected)) }
    }

    @Test
    fun `searchAsJson(string) listener success`() = runTest {
        init()
        val query = "keyword1,keyword2,keyword3"
        val expected = "search json"

        coEvery {
            contentRepository.searchAsJsonSuspend(
                searchTerm = query,
                from = any(),
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Success(success = expected)

        testObject.searchAsJson(searchTerm = query, listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onGetJsonSuccess(response = expected) }
    }

    @Test
    fun `search as json (list) failure passes error result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        val query = listOf("keyword1", "keyword2", "keyword3")
        val expectedModifiedQuery = "keyword1,keyword2,keyword3"
        coEvery {
            contentRepository.searchAsJsonSuspend(
                searchTerm = expectedModifiedQuery,
                from = any(),
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Failure(failure = expected)
        testObject.searchAsJson(searchTerms = query, listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onError(error = expected) }
    }

    @Test
    fun `search  as json (list) failure passes error result to livedata`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        val query = listOf("keyword1", "keyword2", "keyword3")
        val expectedModifiedQuery = "keyword1,keyword2,keyword3"
        coEvery {
            contentRepository.searchAsJsonSuspend(
                searchTerm = expectedModifiedQuery,
                from = any(),
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Failure(failure = expected)
        testObject.searchAsJson(searchTerms = query)

        coVerify(exactly = 1) { jsonLiveData.postValue(Failure(expected)) }
    }

    @Test
    fun `search(list) failure passes error result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        val query = listOf("keyword1", "keyword2", "keyword3")
        val expectedModifiedQuery = "keyword1,keyword2,keyword3"
        coEvery {
            contentRepository.searchSuspend(
                searchTerm = expectedModifiedQuery,
                from = any(),
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Failure(failure = expected)
        testObject.search(searchTerms = query, listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onError(error = expected) }
    }

    @Test
    fun `search(list) failure passes error result to livedata`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        val query = listOf("keyword1", "keyword2", "keyword3")
        val expectedModifiedQuery = "keyword1,keyword2,keyword3"
        coEvery {
            contentRepository.searchSuspend(
                searchTerm = expectedModifiedQuery,
                from = any(),
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Failure(failure = expected)
        testObject.search(searchTerms = query)

        coVerify(exactly = 1) { searchLiveData.postValue(Failure(expected)) }
    }

    @Test
    fun `search(string) success passes result to listener`() = runTest {
        init()
        val expected = mockk<Map<Int, ArcXPContentElement>>()
        coEvery { contentRepository.searchSuspend(searchTerm = keywords) } returns Success(
            success = expected
        )

        testObject.search(searchTerm = keywords, listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onSearchSuccess(response = expected) }
    }

    @Test
    fun `search(string) success passes result to livedata`() = runTest {
        init()
        val expected = Success(success = mockk<Map<Int, ArcXPContentElement>>())
        coEvery { contentRepository.searchSuspend(searchTerm = keywords) } returns expected
        testObject.search(searchTerm = keywords)

        coVerify(exactly = 1) { searchLiveData.postValue(expected) }
    }

    @Test
    fun `search(string) failure passes error result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        coEvery { contentRepository.searchSuspend(searchTerm = keywords) } returns Failure(
            failure = expected
        )

        testObject.search(searchTerm = keywords, listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onError(error = expected) }
    }

    @Test
    fun `search(string) removes special characters from string`() = runTest {
        init()
        val query = "keyword1!, keyword2!, keyword3!"
        val expectedModifiedQuery = "keyword1, keyword2, keyword3"
        val expectedResult = mockk<Map<Int, ArcXPContentElement>>()
        val expected = Success(success = expectedResult)
        coEvery { contentRepository.searchSuspend(searchTerm = expectedModifiedQuery) } returns expected
        testObject.search(searchTerm = query, listener = arcxpContentCallback)

        coVerify { searchLiveData.postValue(expected) }
    }

    @Test
    fun `search(string) keeps commas, spaces, and hyphens in keywords`() = runTest {
        init()
        val query = "keyword 1, keyword 2, keyword 3, a-b-c, a b c"
        val expectedResult = mockk<Map<Int, ArcXPContentElement>>()
        val expected = Success(success = expectedResult)
        coEvery { contentRepository.searchSuspend(searchTerm = query) } returns expected
        testObject.search(searchTerm = query, listener = arcxpContentCallback)

        coVerify { searchLiveData.postValue(expected) }
    }

    @Test
    fun `searchSuspend(string) removes special characters from string`() = runTest {
        init()
        val query = "keyword1!, keyword2!, keyword3!"
        val expected = "keyword1, keyword2, keyword3"
        testObject.searchSuspend(searchTerm = query)

        coVerify(exactly = 1) {
            contentRepository.searchSuspend(
                searchTerm = expected
            )
        }
    }

    @Test
    fun `searchSuspend(string) keeps commas, spaces, and hyphens in keywords`() = runTest {
        init()
        val query = "keyword 1, keyword 2, keyword 3, a-b-c, a b c"
        testObject.searchSuspend(searchTerm = query)

        coVerify(exactly = 1) {
            contentRepository.searchSuspend(
                searchTerm = query
            )
        }
    }

    @Test
    fun `searchSuspend(string) returns response from repository`() =
        runTest {
            init()
            val expected = Failure(
                ArcXPException(
                    type = ArcXPSDKErrorType.SERVER_ERROR,
                    message = "our error"
                )
            )
            coEvery {
                contentRepository.searchSuspend(
                    searchTerm = keywords,
                    size = DEFAULT_PAGINATION_SIZE,
                    from = 0
                )
            } returns expected

            val actual = testObject.searchSuspend(searchTerm = keywords)

            assertEquals(expected, actual)
        }

    @Test
    fun `searchSuspend(list) returns response from repository`() =
        runTest {
            init()
            val list = listOf("apples", "baseball", "cats")
            val expectedKeywords = "apples,baseball,cats"
            val expected = Failure(
                ArcXPException(
                    type = ArcXPSDKErrorType.SERVER_ERROR,
                    message = "our error"
                )
            )
            coEvery {
                contentRepository.searchSuspend(
                    searchTerm = expectedKeywords,
                    size = DEFAULT_PAGINATION_SIZE,
                    from = 0
                )
            } returns expected

            val actual = testObject.searchSuspend(searchTerms = list)

            assertEquals(expected, actual)
        }

    @Test
    fun `search(string) failure passes error result to livedata`() = runTest {
        init()
        val expected = Failure(failure = mockk<ArcXPException>())
        coEvery {
            contentRepository.searchSuspend(
                searchTerm = keywords,
                from = any(),
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns expected
        testObject.search(searchTerm = keywords, listener = arcxpContentCallback)

        coVerify(exactly = 1) { searchLiveData.postValue(expected) }
    }

    @Test
    fun `searchVideos(list) success passes result to listener`() = runTest {
        init()
        val query = listOf("keyword1", "keyword2", "keyword3")
        val expectedModifiedQuery = "keyword1,keyword2,keyword3"
        val expected = mockk<Map<Int, ArcXPContentElement>>()

        coEvery {
            contentRepository.searchVideosSuspend(
                searchTerm = expectedModifiedQuery,
                from = any(),
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Success(success = expected)

        testObject.searchVideos(searchTerms = query, listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onSearchSuccess(response = expected) }
    }

    @Test
    fun `searchVideos(list) success passes result to livedata`() = runTest {
        init()
        val query = listOf("keyword1", "keyword2", "keyword3")
        val expectedModifiedQuery = "keyword1,keyword2,keyword3"
        val expected = mockk<Map<Int, ArcXPContentElement>>()
        coEvery {
            contentRepository.searchVideosSuspend(
                searchTerm = expectedModifiedQuery,
                from = any(),
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Success(success = expected)
        testObject.searchVideos(searchTerms = query, listener = arcxpContentCallback)

        coVerify(exactly = 1) { searchLiveData.postValue(Success(expected)) }
    }

    @Test
    fun `searchVideos(list) failure passes error result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        val query = listOf("keyword1", "keyword2", "keyword3")
        val expectedModifiedQuery = "keyword1,keyword2,keyword3"
        coEvery {
            contentRepository.searchVideosSuspend(
                searchTerm = expectedModifiedQuery,
                from = any(),
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Failure(failure = expected)
        testObject.searchVideos(searchTerms = query, listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onError(error = expected) }
    }

    @Test
    fun `searchVideos(list) failure passes error result to livedata`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        val query = listOf("keyword1", "keyword2", "keyword3")
        val expectedModifiedQuery = "keyword1,keyword2,keyword3"
        coEvery {
            contentRepository.searchVideosSuspend(
                searchTerm = expectedModifiedQuery,
                from = any(),
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Failure(failure = expected)
        testObject.searchVideos(searchTerms = query)

        coVerify(exactly = 1) { searchLiveData.postValue(Failure(expected)) }
    }

    @Test
    fun `searchVideos(string) success passes result to listener`() = runTest {
        init()
        val expected = mockk<Map<Int, ArcXPContentElement>>()
        coEvery { contentRepository.searchVideosSuspend(searchTerm = keywords) } returns Success(
            success = expected
        )

        testObject.searchVideos(searchTerm = keywords, listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onSearchSuccess(response = expected) }
    }

    @Test
    fun `searchVideos(string) success passes result to livedata`() = runTest {
        init()
        val expected = Success(success = mockk<Map<Int, ArcXPContentElement>>())
        coEvery { contentRepository.searchVideosSuspend(searchTerm = keywords) } returns expected
        testObject.searchVideos(searchTerm = keywords)

        coVerify(exactly = 1) { searchLiveData.postValue(expected) }
    }

    @Test
    fun `searchVideos(string) failure passes error result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        coEvery { contentRepository.searchVideosSuspend(searchTerm = keywords) } returns Failure(
            failure = expected
        )

        testObject.searchVideos(searchTerm = keywords, listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onError(error = expected) }
    }

    @Test
    fun `searchVideos(string) removes special characters from string`() = runTest {
        init()
        val query = "keyword1!, keyword2!, keyword3!"
        val expectedModifiedQuery = "keyword1, keyword2, keyword3"
        val expectedResult = mockk<Map<Int, ArcXPContentElement>>()
        val expected = Success(success = expectedResult)
        coEvery { contentRepository.searchVideosSuspend(searchTerm = expectedModifiedQuery) } returns expected
        testObject.searchVideos(searchTerm = query, listener = arcxpContentCallback)

        coVerify { searchLiveData.postValue(expected) }
    }

    @Test
    fun `searchVideosSuspend(string) removes special characters from string`() = runTest {
        init()
        val query = "keyword1!, keyword2!, keyword3!"
        val expected = "keyword1, keyword2, keyword3"
        testObject.searchVideosSuspend(searchTerm = query)

        coVerify(exactly = 1) {
            contentRepository.searchVideosSuspend(
                searchTerm = expected
            )
        }
    }

    @Test
    fun `searchVideos(string) keeps commas, spaces, and hyphens in keywords`() = runTest {
        init()
        val query = "keyword 1, keyword 2, keyword 3, a-b-c, a b c"
        val expectedResult = mockk<Map<Int, ArcXPContentElement>>()
        val expected = Success(success = expectedResult)
        coEvery { contentRepository.searchVideosSuspend(searchTerm = query) } returns expected
        testObject.searchVideos(searchTerm = query, listener = arcxpContentCallback)

        coVerify { searchLiveData.postValue(expected) }
    }

    @Test
    fun `searchVideosSuspend(string) keeps commas, spaces, and hyphens in keywords`() =
        runTest {
            init()
            val query = "keyword 1, keyword 2, keyword 3, a-b-c, a b c"
            testObject.searchVideosSuspend(searchTerm = query)

            coVerify(exactly = 1) {
                contentRepository.searchVideosSuspend(
                    searchTerm = query
                )
            }
        }

    @Test
    fun `searchVideosSuspend(string) response from repository`() =
        runTest {
            init()
            val expected = Failure(
                ArcXPException(
                    type = ArcXPSDKErrorType.SERVER_ERROR,
                    message = "our error"
                )
            )
            coEvery {
                contentRepository.searchVideosSuspend(
                    searchTerm = keywords,
                    size = DEFAULT_PAGINATION_SIZE,
                    from = 0
                )
            } returns expected

            val actual = testObject.searchVideosSuspend(searchTerm = keywords)

            assertEquals(expected, actual)
        }

    @Test
    fun `searchVideosSuspend(list) returns response from repository`() =
        runTest {
            init()
            val list = listOf("apples", "baseball", "cats")
            val expectedKeywords = "apples,baseball,cats"
            val expected = Failure(
                ArcXPException(
                    type = ArcXPSDKErrorType.SERVER_ERROR,
                    message = "our error"
                )
            )
            coEvery {
                contentRepository.searchVideosSuspend(
                    searchTerm = expectedKeywords,
                    size = DEFAULT_PAGINATION_SIZE,
                    from = 0
                )
            } returns expected

            val actual = testObject.searchVideosSuspend(searchTerms = list)

            assertEquals(expected, actual)
        }

    @Test
    fun `searchVideos(string) failure passes error result to livedata`() = runTest {
        init()
        val expected = Failure(failure = mockk<ArcXPException>())
        coEvery {
            contentRepository.searchVideosSuspend(
                searchTerm = keywords,
                from = any(),
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns expected
        testObject.searchVideos(searchTerm = keywords, listener = arcxpContentCallback)

        coVerify(exactly = 1) { searchLiveData.postValue(expected) }
    }

    @Test
    fun `getArcXPStory passes shouldIgnoreCache when populated`() = runTest {
        init()
        coEvery {
            contentRepository.getStory(
                uuid = id,
                shouldIgnoreCache = true
            )
        } returns Failure(failure = mockk())

        testObject.getArcXPStory(
            id = id,
            shouldIgnoreCache = true
        )

        coVerify(exactly = 1) {
            contentRepository.getStory(
                uuid = id,
                shouldIgnoreCache = true
            )
        }
    }

    @Test
    fun `getArcXPStory success passes result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPStory> {
            coEvery { type } returns "story"
        }
        coEvery {
            contentRepository.getStory(
                uuid = id,
                shouldIgnoreCache = false
            )
        } returns Success(success = expected)

        testObject.getArcXPStory(
            id = id,
            listener = arcxpContentCallback
        )

        coVerify(exactly = 1) { arcxpContentCallback.onGetStorySuccess(response = expected) }
    }

    @Test
    fun `getArcXPStory success but wrong type passes failure result to listener`() = runTest {
        val expected = mockk<ArcXPException>()
        val message = "Result Type mai tai was not a story"
        coEvery { expected.message } returns message
        coEvery {
            createArcXPException(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = message
            )
        } returns expected
        coEvery {
            application.getString(
                R.string.incorrect_type,
                "mai tai",
                Utils.AnsTypes.STORY.type
            )
        } returns message
        init()
        val expectedResponse = mockk<ArcXPStory> {
            coEvery { type } returns "mai tai"
        }
        coEvery {
            contentRepository.getStory(
                uuid = id,
                shouldIgnoreCache = true
            )
        } returns Success(success = expectedResponse)


        testObject.getArcXPStory(
            id = id,
            listener = arcxpContentCallback,
            shouldIgnoreCache = true
        )

        coVerify(exactly = 1) {
            arcxpContentCallback.onError(error = expected)
        }
    }

    @Test
    fun `getArcXPStory success but wrong type passes failure result to livedata`() = runTest {
        val expected = mockk<ArcXPException>()
        val message = "Result Type taco was not a story"
        coEvery { expected.message } returns message
        coEvery {
            createArcXPException(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = message
            )
        } returns expected
        coEvery {
            application.getString(
                R.string.incorrect_type,
                "taco",
                Utils.AnsTypes.STORY.type
            )
        } returns message
        init()

        val expectedResponse = mockk<ArcXPStory> {
            coEvery { type } returns "taco"
        }
        coEvery {
            contentRepository.getStory(
                uuid = id,
                shouldIgnoreCache = false
            )
        } returns Success(success = expectedResponse)

        testObject.getArcXPStory(
            id = id
        )

        coVerify(exactly = 1) { storyLiveData.postValue(Failure(expected)) }
    }

    @Test
    fun `getArcXPStory success passes result to livedata`() = runTest {
        init()
        val expected = mockk<ArcXPStory> {
            coEvery { type } returns "story"
        }
        coEvery {
            contentRepository.getStory(
                uuid = id,
                shouldIgnoreCache = false
            )
        } returns Success(success = expected)

        testObject.getArcXPStory(id = id)

        coVerify(exactly = 1) { storyLiveData.postValue(Success(expected)) }
    }

    @Test
    fun `getArcXPStory failure passes error result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        coEvery {
            contentRepository.getStory(
                uuid = id,
                shouldIgnoreCache = true
            )
        } returns Failure(failure = expected)

        testObject.getArcXPStory(
            id = id,
            listener = arcxpContentCallback,
            shouldIgnoreCache = true
        )

        coVerify(exactly = 1) { arcxpContentCallback.onError(error = expected) }
    }

    @Test
    fun `getArcXPStory failure passes error result to livedata`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        coEvery {
            contentRepository.getStory(
                uuid = id,
                shouldIgnoreCache = true
            )
        } returns Failure(failure = expected)
        testObject.getArcXPStory(
            id = id,
            shouldIgnoreCache = true
        )

        coVerify(exactly = 1) { storyLiveData.postValue(Failure(expected)) }
    }

    @Test
    fun `getArcXPStorySuspend success passes result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPStory> {
            coEvery { type } returns "story"
        }
        coEvery {
            contentRepository.getStory(
                uuid = id,
                shouldIgnoreCache = false
            )
        } returns Success(success = expected)

        val actual = testObject.getArcXPStorySuspend(
            id = id,
        )

        assertEquals(expected, (actual as Success).success)
    }

    @Test
    fun `getArcXPStorySuspend success but wrong type passes failure result to listener`() =
        runTest {

            val message = "Result Type gallery was not a story"
            coEvery {
                application.getString(
                    R.string.incorrect_type,
                    Utils.AnsTypes.GALLERY.type,
                    Utils.AnsTypes.STORY.type
                )
            } returns message
            init()
            val expectedResponse = mockk<ArcXPStory> {
                coEvery { type } returns "gallery"
            }
            val expected = ArcXPException(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = message
            )
            coEvery {
                contentRepository.getStory(
                    uuid = id,
                    shouldIgnoreCache = true
                )
            } returns Success(success = expectedResponse)


            val actual = testObject.getArcXPStorySuspend(
                id = id,
                shouldIgnoreCache = true
            )

            assertEquals(expected, (actual as Failure).failure)
        }

    @Test
    fun `getArcXPStorySuspend failure returns error`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        coEvery {
            contentRepository.getStory(
                uuid = id,
                shouldIgnoreCache = true
            )
        } returns Failure(failure = expected)

        val actual = testObject.getArcXPStorySuspend(
            id = id,
            shouldIgnoreCache = true
        )

        assertEquals(expected, (actual as Failure).failure)
    }

    @Test
    fun `getGallery success and passes result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPContentElement> {
            coEvery { type } returns "gallery"
        }
        coEvery {
            contentRepository.getContent(
                uuid = id,
                shouldIgnoreCache = false
            )
        } returns Success(success = expected)

        testObject.getGallery(
            id = id,
            listener = arcxpContentCallback
        )

        coVerify(exactly = 1) { arcxpContentCallback.onGetContentSuccess(response = expected) }
    }

    @Test
    fun `getGallery success passes result to livedata`() = runTest {
        init()
        val expected = mockk<ArcXPContentElement> {
            coEvery { type } returns "gallery"
        }
        coEvery {
            contentRepository.getContent(
                uuid = id,
                shouldIgnoreCache = false
            )
        } returns Success(success = expected)

        testObject.getGallery(id = id)


        coVerify(exactly = 1) { contentLiveData.postValue(Success(expected)) }
    }

    @Test
    fun `getGallery failure passes error result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        coEvery {
            contentRepository.getContent(
                uuid = id,
                shouldIgnoreCache = false
            )
        } returns Failure(failure = expected)

        testObject.getGallery(
            id = id,
            listener = arcxpContentCallback
        )

        coVerify(exactly = 1) { arcxpContentCallback.onError(error = expected) }
    }

    @Test
    fun `get content by type when is incorrect type listener`() = runTest {
        val expected = mockk<ArcXPException>()
        val message = "Result Type story was not a gallery"
        coEvery { expected.message } returns message
        coEvery {
            createArcXPException(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = message
            )
        } returns expected
        coEvery {
            application.getString(
                R.string.incorrect_type,
                Utils.AnsTypes.STORY.type,
                Utils.AnsTypes.GALLERY.type
            )
        } returns message
        init()


        val storyJson1 = TestUtils.getJson("story1.json")
        val story1 = MoshiController.fromJson(
            storyJson1,
            ArcXPContentElement::class.java
        )!!
        coEvery {
            contentRepository.getContent(
                uuid = id,
                shouldIgnoreCache = false
            )
        } returns Success(success = story1)

        testObject.getGallery(
            id = id,
            listener = arcxpContentCallback
        )

        coVerify(exactly = 1) { arcxpContentCallback.onError(error = expected) }
    }

    @Test
    fun `get content by type when is incorrect type live data`() = runTest {
        val expected = mockk<ArcXPException>()
        val message = "Result Type story was not a gallery"
        every { expected.message } returns message
        coEvery {
            application.getString(
                R.string.incorrect_type,
                Utils.AnsTypes.STORY.type,
                Utils.AnsTypes.GALLERY.type
            )
        } returns message
        coEvery {
            createArcXPException(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = message
            )
        } returns expected
        init()


        val storyJson1 = TestUtils.getJson("story1.json")
        val story1 = MoshiController.fromJson(
            storyJson1,
            ArcXPContentElement::class.java
        )!!
        coEvery {
            contentRepository.getContent(
                uuid = id,
                shouldIgnoreCache = false
            )
        } returns Success(success = story1)

        testObject.getGallery(
            id = id,
        )
        val slot = slot<Either<ArcXPException, ArcXPContentElement>>()
        coVerify(exactly = 1) { contentLiveData.postValue(capture(slot)) }
        assertEquals(message, (slot.captured as Failure).failure.message)
    }

    @Test
    fun `getGallery failure passes error result to livedata`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        coEvery {
            contentRepository.getContent(
                uuid = id,
                shouldIgnoreCache = false
            )
        } returns Failure(failure = expected)

        testObject.getGallery(id = id)

        coVerify(exactly = 1) { contentLiveData.postValue(Failure(expected)) }
    }

    @Test
    fun `getVideo success and passes result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPContentElement> {
            coEvery { type } returns "video"
        }
        coEvery {
            contentRepository.getContent(
                uuid = id,
                shouldIgnoreCache = false
            )
        } returns Success(success = expected)

        testObject.getVideo(
            id = id,
            listener = arcxpContentCallback
        )

        coVerify(exactly = 1) { arcxpContentCallback.onGetContentSuccess(response = expected) }
    }

    @Test
    fun `getVideo success passes result to livedata`() = runTest {
        init()
        val expected = mockk<ArcXPContentElement> {
            coEvery { type } returns "video"
        }
        coEvery {
            contentRepository.getContent(
                uuid = id,
                shouldIgnoreCache = false
            )
        } returns Success(success = expected)
        testObject.getVideo(id = id)

        coVerify(exactly = 1) { contentLiveData.postValue(Success(expected)) }
    }

    @Test
    fun `getVideo failure passes error result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        coEvery {
            contentRepository.getContent(
                uuid = id,
                shouldIgnoreCache = false
            )
        } returns Failure(failure = expected)
        testObject.getVideo(
            id = id,
            listener = arcxpContentCallback
        )

        coVerify(exactly = 1) { arcxpContentCallback.onError(error = expected) }
    }

    @Test
    fun `getVideo failure passes error result to livedata`() = runTest {
        init()
        val expected = Failure(mockk<ArcXPException>())
        coEvery {
            contentRepository.getContent(
                uuid = id,
                shouldIgnoreCache = false
            )
        } returns expected

        testObject.getVideo(
            id = id
        )

        coVerify(exactly = 1) { contentLiveData.postValue(expected) }
    }

    @Test
    fun `getSectionList success passes result to listener`() = runTest {
        init()
        val expected = listOf(mockk<ArcXPSection>())
        coEvery {
            contentRepository.getSectionList(shouldIgnoreCache = false)
        } returns Success(success = expected)

        testObject.getSectionList(listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onGetSectionsSuccess(response = expected) }
    }

    @Test
    fun `getSectionListAsJson success passes result to listener`() = runTest {
        init()
        val expected = json
        coEvery {
            contentRepository.getSectionListAsJson(shouldIgnoreCache = false)
        } returns Success(success = expected)

        testObject.getSectionListAsJson(listener = arcxpContentCallback, shouldIgnoreCache = false)

        coVerify(exactly = 1) { arcxpContentCallback.onGetJsonSuccess(response = expected) }
    }

    @Test
    fun `getSectionListAsJson failure passes result to listener`() = runTest {
        init()
        val expected = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = sectionsError
        )
        coEvery {
            contentRepository.getSectionListAsJson(shouldIgnoreCache = false)
        } returns Failure(failure = expected)

        testObject.getSectionListAsJson(listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onError(error = expected) }
    }

    @Test
    fun `getSectionListAsJson failure passes result to livedata`() = runTest {
        init()
        val expectedError = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = sectionsError
        )
        val expected = Failure(failure = expectedError)
        coEvery {
            contentRepository.getSectionListAsJson(shouldIgnoreCache = false)
        } returns expected
        coEvery { application.resources } throws Exception()

        testObject.getSectionListAsJson()

        coVerify(exactly = 1) { jsonLiveData.postValue(expected) }
    }

    @Test
    fun `getSectionList success passes result to livedata`() = runTest {
        init()
        val expectedResult = listOf(mockk<ArcXPSection>())
        val expected = Success(success = expectedResult)
        coEvery {
            contentRepository.getSectionList(shouldIgnoreCache = false)
        } returns expected
        testObject.getSectionList()

        coVerify(exactly = 1) { sectionListLiveData.postValue(expected) }
    }

    @Test
    fun `getSectionListAsJson success passes result to livedata`() = runTest {
        init()
        val expectedJson = json
        val expectedResult = Success(success = expectedJson)
        coEvery {
            contentRepository.getSectionListAsJson(shouldIgnoreCache = false)
        } returns expectedResult
        testObject.getSectionListAsJson()

        coVerify(exactly = 1) { jsonLiveData.postValue(expectedResult) }
    }

    @Test
    fun `getSectionList failure passes error result to listener`() = runTest {
        init()
        val expectedResult = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = sectionsError
        )
        val expected = Failure(failure = expectedResult)
        coEvery {
            contentRepository.getSectionList(shouldIgnoreCache = false)
        } returns expected

        coEvery { application.resources } throws Exception()
        testObject.getSectionList(listener = arcxpContentCallback)


        coVerify(exactly = 1) { arcxpContentCallback.onError(error = expectedResult) }
    }

    @Test
    fun `getSectionList failure passes error result to livedata`() = runTest {
        init()
        val expectedResult = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = sectionsError
        )
        val expected = Failure(failure = expectedResult)
        coEvery {
            contentRepository.getSectionList(shouldIgnoreCache = false)
        } returns expected
        testObject.getSectionList()

        coVerify(exactly = 1) { sectionListLiveData.postValue(expected) }
    }

    @Test
    fun `getSectionList passes shouldIgnoreCache when populated`() = runTest {
        init()
        coEvery {
            contentRepository.getSectionList(shouldIgnoreCache = true)
        } returns Failure(failure = mockk())

        testObject.getSectionList(listener = arcxpContentCallback, shouldIgnoreCache = true)

        coVerify(exactly = 1) {
            contentRepository.getSectionList(shouldIgnoreCache = true)
        }
    }

    @Test
    fun `getCollection coerces size when below valid`() = runTest {
        init()
        val expected = HashMap<Int, ArcXPContentElement>()
        coEvery {
            contentRepository.getCollection(
                collectionAlias = id,
                shouldIgnoreCache = false,
                size = Constants.VALID_COLLECTION_SIZE_RANGE.first,
                from = 0
            )
        } returns Success(success = expected)

        testObject.getCollection(id, size = Constants.VALID_COLLECTION_SIZE_RANGE.first - 1)

        coVerify {
            contentRepository.getCollection(
                collectionAlias = id,
                shouldIgnoreCache = any(),
                from = any(),
                size = Constants.VALID_COLLECTION_SIZE_RANGE.first
            )
        }
    }

    @Test
    fun `getCollection coerces size when above valid`() = runTest {
        init()
        val expected = HashMap<Int, ArcXPContentElement>()
        coEvery {
            contentRepository.getCollection(
                collectionAlias = id,
                shouldIgnoreCache = false,
                size = Constants.VALID_COLLECTION_SIZE_RANGE.last,
                from = 0
            )
        } returns Success(success = expected)

        testObject.getCollection(id, size = 21)

        coVerify {
            contentRepository.getCollection(
                collectionAlias = id,
                shouldIgnoreCache = any(),
                from = any(),
                size = Constants.VALID_COLLECTION_SIZE_RANGE.last
            )
        }
    }

    @Test
    fun `getCollectionSuspend returns value from repository`() = runTest {
        init()
        val expected = Failure(
            ArcXPException(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = "our error"
            )
        )
        coEvery {
            contentRepository.getCollection(
                collectionAlias = id,
                shouldIgnoreCache = false,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } returns expected
        val actual = testObject.getCollectionSuspend(collectionAlias = id)

        assertEquals(expected, actual)
    }

    @Test
    fun `getCollectionSuspend coerces size when below valid`() = runTest {
        init()
        testObject.getCollectionSuspend(id, size = Constants.VALID_COLLECTION_SIZE_RANGE.first - 1)
        coVerify {
            contentRepository.getCollection(
                collectionAlias = id,
                shouldIgnoreCache = any(),
                from = any(),
                size = Constants.VALID_COLLECTION_SIZE_RANGE.first
            )
        }
    }

    @Test
    fun `getCollectionSuspend coerces size when above valid`() = runTest {
        init()
        testObject.getCollectionSuspend(collectionAlias = id, size = 21)
        coVerify {
            contentRepository.getCollection(
                collectionAlias = id,
                shouldIgnoreCache = any(),
                from = any(),
                size = Constants.VALID_COLLECTION_SIZE_RANGE.last
            )
        }
    }

    @Test
    fun `getVideoCollectionSuspend returns value from repository`() = runTest {
        init()
        val expectedVideoCollectionName = "video"
        coEvery { contentConfig.videoCollectionName } returns expectedVideoCollectionName
        val expected = Failure(
            ArcXPException(
                type = ArcXPSDKErrorType.SERVER_ERROR,
                message = "our error"
            )
        )
        coEvery {
            contentRepository.getCollection(
                collectionAlias = expectedVideoCollectionName,
                shouldIgnoreCache = false,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } returns expected
        val actual = testObject.getVideoCollectionSuspend()

        assertEquals(expected, actual)
    }

    @Test
    fun `getVideoCollectionSuspend coerces size when below valid`() = runTest {
        init()
        val expectedVideoCollectionName = "video"
        coEvery { contentConfig.videoCollectionName } returns expectedVideoCollectionName
        testObject.getVideoCollectionSuspend(size = Constants.VALID_COLLECTION_SIZE_RANGE.first - 1)
        coVerify {
            contentRepository.getCollection(
                collectionAlias = expectedVideoCollectionName,
                shouldIgnoreCache = any(),
                from = any(),
                size = Constants.VALID_COLLECTION_SIZE_RANGE.first
            )
        }
    }

    @Test
    fun `getVideoCollectionSuspend coerces size when above valid`() = runTest {
        init()
        val expectedVideoCollectionName = "video"
        coEvery { contentConfig.videoCollectionName } returns expectedVideoCollectionName
        testObject.getVideoCollectionSuspend(size = 21)
        coVerify {
            contentRepository.getCollection(
                collectionAlias = expectedVideoCollectionName,
                shouldIgnoreCache = any(),
                from = any(),
                size = Constants.VALID_COLLECTION_SIZE_RANGE.last
            )
        }
    }

    @Test
    fun `search coerces size when below valid`() = runTest {
        init()
        val expectedResult = mockk<Map<Int, ArcXPContentElement>>()
        val expected = Success(success = expectedResult)
        coEvery {
            contentRepository.searchSuspend(
                searchTerm = keywords,
                from = any(),
                size = Constants.VALID_COLLECTION_SIZE_RANGE.first
            )
        } returns expected

        testObject.search(
            searchTerm = keywords,
            size = Constants.VALID_COLLECTION_SIZE_RANGE.first - 1
        )

        coVerify { searchLiveData.postValue(expected) }
    }

    @Test
    fun `search coerces size when above valid`() = runTest {
        init()
        val expectedResult = mockk<Map<Int, ArcXPContentElement>>()
        val expected = Success(success = expectedResult)
        coEvery {
            contentRepository.searchSuspend(
                searchTerm = keywords,
                size = Constants.VALID_COLLECTION_SIZE_RANGE.last
            )
        } returns expected
        testObject.search(
            searchTerm = keywords,
            size = Constants.VALID_COLLECTION_SIZE_RANGE.last + 1
        )

        coVerify { searchLiveData.postValue(expected) }
    }

    @Test
    fun `getSectionListAsJsonSuspend returns repo result`() = runTest {
        init()
        val expected = Success(success = json)
        coEvery { contentRepository.getSectionListAsJson(shouldIgnoreCache = false) } returns expected

        val actual = testObject.getSectionListAsJsonSuspend()

        assertEquals(expected, actual)

    }

    @Test
    fun `getContentAsJsonSuspend returns successful repo result`() = runTest {
        init()
        val expected = Success(success = json)
        coEvery { contentRepository.getContentAsJson(uuid = id, shouldIgnoreCache = false) } returns expected

        val actual = testObject.getContentAsJsonSuspend(id = id)

        assertEquals(expected, actual)
    }

    @Test
    fun `getContentAsJson returns failing repo result to livedata`() = runTest {
        init()
        val error = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = "our error message"
        )
        val expected = Failure(failure = error)
        coEvery { contentRepository.getContentAsJson(uuid = id, shouldIgnoreCache = false) } returns expected

        testObject.getContentAsJson(id = id)

        coVerify(exactly = 1) {
            jsonLiveData.postValue(expected)
        }
    }

    @Test
    fun `getContentAsJson returns failing repo result to listener`() = runTest {
        init()
        val error = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = "our error message"
        )
        val expectedResponse = Failure(failure = error)

        coEvery { contentRepository.getContentAsJson(uuid = id, shouldIgnoreCache = false) } returns expectedResponse

        testObject.getContentAsJson(id = id, shouldIgnoreCache = false, listener = arcxpContentCallback)

        coVerify(exactly = 1) {
            arcxpContentCallback.onError(error)
        }
    }

    @Test
    fun `getContentAsJson returns livedata and posts repo result through livedata`() = runTest {
        init()
        val expectedResponse = Success(success = json)
        coEvery { contentRepository.getContentAsJson(uuid = id, shouldIgnoreCache = false) } returns expectedResponse

        testObject.getContentAsJson(id = id, shouldIgnoreCache = false)

        coVerify(exactly = 1) { jsonLiveData.postValue(expectedResponse) }
    }

    @Test
    fun `getContentAsJson returns repo result through listener`() = runTest {
        init()
        val expectedResponse = Success(success = json)
        coEvery { contentRepository.getContentAsJson(uuid = id, shouldIgnoreCache = false) } returns expectedResponse

        testObject.getContentAsJson(id = id, shouldIgnoreCache = false, listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onGetJsonSuccess(response = json) }
    }

    @Test
    fun `getCollectionAsJsonSuspend returns repo result`() = runTest {
        init()
        val expected = Success(success = json)
        coEvery {
            contentRepository.getCollectionAsJson(
                collectionAlias = id,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns expected

        val actual = testObject.getCollectionAsJsonSuspend(collectionAlias = "/$id")

        assertEquals(expected, actual)

    }

    @Test
    fun `constructor defaults & livedata getters for coverage`() {
        testObject =
            ArcXPContentManager(
                contentRepository = contentRepository,
                application = application,
                arcXPAnalyticsManager = arcXPAnalyticsManager,
                contentConfig = contentConfig,
            )
        assertNotNull(testObject.contentLiveData)
        assertNotNull(testObject.storyLiveData)
        assertNotNull(testObject.collectionLiveData)
        assertNotNull(testObject.sectionListLiveData)
        assertNotNull(testObject.searchLiveData)
        assertNotNull(testObject.jsonLiveData)
    }

    @Test
    fun `delete collection calls cache manager`() = runTest {
        testObject.deleteCollection(collectionAlias = "alias")
        coVerifySequence {
            contentRepository.deleteCollection(collectionAlias = "alias")
        }
    }

    @Test
    fun `delete item calls cache manager`() = runTest {
        testObject.deleteItem(id = "id")
        coVerifySequence {
            contentRepository.deleteItem(uuid = "id")
        }
    }

    @Test
    fun `delete cache calls cache manager`() = runTest {
        testObject.clearCache()
        coVerifySequence {
            contentRepository.deleteCache()
        }
    }
}
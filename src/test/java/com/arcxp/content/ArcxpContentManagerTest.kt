package com.arcxp.content

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.arcxp.ArcXPMobileSDK
import com.arcxp.ArcXPMobileSDK.analytics
import com.arcxp.ArcXPMobileSDK.application
import com.arcxp.ArcXPMobileSDK.contentConfig
import com.arcxp.commons.analytics.ArcXPAnalyticsManager
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.*
import com.arcxp.commons.util.Constants.DEFAULT_PAGINATION_SIZE
import com.arcxp.commons.util.DependencyFactory.createBuildVersionProvider
import com.arcxp.content.extendedModels.ArcXPCollection
import com.arcxp.content.extendedModels.ArcXPContentElement
import com.arcxp.content.extendedModels.ArcXPStory
import com.arcxp.content.models.ArcXPContentCallback
import com.arcxp.content.models.ArcXPSection
import com.arcxp.content.repositories.ContentRepository
import com.arcxp.content.util.AuthManager
import com.arcxp.sdk.R
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*


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
    lateinit var shared: SharedPreferences

    @RelaxedMockK
    lateinit var contentConfig: ArcXPContentConfig

    @RelaxedMockK
    lateinit var sharedEditor: SharedPreferences.Editor

    @RelaxedMockK
    lateinit var arcXPAnalyticsManager: ArcXPAnalyticsManager

    private val preference = "analytics"
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
                contentConfig = contentConfig
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
        coEvery {
            application.getSharedPreferences(
                preference,
                Context.MODE_PRIVATE
            )
        } returns shared
        coEvery {
            application.getSharedPreferences(
                "analytics",
                Context.MODE_PRIVATE
            )
        } returns shared
        coEvery { application.getString(R.string.section_load_failure) } returns sectionsError
        coEvery { shared.edit() } returns sharedEditor
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
//        init()
        verify(exactly = 1) { AuthManager.accessToken = "bearer token" }
    }

    @Test
    fun `getCollection success passes result to listener`() = runTest {
        init()
        val expected = mockk<HashMap<Int, ArcXPCollection>>()
        coEvery {
            contentRepository.getCollection(
                id = id,
                shouldIgnoreCache = false,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } returns Success(success = expected)

        testObject.getCollection(id = id, listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onGetCollectionSuccess(response = expected) }
    }

    @Test
    fun `getCollectionAsJson success passes result to listener`() = runTest {
        init()
        val expected = Success(success = json)
        coEvery {
            contentRepository.getCollectionAsJson(
                id = id,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns expected

        testObject.getCollectionAsJson(id = id, listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onGetJsonSuccess(response = json) }
    }

    @Test
    fun `getCollection success passes result to livedata`() = runTest {
        init()
        val expected = HashMap<Int, ArcXPCollection>()
        coEvery {
            contentRepository.getCollection(
                id = id,
                shouldIgnoreCache = false,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } returns Success(success = expected)
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPCollection>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPCollection>>>() } returns mockStream

        testObject.getCollection(id = id)

        coVerify(exactly = 1) { mockStream.postValue(Success(expected)) }
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
            contentRepository.getContent(id = id, shouldIgnoreCache = false)
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
                id = id,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns expected
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, String>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, String>>() } returns mockStream

        testObject.getCollectionAsJson(id = id)

        coVerify(exactly = 1) { mockStream.postValue(expected) }
    }

    @Test
    fun `getCollection passes shouldIgnoreCache when populated`() = runTest {
        init()
        val expectedResult = HashMap<Int, ArcXPCollection>()
        val expected = Success(success = expectedResult)
        coEvery {
            contentRepository.getCollection(
                id = id,
                shouldIgnoreCache = true,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } returns expected
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPCollection>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPCollection>>>() } returns mockStream

        testObject.getCollection(
            id = id,
            listener = arcxpContentCallback,
            shouldIgnoreCache = true
        )

        coVerify {
            mockStream.postValue(expected)
        }
    }

    @Test
    fun `getCollection failure passes error result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        coEvery {
            contentRepository.getCollection(
                id = id,
                shouldIgnoreCache = false,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } returns Failure(failure = expected)

        testObject.getCollection(id = id, listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onError(error = expected) }
    }

    @Test
    fun `getCollectionAsJson failure passes error result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        coEvery {
            contentRepository.getCollectionAsJson(
                id = id,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Failure(failure = expected)

        testObject.getCollectionAsJson(id = id, listener = arcxpContentCallback)

        coVerify(exactly = 1) { arcxpContentCallback.onError(error = expected) }
    }

    @Test
    fun `getCollection failure passes error result to livedata`() = runTest {
        init()
        val expectedError = mockk<ArcXPException>()
        val expected = Failure(failure = expectedError)
        coEvery {
            contentRepository.getCollection(
                id = id,
                shouldIgnoreCache = false,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } returns expected
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPCollection>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPCollection>>>() } returns mockStream

        testObject.getCollection(id = id)

        coVerify(exactly = 1) { mockStream.postValue(expected) }
    }

    @Test
    fun `getCollectionAsJson failure passes error result to livedata`() = runTest {
        init()
        val expectedError = mockk<ArcXPException>()
        val expected = Failure(failure = expectedError)
        coEvery {
            contentRepository.getCollectionAsJson(
                id = id,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns expected
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, String>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, String>>() } returns mockStream

        testObject.getCollectionAsJson(id = id)

        coVerify(exactly = 1) { mockStream.postValue(expected) }
    }

    @Test
    fun `getVideoCollection calls getCollection with video collection name`() = runTest {
        init()
        val expectedVideoCollectionName = "video"
        coEvery { contentConfig.videoCollectionName } returns expectedVideoCollectionName
        coEvery {
            contentRepository.getCollection(
                id = expectedVideoCollectionName,
                shouldIgnoreCache = true,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Failure(failure = mockk())
        testObject = spyk(testObject)

        testObject.getVideoCollection(shouldIgnoreCache = true)

        coVerify(exactly = 1) {
            testObject.getCollection(
                id = expectedVideoCollectionName,
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
                id = expectedVideoCollectionName,
                shouldIgnoreCache = false,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Failure(failure = mockk())
        testObject = spyk(testObject)

        testObject.getVideoCollection()

        coVerify(exactly = 1) {
            testObject.getCollection(
                id = expectedVideoCollectionName,
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
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream


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
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

        testObject.search(searchTerms = query, listener = arcxpContentCallback)

        coVerify(exactly = 1) { mockStream.postValue(Success(expected)) }
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
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

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
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

        testObject.search(searchTerms = query)

        coVerify(exactly = 1) { mockStream.postValue(Failure(expected)) }
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
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

        testObject.search(searchTerm = keywords)

        coVerify(exactly = 1) { mockStream.postValue(expected) }
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
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

        testObject.search(searchTerm = query, listener = arcxpContentCallback)

        coVerify { mockStream.postValue(expected) }
    }

    @Test
    fun `search(string) keeps commas, spaces, and hyphens in keywords`() = runTest {
        init()
        val query = "keyword 1, keyword 2, keyword 3, a-b-c, a b c"
        val expectedResult = mockk<Map<Int, ArcXPContentElement>>()
        val expected = Success(success = expectedResult)
        coEvery { contentRepository.searchSuspend(searchTerm = query) } returns expected
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

        testObject.search(searchTerm = query, listener = arcxpContentCallback)

        coVerify { mockStream.postValue(expected) }
    }

    @Test
    fun `searchSuspend(string) removes special characters from string`() = runTest {
        init()
        val query = "keyword1!, keyword2!, keyword3!"
        val expected = "keyword1, keyword2, keyword3"
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

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
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

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
    fun `searchCollectionSuspend(string) removes special characters from string`() = runTest {
        init()
        val query = "keyword1!, keyword2!, keyword3!"
        val expected = "keyword1, keyword2, keyword3"
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

        testObject.searchCollectionSuspend(searchTerm = query)

        coVerify(exactly = 1) {
            contentRepository.searchCollectionSuspend(
                searchTerm = expected
            )
        }
    }

    @Test
    fun `searchCollectionSuspend(string) keeps commas, spaces, and hyphens in keywords`() = runTest {
        init()
        val query = "keyword 1, keyword 2, keyword 3, a-b-c, a b c"
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

        testObject.searchCollectionSuspend(searchTerm = query)

        coVerify(exactly = 1) {
            contentRepository.searchCollectionSuspend(
                searchTerm = query
            )
        }
    }

    @Test
    fun `searchCollectionSuspend(string) returns response from repository`() =
        runTest {
            init()
            val expected = Failure(
                ArcXPException(
                    type = ArcXPSDKErrorType.SERVER_ERROR,
                    message = "our error"
                )
            )
            coEvery {
                contentRepository.searchCollectionSuspend(
                    searchTerm = keywords,
                    size = DEFAULT_PAGINATION_SIZE,
                    from = 0
                )
            } returns expected

            val actual = testObject.searchCollectionSuspend(searchTerm = keywords)

            assertEquals(expected, actual)
        }

    @Test
    fun `searchCollectionSuspend(list) returns response from repository`() =
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
                contentRepository.searchCollectionSuspend(
                    searchTerm = expectedKeywords,
                    size = DEFAULT_PAGINATION_SIZE,
                    from = 0
                )
            } returns expected

            val actual = testObject.searchCollectionSuspend(searchTerms = list)

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
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

        testObject.search(searchTerm = keywords, listener = arcxpContentCallback)

        coVerify(exactly = 1) { mockStream.postValue(expected) }
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
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream


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
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

        testObject.searchVideos(searchTerms = query, listener = arcxpContentCallback)

        coVerify(exactly = 1) { mockStream.postValue(Success(expected)) }
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
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

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
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

        testObject.searchVideos(searchTerms = query)

        coVerify(exactly = 1) { mockStream.postValue(Failure(expected)) }
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
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

        testObject.searchVideos(searchTerm = keywords)

        coVerify(exactly = 1) { mockStream.postValue(expected) }
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
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

        testObject.searchVideos(searchTerm = query, listener = arcxpContentCallback)

        coVerify { mockStream.postValue(expected) }
    }

    @Test
    fun `searchVideosSuspend(string) removes special characters from string`() = runTest {
        init()
        val query = "keyword1!, keyword2!, keyword3!"
        val expected = "keyword1, keyword2, keyword3"
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

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
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

        testObject.searchVideos(searchTerm = query, listener = arcxpContentCallback)

        coVerify { mockStream.postValue(expected) }
    }

    @Test
    fun `searchVideosSuspend(string) keeps commas, spaces, and hyphens in keywords`() =
        runTest {
            init()
            val query = "keyword 1, keyword 2, keyword 3, a-b-c, a b c"
            val mockStream =
                mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                    relaxUnitFun = true
                )
            coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

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
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

        testObject.searchVideos(searchTerm = keywords, listener = arcxpContentCallback)

        coVerify(exactly = 1) { mockStream.postValue(expected) }
    }


    @Test
    fun `getStory passes shouldIgnoreCache when populated`() = runTest {
        init()
        coEvery {
            contentRepository.getContent(
                id = id,
                shouldIgnoreCache = true
            )
        } returns Failure(failure = mockk())

        testObject.getStory(
            id = id,
            shouldIgnoreCache = true
        )

        coVerify(exactly = 1) {
            contentRepository.getContent(
                id = id,
                shouldIgnoreCache = true
            )
        }
    }

    @Test
    fun `getStory success passes result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPContentElement> {
            coEvery { type } returns "story"
        }
        coEvery {
            contentRepository.getContent(
                id = id,
                shouldIgnoreCache = false
            )
        } returns Success(success = expected)

        testObject.getStory(
            id = id,
            listener = arcxpContentCallback
        )

        coVerify(exactly = 1) { arcxpContentCallback.onGetContentSuccess(response = expected) }
    }

    @Test
    fun `getStory success but wrong type passes failure result to listener`() = runTest {
        init()
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, ArcXPContentElement>>>(relaxUnitFun = true)
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, ArcXPContentElement>>() } returns mockStream

        val expectedResponse = mockk<ArcXPContentElement> {
            coEvery { type } returns "not a story"
        }
        val expected = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = "Result did not match the given type: story"
        )
        coEvery {
            contentRepository.getContent(
                id = id,
                shouldIgnoreCache = true
            )
        } returns Success(success = expectedResponse)


        testObject.getStory(
            id = id,
            listener = arcxpContentCallback,
            shouldIgnoreCache = true
        )

        coVerify(exactly = 1) {
            arcxpContentCallback.onError(error = expected)
        }
    }

    @Test
    fun `getStory success but wrong type passes failure result to livedata`() = runTest {
        init()
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, ArcXPContentElement>>>(relaxUnitFun = true)
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, ArcXPContentElement>>() } returns mockStream

        val expectedResponse = mockk<ArcXPContentElement> {
            coEvery { type } returns "not a story"
        }
        val expected = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = "Result did not match the given type: story"
        )
        coEvery {
            contentRepository.getContent(
                id = id,
                shouldIgnoreCache = false
            )
        } returns Success(success = expectedResponse)

        testObject.getStory(
            id = id
        )

        coVerify(exactly = 1) { mockStream.postValue(Failure(expected)) }
    }

    @Test
    fun `getStory success passes result to livedata`() = runTest {
        init()
        val expected = mockk<ArcXPContentElement> {
            coEvery { type } returns "story"
        }
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, ArcXPContentElement>>>(relaxUnitFun = true)
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, ArcXPContentElement>>() } returns mockStream

        coEvery {
            contentRepository.getContent(
                id = id,
                shouldIgnoreCache = false
            )
        } returns Success(success = expected)

        testObject.getStory(id = id)

        coVerify(exactly = 1) { mockStream.postValue(Success(expected)) }
    }

    @Test
    fun `getStory failure passes error result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        coEvery {
            contentRepository.getContent(
                id = id,
                shouldIgnoreCache = true
            )
        } returns Failure(failure = expected)

        testObject.getStory(
            id = id,
            listener = arcxpContentCallback,
            shouldIgnoreCache = true
        )

        coVerify(exactly = 1) { arcxpContentCallback.onError(error = expected) }
    }

    @Test
    fun `getStory failure passes error result to livedata`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        coEvery {
            contentRepository.getContent(
                id = id,
                shouldIgnoreCache = true
            )
        } returns Failure(failure = expected)
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, ArcXPContentElement>>>(relaxUnitFun = true)
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, ArcXPContentElement>>() } returns mockStream

        testObject.getStory(
            id = id,
            shouldIgnoreCache = true
        )

        coVerify(exactly = 1) { mockStream.postValue(Failure(expected)) }
    }

    @Test
    fun `getArcXPStory passes shouldIgnoreCache when populated`() = runTest {
        init()
        coEvery {
            contentRepository.getStory(
                id = id,
                shouldIgnoreCache = true
            )
        } returns Failure(failure = mockk())

        testObject.getArcXPStory(
            id = id,
            shouldIgnoreCache = true
        )

        coVerify(exactly = 1) {
            contentRepository.getStory(
                id = id,
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
                id = id,
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
        init()
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, ArcXPStory>>>(relaxUnitFun = true)
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, ArcXPStory>>() } returns mockStream

        val expectedResponse = mockk<ArcXPStory> {
            coEvery { type } returns "not a story"
        }
        val expected = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = "Result was not a story"
        )
        coEvery {
            contentRepository.getStory(
                id = id,
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
        init()
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, ArcXPStory>>>(relaxUnitFun = true)
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, ArcXPStory>>() } returns mockStream

        val expectedResponse = mockk<ArcXPStory> {
            coEvery { type } returns "not a story"
        }
        val expected = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = "Result was not a story"
        )
        coEvery {
            contentRepository.getStory(
                id = id,
                shouldIgnoreCache = false
            )
        } returns Success(success = expectedResponse)

        testObject.getArcXPStory(
            id = id
        )

        coVerify(exactly = 1) { mockStream.postValue(Failure(expected)) }
    }

    @Test
    fun `getArcXPStory success passes result to livedata`() = runTest {
        init()
        val expected = mockk<ArcXPStory> {
            coEvery { type } returns "story"
        }
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, ArcXPStory>>>(relaxUnitFun = true)
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, ArcXPStory>>() } returns mockStream

        coEvery {
            contentRepository.getStory(
                id = id,
                shouldIgnoreCache = false
            )
        } returns Success(success = expected)

        testObject.getArcXPStory(id = id)

        coVerify(exactly = 1) { mockStream.postValue(Success(expected)) }
    }

    @Test
    fun `getArcXPStory failure passes error result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        coEvery {
            contentRepository.getStory(
                id = id,
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
                id = id,
                shouldIgnoreCache = true
            )
        } returns Failure(failure = expected)
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, ArcXPStory>>>(relaxUnitFun = true)
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, ArcXPStory>>() } returns mockStream

        testObject.getArcXPStory(
            id = id,
            shouldIgnoreCache = true
        )

        coVerify(exactly = 1) { mockStream.postValue(Failure(expected)) }
    }

    @Test
    fun `getGallery success and passes result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPContentElement> {
            coEvery { type } returns "gallery"
        }
        coEvery {
            contentRepository.getContent(
                id = id,
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
                id = id,
                shouldIgnoreCache = false
            )
        } returns Success(success = expected)
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, ArcXPContentElement>>>(relaxUnitFun = true)
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, ArcXPContentElement>>() } returns mockStream

        testObject.getGallery(id = id)


        coVerify(exactly = 1) { mockStream.postValue(Success(expected)) }
    }

    @Test
    fun `getGallery failure passes error result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        coEvery {
            contentRepository.getContent(
                id = id,
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
    fun `getGallery failure passes error result to livedata`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        coEvery {
            contentRepository.getContent(
                id = id,
                shouldIgnoreCache = false
            )
        } returns Failure(failure = expected)

        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, ArcXPContentElement>>>(relaxUnitFun = true)
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, ArcXPContentElement>>() } returns mockStream

        testObject.getGallery(id = id)

        coVerify(exactly = 1) { mockStream.postValue(Failure(expected)) }
    }

    @Test
    fun `getVideo success and passes result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPContentElement> {
            coEvery { type } returns "video"
        }
        coEvery {
            contentRepository.getContent(
                id = id,
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
                id = id,
                shouldIgnoreCache = false
            )
        } returns Success(success = expected)
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, ArcXPContentElement>>>(relaxUnitFun = true)
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, ArcXPContentElement>>() } returns mockStream

        testObject.getVideo(id = id)

        coVerify(exactly = 1) { mockStream.postValue(Success(expected)) }
    }

    @Test
    fun `getVideo failure passes error result to listener`() = runTest {
        init()
        val expected = mockk<ArcXPException>()
        coEvery {
            contentRepository.getContent(
                id = id,
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
        val expected = mockk<ArcXPException>()
        coEvery {
            contentRepository.getContent(
                id = id,
                shouldIgnoreCache = false
            )
        } returns Failure(failure = expected)
        val mockErrorStream =
            mockk<MutableLiveData<Either<ArcXPException, ArcXPContentElement>>>(relaxUnitFun = true)
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, ArcXPContentElement>>() } returns mockErrorStream

        testObject.getVideo(
            id = id
        )
        coVerify(exactly = 1) { mockErrorStream.postValue(Failure(expected)) }
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
            contentRepository.getSectionListAsJson()
        } returns Success(success = expected)

        testObject.getSectionListAsJson(listener = arcxpContentCallback)

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
            contentRepository.getSectionListAsJson()
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
            contentRepository.getSectionListAsJson()
        } returns expected
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, List<ArcXPSection>>>>(relaxUnitFun = true)
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, List<ArcXPSection>>>() } returns mockStream
        coEvery { application.resources } throws Exception()

        testObject.getSectionListAsJson()

        coVerify(exactly = 1) { mockStream.postValue(expected) }
    }

    @Test
    fun `getSectionList success passes result to livedata`() = runTest {
        init()
        val expectedResult = listOf(mockk<ArcXPSection>())
        val expected = Success(success = expectedResult)
        coEvery {
            contentRepository.getSectionList(shouldIgnoreCache = false)
        } returns expected
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, List<ArcXPSection>>>>(relaxUnitFun = true)
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, List<ArcXPSection>>>() } returns mockStream

        testObject.getSectionList()

        coVerify(exactly = 1) { mockStream.postValue(expected) }
    }

    @Test
    fun `getSectionListAsJson success passes result to livedata`() = runTest {
        init()
        val expectedJson = json
        val expectedResult = Success(success = expectedJson)
        coEvery {
            contentRepository.getSectionListAsJson()
        } returns expectedResult
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, String>>>(relaxUnitFun = true)
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, String>>() } returns mockStream

        testObject.getSectionListAsJson()

        coVerify(exactly = 1) { mockStream.postValue(expectedResult) }
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
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, List<ArcXPSection>>>>(relaxUnitFun = true)
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, List<ArcXPSection>>>() } returns mockStream

        testObject.getSectionList()

        coVerify(exactly = 1) { mockStream.postValue(expected) }
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
        val expected = HashMap<Int, ArcXPCollection>()
        coEvery {
            contentRepository.getCollection(
                id = id,
                shouldIgnoreCache = false,
                size = Constants.VALID_COLLECTION_SIZE_RANGE.first,
                from = 0
            )
        } returns Success(success = expected)

        testObject.getCollection(id, size = Constants.VALID_COLLECTION_SIZE_RANGE.first - 1)

        coVerify {
            contentRepository.getCollection(
                id = id,
                shouldIgnoreCache = any(),
                from = any(),
                size = Constants.VALID_COLLECTION_SIZE_RANGE.first
            )
        }
    }

    @Test
    fun `getCollection coerces size when above valid`() = runTest {
        init()
        val expected = HashMap<Int, ArcXPCollection>()
        coEvery {
            contentRepository.getCollection(
                id = id,
                shouldIgnoreCache = false,
                size = Constants.VALID_COLLECTION_SIZE_RANGE.last,
                from = 0
            )
        } returns Success(success = expected)

        testObject.getCollection(id, size = 21)

        coVerify {
            contentRepository.getCollection(
                id = id,
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
                id = id,
                shouldIgnoreCache = false,
                size = DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } returns expected
        val actual = testObject.getCollectionSuspend(id = id)

        assertEquals(expected, actual)
    }

    @Test
    fun `getCollectionSuspend coerces size when below valid`() = runTest {
        init()
        testObject.getCollectionSuspend(id, size = Constants.VALID_COLLECTION_SIZE_RANGE.first - 1)
        coVerify {
            contentRepository.getCollection(
                id = id,
                shouldIgnoreCache = any(),
                from = any(),
                size = Constants.VALID_COLLECTION_SIZE_RANGE.first
            )
        }
    }

    @Test
    fun `getCollectionSuspend coerces size when above valid`() = runTest {
        init()
        testObject.getCollectionSuspend(id = id, size = 21)
        coVerify {
            contentRepository.getCollection(
                id = id,
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
                id = expectedVideoCollectionName,
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
                id = expectedVideoCollectionName,
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
                id = expectedVideoCollectionName,
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
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream


        testObject.search(
            searchTerm = keywords,
            size = Constants.VALID_COLLECTION_SIZE_RANGE.first - 1
        )

        coVerify { mockStream.postValue(expected) }
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
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, Map<Int, ArcXPContentElement>>>() } returns mockStream

        testObject.search(
            searchTerm = keywords,
            size = Constants.VALID_COLLECTION_SIZE_RANGE.last + 1
        )

        coVerify { mockStream.postValue(expected) }
    }

    @Test
    fun `getSectionListAsJsonSuspend returns repo result`() = runTest {
        init()
        val expected = Success(success = json)
        coEvery { contentRepository.getSectionListAsJson() } returns expected

        val actual = testObject.getSectionListAsJsonSuspend()

        assertEquals(expected, actual)

    }

    @Test
    fun `getContentAsJsonSuspend returns successful repo result`() = runTest {
        init()
        val expected = Success(success = json)
        coEvery { contentRepository.getContentAsJson(id = id) } returns expected

        val actual = testObject.getContentAsJsonSuspend(id = id)

        assertEquals(expected, actual)
    }

    @Test
    fun `getContentAsJson returns failing repo result to livedata`() = runTest {
        init()
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, String>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, String>>() } returns mockStream
        val error = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = "our error message"
        )
        val expected = Failure(failure = error)
        coEvery { contentRepository.getContentAsJson(id = id) } returns expected

        testObject.getContentAsJson(id = id)

        coVerify(exactly = 1) {
            mockStream.postValue(expected)
        }
    }

    @Test
    fun `getContentAsJson returns failing repo result to listener`() = runTest {
        init()
        val mockStream =
            mockk<MutableLiveData<Either<ArcXPException, String>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, String>>() } returns mockStream

        val error = ArcXPException(
            type = ArcXPSDKErrorType.SERVER_ERROR,
            message = "our error message"
        )
        val expectedResponse = Failure(failure = error)

        coEvery { contentRepository.getContentAsJson(id = id) } returns expectedResponse

        testObject.getContentAsJson(id = id, listener = arcxpContentCallback)

        coVerify(exactly = 1) {
            arcxpContentCallback.onError(error)
        }
    }

    @Test
    fun `getContentAsJson returns livedata and posts repo result through livedata`() = runTest {
        init()
        val expected =
            mockk<MutableLiveData<Either<ArcXPException, String>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, String>>() } returns expected

        val expectedResponse = Success(success = json)
        coEvery { contentRepository.getContentAsJson(id = id) } returns expectedResponse

        val actual = testObject.getContentAsJson(id = id)

        assertEquals(expected, actual)
        coVerify(exactly = 1) { expected.postValue(expectedResponse) }
    }

    @Test
    fun `getContentAsJson returns repo result through listener`() = runTest {
        init()
        val expected =
            mockk<MutableLiveData<Either<ArcXPException, String>>>(
                relaxUnitFun = true
            )
        coEvery { DependencyFactory.createLiveData<Either<ArcXPException, String>>() } returns expected

        val expectedResponse = Success(success = json)
        coEvery { contentRepository.getContentAsJson(id = id) } returns expectedResponse

        val actual = testObject.getContentAsJson(id = id, listener = arcxpContentCallback)

        assertEquals(expected, actual)
        coVerify(exactly = 1) { arcxpContentCallback.onGetJsonSuccess(response = json) }
    }

    @Test
    fun `getCollectionAsJsonSuspend returns repo result`() = runTest {
        init()
        val expected = Success(success = json)
        coEvery {
            contentRepository.getCollectionAsJson(
                id = id,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns expected

        val actual = testObject.getCollectionAsJsonSuspend(id = id)

        assertEquals(expected, actual)

    }

    @Test
    fun `searchByKeyword calls through to search`() = runTest {
        init()
        val searchTerm = "search term"
        coEvery {
            contentRepository.searchSuspend(
                searchTerm = searchTerm,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Failure(failure = mockk())
        testObject = spyk(testObject)

        testObject.searchByKeyword(keyword = searchTerm)

        coVerify {
            testObject.search(searchTerm = searchTerm)
        }

    }

    @Test
    fun `searchByKeywords calls through to search`() = runTest {
        init()
        val searchTerms = listOf("apple", "banana", "carrot")
        coEvery {
            contentRepository.searchSuspend(
                searchTerm = "apple,banana,carrot",
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Failure(failure = mockk())
        testObject = spyk(testObject)

        testObject.searchByKeywords(keywords = searchTerms)

        coVerify {
            testObject.search(searchTerms = searchTerms)
        }
    }

    @Test
    fun `searchByKeywordsSuspend calls through to searchSuspend`() = runTest {
        init()
        val searchTerms = listOf("apple", "banana", "carrot")
        val expectedReformat = "apple,banana,carrot"
        coEvery {
            contentRepository.searchSuspend(
                searchTerm = expectedReformat,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Failure(failure = mockk())
        testObject = spyk(testObject)

        testObject.searchByKeywordsSuspend(keywords = searchTerms)

        coVerify {
            testObject.searchSuspend(searchTerm = expectedReformat)
        }
    }

    @Test
    fun `searchByKeywordSuspend calls through to searchSuspend`() = runTest {
        init()
        val searchTerm = "search term"
        coEvery {
            contentRepository.searchSuspend(
                searchTerm = searchTerm,
                from = 0,
                size = DEFAULT_PAGINATION_SIZE
            )
        } returns Failure(failure = mockk())
        testObject = spyk(testObject)

        testObject.searchByKeywordSuspend(keyword = searchTerm)

        coVerify {
            testObject.searchSuspend(searchTerm = searchTerm)
        }
    }
}
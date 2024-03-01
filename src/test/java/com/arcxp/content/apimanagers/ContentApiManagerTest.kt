package com.arcxp.content.apimanagers


import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.arcxp.ArcXPMobileSDK.application
import com.arcxp.ArcXPMobileSDK.baseUrl
import com.arcxp.ArcXPMobileSDK.contentConfig
import com.arcxp.commons.testutils.TestUtils.createContentElement
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.Constants
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.MoshiController.toJson
import com.arcxp.commons.util.Success
import com.arcxp.commons.util.Utils
import com.arcxp.content.ArcXPContentConfig
import com.arcxp.content.extendedModels.ArcXPContentElement
import com.arcxp.content.retrofit.ContentService
import com.arcxp.content.retrofit.NavigationService
import com.arcxp.sdk.R
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.util.Calendar
import java.util.Date

class ContentApiManagerTest {

    @get:Rule
    var instantExecuteRule = InstantTaskExecutorRule()

    @RelaxedMockK
    private lateinit var contentService: ContentService

    @RelaxedMockK
    private lateinit var arcXPContentConfig: ArcXPContentConfig

    @RelaxedMockK
    private lateinit var navigationService: NavigationService

    @RelaxedMockK
    private lateinit var application: Application

    @RelaxedMockK
    private lateinit var expectedDate: Date

    private val siteServiceHierarchy = "siteServiceHierarchy"
    private val collectionError = "Get Collection: our exception message"
    private val expectedTime = 1232389L

    private lateinit var testObject: ContentApiManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every {
            application.getString(R.string.get_collection_failure_message, any())
        } returns collectionError
        mockkObject(Utils)
        coEvery { Utils.determineExpiresAt(any()) } returns expectedDate
        every { expectedDate.time } returns expectedTime
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getCollection on success given full`() = runTest {
        val expectedAnswer = "expected json"
        val mockWebServer = MockWebServer()
        val mockResponse = MockResponse().setBody(expectedAnswer)
            .setHeader("expires", "Tue, 01 Mar 2022 22:05:54 GMT")
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()

        every { contentConfig().cacheTimeUntilUpdateMinutes } returns null
        every { baseUrl } returns mockBaseUrl
        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )

        val actual = testObject.getCollection(
            collectionAlias = "id",
            size = Constants.DEFAULT_PAGINATION_SIZE,
            from = 0,
            full = true
        )

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/collection-full/id?size=20&from=0", request1.path)

        assertTrue(actual is Success<Pair<String, Date>>)
        assertEquals(expectedAnswer, (actual as Success<Pair<String, Date>>).success.first)
        assertEquals(expectedDate, actual.success.second)

        mockWebServer.shutdown()
    }

    @Test
    fun `getCollection on success given not full`() = runTest {
        val expectedAnswer = "expected json"
        val mockWebServer = MockWebServer()
        val mockResponse = MockResponse().setBody(expectedAnswer)
            .setHeader("expires", "Tue, 01 Mar 2022 22:05:54 GMT")
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()

        every { contentConfig().cacheTimeUntilUpdateMinutes } returns null
        every { baseUrl } returns mockBaseUrl
        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )

        val actual = testObject.getCollection(
            collectionAlias = "id",
            size = Constants.DEFAULT_PAGINATION_SIZE,
            from = 0,
            full = false
        )

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/collection/id?size=20&from=0", request1.path)

        assertTrue(actual is Success<Pair<String, Date>>)
        assertEquals(expectedAnswer, (actual as Success<Pair<String, Date>>).success.first)
        assertEquals(expectedDate, actual.success.second)

        mockWebServer.shutdown()
    }


    @Test
    fun `getCollection on success uses preloading value when not provided`() = runTest {
        coEvery { contentConfig().preLoading } returns true
        val expectedAnswer = "expected json"
        val mockWebServer = MockWebServer()
        val mockResponse = MockResponse().setBody(expectedAnswer)
            .setHeader("expires", "Tue, 01 Mar 2022 22:05:54 GMT")
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()

        every { contentConfig().cacheTimeUntilUpdateMinutes } returns null
        every { baseUrl } returns mockBaseUrl
        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )

        val actual = testObject.getCollection(
            collectionAlias = "id",
            size = Constants.DEFAULT_PAGINATION_SIZE,
            from = 0,
            full = null
        )

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/collection-full/id?size=20&from=0", request1.path)

        assertTrue(actual is Success<Pair<String, Date>>)
        assertEquals(expectedAnswer, (actual as Success<Pair<String, Date>>).success.first)
        assertEquals(expectedDate, actual.success.second)

        mockWebServer.shutdown()
    }

    @Test
    fun `getCollection Full on success`() = runTest {
        val expectedAnswer = "expected json"
        val mockWebServer = MockWebServer()
        val mockResponse = MockResponse().setBody(expectedAnswer)
            .setHeader("expires", "Tue, 01 Mar 2022 22:05:54 GMT")

        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()
        val initialDate = Calendar.getInstance()
        initialDate.set(2022, Calendar.FEBRUARY, 8, 11, 0, 0)
        mockkStatic(Calendar::class)
        every { Calendar.getInstance() } returns initialDate
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns null
        every { baseUrl } returns mockBaseUrl
        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )

        val actual = testObject.getCollection(
            collectionAlias = "id",
            size = Constants.DEFAULT_PAGINATION_SIZE,
            from = 0,
            full = true
        )

        assertTrue(actual is Success<Pair<String, Date>>)
        assertEquals(expectedAnswer, (actual as Success<Pair<String, Date>>).success.first)
        assertEquals(expectedDate, actual.success.second)
        mockWebServer.shutdown()
    }

    @Test
    fun `getCollection on error`() = runTest {
        val mockResponse = MockResponse().setBody("").setResponseCode(301)
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()

        every { baseUrl } returns mockBaseUrl
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )

        val actual = testObject.getCollection(
            collectionAlias = "id",
            size = Constants.DEFAULT_PAGINATION_SIZE,
            from = 0,
            full = false
        )

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/collection/id?size=20&from=0", request1.path)

        val error = (actual as Failure).failure
        assertEquals(ArcXPSDKErrorType.SERVER_ERROR, error.type)
        assertEquals(collectionError, error.message)
        mockWebServer.shutdown()
    }

    @Test
    fun `getCollection on failure`() = runTest {
        val exception = IOException("our exception message")
        coEvery {
            contentService.getCollectionFull(
                id = "id",
                size = Constants.DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } throws exception

        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )


        val result = testObject.getCollection(
            collectionAlias = "id",
            size = Constants.DEFAULT_PAGINATION_SIZE,
            from = 0,
            full = true
        )
        assertEquals(ArcXPSDKErrorType.SERVER_ERROR, (result as Failure).failure.type)
        assertEquals(collectionError, result.failure.message)
    }


    @Test
    fun `getSectionListSuspend on success`() = runTest {
        val expectedAnswer = "expected json"
        val mockWebServer = MockWebServer()
        val mockResponse = MockResponse().setBody(expectedAnswer)
            .setHeader("expires", "Tue, 01 Mar 2022 22:05:54 GMT")
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        every { baseUrl } returns mockBaseUrl
        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )

        val actual = testObject.getSectionList(siteServiceHierarchy = siteServiceHierarchy)

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/navigation/$siteServiceHierarchy/", request1.path)

        assertTrue(actual is Success)
        assertEquals(expectedAnswer, (actual as Success).success.first)
        mockWebServer.shutdown()
    }

    @Test
    fun `getSectionListSuspend on error`() = runTest {
        val expectedError = "our exception message"
        val expectedMessage = "Failed to load navigation: $expectedError"
        val mockResponse = MockResponse().setBody(expectedError).setResponseCode(301)
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()

        coEvery { baseUrl } returns mockBaseUrl
        coEvery { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )
        coEvery {
            application.getString(
                R.string.failed_to_load_navigation,
                expectedError
            )
        } returns expectedMessage

        val actual = testObject.getSectionList(siteServiceHierarchy = siteServiceHierarchy)

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/navigation/$siteServiceHierarchy/", request1.path)


        (actual as Failure).failure.apply {
            assertEquals(ArcXPSDKErrorType.SERVER_ERROR, type)
            assertEquals(expectedMessage, message)
            assertNull(value)
        }
        mockWebServer.shutdown()
    }

    @Test
    fun `getSectionListSuspend on failure`() = runTest {
        val expectedError = "our exception message"
        val expectedMessage = "Failed to load navigation: $expectedError"
        val exception = Exception(expectedError)
        mockkObject(DependencyFactory)


        coEvery { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        coEvery { baseUrl } returns "https://arcsales"
        coEvery { navigationService.getSectionList(siteServiceHierarchy = siteServiceHierarchy) } throws exception
        coEvery {
            application.getString(
                R.string.failed_to_load_navigation,
                expectedError
            )
        } returns expectedMessage
        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )

        val result = testObject.getSectionList(siteServiceHierarchy = siteServiceHierarchy)


        assertEquals(ArcXPSDKErrorType.SERVER_ERROR, (result as Failure).failure.type)
        assertEquals(expectedMessage, result.failure.message)
        assertEquals(exception, result.failure.value)
    }

    @Test
    fun `getContentSuspend on success`() = runTest {
        val expectedAnswer = "expected json"
        val mockWebServer = MockWebServer()
        val mockResponse = MockResponse().setBody(expectedAnswer)
            .setHeader("expires", "Tue, 01 Mar 2022 22:05:54 GMT")
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()

        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        every { baseUrl } returns mockBaseUrl
        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )

        val actual = testObject.getContent(id = "id")

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/article?_id=id", request1.path)

        assertEquals(expectedAnswer, (actual as Success).success.first)
        mockWebServer.shutdown()
    }


    @Test
    fun `getContentSuspend on error`() = runTest {
        val expectedError = "error body"
        val id = "id"
        val expectedMessage = "Get Content Call Error for ANS id $id: $expectedError"
        val mockResponse = MockResponse().setBody(expectedError).setResponseCode(301)
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()
        coEvery { baseUrl } returns mockBaseUrl
        coEvery { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        coEvery {
            application.getString(
                R.string.content_failure_message,
                id,
                expectedError
            )
        } returns expectedMessage
        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )

        val actual = testObject.getContent(id = "id")

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/article?_id=id", request1.path)
        (actual as Failure).failure.apply {
            assertEquals(ArcXPSDKErrorType.SERVER_ERROR, type)
            assertEquals(expectedMessage, message)
            assertNull(value)
        }
        mockWebServer.shutdown()
    }

    @Test
    fun `getContentSuspend on failure`() = runTest {
        val expectedError = "our exception message"
        val id = "id"
        val expectedMessage = "Get Content Call Error for ANS id $id: $expectedError"
        val exception = Exception(expectedError)
        coEvery { contentService.getContent(id = "id") } throws exception
        coEvery { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        coEvery {
            application.getString(
                R.string.content_failure_message,
                id,
                expectedError
            )
        } returns expectedMessage
        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )

        val result = testObject.getContent(id = "id")

        assertEquals(ArcXPSDKErrorType.SERVER_ERROR, (result as Failure).failure.type)
        assertEquals(expectedMessage, result.failure.message!!)
        assertEquals(exception, result.failure.value)
    }

    @Test
    fun `Search on success`() = runTest {
        val searchResult0 = createContentElement(id = "0")
        val searchResult1 = createContentElement(id = "1")
        val searchResult2 = createContentElement(id = "2")
        val expectedListFromServer = listOf(searchResult0, searchResult1, searchResult2)
        val expectedMap = HashMap<Int, ArcXPContentElement>()
        expectedMap[0] = searchResult0
        expectedMap[1] = searchResult1
        expectedMap[2] = searchResult2
        val mockWebServer = MockWebServer()
        val mockResponse = MockResponse().setBody(toJson(expectedListFromServer)!!)
            .setHeader("expires", "Tue, 01 Mar 2022 22:05:54 GMT")
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()

        every { baseUrl } returns mockBaseUrl
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )

        val actual = testObject.search(searchTerm = "keywords")

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/search/keywords/?size=20&from=0", request1.path)

        assertTrue(actual is Success)
        assertEquals(expectedMap, (actual as Success).success)
        mockWebServer.shutdown()
    }

    @Test
    fun `Search on error`() = runTest {
        val expectedError = "error body"
        val searchTerm = "keywords"
        val expectedMessage = "Search Error for term $searchTerm: $expectedError"
        val mockResponse = MockResponse().setBody(expectedError).setResponseCode(301)
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()

        every { baseUrl } returns mockBaseUrl
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        every {
            application().getString(
                R.string.search_failure_message,
                searchTerm,
                expectedError
            )
        } returns expectedMessage
        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )

        val actual = testObject.search(searchTerm = searchTerm)

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/search/keywords/?size=20&from=0", request1.path)

        assertEquals(ArcXPSDKErrorType.SEARCH_ERROR, (actual as Failure).failure.type)
        assertEquals(expectedMessage, actual.failure.message)
        assertNull(actual.failure.value)
        mockWebServer.shutdown()
    }

    @Test
    fun `Search on failure`() = runTest {
        val searchTerm = "keywords"
        val expectedError = "our exception message"
        val expectedMessage = "Search Error for term $searchTerm: $expectedError"

        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )
        val exception = IOException("our exception message")
        coEvery {
            contentService.search(
                searchTerms = searchTerm,
                size = Constants.DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } throws exception
        every {
            application().getString(
                R.string.search_failure_message,
                searchTerm,
                expectedError
            )
        } returns expectedMessage


        (testObject.search(searchTerm = searchTerm) as Failure).failure.apply {
            assertEquals(ArcXPSDKErrorType.SEARCH_ERROR, type)
            assertEquals(expectedMessage, message)
            assertEquals(exception, value)
        }
    }

    @Test
    fun `SearchAsJson on success`() = runTest {
        val expectedJson = "json"
        val mockWebServer = MockWebServer()
        val mockResponse = MockResponse().setBody(expectedJson)
            .setHeader("expires", "Tue, 01 Mar 2022 22:05:54 GMT")
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()

        every { baseUrl } returns mockBaseUrl
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )

        val actual = testObject.searchAsJson(searchTerm = "keywords")

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/search/keywords/?size=20&from=0", request1.path)

        assertTrue(actual is Success)
        assertEquals(expectedJson, (actual as Success).success)
        mockWebServer.shutdown()
    }


    @Test
    fun `SearchAsJson on error`() = runTest {
        val expectedError = "error body"
        val searchTerm = "keywords"
        val expectedMessage = "Search Error for term $searchTerm: $expectedError"
        val mockResponse = MockResponse().setBody(expectedError).setResponseCode(301)
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()
        every { baseUrl } returns mockBaseUrl
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        every {
            application().getString(
                R.string.search_failure_message,
                searchTerm,
                expectedError
            )
        } returns expectedMessage

        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )


        val actual = testObject.searchAsJson(searchTerm = searchTerm)

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/search/keywords/?size=20&from=0", request1.path)

        assertEquals(ArcXPSDKErrorType.SEARCH_ERROR, (actual as Failure).failure.type)
        assertEquals(expectedMessage, actual.failure.message)
        assertNull(actual.failure.value)
        mockWebServer.shutdown()
    }

    @Test
    fun `SearchAsJson on failure`() = runTest {
        val searchTerm = "keywords"
        val expectedError = "our exception message"
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        val expectedMessage = "Search Error for term $searchTerm: $expectedError"

        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )
        val exception = Exception(expectedError)
        coEvery {
            contentService.searchAsJson(
                searchTerms = "keywords",
                size = Constants.DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } throws exception
        every {
            application().getString(
                R.string.search_failure_message,
                searchTerm,
                expectedError
            )
        } returns expectedMessage

        (testObject.searchAsJson(searchTerm = searchTerm) as Failure).failure.apply {
            assertEquals(ArcXPSDKErrorType.SEARCH_ERROR, type)
            assertEquals(expectedMessage, message)
            assertEquals(exception, value)
        }

    }

    @Test
    fun `Search Videos on success`() = runTest {
        val searchResult0 = createContentElement(id = "0")
        val searchResult1 = createContentElement(id = "1")
        val searchResult2 = createContentElement(id = "2")
        val expectedListFromServer = listOf(searchResult0, searchResult1, searchResult2)
        val expectedMap = HashMap<Int, ArcXPContentElement>()
        expectedMap[0] = searchResult0
        expectedMap[1] = searchResult1
        expectedMap[2] = searchResult2
        val mockWebServer = MockWebServer()
        val mockResponse = MockResponse().setBody(toJson(expectedListFromServer)!!)
            .setHeader("expires", "Tue, 01 Mar 2022 22:05:54 GMT")
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()

        every { baseUrl } returns mockBaseUrl
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )

        val actual = testObject.searchVideos(searchTerm = "keywords")

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/searchVideo/keywords/?size=20&from=0", request1.path)

        assertTrue(actual is Success)
        assertEquals(expectedMap, (actual as Success).success)
        mockWebServer.shutdown()
    }

    @Test
    fun `Search Videos on error`() = runTest {
        val searchTerm = "keywords"
        val expectedError = "i am error"
        val expectedMessage = "Search Error for term $searchTerm: $expectedError"
        val mockResponse = MockResponse().setBody(expectedError).setResponseCode(301)
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()
        every { baseUrl } returns mockBaseUrl
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        every {
            application().getString(
                R.string.search_failure_message,
                searchTerm,
                expectedError
            )
        } returns expectedMessage
        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )

        val actual = testObject.searchVideos(searchTerm = searchTerm)

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/searchVideo/keywords/?size=20&from=0", request1.path)

        assertEquals(ArcXPSDKErrorType.SEARCH_ERROR, (actual as Failure).failure.type)
        assertEquals(expectedMessage, actual.failure.message)
        assertNull(actual.failure.value)
        mockWebServer.shutdown()
    }

    @Test
    fun `Search Videos on failure`() = runTest {
        val searchTerm = "keywords"
        val expectedError = "our exception message"
        val expectedMessage = "Search Error for term $searchTerm: $expectedError"
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        every {
            application().getString(
                R.string.search_failure_message,
                searchTerm,
                expectedError
            )
        } returns expectedMessage
        testObject = ContentApiManager(
            application = application,
            contentConfig = arcXPContentConfig,
            contentService = contentService,
            navigationService = navigationService
        )
        val exception = Exception(expectedError)
        coEvery {
            contentService.searchVideos(
                searchTerms = searchTerm,
                size = Constants.DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } throws exception

        (testObject.searchVideos(searchTerm = searchTerm) as Failure).failure.apply {
            assertEquals(ArcXPSDKErrorType.SEARCH_ERROR, type)
            assertEquals(expectedMessage, message)
            assertEquals(exception, value)
        }
    }
}
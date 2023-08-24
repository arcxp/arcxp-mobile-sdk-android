package com.arcxp.content.apimanagers


import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.arcxp.ArcXPMobileSDK
import com.arcxp.ArcXPMobileSDK.application
import com.arcxp.ArcXPMobileSDK.baseUrl
import com.arcxp.ArcXPMobileSDK.contentConfig
import com.arcxp.commons.testutils.TestUtils.createCollectionElement
import com.arcxp.commons.testutils.TestUtils.createContentElement
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.Constants
import com.arcxp.commons.util.DependencyFactory
import com.arcxp.commons.util.Failure
import com.arcxp.commons.util.MoshiController.toJson
import com.arcxp.commons.util.Success
import com.arcxp.commons.util.Utils
import com.arcxp.content.extendedModels.ArcXPCollection
import com.arcxp.content.extendedModels.ArcXPContentElement
import com.arcxp.content.retrofit.ContentService
import com.arcxp.content.retrofit.NavigationService
import com.arcxp.sdk.R
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class ContentApiManagerTest {

    @get:Rule
    var instantExecuteRule = InstantTaskExecutorRule()

    @RelaxedMockK
    private lateinit var contentService: ContentService

    @RelaxedMockK
    private lateinit var navigationService: NavigationService

    @RelaxedMockK
    private lateinit var application: Application

    @RelaxedMockK
    private lateinit var expectedDate: Date

    private val endpoint = "endpoint"
    private val collectionError = "Get Collection: our exception message"
    private val expectedTime = 1232389L

    private lateinit var testObject: ContentApiManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockkObject(ArcXPMobileSDK)
        every { application() } returns application
        every {
            application.getString(R.string.get_collection_failure_message, any())
        } returns collectionError
        mockkObject(Utils)
        coEvery { Utils.determineExpiresAt(any())} returns expectedDate
        every { expectedDate.time } returns expectedTime
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getCollection on success`() = runTest {
        val expectedAnswer = "expected json"
        val mockWebServer = MockWebServer()
        val mockResponse = MockResponse().setBody(expectedAnswer)
            .setHeader("expires", "Tue, 01 Mar 2022 22:05:54 GMT")
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()

        every { contentConfig().cacheTimeUntilUpdateMinutes } returns null
        every { baseUrl } returns mockBaseUrl
        testObject = ContentApiManager()

        val actual = testObject.getCollection(
            id = "id",
            size = Constants.DEFAULT_PAGINATION_SIZE,
            from = 0
        )

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
        testObject = ContentApiManager()

        val actual = testObject.getCollection(
            id = "id",
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
        testObject = ContentApiManager()

        val actual = testObject.getCollection(
            id = "id",
            size = Constants.DEFAULT_PAGINATION_SIZE,
            from = 0
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
            contentService.getCollection(
                id = "id",
                size = Constants.DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } throws exception
        mockkObject(DependencyFactory)
        every { DependencyFactory.createContentService() } returns contentService
        every { DependencyFactory.createNavigationService() } returns navigationService

        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager()


        val result = testObject.getCollection(
            id = "id",
            size = Constants.DEFAULT_PAGINATION_SIZE,
            from = 0
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
        every { contentConfig().navigationEndpoint } returns endpoint
        testObject = ContentApiManager()

        val actual = testObject.getSectionList()

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/navigation/$endpoint/", request1.path)

        assertTrue(actual is Success)
        assertEquals(expectedAnswer, (actual as Success).success.first)
        mockWebServer.shutdown()
    }

    @Test
    fun `getSectionListSuspend on error`() = runTest {
        val mockResponse = MockResponse().setBody("").setResponseCode(301)
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()

        every { baseUrl } returns mockBaseUrl
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        every { contentConfig().navigationEndpoint } returns endpoint
        testObject = ContentApiManager()

        val actual = testObject.getSectionList()

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/navigation/$endpoint/", request1.path)

        assertTrue(actual is Failure)
        assertEquals(ArcXPSDKErrorType.SERVER_ERROR, (actual as Failure).failure.type)
        assertEquals("Unable to get navigation", actual.failure.message)
        mockWebServer.shutdown()
    }

    @Test
    fun `getSectionListSuspend on failure`() = runTest {

        mockkObject(DependencyFactory)
        every { DependencyFactory.createContentService() } returns contentService
        every { DependencyFactory.createNavigationService() } returns navigationService

        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        every { contentConfig().navigationEndpoint } returns endpoint
        every { baseUrl } returns "https://arcsales"
        coEvery { navigationService.getSectionList(endpoint = endpoint) } throws IOException()

        testObject = ContentApiManager()

        val result = testObject.getSectionList()


        assertEquals(ArcXPSDKErrorType.SERVER_ERROR, (result as Failure).failure.type)
        assertEquals("Unable to get navigation", result.failure.message)
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
        testObject = ContentApiManager()

        val actual = testObject.getContent(id = "id")

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/article?_id=id", request1.path)

        assertEquals(expectedAnswer, (actual as Success).success.first)
        mockWebServer.shutdown()
    }


    @Test
    fun `getContentSuspend on error`() = runTest {
        val mockResponse = MockResponse().setBody("").setResponseCode(301)
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()

        every { baseUrl } returns mockBaseUrl
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager()

        val actual = testObject.getContent(id = "id")

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/article?_id=id", request1.path)

        assertEquals(ArcXPSDKErrorType.SERVER_ERROR, (actual as Failure).failure.type)
        assertTrue(actual.failure.message!!.startsWith("Error: "))
        mockWebServer.shutdown()
    }

    @Test
    fun `getContentSuspend on failure`() = runTest {
        coEvery { contentService.getContent(id = "id") } throws IOException()
        mockkObject(DependencyFactory)
        every { DependencyFactory.createContentService() } returns contentService
        every { DependencyFactory.createNavigationService() } returns navigationService

        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager()

        val result = testObject.getContent(id = "id")

        assertEquals(ArcXPSDKErrorType.SERVER_ERROR, (result as Failure).failure.type)
        assertEquals("Get Content Call Error for ANS id:id", result.failure.message!!)
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
        testObject = ContentApiManager()

        val actual = testObject.search(searchTerm = "keywords")

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/search/keywords/?size=20&from=0", request1.path)

        assertTrue(actual is Success)
        assertEquals(expectedMap, (actual as Success).success)
        mockWebServer.shutdown()
    }

    @Test
    fun `Search on error`() = runTest {
        val mockResponse = MockResponse().setBody("").setResponseCode(301)
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()

        every { baseUrl } returns mockBaseUrl
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager()

        val actual = testObject.search(searchTerm = "keywords")

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/search/keywords/?size=20&from=0", request1.path)

        assertEquals(ArcXPSDKErrorType.SEARCH_ERROR, (actual as Failure).failure.type)
        assertTrue(actual.failure.message!!.startsWith("Search Call Failure: "))
        mockWebServer.shutdown()
    }

    @Test
    fun `Search on failure`() = runTest {
        mockkObject(DependencyFactory)
        every { DependencyFactory.createContentService() } returns contentService
        every { DependencyFactory.createNavigationService() } returns navigationService

        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager()
        val exception = IOException("our exception message")
        coEvery {
            contentService.search(
                searchTerms = "keywords",
                size = Constants.DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } throws exception

        val result = testObject.search(searchTerm = "keywords")

        assertEquals(ArcXPSDKErrorType.SEARCH_ERROR, (result as Failure).failure.type)
        assertEquals("Search Call Error: keywords", result.failure.message!!)
    }

    @Test
    fun `SearchCollection on success`() = runTest {
        val searchResult0 = createCollectionElement(id = "0")
        val searchResult1 = createCollectionElement(id = "1")
        val searchResult2 = createCollectionElement(id = "2")
        val expectedListFromServer = listOf(searchResult0, searchResult1, searchResult2)
        val expectedMap = HashMap<Int, ArcXPCollection>()
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
        testObject = ContentApiManager()

        val actual = testObject.searchCollection(searchTerm = "keywords")

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/search/keywords/?size=20&from=0", request1.path)

        assertTrue(actual is Success)
        assertEquals(expectedMap, (actual as Success).success)
        mockWebServer.shutdown()
    }

    @Test
    fun `SearchCollection on error`() = runTest {
        val mockResponse = MockResponse().setBody("").setResponseCode(301)
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()

        every { baseUrl } returns mockBaseUrl
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager()

        val actual = testObject.searchCollection(searchTerm = "keywords")

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/search/keywords/?size=20&from=0", request1.path)

        assertEquals(ArcXPSDKErrorType.SEARCH_ERROR, (actual as Failure).failure.type)
        assertTrue(actual.failure.message!!.startsWith("Search Collection Call Failure: "))
        mockWebServer.shutdown()
    }

    @Test
    fun `SearchCollection on failure`() = runTest {
        mockkObject(DependencyFactory)
        every { DependencyFactory.createContentService() } returns contentService
        every { DependencyFactory.createNavigationService() } returns navigationService

        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager()
        val exception = IOException("our exception message")
        coEvery {
            contentService.searchCollection(
                searchTerms = "keywords",
                size = Constants.DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } throws exception

        val result = testObject.searchCollection(searchTerm = "keywords")

        assertEquals(ArcXPSDKErrorType.SEARCH_ERROR, (result as Failure).failure.type)
        assertEquals("Search Collection Call Error: keywords", result.failure.message!!)
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
        testObject = ContentApiManager()

        val actual = testObject.searchVideos(searchTerm = "keywords")

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/searchVideo/keywords/?size=20&from=0", request1.path)

        assertTrue(actual is Success)
        assertEquals(expectedMap, (actual as Success).success)
        mockWebServer.shutdown()
    }

    @Test
    fun `Search Videos on error`() = runTest {
        val mockResponse = MockResponse().setBody("").setResponseCode(301)
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()

        every { baseUrl } returns mockBaseUrl
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager()

        val actual = testObject.searchVideos(searchTerm = "keywords")

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/searchVideo/keywords/?size=20&from=0", request1.path)

        assertEquals(ArcXPSDKErrorType.SEARCH_ERROR, (actual as Failure).failure.type)
        assertTrue(actual.failure.message!!.startsWith("Search Call Failure: "))
        mockWebServer.shutdown()
    }

    @Test
    fun `Search Videos on failure`() = runTest {
        mockkObject(DependencyFactory)
        every { DependencyFactory.createContentService() } returns contentService
        every { DependencyFactory.createNavigationService() } returns navigationService

        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager()
        val exception = IOException("our exception message")
        coEvery {
            contentService.searchVideos(
                searchTerms = "keywords",
                size = Constants.DEFAULT_PAGINATION_SIZE,
                from = 0
            )
        } throws exception

        val result = testObject.searchVideos(searchTerm = "keywords")

        assertEquals(ArcXPSDKErrorType.SEARCH_ERROR, (result as Failure).failure.type)
        assertEquals("Search Call Error: keywords", result.failure.message!!)
    }


}
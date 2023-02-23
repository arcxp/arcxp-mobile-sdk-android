package com.arcxp.content.apimanagers


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.arcxp.ArcXPMobileSDK
import com.arcxp.ArcXPMobileSDK.baseUrl
import com.arcxp.ArcXPMobileSDK.contentConfig
import com.arcxp.content.extendedModels.ArcXPContentElement
import com.arcxp.content.models.ArcXPContentCallback
import com.arcxp.content.models.ArcXPContentSDKErrorType.SEARCH_ERROR
import com.arcxp.content.models.ArcXPContentSDKErrorType.SERVER_ERROR
import com.arcxp.content.retrofit.ContentService
import com.arcxp.content.retrofit.NavigationService
import com.arcxp.content.testUtils.createContentElement
import com.arcxp.content.util.Constants
import com.arcxp.content.util.DependencyFactory
import com.arcxp.content.util.Failure
import com.arcxp.content.util.MoshiController.toJson
import com.arcxp.content.util.Success
import io.mockk.*
import io.mockk.impl.annotations.MockK
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
    lateinit var contentService: ContentService

    @RelaxedMockK
    lateinit var navigationService: NavigationService

    @MockK
    lateinit var arcxpContentCallback: ArcXPContentCallback
    private lateinit var testObject: ContentApiManager

    private val endpoint = "endpoint"

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @After
    fun tearDown() {
        unmockkObject(ArcXPMobileSDK)
        unmockkObject(DependencyFactory)
        unmockkStatic(Calendar::class)
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
        val initialDate = Calendar.getInstance()
        initialDate.set(2022, Calendar.FEBRUARY, 8, 11, 0, 0)
        mockkStatic(Calendar::class)
        every { Calendar.getInstance() } returns initialDate
        mockkObject(ArcXPMobileSDK)
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
        unmockkStatic(Calendar::class)
        val resultCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        resultCalendar.time = actual.success.second
        assertEquals(Calendar.TUESDAY, resultCalendar.get(Calendar.DAY_OF_WEEK))
        assertEquals(1, resultCalendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.MARCH, resultCalendar.get(Calendar.MONTH))
        assertEquals(2022, resultCalendar.get(Calendar.YEAR))
        assertEquals(22, resultCalendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(5, resultCalendar.get(Calendar.MINUTE))
        assertEquals(54, resultCalendar.get(Calendar.SECOND))
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
        mockkObject(ArcXPMobileSDK)
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
        unmockkStatic(Calendar::class)
        val resultCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        resultCalendar.time = actual.success.second
        assertEquals(Calendar.TUESDAY, resultCalendar.get(Calendar.DAY_OF_WEEK))
        assertEquals(1, resultCalendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.MARCH, resultCalendar.get(Calendar.MONTH))
        assertEquals(2022, resultCalendar.get(Calendar.YEAR))
        assertEquals(22, resultCalendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(5, resultCalendar.get(Calendar.MINUTE))
        assertEquals(54, resultCalendar.get(Calendar.SECOND))
        mockWebServer.shutdown()
    }

    @Test
    fun `getCollection on error`() = runTest {
        val mockResponse = MockResponse().setBody("").setResponseCode(301)
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()
        mockkObject(ArcXPMobileSDK)
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
        Thread.sleep(100)
        val error = (actual as Failure).failure
        assertEquals(SERVER_ERROR, error.type)
        assertTrue(error.message.startsWith("Get Collection: "))
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
        mockkObject(ArcXPMobileSDK)
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager()


        val result = testObject.getCollection(
            id = "id",
            size = Constants.DEFAULT_PAGINATION_SIZE,
            from = 0
        )
        assertEquals(SERVER_ERROR, (result as Failure).failure.type)
        assertEquals("Get Collection: our exception message", result.failure.message)
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
        val initialDate = Calendar.getInstance()
        initialDate.set(2022, Calendar.FEBRUARY, 8, 11, 0, 0)
        val expected = Calendar.getInstance()
        expected.set(2022, Calendar.FEBRUARY, 8, 11, 1, 0)
        mockkStatic(Calendar::class)
        every { Calendar.getInstance(any<TimeZone>()) } returns initialDate
        mockkObject(ArcXPMobileSDK)
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        every { baseUrl } returns mockBaseUrl
        every { contentConfig().navigationEndpoint } returns endpoint
        testObject = ContentApiManager()

        val actual = testObject.getSectionList()

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/navigation/$endpoint/", request1.path)
        Thread.sleep(100)
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
        mockkObject(ArcXPMobileSDK)
        every { baseUrl } returns mockBaseUrl
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        every { contentConfig().navigationEndpoint } returns endpoint
        testObject = ContentApiManager()

        val actual = testObject.getSectionList()

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/navigation/$endpoint/", request1.path)
        Thread.sleep(100)
        assertTrue(actual is Failure)
        assertEquals(SERVER_ERROR, (actual as Failure).failure.type)
        assertEquals("Unable to get navigation", actual.failure.message)
        mockWebServer.shutdown()
    }

    @Test
    fun `getSectionListSuspend on failure`() = runTest {

        mockkObject(DependencyFactory)
        every { DependencyFactory.createContentService() } returns contentService
        every { DependencyFactory.createNavigationService() } returns navigationService
        mockkObject(ArcXPMobileSDK)
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        every { contentConfig().navigationEndpoint } returns endpoint
        every { baseUrl } returns "https://arcsales"
        coEvery { navigationService.getSectionList(endpoint = endpoint) } throws IOException()

        testObject = ContentApiManager()

        val result = testObject.getSectionList()


        assertEquals(SERVER_ERROR, (result as Failure).failure.type)
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
        val initialDate = Calendar.getInstance()
        initialDate.set(2022, Calendar.FEBRUARY, 8, 11, 0, 0)
        val expected = Calendar.getInstance()
        expected.set(2022, Calendar.FEBRUARY, 8, 11, 1, 0)
        mockkStatic(Calendar::class)
        every { Calendar.getInstance() } returns initialDate
        mockkObject(ArcXPMobileSDK)
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        every { baseUrl } returns mockBaseUrl
        testObject = ContentApiManager()

        val actual = testObject.getContent(id = "id")

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/article?_id=id", request1.path)
        Thread.sleep(100)
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
        mockkObject(ArcXPMobileSDK)
        every { baseUrl } returns mockBaseUrl
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager()

        val actual = testObject.getContent(id = "id")

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/article?_id=id", request1.path)
        Thread.sleep(100)
        assertEquals(SERVER_ERROR, (actual as Failure).failure.type)
        assertTrue(actual.failure.message!!.startsWith("Error: "))
        mockWebServer.shutdown()
    }

    @Test
    fun `getContentSuspend on failure`() = runTest {
        coEvery { contentService.getContent(id = "id") } throws IOException()
        mockkObject(DependencyFactory)
        every { DependencyFactory.createContentService() } returns contentService
        every { DependencyFactory.createNavigationService() } returns navigationService
        mockkObject(ArcXPMobileSDK)
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager()

        val result = testObject.getContent(id = "id")

        assertEquals(SERVER_ERROR, (result as Failure).failure.type)
        assertEquals("Get Content Call Error for ANS id:id", result.failure.message!!)
    }

    @Test
    fun `Search by Keywords Suspend on success`() = runTest {
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
        mockkObject(ArcXPMobileSDK)
        every { baseUrl } returns mockBaseUrl
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager()

        val actual = testObject.search(searchTerm = "keywords")

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/search/keywords/?size=20&from=0", request1.path)
        Thread.sleep(100)
        assertTrue(actual is Success)
        assertEquals(expectedMap, (actual as Success).success)
        mockWebServer.shutdown()
    }

    @Test
    fun `Search by Keywords Suspend on error`() = runTest {
        val mockResponse = MockResponse().setBody("").setResponseCode(301)
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()
        mockkObject(ArcXPMobileSDK)
        every { baseUrl } returns mockBaseUrl
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager()

        val actual = testObject.search(searchTerm = "keywords")

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/search/keywords/?size=20&from=0", request1.path)
        Thread.sleep(100)
        assertEquals(SEARCH_ERROR, (actual as Failure).failure.type)
        assertTrue(actual.failure.message!!.startsWith("Search Call Failure: "))
        mockWebServer.shutdown()
    }

    @Test
    fun `Search by Keywords Suspend on failure`() = runTest {
        mockkObject(DependencyFactory)
        every { DependencyFactory.createContentService() } returns contentService
        every { DependencyFactory.createNavigationService() } returns navigationService
        mockkObject(ArcXPMobileSDK)
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

        assertEquals(SEARCH_ERROR, (result as Failure).failure.type)
        assertEquals("Search Call Error: keywords", result.failure.message!!)
    }

    @Test
    fun `Search Videos by Keywords Suspend on success`() = runTest {
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
        mockkObject(ArcXPMobileSDK)
        every { baseUrl } returns mockBaseUrl
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager()

        val actual = testObject.searchVideos(searchTerm = "keywords")

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/searchVideo/keywords/?size=20&from=0", request1.path)
        Thread.sleep(100)
        assertTrue(actual is Success)
        assertEquals(expectedMap, (actual as Success).success)
        mockWebServer.shutdown()
    }

    @Test
    fun `Search Videos by Keywords Suspend on error`() = runTest {
        val mockResponse = MockResponse().setBody("").setResponseCode(301)
        val mockWebServer = MockWebServer()
        mockWebServer.enqueue(mockResponse)
        mockWebServer.start()
        val mockBaseUrl = mockWebServer.url("\\").toString()
        mockkObject(ArcXPMobileSDK)
        every { baseUrl } returns mockBaseUrl
        every { contentConfig().cacheTimeUntilUpdateMinutes } returns 1
        testObject = ContentApiManager()

        val actual = testObject.searchVideos(searchTerm = "keywords")

        val request1 = mockWebServer.takeRequest()
        assertEquals("/arc/outboundfeeds/searchVideo/keywords/?size=20&from=0", request1.path)
        Thread.sleep(100)
        assertEquals(SEARCH_ERROR, (actual as Failure).failure.type)
        assertTrue(actual.failure.message!!.startsWith("Search Call Failure: "))
        mockWebServer.shutdown()
    }

    @Test
    fun `Search Videos by Keywords Suspend on failure`() = runTest {
        mockkObject(DependencyFactory)
        every { DependencyFactory.createContentService() } returns contentService
        every { DependencyFactory.createNavigationService() } returns navigationService
        mockkObject(ArcXPMobileSDK)
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

        assertEquals(SEARCH_ERROR, (result as Failure).failure.type)
        assertEquals("Search Call Error: keywords", result.failure.message!!)
    }


}
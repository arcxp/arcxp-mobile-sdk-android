package com.arcxp.commerce.base

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import java.io.File

/**
 * Base Class for unit testing with api call involved
 */
abstract class BaseUnitTest : KoinTest {

    private lateinit var mMockServer: MockWebServer

    /**
     * Start Mock Web Server before testing cases, start koin for dependency injection, initiate Auth manager
     */
    @Before
    fun setUp() {
        mMockServer = MockWebServer()
        mMockServer.start()
    }

    /**
     * Mock the internet response
     *
     * @param fileName json file for mock response
     * @param responseCode Http status code
     */
    protected fun mockNetworkResponseWithFileContent(fileName: String, responseCode: Int) = mMockServer.enqueue(
        MockResponse().setResponseCode(responseCode).setBody(getJson(fileName))
    )

    /**
     * Parse json file into json string
     *
     * @param fileName resource file name
     * @return out put json file in form of string
     */
    protected fun getJson(fileName: String): String {
        val file = File(javaClass.classLoader?.getResource(fileName)?.path ?: throw NullPointerException("No path find!"))
        return String(file.readBytes())
    }

    /**
     * Mock base server url
     */
    protected fun getMockWebServerUrl() = mMockServer.url("/").toString()

    /**
     * Shut down the web server, stop koin after each test
     */
    @After
    open fun tearDown() {
        mMockServer.shutdown()
        stopKoin()
    }
}

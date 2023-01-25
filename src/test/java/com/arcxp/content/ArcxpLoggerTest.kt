package com.arcxp.content.sdk

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.arcxp.content.sdk.util.BuildVersionProvider
import com.arcxp.content.sdk.util.DependencyFactory
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*


class ArcxpLoggerTest {

    @RelaxedMockK
    lateinit var application: Application

    @get:Rule
    public var rule = InstantTaskExecutorRule()

    @RelaxedMockK
    lateinit var observer: Observer<ArcXPLogEntry>


    @RelaxedMockK
    lateinit var buildVersionProvider: BuildVersionProvider

    @RelaxedMockK
    lateinit var connectivityManager: ConnectivityManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockkObject(DependencyFactory)
        every { DependencyFactory.buildVersionUtil() } returns buildVersionProvider
        every { application.getSystemService(Context.CONNECTIVITY_SERVICE) } returns connectivityManager
    }

    @Test
    fun `logging works if initialized first`() {
        every { connectivityManager.isDefaultNetworkActive } returns true
        every { buildVersionProvider.sdkInt() } returns 22
        val configuration = Configuration()
        configuration.locale = Locale.US
        every { application.resources.configuration } returns configuration

        val testLogger =
            ArcXPLogger(application, "org", "env", site = "site")

        testLogger.logEntry.observeForever(observer)
        val slot = slot<ArcXPLogEntry>()

        testLogger.i("test")

        verify(exactly = 1) { observer.onChanged(capture(slot)) }
        assertEquals("test", slot.captured.message)

        testLogger.logEntry.removeObserver(observer)

    }

    @Test
    fun `logging works if initialized first api less than 23`() {
        every { connectivityManager.isDefaultNetworkActive } returns true
        every { buildVersionProvider.sdkInt() } returns 22
        val configuration = Configuration()
        configuration.locale = Locale.US
        every { application.resources.configuration } returns configuration

        val testLogger = ArcXPLogger(application, "org", "env", site = "site")

        testLogger.logEntry.observeForever(observer)
        val slot = slot<ArcXPLogEntry>()

        testLogger.i("test")

        verify(exactly = 1) { observer.onChanged(capture(slot)) }
        assertEquals("test", slot.captured.message)

        testLogger.logEntry.removeObserver(observer)

    }

    @Test
    fun `logging works if initialized first over 23`() {
        every { buildVersionProvider.sdkInt() } returns 24
        every { connectivityManager.isDefaultNetworkActive } returns true

        val testLogger = ArcXPLogger(application, "org", "env", site = "site")
        testLogger.logEntry.observeForever(observer)
        val slot = slot<ArcXPLogEntry>()

        testLogger.i("test")

        verify(exactly = 1) { observer.onChanged(capture(slot)) }
        assertEquals("test", slot.captured.message)

        testLogger.logEntry.removeObserver(observer)

    }

    @Test
    fun `info logging works`() {
        every { buildVersionProvider.sdkInt() } returns 24
        every { connectivityManager.isDefaultNetworkActive } returns true

        val testLogger = ArcXPLogger(application, "org", "env", site = "site")

        testLogger.logEntry.observeForever(observer)
        val slot = slot<ArcXPLogEntry>()

        testLogger.i("test")

        verify(exactly = 1) { observer.onChanged(capture(slot)) }
        assertEquals("test", slot.captured.message)

        testLogger.logEntry.removeObserver(observer)

    }

    @Test
    fun `debug logging works`() {
        every { buildVersionProvider.sdkInt() } returns 24
        every { connectivityManager.isDefaultNetworkActive } returns true

        val testLogger = ArcXPLogger(application, "org", "env", site = "site")

        testLogger.logEntry.observeForever(observer)
        val slot = slot<ArcXPLogEntry>()

        testLogger.d("test2")

        verify(exactly = 1) { observer.onChanged(capture(slot)) }
        assertEquals("test2", slot.captured.message)

        testLogger.logEntry.removeObserver(observer)

    }

    @Test
    fun `error logging works`() {
        every { buildVersionProvider.sdkInt() } returns 24
        every { connectivityManager.isDefaultNetworkActive } returns true

        val testLogger = ArcXPLogger(application, "org", "env", site = "site")

        testLogger.logEntry.observeForever(observer)
        val slot = slot<ArcXPLogEntry>()

        testLogger.e("test3")

        verify(exactly = 1) { observer.onChanged(capture(slot)) }
        assertEquals("test3", slot.captured.message)

        testLogger.logEntry.removeObserver(observer)

    }

    @Test
    fun `internal logging works`() {
        every { buildVersionProvider.sdkInt() } returns 24
        every { connectivityManager.isDefaultNetworkActive } returns true

        val testLogger = ArcXPLogger(application, "org", "env", site = "site")

        testLogger.internal(ArcXPLogger.LOG_LEVEL.INFO, "test3", null, null)

        //add to test once we know what we are doing with the internal logging

        clearAllMocks(answers = false)
    }

    @Test
    fun `test breadcrumb list add and clear`() {
        every { buildVersionProvider.sdkInt() } returns 24
        every { connectivityManager.isDefaultNetworkActive } returns true
        val testLogger = ArcXPLogger(application, "org", "env", site = "site")
        testLogger.addBreadcrumb("1")

        assertEquals(testLogger.getBreadcrumbs().size, 1)

        testLogger.clearBreadcrumbs()

        assertEquals(testLogger.getBreadcrumbs().size, 0)

        clearAllMocks(answers = false)
    }

    @Test
    fun `test breadcrumb queue and dequeue`() {
        every { buildVersionProvider.sdkInt() } returns 24
        every { connectivityManager.isDefaultNetworkActive } returns true
        val testLogger = ArcXPLogger(application, "org", "env", site = "site")

        testLogger.addBreadcrumb("1")
        testLogger.addBreadcrumb("2")
        testLogger.addBreadcrumb("3")
        testLogger.addBreadcrumb("4")
        testLogger.addBreadcrumb("5")

        assertEquals(testLogger.getBreadcrumbs().first, "5")
        assertEquals(testLogger.getBreadcrumbs().last, "1")

        testLogger.addBreadcrumb("6")

        assertEquals(testLogger.getBreadcrumbs().first, "6")
        assertEquals(testLogger.getBreadcrumbs().last, "2")

        clearAllMocks(answers = false)

    }


}
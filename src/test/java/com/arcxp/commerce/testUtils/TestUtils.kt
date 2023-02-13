@file:OptIn(ExperimentalCoroutinesApi::class)

package com.arcxp.commerce.testUtils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File

object TestUtils {
    // Reusable JUnit4 TestRule to override the Main dispatcher
    class MainDispatcherRule(
        private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
    ) : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(testDispatcher)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
        }
    }

    fun getJson(filename: String): String {
        val file = File(javaClass.classLoader?.getResource(filename)?.path ?: throw NullPointerException("No path find!"))
        return String(file.readBytes())
    }
}
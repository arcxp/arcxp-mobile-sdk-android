package com.arcxp.commerce.di

import okhttp3.mockwebserver.MockWebServer
import org.koin.dsl.module

/**
 * Creates Mock web server instance for testing
 */
val MockWebServerDITest = module {
    factory {
        MockWebServer()
    }
}

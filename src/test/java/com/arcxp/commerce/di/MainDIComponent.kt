package com.arcxp.commerce.di

/**
 * Main Koin DI component, helps configure server
 */
fun configureTestAppComponent(baseApi: String) = listOf(
    MockWebServerDITest,
    configureNetworkModuleForTest(baseApi)
)

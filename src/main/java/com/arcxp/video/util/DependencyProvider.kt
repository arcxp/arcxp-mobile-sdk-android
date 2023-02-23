package com.arcxp.video.util

import com.arcxp.video.api.VideoApiManager

object DependencyProvider {

    fun buildVersionUtil(): BuildVersionProvider = BuildVersionProviderImpl()
    fun createVideoApiManager(baseUrl: String) = VideoApiManager(baseUrl = baseUrl)
    fun createVideoApiManager(orgName: String, environmentName: String) = VideoApiManager(orgName = orgName, environmentName = environmentName)

}
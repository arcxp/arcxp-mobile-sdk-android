package com.arcxp.video.util

import android.app.Application
import com.arc.arcvideo.ArcMediaClient
import com.arc.arcvideo.ArcXPResizer
import com.arc.arcvideo.api.VideoApiManager

object DependencyProvider {

    fun buildVersionUtil(): BuildVersionProvider = BuildVersionProviderImpl()
    fun createVideoApiManager(baseUrl: String) = VideoApiManager(baseUrl = baseUrl)
    fun createVideoApiManager(orgName: String, environmentName: String) = VideoApiManager(orgName = orgName, environmentName = environmentName)
    fun createArcXPResizer(application: Application, baseUrl: String) = ArcXPResizer(application, baseUrl)
    fun createMediaClient(orgName: String, env: String) = ArcMediaClient.createClient(orgName, env)
}
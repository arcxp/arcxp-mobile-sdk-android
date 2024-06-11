package com.arcxp.sdk

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.video.ArcMediaClient
import com.arcxp.video.ArcMediaPlayer
import com.arcxp.video.ArcVideoStreamCallback
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.listeners.ArcVideoSDKErrorListener
import com.arcxp.video.model.ArcVideoSDKErrorType
import com.arcxp.video.model.ArcVideoStream
import com.arcxp.video.views.ArcVideoFrame

class TestVideoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TestSettings.layoutId!!)
        arcMediaPlayer = ArcMediaPlayer.instantiate(application)
        mediaClient = initMediaClient()
        configurePlayer()

        loadVideo("2c67bd55-32cf-4eae-a11d-17a7c2a73421")//TODO mock network results here
    }

    var arcMediaPlayer: ArcMediaPlayer? = null
    var mediaClient: ArcMediaClient? = null
    private fun configurePlayer() {//TODO config from settings singleton?
        val videoFrame = findViewById<ArcVideoFrame>(TestSettings.videoFrameId!!)
        val arcXPVideoConfigBuilder = ArcXPVideoConfig.Builder()
        arcXPVideoConfigBuilder.setVideoFrame(videoFrame)
        arcXPVideoConfigBuilder.setActivity(this)
        arcXPVideoConfigBuilder.setMaxBitRate(235152000)
        arcXPVideoConfigBuilder.showSeekButton(show = true)
        arcXPVideoConfigBuilder.setAutoStartPlay(true)
        arcXPVideoConfigBuilder.useDialogForFullscreen(use = true)
        arcXPVideoConfigBuilder.setShouldShowFullScreenButton(shouldShowFullScreenButton = true)
        arcXPVideoConfigBuilder.setShouldShowTitleOnControls(shouldShowTitleOnControls = true)
        arcXPVideoConfigBuilder.setShowClosedCaptionTrackSelection(show = false)
        arcMediaPlayer?.configureMediaPlayer(arcXPVideoConfigBuilder.build())
    }

    private fun initMediaClient(): ArcMediaClient {
        ArcXPMobileSDK.initialize(
            application = application,
            baseUrl = "https://arcsales-arcsales-sandbox.web.arc-cdn.net",
            org = "arcsales",
            environment = "sandbox",
            site = "arcsales"
        )//TODO mock network results here
        return ArcXPMobileSDK.mediaClient()
        //createClient(orgName = orgName!!, serverEnvironment = envName!!)
    }


    private fun loadVideo(uuid: String) {
        mediaClient?.findByUuid(uuid, object : ArcVideoStreamCallback {
            override fun onVideoStream(videos: List<ArcVideoStream>?) {
                arcMediaPlayer?.initMedia(video = videos!![0])
                arcMediaPlayer?.displayVideo()
                arcMediaPlayer?.playVideo()
                arcMediaPlayer?.trackErrors(object : ArcVideoSDKErrorListener {
                    override fun onError(
                        errorType: ArcVideoSDKErrorType,
                        message: String,
                        value: Any?
                    ) {
                        Log.e("test", "error from player")
                    }

                })
            }

            override fun onError(type: ArcXPSDKErrorType, message: String, value: Any?) {
                Log.e("test", "error from client")
            }

        })
    }
}
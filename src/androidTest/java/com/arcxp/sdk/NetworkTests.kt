package com.arcxp.sdk

import android.app.Application
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commons.throwables.ArcXPException
import com.arcxp.content.ArcXPContentConfig
import com.arcxp.content.extendedModels.ArcXPStory
import com.arcxp.content.models.ArcXPContentCallback
import com.arcxp.video.util.TAG
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class NetworkTests {

    @Test
    fun callStory() {
        val context = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application)
        val latch = CountDownLatch(1) // Create a CountDownLatch with a count of 1
        context.apply {
            val contentConfig = ArcXPContentConfig.Builder()
                //This is an additional parameter put on the base URL that retrieves the
                //section data for mobile devices.
                .setNavigationEndpoint(endpoint = "default")
                //This is a string corresponding to a video collection content alias
                .setVideoCollectionName(videoCollectionName = "video")
                //Content SDK caches data to decrease the amount of bandwidth needed.
                //This value can be between 10 and 1024 MB
                .setCacheSize(sizeInMB = 1024)
                //After a specified number of minutes cached items will be updated to
                //ensure the latest version is available.
                .setCacheTimeUntilUpdate(minutes = 5)
                //if true will pre-fetch and store in db any stories returned by a collection call
                .setPreloading(preLoading = true)
                .build()




            //Set the base URL for content.  Set the organization, site and environment.
            //These values can be gotten from your ArcXP admin
            ArcXPMobileSDK.initialize(
                application = this,
                site = "irishnews",
                org = "irishnews",
                environment = "sandbox",
                contentConfig = contentConfig,
                baseUrl = "https://irishnews-irishnews-sandbox.web.arc-cdn.net"
            )
        }

        val result = ArcXPMobileSDK.contentManager()
            .getArcXPStory(id = "4IP7WPCQVNGLNM2TGEBXLFMY4A", listener = object:
                ArcXPContentCallback {
            override fun onGetStorySuccess(response: ArcXPStory) {
                Log.d(TAG, response.toString())//fix should reach here
                latch.countDown()
            }

            override fun onError(error: ArcXPException) {
                Log.e(TAG, error.message?:"")//develop reaches here
                latch.countDown()
            }
        })

        assertTrue(latch.await(1, TimeUnit.MINUTES)) // Wait for the latch to count down
    }

}
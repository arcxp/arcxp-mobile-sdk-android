package com.arcxp.sdk

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.arcxp.ArcXPMobileSDK
import com.arcxp.commons.throwables.ArcXPSDKErrorType
import com.arcxp.commons.util.Success
import com.arcxp.content.ArcXPContentConfig
import com.arcxp.content.extendedModels.ArcXPContentElement
import com.arcxp.content.extendedModels.ArcXPStory
import com.arcxp.content.models.ArcXPContentCallback
import com.arcxp.content.models.ArcXPSection
import com.arcxp.video.ArcVideoPlaylistCallback
import com.arcxp.video.ArcVideoStreamCallback
import com.arcxp.video.model.ArcVideoPlaylist
import com.arcxp.video.model.ArcVideoStream
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class NetworkTests {

    /**
     * need to have for this:
     * uuids
     * collection resolver
     * collection with alias
     * site service resolver
     */


    val storyUUID1 = "4YA6YQZR3JB2TJ5R7BL6VPO6OA"
    val storyUUID2 = "R7QI4OWNF5DZ5NJXKSDDOYS6OM"
    val videoUUID1 = "ff799828-aa7e-41f1-9837-98d5d6b74257"
    val videoUUID2 = "3b7ec04e-0ce6-45a3-8524-eb94c53b5e10"
    val collectionAlias = "mobile-topstories"
    val navigation = "mobile-nav"
    val videoPlayListName = "mobilesdk test"
    val org = "arcsales"
    val site = "arcsales"
    val baseURL = "https://arcsales-arcsales-sandbox.web.arc-cdn.net"
    val env = "sandbox"

    @Before
    fun setUp() {

        val context =
            (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application)
        context.apply {
            val contentConfig = ArcXPContentConfig.Builder()
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
                site = site,
                org = org,
                environment = env,
                contentConfig = contentConfig,
                baseUrl = baseURL
            )
        }
    }

    @After
    fun tearDown() {
        ArcXPMobileSDK.contentManager().clearCache()
        ArcXPMobileSDK.reset()

    }

    @Test
    fun callStory() {
        val latch = CountDownLatch(1) // Create a CountDownLatch with a count of 1

        ArcXPMobileSDK.contentManager()
            .getArcXPStory(id = storyUUID1, listener = object :
                ArcXPContentCallback {
                override fun onGetStorySuccess(response: ArcXPStory) {
                    latch.countDown()
                }
            })

        assertTrue(latch.await(1, TimeUnit.MINUTES)) // Wait for the latch to count down
    }

    @Test
    fun callStorySuspend() = runTest {
        val latch = CountDownLatch(1) // Create a CountDownLatch with a count of 1

        (ArcXPMobileSDK.contentManager()
            .getArcXPStorySuspend(id = storyUUID2) as Success).success.let {
            latch.countDown()
        }

        assertTrue(latch.await(1, TimeUnit.MINUTES)) // Wait for the latch to count down
    }

    @Test
    fun callCollection() {
        val latch = CountDownLatch(1) // Create a CountDownLatch with a count of 1

        ArcXPMobileSDK.contentManager().getCollection(
            collectionAlias = collectionAlias,
            listener = object : ArcXPContentCallback {
                override fun onGetCollectionSuccess(response: Map<Int, ArcXPContentElement>) {
                    latch.countDown()
                }
            })

        assertTrue(latch.await(1, TimeUnit.MINUTES)) // Wait for the latch to count down
    }

    @Test
    fun callCollectionSuspend() = runTest {
        val latch = CountDownLatch(1) // Create a CountDownLatch with a count of 1

        (ArcXPMobileSDK.contentManager().getCollectionSuspend(
            collectionAlias = collectionAlias
        ) as Success).success.let { latch.countDown() }

        assertTrue(latch.await(1, TimeUnit.MINUTES)) // Wait for the latch to count down
    }

    @Test
    fun callGenericContent() = runTest {
        val latch = CountDownLatch(1) // Create a CountDownLatch with a count of 1

        (ArcXPMobileSDK.contentManager().getContentSuspend(
            id = storyUUID1
        ) as Success).success.let { latch.countDown() }

        assertTrue(latch.await(1, TimeUnit.MINUTES)) // Wait for the latch to count down
    }

    @Test
    fun callSectionList() {
        val latch = CountDownLatch(1) // Create a CountDownLatch with a count of 1

        ArcXPMobileSDK.contentManager().getSectionList(siteHierarchy = navigation, listener = object : ArcXPContentCallback {
            override fun onGetSectionsSuccess(response: List<ArcXPSection>) {
                latch.countDown()
            }
        })

        assertTrue(latch.await(1, TimeUnit.MINUTES)) // Wait for the latch to count down
    }

    @Test
    fun callSectionListSuspend() = runTest {
        val latch = CountDownLatch(1) // Create a CountDownLatch with a count of 1

        (ArcXPMobileSDK.contentManager()
            .getSectionListSuspend(siteHierarchy = navigation) as Success).success.let { latch.countDown() }

        assertTrue(latch.await(1, TimeUnit.MINUTES)) // Wait for the latch to count down
    }

    @Test
    fun callFindByUUID() {
        val latch = CountDownLatch(1) // Create a CountDownLatch with a count of 1

        ArcXPMobileSDK.mediaClient()
            .findByUuid(uuid = videoUUID1, listener = object : ArcVideoStreamCallback {
                override fun onVideoStream(videos: List<ArcVideoStream>?) {
                    latch.countDown()
                }
            })

        assertTrue(latch.await(1, TimeUnit.MINUTES)) // Wait for the latch to count down
    }

    @Test
    fun callFindByUUIDs() {
        val latch = CountDownLatch(1) // Create a CountDownLatch with a count of 1

        ArcXPMobileSDK.mediaClient().findByUuids(
            uuids = listOf(videoUUID1, videoUUID2),
            listener = object : ArcVideoStreamCallback {
                override fun onVideoStream(videos: List<ArcVideoStream>?) {
                    latch.countDown()
                }
            })

        assertTrue(latch.await(1, TimeUnit.MINUTES)) // Wait for the latch to count down
    }

    @Test
    fun callFindByUUIDsVarArg() {
        val latch = CountDownLatch(1) // Create a CountDownLatch with a count of 1

        ArcXPMobileSDK.mediaClient()
            .findByUuids(videoUUID1, videoUUID2, listener = object : ArcVideoStreamCallback {
                override fun onVideoStream(videos: List<ArcVideoStream>?) {
                    latch.countDown()
                }
            })

        assertTrue(latch.await(1, TimeUnit.MINUTES)) // Wait for the latch to count down
    }

    @Test
    fun callFindByUUIDsVarArgEnd() {
        val latch = CountDownLatch(1) // Create a CountDownLatch with a count of 1

        ArcXPMobileSDK.mediaClient().findByUuids(listener = object : ArcVideoStreamCallback {
            override fun onVideoStream(videos: List<ArcVideoStream>?) {
                latch.countDown()
            }
        }, videoUUID1, videoUUID2)

        assertTrue(latch.await(1, TimeUnit.MINUTES)) // Wait for the latch to count down
    }

    @Test
    fun callFindLive() = runTest {
        val latch = CountDownLatch(1) // Create a CountDownLatch with a count of 1

        (ArcXPMobileSDK.mediaClient()
            .findLiveSuspend() as Success).success.let { latch.countDown() }

        assertTrue(latch.await(1, TimeUnit.MINUTES)) // Wait for the latch to count down
        //TODO may need mock network response here, as it is usually empty depending on org / live vids
    }

    @Test
    fun callFindByPlaylist() {
        val latch = CountDownLatch(1) // Create a CountDownLatch with a count of 1

        ArcXPMobileSDK.mediaClient().findByPlaylist(name = videoPlayListName, listener = object : ArcVideoPlaylistCallback {
            override fun onVideoPlaylist(playlist: ArcVideoPlaylist?) {
                latch.countDown()
            }

            override fun onError(type: ArcXPSDKErrorType, message: String, value: Any?) {

            }

        }, count = 10)

        assertTrue(latch.await(1, TimeUnit.MINUTES)) // Wait for the latch to count down
    }


}
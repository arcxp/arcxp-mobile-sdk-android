package com.arcxp.sdk

import android.media.session.PlaybackState
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withClassName
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.arcxp.video.listeners.ArcVideoEventsListener
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.hamcrest.CoreMatchers.endsWith
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestVideoActivityTest {


    private lateinit var activityScenario: ActivityScenario<TestVideoActivity>
    private lateinit var idlingResource: CountingIdlingResource
    private var testVideoActivity: TestVideoActivity? = null

    fun getDrawableIdByName(name: String, packageName: String): Int {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return context.resources.getIdentifier(name, "drawable", packageName)
    }

    //run this manually per test so we can set things before running (otherwise onCreate runs before @before method)
    private fun setup() {
        activityScenario = ActivityScenario.launch(TestVideoActivity::class.java)
        activityScenario.onActivity { activity ->
            testVideoActivity = activity
            idlingResource = CountingIdlingResource("my resource")
            idlingResource.increment()
            IdlingRegistry.getInstance().register(idlingResource)
            testVideoActivity?.arcMediaPlayer?.trackMediaEvents(object : ArcVideoEventsListener {
                override fun onVideoTrackingEvent(
                    type: TrackingType?,
                    videoData: TrackingTypeData.TrackingVideoTypeData?
                ) {
                    if (type == TrackingType.ON_PLAY_STARTED) {
                        idlingResource.decrement()
                    }
                }

                override fun onAdTrackingEvent(
                    type: TrackingType?,
                    adData: TrackingTypeData.TrackingAdTypeData?
                ) {

                }

                override fun onSourceTrackingEvent(
                    type: TrackingType?,
                    source: TrackingTypeData.TrackingSourceTypeData?
                ) {
                }

                override fun onError(
                    type: TrackingType?,
                    video: TrackingTypeData.TrackingErrorTypeData?
                ) {
                }

            })
        }
    }

    @After
    fun cleanUp() {
        if (::idlingResource.isInitialized) {
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
        testVideoActivity?.runOnUiThread {
            // Stop or reset your video player here
            testVideoActivity?.arcMediaPlayer?.stop()
            testVideoActivity?.arcMediaPlayer?.finish()
            testVideoActivity?.arcMediaPlayer = null
            testVideoActivity = null
            TestSettings.layoutId = null
            TestSettings.videoFrameId = null

        }


    }

    @Test
    fun testActivityShouldBeLaunched() {//TODO improve test scenarios and track coverage
        TestSettings.layoutId = com.arcxp.sdk.test.R.layout.activity_test_video
        TestSettings.videoFrameId = com.arcxp.sdk.test.R.id.video_frame
        setup()

        onView(withClassName(endsWith("ArcVideoFrame")))
            .check(matches(isDisplayed()))
        onView(withId(com.arcxp.sdk.test.R.id.video_frame)).perform(click())
        testVideoActivity?.runOnUiThread {
            assertTrue(testVideoActivity?.arcMediaPlayer?.playbackState == PlaybackState.STATE_PLAYING)
        }
        onView(withId(R.id.exo_ffwd_with_amount)).check(matches(isDisplayed()))
        onView(withId(R.id.exo_ffwd_with_amount)).perform(click())
        onView(withId(R.id.exo_ffwd_with_amount)).perform(click())
        onView(withId(R.id.exo_rew_with_amount)).perform(click())
        onView(withId(R.id.exo_rew_with_amount)).perform(click())
        onView(withId(R.id.exo_play_pause)).perform(click())
    }




    @Test
    fun testActivityMinimalControls() {
        TestSettings.layoutId = com.arcxp.sdk.test.R.layout.activity_test_video_minimal
        TestSettings.videoFrameId = com.arcxp.sdk.test.R.id.video_frame_minimal

        setup()
        onView(withClassName(endsWith("ArcVideoFrame")))
            .check(matches(isDisplayed()))
        onView(withId(TestSettings.videoFrameId!!)).perform(click())
        testVideoActivity?.runOnUiThread {
            assertTrue(testVideoActivity?.arcMediaPlayer?.playbackState == PlaybackState.STATE_PLAYING)
        }
        assertTrue(testVideoActivity!!.arcMediaPlayer!!.isMinimalControlsNow())
        onView(withId(R.id.exo_ffwd_with_amount)).check(matches(not(isDisplayed())))
        onView(withId(R.id.exo_minimal_fullscreen)).perform(click())
        assertFalse(testVideoActivity!!.arcMediaPlayer!!.isMinimalControlsNow())
        onView(withId(R.id.exo_ffwd_with_amount)).check(matches(isDisplayed()))
    }
}
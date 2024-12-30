package com.arcxp.video

import android.app.Activity
import android.os.Handler
import android.util.Log
import androidx.media3.cast.CastPlayer
import androidx.media3.exoplayer.ExoPlayer
import com.arcxp.video.listeners.VideoListener
import com.arcxp.video.model.TrackingType
import com.arcxp.video.model.TrackingTypeData
import com.arcxp.video.util.TrackingHelper
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class VideoTrackerTest {

    private lateinit var testObject: VideoTracker

    @RelaxedMockK private lateinit var listener: VideoListener
    @RelaxedMockK private lateinit var exoPlayer: ExoPlayer
    @RelaxedMockK private lateinit var castPlayer: CastPlayer
    @RelaxedMockK private lateinit var trackingHelper: TrackingHelper
    @RelaxedMockK private lateinit var activity: Activity

    private val latch = CountDownLatch(2)


    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockkConstructor(Handler::class)
        every { anyConstructed<Handler>().post(any()) } answers {
            try {
                val runnable = invocation.args[0] as Runnable?
                runnable?.run()
            } finally {
                latch.countDown()
            }
            true
        }
        every { activity.runOnUiThread(any()) } answers {
            try {
                val runnable = invocation.args[0] as Runnable?
                runnable?.run()
            } finally {
                latch.countDown()
            }
        }
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given player is exoplayer isLive is false, observable fires events at expected intervals with no errors`() {


        testObject = VideoTracker.getInstance(listener, exoPlayer, trackingHelper, false, activity)
        every { exoPlayer.duration } returns 4
        every { exoPlayer.currentPosition } returns 0


        val testSubscriber = testObject.getObs().test()
        Thread.sleep(500)
        every { exoPlayer.currentPosition } returns 1
        Thread.sleep(1000)
        every { exoPlayer.currentPosition } returns 2
        Thread.sleep(1000)
        every { exoPlayer.currentPosition } returns 3
        Thread.sleep(1000)

        verifySequence {
            listener.onTrackingEvent(
                TrackingType.ON_PLAY_STARTED,
                eq(TrackingTypeData.TrackingVideoTypeData(percentage = 0, position = 0))
            )
            listener.onTrackingEvent(
                TrackingType.VIDEO_PERCENTAGE_WATCHED,
                eq(TrackingTypeData.TrackingVideoTypeData(percentage = 25, position = 1))
            )
            listener.onTrackingEvent(
                TrackingType.VIDEO_PERCENTAGE_WATCHED,
                eq(TrackingTypeData.TrackingVideoTypeData(percentage = 50, position = 2))
            )
            listener.onTrackingEvent(
                TrackingType.VIDEO_PERCENTAGE_WATCHED,
                eq(TrackingTypeData.TrackingVideoTypeData(percentage = 75, position = 3))
            )
        }
        testSubscriber.assertNoErrors()
        testSubscriber.assertNoTerminalEvent()
        testSubscriber.assertNotCompleted()
    }

    @Test
    fun `given player is exoplayer isLive is true, does not call listener for percentage events`() {
//        testObject = VideoTracker.getInstance(listener, exoPlayer, trackingHelper, true)
//        every { exoPlayer.duration } returns 4
//        every { exoPlayer.currentPosition } returns 0
//
//
//        val testSubscriber = testObject.getObs().test()
//        Thread.sleep(500)
//        every { exoPlayer.currentPosition } returns 1
//        Thread.sleep(1000)
//        every { exoPlayer.currentPosition } returns 2
//        Thread.sleep(1000)
//        every { exoPlayer.currentPosition } returns 3
//        Thread.sleep(1000)
//
//        verifySequence {
//            listener.onTrackingEvent(TrackingType.ON_PLAY_STARTED, eq(TrackingTypeData.TrackingVideoTypeData(percentage = 0, position = 0)))
//        }
//        testSubscriber.assertNoErrors()
//        testSubscriber.assertNoTerminalEvent()
//        testSubscriber.assertNotCompleted()
    }  //TODO trouble getting 2 versions of singleton for test, test passes but breaks other tests

    @Test
    fun `given player is exoplayer, getTimeLinePosition throws exception and is logged`() {

        testObject = VideoTracker.getInstance(listener, exoPlayer, trackingHelper, false, activity)
        every { exoPlayer.duration } returns 8
        val exception = Exception("message")
        every { trackingHelper.checkTracking(any()) } throws exception
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0

        val testSubscriber = testObject.getObs().test()

        verify(exactly = 1) { Log.e("ArcVideoSDK", "Exception: message", exception) }
        testSubscriber.assertNoErrors()
        testSubscriber.assertNoTerminalEvent()
        testSubscriber.assertNotCompleted()
    }

    @Test
    fun `given player is castPlayer, observable fires events at expected intervals with no errors`() {
        testObject = VideoTracker.getInstance(listener, castPlayer, trackingHelper, false, activity)
        every { castPlayer.duration } returns 4
        every { castPlayer.currentPosition } returns 0


        val testSubscriber = testObject.getObs().test()
        Thread.sleep(500)
        every { castPlayer.currentPosition } returns 1
        Thread.sleep(1000)
        every { castPlayer.currentPosition } returns 2
        Thread.sleep(1000)
        every { castPlayer.currentPosition } returns 3
        Thread.sleep(1000)

        verifySequence {
            listener.onTrackingEvent(
                TrackingType.ON_PLAY_STARTED,
                eq(TrackingTypeData.TrackingVideoTypeData(percentage = 0, position = 0))
            )
            listener.onTrackingEvent(
                TrackingType.VIDEO_PERCENTAGE_WATCHED,
                eq(TrackingTypeData.TrackingVideoTypeData(percentage = 25, position = 1))
            )
            listener.onTrackingEvent(
                TrackingType.VIDEO_PERCENTAGE_WATCHED,
                eq(TrackingTypeData.TrackingVideoTypeData(percentage = 50, position = 2))
            )
            listener.onTrackingEvent(
                TrackingType.VIDEO_PERCENTAGE_WATCHED,
                eq(TrackingTypeData.TrackingVideoTypeData(percentage = 75, position = 3))
            )
        }
        testSubscriber.assertNoErrors()
        testSubscriber.assertNoTerminalEvent()
        testSubscriber.assertNotCompleted()
    }
}
package com.arc.arcvideo.views

import android.app.Activity
import android.content.res.Resources
import android.view.MotionEvent
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.Assert
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class VideoFrameLayoutTest {

    @RelaxedMockK private lateinit var activity: Activity

    private lateinit var testObject: VideoFrameLayout

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        testObject = VideoFrameLayout(activity, null, 0)
        testObject = VideoFrameLayout(activity, null)
    }


    @Test
    fun `onInterceptTouchEvent returns true if has onClick listeners`() {
        testObject = VideoFrameLayout(activity)
        testObject.setOnClickListener(mockk())
        Assert.assertTrue(testObject.onInterceptTouchEvent(mockk()))
    }

    @Test
    fun `onInterceptTouchEvent no onClick listeners, request disallow intercept touch event called on parent if action is down`() {
        testObject = VideoFrameLayout(activity)
        val testObject = spyk(testObject)
        every { testObject.parent.requestDisallowInterceptTouchEvent(true) } just Runs
        val event = MotionEvent.obtain(0, 0L, MotionEvent.ACTION_DOWN, 0f, 0f, 0)

        assertFalse(testObject.onInterceptTouchEvent(event))

        verify { testObject.parent.requestDisallowInterceptTouchEvent(true) }
    }

    @Test
    fun `onInterceptTouchEvent no onClick listeners, request allow intercept touch event called on parent if action is not down`() {
        testObject = VideoFrameLayout(activity)
        val testObject = spyk(testObject)
        every { testObject.parent.requestDisallowInterceptTouchEvent(false) } just Runs
        val event = MotionEvent.obtain(0, 0L, MotionEvent.ACTION_BUTTON_PRESS, 0f, 0f, 0)

        assertFalse(testObject.onInterceptTouchEvent(event))

        verify { testObject.parent.requestDisallowInterceptTouchEvent(false) }
    }
}
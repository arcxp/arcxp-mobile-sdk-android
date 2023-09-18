package com.arcxp.video.views

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.arcxp.sdk.R


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ArcVideoFrameTest {
    @RelaxedMockK private lateinit var activity: Activity
    @RelaxedMockK private lateinit var expectedBackground: Drawable

    private lateinit var testObject: ArcVideoFrame

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Test
    fun `setSelected chooses selected background given true`() {
        every { activity.resources.getDrawable(R.drawable.selected_outline)} returns expectedBackground
        testObject = ArcVideoFrame(activity)

        testObject.isSelected = true

        assertEquals(expectedBackground, testObject.background)
    }

    @Test
    fun `setSelected chooses white background given false`() {
        every { activity.resources.getDrawable(android.R.color.white)} returns expectedBackground
        testObject = ArcVideoFrame(activity)

        testObject.isSelected = false

        assertEquals(expectedBackground, testObject.background)
    }
}
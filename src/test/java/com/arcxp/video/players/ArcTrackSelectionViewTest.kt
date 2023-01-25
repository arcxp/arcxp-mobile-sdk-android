package com.arcxp.video.players

import android.app.Activity
import android.util.Pair
import android.view.View
import android.widget.CheckedTextView
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.RendererCapabilities
import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.ui.TrackNameProvider
import io.mockk.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk= [28])
class ArcTrackSelectionViewTest {
    private lateinit var activity: Activity

    private lateinit var testObject: ArcTrackSelectionView
    private lateinit var disableView: CheckedTextView
    private lateinit var defaultView: CheckedTextView

    @Before
    fun setUp() {
        // Create an activity (Can be any sub-class: i.e. AppCompatActivity, FragmentActivity, etc)
        activity = Robolectric.buildActivity(Activity::class.java).get()

        // Create the view using the activity context
        testObject = ArcTrackSelectionView(activity)
        disableView = testObject.getChildAt(0) as CheckedTextView
        defaultView = testObject.getChildAt(2) as CheckedTextView
    }

    @Test
    fun `constructor initializes views`() {
        assertEquals("None", disableView.text)
        assertFalse(disableView.isEnabled)
        assertTrue(disableView.isFocusable)
        assertTrue(disableView.hasOnClickListeners())
        assertEquals(View.GONE, disableView.visibility)
        assertEquals("Auto", defaultView.text)
        assertFalse(defaultView.isEnabled)
        assertTrue(defaultView.isFocusable)
        assertTrue(disableView.hasOnClickListeners())
    }

    @Test
    fun `init updates views when renderer disabled, trackGroup length 0`() {
        val trackSelector = mockk<DefaultTrackSelector>()
        val trackFilter = mockk<ArcTrackSelectionView.TrackFilter>()
        val trackInfo = mockk<MappingTrackSelector.MappedTrackInfo>()
        val trackGroups = mockk<TrackGroupArray>()
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        val override = mockk<DefaultTrackSelector.SelectionOverride>()
        val group = mockk<TrackGroup>(relaxed = true)
        every { trackSelector.currentMappedTrackInfo } returns trackInfo
        every { trackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(123) } returns true
        every { parameters.getSelectionOverride(123, trackGroups) } returns override
        every { trackInfo.getTrackGroups(123) } returns trackGroups
        every { trackGroups.get(0) } returns group

        testObject.init(trackSelector, 123, trackFilter)
        assertTrue(disableView.isEnabled)
        assertTrue(defaultView.isEnabled)
        assertTrue(disableView.isChecked)
        assertFalse(defaultView.isChecked)
    }

    @Test
    fun `init updates views when renderer enabled, trackGroup length 0`() {
        val trackSelector = mockk<DefaultTrackSelector>()
        val trackFilter = mockk<ArcTrackSelectionView.TrackFilter>()
        val trackInfo = mockk<MappingTrackSelector.MappedTrackInfo>()
        val trackGroups = mockk<TrackGroupArray>()
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        val override = mockk<DefaultTrackSelector.SelectionOverride>()
        val group = mockk<TrackGroup>(relaxed = true)
        every { trackSelector.currentMappedTrackInfo } returns trackInfo
        every { trackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(123) } returns false
        every { parameters.getSelectionOverride(123, trackGroups) } returns override
        every { trackInfo.getTrackGroups(123) } returns trackGroups
        every { trackGroups.get(0) } returns group

        testObject.init(trackSelector, 123, trackFilter)
        assertTrue(disableView.isEnabled)
        assertTrue(defaultView.isEnabled)
        assertFalse(disableView.isChecked)
        assertFalse(defaultView.isChecked)
    }

    @Test
    fun `init updates views when renderer enabled, trackGroup length non-zero, individual group lengths zero`() {
        val trackSelector = mockk<DefaultTrackSelector>()
        val trackFilter = mockk<ArcTrackSelectionView.TrackFilter>()
        val trackInfo = mockk<MappingTrackSelector.MappedTrackInfo>()
        val trackGroup1 = mockk<TrackGroup>(relaxed = true)
        val trackGroup2 = mockk<TrackGroup>(relaxed = true)
        val trackGroup3 = mockk<TrackGroup>(relaxed = true)
        val trackGroups = TrackGroupArray(trackGroup1, trackGroup2, trackGroup3)
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        val override = mockk<DefaultTrackSelector.SelectionOverride>()
        every { trackSelector.currentMappedTrackInfo } returns trackInfo
        every { trackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(123) } returns false
        every { parameters.getSelectionOverride(123, trackGroups) } returns override
        every { trackInfo.getTrackGroups(123) } returns trackGroups

        testObject.init(trackSelector, 123, trackFilter)
        assertTrue(disableView.isEnabled)
        assertTrue(defaultView.isEnabled)
        assertFalse(disableView.isChecked)
        assertFalse(defaultView.isChecked)
    }

    @Test
    fun `init updates views when renderer enabled, trackGroup length non-zero, individual group lengths non-zero, track filter  true, enableAdaptiveSelections false, track support handled, override contains tracks`() {
        val trackSelector = mockk<DefaultTrackSelector>()
        val trackFilter = mockk<ArcTrackSelectionView.TrackFilter>()
        every { trackFilter.filter(any(), any()) } returns true
        val trackInfo = mockk<MappingTrackSelector.MappedTrackInfo>(relaxed = true)
        every { trackInfo.getTrackSupport(123, 0, 0) } returns RendererCapabilities.FORMAT_HANDLED
        val format = mockk<Format>()
        val trackGroup1 = TrackGroup(format)
        val trackGroups = TrackGroupArray(trackGroup1)
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        val override = mockk<DefaultTrackSelector.SelectionOverride>()
        every { override.containsTrack(any()) } returns true
        every { trackSelector.currentMappedTrackInfo } returns trackInfo
        every { trackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(123) } returns false
        every { parameters.getSelectionOverride(123, trackGroups) } returns override
        every { trackInfo.getTrackGroups(123) } returns trackGroups
        val trackNameProvider = mockk<TrackNameProvider>()
        every { trackNameProvider.getTrackName(format) } returns "track name"

        testObject.setTrackNameProvider(trackNameProvider)
        testObject.init(trackSelector, 123, trackFilter)

        val trackView = testObject.getChildAt(4) as CheckedTextView
        assertTrue(disableView.isEnabled)
        assertTrue(defaultView.isEnabled)
        assertFalse(disableView.isChecked)
        assertFalse(defaultView.isChecked)

        assertEquals(View.VISIBLE, trackView.visibility)

        assertEquals("track name", trackView.text.toString())
        assertEquals(Pair(0, 0), trackView.tag)
        assertTrue(trackView.isFocusable)
        assertTrue(trackView.isChecked)
        assertTrue(trackView.hasOnClickListeners())
    }

    @Test
    fun `init updates views when renderer enabled, trackGroup length non-zero, individual group lengths non-zero, track filter true, enableAdaptiveSelections true, track support handled, override contains tracks`() {
        val trackSelector = mockk<DefaultTrackSelector>()
        val trackFilter = mockk<ArcTrackSelectionView.TrackFilter>()
        every { trackFilter.filter(any(), any()) } returns true
        val trackInfo = mockk<MappingTrackSelector.MappedTrackInfo>(relaxed = true)
        every { trackInfo.getTrackSupport(123, 0, 0) } returns RendererCapabilities.FORMAT_HANDLED
        every { trackInfo.getAdaptiveSupport(123, 0, false) } returns 1
        val format = mockk<Format>()
        val trackGroup1 = TrackGroup(format, format)
        val trackGroups = TrackGroupArray(trackGroup1)
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        val override = mockk<DefaultTrackSelector.SelectionOverride>()
        every { override.containsTrack(any()) } returns true
        every { trackSelector.currentMappedTrackInfo } returns trackInfo
        every { trackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(123) } returns false
        every { parameters.getSelectionOverride(123, trackGroups) } returns override
        every { trackInfo.getTrackGroups(123) } returns trackGroups
        val trackNameProvider = mockk<TrackNameProvider>()
        every { trackNameProvider.getTrackName(format) } returns "track name"

        testObject.setTrackNameProvider(trackNameProvider)
        testObject.setAllowAdaptiveSelections(true)
        testObject.init(trackSelector, 123, trackFilter)

        val trackView = testObject.getChildAt(4) as CheckedTextView
        assertTrue(disableView.isEnabled)
        assertTrue(defaultView.isEnabled)
        assertFalse(disableView.isChecked)
        assertFalse(defaultView.isChecked)

        assertEquals(View.VISIBLE, trackView.visibility)

        assertEquals("track name", trackView.text.toString())
        assertEquals(Pair(0, 0), trackView.tag)
        assertTrue(trackView.isFocusable)
        assertTrue(trackView.isChecked)
        assertTrue(trackView.hasOnClickListeners())
    }

    @Test
    fun `init when renderer enabled, trackGroup length non-zero, group lengths non-zero, track filter false, enableAdaptiveSelections false,  track support not handled, override does not contain tracks`() {
        val trackSelector = mockk<DefaultTrackSelector>()
        val trackFilter = mockk<ArcTrackSelectionView.TrackFilter>()
        every { trackFilter.filter(any(), any()) } returns false
        val trackInfo = mockk<MappingTrackSelector.MappedTrackInfo>(relaxed = true)
        every {
            trackInfo.getTrackSupport(
                123,
                0,
                0
            )
        } returns RendererCapabilities.FORMAT_EXCEEDS_CAPABILITIES
        val format = mockk<Format>()
        val trackGroup1 = TrackGroup(format)
        val trackGroups = TrackGroupArray(trackGroup1)
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        val override = mockk<DefaultTrackSelector.SelectionOverride>()
        every { override.containsTrack(any()) } returns false
        every { trackSelector.currentMappedTrackInfo } returns trackInfo
        every { trackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(123) } returns false
        every { parameters.getSelectionOverride(123, trackGroups) } returns override
        every { trackInfo.getTrackGroups(123) } returns trackGroups
        val trackNameProvider = mockk<TrackNameProvider>()
        every { trackNameProvider.getTrackName(format) } returns "track name"

        testObject.setTrackNameProvider(trackNameProvider)
        testObject.init(trackSelector, 123, trackFilter)

        val trackView = testObject.getChildAt(4) as CheckedTextView
        assertTrue(disableView.isEnabled)
        assertTrue(defaultView.isEnabled)
        assertFalse(disableView.isChecked)
        assertFalse(defaultView.isChecked)

        assertEquals(View.GONE, trackView.visibility)

        assertEquals("track name", trackView.text.toString())
        assertNull(trackView.tag)
        assertFalse(trackView.isFocusable)
        assertFalse(trackView.isEnabled)
        assertFalse(trackView.isChecked)
        assertFalse(trackView.hasOnClickListeners())
    }


//    @Test
//    fun `Alert Dialog on Click`() {
//        val pair = ArcTrackSelectionView.getDialog(
//            activity,
//            "title",
//            trackSelector,
//            123,
//            trackFilter
//        )
//        val dialog = pair.first as AlertDialog
//        val selectionView = pair.second as ArcTrackSelectionView
//
//        dialog.show()
//        val ok: Button = dialog.getButton(Dialog.BUTTON_POSITIVE)
//        ok.performClick()

        //TODO can capture the dialog and perform click, but having trouble with the DefaultTrackSelector
        // parameters & parameters Builder mocking, tried mockito, powermock, and mockk
//    }

    @Test
    fun `setShowDisableOption sets visibility on disableView to visible when given true`() {
        testObject.setShowDisableOption(true)
        assertEquals(View.VISIBLE, disableView.visibility)
    }

    @Test
    fun `setShowDisableOption sets visibility on disableView to gone when given false`() {
        testObject.setShowDisableOption(false)
        assertEquals(View.GONE, disableView.visibility)
    }

    @Test(expected = NullPointerException::class)
    fun `setTrackNameProvider throws exception if given null provider`() {
        testObject.setTrackNameProvider(null)
    }

    @Test
    fun `disableView onClick sets boolean true and nulls override`() {
        val trackSelector = mockk<DefaultTrackSelector>()
        val trackFilter = mockk<ArcTrackSelectionView.TrackFilter>()
        val trackInfo = mockk<MappingTrackSelector.MappedTrackInfo>()
        val trackGroups = mockk<TrackGroupArray>()
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        val override = mockk<DefaultTrackSelector.SelectionOverride>()
        val group = mockk<TrackGroup>(relaxed = true)
        every { trackSelector.currentMappedTrackInfo } returns trackInfo
        every { trackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(123) } returns false
        every { parameters.getSelectionOverride(123, trackGroups) } returns override
        every { trackInfo.getTrackGroups(123) } returns trackGroups
        every { trackGroups.get(0) } returns group
        testObject.init(trackSelector, 123, trackFilter)
        assertNotNull(testObject.override)
        assertFalse(testObject.isDisabled)

        disableView.callOnClick()

        assertTrue(testObject.isDisabled)
        assertNull(testObject.override)
    }

    @Test
    fun `defaultView onClick sets boolean false and nulls override`() {
        val trackSelector = mockk<DefaultTrackSelector>()
        val trackFilter = mockk<ArcTrackSelectionView.TrackFilter>()
        val trackInfo = mockk<MappingTrackSelector.MappedTrackInfo>()
        val trackGroups = mockk<TrackGroupArray>()
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        val override = mockk<DefaultTrackSelector.SelectionOverride>()
        val group = mockk<TrackGroup>(relaxed = true)
        every { trackSelector.currentMappedTrackInfo } returns trackInfo
        every { trackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(123) } returns true
        every { parameters.getSelectionOverride(123, trackGroups) } returns override
        every { trackInfo.getTrackGroups(123) } returns trackGroups
        every { trackGroups.get(0) } returns group
        testObject.init(trackSelector, 123, trackFilter)
        assertNotNull(testObject.override)
        assertTrue(testObject.isDisabled)

        defaultView.callOnClick()

        assertFalse(testObject.isDisabled)
        assertNull(testObject.override)
    }

    @Test
    fun `TrackView onClick given multiple override tracks, but not checked adds track to new override`() {
        val trackSelector = mockk<DefaultTrackSelector>()
        val trackFilter = mockk<ArcTrackSelectionView.TrackFilter>()
        every { trackFilter.filter(any(), any()) } returns true
        val trackInfo = mockk<MappingTrackSelector.MappedTrackInfo>(relaxed = true)
        every { trackInfo.getTrackSupport(123, 0, 0) } returns RendererCapabilities.FORMAT_HANDLED
        every { trackInfo.getAdaptiveSupport(123, 0, false) } returns 1
        val format = mockk<Format>()
        val trackGroup1 = TrackGroup(format, format)
        val trackGroups = TrackGroupArray(trackGroup1)
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        val override = DefaultTrackSelector.SelectionOverride(0, 1,2,3, 0, 0)


        every { trackSelector.currentMappedTrackInfo } returns trackInfo
        every { trackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(123) } returns false
        every { parameters.getSelectionOverride(123, trackGroups) } returns override
        every { trackInfo.getTrackGroups(123) } returns trackGroups
        val trackNameProvider = mockk<TrackNameProvider>()
        every { trackNameProvider.getTrackName(format) } returns "track name"
        testObject.setTrackNameProvider(trackNameProvider)
        testObject.setAllowAdaptiveSelections(true)
        testObject.init(trackSelector, 123, trackFilter)
        val trackView = testObject.getChildAt(4) as CheckedTextView
        assertEquals(5, testObject.override!!.tracks.size)

        trackView.callOnClick()
        assertEquals(3, testObject.override!!.tracks.size)


    }

    @Test
    fun `TrackView onClick view checked, override length 1 disables and nulls override`() {
        val trackSelector = mockk<DefaultTrackSelector>()
        val trackFilter = mockk<ArcTrackSelectionView.TrackFilter>()
        every { trackFilter.filter(any(), any()) } returns true
        val trackInfo = mockk<MappingTrackSelector.MappedTrackInfo>(relaxed = true)
        every { trackInfo.getTrackSupport(123, 0, 0) } returns RendererCapabilities.FORMAT_HANDLED
        every { trackInfo.getAdaptiveSupport(123, 0, false) } returns 1
        val format = mockk<Format>()
        val trackGroup1 = TrackGroup(format, format)
        val trackGroups = TrackGroupArray(trackGroup1)
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        val override = DefaultTrackSelector.SelectionOverride(0, 1)


        every { trackSelector.currentMappedTrackInfo } returns trackInfo
        every { trackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(123) } returns false
        every { parameters.getSelectionOverride(123, trackGroups) } returns override
        every { trackInfo.getTrackGroups(123) } returns trackGroups
        val trackNameProvider = mockk<TrackNameProvider>()
        every { trackNameProvider.getTrackName(format) } returns "track name"
        testObject.setTrackNameProvider(trackNameProvider)
        testObject.setAllowAdaptiveSelections(true)
        testObject.init(trackSelector, 123, trackFilter)
        val trackView = testObject.getChildAt(4) as CheckedTextView

        trackView.isChecked = true
        assertNotNull(testObject.override)
        assertFalse(testObject.isDisabled)


        trackView.callOnClick()


        assertNull(testObject.override)
        assertTrue(testObject.isDisabled)

    }

    @Test
    fun `TrackView onClick given multiple override tracks, view checked, override length greater than 1 disables and nulls override`() {
        val trackSelector = mockk<DefaultTrackSelector>()
        val trackFilter = mockk<ArcTrackSelectionView.TrackFilter>()
        every { trackFilter.filter(any(), any()) } returns true
        val trackInfo = mockk<MappingTrackSelector.MappedTrackInfo>(relaxed = true)
        every { trackInfo.getTrackSupport(123, 0, 0) } returns RendererCapabilities.FORMAT_HANDLED
        every { trackInfo.getAdaptiveSupport(123, 0, false) } returns 1
        val format = mockk<Format>()
        val trackGroup1 = TrackGroup(format, format)
        val trackGroups = TrackGroupArray(trackGroup1)
        val parameters = mockk<DefaultTrackSelector.Parameters>()
        val override = DefaultTrackSelector.SelectionOverride(0, 0,1,2,3,4, 0, 0)


        every { trackSelector.currentMappedTrackInfo } returns trackInfo
        every { trackSelector.parameters } returns parameters
        every { parameters.getRendererDisabled(123) } returns false
        every { parameters.getSelectionOverride(123, trackGroups) } returns override
        every { trackInfo.getTrackGroups(123) } returns trackGroups
        val trackNameProvider = mockk<TrackNameProvider>()
        every { trackNameProvider.getTrackName(format) } returns "track name"
        testObject.setTrackNameProvider(trackNameProvider)
        testObject.setAllowAdaptiveSelections(true)
        testObject.init(trackSelector, 123, trackFilter)
        val trackView = testObject.getChildAt(4) as CheckedTextView

        trackView.isChecked = true
        assertNotNull(testObject.override)
        assertFalse(testObject.isDisabled)


        trackView.callOnClick()


        assertArrayEquals(intArrayOf(1,2,3,4), testObject.override!!.tracks)
        assertEquals(0, testObject.override!!.groupIndex)
        assertFalse(testObject.isDisabled)

    }

    @Test
    fun `setShowDefault sets default view visible when true`() {
        testObject.setShowDefault(true)
        assertEquals(View.VISIBLE, defaultView.visibility)
    }

    @Test
    fun `setShowDefault sets default view gone when false`() {
        testObject.setShowDefault(false)
        assertEquals(View.GONE, defaultView.visibility)

    }
}
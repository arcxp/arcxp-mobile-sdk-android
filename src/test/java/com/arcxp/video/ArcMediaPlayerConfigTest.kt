package com.arcxp.video

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ArcMediaPlayerConfigTest {

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)

    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

//    @Test
//    fun `setViewsToHide builder passes views to object correctly`() {
//        val view1 = mockk<View>()
//        val view2 = mockk<View>()
//        val view3 = mockk<View>()
//        val actual = ArcMediaPlayerConfig.Builder().setViewsToHide(view1, view2, view3).build()
//
//        assertEquals(listOf(view1, view2, view3), actual.viewsToHide)
//    }

//    @Test
//    fun `addViewsToHide builder passes views to object correctly`() {
//        val view1 = mockk<View>()
//        val view2 = mockk<View>()
//        val view3 = mockk<View>()
//        val actual = ArcMediaPlayerConfig.Builder()
//            .addViewToHide(view1)
//            .addViewToHide(view2)
//            .addViewToHide(view3)
//            .build()
//
//        assertEquals(listOf(view1, view2, view3), actual.viewsToHide)
//    }

//    @Test
//    fun `addOverlay builder passes values to object correctly`() {
//        val view1 = mockk<View>()
//        val view2 = mockk<View>()
//        val view3 = mockk<View>()
//        val actual = ArcMediaPlayerConfig.Builder()
//            .addOverlay("overlay1", view1)
//            .addOverlay("overlay2", view2)
//            .addOverlay("overlay3", view3)
//            .build()
//
//        assertEquals(3, actual.overlays.size)
//        assertEquals(view1, actual.overlays["overlay1"])
//        assertEquals(view2, actual.overlays["overlay2"])
//        assertEquals(view3, actual.overlays["overlay3"])
//
//    }

    @Test
    fun `addAdParam builder passes values to object correctly`() {
        val actual = ArcMediaPlayerConfig.Builder()
            .addAdParam("adParam1key", "adParam1Value")
            .addAdParam("adParam2key", "adParam2Value")
            .addAdParam("adParam3key", "adParam3Value")
            .build()

        assertEquals(3, actual.adParams.size)
        assertEquals("adParam1Value", actual.adParams["adParam1key"])
        assertEquals("adParam2Value", actual.adParams["adParam2key"])
        assertEquals("adParam3Value", actual.adParams["adParam3key"])
    }


//    @Test
//    fun `builder passes values to object correctly`() {
//        val activity = mockk<Activity>()
//        val videoFrame = mockk<ArcVideoFrame>()
//        val adConfig = mockk<AdConfig>()
//        val arcCastManager = mockk<ArcCastManager>()
//        val actual = ArcMediaPlayerConfig.Builder()
//            .setActivity(activity)
//            .setVideoFrame(videoFrame)
//            .enablePip(true)
//            .showClosedCaption(true)
//            .showCountdown(false)
//            .showProgressBar(false)
//            .showSeekButton(false)
//            .setUseLegacyPlayer(true)
//            .setAdsEnabled(true)
//            .setAdUrl("ad url")
//            .setAdConfig(adConfig)
//            .setPreferredStreamType(ArcMediaPlayerConfig.PreferredStreamType.GIFMP4)
//            .setMaxBitRate(1)
//            .setServerSideAdsEnabled(true)
//            .setClientSideAdsEnabled(true)
//            .setAutoStartPlay(false)
//            .setStartMuted(true)
//            .setFocusSkipButton(false)
//            .setCcStartMode(ArcMediaPlayerConfig.CCStartMode.ON)
//            .setAutoShowControls(false)
//            .setShowClosedCaptionTrackSelection(false)
//            .setCastManager(arcCastManager)
//            .enablePAL(true)
//            .setPalPpid("palPpid")
//            .setOmidVersionName("123")
//            .setPalVersionName("456")
//            .setPalPartnerName("palpartner")
//            .enableOpenMeasurement(true)
//            .setControlsShowTimeoutMs(2)
//            .enableLogging()
//            .useDialogForFullscreen(true)
//            .setKeepControlsSpaceOnHide(false)
//            .setDisableControlsToggleWithTouch(true)
//            .setUserAgent("agent")
//            .setShowNextPreviousButtons(true)
//            .setShouldDisableNextButton(true)
//            .setShouldDisablePreviousButton(true)
//            .build()
//
//        assertEquals(activity, actual.activity)
//        assertEquals(videoFrame, actual.videoFrame)
//        assertTrue(actual.isEnablePip)
//        assertTrue(actual.enableClosedCaption())
//        assertFalse(actual.isShowCountDown)
//        assertFalse(actual.isShowProgressBar)
//        assertFalse(actual.isShowSeekButton)
//        assertTrue(actual.isUseLegacyPlayer)
//        assertTrue(actual.isEnableAds)
//        assertEquals("ad url", actual.adConfigUrl)
//        assertEquals(adConfig, actual.adConfig)
//        assertEquals(
//            ArcMediaPlayerConfig.PreferredStreamType.GIFMP4,
//            actual.getPreferredStreamType()
//        )
//        assertEquals(1, actual.maxBitRate)
//        assertTrue(actual.isEnableServerSideAds)
//        assertTrue(actual.isEnableClientSideAds)
//        assertFalse(actual.isAutoStartPlay)
//        assertTrue(actual.isStartMuted)
//        assertFalse(actual.isFocusSkipButton)
//        assertEquals(ArcMediaPlayerConfig.CCStartMode.ON, actual.ccStartMode)
//        assertFalse(actual.isAutoShowControls)
//        assertFalse(actual.isShowClosedCaptionTrackSelection)
//        assertEquals(arcCastManager, actual.arcCastManager)
//        assertTrue(actual.isEnablePAL)
//        assertEquals("palpartner", actual.palPartnerName)
//        assertEquals("palPpid", actual.palPpid)
//        assertEquals("456", actual.palVersionName)
//        assertTrue(actual.isEnableOmid)
//        assertEquals("washpost", actual.omidPartnerName)
//        assertEquals("wapo", actual.omidPpid)
//        assertEquals("123", actual.omidVersionName)
//        assertEquals("2.13.3", actual.exoplayerVersion)
//        assertEquals(2, actual.controlsShowTimeoutMs)
//        assertTrue(actual.isLoggingEnabled)
//        assertTrue(actual.isUseFullScreenDialog)
//        assertFalse(actual.isKeepControlsSpaceOnHide)
//        assertTrue(actual.isDisableControlsWithTouch)
//        assertEquals("agent", actual.userAgent)
//        assertTrue(actual.showNextPreviousButtons)
//        assertTrue(actual.shouldDisableNextButton)
//        assertTrue(actual.shouldDisablePreviousButton)
//    }

    @Test
    fun `PreferredStreamType next cycles through correctly`() {
        assertTrue(
            ArcMediaPlayerConfig.PreferredStreamType.HLS.next()
                    == ArcMediaPlayerConfig.PreferredStreamType.TS
        )
        assertTrue(
            ArcMediaPlayerConfig.PreferredStreamType.TS.next()
                    == ArcMediaPlayerConfig.PreferredStreamType.MP4
        )
        assertTrue(
            ArcMediaPlayerConfig.PreferredStreamType.MP4.next()
                    == ArcMediaPlayerConfig.PreferredStreamType.GIF
        )
        assertTrue(
            ArcMediaPlayerConfig.PreferredStreamType.GIF.next()
                    == ArcMediaPlayerConfig.PreferredStreamType.GIFMP4
        )
        assertTrue(
            ArcMediaPlayerConfig.PreferredStreamType.GIFMP4.next()
                    == ArcMediaPlayerConfig.PreferredStreamType.HLS
        )

    }
}

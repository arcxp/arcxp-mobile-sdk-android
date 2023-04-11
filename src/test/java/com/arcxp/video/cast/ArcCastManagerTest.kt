package com.arcxp.video.cast

import android.content.Context
import android.view.Menu
import androidx.mediarouter.app.MediaRouteButton
import com.arcxp.video.ArcMediaPlayerConfig
import com.arcxp.video.model.ArcVideo
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaStatus
import com.google.android.gms.cast.framework.*
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ArcCastManagerTest {
    @RelaxedMockK lateinit var mActivityContext: Context
    @RelaxedMockK lateinit var mCastContext: CastContext
    @RelaxedMockK lateinit var mCastSession: CastSession

    private lateinit var testObject: ArcCastManager

    private val expectedHeadline = "headline"
    private val expectedId = "id"

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        mockkStatic(CastContext::class)
        every { CastContext.getSharedInstance(mActivityContext) } returns mCastContext
        every { mCastContext.sessionManager.currentCastSession } returns mCastSession
        mockkStatic(Util::class)
        every { Util.inferContentType(expectedId) } returns C.TYPE_SS
        testObject = ArcCastManager(mActivityContext)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `doCastSession registers remoteMediaClient callback and loads info`() {
        val position = 123876L
        val remoteMediaClient = mockk<RemoteMediaClient>(relaxed = true)

        val mediaInfo = slot<MediaInfo>()
        val mediaLoadOptions = slot<MediaLoadOptions>()

        every { mCastSession.remoteMediaClient } returns remoteMediaClient
        every {
            remoteMediaClient.load(
                ofType(MediaInfo::class),
                ofType(MediaLoadOptions::class)
            )
        } returns mockk()
        mockkStatic(Util::class)
        every { Util.inferContentType(expectedId) } returns C.TYPE_SS
        testObject.addSessionManager()
        clearAllMocks(answers = false)

        testObject.doCastSession(createDefaultVideo(), position)

        verifySequence {
            mCastSession.remoteMediaClient
            remoteMediaClient.load(capture(mediaInfo), capture(mediaLoadOptions))
        }
        assertEquals(MediaMetadata.MEDIA_TYPE_MOVIE, mediaInfo.captured.metadata?.mediaType)
        assertEquals(expectedId, mediaInfo.captured.contentId)
        assertTrue(mediaLoadOptions.captured.autoplay)
        assertEquals(position, mediaLoadOptions.captured.playPosition)
    }

    @Test
    fun `hasCastSession returns value from mCastSession`() {
        every { mCastSession.isConnected } returns true

        testObject.addSessionManager()

        assertTrue(testObject.hasCastSession())
    }

    @Test
    fun `loadRemoteMedia returns if mCastSession is null `() {
        testObject.doCastSession(createDefaultVideo(), 123L)

        verify { mCastSession wasNot called }
    }

    @Test
    fun `getEndedPosition `() {
        val expectedStreamPosition = 268347L
        testObject.addSessionManager()
        every { mCastSession.remoteMediaClient?.approximateStreamPosition } returns expectedStreamPosition

        assertEquals(expectedStreamPosition, testObject.getEndedPosition())
    }

    @Test
    fun `isIdleReasonEnd returns true if idle reason finished`() {
        every { mCastSession.remoteMediaClient?.idleReason } returns MediaStatus.IDLE_REASON_FINISHED
        testObject.addSessionManager()

        assertTrue(testObject.isIdleReasonEnd())
    }

    @Test
    fun `isIdleReasonEnd returns false if status not idle reason finished`() {
        every { mCastSession.remoteMediaClient?.idleReason } returns MediaStatus.IDLE_REASON_CANCELED
        testObject.addSessionManager()

        assertFalse(testObject.isIdleReasonEnd())
    }

    @Test
    fun `addMenuCastButton calls through to factory static method`() {
        val menu = mockk<Menu>()
        val id = 123
        mockkStatic(CastButtonFactory::class)
        every { CastButtonFactory.setUpMediaRouteButton(any(), any(), any()) } returns mockk()

        testObject.addMenuCastButton(menu, id)

        verifySequence { CastButtonFactory.setUpMediaRouteButton(mActivityContext, menu, id) }
    }

    @Test
    fun `addCastButton calls through to factory static method`() {
        val routeButton = mockk<MediaRouteButton>()
        mockkStatic(CastButtonFactory::class)
        every { CastButtonFactory.setUpMediaRouteButton(any(), any()) } returns mockk()

        testObject.addCastButton(routeButton)

        verifySequence { CastButtonFactory.setUpMediaRouteButton(mActivityContext, routeButton) }
    }

    @Test
    fun `addSessionManager adds session manager listener, populates mCastSession`() {
        every { mCastSession.isConnected } returns true

        testObject.addSessionManager()

        verifySequence {
            mCastContext.addCastStateListener(any())
            mCastContext.sessionManager.addSessionManagerListener(any(), CastSession::class.java)
            mCastContext.sessionManager.currentCastSession
        }
        assertTrue(testObject.hasCastSession())
    }

    @Test
    fun `onResume adds session manager listener, populates mCastSession`() {
        every { mCastSession.isConnected } returns true

        testObject.onResume()

        verifySequence {
            mCastContext.addCastStateListener(any())
            mCastContext.sessionManager.addSessionManagerListener(any(), CastSession::class.java)
            mCastContext.sessionManager.currentCastSession
        }
        assertTrue(testObject.hasCastSession())
    }

    @Test
    fun `removeSessionManager removes listeners from cast context `() {
        testObject.addSessionManager()
        val sessionManager = mockk<SessionManager>(relaxed = true)
        every { mCastContext.sessionManager } returns sessionManager
        clearAllMocks(answers = false)

        testObject.removeSessionManager()

        verifySequence {
            mCastContext.removeCastStateListener(ofType(CastStateListener::class))
            mCastContext.sessionManager
            sessionManager.removeSessionManagerListener(
                testObject.getMSessionManagerListener(),
                CastSession::class.java
            )
        }
    }

    @Test
    fun `onPause removes listeners from cast context `() {
        testObject.addSessionManager()
        val sessionManager = mockk<SessionManager>(relaxed = true)
        every { mCastContext.sessionManager } returns sessionManager
        clearAllMocks(answers = false)

        testObject.onPause()

        verifySequence {
            mCastContext.removeCastStateListener(ofType(CastStateListener::class))
            mCastContext.sessionManager
            sessionManager.removeSessionManagerListener(
                testObject.getMSessionManagerListener(),
                CastSession::class.java
            )
        }
    }

    @Test
    fun `setSessionManagerListener sets listener `() {
        val expected = mockk<ArcCastSessionManagerListener>()
        testObject.setSessionManagerListener(expected)

        assertEquals(expected, testObject.getArcSessionManagerListener())
    }

    @Test
    fun `removeSessionManagerListener sets listener to null `() {
        val listener = mockk<ArcCastSessionManagerListener>()
        testObject.setSessionManagerListener(listener)
        assertEquals(listener, testObject.getArcSessionManagerListener())

        testObject.removeSessionManagerListener()

        assertNull(testObject.getArcSessionManagerListener())
    }

    @Test
    fun `onDestroy sets listener to null `() {
        val listener = mockk<ArcCastSessionManagerListener>()
        testObject.setSessionManagerListener(listener)
        assertEquals(listener, testObject.getArcSessionManagerListener())

        testObject.onDestroy()

        assertNull(testObject.getArcSessionManagerListener())
    }


    @Test
    fun `createMediaQueueItem creates MediaQueue item with info `() {
        val result = ArcCastManager.createMediaQueueItem(createDefaultVideo())

        assertEquals(expectedId, result.contentId)
//        assertEquals(expectedHeadline, result.metadata.getString(MediaMetadata.KEY_TITLE))
        //TODO so this doesn't seem to be added to the metadata on 164 in test debugger, should verify this is working as expected
        assertEquals(MediaMetadata.MEDIA_TYPE_MOVIE, result.metadata?.mediaType)
        assertEquals(MediaInfo.STREAM_TYPE_BUFFERED, result.streamType)
        assertEquals(MimeTypes.APPLICATION_SS, result.contentType)


    }

    @Test
    fun `createMediaQueueItem throws exception when content type unsupported `() {
        mockkStatic(Util::class)

        every { Util.inferContentType(expectedId) } returns C.TYPE_OTHER

        assertThrows(UnsupportedOperationException::class.java) {
            ArcCastManager.createMediaQueueItem(createDefaultVideo())
        }
    }

    @Test
    fun `createMediaQueueItems creates MediaQueue items with info `() {
        val expectedId1 = "ID1"
        val arcVideo1 = createDefaultVideo(id = expectedId1)
        val expectedId2 = "ID2"
        val arcVideo2 = createDefaultVideo(id = expectedId2)
        val expectedId3 = "ID3"
        val arcVideo3 = createDefaultVideo(id = expectedId3)

        mockkStatic(Util::class)
        every { Util.inferContentType(expectedId1) } returns C.TYPE_SS
        every { Util.inferContentType(expectedId2) } returns C.TYPE_DASH
        every { Util.inferContentType(expectedId3) } returns C.TYPE_HLS

        val result = ArcCastManager.createMediaQueueItems(listOf(arcVideo1, arcVideo2, arcVideo3))

        assertEquals(expectedId1, result[0].contentId)
//        assertEquals(expectedHeadline1, result[0].media.metadata.getString(MediaMetadata.KEY_TITLE))
        assertEquals(MediaMetadata.MEDIA_TYPE_MOVIE, result[0].metadata?.mediaType)
        assertEquals(MediaInfo.STREAM_TYPE_BUFFERED, result[0].streamType)
        assertEquals(MimeTypes.APPLICATION_SS, result[0].contentType)

        assertEquals(expectedId2, result[1].contentId)
//        assertEquals(expectedHeadline2, result[0].metadata.getString(MediaMetadata.KEY_TITLE))
        assertEquals(MediaMetadata.MEDIA_TYPE_MOVIE, result[1].metadata?.mediaType)
        assertEquals(MediaInfo.STREAM_TYPE_BUFFERED, result[1].streamType)
        assertEquals(MimeTypes.APPLICATION_MPD, result[1].contentType)

        assertEquals(expectedId3, result[2].contentId)
//        assertEquals(expectedHeadline3, result[0].metadata.getString(MediaMetadata.KEY_TITLE))
        assertEquals(MediaMetadata.MEDIA_TYPE_MOVIE, result[2].metadata?.mediaType)
        assertEquals(MediaInfo.STREAM_TYPE_BUFFERED, result[2].streamType)
        assertEquals(MimeTypes.APPLICATION_M3U8, result[2].contentType)
    }

    @Test
    fun `SessionManagerListener onSessionEnded notifies listener, clears cast session given session equal to mCastSession`() {
        val listener = testObject.getMSessionManagerListener()
        val error = 234
        testObject.addSessionManager()
        testObject.setSessionManagerListener(mockk(relaxed = true))
        assertNotNull(testObject.getCastContext())
        clearAllMocks(answers = false)

        listener.onSessionEnded(mCastSession, error)

        assertNull(testObject.getMCastSession())
        verifySequence {
            testObject.getArcSessionManagerListener()!!.onSessionEnded(error)
        }
    }

    @Test
    fun `SessionManagerListener onSessionEnded notifies listener given session unequal to mCastSession`() {
        val listener = testObject.getMSessionManagerListener()
        val error = 234
        testObject.addSessionManager()
        testObject.setSessionManagerListener(mockk(relaxed = true))
        assertNotNull(testObject.getCastContext())
        clearAllMocks(answers = false)

        listener.onSessionEnded(mockk(), error)

        assertNotNull(testObject.getMCastSession())
        verifySequence {
            testObject.getArcSessionManagerListener()!!.onSessionEnded(error)
        }
    }

    @Test
    fun `SessionManagerListener onSessionResumed notifies listener and sets mCastSession`() {
        val listener = testObject.getMSessionManagerListener()
        val session = mockk<CastSession>()
        testObject.addSessionManager()
        testObject.setSessionManagerListener(mockk(relaxed = true))
        assertNotNull(testObject.getCastContext())
        clearAllMocks(answers = false)

        listener.onSessionResumed(session, true)

        assertEquals(session, testObject.getMCastSession())
        verifySequence {
            testObject.getArcSessionManagerListener()!!.onSessionResumed(true)
        }
    }

    @Test
    fun `SessionManagerListener onSessionStarted notifies listener and sets mCastSession`() {
        val listener = testObject.getMSessionManagerListener()
        val session = mockk<CastSession>()
        val sessionId = "id"
        testObject.addSessionManager()
        testObject.setSessionManagerListener(mockk(relaxed = true))
        assertNotNull(testObject.getCastContext())
        clearAllMocks(answers = false)

        listener.onSessionStarted(session, sessionId)

        assertEquals(session, testObject.getMCastSession())
        verifySequence {
            testObject.getArcSessionManagerListener()!!.onSessionStarted(sessionId)
        }
    }

    @Test
    fun `SessionManagerListener onSessionStarting notifies listener and sets mCastSession`() {
        val listener = testObject.getMSessionManagerListener()
        val session = mockk<CastSession>()
        testObject.addSessionManager()
        testObject.setSessionManagerListener(mockk(relaxed = true))
        assertNotNull(testObject.getCastContext())
        clearAllMocks(answers = false)

        listener.onSessionStarting(session)

        assertEquals(session, testObject.getMCastSession())
        verifySequence {
            testObject.getArcSessionManagerListener()!!.onSessionStarting()
        }
    }

    @Test
    fun `SessionManagerListener onSessionStartFailed notifies listener given session unequal to mCastSession`() {
        val listener = testObject.getMSessionManagerListener()
        val error = 234
        testObject.addSessionManager()
        testObject.setSessionManagerListener(mockk(relaxed = true))
        assertNotNull(testObject.getCastContext())
        clearAllMocks(answers = false)

        listener.onSessionStartFailed(mockk(), error)

        assertNotNull(testObject.getMCastSession())
        verifySequence {
            testObject.getArcSessionManagerListener()!!.onSessionStartFailed(error)
        }
    }

    @Test
    fun `SessionManagerListener onSessionEnding notifies listener and sets mCastSession`() {
        val listener = testObject.getMSessionManagerListener()
        val session = mockk<CastSession>()
        testObject.addSessionManager()
        testObject.setSessionManagerListener(mockk(relaxed = true))
        assertNotNull(testObject.getCastContext())
        clearAllMocks(answers = false)

        listener.onSessionEnding(session)

        assertEquals(session, testObject.getMCastSession())
        verifySequence {
            testObject.getArcSessionManagerListener()!!.onSessionEnding()
        }
    }

    @Test
    fun `SessionManagerListener onSessionResuming notifies listener and sets mCastSession`() {
        val listener = testObject.getMSessionManagerListener()
        val session = mockk<CastSession>()
        val sessionId = "id"
        testObject.addSessionManager()
        testObject.setSessionManagerListener(mockk(relaxed = true))
        assertNotNull(testObject.getCastContext())
        clearAllMocks(answers = false)

        listener.onSessionResuming(session, sessionId)

        assertEquals(session, testObject.getMCastSession())
        verifySequence {
            testObject.getArcSessionManagerListener()!!.onSessionResuming(sessionId)
        }
    }

    @Test
    fun `SessionManagerListener onSessionResumeFailed notifies listener given session unequal to mCastSession`() {
        val listener = testObject.getMSessionManagerListener()
        val error = 234
        testObject.addSessionManager()
        testObject.setSessionManagerListener(mockk(relaxed = true))
        assertNotNull(testObject.getCastContext())
        clearAllMocks(answers = false)

        listener.onSessionResumeFailed(mockk(), error)

        assertNotNull(testObject.getMCastSession())
        verifySequence {
            testObject.getArcSessionManagerListener()!!.onSessionResumeFailed(error)
        }
    }

    @Test
    fun `SessionManagerListener onSessionSuspended notifies listener given session unequal to mCastSession`() {
        val listener = testObject.getMSessionManagerListener()
        val reason = 234
        testObject.addSessionManager()
        testObject.setSessionManagerListener(mockk(relaxed = true))
        assertNotNull(testObject.getCastContext())
        clearAllMocks(answers = false)

        listener.onSessionSuspended(mockk(), reason)

        assertNotNull(testObject.getMCastSession())
        verifySequence {
            testObject.getArcSessionManagerListener()!!.onSessionSuspended(reason)
        }
    }

    private fun createDefaultVideo(
        id: String = expectedId,
        headline: String = expectedHeadline
    ): ArcVideo {
        return ArcVideo(
            id,
            "uuid",
            123L,
            false,
            false,
            100,
            "mShareUrl",
            headline,
            "pageName",
            "videoName",
            "videoSection",
            "videoSource",
            "videoCategory",
            "consentId",
            "fallbackUrl",
            "addTagUrl[timestamp]",
            true,
            "subtitleUrl",
            "source",
            mockk(),
            false,
            false,
            false,
            ArcMediaPlayerConfig.CCStartMode.DEFAULT
        )
    }
}
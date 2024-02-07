package com.arcxp.video;

import static com.arcxp.video.util.TrackingExtentionsKt.eventTracking;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Rational;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.arcxp.ArcXPMobileSDK;
import com.arcxp.commons.throwables.ArcXPException;
import com.arcxp.commons.throwables.ArcXPSDKErrorType;
import com.arcxp.commons.util.BuildVersionProvider;
import com.arcxp.commons.util.DependencyFactory;
import com.arcxp.sdk.R;
import com.arcxp.video.cast.ArcCastManager;
import com.arcxp.video.listeners.ArcKeyListener;
import com.arcxp.video.listeners.ArcVideoEventsListener;
import com.arcxp.video.listeners.ArcVideoSDKErrorListener;
import com.arcxp.video.listeners.VideoListener;
import com.arcxp.video.listeners.VideoPlayer;
import com.arcxp.video.model.ArcVideo;
import com.arcxp.video.model.ArcVideoSDKErrorType;
import com.arcxp.video.model.ArcVideoStream;
import com.arcxp.video.model.ArcVideoStreamVirtualChannel;
import com.arcxp.video.model.AvailList;
import com.arcxp.video.model.Stream;
import com.arcxp.video.model.TrackingType;
import com.arcxp.video.model.TrackingTypeData;
import com.arcxp.video.model.VideoAdData;
import com.arcxp.video.players.PlayerContract;
import com.arcxp.video.service.AdUtils;
import com.arcxp.video.util.TrackingHelper;
import com.arcxp.video.util.Utils;
import com.arcxp.video.views.VideoFrameLayout;
import com.iab.omid.library.washpost.Omid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class handles all management functions for video playback.  It is the first non-client
 * facing class of the video playback capabilities.
 * <p>
 * ### How does it relate to other SDK classes?
 * <p>
 * ### What are the core components that make it up?
 *
 * @hide
 */
@SuppressLint("UnsafeOptInUsageError")
public class ArcVideoManager implements VideoListener {
    /**
     * Logging tag
     */
    public static final String TAG = ArcVideoManager.class.getSimpleName();
    /**
     * Indicates an uninitialized video position
     */
    public static final long NO_POSITION = -1;

    @NonNull
    private final Context mContext;

    @NonNull
    private final BuildVersionProvider buildVersionProvider = DependencyFactory.INSTANCE.createBuildVersionProvider();

    /**
     * Values used in saving the playback position
     */
    @NonNull
    private final HashMap<String, Long> mIdToPosition;

    /**
     * Container for the video player
     */
    @NonNull
    private final VideoFrameLayout mVideoFrameLayout;

    /**
     * Utils class
     */
    @NonNull
    private final Utils utils;

    /**
     * The video player object.
     */
    @Nullable
    private PlayerContract postTvPlayer;

    /**
     * Messaging that is displayed in the video view
     */
    @NonNull
    private final TextView mMessageText;

    /**
     * Overlay to display mMessageText
     */
    @NonNull
    private final RelativeLayout mMessageOverlay;

    /**
     * This will be true if the video is playing, false otherwise
     */
    private boolean mIsPlaying = false;
    /**
     *
     */
    private boolean mIsStickyPlayer = false;
    /**
     * This will be true if the video is being displayed picture-in-picture
     */
    private boolean mIsInPIP = false;

    /**
     * Passed in flag indicating if the video is a live broadcast
     */
    private boolean mIsLive = false;

    /**
     * Flag to indicate if a PIP command was a stop request
     */
    private boolean isPIPStopRequest = false;

    /**
     * Needed to start PIP.  Currently the only thing that is set is aspect ratio but ability
     * to customize PIP would be a good future enhancement.
     */
    private PictureInPictureParams.Builder mPictureInPictureParamsBuilder;

    /**
     * Config info passed in from the media player
     */
    private ArcXPVideoConfig configInfo;

    /**
     * Class used to pass back all events.
     */
    private ArcVideoEventsListener eventTracker;

    /**
     * This data is extracted from the {@link ArcVideoStream} and used for client and server side
     * ad setup
     */
    private VideoAdData videoAdData;

    /**
     * This timer fires every ad_polling_delay_ms in order to check for ad avails if client
     * and server side ads are enabled
     */
    private Timer mTimer;
    /**
     * TimerTask for the above timer
     */
    private TimerTask timerTask;

    /**
     * Flag to indicate if playback has started
     */
    private boolean playStarted = false;

    /**
     * {@link TrackingHelper} object
     */
    private TrackingHelper trackingHelper;

    /**
     * Callback for errors
     */
    private ArcVideoSDKErrorListener errorListener;

    /**
     * Constructor
     *
     * @param appContext Context
     */
    @SuppressLint("NewApi")
    protected ArcVideoManager(final @NonNull Context appContext, final @NonNull Utils utils) {
        mContext = appContext;
        this.utils = utils;
        mIdToPosition = new HashMap<>();
        mVideoFrameLayout = utils.createVideoFrameLayout(mContext);
        mVideoFrameLayout.setId(View.generateViewId());

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mVideoFrameLayout.setLayoutParams(params);
        mVideoFrameLayout.setBackgroundColor(Color.parseColor("#000000"));

        mMessageText = utils.createTextView(mContext);
        mMessageText.setTextColor(Color.WHITE);
        mMessageText.setGravity(Gravity.CENTER);
        mMessageOverlay = utils.createRelativeLayout(mContext);
        mMessageOverlay.setBackgroundColor(Color.BLACK);
        RelativeLayout.LayoutParams params2 = utils.createRelativeLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params2.addRule(RelativeLayout.CENTER_IN_PARENT);
        mMessageOverlay.addView(mMessageText, params2);

        if (buildVersionProvider.sdkInt() >= Build.VERSION_CODES.O) {
            mPictureInPictureParamsBuilder = new PictureInPictureParams.Builder();
        }
    }

    /**
     * Assigns the {@link ArcXPVideoConfig} object to the media player.  The object will be
     * used during initialization of the media.
     *
     * @param config
     */
    void initMediaPlayer(@NonNull final ArcXPVideoConfig config) {

        configInfo = config;
        if (config.isEnableOmid()) {
            Omid.activate(mContext);
        }
    }

    /**
     * Initialize a single local video object.
     *
     * @param video {@link ArcVideo} object representing the video to be played.
     * @throws IllegalStateException
     */
    synchronized void initMedia(@NonNull final ArcVideo video) throws ArcXPException {
        if (configInfo == null) {
            throw DependencyFactory.INSTANCE.createArcXPException(ArcXPSDKErrorType.INIT_ERROR,
                    ArcXPMobileSDK.INSTANCE.application().getString(R.string.media_player_uninitialized_error), video);
        }
        if (configInfo.getAdConfig() == null) {
            video.setAdTagUrl(configInfo.getAdConfigUrl());
            video.setShouldPlayAds(configInfo.isEnableAds());
        } else {
            video.setAdTagUrl(configInfo.getAdConfig().getAdConfigUrl());
            video.setShouldPlayAds(configInfo.getAdConfig().isAdEnabled());
        }

        video.setStartMuted(configInfo.isStartMuted());
        mIsLive = video.isLive();
        initVideoForPlayback(video);
    }

    /**
     * Initialize a video downloaded from the Arc server using findByUuid().
     *
     * @param videoStream
     * @throws IllegalStateException
     */
    synchronized void initMedia(@NonNull final ArcVideoStream videoStream) throws ArcXPException {
        if (configInfo == null) {
            throw DependencyFactory.INSTANCE.createArcXPException(ArcXPSDKErrorType.INIT_ERROR,
                    ArcXPMobileSDK.INSTANCE.application().getString(R.string.media_player_uninitialized_error), videoStream);
        }
        ArcVideo video = new ArcVideo.Builder()
                .setVideoStream(videoStream, configInfo)
                .build();
        mIsLive = video.isLive();

        if (configInfo.isEnableClientSideAds() && mIsLive) {
            getVideoManifest(videoStream, video.getBestStream());
            if (videoAdData != null && videoAdData.getManifestUrl() != null) {
                video.setId(videoAdData.getManifestUrl());
            }
        }

        initVideoForPlayback(video);

        if (configInfo.isEnableServerSideAds() && mIsLive) {
            if (!(configInfo.isEnableClientSideAds())) {
                getVideoManifest(videoStream, video.getBestStream());
            }
            AdUtils.enableServerSideAds(videoStream, video.getBestStream());
        }
    }

    /**
     * Initialize a video downloaded from the Arc server using findByUuid().
     *
     * @param videoStream
     * @param shareUrl
     * @throws IllegalStateException
     */
    synchronized void initMediaWithShareUrl(@NonNull final ArcVideoStream videoStream, @NonNull final String shareUrl) throws ArcXPException {
        if (configInfo == null) {
            throw DependencyFactory.INSTANCE.createArcXPException(ArcXPSDKErrorType.INIT_ERROR,
                    ArcXPMobileSDK.INSTANCE.application().getString(R.string.media_player_uninitialized_error), videoStream);
        }
        ArcVideo video = new ArcVideo.Builder()
                .setVideoStream(videoStream, configInfo)
                .setShareUrl(shareUrl)
                .build();
        initMedia(video);
    }

    /**
     * Initialize a virtual channel video downloaded from the Arc server using findByUuid().
     *
     * @param arcVideoStreamVirtualChannel ArcVideoStreamVirtualChannel object returned from virtual channel endpoint
     * @throws IllegalStateException
     */
    synchronized void initMedia(@NonNull final ArcVideoStreamVirtualChannel arcVideoStreamVirtualChannel) throws ArcXPException {
        if (configInfo == null) {
            throw DependencyFactory.INSTANCE.createArcXPException(ArcXPSDKErrorType.INIT_ERROR,
                    ArcXPMobileSDK.INSTANCE.application().getString(R.string.media_player_uninitialized_error), null);
        }
        String adUrl = null;
        if (configInfo.isEnableAds()) {
            if (arcVideoStreamVirtualChannel.getAdSettings() != null
                    && arcVideoStreamVirtualChannel.getAdSettings().getUrl() != null
                    && !arcVideoStreamVirtualChannel.getAdSettings().getUrl().isEmpty()) {
                adUrl = arcVideoStreamVirtualChannel.getAdSettings().getUrl();
            }
        }
        boolean usingAds = configInfo.isEnableAds() && adUrl != null;
        String finalUrl = usingAds ? adUrl : arcVideoStreamVirtualChannel.getUrl();

        ArcVideo video = new ArcVideo.Builder()
                .setVideoStreamVirtual(finalUrl, configInfo)
                .build();
        mIsLive = video.isLive();
        if (usingAds) {
            getVideoManifest(finalUrl);//enables the avail line log.e logging
            if (videoAdData != null && videoAdData.getManifestUrl() != null) {
                video.setId(videoAdData.getManifestUrl());
            }
        }//this breaks non ad playback
        initVideoForPlayback(video);
    }

    private void initVideoForPlayback(ArcVideo video) {
        video.setAutoStartPlay(configInfo.isAutoStartPlay());
        trackingHelper = utils.createTrackingHelper(video.getId(), this, configInfo, mContext, mVideoFrameLayout, this);
        mIsPlaying = false;
        //never initialize more than one video player
        if (postTvPlayer != null) {
            if (mIsInPIP) {
                postTvPlayer.release();
            } else {
                release();
            }
        }

        long savedPos = getSavedPosition(video.getId());
        long targetPos = video.getStartPos();
        setSavedPosition(video.getId(), (savedPos == NO_POSITION) ? targetPos : savedPos);
        postTvPlayer = utils.createPostTvPlayerImpl(configInfo, this, trackingHelper);
        postTvPlayer.playVideo(video);
        mIsPlaying = true;
    }

    /**
     * Initialize a video downloaded from the Arc server using findByUuid() and include an ad
     * URL to be played with it.
     *
     * @param videoStream
     * @param adUrl
     * @throws IllegalStateException
     */
    synchronized void initMedia(@NonNull final ArcVideoStream videoStream, String adUrl) throws ArcXPException {
        if (configInfo == null) {
            throw DependencyFactory.INSTANCE.createArcXPException(ArcXPSDKErrorType.INIT_ERROR,
                    ArcXPMobileSDK.INSTANCE.application().getString(R.string.media_player_uninitialized_error), videoStream);
        }
        videoStream.setAdTagUrl(adUrl);

        ArcVideo video = new ArcVideo.Builder()
                .setVideoStream(videoStream, configInfo)
                .build();
        mIsLive = video.isLive();

        if (configInfo.isEnableClientSideAds() && mIsLive) {
            getVideoManifest(videoStream, video.getBestStream());
            if (videoAdData != null && videoAdData.getManifestUrl() != null) {
                video.setId(videoAdData.getManifestUrl());
            }
        }

        initVideoForPlayback(video);

        if (configInfo.isEnableServerSideAds()) {
            AdUtils.enableServerSideAds(videoStream, video.getBestStream());
        }

    }

    /**
     * Initialize a list of videos downloaded from the Arc server using findByUuids().
     *
     * @param videoStreams
     * @throws IllegalStateException
     */
    synchronized void initMedia(@NonNull final List<ArcVideoStream> videoStreams) throws ArcXPException {
        if (configInfo == null) {
            throw DependencyFactory.INSTANCE.createArcXPException(ArcXPSDKErrorType.INIT_ERROR,
                    ArcXPMobileSDK.INSTANCE.application().getString(R.string.media_player_uninitialized_error), videoStreams);
        }
        List<ArcVideo> videos = new ArrayList<ArcVideo>();
        for (ArcVideoStream stream : videoStreams) {
            ArcVideo video = new ArcVideo.Builder()
                    .setVideoStream(stream, configInfo)
                    .build();
            videos.add(video);

            if (configInfo.isEnableClientSideAds() && video.isLive()) {
                getVideoManifest(stream, video.getBestStream());
                if (videoAdData != null && videoAdData.getManifestUrl() != null) {
                    video.setId(videoAdData.getManifestUrl());
                }
            }

            if (configInfo.isEnableServerSideAds()) {
                AdUtils.enableServerSideAds(stream, video.getBestStream());
            }

            video.setAutoStartPlay(configInfo.isAutoStartPlay());
        }

        trackingHelper = utils.createTrackingHelper(videos.get(0).getId(), this, configInfo, mContext, mVideoFrameLayout, this);

        mIsLive = videos.get(0).isLive();

        mIsPlaying = false;
        //never initialize more than one video player
        if (postTvPlayer != null && !mIsInPIP) {
            release();
        } else if (postTvPlayer != null && mIsInPIP) {
            postTvPlayer.release();
        }
        postTvPlayer = utils.createPostTvPlayerImpl(configInfo, this, trackingHelper);
        postTvPlayer.playVideos(videos);
        mIsPlaying = true;
    }

    /**
     * Initialize a list of videos downloaded from the Arc server using findByUuids() and include a list of ad URLs to
     * be played with the videos.  There must be a 1-1 correspondence between the videos and the ad URLs.  The position of the
     * URL determines which video it corresponds to.  videoStreams[0] will play adUrls[0], videoStreams[1] will play adUrls[1].
     *
     * @param videoStreams
     * @param adUrls
     * @throws IllegalStateException
     */
    synchronized void initMedia(@NonNull final List<ArcVideoStream> videoStreams, final List<String> adUrls) throws ArcXPException {
        if (configInfo == null) {
            throw DependencyFactory.INSTANCE.createArcXPException(ArcXPSDKErrorType.INIT_ERROR,
                    ArcXPMobileSDK.INSTANCE.application().getString(R.string.media_player_uninitialized_error), videoStreams);
        }
        if (videoStreams.size() > adUrls.size()) {
            throw DependencyFactory.INSTANCE.createArcXPException(ArcXPSDKErrorType.INIT_ERROR,
                    ArcXPMobileSDK.INSTANCE.application().getString(R.string.ad_count_error),
                    null);
        } else {
            int count = 0;
            for (count = 0; count < videoStreams.size(); count++) {
                videoStreams.get(count).setAdTagUrl(adUrls.get(count));
            }
        }
        List<ArcVideo> videos = new ArrayList<ArcVideo>();
        for (ArcVideoStream stream : videoStreams) {
            ArcVideo video = new ArcVideo.Builder()
                    .setVideoStream(stream, configInfo)
                    .build();
            videos.add(video);

            if (configInfo.isEnableClientSideAds() && mIsLive) {
                getVideoManifest(stream, video.getBestStream());
                if (videoAdData != null && videoAdData.getManifestUrl() != null) {
                    video.setId(videoAdData.getManifestUrl());
                }
            }

            if (configInfo.isEnableServerSideAds()) {
                AdUtils.enableServerSideAds(stream, video.getBestStream());
            }

            video.setAutoStartPlay(configInfo.isAutoStartPlay());
        }

        trackingHelper = utils.createTrackingHelper(videos.get(0).getId(), this, configInfo, mContext, mVideoFrameLayout, this);

        mIsLive = videos.get(0).isLive();

        mIsPlaying = false;
        //never initialize more than one video player
        if (postTvPlayer != null && !mIsInPIP) {
            release();
        } else if (postTvPlayer != null && mIsInPIP) {
            postTvPlayer.release();
        }
        postTvPlayer = utils.createPostTvPlayerImpl(configInfo, this, trackingHelper);
        postTvPlayer.playVideos(videos);
        mIsPlaying = true;
    }

    /**
     * Adds a video to the list of videos to be played.  This video will be played at the end of the
     * current video list.
     *
     * @param videoStream
     */
    synchronized void addVideo(ArcVideoStream videoStream) throws ArcXPException {
        if (configInfo == null) {
            throw DependencyFactory.INSTANCE.createArcXPException(ArcXPSDKErrorType.INIT_ERROR,
                    ArcXPMobileSDK.INSTANCE.application().getString(R.string.media_player_uninitialized_error), videoStream);
        }
        if (postTvPlayer == null) {
            throw DependencyFactory.INSTANCE.createArcXPException(ArcXPSDKErrorType.INIT_ERROR,
                    ArcXPMobileSDK.INSTANCE.application().getString(R.string.video_player_uninitialized_error), videoStream);
        }
        ArcVideo video = new ArcVideo.Builder()
                .setVideoStream(videoStream, configInfo)
                .build();
        postTvPlayer.addVideo(video);
    }

    /**
     * Adds a video to the list of videos to be played along with a URL for the ad to be played with the video.
     * This video will be played at the end of the current video list.
     *
     * @param videoStream
     * @param adUrl
     */

    synchronized void addVideo(ArcVideoStream videoStream, String adUrl) throws ArcXPException {
        if (configInfo == null) {
            throw DependencyFactory.INSTANCE.createArcXPException(ArcXPSDKErrorType.INIT_ERROR,
                    ArcXPMobileSDK.INSTANCE.application().getString(R.string.media_player_uninitialized_error), videoStream);
        }
        if (postTvPlayer == null) {
            throw DependencyFactory.INSTANCE.createArcXPException(ArcXPSDKErrorType.INIT_ERROR,
                    ArcXPMobileSDK.INSTANCE.application().getString(R.string.video_player_uninitialized_error), videoStream);
        }
        if (adUrl != null && !adUrl.isEmpty()) {
            videoStream.setAdTagUrl(adUrl);
        }
        ArcVideo video = new ArcVideo.Builder()
                .setVideoStream(videoStream, configInfo)
                .build();
        postTvPlayer.addVideo(video);
    }

    /**
     * Returns if the the current video is playing.
     *
     * @return True if playing, false if paused or stopped.
     */
    public boolean isPlaying() {
        if (postTvPlayer != null) {
            return postTvPlayer.isPlaying();
        }
        return false;
    }

    private boolean isPIPSupported() {
        return (buildVersionProvider.sdkInt() >= Build.VERSION_CODES.O);
    }

    @Override
    public boolean isPipEnabled() {
        return configInfo.isEnablePip() && isPIPSupported();
    }

    private void getVideoManifest(ArcVideoStream videoStream, Stream stream) {
        videoAdData = AdUtils.getVideoManifest(videoStream, stream, configInfo);
        if (videoAdData != null && videoAdData.getError() != null && errorListener != null) {
            errorListener.onError(ArcVideoSDKErrorType.VIDEO_STREAM_DATA_ERROR,
                    videoAdData.getError().getMessage() != null ? videoAdData.getError().getMessage() : "",
                    videoStream);
        }
    }

    private void getVideoManifest(String url) {
        videoAdData = AdUtils.getVideoManifest(url, configInfo);
    }

    void setErrorListener(ArcVideoSDKErrorListener listener) {
        errorListener = listener;
    }

    private void startTimer() {
        if (videoAdData != null && videoAdData.getTrackingUrl() != null) {
            createTimer(2000, mContext.getResources().getInteger(R.integer.ad_polling_delay_ms));
        }
    }

    @VisibleForTesting
    void createTimer(long delay, long period) {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = utils.createTimer();
        timerTask = createTimerTask();
        mTimer.schedule(timerTask, delay > 0 ? delay : 18000, period > 0 ? period : 18000);
    }

    @VisibleForTesting
    TimerTask createTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(() -> timerWork());
            }
        };
    }

    @VisibleForTesting
    void timerWork() {
        if (videoAdData != null && videoAdData.getTrackingUrl() != null) {
            AvailList avails = AdUtils.getAvails(videoAdData.getTrackingUrl());

            if (configInfo.isLoggingEnabled())
                Log.d("ArcVideoSDK", "Avails Received: " + avails);

            if (avails != null && avails.getAvails() != null && avails.getAvails().size() > 0) {
                trackingHelper.addEvents(avails, getCurrentTimelinePosition());
            }
        }
        createTimer(mContext.getResources().getInteger(R.integer.ad_polling_delay_ms),
                mContext.getResources().getInteger(R.integer.ad_polling_delay_ms));
    }

    @SuppressLint("NewApi")
    private void minimize() {
        if (configInfo.getVideoFrame() == null) {
            return;
        }
        if (isPIPSupported()) {
            toggleOptionalViews(false);

            Display display = configInfo.getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int screenHeight = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    mContext.getResources().getConfiguration().screenHeightDp,
                    mContext.getResources().getDisplayMetrics()));
            int screenWidth = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    mContext.getResources().getConfiguration().screenWidthDp,
                    mContext.getResources().getDisplayMetrics()));
            int height, width;
            width = screenHeight;
            height = screenHeight / 16 * 9;
            Rational aspectRatio = new Rational(width, height);

            mPictureInPictureParamsBuilder.setAspectRatio(aspectRatio).build();
            configInfo.getActivity().enterPictureInPictureMode(mPictureInPictureParamsBuilder.build());
        }
    }

    void displayVideo() {
        if (getPlayerFrame().getParent() != configInfo.getVideoFrame()) {
            setIsStickyPlayer(false);
            removePlayerFrame();
            configInfo.getVideoFrame().addView(getPlayerFrame());

//            configInfo.getVideoFrame().setOnKeyListener((v, keyCode, event) -> {
//                Log.e("TAG", "TAG");
//                return false;
//            });
        }
    }

    @Override
    public void removePlayerFrame() {
        if (!mIsStickyPlayer) {
            if (mMessageOverlay.getParent() instanceof ViewGroup) {
                //remove the error message if showing
                ((ViewGroup) mMessageOverlay.getParent()).removeView(mMessageOverlay);
            }
            if (mVideoFrameLayout.getParent() instanceof ViewGroup) {
                //remove inline player from view holder
                ((ViewGroup) mVideoFrameLayout.getParent()).removeView(mVideoFrameLayout);
                mVideoFrameLayout.setTag(null);
            }
        } else {
            //remove sticky player from activity
            mIsStickyPlayer = false;
            if (postTvPlayer != null) {
                mVideoFrameLayout.setOnClickListener(null);
                postTvPlayer.onStickyPlayerStateChanged(false);
            }
        }
    }

    void pausePlay(boolean shouldPlay) {
        if (postTvPlayer != null)
            postTvPlayer.pausePlay(shouldPlay);
    }

    void toggleCaptions() {
        if (postTvPlayer != null)
            postTvPlayer.toggleCaptions();
    }

    void onScrolled(@NonNull View.OnClickListener listener) {
        if (!mIsStickyPlayer && mIsPlaying) {
            if (postTvPlayer != null) {
                mIsStickyPlayer = true;
            }
        }
    }

    @Override
    public boolean isStickyPlayer() {
        return mIsStickyPlayer;
    }

    /**
     * Method to start PIP.  It is called when the PIP button is pressed on the video player.
     *
     * @param mVideo
     */
    @Override
    public void startPIP(ArcVideo mVideo) {
        if ((isPIPSupported()) && mVideo != null && configInfo.isEnablePip()) {
            setIsInPIP(true);
            minimize();
        }
    }

    /**
     * Method to stop PIP.  It is called when the PIP view is exited back to normal view mode
     */
    public void stopPIP() {
        if ((isPIPSupported()) && configInfo.isEnablePip()) {
            postTvPlayer.onPipExit();//TODO improve this call
            setIsInPIP(false);
            toggleOptionalViews(true);
        }
    }

    /**
     * Called from the parent activities onBackPressed() method.  This will put the video in PIP mode.
     * Failure to call this will close the app rather than turn on PIP.
     *
     * @return
     */
    @SuppressLint("NewApi")
    boolean onBackPressed() {
        //PIP is not supported below API 24 so this branch will not be called below that min SDK version
        if (isPIPSupported() && configInfo != null && configInfo.getActivity() != null &&
                !configInfo.getActivity().isInPictureInPictureMode() && configInfo.isEnablePip()) {
            if (postTvPlayer.isCasting()) {
                return false;
            } else {
                postTvPlayer.onPipEnter();
            }

            setIsInPIP(true);
            minimize();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the playWhenReady state of the player.
     *
     * @return
     */
    boolean getPlayWhenReadyState() {
        if (postTvPlayer != null) {
            return postTvPlayer.getPlayWhenReadyState();
        }
        return false;
    }

    @Override
    public void onTrackingEvent(@NonNull TrackingType type, @Nullable TrackingTypeData trackingData) {
        if (trackingData instanceof TrackingTypeData.TrackingVideoTypeData && postTvPlayer != null && postTvPlayer.getVideo() != null) {
            if (configInfo.isLoggingEnabled()) {
                Log.d("ArcVideoSDK", "onTrackingEvent " + type + " at " + ((TrackingTypeData.TrackingVideoTypeData) trackingData).component1());
            }
            type = preProcessVideoTracking(type, (TrackingTypeData.TrackingVideoTypeData) trackingData);
        }
        if (eventTracker != null) {
            String ret = videoAdData != null ? videoAdData.getSessionId() : "";
            eventTracking(type, trackingData, postTvPlayer != null ? postTvPlayer.getVideo() : null, ret, eventTracker);
        }
    }

    private TrackingType preProcessVideoTracking(TrackingType type, TrackingTypeData.TrackingVideoTypeData trackingData) {
        switch (type) {
            case ON_PLAY_STARTED: {
                if (!playStarted) {
                    playStarted = true;
                    startTimer();
                }
                mIsPlaying = true;
                break;
            }
            case ON_PLAY_COMPLETED: {
                mIsPlaying = false;
                playStarted = false;
                if (trackingData.getPosition() != null) {
                    setSavedPosition(getId(), trackingData.getPosition());
                }

                if (mTimer != null) {
                    mTimer.cancel();
                    mTimer.purge();
                }

                break;
            }
            case VIDEO_PERCENTAGE_WATCHED: {
                if (trackingData.getPercentage() != null && !mIsLive) {
                    int percentage = trackingData.getPercentage();
                    if (percentage == 25) {
                        type = TrackingType.VIDEO_25_WATCHED;
                    } else if (percentage == 50) {
                        type = TrackingType.VIDEO_50_WATCHED;
                    } else if (percentage == 75) {
                        type = TrackingType.VIDEO_75_WATCHED;
                    }
                }
                break;
            }
            case AD_PLAY_STARTED:
                if (trackingData.getPercentage() != null) {
                    int percentage = trackingData.getPercentage();
                    if (percentage == 0) {
                        type = TrackingType.PREROLL_AD_STARTED;
                    } else if (percentage > 0 && percentage < 90) {
                        type = TrackingType.MIDROLL_AD_STARTED;
                    } else if (percentage >= 90) {
                        type = TrackingType.POSTROLL_AD_STARTED;
                    }
                }
                break;
            case AD_PLAY_COMPLETED:
                if (trackingData.getPercentage() != null) {
                    int percentage = trackingData.getPercentage();
                    if (percentage == 0) {
                        type = TrackingType.PREROLL_AD_COMPLETED;
                    } else if (percentage > 0 && percentage != 100) {
                        type = TrackingType.MIDROLL_AD_COMPLETED;
                    } else if (percentage == 100) {
                        type = TrackingType.POSTROLL_AD_COMPLETED;
                    }
                }
                break;
        }
        return type;
    }

    @Override
    public void setSavedPosition(String id, long value) {
        mIdToPosition.put(id, value);
    }

    @Override
    public void setNoPosition(String id) {
        mIdToPosition.put(id, NO_POSITION);
    }

    @Override
    public long getSavedPosition(String id) {
        Long position = mIdToPosition.get(id);
        if (position != null) {
            return position;
        }
        return NO_POSITION;
    }

    @NonNull
    public RelativeLayout getPlayerFrame() {
        return mVideoFrameLayout;
    }

    @Override
    public void addVideoView(View view) {
        if (view.getParent() != null && view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        mVideoFrameLayout.addView(view);
    }

    @Override
    public void addVideoFragment(Fragment fragment) {
//        final Activity activity = ((PostTvApplication) mAppContext).getCurrentActivity();
//        if (activity instanceof PostTvActivity) {
//            mAppContext.addFragment(mVideoFrameLayout.getId(), fragment);
//        }
    }

    @Override
    public void removeVideoFragment(Fragment fragment) {
//        final Activity activity = ((PostTvApplication) mAppContext).getCurrentActivity();
//        if (activity instanceof PostTvActivity) {
//            ((PostTvActivity) activity).removeFragment(fragment);
//        }
    }

    @Override
    public void setIsLoading(boolean isLoading) {
//        if (isLoading) {
//            if (mProgressLayout.getParent() == null) {
//                mVideoFrameLayout.addView(mProgressLayout);
//            }
//        } else {
//            mVideoFrameLayout.removeView(mProgressLayout);
//        }
    }

    @Override
    public void release() {
        if (postTvPlayer != null && !mIsInPIP) {
            postTvPlayer.release();
        }
        mIsPlaying = false;
        mIsStickyPlayer = false;
        playStarted = false;

        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
    }

    @Override
    public void onError(ArcVideoSDKErrorType type, String message, Object value) {
        if (!configInfo.getDisableErrorOverlay()) {
            ViewParent parent = mVideoFrameLayout.getParent();
            if (!mIsStickyPlayer && !mIsInPIP && parent instanceof ViewGroup) {
                release();
                mMessageText.setText(message);
                if (mMessageOverlay.getParent() != parent) {
                    if (mMessageOverlay.getParent() instanceof ViewGroup) {
                        ((ViewGroup) mMessageOverlay.getParent()).removeView(mMessageOverlay);
                    }
                    ((ViewGroup) parent).addView(mMessageOverlay);
                }
            }
        }
        if (errorListener != null) {
            String error = mContext.getString(R.string.source_error);
            if (value instanceof Exception) {
                error = ((Exception) value).getMessage();
            }
            errorListener.onError(type, error, value);
        }
    }

    @Override
    public void logError(String log) {

    }

    @Nullable
    public String getId() {
        if (postTvPlayer != null) {
            return postTvPlayer.getId();
        }
        return null;
    }

    @Override
    public String getSessionId() {
        if (videoAdData != null) {
            return videoAdData.getSessionId();
        } else {
            return null;
        }
    }

    @Override
    public void onActivityResume() {
        if (postTvPlayer != null) {
            postTvPlayer.onActivityResume();
        }
    }

    String getVideoURl() {
        return postTvPlayer.getVideo().getId();
    }

    ArcVideo getVideo() {
        return postTvPlayer.getVideo();
    }

    @Override
    public boolean isInPIP() {
        return mIsInPIP;
    }

    void setIsInPIP(boolean isPIP) {
        mIsInPIP = isPIP;
    }

    @Nullable
    public Activity getCurrentActivity() {
        return configInfo.getActivity();
    }

    @Override
    public void onShareVideo(String headline, String url) {

    }

    @Override
    public void onMute(boolean isMute) {

    }

    @Override
    public long getAdType() {
        return postTvPlayer.getAdType();
    }

    @Override
    public void pausePIP() {

    }

    public boolean isLive() {
        return mIsLive;
    }

    public void setIsStickyPlayer(boolean isStickyPlayer) {
        mIsStickyPlayer = isStickyPlayer;
    }

    public void initEvents(ArcVideoEventsListener trackEvents) {
        eventTracker = trackEvents;
    }

    public void showControls() {
        if (postTvPlayer != null) {
            postTvPlayer.showControls(true);
        }
    }

    public void hideControls() {
        if (postTvPlayer != null) {
            postTvPlayer.showControls(false);
        }
    }

    public boolean isControlsVisible() {
        if (postTvPlayer != null) {
            return postTvPlayer.isControllerFullyVisible();
        }
        return false;
    }

    public long getCurrentVideoDuration() {
        if (postTvPlayer != null) {
            return postTvPlayer.getCurrentVideoDuration();
        }
        return 0;
    }

    public boolean isClosedCaptionVisible() {//TODO deprecate/remove one of these identical functions
        if (postTvPlayer != null) {
            return postTvPlayer.isClosedCaptionVisibleAndOn();
        }
        return false;
    }

    public boolean isClosedCaptionTurnedOn() {//TODO deprecate/remove one of these identical functions
        if (postTvPlayer != null) {
            return postTvPlayer.isClosedCaptionVisibleAndOn();
        }
        return false;
    }

    public boolean isClosedCaptionAvailable() {
        if (postTvPlayer != null) {
            return postTvPlayer.isClosedCaptionAvailable();
        }
        return false;
    }

    public boolean enableClosedCaption(boolean enable) {
        if (postTvPlayer != null) {
            return postTvPlayer.enableClosedCaption(enable);
        }
        return false;
    }

    public boolean setCcButtonDrawable(@DrawableRes int ccButtonDrawable) {
        if (postTvPlayer != null) {
            return postTvPlayer.setCcButtonDrawable(ccButtonDrawable);
        }
        return false;
    }

    public boolean isShowClosedCaptionDialog() {
        return configInfo.isShowClosedCaptionTrackSelection();
    }

    public boolean isFullScreen() {
        if (postTvPlayer != null) {
            return postTvPlayer.isFullScreen();
        }
        return false;
    }

    int oldWidth;
    int oldHeight;

    ViewGroup.LayoutParams oldParams;

    @Override
    public void setFullscreen(boolean full) {

        if (configInfo.isUseFullScreenDialog()) {
            if (postTvPlayer != null) {
                postTvPlayer.setFullscreen(full);
            }
        } else {

            if (!full) {
                getCurrentActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                getCurrentActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) configInfo.getVideoFrame().getLayoutParams();
                params.width = oldWidth;
                params.height = oldHeight;
                //oldParams = new ViewGroup.LayoutParams(params);
                configInfo.getVideoFrame().setLayoutParams(params);
            } else {
                getCurrentActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                getCurrentActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) configInfo.getVideoFrame().getLayoutParams();
                oldWidth = params.width;
                oldHeight = params.height;
                //oldParams = new ViewGroup.LayoutParams(params);
                params.width = params.MATCH_PARENT;
                params.height = params.MATCH_PARENT;
                configInfo.getVideoFrame().setLayoutParams(params);
            }

            postTvPlayer.setFullscreenUi(full);
        }

    }

    public VideoPlayer getVideoPlayer() {
        return postTvPlayer != null ? postTvPlayer.getVideoPlayer() : null;
    }

    public void setFullscreenListener(ArcKeyListener listener) {
        if (postTvPlayer != null) {
            postTvPlayer.setFullscreenListener(listener);
        }
    }

    public void setPlayerKeyListener(ArcKeyListener listener) {
        if (postTvPlayer != null) {
            postTvPlayer.setPlayerKeyListener(listener);
        }
    }

    public void startPlay() {
        if (postTvPlayer != null) {
            postTvPlayer.start();
        }
    }

    public void stopPlay() {
        if (postTvPlayer != null) {
            postTvPlayer.stop();
        }
    }

    public void pausePlay() {
        if (postTvPlayer != null) {
            postTvPlayer.pause();
        }
    }

    public void resumePlay() {
        if (postTvPlayer != null) {
            postTvPlayer.resume();
        }
    }

    public void seekTo(int ms) {
        if (postTvPlayer != null) {
            postTvPlayer.seekTo(ms);
        }
    }

    public void setVolume(float volume) {
        if (postTvPlayer != null) {
            postTvPlayer.setVolume(volume);
        }
    }

    public int getPlaybackState() {
        if (postTvPlayer != null) {
            return postTvPlayer.getPlaybackState();
        }
        return 0;
    }

    public long getPlayheadPosition() {
        if (postTvPlayer != null) {
            return postTvPlayer.getCurrentPosition();
        }
        return -1;
    }

    public long getCurrentTimelinePosition() {
        if (postTvPlayer != null) {
            return postTvPlayer.getCurrentTimelinePosition();
        }
        return -1;
    }

    public View getOverlay(String tag) {
        if (postTvPlayer != null) {
            return postTvPlayer.getOverlay(tag);
        }
        return null;
    }

    public ArcCastManager getCastManager() {
        return configInfo.getArcCastManager();
    }

    public void onPause() {
        if (configInfo != null && configInfo.getArcCastManager() != null) {
            configInfo.getArcCastManager().onPause();
        }
    }

    public void onStop() {
        isPIPStopRequest = true;
    }

    public void onDestroy() {
        if (configInfo != null && configInfo.getArcCastManager() != null) {
            configInfo.getArcCastManager().onDestroy();
        }
        if (trackingHelper != null) {
            trackingHelper.onDestroy();
        }
    }

    public void onResume() {
        if (configInfo != null && configInfo.getArcCastManager() != null) {
            configInfo.getArcCastManager().onResume();
        }
    }

    public boolean getIsPipStopRequest() {
        return isPIPStopRequest;
    }


    public void toggleOptionalViews(boolean shouldShow) {
        if (configInfo.getViewsToHide() != null) {
            for (View v : configInfo.getViewsToHide()) {
                v.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
            }
        }
        if (configInfo.getActivity() != null) {
            if (configInfo.getActivity().getActionBar() != null) {
                if (shouldShow) {
                    configInfo.getActivity().getActionBar().show();
                } else {
                    configInfo.getActivity().getActionBar().hide();
                }
            }

            if (configInfo.getActivity() instanceof AppCompatActivity) {
                ActionBar actionBar = ((AppCompatActivity) configInfo.getActivity()).getSupportActionBar();
                if (actionBar != null) {
                    if (shouldShow) {
                        actionBar.show();
                    } else {
                        actionBar.hide();
                    }
                }
            }
        }

    }

    public boolean onKeyEvent(KeyEvent event) {
        return postTvPlayer.onKeyEvent(event);
    }

    @VisibleForTesting
    ArcXPVideoConfig getConfigInfo() {
        return configInfo;
    }

    @VisibleForTesting
    boolean mIsPlaying() {
        return mIsPlaying;
    }

    @VisibleForTesting
    boolean isPlayStarted() {
        return playStarted;
    }
}
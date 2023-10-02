package com.arcxp.video.players;

import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.MotionEvent.ACTION_UP;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.arcxp.video.model.TrackingType.BEHIND_LIVE_WINDOW_ADJUSTMENT;
import static com.arcxp.video.model.TrackingType.NEXT_BUTTON_PRESSED;
import static com.arcxp.video.model.TrackingType.ON_OPEN_FULL_SCREEN;
import static com.arcxp.video.model.TrackingType.ON_PLAY_RESUMED;
import static com.arcxp.video.model.TrackingType.PREV_BUTTON_PRESSED;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.CaptioningManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;

import com.arcxp.sdk.R;
import com.arcxp.video.ArcXPVideoConfig;
import com.arcxp.video.VideoTracker;
import com.arcxp.video.cast.ArcCastManager;
import com.arcxp.video.listeners.ArcKeyListener;
import com.arcxp.video.listeners.VideoListener;
import com.arcxp.video.listeners.VideoPlayer;
import com.arcxp.video.model.ArcAd;
import com.arcxp.video.model.ArcVideo;
import com.arcxp.video.model.ArcVideoSDKErrorType;
import com.arcxp.video.model.TrackingType;
import com.arcxp.video.model.TrackingTypeData;
import com.arcxp.video.util.PrefManager;
import com.arcxp.video.util.TrackingHelper;
import com.arcxp.video.util.Utils;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.Tracks;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.drm.DrmSessionManagerProvider;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.LoadErrorHandlingPolicy;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.cast.Cast;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import rx.Subscription;

/**
 * @hide
 */
public class PostTvPlayerImpl implements Player.Listener, VideoPlayer,
        MediaSource.Factory, SessionAvailabilityListener,
        AdEvent.AdEventListener {
    private static final String TAG = PostTvPlayerImpl.class.getSimpleName();
    static final String ID_SUBTITLE_URL = "ID_SUBTITLE_URL";

    @NonNull
    private final VideoListener mListener;
//    @Nullable
//    private ExoPlayer mLocalPlayer;
//    @Nullable
//    private StyledPlayerView playerState.getMLocalPlayerView();
//    private CastPlayer mCastPlayer;
//    @Nullable
//    private PlayerControlView mCastControlView;
    @Nullable
    private String mVideoId;
    private boolean mIsFullScreen = false;
    @Nullable
    private Dialog mFullScreenDialog;
    private final HashMap<String, View> mFullscreenOverlays;
    @Nullable
    private ArcVideo mVideo;
    @Nullable
    private List<ArcVideo> mVideos = new ArrayList<>();

    @Nullable
    private TextView title;

    @Nullable
    private String mShareUrl;
    @Nullable
    private String mHeadline;
    private float mCurrentVolume = 0f;
    @Nullable
    private VideoTracker mVideoTracker;
    @Nullable
    private ImaAdsLoader mAdsLoader;
    private boolean mIsLive;
    private ImageButton ccButton;
    @Nullable
    private Subscription videoTrackingSub;
    private final DefaultTrackFilter defaultTrackFilter = new DefaultTrackFilter();

    private final TrackingHelper trackingHelper;

    private final Timeline.Period period = new Timeline.Period();

    /* temporarily disabled controls during an ad */
    private boolean disabledControlsForAd = false;
    private boolean castSubtitlesOn = false;
    private boolean castMuteOn = false;
    private boolean castFullScreenOn = false;
    private int currentVideoIndex = 0;

    private ArcKeyListener mArcKeyListener;
    private final ArcCastManager arcCastManager;
    private Player currentPlayer;
    private View currentPlayView;
    private boolean wasInFullScreenBeforePip = false;

    private boolean firstAdCompleted = false;

    private final ArcXPVideoConfig mConfig;
    private final Utils utils;

    boolean adPlaying = false;
    boolean adPaused = false;

    private final PlayerState playerState;

    public PostTvPlayerImpl(@NonNull ArcXPVideoConfig config, @NonNull VideoListener listener,
                            @NonNull TrackingHelper helper, Utils utils,
                            @NonNull ArcCastManager arcCastManager) {
        this.mConfig = config;
        this.trackingHelper = helper;
        this.mFullscreenOverlays = config.getOverlays();
        this.arcCastManager = arcCastManager;
        this.utils = utils;
        this.mListener = listener;
        this.playerState = utils.createPlayerState(Objects.requireNonNull(config.getActivity()), listener, utils, config);
    }

    @Override
    public void playVideo(@NonNull final ArcVideo video) {
        try {
            if (video.id == null) { //TODO so this section.. sets mVideoId, then it is overwritten with video.id in playVideo() (even if it is null), probably can scrap or update to do something
                if (video.fallbackUrl != null) {
                    mVideoId = video.fallbackUrl;
                } else {
                    mVideoId = "";
                }
            } else {
                mVideoId = video.id;
            }

            mVideos.add(video);
            mVideo = video;
            playVideo();
        } catch (Exception e) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.getMessage(), mVideo);
        }
    }

    private void playVideo() {
        mIsLive = mVideo.isLive;
        mHeadline = mVideo.headline;
        mShareUrl = mVideo.shareUrl;
        mVideoId = mVideo.id;

        trackingHelper.initVideo(mVideo.id);

        initLocalPlayer();
        initCastPlayer();
        CastPlayer castPlayer = playerState.getMCastPlayer();
        ExoPlayer exoPlayer = playerState.getMLocalPlayer();
        setCurrentPlayer(castPlayer != null && castPlayer.isCastSessionAvailable() ? castPlayer : exoPlayer);
        playerState.getMLocalPlayerView().setOnTouchListener((v, event) -> {
            if (event.getAction() == ACTION_UP) {
                trackingHelper.onTouch(event, getCurrentTimelinePosition());
            }
            if (!mConfig.isDisableControlsWithTouch()) {
                v.performClick();
                return false;
            }
            return true;
        });
    }

    private void initLocalPlayer() {
        ExoPlayer exoPlayer = utils.createExoPlayer();
        playerState.setMLocalPlayer(exoPlayer);

        playerState.getMLocalPlayer().addListener(this);
        StyledPlayerView playerView = utils.createPlayerView();
        playerState.setMLocalPlayerView(playerView);
        playerView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        playerView.setResizeMode(mConfig.getVideoResizeMode().mode());
        playerView.setId(R.id.wapo_player_view);
        playerView.setPlayer(exoPlayer);
        playerView.setControllerAutoShow(mConfig.isAutoShowControls());
        title = playerView.findViewById(R.id.styled_controller_title_tv);

        if (mVideo != null && mVideo.startMuted) {
            mCurrentVolume = exoPlayer.getVolume();
            exoPlayer.setVolume(0f);
        }

        setUpPlayerControlListeners();
        setAudioAttributes();

        playerView.setOnTouchListener((v, event) -> {
            v.performClick();
            if (event.getAction() == ACTION_UP) {
                trackingHelper.onTouch(event, getCurrentTimelinePosition());
            }
            return false;
        });

        if (!mIsFullScreen) {
            mListener.addVideoView(playerView);
        } else {
            addPlayerToFullScreen();
        }

        for (View v : mFullscreenOverlays.values()) {
            playerView.addView(v);
        }


        if (mConfig.isDisableControlsFully()) {
            playerView.setUseController(false);
        } else {
            ccButton = playerView.findViewById(R.id.exo_cc);

            if (ccButton != null) {
                setVideoCaptionsStartupDrawable();
            }
        }
    }

    private void initCastPlayer() {
        if (arcCastManager != null) {
            CastPlayer mCastPlayer = utils.createCastPlayer(arcCastManager.getCastContext());
            playerState.setMCastPlayer(mCastPlayer);
            mCastPlayer.addListener(this);
            mCastPlayer.setSessionAvailabilityListener(this);

            PlayerControlView mCastControlView = utils.createPlayerControlView();
            playerState.setMCastControlView(mCastControlView);
            mCastControlView.setId(R.id.wapo_cast_control_view);
            mCastControlView.setPlayer(mCastPlayer);
            mCastControlView.setShowTimeoutMs(-1);

            ImageButton fullScreen = mCastControlView.findViewById(R.id.exo_fullscreen);
            ImageButton pipButton = mCastControlView.findViewById(R.id.exo_pip);
            ImageButton shareButton = mCastControlView.findViewById(R.id.exo_share);
            ImageButton volumeButton = mCastControlView.findViewById(R.id.exo_volume);
            ImageButton ccButton = mCastControlView.findViewById(R.id.exo_cc);
            ImageView artwork = mCastControlView.findViewById(R.id.exo_artwork);

            if (artwork != null) {
                artwork.setVisibility(VISIBLE);
                if (mVideo != null && mConfig.getArtworkUrl() != null) {
                    utils.loadImageIntoView(mConfig.getArtworkUrl(), artwork);
                }
            }
            if (fullScreen != null) {
                fullScreen.setVisibility(VISIBLE);
                fullScreen.setOnClickListener(v -> toggleFullScreenCast());
            }
            if (pipButton != null) {
                pipButton.setVisibility(GONE);
            }
            if (volumeButton != null) {
                if (mVideo != null) {
                    volumeButton.setVisibility(VISIBLE);
                    volumeButton.setOnClickListener(v -> {
                        castMuteOn = !castMuteOn;
                        arcCastManager.setMute(castMuteOn);
                        volumeButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()),
                                castMuteOn ? R.drawable.MuteDrawableButton : R.drawable.MuteOffDrawableButton));
                    });
                    volumeButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), R.drawable.MuteOffDrawableButton));
                } else {
                    volumeButton.setVisibility(GONE);
                }
            }
            if (ccButton != null) {
                if (mVideo != null && (mVideo.subtitleUrl != null || mVideo.isLive)) {
                    ccButton.setVisibility(VISIBLE);
                    ccButton.setOnClickListener(v -> {
                                castSubtitlesOn = !castSubtitlesOn;
                                arcCastManager.showSubtitles(castSubtitlesOn);
                                ccButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()),
                                        castSubtitlesOn ? R.drawable.CcDrawableButton : R.drawable.CcOffDrawableButton));
                            }
                    );
                    ccButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), R.drawable.CcOffDrawableButton));
                } else {
                    ccButton.setVisibility(GONE);
                }
            }
            if (shareButton != null) {
                shareButton.setOnClickListener(v -> {
                    TrackingTypeData.TrackingVideoTypeData videoData = utils.createTrackingVideoTypeData();
                    videoData.setArcVideo(mVideo);
                    videoData.setPosition(mCastPlayer.getCurrentPosition());
                    onVideoEvent(TrackingType.ON_SHARE, videoData);
                    mListener.onShareVideo(mHeadline, mShareUrl);
                });
                shareButton.setVisibility(TextUtils.isEmpty(mShareUrl) ? GONE : VISIBLE);
            }

            mListener.addVideoView(mCastControlView);
        }
    }


    private void setCurrentPlayer(Player currentPlayer) {
        if (this.currentPlayer == currentPlayer) {
            return;
        }

        // View management.
        ExoPlayer mLocalPlayer = playerState.getMLocalPlayer();
        PlayerState playerState = utils.createPlayerState(mConfig.getActivity(), mListener, utils, mConfig);
        PlayerControlView mCastControlView = playerState.getMCastControlView();
        
        if (currentPlayer == mLocalPlayer) {
            if (playerState.getMLocalPlayerView() != null) {
                playerState.getMLocalPlayerView().setVisibility(VISIBLE);
                currentPlayView = playerState.getMLocalPlayerView();
            }
            if (playerState.getMCastControlView() != null) {
                playerState.getMCastControlView().hide();
                playerState.getMCastControlView().setKeepScreenOn(false);
            }

        } else /* currentPlayer == castPlayer */ {
            if (playerState.getMLocalPlayerView() != null) playerState.getMLocalPlayerView().setVisibility(GONE);
            if (mCastControlView != null) {
                mCastControlView.show();
                mCastControlView.setKeepScreenOn(true);
                currentPlayView = mCastControlView;
            }
        }

        Player previousPlayer = this.currentPlayer;
        if (previousPlayer != null) {
            if (previousPlayer.getPlaybackState() != Player.STATE_ENDED) {
                mListener.setSavedPosition(mVideoId, previousPlayer.getCurrentPosition());
            }
            if (mVideo.shouldPlayAds && !TextUtils.isEmpty(mVideo.adTagUrl)) {
                try {
                    mAdsLoader = new ImaAdsLoader.Builder(Objects.requireNonNull(mConfig.getActivity()))
                            .setAdEventListener(this)
                            .build();
                    mAdsLoader.getAdsLoader().addAdsLoadedListener(utils.createAdsLoadedListener(mListener, mVideo, this));
                    mAdsLoader.setPlayer(mLocalPlayer);//TODO test ads here!!
                } catch (Exception e) {
                    if (mConfig.isLoggingEnabled()) {
                        Log.e("ArcVideoSDK", "Error preparing ad for video " + mVideoId, e);
                    }
                }
            }
            previousPlayer.stop();
            previousPlayer.clearMediaItems();
        }
        this.currentPlayer = currentPlayer;
        mVideoTracker = VideoTracker.getInstance(mListener, currentPlayer, trackingHelper, mIsLive, Objects.requireNonNull(mConfig.getActivity()));
        startVideoOnCurrentPlayer();
    }

    private void startVideoOnCurrentPlayer() {
        if (currentPlayer != null && mVideo != null) {
            currentPlayer.setPlayWhenReady(mVideo.autoStartPlay);
            if (currentPlayer == playerState.getMLocalPlayer() && playerState.getMLocalPlayer() != null) {
                playOnLocal();
            } else if (currentPlayer == playerState.getMCastPlayer() && playerState.getMCastPlayer() != null) {
                playOnCast();
            }
            currentPlayer.seekTo(mListener.getSavedPosition(mVideoId));

            trackingHelper.onPlaybackStart();
        }
    }

    private void playOnLocal() {
        MediaSource contentMediaSource = createMediaSourceWithCaptions();
        MediaSource adsMediaSource = null;
        if (mVideo.shouldPlayAds && !TextUtils.isEmpty(mVideo.adTagUrl)) {
            try {
                mAdsLoader = new ImaAdsLoader.Builder(Objects.requireNonNull(mConfig.getActivity()))
                        .setAdEventListener(this)
                        .build();

                MediaSource.Factory mediaSourceFactory =
                        new DefaultMediaSourceFactory(playerState.getMMediaDataSourceFactory())
                                .setLocalAdInsertionComponents(unusedAdTagUri -> mAdsLoader, playerState.getMLocalPlayerView());

                mAdsLoader.setPlayer(playerState.getMLocalPlayer());
                Uri adUri = Uri.parse(mVideo.adTagUrl.replaceAll("\\[(?i)timestamp]", Long.toString(new Date().getTime())));
                DataSpec dataSpec = new DataSpec(adUri);
                Pair<String, String> pair = new Pair<>("", adUri.toString());
                adsMediaSource = utils.createAdsMediaSource(contentMediaSource, dataSpec, pair, mediaSourceFactory, mAdsLoader, playerState.getMLocalPlayerView());
            } catch (Exception e) {
                mListener.onError(ArcVideoSDKErrorType.INIT_ERROR, e.getMessage(), e);
            }
        }
        initVideoCaptions();
        if (adsMediaSource != null) {
            playerState.getMLocalPlayer().setMediaSource(adsMediaSource);
            playerState.getMLocalPlayer().prepare();
        } else {
            playerState.getMLocalPlayer().setMediaSource(contentMediaSource);
            playerState.getMLocalPlayer().prepare();
            setUpPlayerControlListeners();
        }
    }

    private void playOnCast() {
        try {
            if (mVideo != null) {
                arcCastManager.doCastSession(mVideo, mListener.getSavedPosition(mVideoId), mConfig.getArtworkUrl());
            }
        } catch (UnsupportedOperationException e) {
            mListener.onTrackingEvent(TrackingType.ON_ERROR_OCCURRED, new TrackingTypeData.TrackingErrorTypeData(mVideo, mListener.getSessionId(), null));
        }
    }

    private void addToCast() {
        try {
            playerState.getMCastPlayer().addMediaItem(ArcCastManager.createMediaItem(mVideo));
        } catch (UnsupportedOperationException e) {
            mListener.onTrackingEvent(TrackingType.ON_ERROR_OCCURRED, new TrackingTypeData.TrackingErrorTypeData(mVideo, mListener.getSessionId(), null));
        }
    }

    @VisibleForTesting
    void playVideoAtIndex(int index) {
        try {
            if (mVideos != null && !mVideos.isEmpty()) {
                if (index < 0) {
                    index = 0;
                }
                if (index >= mVideos.size()) {
                    index = mVideos.size() - 1;
                }
                if (!mIsFullScreen) {
                    mListener.addVideoView(playerState.getMLocalPlayerView());
                } else {
                    addPlayerToFullScreen();
                }
                mVideoTracker = VideoTracker.getInstance(mListener, playerState.getMLocalPlayer(), trackingHelper, mIsLive, Objects.requireNonNull(mConfig.getActivity()));

                mVideo = mVideos.get(index);
                if (currentPlayer == playerState.getMLocalPlayer() && playerState.getMLocalPlayer() != null) {
                    playOnLocal();
                } else if (currentPlayer == playerState.getMCastPlayer() && playerState.getMCastPlayer() != null) {
                    addToCast();
                }
                for (View v : mFullscreenOverlays.values()) {
                    if (v.getParent() != null && v.getParent() instanceof ViewGroup) {
                        ((ViewGroup) v.getParent()).removeView(v);
                    }
                    playerState.getMLocalPlayerView().addView(v);
                }
                MediaSource contentMediaSource = createMediaSourceWithCaptions();
                MediaSource adsMediaSource = null;
                if (mVideo.shouldPlayAds && !TextUtils.isEmpty(mVideo.adTagUrl)) {
                    try {
                        mAdsLoader = new ImaAdsLoader.Builder(Objects.requireNonNull(mConfig.getActivity()))
                                .setAdEventListener(this)
                                .build();

                        MediaSource.Factory mediaSourceFactory =
                                new DefaultMediaSourceFactory(playerState.getMMediaDataSourceFactory())
                                        .setLocalAdInsertionComponents(unusedAdTagUri -> mAdsLoader, playerState.getMLocalPlayerView());

                        mAdsLoader.setPlayer(playerState.getMLocalPlayer());
                        Uri adUri = Uri.parse(mVideo.adTagUrl.replaceAll("\\[(?i)timestamp]", Long.toString(new Date().getTime())));
                        DataSpec dataSpec = new DataSpec(adUri);
                        Pair<String, String> pair = new Pair<>("", adUri.toString());
                        adsMediaSource = utils.createAdsMediaSource(contentMediaSource, dataSpec, pair, mediaSourceFactory, mAdsLoader, playerState.getMLocalPlayerView());//TODO test ads here too!!
                    } catch (Exception e) {
                        if (mConfig.isLoggingEnabled()) {
                            Log.d("ArcVideoSDK", "Error preparing ad for video " + mVideoId, e);
                        }
                    }
                }
                if (adsMediaSource != null) {
                    playerState.getMLocalPlayer().setMediaSource(adsMediaSource);
                } else {
                    playerState.getMLocalPlayer().setMediaSource(contentMediaSource);
                }

                setUpPlayerControlListeners();
            }
        } catch (Exception e) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.getMessage(), mVideo);
        }
    }

    @Override
    public void playVideos(@NonNull List<ArcVideo> videos) {
        if (videos.size() == 0) {
            onVideoEvent(TrackingType.ERROR_PLAYLIST_EMPTY, null);
            return;
        }
        mVideos = videos;
        mVideo = videos.get(0);

        playVideo();

    }

    @Override
    public void addVideo(@NonNull ArcVideo video) {
        if (mVideos != null) {
            mVideos.add(video);
        }
    }

    @Override
    public void pausePlay(boolean shouldPlay) {
        try {
            if (playerState.getMLocalPlayer() != null && playerState.getMLocalPlayerView() != null) {
                playerState.getMLocalPlayer().setPlayWhenReady(shouldPlay);
                playerState.getMLocalPlayerView().hideController();
            }
        } catch (Exception e) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.getMessage(), mVideo);
        }
    }

    @Override
    public void start() {
        try {
            if (playerState.getMLocalPlayer() != null) {
                playerState.getMLocalPlayer().setPlayWhenReady(true);
            }
        } catch (Exception e) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.getMessage(), mVideo);
        }
    }

    @Override
    public void pause() {
        try {
            if (playerState.getMLocalPlayer() != null) {
                playerState.getMLocalPlayer().setPlayWhenReady(false);
            }
        } catch (Exception e) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.getMessage(), mVideo);
        }
    }

    @Override
    public void resume() {

        try {
            if (playerState.getMLocalPlayer() != null) {
                playerState.getMLocalPlayer().setPlayWhenReady(true);
                createTrackingEvent(ON_PLAY_RESUMED);
            }
        } catch (Exception e) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.getMessage(), mVideo);
        }
    }

    @Override
    public void stop() {
        try {
            if (playerState.getMLocalPlayer() != null) {
                playerState.getMLocalPlayer().setPlayWhenReady(false);
                playerState.getMLocalPlayer().stop();
                playerState.getMLocalPlayer().seekTo(0);
            }
        } catch (Exception e) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.getMessage(), mVideo);
        }
    }

    @Override
    public void seekTo(int ms) {
        try {
            if (playerState.getMLocalPlayer() != null) {
                playerState.getMLocalPlayer().seekTo(ms);
            }
        } catch (Exception e) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.getMessage(), mVideo);
        }
    }

    @Override
    public int getPlaybackState() {
        if (currentPlayer != null) {
            return currentPlayer.getPlaybackState();
        }
        return 0;
    }

    @Override
    public void onVolumeChanged(float volume) {
        trackingHelper.volumeChange(volume);
    }

    @Override
    public boolean isFullScreen() {
        return mIsFullScreen;
    }

    @Override
    public void setFullscreenListener(ArcKeyListener listener) {
        mArcKeyListener = listener;
    }

    @Override
    public boolean getPlayWhenReadyState() {
        if (currentPlayer != null) {
            return currentPlayer.getPlayWhenReady();
        }
        return false;
    }

    @Override
    public void setPlayerKeyListener(final ArcKeyListener listener) {
        try {
            if (playerState.getMLocalPlayerView() != null) {
                playerState.getMLocalPlayerView().setOnKeyListener((v, keyCode, event) -> {
                    if (listener != null && event.getAction() == KeyEvent.ACTION_UP) {
                        if (event.getKeyCode() == KEYCODE_BACK) {
                            listener.onBackPressed();
                        } else {
                            listener.onKey(keyCode, event);
                        }
                    }
                    return false;
                });
            }
        } catch (Exception e) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.getMessage(), mVideo);
        }
        if (playerState.getMCastControlView() != null) {
            playerState.getMCastControlView().setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (listener != null) {
                        listener.onKey(keyCode, event);
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void setVolume(float volume) {
        try {
            if (playerState.getMLocalPlayer() != null) {
                playerState.getMLocalPlayer().setVolume(volume);
                final ImageButton volumeButton = playerState.getMLocalPlayerView().findViewById(R.id.exo_volume);
                if (volume > 0.0f) {
                    volumeButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), R.drawable.MuteOffDrawableButton));
                } else {
                    volumeButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), R.drawable.MuteDrawableButton));
                }
            }
        } catch (Exception e) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.getMessage(), mVideo);
        }
    }

    @Override
    public void toggleCaptions() {
        showCaptionsSelectionDialog();
    }

    @Override
    public boolean isPlaying() {
        if (currentPlayer != null) {
            return ((currentPlayer.getPlaybackState() == Player.STATE_READY)) && currentPlayer.getPlayWhenReady();
        }
        return false;
    }

    @Override
    public StyledPlayerView getPlayControls() {
        return playerState.getMLocalPlayerView();
    }

    @Override
    public long getAdType() {
        boolean adPlaying = playerState.getMLocalPlayer() != null && playerState.getMLocalPlayer().isPlayingAd();
        long adGroupTime = 0;

        if (adPlaying) {
            Timeline timeline = playerState.getMLocalPlayer().getCurrentTimeline();
            Timeline.Period period = timeline.getPeriod(playerState.getMLocalPlayer().getCurrentPeriodIndex(), new Timeline.Period());
            adGroupTime = period.getAdGroupTimeUs(playerState.getMLocalPlayer().getCurrentAdGroupIndex());
        }

        return adGroupTime;
    }

    private void setAudioAttributes() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build();
    }

    private void setUpPlayerControlListeners() {
        if (!mConfig.isDisableControlsFully()) {
            try {
                if (playerState.getMLocalPlayer() == null || playerState.getMLocalPlayerView() == null) {
                    return;
                }
                ImageButton fullscreenButton = playerState.getMLocalPlayerView().findViewById(R.id.exo_fullscreen);
                if (fullscreenButton != null) {
                    if (mConfig.getShowFullScreenButton()) {
                        fullscreenButton.setOnClickListener(v -> toggleFullScreenDialog(mIsFullScreen));
                        fullscreenButton.setVisibility(VISIBLE);
                    } else {
                        fullscreenButton.setVisibility(GONE);
                    }

                }
                ImageButton shareButton = playerState.getMLocalPlayerView().findViewById(R.id.exo_share);
                if (shareButton != null) {
                    shareButton.setOnClickListener(v -> {
                        TrackingTypeData.TrackingVideoTypeData videoData = utils.createTrackingVideoTypeData();
                        videoData.setArcVideo(mVideo);
                        videoData.setPosition(playerState.getMLocalPlayer().getCurrentPosition());
                        onVideoEvent(TrackingType.ON_SHARE, videoData);
                        mListener.onShareVideo(mHeadline, mShareUrl);
                    });
                    if (TextUtils.isEmpty(mShareUrl)) {
                        if (mConfig.isKeepControlsSpaceOnHide()) {
                            shareButton.setVisibility(View.INVISIBLE);
                        } else {
                            shareButton.setVisibility(GONE);
                        }
                    } else {
                        shareButton.setVisibility(VISIBLE);
                    }
                } else {
                    logNullErrorIfEnabled("shareButton", "setUpPlayerControlListeners");
                }
                ImageButton backButton = playerState.getMLocalPlayerView().findViewById(R.id.exo_back);
                if (backButton != null) {
                    if (mConfig.getShowBackButton()) {
                        backButton.setOnClickListener(v -> {
                            TrackingTypeData.TrackingVideoTypeData videoData = utils.createTrackingVideoTypeData();
                            videoData.setArcVideo(mVideo);
                            videoData.setPosition(playerState.getMLocalPlayer().getCurrentPosition());
                            onVideoEvent(TrackingType.BACK_BUTTON_PRESSED, videoData);
                        });
                        backButton.setVisibility(VISIBLE);
                    } else {
                        backButton.setVisibility(GONE);
                    }
                }

                ImageButton pipButton = playerState.getMLocalPlayerView().findViewById(R.id.exo_pip);
                if (pipButton != null) {
                    if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) || !mListener.isPipEnabled()) {
                        pipButton.setVisibility(GONE);
                    }
                    pipButton.setOnClickListener(v -> onPipEnter());
                } else {
                    logNullErrorIfEnabled("pipButton", "setUpPlayerControlListeners");
                }
                final ImageButton volumeButton = playerState.getMLocalPlayerView().findViewById(R.id.exo_volume);
                if (volumeButton != null) {
                    if (mConfig.getShowVolumeButton()) {
                        volumeButton.setVisibility(VISIBLE);
                        volumeButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), playerState.getMLocalPlayer().getVolume() != 0 ? R.drawable.MuteOffDrawableButton : R.drawable.MuteDrawableButton));
                        volumeButton.setOnClickListener(v -> {
                            if (playerState.getMLocalPlayer() != null && mVideo != null) {
                                TrackingTypeData.TrackingVideoTypeData videoData = utils.createTrackingVideoTypeData();
                                videoData.setArcVideo(mVideo);
                                videoData.setPosition(playerState.getMLocalPlayer().getCurrentPosition());
                                if (playerState.getMLocalPlayer().getVolume() != 0) {
                                    mCurrentVolume = playerState.getMLocalPlayer().getVolume();
                                    playerState.getMLocalPlayer().setVolume(0f);
                                    volumeButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), R.drawable.MuteDrawableButton));
                                    mListener.onTrackingEvent(TrackingType.ON_MUTE, videoData);
                                    trackingHelper.volumeChange(0f);
                                } else {
                                    playerState.getMLocalPlayer().setVolume(mCurrentVolume);
                                    volumeButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), R.drawable.MuteOffDrawableButton));
                                    mListener.onTrackingEvent(TrackingType.ON_UNMUTE, videoData);
                                    trackingHelper.volumeChange(mCurrentVolume);
                                }
                            }
                        });
                    } else {
                        volumeButton.setVisibility(GONE);
                    }
                } else {
                    logNullErrorIfEnabled("volumeButton", "setUpPlayerControlListeners");
                }

                ccButton = playerState.getMLocalPlayerView().findViewById(R.id.exo_cc);

                if (ccButton != null) {
                    ccButton.setOnClickListener(v -> {
                        if (mConfig.isShowClosedCaptionTrackSelection()) {
                            showCaptionsSelectionDialog();
                        } else {
                            toggleClosedCaption();
                        }
                    });
                    if (mConfig.enableClosedCaption() && isClosedCaptionAvailable()) {
                        ccButton.setVisibility(VISIBLE);
                    } else {
                        if (mConfig.isKeepControlsSpaceOnHide()) {
                            ccButton.setVisibility(View.INVISIBLE);
                        } else {
                            ccButton.setVisibility(GONE);
                        }
                    }
                } else {
                    logNullErrorIfEnabled("ccButton", "setUpPlayerControlListeners");
                }
                ImageButton nextButton = playerState.getMLocalPlayerView().findViewById(R.id.exo_next_button);
                ImageButton previousButton = playerState.getMLocalPlayerView().findViewById(R.id.exo_prev_button);

                if (mConfig.getShowNextPreviousButtons()) {
                    //separated these out in case somebody wants a next and not previous or vice versa
                    if (nextButton != null) {
                        nextButton.setVisibility(VISIBLE);
                        nextButton.setOnClickListener(v -> createTrackingEvent(NEXT_BUTTON_PRESSED));
                        if (mConfig.getShouldDisableNextButton()) {
                            nextButton.setEnabled(false);
                            nextButton.setAlpha(0.5f);
                        }
                    }
                    if (previousButton != null) {
                        previousButton.setVisibility(VISIBLE);
                        previousButton.setOnClickListener(v -> createTrackingEvent(PREV_BUTTON_PRESSED));
                        if (mConfig.getShouldDisablePreviousButton()) {
                            previousButton.setEnabled(false);
                            previousButton.setAlpha(0.5f);
                        }
                    }
                    //case of multiple videos being played, we enable next/prev functionality within sdk (and callbacks)
                    if (playingListOfVideos()) {
                        if (nextButton != null) {
                            if (haveMoreVideosToPlay()) {
                                nextButton.setOnClickListener(
                                        view -> {
                                            playVideoAtIndex(++currentVideoIndex);
                                            createTrackingEvent(NEXT_BUTTON_PRESSED);
                                        });
                            } else {
                                nextButton.setAlpha(0.5f);
                            }
                        }
                        if (previousButton != null) {
                            previousButton.setOnClickListener(view -> {
                                playVideoAtIndex(--currentVideoIndex);
                                createTrackingEvent(PREV_BUTTON_PRESSED);
                            });
                        }
                    }
                } else {
                    if (nextButton != null) {
                        nextButton.setVisibility(GONE);
                    }
                    if (previousButton != null) {
                        previousButton.setVisibility(GONE);
                    }
                }


                //seek buttons
                playerState.getMLocalPlayerView().setShowFastForwardButton(mConfig.isShowSeekButton() && !mIsLive);
                playerState.getMLocalPlayerView().setShowRewindButton(mConfig.isShowSeekButton() && !mIsLive);


                View exoPosition = playerState.getMLocalPlayerView().findViewById(R.id.exo_position);
                View exoDuration = playerState.getMLocalPlayerView().findViewById(R.id.exo_duration);
                DefaultTimeBar exoProgress = playerState.getMLocalPlayerView().findViewById(R.id.exo_progress);
                View separator = playerState.getMLocalPlayerView().findViewById(R.id.separator);
                if (exoDuration != null && exoPosition != null && exoProgress != null) {

                    exoProgress.setScrubberColor(Objects.requireNonNull(mConfig.getActivity()).getResources().getColor(R.color.TimeBarScrubberColor));
                    exoProgress.setPlayedColor(Objects.requireNonNull(mConfig.getActivity()).getResources().getColor(R.color.TimeBarPlayedColor));
                    exoProgress.setUnplayedColor(Objects.requireNonNull(mConfig.getActivity()).getResources().getColor(R.color.TimeBarUnplayedColor));
                    exoProgress.setBufferedColor(Objects.requireNonNull(mConfig.getActivity()).getResources().getColor(R.color.TimeBarBufferedColor));
                    exoProgress.setAdMarkerColor(Objects.requireNonNull(mConfig.getActivity()).getResources().getColor(R.color.AdMarkerColor));
                    exoProgress.setPlayedAdMarkerColor(Objects.requireNonNull(mConfig.getActivity()).getResources().getColor(R.color.AdPlayedMarkerColor));
                    LinearLayout exoTimeBarLayout = playerState.getMLocalPlayerView().findViewById(R.id.time_bar_layout);
                    if (!mConfig.isShowProgressBar() && exoTimeBarLayout != null) {
                        exoTimeBarLayout.setVisibility(GONE);
                    } else if (exoTimeBarLayout != null) {
                        exoTimeBarLayout.setVisibility(VISIBLE);
                    }

                    if (mIsLive) {
                        exoPosition.setVisibility(GONE);
                        exoDuration.setVisibility(GONE);
                        exoProgress.setVisibility(GONE);
                        separator.setVisibility(GONE);
                    } else {
                        exoPosition.setVisibility(VISIBLE);
                        exoDuration.setVisibility(
                                mConfig.isShowCountDown() ? VISIBLE : GONE);
                        exoProgress.setVisibility(VISIBLE);
                        separator.setVisibility(VISIBLE);
                    }
                } else {
                    logNullErrorIfEnabled("exo Duration, Position, or Progress", "setUpPlayerControlListeners");
                }

                playerState.getMLocalPlayerView().requestFocus();//TODO continue investigating this for fire tv// This doesn't seem to help anything, and I cannot tell this logic accomplishes anything

                if (mConfig.getControlsShowTimeoutMs() != null) {
                    playerState.getMLocalPlayerView().setControllerShowTimeoutMs(mConfig.getControlsShowTimeoutMs());
                }

                if (mConfig.isDisableControlsWithTouch()) {
                    playerState.getMLocalPlayerView().setControllerHideOnTouch(true);
                }

                if (title != null) {
                    if (mConfig.getShowTitleOnController()) {
                        if (mVideo != null) {
                            title.setText(mVideo.headline);
                            title.setVisibility(VISIBLE);
                        }
                    } else {
                        title.setVisibility(View.INVISIBLE);
                    }
                }
            } catch (Exception e) {
                mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.getMessage(), mVideo);
            }
        }
    }

    public boolean onKeyEvent(KeyEvent event) {
        return playerState.getMLocalPlayerView().dispatchKeyEvent(event);
    }

    private void openPIPSettings() {
        try {
            final Activity activity = mConfig.getActivity();
            utils.createAlertDialogBuilder(activity)
                    .setTitle("Picture-in-Picture functionality is disabled")
                    .setMessage("Would you like to enable Picture-in-Picture?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = utils.createIntent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", Objects.requireNonNull(mConfig.getActivity()).getPackageName(), null);
                            intent.setData(uri);
                            activity.startActivity(intent);
                        }

                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setCancelable(true)
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
        } catch (Exception e) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.getMessage(), mVideo);
        }
    }

    /**
     * Enable/Disable captions rendering according to user preferences
     */
    private void initCaptions() {
        boolean captionsEnabled = isVideoCaptionsEnabled();
        if (playerState.getMTrackSelector() != null) {
            final MappingTrackSelector.MappedTrackInfo mappedTrackInfo = playerState.getMTrackSelector().getCurrentMappedTrackInfo();
            if (mappedTrackInfo != null) {
                final int textRendererIndex = getTextRendererIndex(mappedTrackInfo);
                if (textRendererIndex != -1) {
                    DefaultTrackSelector.Parameters.Builder parametersBuilder = playerState.getMTrackSelector().buildUponParameters();
                    parametersBuilder.setRendererDisabled(textRendererIndex, !captionsEnabled);
                    playerState.getMTrackSelector().setParameters(parametersBuilder);
                }
            }
        }
    }

    public boolean isClosedCaptionAvailable() {
        try {
            if (playerState.getMTrackSelector() != null) {
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo = playerState.getMTrackSelector().getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    int textRendererIndex = getTextRendererIndex(mappedTrackInfo);

                    return (hasAvailableSubtitlesTracks(mappedTrackInfo, textRendererIndex) > 0);
                } else {
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isClosedCaptionTurnedOn() {
        return PrefManager.getBoolean(Objects.requireNonNull(mConfig.getActivity()), PrefManager.IS_CAPTIONS_ENABLED, false);
    }

    public boolean enableClosedCaption(boolean enable) {
        if (isClosedCaptionAvailable()) {
            return toggleClosedCaption(enable);
        }
        return false;
    }

    private void initVideoCaptions() {
        try {
            boolean captionsEnabled = PrefManager.getBoolean(Objects.requireNonNull(mConfig.getActivity()), PrefManager.IS_CAPTIONS_ENABLED, false);

            if (playerState.getMTrackSelector() != null) {
                final MappingTrackSelector.MappedTrackInfo mappedTrackInfo = playerState.getMTrackSelector().getCurrentMappedTrackInfo();

                if (mappedTrackInfo != null) {
                    final int textRendererIndex = getTextRendererIndex(mappedTrackInfo);
                    if (textRendererIndex != -1) {
                        DefaultTrackSelector.Parameters.Builder parametersBuilder = playerState.getMTrackSelector().buildUponParameters();

                        if (captionsEnabled) {
                            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(textRendererIndex);
                            DefaultTrackSelector.SelectionOverride override = utils.createSelectionOverride(trackGroups.length - 1, 0);
                            parametersBuilder.setSelectionOverride(textRendererIndex, trackGroups, override);
                            parametersBuilder.setRendererDisabled(textRendererIndex, false);
                        } else {
                            parametersBuilder.clearSelectionOverrides(textRendererIndex);
                            parametersBuilder.setRendererDisabled(textRendererIndex, true);
                        }

                        playerState.getMTrackSelector().setParameters(parametersBuilder);

                        if (!mConfig.isShowClosedCaptionTrackSelection() && ccButton != null) {
                            if (captionsEnabled) {
                                ccButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), R.drawable.CcDrawableButton));
                            } else {
                                ccButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), R.drawable.CcOffDrawableButton));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.getMessage(), mVideo);
        }
    }

    private boolean toggleClosedCaption(boolean show) {
        if (mConfig.isLoggingEnabled()) {
            String showString = show ? "on" : "off";
            Log.d("ArcVideoSDK", "Call to toggle CC " + showString);
        }
        try {
            if (playerState.getMTrackSelector() != null) {
                final MappingTrackSelector.MappedTrackInfo mappedTrackInfo = playerState.getMTrackSelector().getCurrentMappedTrackInfo();

                if (mappedTrackInfo != null) {
                    final int textRendererIndex = getTextRendererIndex(mappedTrackInfo);
                    if (textRendererIndex != -1) {

                        DefaultTrackSelector.Parameters.Builder parametersBuilder = playerState.getMTrackSelector().buildUponParameters();

                        if (show) {
                            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(textRendererIndex);
                            DefaultTrackSelector.SelectionOverride override = new DefaultTrackSelector.SelectionOverride(trackGroups.length - 1, 0);
                            parametersBuilder.setSelectionOverride(textRendererIndex, trackGroups, override);
                            parametersBuilder.setRendererDisabled(textRendererIndex, false);
                        } else {
                            parametersBuilder.clearSelectionOverrides(textRendererIndex);
                            parametersBuilder.setRendererDisabled(textRendererIndex, true);
                        }

                        playerState.getMTrackSelector().setParameters(parametersBuilder);
                        setVideoCaptionsEnabled(show);

                        PrefManager.saveBoolean(Objects.requireNonNull(mConfig.getActivity()), PrefManager.IS_CAPTIONS_ENABLED, show);

                        return true;
                    }
                }
            }
        } catch (Exception e) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.getMessage(), mVideo);
        }
        return false;
    }

    private void toggleClosedCaption() {
        if (playerState.getMTrackSelector() != null) {
            final MappingTrackSelector.MappedTrackInfo mappedTrackInfo = playerState.getMTrackSelector().getCurrentMappedTrackInfo();

            if (mappedTrackInfo != null) {
                final int textRendererIndex = getTextRendererIndex(mappedTrackInfo);
                if (textRendererIndex != -1) {

                    DefaultTrackSelector.Parameters parameters = playerState.getMTrackSelector().getParameters();

                    boolean show = !parameters.getRendererDisabled(textRendererIndex);

                    DefaultTrackSelector.Parameters.Builder parametersBuilder = playerState.getMTrackSelector().buildUponParameters();

                    if (!show) {
                        if (mConfig.isLoggingEnabled()) {
                            Log.d("ArcVideoSDK", "Toggling CC on");
                        }
                        TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(textRendererIndex);
                        DefaultTrackSelector.SelectionOverride override = new DefaultTrackSelector.SelectionOverride(trackGroups.length - 1, 0);
                        parametersBuilder.setSelectionOverride(textRendererIndex, trackGroups, override);
                        parametersBuilder.setRendererDisabled(textRendererIndex, false);
                    } else {
                        if (mConfig.isLoggingEnabled()) {
                            Log.d("ArcVideoSDK", "Toggling CC off");
                        }
                        parametersBuilder.clearSelectionOverrides(textRendererIndex);
                        parametersBuilder.setRendererDisabled(textRendererIndex, true);
                    }

                    playerState.getMTrackSelector().setParameters(parametersBuilder);
                    setVideoCaptionsEnabled(!show);

                    PrefManager.saveBoolean(Objects.requireNonNull(mConfig.getActivity()), PrefManager.IS_CAPTIONS_ENABLED, !show);
                }
            }
        }
    }

    public boolean setCcButtonDrawable(@DrawableRes int ccButtonDrawable) {
        if (ccButton != null) {
            ccButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), ccButtonDrawable));
            return true;
        }
        return false;
    }

    /**
     * Returns a number of caption tracks that have a non-null language
     */
    private int hasAvailableSubtitlesTracks(MappingTrackSelector.MappedTrackInfo mappedTrackInfo, int textRendererIndex) {
        int result = 0;
        try {
            TrackGroupArray trackGroups = mappedTrackInfo.getTrackGroups(textRendererIndex);
            for (int groupIndex = 0; groupIndex < trackGroups.length; groupIndex++) {
                TrackGroup group = trackGroups.get(groupIndex);
                for (int trackIndex = 0; trackIndex < group.length; trackIndex++) {
                    Format format = group.getFormat(trackIndex);
                    if (defaultTrackFilter.filter(format, trackGroups)) {
                        result++;
                    }
                }
            }
        } catch (Exception e) {
            if (mConfig.isLoggingEnabled()) {
                Log.d("ArcVideoSDK", "Exception thrown detecting CC tracks. " + e.getMessage());
            }
        }
        return result;
    }

    private void showCaptionsSelectionDialog() {
        try {
            if (playerState.getMTrackSelector() != null) {
                final MappingTrackSelector.MappedTrackInfo mappedTrackInfo = playerState.getMTrackSelector().getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    final int textRendererIndex = getTextRendererIndex(mappedTrackInfo);
                    if (textRendererIndex != -1) {
                        Pair<AlertDialog, ArcTrackSelectionView> dialogPair =
                                ArcTrackSelectionView.getDialog(mConfig.getActivity(),
                                        Objects.requireNonNull(mConfig.getActivity()).getString(R.string.captions_dialog_title),
                                        playerState.getMTrackSelector(),
                                        textRendererIndex,
                                        defaultTrackFilter);
                        dialogPair.second.setShowDisableOption(true);
                        dialogPair.second.setAllowAdaptiveSelections(false);
                        dialogPair.second.setShowDefault(false);
                        dialogPair.first.show();

                        dialogPair.first.setOnDismissListener(dialog -> {
                            // save the chosen option to preferences
                            DefaultTrackSelector.Parameters parameters = playerState.getMTrackSelector().getParameters();
                            boolean isDisabled = parameters.getRendererDisabled(textRendererIndex);
                            setVideoCaptionsEnabled(!isDisabled);
                        });
                    }
                }
            }
        } catch (Exception e) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.getMessage(), mVideo);
        }
    }

    private int getTextRendererIndex(MappingTrackSelector.MappedTrackInfo mappedTrackInfo) {
        try {
            int count = mappedTrackInfo.getRendererCount();
            for (int i = 0; i < count; i++) {
                if (mappedTrackInfo.getRendererType(i) == C.TRACK_TYPE_TEXT) {
                    return i;
                }
            }
        } catch (Exception e) {
        }
        return -1;
    }

    @Override
    public void setFullscreen(boolean full) {
        toggleFullScreenDialog(!full);

        if (!mConfig.isUseFullScreenDialog()) {
            mListener.setFullscreen(full);
        }
    }

    @Override
    public void setFullscreenUi(boolean full) {
        if (full) {
            if (trackingHelper != null) {
                trackingHelper.fullscreen();
            }
            ImageButton fullScreenButton = playerState.getMLocalPlayerView().findViewById(R.id.exo_fullscreen);
            if (fullScreenButton != null) {
                fullScreenButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), R.drawable.FullScreenDrawableButtonCollapse));
            } else {
                logNullErrorIfEnabled("fullScreenButton", "setFullscreenUi");
            }
            mIsFullScreen = true;
            createTrackingEvent(ON_OPEN_FULL_SCREEN);
        } else {
            if (trackingHelper != null) {
                trackingHelper.normalScreen();
            }
            if (playerState.getMLocalPlayerView() != null) {
                ImageButton fullScreenButton = playerState.getMLocalPlayerView().findViewById(R.id.exo_fullscreen);
                fullScreenButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), R.drawable.FullScreenDrawableButton));
                if (mListener.isStickyPlayer()) {
                    playerState.getMLocalPlayerView().hideController();
                    playerState.getMLocalPlayerView().requestLayout();
                }
            }
            mIsFullScreen = false;
            TrackingTypeData.TrackingVideoTypeData videoData = utils.createTrackingVideoTypeData();
            videoData.setArcVideo(mVideo);
            if (playerState.getMLocalPlayer() != null) {
                videoData.setPosition(playerState.getMLocalPlayer().getCurrentPosition());
            }
            onVideoEvent(TrackingType.ON_CLOSE_FULL_SCREEN, videoData);
        }
    }

    private void toggleFullScreenCast() {

        if (castFullScreenOn) {
            castFullScreenOn = false;

            ((ViewGroup) playerState.getMCastControlView().getParent()).removeView(playerState.getMCastControlView());
            mListener.getPlayerFrame().addView(playerState.getMCastControlView());

            if (mFullScreenDialog != null) {
                mFullScreenDialog.setOnDismissListener(null);
                mFullScreenDialog.dismiss();
            }

            TrackingTypeData.TrackingVideoTypeData videoData = utils.createTrackingVideoTypeData();
            videoData.setArcVideo(mVideo);
            videoData.setPosition(playerState.getMCastPlayer().getCurrentPosition());
            onVideoEvent(TrackingType.ON_CLOSE_FULL_SCREEN, videoData);
            trackingHelper.normalScreen();
            for (View v : mFullscreenOverlays.values()) {
                ((ViewGroup) v.getParent()).removeView(v);
                mListener.getPlayerFrame().addView(v);
            }
            ImageButton fullScreenButton = playerState.getMCastControlView().findViewById(R.id.exo_fullscreen);
            if (fullScreenButton != null) {
                fullScreenButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), R.drawable.FullScreenDrawableButton));
            } else {
                logNullErrorIfEnabled("fullScreenButton", "toggleFullScreenDialog");
            }
        } else {
            castFullScreenOn = true;
            if (playerState.getMCastControlView().getParent() != null) {
                ((ViewGroup) playerState.getMCastControlView().getParent()).removeView(playerState.getMCastControlView());
            }
            mFullScreenDialog = utils.createFullScreenDialog(Objects.requireNonNull(mConfig.getActivity()));
            mFullScreenDialog.addContentView(playerState.getMCastControlView(), utils.createLayoutParams());

            ImageButton fullScreenButton = playerState.getMCastControlView().findViewById(R.id.exo_fullscreen);
            if (fullScreenButton != null) {
                fullScreenButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), R.drawable.FullScreenDrawableButtonCollapse));
            } else {
                logNullErrorIfEnabled("fullScreenButton", "toggleFullScreenDialog");
            }

            addOverlayToFullScreen();
            mFullScreenDialog.show();
            mFullScreenDialog.setOnDismissListener(v -> toggleFullScreenCast());
            mIsFullScreen = true;
            createTrackingEvent(ON_OPEN_FULL_SCREEN);
            trackingHelper.fullscreen();
        }
    }

    private synchronized void toggleFullScreenDialog(boolean isFullScreen) {
        if (!isFullScreen) {
            mFullScreenDialog = utils.createFullScreenDialog(Objects.requireNonNull(mConfig.getActivity()));
            mFullScreenDialog.setOnKeyListener((dialog, keyCode, event) -> {
                //we need to avoid intercepting volume controls
                if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                    return false;
                }
                if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                    return false;
                }
                if (keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
                    return false;
                }//TODO improve this function and test
                //we do this so we don't get trigger for down and up
                if (event.getAction() != KeyEvent.ACTION_UP) {
                    return true;
                }
                if (keyCode == KEYCODE_BACK) {
                    if (mListener.isPipEnabled()) {
                        onPipEnter();

                    } else {
                        if (mArcKeyListener != null) {
                            mArcKeyListener.onBackPressed();
                        }
                    }
                } else {
                    if (firstAdCompleted || !mConfig.isEnableAds()) {
                        if (playerState.getMLocalPlayerView() != null && !playerState.getMLocalPlayerView().isControllerFullyVisible()) {
                            playerState.getMLocalPlayerView().showController();
                        }
                    }
                    if (mArcKeyListener != null) {
                        mArcKeyListener.onKey(keyCode, event);
                    }
                }
                return false;
            });
        }
        if (mFullScreenDialog != null && playerState.getMLocalPlayerView() != null) {
            if (!isFullScreen) {
                if (playerState.getMLocalPlayerView().getParent() instanceof ViewGroup) {
                    ((ViewGroup) playerState.getMLocalPlayerView().getParent()).removeView(playerState.getMLocalPlayerView());
                }
                addPlayerToFullScreen();
                addOverlayToFullScreen();
                ImageButton fullScreenButton = playerState.getMLocalPlayerView().findViewById(R.id.exo_fullscreen);
                if (fullScreenButton != null) {
                    fullScreenButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), R.drawable.FullScreenDrawableButtonCollapse));
                } else {
                    logNullErrorIfEnabled("fullScreenButton", "toggleFullScreenDialog");
                }
                mFullScreenDialog.show();
                mIsFullScreen = true;
                createTrackingEvent(ON_OPEN_FULL_SCREEN);
                trackingHelper.fullscreen();

            } else {
                if (playerState.getMLocalPlayerView().getParent() instanceof ViewGroup) {
                    ((ViewGroup) playerState.getMLocalPlayerView().getParent()).removeView(playerState.getMLocalPlayerView());
                }
                mListener.getPlayerFrame().addView(playerState.getMLocalPlayerView());
                for (View v : mFullscreenOverlays.values()) {
                    ((ViewGroup) v.getParent()).removeView(v);
                    mListener.getPlayerFrame().addView(v);
                }
                ImageButton fullScreenButton = playerState.getMLocalPlayerView().findViewById(R.id.exo_fullscreen);
                if (fullScreenButton != null) {
                    fullScreenButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), R.drawable.FullScreenDrawableButton));
                }
                if (mListener.isStickyPlayer()) {
                    playerState.getMLocalPlayerView().hideController();
                    playerState.getMLocalPlayerView().requestLayout();
                }
                mIsFullScreen = false;
                mFullScreenDialog.dismiss();
                TrackingTypeData.TrackingVideoTypeData videoData = utils.createTrackingVideoTypeData();
                videoData.setArcVideo(mVideo);
                videoData.setPosition(playerState.getMLocalPlayer() != null ? playerState.getMLocalPlayer().getCurrentPosition() : 0L);
                onVideoEvent(TrackingType.ON_CLOSE_FULL_SCREEN, videoData);
                trackingHelper.normalScreen();
            }
        }
    }

    private void addPlayerToFullScreen() {
        if (mFullScreenDialog != null && playerState.getMLocalPlayerView() != null) {
            mFullScreenDialog.addContentView(playerState.getMLocalPlayerView(), utils.createLayoutParams());
        }
    }

    private void addOverlayToFullScreen() {
        if (mFullScreenDialog != null) {
            for (View v : mFullscreenOverlays.values()) {
                ((ViewGroup) v.getParent()).removeView(v);
                mFullScreenDialog.addContentView(v, utils.createLayoutParams());
                v.bringToFront();
            }
        }
    }

    @Override
    public void onActivityResume() {
        if (mIsFullScreen && mVideo != null && playerState.getMLocalPlayerView() == null) {
            playVideo(mVideo);
        }
    }

    @Override
    public void release() {
        if (mIsFullScreen) {
            try {
                toggleFullScreenDialog(true);
            } catch (Exception e) {
            }
        }
        if (playerState.getMLocalPlayerView() != null) {
            try {
                if (playerState.getMLocalPlayerView().getParent() instanceof ViewGroup) {
                    ((ViewGroup) playerState.getMLocalPlayerView().getParent()).removeView(playerState.getMLocalPlayerView());
                }
                playerState.setMLocalPlayerView(null);
            } catch (Exception e) {
            }
        }
        if (videoTrackingSub != null) {
            try {
                videoTrackingSub.unsubscribe();
                videoTrackingSub = null;
            } catch (Exception e) {
            }
        }
        if (playerState.getMLocalPlayer() != null) {
            try {
                playerState.getMLocalPlayer().stop();
                playerState.getMLocalPlayer().release();
                playerState.setMLocalPlayer(null);
            } catch (Exception e) {
            }
        }
        if (playerState.getMTrackSelector() != null) {
            playerState.setMTrackSelector(null);
        }
        if (mAdsLoader != null) {
            try {
                mAdsLoader.setPlayer(null);
                mAdsLoader.release();
                mAdsLoader = null;
            } catch (Exception e) {
            }
        }
        if (!mIsFullScreen) {
            try {
                mListener.removePlayerFrame();
            } catch (Exception e) {
            }
        }
        if (playerState.getMCastPlayer() != null) {
            try {
                playerState.getMCastPlayer().setSessionAvailabilityListener(null);
                playerState.getMCastPlayer().release();
            } catch (Exception e) {
            }
        }
        if (playerState.getMCastControlView() != null) {
            try {
                playerState.getMCastControlView().setPlayer(null);
                if (playerState.getMCastControlView().getParent() instanceof ViewGroup) {
                    ((ViewGroup) playerState.getMCastControlView().getParent()).removeView(playerState.getMCastControlView());
                }
                playerState.setMCastControlView(null);
            } catch (Exception e) {
            }
        }

    }

    @Nullable
    @Override
    public String getId() {
        return mVideoId;
    }

    @Override
    public void onStickyPlayerStateChanged(boolean isSticky) {
        if (playerState.getMLocalPlayerView() != null) {
            if (isSticky && !mIsFullScreen) {
                playerState.getMLocalPlayerView().hideController();
                playerState.getMLocalPlayerView().requestLayout();
                playerState.getMLocalPlayerView().setControllerVisibilityListener((StyledPlayerView.ControllerVisibilityListener) visibilityState -> {
                    if (playerState.getMLocalPlayerView() != null
                            && visibilityState == VISIBLE) {
                        playerState.getMLocalPlayerView().hideController();
                        playerState.getMLocalPlayerView().requestLayout();
                    }
                });
            } else {
                playerState.getMLocalPlayerView().setControllerVisibilityListener((StyledPlayerView.ControllerVisibilityListener) null);
            }
        }
    }

    @Nullable
    @Override
    public ArcVideo getVideo() {
        return mVideo;
    }

    @Override
    public long getCurrentVideoDuration() {
        if (currentPlayer != null) {
            return currentPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public void showControls(boolean show) {
        if (playerState.getMLocalPlayerView() != null) {
            if (show && disabledControlsForAd) {
                Log.d("ArcVideoSDK", "Called showControls() but controls are disabled");
            }
            if (show && !disabledControlsForAd) {
                Log.d("ArcVideoSDK", "Calling showControls()");
                playerState.getMLocalPlayerView().showController();
                return;
            }
            if (!show) {
                Log.d("ArcVideoSDK", "Calling hideControls()");
                playerState.getMLocalPlayerView().hideController();
            }
        }
    }

    @Override
    public long getCurrentPosition() {
        if (currentPlayer != null) {
            return currentPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    @Override
    public long getCurrentTimelinePosition() {
        if (playerState.getMLocalPlayer() != null) {
            try {
                return playerState.getMLocalPlayer().getCurrentPosition() - playerState.getMLocalPlayer().getCurrentTimeline().getPeriod(playerState.getMLocalPlayer().getCurrentPeriodIndex(), period).getPositionInWindowMs();
            } catch (Exception e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    @Override
    public void onIsLoadingChanged(boolean isLoading) {
        mListener.setIsLoading(isLoading);
    }

    private boolean showCaptions() {
        return mConfig.isShowClosedCaptionTrackSelection() && isClosedCaptionAvailable();
    }


    @Override
    public void onTimelineChanged(Timeline timeline, int reason) {
        if (ccButton != null) {
            if (showCaptions()) {
                ccButton.setVisibility(VISIBLE);
            } else {
                if (mConfig.isKeepControlsSpaceOnHide()) {
                    ccButton.setVisibility(View.INVISIBLE);
                } else {
                    ccButton.setVisibility(GONE);
                }
            }
        }
    }

    @Override
    public void onTracksChanged(@NonNull Tracks tracks) {
        String language = "none";
        for (Tracks.Group group : tracks.getGroups()) {
            for (int index = 0; index < group.length; index++) {
                Format f = group.getTrackFormat(index);
                if (f.id != null && f.id.startsWith("CC:")) {
                    language = f.id.substring(f.id.lastIndexOf("CC:") + 3);
                }
            }
        }
        TrackingTypeData.TrackingSourceTypeData source = new TrackingTypeData.TrackingSourceTypeData();
        source.setSource(language);
        onVideoEvent(TrackingType.SUBTITLE_SELECTION, source);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playerStateCode) {
        if (playerStateCode == Player.STATE_IDLE && isCasting() && mVideo != null) {
            arcCastManager.reloadVideo(mVideo);
        } else {
            try {
                if (playerState.getMLocalPlayer() == null || mVideoTracker == null || playerState.getMLocalPlayerView() == null) {
                    return;
                }
                if (ccButton != null) {
                    if (showCaptions()) {
                        ccButton.setVisibility(VISIBLE);
                    } else {
                        if (mConfig.isKeepControlsSpaceOnHide()) {
                            ccButton.setVisibility(View.INVISIBLE);
                        } else {
                            ccButton.setVisibility(GONE);
                        }
                    }
                }
                TrackingTypeData.TrackingVideoTypeData videoData = new TrackingTypeData.TrackingVideoTypeData();
                videoData.setPosition(playerState.getMLocalPlayer().getCurrentPosition());
                if (playerStateCode == Player.STATE_BUFFERING) {
                    mListener.setIsLoading(true);
                } else {
                    if (playWhenReady && playerStateCode != Player.STATE_IDLE
                            && playerStateCode != Player.STATE_ENDED) {
                        playerState.getMLocalPlayerView().setKeepScreenOn(true);
                        if (mVideoTracker != null && (videoTrackingSub == null
                                || videoTrackingSub.isUnsubscribed())) {
                            videoTrackingSub = mVideoTracker.getObs().subscribe();
                        }
                        if (playWhenReady && playerStateCode == Player.STATE_READY && (mIsLive || playerState.getMLocalPlayer().getCurrentPosition() > 50)) {
                            mListener.onTrackingEvent(ON_PLAY_RESUMED, videoData);
                            trackingHelper.resumePlay();
                        }
                        if (!playerState.getMLocalPlayer().isPlayingAd()) {
                            initVideoCaptions();
                        }
                    } else if (mVideoId != null) {
                        playerState.getMLocalPlayerView().setKeepScreenOn(false);
                        if (mListener.isInPIP()) {
                            mListener.pausePIP();
                        }
                        if (playerStateCode == Player.STATE_ENDED) {
                            videoData.setPercentage(100);
                            videoData.setArcVideo(mVideo);
                            mListener.onTrackingEvent(TrackingType.ON_PLAY_COMPLETED, videoData);
                            if (videoTrackingSub != null) {
                                videoTrackingSub.unsubscribe();
                                videoTrackingSub = null;
                            }
                            mVideoTracker.reset();
                            mListener.setNoPosition(mVideoId);
                            if (haveMoreVideosToPlay()) {
                                playVideoAtIndex(++currentVideoIndex);
                            }
                            trackingHelper.onPlaybackEnd();
                        }
                        if (videoTrackingSub != null) {
                            videoTrackingSub.unsubscribe();
                            videoTrackingSub = null;
                        }
                        if (!playWhenReady && playerStateCode == Player.STATE_READY) {
                            mListener.onTrackingEvent(TrackingType.ON_PLAY_PAUSED, videoData);
                            trackingHelper.pausePlay();
                        }
                    }
                    mListener.setIsLoading(false);
                }
            } catch (Exception e) {
                if (mConfig.isLoggingEnabled()) {
                    Log.e("TAG", "Exoplayer Exception - " + e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void onPlaybackSuppressionReasonChanged(int playbackSuppressionReason) {

    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(@NonNull PlaybackException e) {
        if (e.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
            if (playerState.getMLocalPlayerView() != null) {
                for (View v : mFullscreenOverlays.values()) {
                    playerState.getMLocalPlayerView().removeView(v);
                }
            }
            if (playerState.getMLocalPlayer() != null) {
                playerState.getMLocalPlayer().seekToDefaultPosition();
                playerState.getMLocalPlayer().prepare();
            }
            onVideoEvent(BEHIND_LIVE_WINDOW_ADJUSTMENT, new TrackingTypeData.TrackingErrorTypeData(mVideo, mListener.getSessionId(), null));
            return;
        }
        if (e.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED) {
            mListener.onError(ArcVideoSDKErrorType.SOURCE_ERROR, Objects.requireNonNull(mConfig.getActivity()).getString(R.string.source_error), e.getCause());

            if (e.getCause() instanceof FileDataSource.FileDataSourceException) {
                // no url passed from backend
                mListener.logError("Exoplayer Source Error: No url passed from backend. Caused by:\n" + e.getCause());
            }
        } else {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, Objects.requireNonNull(mConfig.getActivity()).getString(R.string.unknown_error), e);
        }
        if (mConfig.isLoggingEnabled()) {
            Log.e(TAG, "ExoPlayer Error", e);
        }
    }

    @Override
    public void onPositionDiscontinuity(@NonNull Player.PositionInfo
                                                oldPosition, @NonNull Player.PositionInfo newPosition, int reason) {
        if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
            if (currentPlayer != null) {
                int latestWindowIndex = currentPlayer.getCurrentWindowIndex();
                try {//TODO this block seems to get trigger a lot, but seems to require a playlist to work/test
                    TrackingTypeData.TrackingVideoTypeData videoData = utils.createTrackingVideoTypeData();
                    videoData.setPercentage(100);
                    videoData.setArcVideo(mVideos.get(latestWindowIndex - 1));
                    onVideoEvent(TrackingType.ON_PLAY_COMPLETED, videoData);
                    TrackingTypeData.TrackingVideoTypeData videoData2 = utils.createTrackingVideoTypeData();
                    videoData2.setPercentage(0);
                    videoData2.setPosition(0L);
                    videoData2.setArcVideo(mVideos.get(latestWindowIndex));
                    mVideo = mVideos.get(latestWindowIndex);
                    onVideoEvent(TrackingType.ON_PLAY_STARTED, videoData2);
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }


    @Nullable
    private MediaSource createMediaSourceWithCaptions() {
        try {
            MediaSource videoMediaSource = createMediaSource(new MediaItem.Builder().setUri(Uri.parse(mVideo.id)).build());
            if (videoMediaSource != null) {
                if (mVideo != null && !TextUtils.isEmpty(mVideo.subtitleUrl)) {

                    MediaItem.SubtitleConfiguration config = new MediaItem.SubtitleConfiguration.Builder(Uri.parse(mVideo.subtitleUrl)).setMimeType(MimeTypes.TEXT_VTT).setLanguage("en").setId(mVideo.id).build();
                    SingleSampleMediaSource singleSampleSource = utils.createSingleSampleMediaSourceFactory(playerState.getMMediaDataSourceFactory())
                            .setTag(mVideo.id)
                            .createMediaSource(config, C.TIME_UNSET);
                    return utils.createMergingMediaSource(videoMediaSource, singleSampleSource);
                }
            }
            return videoMediaSource;
        } catch (Exception e) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.getMessage(), mVideo);
        }
        return null;
    }

    public boolean isVideoCaptionEnabled() {
        try {
            if (mVideo != null && mVideo.ccStartMode == ArcXPVideoConfig.CCStartMode.DEFAULT) {
                boolean defaultValue = false;
                Object service = Objects.requireNonNull(mConfig.getActivity()).getSystemService(Context.CAPTIONING_SERVICE);
                if (service instanceof CaptioningManager) {
                    defaultValue = ((CaptioningManager) service).isEnabled();
                }
                return PrefManager.getBoolean(Objects.requireNonNull(mConfig.getActivity()), PrefManager.IS_CAPTIONS_ENABLED, defaultValue);
            } else
                return mVideo != null && mVideo.ccStartMode == ArcXPVideoConfig.CCStartMode.ON;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public View getOverlay(String tag) {
        return mFullscreenOverlays.get(tag);
    }

    @Override
    public void removeOverlay(String tag) {
        View v = mFullscreenOverlays.get(tag);
        mFullscreenOverlays.remove(tag);
        ((ViewGroup) v.getParent()).removeView(v);
    }

    private void setVideoCaptionsEnabled(boolean value) {
        PrefManager.saveBoolean(Objects.requireNonNull(mConfig.getActivity()), PrefManager.IS_CAPTIONS_ENABLED, value);

        if (ccButton != null) {
            if (isVideoCaptionsEnabled()) {
                ccButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), R.drawable.CcDrawableButton));
            } else {
                ccButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), R.drawable.CcOffDrawableButton));
            }
        }
    }

    private void setVideoCaptionsStartupDrawable() {
        boolean enabled = PrefManager.getBoolean(Objects.requireNonNull(mConfig.getActivity()), PrefManager.IS_CAPTIONS_ENABLED, false) || mConfig.getCcStartMode() == ArcXPVideoConfig.CCStartMode.ON;

        if (enabled) {
            ccButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), R.drawable.CcDrawableButton));
        } else {
            ccButton.setImageDrawable(ContextCompat.getDrawable(Objects.requireNonNull(mConfig.getActivity()), R.drawable.CcOffDrawableButton));
        }
    }

    private boolean isVideoCaptionsEnabled() {
        try {
            if (playerState.getMTrackSelector() != null) {
                final MappingTrackSelector.MappedTrackInfo mappedTrackInfo = playerState.getMTrackSelector().getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    final int textRendererIndex = getTextRendererIndex(mappedTrackInfo);
                    if (textRendererIndex != -1) {

                        DefaultTrackSelector.Parameters parameters = playerState.getMTrackSelector().getParameters();
                        boolean val = parameters.getRendererDisabled(textRendererIndex);

                        return !val;
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    @Override
    public MediaSourceFactory setDrmSessionManagerProvider(@Nullable DrmSessionManagerProvider
                                                                   drmSessionManagerProvider) {
        return null;
    }

    @Override
    public MediaSourceFactory setLoadErrorHandlingPolicy(@Nullable LoadErrorHandlingPolicy
                                                                 loadErrorHandlingPolicy) {
        return null;
    }

    @Override
    public int[] getSupportedTypes() {
        // IMA does not support Smooth Streaming ads.
        return new int[]{C.TYPE_HLS, C.TYPE_SS, C.TYPE_DASH, C.TYPE_OTHER};
    }

    @Override
    public MediaSource createMediaSource(MediaItem mediaItem) {
        if (mediaItem.localConfiguration != null) {
            Uri mediaUri = mediaItem.localConfiguration.uri;

            @C.ContentType int type = Util.inferContentType(mediaUri);
            switch (type) {
                case C.CONTENT_TYPE_HLS:
                    return new HlsMediaSource.Factory(playerState.getMMediaDataSourceFactory()).createMediaSource(mediaItem);
                case C.CONTENT_TYPE_SS:
                    return new SsMediaSource.Factory(playerState.getMMediaDataSourceFactory()).createMediaSource(mediaItem);
                case C.CONTENT_TYPE_DASH:
                    return new DashMediaSource.Factory(playerState.getMMediaDataSourceFactory()).createMediaSource(mediaItem);
                case C.CONTENT_TYPE_OTHER:
                    return new ProgressiveMediaSource.Factory(playerState.getMMediaDataSourceFactory()).createMediaSource(mediaItem);
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

    public void onVideoEvent(@NotNull TrackingType trackingType, @Nullable TrackingTypeData
            value) {
        if (trackingType == TrackingType.VIDEO_PERCENTAGE_WATCHED && mIsLive) {
            return;
        }
        Log.e("ArcVideoSDK", "onVideoEvent " + trackingType);
        mListener.onTrackingEvent(trackingType, value);
    }

    @Override
    public void onAdEvent(AdEvent adEvent) {
        ArcAd ad = new ArcAd();
        if (adEvent.getAd() != null) {
            ad.setAdId(adEvent.getAd().getAdId());
            ad.setAdDuration(adEvent.getAd().getDuration());
            ad.setAdTitle(adEvent.getAd().getTitle());
            ad.setClickthroughUrl(adEvent.getAd().getSurveyUrl());
        }
        TrackingTypeData.TrackingAdTypeData value = new TrackingTypeData.TrackingAdTypeData();
        value.setPosition(getCurrentTimelinePosition());
        value.setArcAd(ad);
        switch (adEvent.getType()) {
            case AD_BREAK_READY:
                disableControls();
                break;
            case COMPLETED:
                onVideoEvent(TrackingType.AD_PLAY_COMPLETED, value);
                break;
            case AD_BREAK_ENDED:
                firstAdCompleted = true;
                onVideoEvent(TrackingType.AD_BREAK_ENDED, value);
                break;
            case ALL_ADS_COMPLETED:
                firstAdCompleted = true;
                adEnded();
                onVideoEvent(TrackingType.ALL_MIDROLL_AD_COMPLETE, value);
                break;
            case FIRST_QUARTILE:
                onVideoEvent(TrackingType.VIDEO_25_WATCHED, value);
                break;
            case MIDPOINT:
                onVideoEvent(TrackingType.VIDEO_50_WATCHED, value);
                break;
            case PAUSED:
                if (adPlaying && !adPaused) {
                    currentPlayer.pause();
                    adPaused = true;
                    onVideoEvent(TrackingType.AD_PAUSE, value);
                } else {
                    currentPlayer.play();
                    adPaused = false;
                    onVideoEvent(TrackingType.AD_RESUME, value);
                }
                break;
            case THIRD_QUARTILE:
                onVideoEvent(TrackingType.VIDEO_75_WATCHED, value);
                break;
            case LOADED:
                disableControls();
                onVideoEvent(TrackingType.AD_LOADED, value);
                break;
            case AD_BREAK_STARTED:
                disableControls();
                onVideoEvent(TrackingType.AD_BREAK_STARTED, value);
                break;
            case SKIPPABLE_STATE_CHANGED:
                onVideoEvent(TrackingType.AD_SKIP_SHOWN, value);
                break;
            case SKIPPED:
                firstAdCompleted = true;
                adEnded();
                onVideoEvent(TrackingType.AD_SKIPPED, value);
                break;
            case STARTED:
                onVideoEvent(TrackingType.AD_PLAY_STARTED, value);
                break;
            case CLICKED:
            case TAPPED:
                onVideoEvent(TrackingType.AD_CLICKED, value);
                break;
            case CUEPOINTS_CHANGED:
            case LOG:
            case ICON_TAPPED:
            case AD_PROGRESS:
            case AD_BUFFERING:
            case RESUMED:
                break;
        }
    }

    private void adEnded() {
        disabledControlsForAd = false;
        adPlaying = false;
        if (playerState.getMLocalPlayerView() != null) {
            if (!mConfig.isDisableControlsFully()) {
                playerState.getMLocalPlayerView().setUseController(true);
            }
        }
    }

    private void disableControls() {
        disabledControlsForAd = true;
        adPlaying = true;
        if (playerState.getMLocalPlayerView() != null) {
            if (!mConfig.isDisableControlsFully()) {
                playerState.getMLocalPlayerView().setUseController(false);
            }
        }
    }

    @Override
    public void onCastSessionAvailable() {
        setCurrentPlayer(playerState.getMCastPlayer());
    }

    @Override
    public void onCastSessionUnavailable() {
        setCurrentPlayer(playerState.getMLocalPlayer());
    }

    @VisibleForTesting
    @Nullable
    List<ArcVideo> getMVideos() {
        return mVideos;
    }

    @VisibleForTesting
    boolean getMIsLive() {
        return mIsLive;
    }

    @VisibleForTesting
    @Nullable
    String getMHeadline() {
        return mHeadline;
    }

    @VisibleForTesting
    @Nullable
    String getMShareUrl() {
        return mShareUrl;
    }

    @VisibleForTesting
    @Nullable
    String getMVideoId() {
        return mVideoId;
    }

    @VisibleForTesting
    boolean isControlDisabled() {
        return disabledControlsForAd;
    }

    @VisibleForTesting
    @Nullable
    public StyledPlayerView getMPlayerView() {
        return playerState.getMLocalPlayerView();
    }

//    @VisibleForTesting
//    @Nullable
//    public ExoPlayer getMPlayer() {
//        return mLocalPlayer;
//    }
//
//    @VisibleForTesting
//    @Nullable
//    public DefaultTrackSelector getMTrackSelector() {
//        return mTrackSelector;
//    }

    @VisibleForTesting
    @Nullable
    public ImaAdsLoader getMAdsLoader() {
        return mAdsLoader;
    }

//    @VisibleForTesting
//    @Nullable
//    public PlayerControlView getMCastControlView() {
//        return mCastControlView;
//    }

    @VisibleForTesting
    @Nullable
    public ArcVideo getMVideo() {
        return mVideo;
    }

    @VisibleForTesting
    public boolean isFirstAdCompleted() {
        return firstAdCompleted;
    }

    private void logNullErrorIfEnabled(String nullMemberName, String callingMethod) {
        if (mConfig.isLoggingEnabled()) {
            Log.d("ArcVideoSDK", nullMemberName + " is null, called from " + callingMethod);
        }
    }

    public void onPipEnter() {
        if (mListener.isPipEnabled()) {
            if (!mIsFullScreen) {
                toggleFullScreenDialog(false);
            } else {
                wasInFullScreenBeforePip = true;
            }
            if (playerState.getMLocalPlayerView() != null) {
                playerState.getMLocalPlayerView().hideController();
            } else {
                logNullErrorIfEnabled("playerState.getMLocalPlayerView()", "onPipEnter");
            }
            mListener.setSavedPosition(mVideoId, playerState.getMLocalPlayer() != null ? playerState.getMLocalPlayer().getCurrentPosition() : 0L);
            mListener.startPIP(mVideo);
        } else {
            openPIPSettings();
        }
    }

    public void onPipExit() {
        if (playerState.getMLocalPlayerView() != null) {
            if (!mConfig.isDisableControlsFully()) {
                playerState.getMLocalPlayerView().setUseController(true);
            }
        }
        if (wasInFullScreenBeforePip) {
            wasInFullScreenBeforePip = false;
        } else {
            toggleFullScreenDialog(true);
        }
    }

    public boolean isCasting() {
        return currentPlayer == playerState.getMCastPlayer();
    }

    private boolean haveMoreVideosToPlay() {
        return mVideos != null && currentVideoIndex < mVideos.size() - 1;
    }

    private boolean playingListOfVideos() {
        return mVideos != null && mVideos.size() > 1;
    }

    private void createTrackingEvent(TrackingType trackingType) {
        TrackingTypeData.TrackingVideoTypeData videoData = utils.createTrackingVideoTypeData();
        videoData.setArcVideo(mVideo);
        videoData.setPosition(playerState.getMLocalPlayer() != null ? playerState.getMLocalPlayer().getCurrentPosition() : 0L);
        mListener.onTrackingEvent(trackingType, videoData);
    }
}

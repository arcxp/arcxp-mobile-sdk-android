package com.arcxp.video.players;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.arcxp.video.model.TrackingType.BEHIND_LIVE_WINDOW_ADJUSTMENT;
import static com.arcxp.video.model.TrackingType.ON_PLAY_RESUMED;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.CaptioningManager;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.arcxp.sdk.R;
import com.arcxp.video.ArcXPVideoConfig;
import com.arcxp.video.VideoTracker;
import com.arcxp.video.cast.ArcCastManager;
import com.arcxp.video.listeners.VideoListener;
import com.arcxp.video.model.ArcAd;
import com.arcxp.video.model.ArcVideoSDKErrorType;
import com.arcxp.video.model.TrackingType;
import com.arcxp.video.model.TrackingTypeData;
import com.arcxp.video.util.PrefManager;
import com.arcxp.video.util.TrackingHelper;
import com.arcxp.video.util.Utils;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.Tracks;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.FileDataSource;

import java.util.Date;
import java.util.Objects;

/**
 * @hide
 */
public class PostTvPlayerImpl implements Player.Listener,
        AdEvent.AdEventListener, PostTvContract {
    private static final String TAG = PostTvPlayerImpl.class.getSimpleName();
    static final String ID_SUBTITLE_URL = "ID_SUBTITLE_URL";

    @NonNull
    private final VideoListener mListener;
    private final TrackingHelper trackingHelper;
    private final ArcCastManager arcCastManager;
    private final ArcXPVideoConfig mConfig;
    private final Utils utils;
    private final CaptionsManager captionsManager;
    private final PlayerState playerState;
    public final PlayerStateHelper playerStateHelper;//TODO don't expose if possible
    private final ArcVideoPlayer videoPlayer;

    public PostTvPlayerImpl(@NonNull ArcXPVideoConfig config,
                            @NonNull VideoListener listener,
                            @NonNull TrackingHelper helper,
                            @NonNull Utils utils) {
        this.mConfig = config;
        this.trackingHelper = helper;
        this.arcCastManager = config.getArcCastManager();
        this.utils = utils;
        this.mListener = listener;
        this.playerState = utils.createPlayerState(Objects.requireNonNull(config.getActivity()), listener, utils, config);
        this.captionsManager = utils.createCaptionsManager(this.playerState, this.mConfig, this.mListener);
        this.playerStateHelper = utils.createPlayerStateHelper(this.playerState, this.trackingHelper, this.utils, this.mListener, this, this, this.captionsManager);
        this.videoPlayer = utils.createArcVideoPlayer(this.playerState, this.playerStateHelper, this.mListener, this.mConfig, this.arcCastManager, this.trackingHelper, this.captionsManager, this, this);
    }


    @VisibleForTesting
    @Override
    public void playVideoAtIndex(int index) {
        try {
            if (playerState.getMVideos() != null && !playerState.getMVideos().isEmpty()) {
                if (index < 0) {
                    index = 0;
                }
                if (index >= playerState.getMVideos().size()) {
                    index = playerState.getMVideos().size() - 1;
                }
                if (!playerState.getMIsFullScreen()) {
                    mListener.addVideoView(playerState.getMLocalPlayerView());
                } else {
                    playerStateHelper.addPlayerToFullScreen();
                }
                playerState.setMVideoTracker(VideoTracker.getInstance(mListener, playerState.getMLocalPlayer(), trackingHelper, playerState.getMIsLive(), Objects.requireNonNull(mConfig.getActivity())));

                playerState.setMVideo(playerState.getMVideos().get(index));
                if (playerState.getCurrentPlayer() == playerState.getMLocalPlayer() && playerState.getMLocalPlayer() != null) {
                    videoPlayer.playOnLocal();
                } else if (playerState.getCurrentPlayer() == playerState.getMCastPlayer() && playerState.getMCastPlayer() != null) {
                    videoPlayer.addToCast();
                }
                for (View v : playerState.getMFullscreenOverlays().values()) {
                    if (v.getParent() != null && v.getParent() instanceof ViewGroup) {
                        ((ViewGroup) v.getParent()).removeView(v);
                    }
                    playerState.getMLocalPlayerView().addView(v);
                }
                MediaSource contentMediaSource = captionsManager.createMediaSourceWithCaptions();
                MediaSource adsMediaSource = null;
                if (playerState.getMVideo().shouldPlayAds && !TextUtils.isEmpty(playerState.getMVideo().adTagUrl)) {
                    try {
                        playerState.setMAdsLoader(new ImaAdsLoader.Builder(Objects.requireNonNull(mConfig.getActivity()))
                                .setAdEventListener(this)
                                .build());

                        MediaSource.Factory mediaSourceFactory =
                                new DefaultMediaSourceFactory(playerState.getMMediaDataSourceFactory())
                                        .setLocalAdInsertionComponents(unusedAdTagUri -> playerState.getMAdsLoader(), playerState.getMLocalPlayerView());

                        playerState.getMAdsLoader().setPlayer(playerState.getMLocalPlayer());
                        Uri adUri = Uri.parse(playerState.getMVideo().adTagUrl.replaceAll("\\[(?i)timestamp]", Long.toString(new Date().getTime())));
                        DataSpec dataSpec = new DataSpec(adUri);
                        Pair<String, String> pair = new Pair<>("", adUri.toString());
                        adsMediaSource = utils.createAdsMediaSource(contentMediaSource, dataSpec, pair, mediaSourceFactory, playerState.getMAdsLoader(), playerState.getMLocalPlayerView());//TODO test ads here too!!
                    } catch (Exception e) {
                        if (mConfig.isLoggingEnabled()) {
                            Log.d("ArcVideoSDK", "Error preparing ad for video " + playerState.getMVideoId(), e);
                        }
                    }
                }
                if (adsMediaSource != null) {
                    playerState.getMLocalPlayer().setMediaSource(adsMediaSource);
                } else {
                    playerState.getMLocalPlayer().setMediaSource(contentMediaSource);
                }

                playerStateHelper.setUpPlayerControlListeners();
            }
        } catch (Exception e) {
            mListener.onError(ArcVideoSDKErrorType.EXOPLAYER_ERROR, e.getMessage(), e);
        }
    }

    @Override
    public void onVolumeChanged(float volume) {
        trackingHelper.volumeChange(volume);
    }


    public boolean onKeyEvent(KeyEvent event) {
        return playerState.getMLocalPlayerView().dispatchKeyEvent(event);
    }

    /**
     * Enable/Disable captions rendering according to user preferences
     */
    private void initCaptions() {
        boolean captionsEnabled = captionsManager.isVideoCaptionsEnabled();
        if (playerState.getMTrackSelector() != null) {
            final MappingTrackSelector.MappedTrackInfo mappedTrackInfo = playerState.getMTrackSelector().getCurrentMappedTrackInfo();
            if (mappedTrackInfo != null) {
                final int textRendererIndex = playerStateHelper.getTextRendererIndex(mappedTrackInfo);
                if (textRendererIndex != -1) {
                    DefaultTrackSelector.Parameters.Builder parametersBuilder = playerState.getMTrackSelector().buildUponParameters();
                    parametersBuilder.setRendererDisabled(textRendererIndex, !captionsEnabled);
                    playerState.getMTrackSelector().setParameters(parametersBuilder);
                }
            }
        }
    } //TODO what does this do? we don't use it


    public boolean enableClosedCaption(boolean enable) {
        return captionsManager.enableClosedCaption(enable);
    }//called from manager


    @Override
    public void onIsLoadingChanged(boolean isLoading) {
        mListener.setIsLoading(isLoading);
    }

    private boolean showCaptions() {
        return mConfig.isShowClosedCaptionTrackSelection() && captionsManager.isClosedCaptionAvailable();
    }


    @Override
    public void onTimelineChanged(@NonNull Timeline timeline, int reason) {
        if (playerState.getCcButton() != null) {
            if (showCaptions()) {
                playerState.getCcButton().setVisibility(VISIBLE);
            } else {
                if (mConfig.isKeepControlsSpaceOnHide()) {
                    playerState.getCcButton().setVisibility(View.INVISIBLE);
                } else {
                    playerState.getCcButton().setVisibility(GONE);
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
        playerStateHelper.onVideoEvent(TrackingType.SUBTITLE_SELECTION, source);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playerStateCode) {
        if (playerStateCode == Player.STATE_IDLE && videoPlayer.isCasting() && playerState.getMVideo() != null) {
            arcCastManager.reloadVideo(playerState.getMVideo());
        } else {
            try {
                if (playerState.getMLocalPlayer() == null || playerState.getMVideoTracker() == null || playerState.getMLocalPlayerView() == null) {
                    return;
                }
                if (playerState.getCcButton() != null) {
                    if (showCaptions()) {
                        playerState.getCcButton().setVisibility(VISIBLE);
                    } else {
                        if (mConfig.isKeepControlsSpaceOnHide()) {
                            playerState.getCcButton().setVisibility(View.INVISIBLE);
                        } else {
                            playerState.getCcButton().setVisibility(GONE);
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
                        if (playerState.getMVideoTracker() != null && (playerState.getVideoTrackingSub() == null
                                || playerState.getVideoTrackingSub().isUnsubscribed())) {
                            playerState.setVideoTrackingSub(playerState.getMVideoTracker().getObs().subscribe());
                        }
                        if (playWhenReady && playerStateCode == Player.STATE_READY && (playerState.getMIsLive() || playerState.getMLocalPlayer().getCurrentPosition() > 50)) {
                            mListener.onTrackingEvent(ON_PLAY_RESUMED, videoData);
                            trackingHelper.resumePlay();
                        }
                        if (!playerState.getMLocalPlayer().isPlayingAd()) {
                            captionsManager.initVideoCaptions();
                        }
                    } else if (playerState.getMVideoId() != null) {
                        playerState.getMLocalPlayerView().setKeepScreenOn(false);
                        if (mListener.isInPIP()) {
                            mListener.pausePIP();
                        }
                        if (playerStateCode == Player.STATE_ENDED) {
                            videoData.setPercentage(100);
                            videoData.setArcVideo(playerState.getMVideo());
                            mListener.onTrackingEvent(TrackingType.ON_PLAY_COMPLETED, videoData);
                            if (playerState.getVideoTrackingSub() != null) {
                                playerState.getVideoTrackingSub().unsubscribe();
                                playerState.setVideoTrackingSub(null);
                            }
                            playerState.getMVideoTracker().reset();
                            mListener.setNoPosition(playerState.getMVideoId());
                            if (playerStateHelper.haveMoreVideosToPlay()) {
                                playVideoAtIndex(playerState.incrementVideoIndex(true));
                            }
                            trackingHelper.onPlaybackEnd();
                        }
                        if (playerState.getVideoTrackingSub() != null) {
                            playerState.getVideoTrackingSub().unsubscribe();
                            playerState.setVideoTrackingSub(null);
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
                for (View v : playerState.getMFullscreenOverlays().values()) {
                    playerState.getMLocalPlayerView().removeView(v);
                }
            }
            if (playerState.getMLocalPlayer() != null) {
                playerState.getMLocalPlayer().seekToDefaultPosition();
                playerState.getMLocalPlayer().prepare();
            }
            playerStateHelper.onVideoEvent(BEHIND_LIVE_WINDOW_ADJUSTMENT, new TrackingTypeData.TrackingErrorTypeData(playerState.getMVideo(), mListener.getSessionId(), null));
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
            if (playerState.getCurrentPlayer() != null) {
                int latestWindowIndex = playerState.getCurrentPlayer().getCurrentWindowIndex();
                try {//TODO this block seems to get trigger a lot, but seems to require a playlist to work/test
                    TrackingTypeData.TrackingVideoTypeData videoData = utils.createTrackingVideoTypeData();
                    videoData.setPercentage(100);
                    videoData.setArcVideo(playerState.getMVideos().get(latestWindowIndex - 1));
                    playerStateHelper.onVideoEvent(TrackingType.ON_PLAY_COMPLETED, videoData);
                    TrackingTypeData.TrackingVideoTypeData videoData2 = utils.createTrackingVideoTypeData();
                    videoData2.setPercentage(0);
                    videoData2.setPosition(0L);
                    videoData2.setArcVideo(playerState.getMVideos().get(latestWindowIndex));
                    playerState.setMVideo(playerState.getMVideos().get(latestWindowIndex));
                    playerStateHelper.onVideoEvent(TrackingType.ON_PLAY_STARTED, videoData2);
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

    public boolean isVideoCaptionEnabled() {
        try {
            if (playerState.getMVideo() != null && playerState.getMVideo().ccStartMode == ArcXPVideoConfig.CCStartMode.DEFAULT) {
                boolean defaultValue = false;
                Object service = Objects.requireNonNull(mConfig.getActivity()).getSystemService(Context.CAPTIONING_SERVICE);
                if (service instanceof CaptioningManager) {
                    defaultValue = ((CaptioningManager) service).isEnabled();
                }
                return PrefManager.getBoolean(Objects.requireNonNull(mConfig.getActivity()), PrefManager.IS_CAPTIONS_ENABLED, defaultValue);
            } else
                return playerState.getMVideo() != null && playerState.getMVideo().ccStartMode == ArcXPVideoConfig.CCStartMode.ON;
        } catch (Exception e) {
            return false;
        }
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
        value.setPosition(videoPlayer.getCurrentTimelinePosition());
        value.setArcAd(ad);
        switch (adEvent.getType()) {
            case AD_BREAK_READY:
                videoPlayer.disableControls();
                break;
            case COMPLETED:
                playerStateHelper.onVideoEvent(TrackingType.AD_PLAY_COMPLETED, value);
                break;
            case AD_BREAK_ENDED:
                playerState.setFirstAdCompleted(true);
                playerStateHelper.onVideoEvent(TrackingType.AD_BREAK_ENDED, value);
                break;
            case ALL_ADS_COMPLETED:
                playerState.setFirstAdCompleted(true);
                adEnded();
                playerStateHelper.onVideoEvent(TrackingType.ALL_MIDROLL_AD_COMPLETE, value);
                break;
            case FIRST_QUARTILE:
                playerStateHelper.onVideoEvent(TrackingType.VIDEO_25_WATCHED, value);
                break;
            case MIDPOINT:
                playerStateHelper.onVideoEvent(TrackingType.VIDEO_50_WATCHED, value);
                break;
            case PAUSED:
                if (playerState.getAdPlaying() && !playerState.getAdPaused()) {
                    playerState.getCurrentPlayer().pause();
                    playerState.setAdPaused(true);
                    playerStateHelper.onVideoEvent(TrackingType.AD_PAUSE, value);
                } else {
                    playerState.getCurrentPlayer().play();
                    playerState.setAdPaused(false);
                    playerStateHelper.onVideoEvent(TrackingType.AD_RESUME, value);
                }
                break;
            case THIRD_QUARTILE:
                playerStateHelper.onVideoEvent(TrackingType.VIDEO_75_WATCHED, value);
                break;
            case LOADED:
                videoPlayer.disableControls();
                playerStateHelper.onVideoEvent(TrackingType.AD_LOADED, value);
                break;
            case AD_BREAK_STARTED:
                videoPlayer.disableControls();
                playerStateHelper.onVideoEvent(TrackingType.AD_BREAK_STARTED, value);
                break;
            case SKIPPABLE_STATE_CHANGED:
                playerStateHelper.onVideoEvent(TrackingType.AD_SKIP_SHOWN, value);
                break;
            case SKIPPED:
                playerState.setFirstAdCompleted(true);
                adEnded();
                playerStateHelper.onVideoEvent(TrackingType.AD_SKIPPED, value);
                break;
            case STARTED:
                playerStateHelper.onVideoEvent(TrackingType.AD_PLAY_STARTED, value);
                break;
            case CLICKED:
            case TAPPED:
                playerStateHelper.onVideoEvent(TrackingType.AD_CLICKED, value);
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
        playerState.setDisabledControlsForAd(false);
        playerState.setAdPlaying(false);
        if (playerState.getMLocalPlayerView() != null) {
            if (!mConfig.isDisableControlsFully()) {
                playerState.getMLocalPlayerView().setUseController(true);
            }
        }
    }


    public ArcVideoPlayer getVideoPlayer() {
        return videoPlayer;
    }
}

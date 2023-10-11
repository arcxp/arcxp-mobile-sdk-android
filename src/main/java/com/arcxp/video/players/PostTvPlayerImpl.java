package com.arcxp.video.players;

import androidx.annotation.NonNull;

import com.arcxp.video.ArcXPVideoConfig;
import com.arcxp.video.cast.ArcCastManager;
import com.arcxp.video.listeners.VideoListener;
import com.arcxp.video.model.ArcAd;
import com.arcxp.video.model.TrackingType;
import com.arcxp.video.model.TrackingTypeData;
import com.arcxp.video.util.TrackingHelper;
import com.arcxp.video.util.Utils;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.android.exoplayer2.Player;

import java.util.Objects;

/**
 * @hide
 */
public class PostTvPlayerImpl implements
        AdEvent.AdEventListener, PostTvContract {

    private final ArcXPVideoConfig mConfig;
    private final PlayerState playerState;
    public final PlayerStateHelper playerStateHelper;//TODO don't expose if possible
    private final ArcVideoPlayer videoPlayer;

    public PostTvPlayerImpl(@NonNull ArcXPVideoConfig config,
                            @NonNull VideoListener listener,
                            @NonNull TrackingHelper helper,
                            @NonNull Utils utils) {
        this.mConfig = config;
        ArcCastManager arcCastManager = config.getArcCastManager();
        this.playerState = utils.createPlayerState(Objects.requireNonNull(config.getActivity()), listener, config);
        CaptionsManager captionsManager = utils.createCaptionsManager(this.playerState, this.mConfig, listener);
        this.playerStateHelper = utils.createPlayerStateHelper(this.playerState, helper, listener, this, captionsManager);
        this.videoPlayer = utils.createArcVideoPlayer(this.playerState, this.playerStateHelper, listener, this.mConfig, arcCastManager, helper, captionsManager, this);
        Player.Listener playerListener = utils.createPlayerListener(this.playerState, this.playerStateHelper, listener, this.mConfig, arcCastManager, helper, captionsManager, this, this.videoPlayer);
        this.videoPlayer.setPlayerListener(playerListener);
        this.playerStateHelper.setPlayerListener(playerListener);
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

    @Override
    public void playVideoAtIndex(int index) {
        videoPlayer.playVideoAtIndex(index);
    } // called from helper
}

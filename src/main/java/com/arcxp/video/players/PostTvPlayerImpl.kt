package com.arcxp.video.players;

import androidx.annotation.NonNull;

import com.arcxp.video.ArcXPVideoConfig;
import com.arcxp.video.cast.ArcCastManager;
import com.arcxp.video.listeners.VideoListener;
import com.arcxp.video.util.TrackingHelper;
import com.arcxp.video.util.Utils;
import com.google.android.exoplayer2.Player;

import java.util.Objects;

/**
 * @hide
 */
public class PostTvPlayerImpl implements PostTvContract {
    private final PlayerStateHelper playerStateHelper;
    private final ArcVideoPlayer videoPlayer;

    public PostTvPlayerImpl(@NonNull ArcXPVideoConfig config,
                            @NonNull VideoListener listener,
                            @NonNull TrackingHelper helper,
                            @NonNull Utils utils) {
        ArcCastManager arcCastManager = config.getArcCastManager();
        PlayerState playerState = utils.createPlayerState(Objects.requireNonNull(config.getActivity()), listener, config);
        CaptionsManager captionsManager = utils.createCaptionsManager(playerState, config, listener);
        this.playerStateHelper = utils.createPlayerStateHelper(playerState, helper, listener, this, captionsManager);
        this.videoPlayer = utils.createArcVideoPlayer(playerState, this.playerStateHelper, listener, config, arcCastManager, helper, captionsManager);
        ArcAdEventListener arcAdEventListener = utils.createArcAdEventListener(playerState, this.playerStateHelper, config, this.videoPlayer);
        Player.Listener playerListener = utils.createPlayerListener(playerState, this.playerStateHelper, listener, config, arcCastManager, helper, captionsManager, arcAdEventListener, this.videoPlayer);
        this.videoPlayer.setPlayerListener(playerListener);
        this.videoPlayer.setAdEventListener(arcAdEventListener);
        this.playerStateHelper.setPlayerListener(playerListener);
    }

    public ArcVideoPlayer getVideoPlayer() {
        return videoPlayer;
    }

    public void onPipExit() { playerStateHelper.onPipExit();}
    public void onPipEnter() { playerStateHelper.onPipEnter();}
    @Override
    public void playVideoAtIndex(int index) {
        videoPlayer.playVideoAtIndex(index);
    } // called from helper
}

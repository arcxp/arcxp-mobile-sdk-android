/*
 * Copyright (C) 2015 . The Washington Post. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arcxp.video.util;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arcxp.sdk.R;
import com.arcxp.video.ArcVideoManager;
import com.arcxp.video.ArcXPVideoConfig;
import com.arcxp.video.cast.ArcCastManager;
import com.arcxp.video.listeners.AdsLoadedListener;
import com.arcxp.video.listeners.VideoListener;
import com.arcxp.video.listeners.VideoPlayer;
import com.arcxp.video.model.ArcVideo;
import com.arcxp.video.model.PlayerState;
import com.arcxp.video.model.TrackingTypeData;
import com.arcxp.video.players.ArcAdEventListener;
import com.arcxp.video.players.ArcVideoPlayer;
import com.arcxp.video.players.CaptionsManager;
import com.arcxp.video.players.PlayerContract;
import com.arcxp.video.players.PlayerListener;
import com.arcxp.video.players.PlayerStateHelper;
import com.arcxp.video.players.PostTvPlayerImpl;
import com.arcxp.video.views.VideoFrameLayout;
import com.bumptech.glide.Glide;
import com.google.ads.interactivemedia.pal.ConsentSettings;
import com.google.ads.interactivemedia.pal.NonceLoader;
import com.google.ads.interactivemedia.pal.NonceRequest;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.ext.cast.DefaultMediaItemConverter;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.cast.framework.CastContext;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.Scanner;
import java.util.Timer;

/**
 * Created by muppallav on 2/12/15.
 *
 * @hide
 */
public class Utils {
    final Application application;

    public Utils(Application application) {
        this.application = application;
    }

    private final static String NON_THIN = "[^iIl1\\.,']";

    public static boolean isAmazonBuild() {
        return "Amazon".equals(Build.MANUFACTURER);
    }

    public static String inputStreamToString(InputStream inputStream) {
        InputStream stream = BufferedInputStream.class.isInstance(inputStream) ? inputStream : new BufferedInputStream(inputStream);
        Scanner scanner = new Scanner(stream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    public ExoPlayer createExoPlayer() {
        return new ExoPlayer.Builder(application).setTrackSelector(createDefaultTrackSelector())
                .setSeekForwardIncrementMs(application.getResources().getInteger(R.integer.ff_inc))
                .setSeekBackIncrementMs(application.getResources().getInteger(R.integer.rew_inc))
                .setLooper(Looper.getMainLooper())
                .build();
    }

    public StyledPlayerView createPlayerView() {
        return new StyledPlayerView(application);
    }

    public CastPlayer createCastPlayer(CastContext castContext) {
        return new CastPlayer(castContext, new DefaultMediaItemConverter(), application.getResources().getInteger(R.integer.rew_inc), application.getResources().getInteger(R.integer.rew_inc));
    }

    public PlayerControlView createPlayerControlView() {
        return new PlayerControlView(application);
    }

    public DefaultDataSourceFactory createDefaultDataSourceFactory(Context mAppContext, String userAgent) {
        return new DefaultDataSourceFactory(mAppContext,
                userAgent == null || userAgent.isEmpty() ?
                        Util.getUserAgent(mAppContext, mAppContext.getResources().getString(R.string.app_name)) :
                        userAgent);
    }

    public AdsLoadedListener createAdsLoadedListener(@NonNull VideoListener listener, ArcVideo config, VideoPlayer player) {
        return new AdsLoadedListener(listener, config, player, listener.getSessionId());
    }

    public SingleSampleMediaSource.Factory createSingleSampleMediaSourceFactory(@NonNull DataSource.Factory dataSourceFactory) {
        return new SingleSampleMediaSource.Factory(dataSourceFactory);
    }

    public MergingMediaSource createMergingMediaSource(MediaSource... mediaSources) {
        return new MergingMediaSource(mediaSources);
    }

    public AdsMediaSource createAdsMediaSource(
            MediaSource contentMediaSource,
            DataSpec adTagDataSpec,
            Object adsId,
            MediaSource.Factory adMediaSourceFactory,
            AdsLoader adsLoader,
            com.google.android.exoplayer2.ui.AdViewProvider adViewProvider) {
        return new AdsMediaSource(contentMediaSource, adTagDataSpec, adsId, adMediaSourceFactory, adsLoader, adViewProvider);
    }

    public TrackingTypeData.TrackingVideoTypeData createTrackingVideoTypeData() {
        return new TrackingTypeData.TrackingVideoTypeData();
    }

    public DefaultTrackSelector.SelectionOverride createSelectionOverride(int groupIndex, int... tracks) {
        return new DefaultTrackSelector.SelectionOverride(groupIndex, tracks);
    }

    public AlertDialog.Builder createAlertDialogBuilder(Context activity) {
        return new AlertDialog.Builder(activity);
    }

    public Intent createIntent() {
        return new Intent();
    }

    public DefaultTrackSelector createDefaultTrackSelector() {
        return new DefaultTrackSelector(application);
    }

    public Dialog createFullScreenDialog(Context mAppContext) {
        return new Dialog(mAppContext, R.style.Fullscreen);
    }

    public ViewGroup.LayoutParams createLayoutParams() {
        return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public RelativeLayout.LayoutParams createRelativeLayoutParams(int width, int height) {
        return new RelativeLayout.LayoutParams(width, height);
    }

    public NonceLoader createNonceLoader(Context context) {
        return new NonceLoader(context, ConsentSettings.builder().allowStorage(false).build());
    }

    public TrackingTypeData.TrackingAdTypeData createNonceData() {
        return new TrackingTypeData.TrackingAdTypeData();
    }

    public void loadImageIntoView(String url, ImageView imageView) {
        Glide.with(application).load(url).into(imageView);
    }

    public VideoFrameLayout createVideoFrameLayout(Context context) {
        return new VideoFrameLayout(context);
    }

    public TrackingHelper createTrackingHelper(String videoId, ArcVideoManager arcVideoManager, ArcXPVideoConfig arcXPVideoConfig, Context mContext, VideoFrameLayout videoFrameLayout, VideoListener videoListener) {
        return new TrackingHelper(videoId, arcVideoManager, arcXPVideoConfig, mContext, videoFrameLayout, videoListener, this);
    }

    public PlayerContract createPostTvPlayerImpl(@NonNull ArcXPVideoConfig configInfo, @NonNull VideoListener videoListener, @NonNull TrackingHelper trackingHelper) {
        PlayerState playerState = new PlayerState(Objects.requireNonNull(configInfo.getActivity()), videoListener, this, configInfo);
        CaptionsManager captionsManager = new CaptionsManager(playerState, this, configInfo, videoListener);
        PlayerStateHelper playerStateHelper = new PlayerStateHelper(playerState, trackingHelper, this, videoListener, captionsManager);
        ArcCastManager arcCastManager = configInfo.getArcCastManager();
        ArcVideoPlayer arcVideoPlayer = new ArcVideoPlayer(playerState, playerStateHelper, videoListener, configInfo, arcCastManager, this, trackingHelper, captionsManager);
        AdEvent.AdEventListener arcAdEventListener = new ArcAdEventListener(playerState, playerStateHelper, arcVideoPlayer, configInfo);
        PlayerListener playerListener = new PlayerListener(trackingHelper, playerState, playerStateHelper, videoListener, captionsManager, configInfo, arcCastManager, this, arcAdEventListener, arcVideoPlayer);
        return new PostTvPlayerImpl(
                playerStateHelper,
                arcVideoPlayer,
                arcAdEventListener,
                playerListener
        );
    }

    public TextView createTextView(@NonNull Context context) {
        return new TextView(context);
    }

    public RelativeLayout createRelativeLayout(@NonNull Context context) {
        return new RelativeLayout(context);
    }

    public Format createTextSampleFormat(@Nullable String id, @Nullable String sampleMimeType, @C.SelectionFlags int selectionFlags, @Nullable String language) {
        return new Format.Builder()
                .setId(id)
                .setLanguage(language)
                .setSelectionFlags(selectionFlags)
                .setSampleMimeType(sampleMimeType)
                .build();
    }

    public MediaItem.SubtitleConfiguration createSubtitleConfiguration(Uri uri) {
        return new MediaItem.SubtitleConfiguration.Builder(uri)
                .setMimeType(MimeTypes.TEXT_VTT)
                .setLanguage("en")
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build();
    }

    public NonceRequest createNonceRequest(ArcXPVideoConfig config, String descriptionUrl, VideoFrameLayout layout) {

        return NonceRequest.builder()
                .descriptionURL(descriptionUrl)
                .omidVersion(config.getPalVersionName())
                .omidPartnerName(config.getPalPartnerName())
                .playerType("Exoplayer")
                .playerVersion(config.getExoplayerVersion())
                .ppid(config.getPalPpid())
                .videoPlayerHeight(layout.getWidth())
                .videoPlayerWidth(layout.getHeight())
                .willAdAutoPlay(true)
                .willAdPlayMuted(false)
                .build();
    }

    public Timer createTimer() {
        return new Timer();
    }

    @NotNull
    public AudioAttributes.Builder createAudioAttributeBuilder() {
        return new AudioAttributes.Builder();
    }

    public OmidHelper createOmidHelper(
            @NonNull Context context,
            @NonNull ArcXPVideoConfig config,
            VideoFrameLayout layout,
            VideoPlayer videoPlayer
    ) {
        return new OmidHelper(context,
                config,
                layout,
                videoPlayer
        );
    }

    public PalHelper createPalHelper(
            @NonNull Context context,
            @NonNull ArcXPVideoConfig config,
            VideoFrameLayout layout,
            VideoListener videoListener
    ) {
        return new PalHelper(context,
                config,
                layout,
                this,
                videoListener
        );
    }

    public MediaItem createMediaItem(String id) {
        return new MediaItem.Builder().setUri(Uri.parse(id)).build();
    }

    public MediaItem.SubtitleConfiguration createSubtitleConfig(String id, String url) {
        return new MediaItem.SubtitleConfiguration.Builder(Uri.parse(url))
                .setMimeType(MimeTypes.TEXT_VTT)
                .setLanguage("en")
                .setId(id)
                .build();
    }

    public DataSpec createDataSpec(Uri uri) {
        return new DataSpec(uri);
    }
}

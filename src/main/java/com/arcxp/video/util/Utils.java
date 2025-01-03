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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arcxp.commons.util.DependencyFactory;
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

import androidx.media3.common.C;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.common.Format;
import androidx.media3.common.MediaItem;
import androidx.media3.common.AudioAttributes;
import androidx.media3.cast.CastPlayer;
import androidx.media3.cast.DefaultMediaItemConverter;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.MergingMediaSource;
import androidx.media3.exoplayer.source.SingleSampleMediaSource;
import androidx.media3.exoplayer.source.ads.AdsLoader;
import androidx.media3.exoplayer.source.ads.AdsMediaSource;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.ui.PlayerControlView;
import androidx.media3.ui.PlayerView;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DataSpec;
import androidx.media3.datasource.DefaultDataSourceFactory;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.util.Util;
import com.google.android.gms.cast.framework.CastContext;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.Scanner;
import java.util.Timer;

/**
 * Utils is a utility class that provides various helper methods and factory functions for the ArcXP platform.
 * It includes methods for creating and configuring ExoPlayer instances, handling media sources, managing UI components, and performing common operations such as converting input streams to strings.
 *
 * The class defines the following methods:
 * - isAmazonBuild: Checks if the device is an Amazon build.
 * - inputStreamToString: Converts an InputStream to a String.
 * - createExoPlayer: Creates an ExoPlayer instance with the specified track selector.
 * - createPlayerView: Creates a PlayerView instance.
 * - createCastPlayer: Creates a CastPlayer instance with the specified CastContext.
 * - createPlayerControlView: Creates a PlayerControlView instance.
 * - createDefaultDataSourceFactory: Creates a DefaultDataSourceFactory instance with the specified user agent.
 * - createAdsLoadedListener: Creates an AdsLoadedListener instance with the specified parameters.
 * - createSingleSampleMediaSourceFactory: Creates a SingleSampleMediaSource.Factory instance with the specified DataSource.Factory.
 * - createMergingMediaSource: Creates a MergingMediaSource instance with the specified media sources.
 * - createAdsMediaSource: Creates an AdsMediaSource instance with the specified parameters.
 * - createTrackingVideoTypeData: Creates a TrackingTypeData.TrackingVideoTypeData instance.
 * - createSelectionOverride: Creates a DefaultTrackSelector.SelectionOverride instance with the specified group index and tracks.
 * - createAlertDialogBuilder: Creates an AlertDialog.Builder instance with the specified context.
 * - createIntent: Creates an Intent instance.
 * - createDefaultTrackSelector: Creates a DefaultTrackSelector instance.
 * - createFullScreenDialog: Creates a Dialog instance for full-screen mode with the specified context.
 * - createLayoutParams: Creates a ViewGroup.LayoutParams instance with match parent dimensions.
 * - createRelativeLayoutParams: Creates a RelativeLayout.LayoutParams instance with the specified width and height.
 * - createNonceLoader: Creates a NonceLoader instance with the specified context.
 * - createNonceData: Creates a TrackingTypeData.TrackingAdTypeData instance.
 * - loadImageIntoView: Loads an image from the specified URL into the specified ImageView using Glide.
 * - createVideoFrameLayout: Creates a VideoFrameLayout instance with the specified context.
 * - createTrackingHelper: Creates a TrackingHelper instance with the specified parameters.
 * - createPostTvPlayerImpl: Creates a PostTvPlayerImpl instance with the specified parameters.
 * - createTextView: Creates a TextView instance with the specified context.
 * - createRelativeLayout: Creates a RelativeLayout instance with the specified context.
 * - createTextSampleFormat: Creates a Format instance for text samples with the specified parameters.
 * - createSubtitleConfiguration: Creates a MediaItem.SubtitleConfiguration instance with the specified URI.
 * - createNonceRequest: Creates a NonceRequest instance with the specified parameters.
 * - createTimer: Creates a Timer instance.
 * - createAudioAttributeBuilder: Creates an AudioAttributes.Builder instance.
 * - createOmidHelper: Creates an OmidHelper instance with the specified parameters.
 * - createPalHelper: Creates a PalHelper instance with the specified parameters.
 * - createMediaItem: Creates a MediaItem instance with the specified URI.
 * - createSubtitleConfig: Creates a MediaItem.SubtitleConfiguration instance with the specified ID and URL.
 * - createDataSpec: Creates a DataSpec instance with the specified URI.
 * - isMinimalMode: Checks if the player control view is in minimal mode based on the specified parameters.
 *
 * Usage:
 * - Use the provided static methods to perform common operations and create instances of various components.
 *
 * Example:
 *
 * val exoPlayer = Utils(application).createExoPlayer(DefaultTrackSelector())
 * val playerView = Utils(application).createPlayerView()
 *
 * Note: This class is intended for internal use only and should not be exposed publicly.
 *
 * @method isAmazonBuild Checks if the device is an Amazon build.
 * @method inputStreamToString Converts an InputStream to a String.
 * @method createExoPlayer Creates an ExoPlayer instance with the specified track selector.
 * @method createPlayerView Creates a PlayerView instance.
 * @method createCastPlayer Creates a CastPlayer instance with the specified CastContext.
 * @method createPlayerControlView Creates a PlayerControlView instance.
 * @method createDefaultDataSourceFactory Creates a DefaultDataSourceFactory instance with the specified user agent.
 * @method createAdsLoadedListener Creates an AdsLoadedListener instance with the specified parameters.
 * @method createSingleSampleMediaSourceFactory Creates a SingleSampleMediaSource.Factory instance with the specified DataSource.Factory.
 * @method createMergingMediaSource Creates a MergingMediaSource instance with the specified media sources.
 * @method createAdsMediaSource Creates an AdsMediaSource instance with the specified parameters.
 * @method createTrackingVideoTypeData Creates a TrackingTypeData.TrackingVideoTypeData instance.
 * @method createSelectionOverride Creates a DefaultTrackSelector.SelectionOverride instance with the specified group index and tracks.
 * @method createAlertDialogBuilder Creates an AlertDialog.Builder instance with the specified context.
 * @method createIntent Creates an Intent instance.
 * @method createDefaultTrackSelector Creates a DefaultTrackSelector instance.
 * @method createFullScreenDialog Creates a Dialog instance for full-screen mode with the specified context.
 * @method createLayoutParams Creates a ViewGroup.LayoutParams instance with match parent dimensions.
 * @method createRelativeLayoutParams Creates a RelativeLayout.LayoutParams instance with the specified width and height.
 * @method createNonceLoader Creates a NonceLoader instance with the specified context.
 * @method createNonceData Creates a TrackingTypeData.TrackingAdTypeData instance.
 * @method loadImageIntoView Loads an image from the specified URL into the specified ImageView using Glide.
 * @method createVideoFrameLayout Creates a VideoFrameLayout instance with the specified context.
 * @method createTrackingHelper Creates a TrackingHelper instance with the specified parameters.
 * @method createPostTvPlayerImpl Creates a PostTvPlayerImpl instance with the specified parameters.
 * @method createTextView Creates a TextView instance with the specified context.
 * @method createRelativeLayout Creates a RelativeLayout instance with the specified context.
 * @method createTextSampleFormat Creates a Format instance for text samples with the specified parameters.
 * @method createSubtitleConfiguration Creates a MediaItem.SubtitleConfiguration instance with the specified URI.
 * @method createNonceRequest Creates a NonceRequest instance with the specified parameters.
 * @method createTimer Creates a Timer instance.
 * @method createAudioAttributeBuilder Creates an AudioAttributes.Builder instance.
 * @method createOmidHelper Creates an OmidHelper instance with the specified parameters.
 * @method createPalHelper Creates a PalHelper instance with the specified parameters.
 * @method createMediaItem Creates a MediaItem instance with the specified URI.
 * @method createSubtitleConfig Creates a MediaItem.SubtitleConfiguration instance with the specified ID and URL.
 * @method createDataSpec Creates a DataSpec instance with the specified URI.
 * @method isMinimalMode Checks if the player control view is in minimal mode based on the specified parameters.
 */
@SuppressLint("UnsafeOptInUsageError")
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

    public ExoPlayer createExoPlayer(DefaultTrackSelector trackSelector) {
        return new ExoPlayer.Builder(application).setTrackSelector(trackSelector)
                .setSeekForwardIncrementMs(application.getResources().getInteger(R.integer.ff_inc))
                .setSeekBackIncrementMs(application.getResources().getInteger(R.integer.rew_inc))
                .setLooper(Looper.getMainLooper())
                .build();
    }

    public PlayerView createPlayerView() {
        return new PlayerView(application);
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
            androidx.media3.common.AdViewProvider adViewProvider) {
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
        PlayerStateHelper playerStateHelper = new PlayerStateHelper(playerState, trackingHelper, this, videoListener, captionsManager, DependencyFactory.INSTANCE.createBuildVersionProvider());
        ArcCastManager arcCastManager = configInfo.getArcCastManager();
        ArcVideoPlayer arcVideoPlayer = new ArcVideoPlayer(playerState, playerStateHelper, videoListener, configInfo, arcCastManager, this, trackingHelper, captionsManager);
        AdEvent.AdEventListener arcAdEventListener = new ArcAdEventListener(playerState, playerStateHelper, arcVideoPlayer, configInfo, captionsManager);
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
                .skippablesSupported(false)
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
    /** the following was copied from PlayerControlViewLayoutManager.java media3 1.3.0
     * only alteration were the params, instead of member variables */
    public static boolean isMinimalMode(View playerControlView, View centerControls, View timeView, View overflowShowButton, View bottomBar) {
        int width =
                playerControlView.getWidth()
                        - playerControlView.getPaddingLeft()
                        - playerControlView.getPaddingRight();
        int height =
                playerControlView.getHeight()
                        - playerControlView.getPaddingBottom()
                        - playerControlView.getPaddingTop();

        int centerControlWidth =
                getWidthWithMargins(centerControls)
                        - (centerControls != null
                        ? (centerControls.getPaddingLeft() + centerControls.getPaddingRight())
                        : 0);
        int centerControlHeight =
                getHeightWithMargins(centerControls)
                        - (centerControls != null
                        ? (centerControls.getPaddingTop() + centerControls.getPaddingBottom())
                        : 0);

        int defaultModeMinimumWidth =
                Math.max(
                        centerControlWidth,
                        getWidthWithMargins(timeView) + getWidthWithMargins(overflowShowButton));
        int defaultModeMinimumHeight = centerControlHeight + (2 * getHeightWithMargins(bottomBar));

        return width <= defaultModeMinimumWidth || height <= defaultModeMinimumHeight;
    }

    private static int getWidthWithMargins(@Nullable View v) {
        if (v == null) {
            return 0;
        }
        int width = v.getWidth();
        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
            width += marginLayoutParams.leftMargin + marginLayoutParams.rightMargin;
        }
        return width;
    }

    private static int getHeightWithMargins(@Nullable View v) {
        if (v == null) {
            return 0;
        }
        int height = v.getHeight();
        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
            height += marginLayoutParams.topMargin + marginLayoutParams.bottomMargin;
        }
        return height;
    }
}

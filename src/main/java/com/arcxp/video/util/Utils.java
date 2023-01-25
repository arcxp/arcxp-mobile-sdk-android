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

package com.arc.arcvideo.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arc.arcvideo.ArcMediaPlayerConfig;
import com.arc.arcvideo.ArcVideoManager;
import com.arc.arcvideo.listeners.AdsLoadedListener;
import com.arc.arcvideo.listeners.VideoListener;
import com.arc.arcvideo.listeners.VideoPlayer;
import com.arc.arcvideo.model.ArcVideo;
import com.arc.arcvideo.model.TrackingTypeData;
import com.arc.arcvideo.players.PostTvPlayerImpl;
import com.arc.arcvideo.views.VideoFrameLayout;
import com.arc.flagship.features.arcvideo.R;
import com.bumptech.glide.Glide;
import com.google.ads.interactivemedia.pal.ConsentSettings;
import com.google.ads.interactivemedia.pal.NonceLoader;
import com.google.ads.interactivemedia.pal.NonceRequest;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.ext.cast.DefaultMediaItemConverter;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AdViewProvider;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.cast.framework.CastContext;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by muppallav on 2/12/15.
 *
 * @hide
 */
public class Utils {

    private final static String NON_THIN = "[^iIl1\\.,']";

    public static boolean isAmazonBuild() {
        return "Amazon".equals(Build.MANUFACTURER);
    }

    public static String inputStreamToString(InputStream inputStream) {
        InputStream stream = BufferedInputStream.class.isInstance(inputStream) ? inputStream : new BufferedInputStream(inputStream);
        Scanner scanner = new Scanner(stream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    public ExoPlayer.Builder createExoPlayerBuilder(Activity activity) {
        return new ExoPlayer.Builder(activity);
    }

    public StyledPlayerView createPlayerView(Context context) {
        return new StyledPlayerView(context);
    }

    public CastPlayer createCastPlayer(CastContext castContext, long seekBackIncrementMs, long seekForwardIncrementMs) {
        return new CastPlayer(castContext, new DefaultMediaItemConverter(), seekBackIncrementMs, seekForwardIncrementMs);
    }

    public PlayerControlView createPlayerControlView(Context context) {
        return new PlayerControlView(context);
    }

    public DefaultDataSourceFactory createDefaultDataSourceFactory(Context mAppContext, String userAgent) {
        return new DefaultDataSourceFactory(mAppContext,
                userAgent == null || userAgent.isEmpty() ?
                        Util.getUserAgent(mAppContext, mAppContext.getResources().getString(R.string.app_name)) :
                        userAgent);
    }

    public AdsLoadedListener createAdsLoadedListener(@NonNull VideoListener listener, ArcVideo config, VideoPlayer player, String sessionId) {
        return new AdsLoadedListener(listener, config, player, sessionId);
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
        return new DefaultTrackSelector();
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

    public void loadImageIntoView(Context context, String url, ImageView imageView) {
        Glide.with(context).load(url).into(imageView);
    }

    public VideoFrameLayout createVideoFrameLayout(Context context) {
        return new VideoFrameLayout(context);
    }

    public TrackingHelper createTrackingHelper(String videoId, ArcVideoManager arcVideoManager, ArcMediaPlayerConfig arcMediaPlayerConfig, Context mContext, VideoFrameLayout videoFrameLayout, VideoListener videoListener) {
        return new TrackingHelper(videoId, arcVideoManager, arcMediaPlayerConfig, mContext, videoFrameLayout, videoListener);
    }

    public PostTvPlayerImpl createPostTvPlayerImpl(@NonNull ArcMediaPlayerConfig configInfo, @NonNull ArcVideoManager arcVideoManager, @NonNull VideoListener videoListener, @NonNull TrackingHelper trackingHelper) {
        return new PostTvPlayerImpl(configInfo, arcVideoManager, videoListener, trackingHelper, this);
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

    public NonceRequest createNonceRequest(ArcMediaPlayerConfig config, String descriptionUrl, VideoFrameLayout layout) {

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
}

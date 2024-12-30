package com.arcxp.video.listeners;


import androidx.annotation.NonNull;

import com.arcxp.video.model.ArcAd;
import com.arcxp.video.model.ArcVideo;
import com.arcxp.video.model.TrackingTypeData;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.arcxp.video.model.TrackingType;
import com.google.ads.interactivemedia.v3.api.AdsRenderingSettings;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;

/**
 * AdsLoadedListener is a class that implements the AdsLoader.AdsLoadedListener interface to handle events related to the loading of ads within the ArcXP platform.
 * It manages the initialization of ads, listens for ad events, and triggers tracking events based on the ad lifecycle.
 *
 * The class defines the following properties:
 * - adType: A long value representing the type of ad (pre-roll, mid-roll, post-roll).
 * - adData: An instance of TrackingTypeData.TrackingAdTypeData containing data related to the ad.
 * - mListener: An instance of VideoListener for handling video-related events.
 * - configInfo: An instance of ArcVideo containing configuration information for the video.
 * - player: An instance of VideoPlayer for controlling video playback.
 * - sessionId: A string representing the session ID for the current video session.
 *
 * Usage:
 * - Create an instance of AdsLoadedListener with the necessary parameters.
 * - Use the onAdsManagerLoaded method to initialize the ads manager and set up ad event listeners.
 *
 * Example:
 *
 * AdsLoadedListener adsLoadedListener = new AdsLoadedListener(listener, config, player, sessionId);
 * adsLoader.addAdsLoadedListener(adsLoadedListener);
 *
 * Note: Ensure that the Google IMA SDK is properly configured before using AdsLoadedListener.
 *
 * @property adType A long value representing the type of ad (pre-roll, mid-roll, post-roll).
 * @property adData An instance of TrackingTypeData.TrackingAdTypeData containing data related to the ad.
 * @property mListener An instance of VideoListener for handling video-related events.
 * @property configInfo An instance of ArcVideo containing configuration information for the video.
 * @property player An instance of VideoPlayer for controlling video playback.
 * @property sessionId A string representing the session ID for the current video session.
 * @method onAdsManagerLoaded Initializes the ads manager and sets up ad event listeners.
 */
public class AdsLoadedListener implements AdsLoader.AdsLoadedListener {
    private long adType = 0;
    private TrackingTypeData.TrackingAdTypeData adData;
    @NonNull
    private VideoListener mListener;
    private ArcVideo configInfo;
    private VideoPlayer player;
    private String sessionId;

    public AdsLoadedListener(@NonNull VideoListener listener, ArcVideo config, VideoPlayer player, String sessionId) {
        this.mListener = listener;
        this.configInfo = config;
        this.player = player;
        this.sessionId = sessionId;
    }

    @Override
    public void onAdsManagerLoaded(AdsManagerLoadedEvent adsManagerLoadedEvent) {
        AdsRenderingSettings adsRenderingSettings = ImaSdkFactory.getInstance().createAdsRenderingSettings();
        if (configInfo != null) {
            adsRenderingSettings.setFocusSkipButtonWhenAvailable(configInfo.getFocusSkipButton());
        }
        adsManagerLoadedEvent.getAdsManager().init(adsRenderingSettings);
        adsManagerLoadedEvent.getAdsManager().addAdEventListener(adEvent -> {
            adData = new TrackingTypeData.TrackingAdTypeData(player.getCurrentTimelinePosition(), new ArcAd(adEvent.getAd()), sessionId, null);
            switch (adEvent.getType()) {
                case STARTED:
                    adType = mListener.getAdType();
                    if(adType == 0) {
                        mListener.onTrackingEvent(TrackingType.PREROLL_AD_STARTED, adData);
                    } else if(adType > 0){
                        mListener.onTrackingEvent(TrackingType.MIDROLL_AD_STARTED, adData);
                    } else {
                        mListener.onTrackingEvent(TrackingType.POSTROLL_AD_STARTED, adData);
                    }
                    break;
                case COMPLETED:
                    if(adType  == 0) {
                        mListener.onTrackingEvent(TrackingType.PREROLL_AD_COMPLETED, adData);
                    } else if(adType > 0){
                        mListener.onTrackingEvent(TrackingType.MIDROLL_AD_COMPLETED, adData);
                    } else {
                        mListener.onTrackingEvent(TrackingType.POSTROLL_AD_COMPLETED, adData);
                    }
                    break;
                case LOADED:
                    mListener.onTrackingEvent(TrackingType.AD_LOADED, adData);
                    break;
            }
        });
    }
}
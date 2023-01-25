package com.arcxp.video.listeners;


import androidx.annotation.NonNull;

import com.arc.arcvideo.ArcMediaPlayerConfig;
import com.arc.arcvideo.model.ArcAd;
import com.arc.arcvideo.model.ArcVideo;
import com.arc.arcvideo.model.TrackingTypeData;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.AdsLoader;
import com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent;
import com.arc.arcvideo.model.TrackingType;
import com.google.ads.interactivemedia.v3.api.AdsRenderingSettings;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;

/**
 * @hide
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
            adsRenderingSettings.setFocusSkipButtonWhenAvailable(configInfo.focusSkipButton);
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
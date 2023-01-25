package com.arc.arcvideo.model;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.arc.arcvideo.ArcMediaPlayerConfig;
import com.google.android.exoplayer2.C;

import java.util.Objects;

@Keep
public class ArcVideo {
    public static final int US_PER_MS = 1000;

    @NonNull
    public String id;
    public String uuid;
    public final long startPos;
    public final boolean isYouTube;
    public final boolean isLive;
    public final long duration;
    public final String shareUrl;
    public final String headline;
    public final String pageName;
    public final String videoName;
    public final String videoSection;
    public final String videoSource;
    public final String videoCategory;
    public final String contentId;
    public final String fallbackUrl;
    public String adTagUrl;
    public boolean shouldPlayAds;
    public final String subtitleUrl;
    public final Object source;
    private final Stream bestStream;
    public boolean autoStartPlay;
    public boolean startMuted;
    public boolean focusSkipButton;
    public ArcMediaPlayerConfig.CCStartMode ccStartMode = ArcMediaPlayerConfig.CCStartMode.DEFAULT;

    public ArcVideo(@NonNull String id, String uuid, long startPos, boolean isYouTube, boolean isLive, long duration, String shareUrl, String headline,
                    String pageName, String videoName, String videoSection, String videoSource, String videoCategory, String contentId,
                    String fallbackUrl, String adTagUrl, boolean shouldPlayAds, String subtitleUrl, Object source, Stream bestStream,
                    boolean autoStartPlay, boolean startMuted, boolean focusSkipButton, ArcMediaPlayerConfig.CCStartMode ccStartMode) {
        this.id = id;
        this.uuid = uuid;
        this.startPos = startPos;
        this.isYouTube = isYouTube;
        this.isLive = isLive;
        if (duration < 1) {
            this.duration = C.TIME_UNSET / US_PER_MS;
        } else {
            this.duration = duration;
        }
        this.shareUrl = shareUrl;
        this.headline = headline;
        this.pageName = pageName;
        this.videoName = videoName;
        this.videoSection = videoSection;
        this.videoSource = videoSource;
        this.videoCategory = videoCategory;
        this.contentId = contentId;
        this.fallbackUrl = fallbackUrl;
        this.adTagUrl = adTagUrl;
        this.shouldPlayAds = shouldPlayAds;
        this.subtitleUrl = subtitleUrl;
        this.source = source;
        this.bestStream = bestStream;
        this.autoStartPlay = autoStartPlay;
        this.startMuted = startMuted;
        this.focusSkipButton = focusSkipButton;
        this.ccStartMode = ccStartMode;
    }

    public Stream getBestStream() {
        return this.bestStream;
    }

    public String getUrl() {
        return this.id;
    }

    public static class Builder {
        private String id;
        private String uuid;
        private long startPos = 0;
        private boolean isYouTube = false;
        private boolean isLive = false;
        private long duration;
        private String shareUrl;
        private String headline;
        private String pageName;
        private String videoName;
        private String videoSection;
        private String videoSource;
        private String videoCategory;
        private String contentId;
        private String fallbackUrl;
        private String adTagUrl;
        private boolean shouldPlayAds;
        private String subtitleUrl;
        private Object source;
        private Stream bestStream;
        private boolean autoStart = true;
        private boolean startMuted = false;
        private boolean focusSkipButton = true;
        private ArcMediaPlayerConfig.CCStartMode ccStartMode = ArcMediaPlayerConfig.CCStartMode.DEFAULT;

        /**
         * @deprecated Use setUrl(String)
         * @param id URL of the video
         * @return (@link ArcVideo.Builder}
         */
        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setUrl(String url) {
            this.id = url;
            return this;
        }

        public Builder setStartPos(long startPos) {
            this.startPos = startPos;
            return this;
        }

        public Builder setIsYouTube(boolean isYouTube) {
            this.isYouTube = isYouTube;
            return this;
        }

        public Builder setIsLive(boolean isLive) {
            this.isLive = isLive;
            return this;
        }

        public Builder setDuration(long duration) {
            this.duration = duration;
            return this;
        }

        public Builder setShareUrl(String shareUrl) {
            this.shareUrl = shareUrl;
            return this;
        }

        public Builder setHeadline(String headline) {
            this.headline = headline;
            return this;
        }

        public Builder setPageName(String pageName) {
            this.pageName = pageName;
            return this;
        }

        public Builder setVideoName(String videoName) {
            this.videoName = videoName;
            return this;
        }

        public Builder setVideoSection(String videoSection) {
            this.videoSection = videoSection;
            return this;
        }

        public Builder setVideoSource(String videoSource) {
            this.videoSource = videoSource;
            return this;
        }

        public Builder setVideoCategory(String videoCategory) {
            this.videoCategory = videoCategory;
            return this;
        }

        public Builder setContentId(String contentId) {
            this.contentId = contentId;
            return this;
        }

        public Builder setFallbackUrl(String fallbackUrl) {
            this.fallbackUrl = fallbackUrl;
            return this;
        }

        public Builder setAdTagUrl(String adTagUrl) {
            this.adTagUrl = adTagUrl;
            return this;
        }

        public Builder setShouldPlayAds(boolean shouldPlayAds) {
            this.shouldPlayAds = shouldPlayAds;
            return this;
        }

        public Builder setSubtitleUrl(String subtitleUrl) {
            this.subtitleUrl = subtitleUrl;
            return this;
        }

        public Builder setSource(Object source) {
            this.source = source;
            return this;
        }

        public Builder setAutoStartPlay(boolean start) {
            this.autoStart = start;
            return this;
        }

        public Builder setStartMuted(boolean muted) {
            this.startMuted = muted;
            return this;
        }

        public Builder setFocusSkipButton(boolean focus) {
            this.focusSkipButton = focus;
            return this;
        }

        public Builder setCcStartMode(ArcMediaPlayerConfig.CCStartMode mode) {
            this.ccStartMode = mode;
            return this;
        }

        public Builder setUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder setVideoStream(ArcVideoStream stream, ArcMediaPlayerConfig config) {
            this.bestStream = stream.findBestStream(config.getPreferredStreamType(), config.getMaxBitRate());
            this.id = bestStream.getUrl().replace("\\", "");
            this.uuid = stream.getUuid();
            if (stream.getAdTagUrl() == null) {
                this.shouldPlayAds = config.getAdConfig() != null ? config.getAdConfig().isAdEnabled() : config.isEnableAds();
                this.adTagUrl = config.getAdConfig() != null ? config.getAdConfig().getAdConfigUrl() : config.getAdConfigUrl();
            } else {
                this.shouldPlayAds = true;
                this.adTagUrl = stream.getAdTagUrl();
            }
            this.startPos = 0;
            this.isYouTube = "youtube".equals(stream.getVideoType());
            this.isLive = "live".equals(stream.getVideoType());
            this.startMuted = config.isStartMuted();
            this.focusSkipButton = config.isFocusSkipButton();
            this.headline = stream.getHeadlines().getBasic();
            this.ccStartMode = config.getCcStartMode();
            if (stream.getSubtitles() != null && stream.getSubtitles().getUrls() != null) {
                for (SubtitleUrl url: stream.getSubtitles().getUrls()) {
                    if (url.getFormat().equals("WEB_VTT")) {
                        this.subtitleUrl = url.getUrl();
                        break;
                    }
                }
            }
            return this;
        }

        public Builder setVideoStreamVirtual(String url, ArcMediaPlayerConfig config) {
            this.id = url;
            this.isLive = true;
            this.startPos = 0;
            this.startMuted = config.isStartMuted();
            this.focusSkipButton = config.isFocusSkipButton();
            this.ccStartMode = config.getCcStartMode();
            this.adTagUrl = config.getAdConfigUrl();
            this.shouldPlayAds = config.isEnableAds();
            return this;
        }

        public ArcVideo build() {
            return new ArcVideo(id, uuid, startPos, isYouTube, isLive, duration, shareUrl, headline, pageName,
                    videoName, videoSection, videoSource, videoCategory, contentId, fallbackUrl,
                    adTagUrl, shouldPlayAds, subtitleUrl, source, bestStream, autoStart, startMuted,
                    focusSkipButton, ccStartMode);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArcVideo arcVideo = (ArcVideo) o;
        return startPos == arcVideo.startPos &&
                isYouTube == arcVideo.isYouTube &&
                isLive == arcVideo.isLive &&
                duration == arcVideo.duration &&
                shouldPlayAds == arcVideo.shouldPlayAds &&
                autoStartPlay == arcVideo.autoStartPlay &&
                startMuted == arcVideo.startMuted &&
                focusSkipButton == arcVideo.focusSkipButton &&
                id.equals(arcVideo.id) &&
                Objects.equals(uuid, arcVideo.uuid) &&
                Objects.equals(shareUrl, arcVideo.shareUrl) &&
                Objects.equals(headline, arcVideo.headline) &&
                Objects.equals(pageName, arcVideo.pageName) &&
                Objects.equals(videoName, arcVideo.videoName) &&
                Objects.equals(videoSection, arcVideo.videoSection) &&
                Objects.equals(videoSource, arcVideo.videoSource) &&
                Objects.equals(videoCategory, arcVideo.videoCategory) &&
                Objects.equals(contentId, arcVideo.contentId) &&
                Objects.equals(fallbackUrl, arcVideo.fallbackUrl) &&
                Objects.equals(adTagUrl, arcVideo.adTagUrl) &&
                Objects.equals(subtitleUrl, arcVideo.subtitleUrl) &&
                Objects.equals(source, arcVideo.source) &&
                Objects.equals(bestStream, arcVideo.bestStream) &&
                ccStartMode == arcVideo.ccStartMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uuid, startPos, isYouTube, isLive, duration, shareUrl, headline, pageName, videoName, videoSection, videoSource, videoCategory, contentId, fallbackUrl, adTagUrl, shouldPlayAds, subtitleUrl, source, bestStream, autoStartPlay, startMuted, focusSkipButton, ccStartMode);
    }
}
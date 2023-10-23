package com.arcxp.video.model

import androidx.annotation.Keep
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.ArcXPVideoConfig.CCStartMode
import com.google.android.exoplayer2.C
import java.util.Objects

@Keep
class ArcVideo(
    var url: String?,
    var uuid: String?,
    @JvmField val startPos: Long,
    val isYouTube: Boolean,
    @JvmField val isLive: Boolean,
    duration: Long,
    shareUrl: String?,
    headline: String?,
    pageName: String?,
    videoName: String?,
    videoSection: String?,
    videoSource: String?,
    videoCategory: String?,
    contentId: String?,
    fallbackUrl: String?,
    adTagUrl: String?,
    shouldPlayAds: Boolean,
    subtitleUrl: String?,
    source: Any?,
    bestStream: Stream?,
    autoStartPlay: Boolean,
    startMuted: Boolean,
    focusSkipButton: Boolean,
    ccStartMode: CCStartMode
) {
    var duration: Long = 0
    val shareUrl: String?
    val headline: String?
    val pageName: String?
    val videoName: String?
    val videoSection: String?
    val videoSource: String?
    val videoCategory: String?
    val contentId: String?
    val fallbackUrl: String?
    @JvmField
    var adTagUrl: String?
    @JvmField
    var shouldPlayAds: Boolean
    val subtitleUrl: String?
    val source: Any?
    @JvmField
    val bestStream: Stream?
    @JvmField
    var autoStartPlay: Boolean

    var startMuted: Boolean
    @JvmField
    var focusSkipButton: Boolean
    var ccStartMode = CCStartMode.DEFAULT

    init {
        if (duration < 1) {
            this.duration = C.TIME_UNSET / US_PER_MS
        } else {
            this.duration = duration
        }
        this.shareUrl = shareUrl
        this.headline = headline
        this.pageName = pageName
        this.videoName = videoName
        this.videoSection = videoSection
        this.videoSource = videoSource
        this.videoCategory = videoCategory
        this.contentId = contentId
        this.fallbackUrl = fallbackUrl
        this.adTagUrl = adTagUrl
        this.shouldPlayAds = shouldPlayAds
        this.subtitleUrl = subtitleUrl
        this.source = source
        this.bestStream = bestStream
        this.autoStartPlay = autoStartPlay
        this.startMuted = startMuted
        this.focusSkipButton = focusSkipButton
        this.ccStartMode = ccStartMode
    }

    class Builder {
        private var id: String? = null
        private var uuid: String? = null
        private var startPos: Long = 0
        private var isYouTube = false
        private var isLive = false
        private var duration: Long = 0
        private var shareUrl: String? = null
        private var headline: String? = null
        private var pageName: String? = null
        private var videoName: String? = null
        private var videoSection: String? = null
        private var videoSource: String? = null
        private var videoCategory: String? = null
        private var contentId: String? = null
        private var fallbackUrl: String? = null
        private var adTagUrl: String? = null
        private var shouldPlayAds = false
        private var subtitleUrl: String? = null
        private var source: Any? = null
        private var bestStream: Stream? = null
        private var autoStart = true
        private var startMuted = false
        private var focusSkipButton = true
        private var ccStartMode = CCStartMode.DEFAULT


        fun setUrl(url: String?): Builder {
            id = url
            return this
        }

        fun setStartPos(startPos: Long): Builder {
            this.startPos = startPos
            return this
        }

        fun setIsYouTube(isYouTube: Boolean): Builder {
            this.isYouTube = isYouTube
            return this
        }

        fun setIsLive(isLive: Boolean): Builder {
            this.isLive = isLive
            return this
        }

        fun setDuration(duration: Long): Builder {
            this.duration = duration
            return this
        }

        fun setShareUrl(shareUrl: String?): Builder {
            this.shareUrl = shareUrl
            return this
        }

        fun setHeadline(headline: String?): Builder {
            this.headline = headline
            return this
        }

        fun setPageName(pageName: String?): Builder {
            this.pageName = pageName
            return this
        }

        fun setVideoName(videoName: String?): Builder {
            this.videoName = videoName
            return this
        }

        fun setVideoSection(videoSection: String?): Builder {
            this.videoSection = videoSection
            return this
        }

        fun setVideoSource(videoSource: String?): Builder {
            this.videoSource = videoSource
            return this
        }

        fun setVideoCategory(videoCategory: String?): Builder {
            this.videoCategory = videoCategory
            return this
        }

        fun setContentId(contentId: String?): Builder {
            this.contentId = contentId
            return this
        }

        fun setFallbackUrl(fallbackUrl: String?): Builder {
            this.fallbackUrl = fallbackUrl
            return this
        }

        fun setAdTagUrl(adTagUrl: String?): Builder {
            this.adTagUrl = adTagUrl
            return this
        }

        fun setShouldPlayAds(shouldPlayAds: Boolean): Builder {
            this.shouldPlayAds = shouldPlayAds
            return this
        }

        fun setSubtitleUrl(subtitleUrl: String?): Builder {
            this.subtitleUrl = subtitleUrl
            return this
        }

        fun setSource(source: Any?): Builder {
            this.source = source
            return this
        }

        fun setAutoStartPlay(start: Boolean): Builder {
            autoStart = start
            return this
        }

        fun setStartMuted(muted: Boolean): Builder {
            startMuted = muted
            return this
        }

        fun setFocusSkipButton(focus: Boolean): Builder {
            focusSkipButton = focus
            return this
        }

        fun setCcStartMode(mode: CCStartMode): Builder {
            ccStartMode = mode
            return this
        }

        fun setUuid(uuid: String?): Builder {
            this.uuid = uuid
            return this
        }

        fun setVideoStream(stream: ArcVideoStream, config: ArcXPVideoConfig): Builder {
            bestStream = stream.findBestStream(config.getPreferredStreamType(), config.maxBitRate)
            id = bestStream!!.url.replace("\\", "")
            uuid = stream.uuid
            if (stream.adTagUrl == null) {
                shouldPlayAds =
                    if (config.adConfig != null) config.adConfig.isAdEnabled else config.isEnableAds
                adTagUrl =
                    if (config.adConfig != null) config.adConfig.getAdConfigUrl() else config.adConfigUrl
            } else {
                shouldPlayAds = true
                adTagUrl = stream.adTagUrl
            }
            startPos = 0
            isYouTube = "youtube" == stream.videoType
            isLive = "live" == stream.videoType
            startMuted = config.isStartMuted
            focusSkipButton = config.isFocusSkipButton
            headline = stream.headlines!!.basic
            ccStartMode = config.ccStartMode
            if (stream.subtitles != null && stream.subtitles.urls != null) {
                for ((format, url1) in stream.subtitles.urls) {
                    if (format == "WEB_VTT") {
                        subtitleUrl = url1
                        break
                    }
                }
            }
            return this
        }

        fun setVideoStreamVirtual(url: String?, config: ArcXPVideoConfig): Builder {
            id = url
            isLive = true
            startPos = 0
            startMuted = config.isStartMuted
            focusSkipButton = config.isFocusSkipButton
            ccStartMode = config.ccStartMode
            adTagUrl = config.adConfigUrl
            shouldPlayAds = config.isEnableAds
            return this
        }

        fun build(): ArcVideo {
            return ArcVideo(
                id, uuid, startPos, isYouTube, isLive, duration, shareUrl, headline, pageName,
                videoName, videoSection, videoSource, videoCategory, contentId, fallbackUrl,
                adTagUrl, shouldPlayAds, subtitleUrl, source, bestStream, autoStart, startMuted,
                focusSkipButton, ccStartMode
            )
        }
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val arcVideo = o as ArcVideo
        return startPos == arcVideo.startPos && isYouTube == arcVideo.isYouTube && isLive == arcVideo.isLive && duration == arcVideo.duration && shouldPlayAds == arcVideo.shouldPlayAds && autoStartPlay == arcVideo.autoStartPlay && startMuted == arcVideo.startMuted && focusSkipButton == arcVideo.focusSkipButton && url == arcVideo.url && uuid == arcVideo.uuid && shareUrl == arcVideo.shareUrl && headline == arcVideo.headline && pageName == arcVideo.pageName && videoName == arcVideo.videoName && videoSection == arcVideo.videoSection && videoSource == arcVideo.videoSource && videoCategory == arcVideo.videoCategory && contentId == arcVideo.contentId && fallbackUrl == arcVideo.fallbackUrl && adTagUrl == arcVideo.adTagUrl && subtitleUrl == arcVideo.subtitleUrl && source == arcVideo.source && bestStream == arcVideo.bestStream && ccStartMode === arcVideo.ccStartMode
    }

    override fun hashCode(): Int {
        return Objects.hash(
            url,
            uuid,
            startPos,
            isYouTube,
            isLive,
            duration,
            shareUrl,
            headline,
            pageName,
            videoName,
            videoSection,
            videoSource,
            videoCategory,
            contentId,
            fallbackUrl,
            adTagUrl,
            shouldPlayAds,
            subtitleUrl,
            source,
            bestStream,
            autoStartPlay,
            startMuted,
            focusSkipButton,
            ccStartMode
        )
    }

    companion object {
        const val US_PER_MS = 1000
    }
}
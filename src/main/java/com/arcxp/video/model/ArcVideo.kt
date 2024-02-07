package com.arcxp.video.model

import androidx.annotation.Keep
import com.arcxp.video.ArcXPVideoConfig
import com.arcxp.video.ArcXPVideoConfig.CCStartMode
import androidx.media3.common.C

@Keep
class ArcVideo(
    var id: String?,
    var uuid: String?,
    val startPos: Long = 0,
    val isYouTube: Boolean,
    val isLive: Boolean,
    private var _duration: Long = 0,
    val shareUrl: String?,
    val headline: String?,
    val pageName: String?,
    val videoName: String?,
    val videoSection: String?,
    val videoSource: String?,
    val videoCategory: String?,
    val contentId: String?,
    val fallbackUrl: String?,
    var adTagUrl: String?,
    var shouldPlayAds: Boolean,
    val subtitleUrl: String?,
    val source: Any?,
    val bestStream: Stream?,
    var autoStartPlay: Boolean,
    var startMuted: Boolean,
    var focusSkipButton: Boolean,
    var ccStartMode: CCStartMode = CCStartMode.DEFAULT
) {
    var duration: Long = _duration
        private set

    init {
        if (_duration < 1) {
            this.duration = C.TIME_UNSET / US_PER_MS
        }
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
            if (stream.subtitles?.urls != null) {
                for (url in stream.subtitles.urls) {
                    if (url.format.equals("WEB_VTT")) {
                        this.subtitleUrl = url.url
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



    companion object {
        const val US_PER_MS = 1000
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArcVideo

        if (id != other.id) return false
        if (uuid != other.uuid) return false
        if (startPos != other.startPos) return false
        if (isYouTube != other.isYouTube) return false
        if (isLive != other.isLive) return false
        if (_duration != other._duration) return false
        if (shareUrl != other.shareUrl) return false
        if (headline != other.headline) return false
        if (pageName != other.pageName) return false
        if (videoName != other.videoName) return false
        if (videoSection != other.videoSection) return false
        if (videoSource != other.videoSource) return false
        if (videoCategory != other.videoCategory) return false
        if (contentId != other.contentId) return false
        if (fallbackUrl != other.fallbackUrl) return false
        if (adTagUrl != other.adTagUrl) return false
        if (shouldPlayAds != other.shouldPlayAds) return false
        if (subtitleUrl != other.subtitleUrl) return false
        if (source != other.source) return false
        if (bestStream != other.bestStream) return false
        if (autoStartPlay != other.autoStartPlay) return false
        if (startMuted != other.startMuted) return false
        if (focusSkipButton != other.focusSkipButton) return false
        if (ccStartMode != other.ccStartMode) return false
        if (duration != other.duration) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (uuid?.hashCode() ?: 0)
        result = 31 * result + startPos.hashCode()
        result = 31 * result + isYouTube.hashCode()
        result = 31 * result + isLive.hashCode()
        result = 31 * result + _duration.hashCode()
        result = 31 * result + (shareUrl?.hashCode() ?: 0)
        result = 31 * result + (headline?.hashCode() ?: 0)
        result = 31 * result + (pageName?.hashCode() ?: 0)
        result = 31 * result + (videoName?.hashCode() ?: 0)
        result = 31 * result + (videoSection?.hashCode() ?: 0)
        result = 31 * result + (videoSource?.hashCode() ?: 0)
        result = 31 * result + (videoCategory?.hashCode() ?: 0)
        result = 31 * result + (contentId?.hashCode() ?: 0)
        result = 31 * result + (fallbackUrl?.hashCode() ?: 0)
        result = 31 * result + (adTagUrl?.hashCode() ?: 0)
        result = 31 * result + shouldPlayAds.hashCode()
        result = 31 * result + (subtitleUrl?.hashCode() ?: 0)
        result = 31 * result + (source?.hashCode() ?: 0)
        result = 31 * result + (bestStream?.hashCode() ?: 0)
        result = 31 * result + autoStartPlay.hashCode()
        result = 31 * result + startMuted.hashCode()
        result = 31 * result + focusSkipButton.hashCode()
        result = 31 * result + ccStartMode.hashCode()
        result = 31 * result + duration.hashCode()
        return result
    }


}
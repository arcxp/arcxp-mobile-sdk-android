# Implementing Video Analytics

The Mobile SDK Video module supplies numerous events for you to connect to your analytics service of choice.

Events are returned through the media player callback `trackMediaEvents().` You can read more about `trackMediaEvents()` in the [Getting Started](getting-started-with-the-video-module.md) guide. This method contains four callback methods, and can be implemented like this:

```kotlin
mediaPlayer.trackMediaEvents(object: ArcVideoEventsListener {
    override fun onVideoTrackingEvent(type: trackingType?, videoData: TrackingVideoTypeData?) {
    }
    override fun onSourceTrackingEvent(type: TrackingType?, source: TrackingSourceTypeData?) {
    }
    override fund onError(type: TrackingType?, video: TrackingErrorTypeData?) {
    }
    override fun onAdTrackingEvent(type: TrackingType?, adData: TrackingAdTypeData?) {
    }
})
```

The data returned with each callback method depends on the method. This signature for each of these data types is:

```kotlin
sealed class TrackingTypeData {
    data class TrackingVideoTypeData(
        var position: Long? = null,//position within the video
        var percentage: Int? = null,//percentage of video played
        var arcVideo: ArcVideo? = null,//video object being played
        var sessionId: String? = null//unique session ID for the video (livestream only)
    ) : TrackingTypeData()
     data class TrackingAdTypeData(
        var position: Long? = null,//position within the video
        var arcAd: ArcAd? = null,//ad object
        var sessionId: String? = null//unique session ID for the video (livestream only)
    ) : TrackingTypeData() 
     data class TrackingSourceTypeData(
        var position: Long? = null,//position within the video
        var source: String? = null,//identifier for the source
        var sessionId: String? = null//unique session ID for the video (livestream only)
    ) : TrackingTypeData()
     data class TrackingErrorTypeData(
        var arcVideo: ArcVideo? = null, //video object being played
        var sessionId: String? = null,//unique session ID for the video (livestream only)
        var adData: VideoAdAvail? = null//avail data that caused failure
    ) : TrackingTypeData()
}                        
```

## Events available

| Event | Description |
| --- | --- |
|`ON-PLAY-STARTED`| Reported when video playback starts. |
|`ON-PLAY-COMPLETED`| Reported when video playback completes. |
|`ON-PLAY-PAUSED`| Reported when video playback is paused. |
|`ON-PLAY-RESUMED`| Reported when video playback is resumed |
|`ON-OPEN-FULL-SCREEN`| Reported when the user enters full screen mode. |
|`ON-CLOSE-FULL-SCREEN`| Reported when the user exits full screen mode. |
|`ON-SHARE`| Sends a pre-configured event and url when share button is clicked. |
|`VIDEO-PERCENTAGE-WATCHED`| Reported when video progress is 25%, 50%, and 75% completed |
|`ON-ERROR-OCCURRED`| Reported when an error involving cast occurs. |
|`ON-MUTE`| Reported when the user mutes the video. |
|`ON-UNMUTE`| Reported when the user turns off mute on a muted video. |
|`VIDEO-25-WATCHED`| Reported when the video playback reaches 25%. |
|`VIDEO-50-WATCHED`| Reported when the video playback reaches 50%. |
|`VIDEO-75-WATCHED`| Reported when the video playback reaches 75%. |
|`PREROLL-AD-STARTED`| Reported when a preroll ad starts playing. |
|`PREROLL-AD-COMPLETED`| Reported when a preroll ad completes playing. |
|`MIDROLL-AD-STARTED`| Reported when a midroll ad starts playing. |
|`MIDROLL-AD-COMPLETED`| Reported when a midroll ad completes playing. |
|`ALL-MIDROLL-AD-COMPLETE`| Fired when a set of midroll ads is complete and the video playback resumes. |
|`POSTROLL-AD-STARTED`| Reported when a postroll ad starts playing. |
|`POSTROLL-AD-COMPLETED`| Reported when a postroll ad completes playing. |
|`MIDROLL-AD-25, MIDROLL-AD-50, MIDROLL-AD-75`| Reported when the midroll ad playback reaches the specified percentage. |
|`AD-CLICKTHROUGH`| Fired when an ad is clicked. This will return the beacon URL for the ad. |
|`AD-COMPANION-INFO`| Fired when this information for the ad is passed from the server. It will be returned through the data element of the callback. |
|`AD-MEDIA-FILES`| Fired when this information for the ad is passed from the server. It will be returned through the data element of the callback. |
|`AD-SKIPPED`| Reported when the user selects to skip an ad. |
|`AD-SKIP-SHOWN`| Reported when the skip button becomes visible in the player window |
|`AD-IMPRESSION`|`Not implemented yet.`|
|`ON-PLAYER-TOUCHED`| Fired when the player is touched regardless of if it is playing an ad or a video. If it is sent when an ad is playing the data returned will be of type `TrackingAdTypeData`. If it is sent during video playback, it will return the ID of the video as a string. |
|`ERROR-PLAYLIST-EMPTY`| Reported when the user tries to play an empty playlist |
|`SUBTITLE-SELECTION`| This event is returned through the `onSourceTrackingEvent` callback. It returns a string as its data that is the language selected for the closed captioning. It is only called if the closed caption dialog is used for selection. |
|`BEHIND-LIVE-WINDOW-ADJUSTMENT`| Triggered when pause on a live stream is on long enough that the current window is no longer valid so the player has to jump to the current window. This is caught and handled, but an event is fired when video jumps position. |
|`NEXT-BUTTON-PRESSED`| Triggered when the next button is clicked. |
|`PREV-BUTTON-PRESSED`| Triggered when the previous button is clicked. |
|`MALFORMED-AD-AVAIL`| Triggered when the Avail data is not as expected. |
|`BACK-BUTTON-PRESSED`| Triggered when the back button is clicked (not the physical device button but back button within playback control view. |
|`AD-PAUSE`| Triggered when an ad is paused. |
|`AD-RESUME`| Triggered when an ad is resumed. |

## Example

The following is an example of implementing a callback method (fill in callback methods as needed):

```kotlin
mediaPlayer.trackMediaEvents(object : ArcVideoEventsListener {
    override fun onVideoTrackingEvent(type: TrackingType, videoData: TrackingTypeData.TrackingVideoTypeData) {
                when (type) {
                    TrackingType.ON_MUTE -> {
                    }
                    TrackingType.ON_UNMUTE -> {
                    }
                    TrackingType.ON_PLAY_STARTED -> {
                    }
                    TrackingType.ON_PLAY_COMPLETED -> {
                    }
                    TrackingType.VIDEO_PERCENTAGE_WATCHED -> {
                    }
                }
            } 

    override fun onAdTrackingEvent(type: TrackingType, adData: TrackingTypeData.TrackingAdTypeData) {
        val arcAd: ArcAd = adData.arcAd ?: return
        when (type) {
            TrackingType.AD_CLICKTHROUGH -> {
            }
            TrackingType.AD_SKIPPED -> {
            }
            TrackingType.AD_SKIP_SHOWN -> {
            }
        }
    } 
    override fun onSourceTrackingEvent(type: TrackingType, trackingSourceTypeData: TrackingTypeData.TrackingSourceTypeData) {
    
    } 
    override fun onError(type: TrackingType, trackingErrorTypeData: TrackingTypeData.TrackingErrorTypeData) {
    
    }
})
```

## ArcAd

An `ArcAd` object is passed back with the `onAdTrackingEvent()` method through the `TrackingAdTypeData` object. This object contains important information about the currently playing ad.

| Parameter | Type | Description |
| --- | --- | --- |
| `adId` | String? | The unique ID of the ad |
| `adDuration` | Double? | Duration of the ad in seconds |
| `adTitle` | String? | Title of the ad |
| `companionAds` | List<JSONObject>? | Companion ad objects in JSON format |
| `mediaFiles` | MediaFiles? | MediaFiles object |
| `clickthroughUrl` | String? | The url to be navigated when the ad is touched |

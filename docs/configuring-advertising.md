# Configuring Advertising

The Mobile SDK's Video module for Android uses the Google IMA SDK for standard video pre-roll ads. You'll have to add the ad tag URL when instantiating the player for video on demand.

Live streams additionally support mid-roll ads using Arc's Dynamic Ad Insertion features. This page will walk you through setting up server-side ad reporting in the Mobile SDK Video module for Android.

## Client Side Ads and Server Side Ads

Enabling ads on live video streams involves turning on the client side ads and server side ads through the `ArcXPVideoConfig` object as described in the <a href= text='Getting Started' /> guide.

Setting the configuration for server side ads will tell the server to insert the ads into the live stream. Setting the configuration for client side ads tells the player to use the URL that contains the ads.

Ad information for the stream is returned through a separate URL. Once the stream is playing the SDK will periodically poll this URL to receive new or updated ad information. The default polling interval is set for 18 seconds. This can be changed by adding the following line to one of the XML files.
```xml
<integer name="ad_polling_delay_ms>integer milliseconds</integer>
```
It shouldn't be necessary to change this polling interval. We recommend setting it no longer than one minute.

When ad information is processed by the SDK, it will send a corresponding event to the client code as well as calling the beacon URL that is included in the ad data, except for the `AD_CLICKTHROUGH` event.

<Aside type='note'>
In this case, the beacon URL is passed back to the client code through the callback and it is the responsibility of the client application to handle reporting the clickthrough.
</Aside>

Once an ad starts playing, the SDK will report the various ad events to the client code through the callback method `onAdTrackingEvent()` which is defined in `ArcVideoEventsListener()`.

```kotlin
mediaPlayer.trackMediaEvents(object: ArcVideoEventsListener {
        …
    override fun  onAdTrackingEvent(type: TrackingType, adData: TrackingAdTypeData) {
        when (type) {
            ….
        }
    }
})
```

The `TrackingAdTypeData` class has the following signature:

```kotlin
data class TrackingAdTypeData(var position: Long,
    var arcAd: ArcAd,
    var sessionId: String)
```

where the position is the position within the video where the ad starts, the sessionId is the session identifier for the video and the `ArcAd` class has the following signature:

```kotlin
class ArcAd() {
    var adId: String,
    var adDuration: Double
    var adTitle: String,
    var companionAds: List<JSONObject>
    var clickthroughUrl: String
}
```

The following table lists all of the events that can be sent. Note that preroll and post-roll ads are only available with Google IMA ads for video on demand. For live streams, all ads are considered mid-roll ads.

Ad Callback Compatibility

| Event | Description | Google | Server |
| --- | --- | --- | --- |
| AD_LOADED | Google IMA ad has loaded | ✅ |   |
| AD_CLICKED | Google IMA ad is clicked | ✅ |   |
| PREROLL_AD_STARTED | Preroll ad starts playing | ✅ |   |
| PREROLL_AD_COMPLETED | Preroll ad completes | ✅ |   |
| MIDROLL_AD_STARTED | Midroll ad starts playing | ✅ | ✅ |
| MIDROLL_AD_COMPLETED | Midroll ad completes | ✅ | ✅ |
| POSTROLL_AD_STARTED | Postroll ad starts | ✅ |   |
| POSTROLL_AD_COMPLETED | Postroll ad completes | ✅ |   |
| AD_IMPRESSION | Ad impression event is sent |   | ✅ |
| ALL_MIDROLL_AD_COMPLETE | All midroll ads in a set have completed |   | ✅ |
| AD_CLICKTHROUGH | A server ad is clicked |   | ✅ |
| MIDROLL_AD_25 | Ad reaches 25% played |   | ✅ |
| MIDROLL_AD_50 | Ad reaches 50% played |   | ✅ |
| MIDROLL_AD_75 | Ad reaches 75% played |   | ✅ |
| AD_MEDIA_FILES | Media file information is available |   | ✅ |
| AD_COMPANION_INFO | Companion info is available |   | ✅ |
| AD_SKIPPED | Ad skip button is pressed | ✅ |   |
| AD_SKIP_SHOWN | Ad skip button is shown | ✅ |   |
| AD_PAUSE | Fired when an ad is paused. | ✅ |   |
| AD_RESUME | Fired when an ad is resumed. | ✅ |   |
| AD_BREAK_ENDED | Fired when an ad period in a stream ends | ✅ |   |

## Adding Ad Parameters

Custom ad parameters can be added to the player using the method `addAdParams()`. Multiple calls can be made to this method and each parameter will be added to the call. This method takes a key and a value and adds them to the call as a JSON blob, formatted like this:

```
{ “adParams” :
    {
        “key1”: “value1”,
        “key2”: “value2”
    }
}
```

## Customizing Ads for Each Video

Changing the ad experience for each video can be accomplished by changing the configuration settings prior to playing. This can be done by either creating a new `ArcXPVideoConfig` object and passing it into the player or by using the setter methods associated directly with the player. If the ad configuration is not changed between videos it will retain the settings from the previous video until the player is torn down.

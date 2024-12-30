

# Tracking Errors

Errors are tracked through events that are returned by the media player callback `trackErrors()`. This method can be implemented as follows:

```

mediaPlayer.trackErrors(object: ArcVideoSDKErrorListener {
    override fun onError(errorType: ArcVideoSDKErrorType, message: String, value:
        Any?) {
            
    }
})
```

Errors are also returned through the `onError()` callback method in the media client API calls:

```

mediaClient.findByUuid(uuid, object: ArcVideoStreamCallback {
    override fun onVideoStream(videos: List<ArcVideoStream>?) {
        //Do something with the videos
    }
    
    override fun onError(type: ArcVideoSDKErrorType, message:
                        String, value: Any?) {
        //Handle errors
    }
});
```

The `ArcVideoSDKErrorType` enumeration has the following possible values:

| Error | Description |
| --- | --- |
| `EXOPLAYER_ERROR` | Sent when there are general Exoplayer playback or setup errors. The value object returned will either be the exception if one was thrown or the video object that caused the error. |
| `SOURCE_ERROR` | This error is sent when Exoplayer throws an exception because of a problem with the video source. |
| `RENDERER_ERROR` | This error is sent when Exoplayer throws an exception because of a problem with the video source. |
| `INIT_ERROR` | This error is not reported through the callback but rather by throwing an `ArcException`. This exception is thrown when there is a violation of the rules for initializing the player. |
| `SERVER_ERROR` | This error is reported through an `ArcException` and only for the media client. It is thrown when the Arc server returns a response other than 200 when making a call to `findByUuid()` or `findByUuids()`. |
| `VIDEO_STREAM_DATA_ERROR` | This error is reported when server side ads are enabled and there is an error retrieving the manifest URL from the server. |

## Exceptions

The SDK will throw an `ArcException` when problems arise. It is recommended that all methods that make calls into the SDK catch these exceptions. They are thrown for the following reasons:

* When a call to `initMedia()` is made without making a call to `initMediaPlayer()`.
* When calling `initMedia(List of ArcVideoStream, List of AdUrls)` and there are more `ArcVideoStream` objects than `AdUrl` strings or stream list is null
* When a call to `addVideo()` is made without making a call to `initMediaPlayer()`.
* When a call to `addVideo()` is made without making a call to `initMedia()`.
* When calling `findByUuid()` is made without making a call to `ArcMediaClient.initialize()`.
* When calling `findByUuids()` is made without making a call to `ArcMediaClient.initialize()`

## Common Problems

### Fullscreen does not take up the full screen when activated

The `ArcVideoFrame` is expanded to fullscreen by setting the `layout_width` and `layout_height` to `match_parent`. If the `ArcVideoFrame` is the child of a view that is either not the root view or does not encompass the entire screen the `ArcVideoFrame` will expand to only fill up this view. There are two solutions:

1. In the `ArcXPVideoConfig` set call the method `useDialogForFullscreen(true)`. This will use a fullscreen dialog rather than expanding the width and height.
2. Make the `ArcVideoFrame` the child of a view that encompasses the whole screen. This may not be possible for some layouts, which is why option 1 is offered.

### The CC button is active but CC is not displaying

It is possible that the video has a closed caption track but the track contains no information. In this situation, the CC button will be visible. This is a problem that must be solved within the video and is not a problem with the SDK.

### When the video reduces to picture-in-picture (PIP), the toolbar and other visual items are still shown

The PIP functionality takes the screen and reduces it to the PIP window, including everything that is shown within it. The items that are not the `ArcVideoFrame` must be hidden when this reduction is done. Pass a list of these views into the `ArcXPVideoConfig` using `setViewsToHide()`. These views will be set to `GONE` when the PIP window is activated. They will be set to `VISIBLE` when the PIP window is expanded to full size.

### Ad events appear to not be firing

All events are held in a single class `TrackingType`. This means you can listen for any event from any callback method. Make sure you are listening for ad events in `onAdTrackingEvents()` and not `onVideoTrackingEvents()` or one of the other callback methods.

### When a track is played that does not have closed captioning and the CC button is removed, the controls adjust to the left

When the CC button is removed from the control bar, its visibility is set to **GONE**. This causes the space to be reclaimed by any buttons to the right of it. If this is not desirable the `ArcXPVideoConfig` method `setKeepControlsSpaceOnHide(true)` can be set. This will cause the CC button visibility to be set to **INVISIBLE** instead and the other buttons will not adjust.

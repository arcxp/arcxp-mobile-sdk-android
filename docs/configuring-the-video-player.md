# Configuring the Video Player

Whether you're configuring the player via a configuration file or configuring it directly at the point of implementation, there are many methods available.

| Method | Values | Description | Notes |
| --- | --- | --- | --- |
| `setActivity(Activity a)` |   | Sets the parent activity for the player | Required |
| `setVideoFrame(ArcVideoFrame frame)` |   | Sets the ArcVideoFrame object for the player. | Required |
| `setAutoStartPlay(boolean start)` | Default is true. | Determines if the video automatically starts playing. |   |
| `setStartMuted(boolean mute)` | Default is false. | Start the video muted. |   |
| `setPreferredStreamType(PreferredStreamType type)` | HLS<br />TS<br />MP4<br />GIF<br />GIFMP4 | Sets the preferred stream type to play when there are multiple types returned in an `ArcVideoStream` object. | An explanation of how this logic works is below. |
| `setMaxBitRate(int rate)` |   | The maximum bit rate of the video to play when there are multiple streams to choose from. This is used in conjunction with the `setPreferredStreamType()` method above. | See below. |
| `showClosedCaption(boolean show)` |   | Show or hide the closed caption button on the player control bar. |   |
| `showProgressBar(boolean show)` |   | Show or hide the player video progress bar including the countdown text on the player control bar. |   |
| `showCountdown(boolean show)` |   | Show or hide the countdown text on the video progress bar on the player control bar. |   |
| `showSeekButton(boolean show)` |   | Show or hide the rewind and fast forward buttons on the player control bar. |   |
| `setAutoShowControls(boolean show)` |   | Determines if the player controls show automatically when playback ends. |   |
| `setControlsShowTimeoutMs(int ms)` |   | Set the amount of time the player controls are shown before disappearing. Setting this to 0 or lower will cause the controls to always be displayed. |   |
| `setDisplayControlsToggleWithTouch(boolean set)` |   | Disable showing the player controls when the user touches the screen. This will require the user to handle video player touches themselves. |   |
| `addOverlay(String tag, View view)` |   | See more details on overlays below. |   |
| `useDialogForFullscreen(boolean use)` | Default is false. | See section on fullscreen below. |   |
| `setCastManager(arcCastManager: ArcCastManager)` |   | Sets the cast manager for enabling Chromecast. See the Chromecast page for more information. |   |
| `enableLogging(boolean enable)` |   | Turn on enhanced logging that will show up in the `logcat` log. |   |
| `setHideControlsDuringAds` | Default is false | Determines if player controls show during ads |   |
| `setDisableControls` | Default is false | Disable playback controls.  They will not appear at any time. |   |

## Ad Configs

| Method | Values | Description | Notes |
| --- | --- | --- | --- |
| `setAdsEnabled(boolean enable)` | Default is false. | This flag indicates if ads should not be shown as part of the video content. | Google IMA only |
| `setFocusSkipButton(boolean focus)` |   | Place focus on the skip button when it is shown during skippable ads. | Google IMA only |
| `setServerSideAdsEnabled(boolean set)` |   | Make the server call to enable server side ads. Will only work with **ArcVideoStream** objects that have server-side ads enabled. | Live only |
| `setClientSideAdsEnabled(boolean set)` |   | Enable client side ad reporting when viewing mid-roll live stream ads. | Live only |
| `addAdParam(String key, String value)` | JSON - `{ “adParams” : {“key1”: “value1”,` `“key2”: “value2”` `}` `}` | Add a single ad parameter to be sent to Arc's server-side ad insertion system. Multiple calls to this method can be made. | Live only |
| `setUserAgent(String userAgentString)` |   | Sets the user agent string that is passed to the server when enabling client and server side ads. | Live only |

## Closed Captioning Settings

| Method | Values | Description | Notes |
| --- | --- | --- | --- |
| `setCcStartMode(CCStartMode mode)` | ON<br />OFF<br />DEFAULT | Controls the behavior of closed captioning behavior when the player starts.<br />\* ON - Closed Captioning is on by default but can be turned off.<br />\* OFF - Closed Captioning is off by default but can be turned on.<br />\* DEFAULT - Closed Captioning follows the Captioning Service setting of the device. | Works for Android 19 and above only. |
| `setShowClosedCaptionTrackSelection(boolean show)` |   | If true then the track selection dialog will be shown that allows the user to select the closed captioning track. If false then the CC button will toggle between off and the default CC track. |   |

## Picture-in-Picture Settings

| Method | Values | Description | Notes |
| --- | --- | --- | --- |
| `enablePip(boolean enable)` | Default is false. | Enable picture-in-picture (PIP). If it is on then the video controls will have a pip menu button. PIP will occur when the button is pressed or the user presses the back button while a video is playing. |   |
| `setViewsToHide(View ….v)` |   | This is a variable size list of views that will be hidden when PIP occurs. All views that are visible on the screen with the exception of the ArcVideoFrame should be listed here. The views in this list will be set to GONE when PIP occurs so that only the video frame is showing. |   |

## Using the `ArcXPVideoConfig` Object

To configure your player using the **ArcXPVideoConfig** object, create it using the **ArcXPVideoConfig** builder:

```kotlin
val mediaPlayer = MediaPlayer.createPlayer(this)
val config = ArcXPVideoConfig.Builder()
	.setActivity(this)          
	.setVideoFrame(arcVideoFrame)  
	.enablePip(true)       
	.setViewsToHide(view1, view2, view3 ….) 
	...
	.build()
	
mediaPlayer.configureMediaPlayer(config)
```

It is best to use this method when multiple media players are needed with the same configuration.

Alternatively, the media player can be configured directly.

```kotlin
mediaPlayer.setActivity(this)
		   .setVideoFrame(arcVideoFrame)
		   .enablePip(true)
		   .setViewsToHide(view1, view2, view3 ….)
```

All of the configuration methods return the media player object, so they can be chained together as shown above.

## Setting Your Stream Type and Bitrate

An **ArcVideoStreamObject** will typically return multiple streams that each have a different video type and bitrate. Your app will choose a stream based on the preferred stream type and a maximum bit rate you configure.

Preferred streams have a hierarchy by default:

<Steps>
1.  HLS
2.  TS
3.  MP4
4.  GIF
5.  GIF-MP4
</Steps>

The algorithm to choose the correct stream will loop through all available streams of the preferred type. Of those it will find the one that does not exceed the given max bitrate. If a stream of the preferred type does not exist then it will go to the next preferred stream type and repeat the search, working its way down until it finds a stream that does not exceed the bit rate. For example, if the preferred type is set to TS and there are no TS streams in the object, then it will repeat the search using MP4, then GIF, then GIF-MP4.

## Adding Overlays

One or more views can be added on top of the video view using the configuration option **addOverlay()**. This method takes a tag name for the view as well as the view. The client app will still have a pointer to the overlays so they can then control the visibility or other parameters of the views from within the client code. This method can can be called multiple times in order to add multiple overlays.

The views can be retrieved and removed through the media player as follows:

*   `mediaPlayer.getOverlay(tag)`
*   `mediaPlayer.removeOverlay(tag)`

## Using Fullscreen

Fullscreen can be displayed in two different ways: as a dialog, or by expanding the `layout_height` and `layout_width` parameters of the `ArcVideoFrame` to `match_parent`. To determine if the dialog should be used, use the `useDialogForFullscreen(boolean use)` method. It defaults to false.

If your layout has the `ArcVideoFrame` element as a direct child of the root view, then setting this to false will work because the frame will expand to the full size of the screen.

If your layout has the `ArcVideoFrame` as a child of a view that is not the root view, and that parent is not the full size of the screen, then you should use a dialog; expanding the height and width of the video frame will not take up the whole screen.

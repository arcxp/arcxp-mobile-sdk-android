# Video Module

The Video module of the Arc Mobile SDK offers an Android SDK for use on mobile devices and smart TVs running Android TV or Fire TV software. The SDK supports livestreams and VODs hosted in Video Center, and offers both preroll ads using Google DFP / Google Ad Manager as well as mid-roll livestream ads using Arc’s DAI features.

## Prerequisites

[**Initialize the Arc XP Mobile SDK in your app**](getting-started-initialization.md)

- Android 5.1+/SDK Version 22
- Android Studio V3.0 or higher

## Getting Started

Implementing video playback with the Video module will require interacting with three classes, `ArcVideoFrame`, `ArcMediaClient` and `ArcMediaPlayer`.

The `ArcVideoFrame` is the rendering window for the video. It is added to the layout and will contain the player.

The `ArcMediaClient` is the API that is used to retrieve video information from the Arc servers.

The `ArcMediaPlayer` is the controlling class for the video playback. It will be the programmatic interface for all playback functionality. It is used to control playback as well as receive the callbacks to report information about the playback.

## Video Renderer

The video player is rendered inside of the custom class `ArcVideoFrame`. The instance of this class needs to be inserted into the layout xml file of the activity or fragment that will contain the video player.

```xml
<RelativeLayout
    android:layout_width=match_parent"
    android:layout_height="match_parent">
    …….
    <ArcVideoFrame
        android:id="@+id/arcVideoFrame"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
    ……
</RelativeLayout>
```

An instance of this object will need to be retrieved inside of the method that retrieves your view objects (normally `onCreate()` or `onCreateView()`).

```kotlin
override fun onCreate(savedInstanceState: Bundle) {
    setContentView(R.layout.my_activity);
    arcVideoFrame = findViewById(R.id.arcVideoFrame)
}
```

The `ArcVideoFrame` object should ideally be a direct child of the root view, but it is not a requirement. More about this will be discussed in the section on fullscreen.

## Media Player

The media playback is controlled by the class `ArcMediaPlayer`.

There are two methods that instantiate `ArcMediaPlayer` objects:

- `ArcMediaPlayer.instantiate(Context context)` - This static method returns a singleton instance of the media player. It can be called anywhere in the code and will return the same object instance. This can be used when there is only need for a single media player object.
- `ArcMediaPlayer.createPlayer(Context context)` - This static method returns a new `ArcMediaPlayer` object. It can be used when there is a need for multiple media players within an app such as having multiple media players on a single activity or fragment.

## Configuring the media player

There are two ways to configure the media player:

1. Create an `ArcXPVideoConfig` object that holds the configuration for the media player. This can be done through
2. Configure the media player directly.

Both of these have the same methods available. The configuration object method (option 1) is used to set up large amounts of items during initialization. The direct method (option 2) works better when making changes during the course of video playback.

For a full list of available methods to customize the player, [Click Here](configuring-the-video-player.md).

## Initialize the video

The media player can be initialized with a video or list of videos (playlist) by passing in an `ArcVideoStream` object or a list of `ArcVideoStream` objects. An `ArcVideoStream` object is returned using the `ArcMediaClient.findByUuid()` or `ArcMediaClient.findByUuids()` method. These are discussed below, in the Media Client section.

The media player needs to be initialized with the object to be played before the play command is executed. You can initialize a single `ArcVideoStream` or a list of streams, which will then be played in order as a playlist.

- `mediaPlayer.initMedia(ArcVideoStream stream)` OR
- `mediaPlayer.initMedia(List\<ArcVideoStream\> streams)`

You can also initialize the stream or streams with an `adUrl` string (or list of strings).

- `mediaPlayer.initMedia(ArcVideoStream stream, String adUrl)`
- `mediaPlayer.initMedia(List\<ArcVideoStream\> streams, List\<String\> adUrls)`

In this case, the ad tag URL specified in the configuration will be ignored and the ad URL passed in will be used. This allows for using a different ad URL for each video in a playlist, among other use cases.

_**PLEASE NOTE:**_ _If the length of the video stream list is greater than the length of the ad URL list, then the_ _`initMedia`_ _call will return without doing anything and an error will be returned in the error listener._

_If the_ _`initMedia()`_ _method is called on an_ _`ArcMediaPlayer`_ _object before it has been initialized with the_ _`initMediaPlayer()`_ _method, an_ _`ArcException`_ _will be thrown._

## Display the video

To render the media player inside of the `ArcVideoFrame` call:

`mediaPlayer.displayVideo();`

If `setAutoStartPlay()` is set to false, then the video will be displayed but not started. Otherwise the video will start playing.

## Finishing up

When exiting the page that contains the `ArcMediaPlayer` it is recommended that `ArcMediaPlayer.finish()` be called. This will release video objects and avoid memory leaks.

The media player has implementation of the standard activity lifecycle functions that need to be called:

```kotlin
override public fun onStop() {
    super.onStop()
    mediaPlayer.onStop()
}
```

This should be done for `onStop()`, `onPause()`, `onResume` and `onDestroy()`.

## Controlling playback and initialization

The ArcMediaPlayer class has methods for configuration, which have already been described, controlling playback, and initialization. Here are the additional methods:

| Method | Values | Description | Notes |
| --- | --- | --- | --- |
| _addVideo(video: ArcVideoStream, adUrl: String)_ |   | These methods are similar to the `initMedia()` methods, except that they add an additional video to be played at the end of the video list that has already been passed in with `initMedia()`. | _adUrl_ is optional. |
| _trackMediaEvents(listener: ArcVideoEventsListener)_ |   | Used to track analytics events. See the page about Setting Up Analytics. |   |
| _trackErrors(listener: ArcVideoSDKErrorListener)_ |   | Used to listen for errors. See the Tracking Errors page. |   |
| _onBackPressed()_ |   | This method must be called when `onBackPressed()` is called for the parent activity. |   |
| stopPip() |   | Programmatically stop picture-in-picture if it is playing. |   |
| showControls() |   | Show the controls bar. |   |
| hideControls() |   | Hide the controls bar. |   |
| isControlsVisible() |   | Returns if the controls bar is visible. |   |
| setFullscreen(full: boolean) |   | Turn fullscreen on or off programmatically. |   |
| isFullScreen() |   | Returns if the player is currently in fullscreen mode. |   |
| setFullscreenKeyListener(listener: ArcKeyListener) |   | Listen for key presses on the fullscreen player. |   |
| setPlayerKeyListener(listener: ArcKeyListener) |   | Used to listen for key presses on the player. |   |
| stop() |   | Stop playback. |   |
| start() |   | Start playback. |   |
| pause() |   | Pause playback. |   |
| resume() |   | Resume playback. |   |
| seekTo(ms: Integer) |   | Seek to the specified time, in milliseconds. |   |
| setVolume(volume: Float) |   | Set the volume of the currently playing video. |   |
| toggleClosedCaption(show: Boolean) |   | Toggle closed captions on and off. |   |
| getPlaybackState() |   | Returns the current playback state. |   |
| getOverlay(tag: String) |   | Returns the overlay view with the specified tag. |   |
| onDestroy() |   | Cleans up Chromecast. Call from onDestroy() in the activity. |   |
| getSdkVersion() |   | Returns the current version of the SDK. |   |

## Using the Media Client

The `ArcMediaClient` class allows access to Video Center to retrieve video objects for the media player. It has the following calls:

- `findByUuid(uuid: String, listener: ArcVideoStreamCallback)` - Passed a single UUID this method returns a list containing a single ArcVideoStream object for the respective UUID. If the UUID cannot be found and there is no error an empty list will be returned.
- `findByUuids(vararg uuids: String, listener: ArcVideoStreamCallback)` - Passed multiple UUIDs this method returns a list multiple ArcVideoStream objects for the respective UUIDs. If a UUID cannot be found and there is no error the UUID will not be in the list.
- `findByUuids(listener: ArcVideoStreamCallback, vararg uuids: String)` - A Kotlin friendly version of the findByUuids method.
- `findByPlaylist(name: String, count: Int, listener: ArcVideoPlaylistCallback)` - Passed a playlist name and number of videos to return this method returns an ArcVideoPlaylist object which contains the list of ArcVideoStream objects.

## Instantiating the media client

The `ArcMediaClient` class can be instantiated either as a singleton or as multiple objects each with their organization name. The organization name is a combination of the client and server environment, such as _xyz-prod_ or _xyz-sandbox_.

`Note:` Prior to version 1.1 the organization name string is the full base URL such as _https://companyxyz-prod.api.cdn.arcpublishing.com_ and the methods do not accept the server environment.

```kotlin
class MyFragment : Fragment() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ArcMediaClient.instantiate("orgName")
    }
    
    fun doSomething() {
        var mediaClient = ArcMediaClient.getInstance()
        mediaClient.findByUuid(.....)
    }
}
```

Or creating multiple instances.

```kotlin
class MyFragment: Fragment() {
    
    var client1 : ArcMediaClient? = null
    var client2: ArcMediaClient? = null
    
    fun connect() {
        client1 = ArcMediaClient.createClient("xyz-sandbox")
        client2 = ArcMediaClient.createClient("xyz-prod") 
    }
    
    fun useClient1() {
        client1.findByUuid(.....)
    }
    
    fun useClient2() {
        client2.findByUuid(....)
    }
}
```

## Using Geographic Restrictions (video sdk v1.6.2 or below, skip to next section for single sdk)

Starting in video SDK Version 1.1, it is possible to take advantage of Video Center’s Geographic Restrictions features. This will allow you to restrict the availability of videos based on the location of the user. To enable this functionality, the `ArcMediaClient` object must be created with a `true` boolean flag as the final argument.

ArcMediaClient.Instantiate(orgName, true)

or

client1 = ArcMediaClient.createClient(orgName, true)

## Using Geographic Restrictions (video sdk 1.7.0, single sdk in all versions)

Starting in (video sdk 1.7.0, single sdk in all versions) Video Center’s Geographic Restrictions features are calculated by the Video Center server. The client code will not provide a flag to turn the capability on so the final boolean parameter shown in the previous section will not be added.

## Making API Calls

Data is returned from the API calls using the `ArcVideoStreamCallback` object:

```kotlin
ArcMediaClient.createClient("xyz-prod") 
```

```kotlin
ArcMediaClient.getInstance().findByUuid(uuid, 
	object: ArcVideoStreamCallback {
		override fun onVideoStream(videos: List<ArcVideoStream>?) {
			//single stream returned so do something with videos.get(0)
			//used if checkGeoRestriction flag = false or is omitted
		}
		
		override fun onVideoResponse(videos: ArcVideoResponse) {
		    //returns result of a geo restricted call.  This method does not exist prior to (video SDK version 1.0.2, single sdk in all versions)
		    //used if checkGeoRestriction flag = true
		}
```

```kotlin
override fun onError(type: ArcVideoSDKErrorType, message: 
String, value: Any?) {
		}
	}); 
```

The results will be returned through the `onVideoResponse` method, and the results will be an `ArcVideoResponse` object. The `ArcVideoResponse` object has the following signature:

```kotlin
data class ArcVideoResponse(
    val arcTypeResponse: ArcTypeResponse?,
    val arcVideoStreams: List<ArcVideoStream>? = null
) 
```

```kotlin
data class ArcTypeResponse(
    val type: String,
    val allow: Boolean,
    val params: TypeParams,
    val computedLocation: ComputedLocation
)
```

```kotlin
data class TypeParams(
    val country: String,
    val zip: String,
    val dma: String
)
```

```kotlin
data class ComputedLocation(
    val country: String,
    val zip: String,
    val dma: String
)
```

If the video is restricted within the users geographic region then the `ArcTypeResponse` object will be populated. If the the video is not georestricted then the `ArcTypeResponse` will be null and the `arcVideoStreams` object will contain `ArcVideoStream` objects.

## Turning on OM and PAL

(version 1.3.3 and later of video sdk or all versions of single sdk) support the Open Measurement standard and Google PAL for live streams using server-side ad insertion.

It is not required that you use both PAL and OM but if PAL is implemented OM must be included.

OM and PAL are turned off by default in the SDK. They can be turned on through the `ArcXPVideoConfig` object.

```kotlin
val mediaPlayer = MediaPlayer.createPlayer(this)
val config = ArcXPVideoConfig.Builder()
	.....        
	.enableOpenMeasurement(true)
	.enablePAL(true)
	...
	.build()
	
mediaPlayer.configureMediaPlayer(config)
```

## Virtual Channels (Beta)

Virtual channels are currently a beta feature that is available in SDK (video sdk versions higher than 1.3.3, single sdk in all versions)

To return a virtual channel stream the flag shouldUseVirtualChannel has been added to the findByUuid() method. The change to the code is:

```kotlin
ArcMediaClient.createClient("xyz-prod", true)
```

```kotlin
ArcMediaClient.getInstance().findByUuid(uuid, 
	object: ArcVideoStreamCallback {
		override fun onVideoStream(videos: List<ArcVideoStream>?) {
			//single stream returned so do something with videos.get(0)
			//used if checkGeoRestriction flag = false or is omitted
		}
		
		override fun onVideoResponse(videos: ArcVideoResponse) {
		    //returns result of a geo restricted call.  This method does not exist prior to (video SDK version 1.0.2, single sdk in all versions)
		    //used if checkGeoRestriction flag = true
		}
```

```kotlin
override fun onError(type: ArcVideoSDKErrorType, message: String, value: Any?) { } override onVideoStreamVirtual(video: ArcVideoStreamVirtualChannel) { //returns result of a call to a virtual channel } }, shouldUseVirtualChannel = true);
```

The client code must implement the onVideoStreamVirtual() method. The ArcVideoStreamVirtualChannel object is defined as:

```kotlin
data class ArcVideoStreamVirtualChannel(val url: String, val adSettings: AdSettings) {
data class AdSettings(
        val enabled: Boolean,
        val url: String?
    )
}
```

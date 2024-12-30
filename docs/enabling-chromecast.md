# Enabling Chromecast

The Arc Mobile SDK for Android supports Chromecast integration for your app.

## Step 1: Create an ArcCastManager object

The Video Module provides Chromecast integration through the **ArcCastManager** class. This class must be instantiated.

```kotlin

public class MyActivity: AppCompatActivity() {
    …


    private val arcCastManager: ArcCastManager ?= null

    override fun onCreate(savedInstanceState: Bundle) {
            …
            arcCastManager = ArcCastManager(this)
            arcCastManager.setSessionManagerListener(this)
            …
    }
    …
}
```

## Step 2: Add the cast icon to the options menu

```kotlin

override fun onCreateOptionsMenu(menu: Menu) : boolean {
    menuInflater.inflate(R.menu.player, menu)
    arcCastManager.addMenuCastButton(menu, R.id.media_route_menu_item)
    return true
}
```

This is the menu file:

```xml

<?xml version="1.0″ encoding="utf-8″?>
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto" >
    <item
        android:id="@+id/media_route_menu_item"
        android:title="Cast On"
        app:actionProviderClass="com.arc.arcvideo.cast.ArcMediaRouteActionProvider"
        android:orderInCategory="101″
        app:showAsAction="always" />
</menu>
```

## Step 3: Send the ArcCastManager to the media player

In order to enable Chromecast in your player instance, the **ArcCastManager** object created in step 1 needs to be sent to the media player, either by using the **setCastManager** method or through the configs.

Using the method:

```kotlin
    mediaPlayer.setCastManager(arcCastManager)
```

Via config:

```kotlin
var config = ArcXPVideoConfig.Builder()
…
.setCastManager(arcCastManager)
...
```

## Step 4: Cast the video

Casting will be handled automatically by the SDK. The user will just need to touch the cast button and pick a device.

### (Optional) Implement ArcCastSessionManagerListener

The activity can optionally listen for events coming back from the casting session. To do this, it must implement **ArcCastSessionManagerListener**.

```kotlin
public class MyActivity: AppCompatActivity(), ArcCastSessionManagerListener {
    fun onSessionEnded(error: Int) {}
    fun onSessionResumed(wasSuspended: Boolean) {}
    fun onSessionStarted(sessionId: String) {}
    fun onSessionStarting() {}
    fun onSessionStartFailed(error: Int) {}
    fun onSessionEnding() {}
    fun onSessionResuming(sessionId: String) {}
    fun onSessionResumeFailed(error: Int) {}
    fun onSessionSuspended(reason: Int) {}
}
```

## Customizations

The image associated with the Chromecast session can be set in the `ArcXPVideoConfig` object. this will show as a background art in the cast player window, and show on dialog when you press the Chromecast button, and will show on the notification window.

```kotlin
var config = ArcXPVideoConfig.Builder()
    …
    .setArtworkUrl(URL to image)
...
```

# Frequently Asked Questions


<details>
<summary>Do I have to use the Subscriptions Module in the SDK if I just want to show content or video?</summary>
<br />
<i>No. Both Content and Subscriptions are optional features of the Mobile SDK. If you wish to implement the Arc XP Paywall or Login the the Subscriptions Module will be required. If you are using your own Paywall and Login implementation then the Subscriptions Module is not required.</i>
</details>
<details>
<summary>How do I setup third-party login options?</summary>
<br />
<i>You must create a developer account on Google, Facebook or Apple and generate a security key that you will load into the Subscriptions SDK Module on initialization. </i>

[Documentation for Social Account Setup](https://dev.arcxp.com/subscriptions/identity/configure/third-party-authentication-providers-facebook-google-apple)
</details>
<details>
<summary>Does the SDK handle content elements that are not listed?</summary>
<br />
<i>Yes. If the content type you are trying to use is not supported by one of the explicit get methods, you can use the getContentSuspend call that will return a ArcXPContentElement object.</i>

</details>
<details>
<summary>Is Picture-in-Picture mode available in video player?</summary>
<br />
<i>Yes! Implementing the Video SDK will allow you to enable the Picture-in-Picture mode in the Builder class when creating the ArcXPVideoConfig object.</i>

```kotlin
val mediaPlayer = MediaPlayer.createPlayer(this)
val config = ArcMediaConfig.Builder()
	.setActivity(this)          
	.setVideoFrame(arcVideoFrame)  
	.enablePip(true)       
	.setViewsToHide(view1, view2, view3 ….) 
	...
	.build()
	
mediaPlayer.configureMediaPlayer(config)
```

[More information regarding Video configuration](/docs/configuring-the-video-player.md)

</details>
<details>
<summary>How do I add the SDK into my Android Studio app?</summary>
<br />
<i>Inside your build.gradle(:app) file, add the following dependencies (use latest version): </i>

```
implementation 'arcxp-sdk-android:arcxp-mobile-sdk-android:1.3.1'
```

</details>
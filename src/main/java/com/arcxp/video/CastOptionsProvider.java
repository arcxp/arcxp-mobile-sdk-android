package com.arcxp.video;

import android.content.Context;

import androidx.media3.common.util.UnstableApi;

import com.google.android.gms.cast.LaunchOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.cast.framework.media.CastMediaOptions;
import com.google.android.gms.cast.framework.media.ImageHints;
import com.google.android.gms.cast.framework.media.ImagePicker;
import com.google.android.gms.cast.framework.media.MediaIntentReceiver;
import com.google.android.gms.cast.framework.media.NotificationOptions;
import com.google.android.gms.common.images.WebImage;

import java.util.Arrays;
import java.util.List;

/**
 * @hide
 */
@UnstableApi
public class CastOptionsProvider implements OptionsProvider {

    /**
     * App id that points to the Default Media Receiver app with basic DRM support.
     *
     * <p>Applications that require more complex DRM authentication should <a
     * href="https://developers.google.com/cast/docs/web_receiver/streaming_protocols#drm">create a
     * custom receiver application</a>.
     */
    public static final String APP_ID_DEFAULT_RECEIVER_WITH_DRM = androidx.media3.cast.DefaultCastOptionsProvider.APP_ID_DEFAULT_RECEIVER_WITH_DRM;
    //TODO we may want to allow client to pass this in if needed

    @Override
    public CastOptions getCastOptions(Context context) {
        NotificationOptions notificationOptions = new NotificationOptions.Builder()
                .setActions(Arrays.asList(MediaIntentReceiver.ACTION_REWIND,
                        MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK,
                        MediaIntentReceiver.ACTION_FORWARD,
                        MediaIntentReceiver.ACTION_STOP_CASTING), new int[]{1, 2})
//                .setTargetActivityClassName("com.example.myapplication.MainActivity2")
                //TODO so if we can access the calling activity name somehow, if app is not in focus and you click on it in notification, this will bring you back to app
                // without it, it will do nothing upon clicking notification.
                // Uncommenting above will demonstrate in demo video player, but probably we should not enable unless we can provide this functionality to customers.
                // this seems to run once at beginning of demo app (from manifest declaration), unsure how to pass an activity name at this point.  Also client may have several activities.
                // perhaps this could be done by client, but would require several dependencies and may be confusing, unsure how to solve exactly.


                .build();
        CastMediaOptions mediaOptions = new CastMediaOptions.Builder()
                .setImagePicker(new ImagePickerImpl())
                .setNotificationOptions(notificationOptions)
//                .setExpandedControllerActivityClassName(MainActivity.class.getName())
                .build();
        /** Following lines enable Cast Connect */
        LaunchOptions launchOptions = new LaunchOptions.Builder()
                .setAndroidReceiverCompatible(true)
                .build();
        return new CastOptions.Builder()
                .setLaunchOptions(launchOptions)
                .setReceiverApplicationId(APP_ID_DEFAULT_RECEIVER_WITH_DRM)
                .setCastMediaOptions(mediaOptions)
                .build();
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context appContext) {
        return null;
    }

    private static class ImagePickerImpl extends ImagePicker {

        @Override
        public WebImage onPickImage(MediaMetadata mediaMetadata, ImageHints hints) {
            int type = hints.getType();
            if ((mediaMetadata == null) || !mediaMetadata.hasImages()) {
                return null;
            }
            List<WebImage> images = mediaMetadata.getImages();
            if (images.size() == 1) {
                return images.get(0);
            } else {
                if (type == ImagePicker.IMAGE_TYPE_MEDIA_ROUTE_CONTROLLER_DIALOG_BACKGROUND) {
                    return images.get(0);
                } else {
                    return images.get(1);
                }
            }
        }
    }
}

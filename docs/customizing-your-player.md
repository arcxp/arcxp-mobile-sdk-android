# Customizing Video Player

The video player can be used with the default control icons, or the icons can be replaced with your own custom versions.

To replace an existing player control, you must specify a new drawable element. This can be done in any of the .xml files, but we recommend you either use `values.xml` or create a new file called `drawables.xml`. The entry in this file will look like this:

```xml
<drawable name="PlayDrawableButton">@drawable/my_play_button</drawable>
```

In this example, `my_play_button` specifies the drawable replacing the existing play button. The following images can be replaced using the specified name:

| Button | Name |
| --- | --- |
| **Play button** | PlayDrawableButton |
| **Pause button** | PauseDrawableButton |
| **Mute button** | MuteDrawableButton |
| **Unmute button** | MuteOffDrawableButton |
| **Share button** | ShareDrawableButton |
| **Closed caption button** | CcDrawableButton |
| **Closed caption button disable** | CcOffDrawableButton |
| **PIP button** | PipDrawableButton |
| **Full screen button** | FullScreenDrawableButton |
| **Full screen button - collapse to normal** | FullScreenDrawableButtonCollapse |

It is also possible to change the color on all player icons. This can be done by specifying the following color attribute:

```xml
<color name="control_button_tint">@color/my_new_tint_color</color>
```

Additionally `title_text`, `ff_rwd_text_color` can be set in same manner.

The video player progress bar can also be customized. The following colors can be specified in the same manner:

* TimeBarPlayedColor
* TimeBarBufferedColor
* TimeBarUnplayedColor
* TimeBarScrubberColor
* AdMarkerColor
* AdPlayedMarkerColor
* timeline_text_duration
* timeline_text_separator_color
* timeline_text_position

## Customizing Full Screen Videos

To customize the style of the fullscreen video, customize the following style:

```xml
<style name="Fullscreen" parent="android:Theme.Black.NoTitleBar.Fullscreen">
    <item name="android:windowFullscreen">true</item>
    <item name="android:windowContentOverlay">@null</item>
    <item name="android:paddingLeft">10dp</item>
    <item name="android:paddingRight">10dp</item>
</style>
```

## Advanced XML Customization

Full Customization of the control layout is possible, though requires overriding an included XML layout(`exo_styled_player_control_view.xml`) and including it in your res/layout folder of your application. The following is the default included with Video SDK. You should keep the given IDs with their given view types, the SDK may not function correctly if you do not.

```xml
<?xml version="1.0" encoding="utf-8"?>
<merge
    xmlns:android="http://schemas.android.com/apk/res/android">
    <View android:id="@id/exo_controls_background"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:background="@color/exo_black_opacity_60"/>

    <FrameLayout android:id="@id/exo_bottom_bar"
        android:layout_width="match_parent"
        android:layout_height="@dimen/exo_styled_bottom_bar_height"
        android:layout_marginTop="@dimen/exo_styled_bottom_bar_margin_top"
        android:layout_gravity="bottom"
        android:background="@color/exo_bottom_bar_background"
        android:layoutDirection="ltr">

        <LinearLayout android:id="@id/exo_time"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:paddingStart="@dimen/exo_styled_bottom_bar_time_padding"
            android:paddingEnd="@dimen/exo_styled_bottom_bar_time_padding"
            android:paddingLeft="@dimen/exo_styled_bottom_bar_time_padding"
            android:paddingRight="@dimen/exo_styled_bottom_bar_time_padding"
            android:layout_gravity="center_vertical|start"
            android:layoutDirection="ltr">

            <TextView android:id="@id/exo_position"
                style="@style/ExoStyledControls.TimeText.Position"/>

            <TextView
                android:id="@+id/separator"
                style="@style/ExoStyledControls.TimeText.Separator"/>

            <TextView android:id="@id/exo_duration"
                style="@style/ExoStyledControls.TimeText.Duration"/>

        </LinearLayout>

        <LinearLayout android:id="@id/exo_basic_controls"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical|end"
            android:layoutDirection="ltr">

            <ImageButton android:id="@id/exo_settings"
                style="@style/ExoMediaButton.Settings"
                android:contentDescription="@string/settings_button_content_description" />

            <ImageButton
                android:id="@+id/exo_volume"
                style="@style/ExoMediaButton.Volume"
                android:contentDescription="@string/volume_button_content_description"/>

            <ImageButton android:id="@+id/exo_cc"
                style="@style/ExoMediaButton.Cc"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_gravity="center"
                android:contentDescription="@string/cc_button_content_description"/>

            <ImageButton
                android:id="@+id/exo_pip"
                style="@style/ExoMediaButton.Featured.Pip"
                android:contentDescription="@string/pip_button_content_description"/>

            <ImageButton android:id="@+id/exo_fullscreen"
                style="@style/ExoMediaButton.Featured.FullScreen"
                android:contentDescription="@string/fullscreen_button_content_description"/>

            <ImageButton
                android:id="@+id/exo_share"
                style="@style/ExoMediaButton.Featured.Share"
                android:contentDescription="@string/share_button_content_description"/>

            <ImageButton android:id="@id/exo_overflow_show"
                style="@style/ExoStyledControls.Button.Bottom.OverflowShow"
                android:contentDescription="@string/overflow_menu_button_content_description"/>


        </LinearLayout>

        <HorizontalScrollView android:id="@id/exo_extra_controls_scroll_view"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_vertical|end"
            android:visibility="invisible">

            <LinearLayout android:id="@id/exo_extra_controls"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layoutDirection="ltr">

                <ImageButton android:id="@id/exo_overflow_hide"
                    style="@style/ExoStyledControls.Button.Bottom.OverflowHide"
                    android:contentDescription="@string/overflow_menu_button_content_description"/>

            </LinearLayout>

        </HorizontalScrollView>

    </FrameLayout>

    <View android:id="@id/exo_progress_placeholder"
        android:layout_width="match_parent"
        android:layout_height="@dimen/exo_styled_progress_layout_height"
        android:layout_gravity="bottom"
        android:layout_marginBottom="@dimen/exo_styled_progress_margin_bottom"/>

    <LinearLayout android:id="@id/exo_minimal_controls"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|end"
        android:layout_marginBottom="@dimen/exo_styled_minimal_controls_margin_bottom"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:layoutDirection="ltr">

        <ImageButton android:id="@id/exo_minimal_fullscreen"
            style="@style/ExoStyledControls.Button.Bottom.FullScreen"
            android:contentDescription="@string/fullscreen_button_content_description"/>

    </LinearLayout>

    <LinearLayout
        android:id="@id/exo_center_controls"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:background="@android:color/transparent"
        android:gravity="center"
        android:padding="@dimen/exo_styled_controls_padding"
        android:clipToPadding="false">

        <ImageButton android:id="@id/exo_prev_button"
            style="@style/ExoStyledControls.Button.Center.Previous"
            android:contentDescription="@string/prev_button_content_description"/>

        <include layout="@layout/rwd_layout" />

        <ImageButton android:id="@id/exo_play_pause"
            style="@style/ExoStyledControls.Button.Center.PlayPause"
            android:contentDescription="@string/play_pause_button_content_description"/>

        <include layout="@layout/ffwd_layout" />

        <ImageButton
            android:id="@+id/exo_next_button"
            style="@style/ExoStyledControls.Button.Center.Next"
            android:contentDescription="@string/next_button_content_description"/>

    </LinearLayout>

        <TextView
            android:id="@+id/styled_controller_title_tv"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="18dp"
            android:layout_marginTop="18dp"
            android:shadowColor="#000000"
            android:shadowRadius="3"
            android:textColor="@color/title_text"
            android:textAlignment="viewStart"
            android:textSize="24sp" />

</merge>
```

package com.arcxp.video.views;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * VideoFrameLayout is a custom RelativeLayout used within the ArcXP platform.
 * It provides constructors to initialize the layout with different parameters.
 *
 * The class defines the following constructors:
 * - VideoFrameLayout(Context context): Initializes the layout with the given context.
 * - VideoFrameLayout(Context context, AttributeSet attrs): Initializes the layout with the given context and attribute set.
 * - VideoFrameLayout(Context context, AttributeSet attrs, int defStyleAttr): Initializes the layout with the given context, attribute set, and default style attribute.
 *
 * Usage:
 * - Use this custom RelativeLayout to create a video frame layout with specific attributes.
 *
 * Example:
 *
 * VideoFrameLayout videoFrameLayout = new VideoFrameLayout(context);
 *
 * Note: This class is intended for internal use only and should not be exposed publicly.
 *
 * @constructor VideoFrameLayout Initializes the layout with the given context.
 * @constructor VideoFrameLayout Initializes the layout with the given context and attribute set.
 * @constructor VideoFrameLayout Initializes the layout with the given context, attribute set, and default style attribute.
 */
public class VideoFrameLayout extends RelativeLayout {
    public VideoFrameLayout(@NonNull Context context) {
        super(context);
    }

    public VideoFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}

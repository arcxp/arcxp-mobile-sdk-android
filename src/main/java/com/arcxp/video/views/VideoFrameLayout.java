package com.arc.arcvideo.views;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * @hide
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

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (hasOnClickListeners()) {
            return true;
        }
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(event.getAction() == MotionEvent.ACTION_DOWN);
        }
        return super.onInterceptTouchEvent(event);
    }
}

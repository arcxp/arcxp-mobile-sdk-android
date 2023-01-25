package com.arcxp.video.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.arc.flagship.features.arcvideo.R

/**
 * @suppress
 */
public class ArcVideoFrame : FrameLayout {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs : AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
//        if (hasOnClickListeners()) {
//            return true
//        }
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(event.action == MotionEvent.ACTION_DOWN)
        }
        return super.onInterceptTouchEvent(event)
    }
    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (selected) {
            background = resources.getDrawable(R.drawable.selected_outline)
        } else {
            background = resources.getDrawable(android.R.color.white)
        }
    }


}
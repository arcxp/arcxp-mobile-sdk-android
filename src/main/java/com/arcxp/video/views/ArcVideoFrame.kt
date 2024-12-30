package com.arcxp.video.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.arcxp.sdk.R

/**
 * ArcVideoFrame is a custom FrameLayout used within the ArcXP platform.
 * It provides additional functionality to change the background drawable when the frame is selected.
 *
 * The class defines the following constructors:
 * - ArcVideoFrame(context: Context): Initializes the frame with the given context.
 * - ArcVideoFrame(context: Context, attrs: AttributeSet?): Initializes the frame with the given context and attribute set.
 * - ArcVideoFrame(context: Context, attrs: AttributeSet?, defStyleAttr: Int): Initializes the frame with the given context, attribute set, and default style attribute.
 *
 * The class overrides the following methods:
 * - setSelected(selected: Boolean): Changes the background drawable based on the selection state.
 *
 * Usage:
 * - Use this custom FrameLayout to provide a selectable frame with a custom background drawable.
 *
 * Example:
 *
 * val arcVideoFrame = ArcVideoFrame(context)
 * arcVideoFrame.setSelected(true)
 *
 * Note: This class is intended for internal use only and should not be exposed publicly.
 *
 * @constructor ArcVideoFrame Initializes the frame with the given context.
 * @constructor ArcVideoFrame Initializes the frame with the given context and attribute set.
 * @constructor ArcVideoFrame Initializes the frame with the given context, attribute set, and default style attribute.
 * @method setSelected Changes the background drawable based on the selection state.
 */
public class ArcVideoFrame : FrameLayout {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs : AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (selected) {
            background = resources.getDrawable(R.drawable.selected_outline)
        } else {
            background = resources.getDrawable(android.R.color.white)
        }
    }


}
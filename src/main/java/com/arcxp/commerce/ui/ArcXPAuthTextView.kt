package com.arcxp.commerce.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * Sealed class would differentiate text view and handle options
 *
 * @param context Context of TextView
 * @param attributeSet attributes of AttributeSet
 */
sealed class ArcXPAuthTextView(context: Context, attributeSet: AttributeSet? = null) : AppCompatTextView(context, attributeSet)

class SettingTextView(context: Context, attributeSet: AttributeSet? = null) : ArcXPAuthTextView(context, attributeSet)

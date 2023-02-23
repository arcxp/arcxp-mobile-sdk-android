package com.arcxp.commerce.ui

import android.content.Context
import android.content.res.TypedArray
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.arcxp.commons.util.Constants
import com.arcxp.sdk.R

/**
 * Sealed class would differentiate edit text view and handle validation
 *
 * @param context Context of EditText View
 * @param attributes attributes of AttributeSet
 */

/**
 * @suppress
 */
public sealed class ArcXPAuthEditText(context: Context, attributes: AttributeSet? = null) :
    AppCompatEditText(context, attributes) {
    protected var required: Boolean

    init {
        val a: TypedArray =
            context.theme.obtainStyledAttributes(attributes, R.styleable.AuthEditTextView, 0, 0)
        required = a.getBoolean(R.styleable.AuthEditTextView_required, true)
    }

    abstract fun validate(): Int?
    abstract fun errorText(errorType: Int): String
}

/**
 * @suppress
 */
fun ArcXPAuthEditText.addAfterTextListener() {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            validate()?.let { it -> error = errorText(it) }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    })
}

/**
 * @suppress
 */
class EmailEditText(context: Context, attributes: AttributeSet? = null) :
    ArcXPAuthEditText(context, attributes) {
    override fun validate() = when {
        text.toString().isEmpty() -> Constants.ZERO
        !text.toString().matches(Regex("^(.+)@(.+)\$")) -> Constants.ONE
        else -> null
    }

    override fun errorText(errorType: Int) = when (errorType) {
        Constants.ZERO -> context.getString(R.string.field_required)
        Constants.ONE -> context.getString(R.string.email_format)
        else -> context.getString(R.string.field_required)
    }

    override fun getHint(): CharSequence {
        return context.getString(R.string.hint_email)
    }

}

/**
 * @suppress
 */
class UserNameEditText(context: Context, attributes: AttributeSet? = null) :
    ArcXPAuthEditText(context, attributes) {
    override fun validate() = when {
        text.toString().isEmpty() -> Constants.ZERO
        else -> null
    }

    override fun errorText(errorType: Int) = when (errorType) {
        Constants.ZERO -> context.getString(R.string.field_required)
        else -> context.getString(R.string.field_required)
    }

    override fun getHint(): CharSequence {
        return context.getString(R.string.hint_user_name)
    }
}

/**
 * @suppress
 */
class DisplayNameEditText(context: Context, attributes: AttributeSet? = null) :
    ArcXPAuthEditText(context, attributes) {
    override fun validate() = when {
        text.toString().isEmpty() -> Constants.ZERO
        else -> null
    }

    override fun errorText(errorType: Int) = when (errorType) {
        Constants.ZERO -> context.getString(R.string.field_required)
        else -> context.getString(R.string.field_required)
    }

    override fun getHint(): CharSequence {
        return context.getString(R.string.hint_display_name)
    }
}

/**
 * @suppress
 */
class OldPasswordEditText(context: Context, attributes: AttributeSet? = null) :
    ArcXPAuthEditText(context, attributes) {
    override fun validate() = when {
        text.toString().isEmpty() -> Constants.ZERO
        else -> null
    }

    override fun errorText(errorType: Int) = when (errorType) {
        Constants.ZERO -> context.getString(R.string.field_required)
        else -> context.getString(R.string.field_required)
    }

    override fun getHint(): CharSequence {
        return context.getString(R.string.hint_old_password)
    }

}

/**
 * @suppress
 */
class PasswordEditText(context: Context, attributes: AttributeSet? = null) :
    ArcXPAuthEditText(context, attributes) {
    override fun validate() = when {
        text.toString().isEmpty() -> Constants.ZERO
        !text.toString().matches(Regex("(.*)[0-9](.*)")) -> Constants.ONE
        else -> null
    }

    override fun errorText(errorType: Int) = when (errorType) {
        Constants.ONE -> context.getString(R.string.password_requirement)
        else -> context.getString(R.string.field_required)
    }

    override fun getHint(): CharSequence {
        return context.getString(R.string.hint_password)
    }
}

/**
 * @suppress
 */
class ConfirmPasswordEditText(context: Context, attributes: AttributeSet? = null) :
    ArcXPAuthEditText(context, attributes) {

    override fun validate() = when {
        text.toString().isEmpty() -> Constants.ZERO
        else -> null
    }

    override fun errorText(errorType: Int) = when (errorType) {
        Constants.ZERO -> context.getString(R.string.field_required)
        Constants.ONE -> context.getString(R.string.not_match_password)
        else -> context.getString(R.string.field_required)
    }

    override fun getHint(): CharSequence {
        return context.getString(R.string.hint_confirm_password)
    }

    fun addAfterTextListener(passwordEditText: PasswordEditText) {
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (passwordEditText.text.toString() != text.toString()) {
                    error = errorText(Constants.ONE)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

}

/**
 * @suppress
 */
class FirstNameEditText(context: Context, attributes: AttributeSet? = null) :
    ArcXPAuthEditText(context, attributes) {
    override fun validate() = when {
        text.toString().isEmpty() -> Constants.ZERO
        !text.toString().matches(Regex("[A-Z][a-z]*")) -> Constants.ONE
        else -> null
    }

    override fun errorText(errorType: Int) = when (errorType) {
        Constants.ZERO -> context.getString(R.string.field_required)
        else -> context.getString(R.string.field_required)
    }

    override fun getHint(): CharSequence {
        return context.getString(R.string.hint_first_name)
    }
}

/**
 * @suppress
 */
class LastNameEditText(context: Context, attributes: AttributeSet? = null) :
    ArcXPAuthEditText(context, attributes) {
    override fun validate() = when {
        text.toString().isEmpty() -> Constants.ZERO
        !text.toString().matches(Regex("[A-Z][a-z]*")) -> Constants.ONE
        else -> null
    }

    override fun errorText(errorType: Int) = when (errorType) {
        Constants.ZERO -> context.getString(R.string.field_required)
        else -> context.getString(R.string.field_required)
    }

    override fun getHint(): CharSequence {
        return context.getString(R.string.hint_last_name)
    }
}

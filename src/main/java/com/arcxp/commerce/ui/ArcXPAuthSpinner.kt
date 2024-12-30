package com.arcxp.commerce.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import com.arcxp.commerce.models.Gender
import com.arcxp.commerce.models.LocationType

/**
 * Sealed class would differentiate spinner and handle options
 *
 * @param context Context of Spinner
 * @param attributeSet attributes of AttributeSet
 */
sealed class ArcXPAuthSpinner(context: Context, attributeSet: AttributeSet? = null) :
    AppCompatSpinner(context, attributeSet) {
    abstract fun bindUpAdapter()
}

class GenderSpinner(context: Context, attributeSet: AttributeSet? = null) :
    ArcXPAuthSpinner(context, attributeSet) {
    override fun bindUpAdapter() {
        adapter = ArrayAdapter(
            context, android.R.layout.simple_spinner_item, arrayOf(
                Gender.NON_CONFORMING.name,
                Gender.MALE.name,
                Gender.FEMALE.name,
                Gender.PREFER_NOT_TO_SAY.name
            )
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

}

class AddressesSpinner(context: Context, attributeSet: AttributeSet? = null) :
    ArcXPAuthSpinner(context, attributeSet) {
    override fun bindUpAdapter() {
        adapter = ArrayAdapter(
            context, android.R.layout.simple_spinner_item, arrayOf(
                LocationType.HOME.name,
                LocationType.WORK.name,
                LocationType.PRIMARY.name,
                LocationType.OTHER.name
            )
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

}
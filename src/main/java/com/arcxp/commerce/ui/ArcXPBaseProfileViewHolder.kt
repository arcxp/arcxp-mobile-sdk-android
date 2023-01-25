package com.arcxp.commerce.ui

import android.view.View

/**
 * @suppress
 */
abstract class ArcXPBaseProfileViewHolder (
    itemView: View
) : ArcXPAuthRecyclerAdapter.AuthViewHolder<ProfileAdapterData>(itemView)

/**
 * @suppress
 */
class ProfileAdapterData(
    val key: String,
    val value: String,
    private val viewType: Int
): ArcXPAuthRecyclerAdapter.AuthAdapterData() {
    override fun getItemViewType() = viewType
}

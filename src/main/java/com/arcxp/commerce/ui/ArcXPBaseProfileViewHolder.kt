package com.arcxp.commerce.ui

import android.view.View

abstract class ArcXPBaseProfileViewHolder (
    itemView: View
) : ArcXPAuthRecyclerAdapter.AuthViewHolder<ProfileAdapterData>(itemView)

class ProfileAdapterData(
    val key: String,
    val value: String,
    private val viewType: Int
): ArcXPAuthRecyclerAdapter.AuthAdapterData() {
    override fun getItemViewType() = viewType
}

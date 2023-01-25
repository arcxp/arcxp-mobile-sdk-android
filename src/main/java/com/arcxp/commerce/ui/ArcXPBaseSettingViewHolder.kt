package com.arcxp.commerce.ui

import android.view.View

/**
 * @suppress
 */
abstract class ArcXPBaseSettingsViewHolder(
    itemView: View,
    private val listener: NavigateTarget
) : ArcXPAuthRecyclerAdapter.AuthViewHolder<SettingAdapterData>(itemView) {
    override fun bind(data: SettingAdapterData) {
        itemView.setOnClickListener {
            listener(data.target)
        }
        bindData(data.customData)
    }

    abstract fun bindData(data: Any?)
}

/**
 * @suppress
 */
class SettingAdapterData(
    val target: String,
    private val viewType: Int,
    val customData: Any? = null
) : ArcXPAuthRecyclerAdapter.AuthAdapterData() {
    override fun getItemViewType() = viewType
}

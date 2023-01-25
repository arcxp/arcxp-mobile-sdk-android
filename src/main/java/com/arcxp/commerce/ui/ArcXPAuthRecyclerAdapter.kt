package com.arcxp.commerce.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

/**
 * Adapter class to handle RecyclerView data binding
 *
 * @param context Context of RecyclerView
 * @param itemsList List data to be displayed
 * @param [viewHolderCreator] return the object matching the #AutViewHolder
 * @suppress
 */
class ArcXPAuthRecyclerAdapter<T: ArcXPAuthRecyclerAdapter.AuthAdapterData, S: ArcXPAuthRecyclerAdapter.AuthViewHolder<T>>(
    private val context: Context,
    private val itemsList: List<T>,
    private val viewHolderCreator: (Context, ViewGroup, Int) -> S
): RecyclerView.Adapter<S>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): S {
        return viewHolderCreator(context, parent, viewType)
    }

    override fun getItemCount() =  itemsList.size

    override fun onBindViewHolder(holder: S, position: Int) = holder.bind(itemsList[position])

    override fun getItemViewType(position: Int): Int {
        return itemsList[position].getItemViewType()
    }

    abstract class AuthViewHolder<T>(itemView: View): ViewHolder(itemView) {
        abstract fun bind(data: T)
    }

    abstract class AuthAdapterData {
        abstract fun getItemViewType(): Int
    }

}

package com.arcxp.commerce.apimanagers

import android.content.Context
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arcxp.commerce.ui.ArcXPAuthRecyclerAdapter
import com.arcxp.commerce.ui.ArcXPBaseSettingsViewHolder
import com.arcxp.commerce.ui.SettingAdapterData
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.viewmodels.SettingViewModel

/**
 * Manager class for handling setting. Used by client developer.
 *
 * @param fragment The Fragment hold setting screen
 * @param settingUIInteraction the ui interaction [SettingUIInteraction] implemented by the fragment
 * @param viewModel view model [SettingViewModel] bind to the fragment
 *
 * eg: class SettingFragment: Fragment, SettingUIInteraction {
 *        ...
 *        val apiManager = createApiManager { SettingApiManager(it, it, SettingViewModel()) }
 *        ...
 *     }
 */
/**
 * @suppress
 */
class SettingApiManager<T : Fragment>(
    private val fragment: T,
    private val settingUIInteraction: SettingUIInteraction,
    private val viewModel: SettingViewModel
) : BaseApiManager<T>(fragment) {

    /**
     * Add observer for data change when view is created
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate() {
        viewModel.logoutResponse.observe(fragment.viewLifecycleOwner, Observer {
            if (it) settingUIInteraction.onLogoutSuccess()
        })

        viewModel.deletionResponse.observe(fragment.viewLifecycleOwner, Observer {
            if (it) settingUIInteraction.onDeletionApproved()
        })

        viewModel.logoutErrorResponse.observe(fragment.viewLifecycleOwner, Observer {
            settingUIInteraction.onLogoutError(it)
        })

        viewModel.deletionErrorResponse.observe(fragment.viewLifecycleOwner, Observer {
            settingUIInteraction.onDeletionError(it)
        })
    }

    /**
     * log out current user logic
     */
    fun logout() {
        viewModel.logout()
    }

    /**
     * Make account deletion request
     */
    fun deleteUser() {
        viewModel.deleteUser()
    }


    /**
     * Function to present ui after fetching profile data
     *
     * @param recyclerView list would be showing data
     *
     * eg: apiManager.setUpSettingView(recycler_settings)
     */
    fun setUpSettingView(recyclerView: RecyclerView) {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(fragment.requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = ArcXPAuthRecyclerAdapter(
                fragment.requireContext(),
                settingUIInteraction.getItemsList()
            ) { cxt, parent, viewType ->
                settingUIInteraction.createViewHolders(viewType, cxt, parent)
            }
        }
    }

}

/**
 * Interface used to update fragment UI
 * @suppress
 */
interface SettingUIInteraction {
    fun onLogoutSuccess()
    fun onLogoutError(error: ArcXPError)
    fun onDeletionApproved()
    fun onDeletionError(error: ArcXPError)
    fun getItemsList(): List<SettingAdapterData>
    fun createViewHolders(viewType: Int, cxt: Context, parent: ViewGroup): ArcXPBaseSettingsViewHolder
}

package com.arcxp.commerce.apimanagers

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import com.arcxp.commerce.ui.*
import com.arcxp.commerce.viewmodels.ProfileViewModel

/**
 * Manager class for handling profile loading and viewing. Used by client developer.
 *
 * @param fragment The Fragment hold profile screen
 * @param profileUIInteraction the ui interaction [ProfileUIInteraction] implemented by the fragment
 * @param viewModel view model [ProfileViewModel] bind to the fragment
 *
 * eg: class ProfileFragment: Fragment, ProfileUIInteraction {
 *        ...
 *        val apiManager = createApiManager { ProfileApiManager(it, it, ProfileViewModel()) }
 *        ...
 *     }
 */
/**
 * @suppress
 */
class ProfileApiManager<T: Fragment>(
    private val fragment: T,
    private val profileUIInteraction: ProfileUIInteraction,
    private val viewModel: ProfileViewModel
) : BaseApiManager<T>(fragment) {

    /**
     * Add observer for data change when view is created
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)

    /**
     * Function to get user profile
     *
     * eg: apiManager.getProfile()
     */
    fun getProfile() {
        viewModel.getProfile()
    }

    /**
     * Function to present ui after fetching profile data, recommend to use at
     * onSuccessFetchProfile for ProfileUIInteraction interface.
     *
     * @param recyclerView list would be showing data
     * @param profileResponse profile data retrieved from api
     *
     * eg: class ProfileFragment: Fragment, ProfileUIInteraction {
     *        ...
     *        val apiManager = createApiManager { ProfileApiManager(it, it, ProfileViewModel()) }
     *        ...
     *
     *        override fun onSuccessFetchProfile(profileResponse: ProfileManageResponse) {
     *           apiManager.setUpProfileView(profile_list, profileResponse)
     *         }
     *     }
     */

}

/**
 * Interface used to update fragment UI
 * @suppress
 */
interface ProfileUIInteraction {
}

package com.arcxp.commerce.apimanagers

import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import com.arcxp.commerce.models.ArcXPAddressRequest
import com.arcxp.commerce.models.ArcXPContactRequest
import com.arcxp.commerce.extendedModels.ArcXPProfileManage
import com.arcxp.commerce.models.ArcXPProfilePatchRequest
import com.arcxp.commerce.ui.*
import com.arcxp.commerce.util.ArcXPError
import com.arcxp.commerce.viewmodels.PatchProfileViewModel

/**
 * Manager class for handling patch profile related logic, used by client developer.
 *
 * @param fragment The Fragment hold patch profile screen
 * @param patchProfileUIInteraction the ui interaction [PatchProfileUIInteraction] implemented by the fragment
 * @param viewModel view model [PatchProfileViewModel] bind to the fragment
 *
 * eg: class PatchProfileFragment: Fragment, PatchProfileInteraction {
 *        ...
 *        val apiManager = createApiManager { PatchProfileApiManager(it, it, PatchProfileViewModel()) }
 *        ...
 *     }
 */
/**
 * @suppress
 */
class PatchProfileApiManager<T : Fragment>(
    private val fragment: T,
    private val patchProfileUIInteraction: PatchProfileUIInteraction,
    private val viewModel: PatchProfileViewModel
) : BaseApiManager<T>(fragment) {

    /**
     * Add observer for data change when view is created
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        viewModel.profileResponse.observe(fragment.viewLifecycleOwner, Observer {
            patchProfileUIInteraction.onSuccessPatched(it)
        })

        viewModel.errorResponse.observe(fragment.viewLifecycleOwner, Observer {
            patchProfileUIInteraction.onError(it)
        })
    }

    /**
     * Set up logic for click button to send out profile patch request with the updated profile
     *
     * @param etUpdate edit text for property would be updated
     * @param spnUpdate spinner for property would be updated
     * @param btnChange Button to trigger profile patch request
     * @param progress optional data loading indicator while making api call
     *
     * eg: apiManager.setUpProfileUpdate(etUpdate = et_update_first_name, spnUpdate = spinner_gender, btnChange = btn_update) {
     *        activity?.showProgress(true)
     *    }
     */
    fun setUpProfileUpdate(
        etUpdate: ArcXPAuthEditText? = null,
        spnUpdate: ArcXPAuthSpinner? = null,
        btnChange: Button,
        progress: (() -> Unit)? = null
    ) {
        etUpdate?.addAfterTextListener()
        spnUpdate?.bindUpAdapter()
        btnChange.setOnClickListener {
            progress?.invoke()
            etUpdate?.let { edt ->
                edt.validate()?.let { errorIndex ->
                    edt.error = edt.errorText(errorIndex)
                    return@setOnClickListener
                }
                when (edt) {
                    is FirstNameEditText -> viewModel.patchProfile(ArcXPProfilePatchRequest(firstName = edt.text.toString()))
                    is LastNameEditText -> viewModel.patchProfile(ArcXPProfilePatchRequest(lastName = edt.text.toString()))
                    is UserNameEditText -> viewModel.patchProfile(ArcXPProfilePatchRequest(displayName = edt.text.toString()))
                    else -> return@let
                }
            }
            spnUpdate?.let { spu ->
                when (spu) {
                    is GenderSpinner -> viewModel.patchProfile(ArcXPProfilePatchRequest(gender = spu.selectedItem.toString()))
                    else -> {}
                }
            }
        }

    }

    fun updateProfileBirthdate(birthDay : Int , birthMonth : Int, birthYear: Int){
        viewModel.patchProfile(ArcXPProfilePatchRequest(birthMonth = birthMonth.toString(), birthDay = birthDay.toString(), birthYear = birthYear.toString()))
    }

    fun updateAddress(addressRequest: List<ArcXPAddressRequest>){
        viewModel.patchProfile(ArcXPProfilePatchRequest(addresses = addressRequest))
    }

    fun updateContact(contactRequest: List<ArcXPContactRequest>){
        viewModel.patchProfile(ArcXPProfilePatchRequest(contacts = contactRequest))
    }
}

/**
 * Interface used to update fragment UI
 * @suppress
 */
interface PatchProfileUIInteraction {
    fun onSuccessPatched(profileManageResponse: ArcXPProfileManage)
    fun onError(error: ArcXPError)
}

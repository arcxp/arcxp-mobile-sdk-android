package com.arcxp.commerce.apimanagers

import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver

/**
 * Open manager class implemented Android lifecycle interface, registered fragment's view lifecycle
 * with listener at the the time of creation and deregister at the time of destroy. The inheritor would interact
 * data with respect of view's lifecycle. Used by SDK developer.
 *
 * The BaseApiManager class will be acting as the parent class for all succeeded Api manager classes. The purpose
 * of Api Manager is to handle all the communication between view model and fragment to reduce the code work of
 * developers who would be using commerce SDK. It acted as a bridge between fragment and view model to let the
 * developers only focus on UI integration and customized functionality.
 *
 * To use the general api manager, simply create a specific class extended to it and pass the fragment
 * instance to BaseApiManager. The base manager is visible inside SDK, the succeeded class should be
 * created within the SDK only.
 *
 * eg: class LoginApiManager<T : Fragment>(
 *         private val fragment: T,
 *         ...
 *     ) : BaseApiManager<T>(fragment) {
 *     ...
 *     }
 *
 * @param fragment The Fragment bind to the Api manager
 */
/**
 * @suppress
 */
open class BaseApiManager<T : Fragment>(private val fragment: T?) : LifecycleObserver {

    internal fun getTargetFragment() = fragment

    /**
     * Add fragment view lifecycle observer to current api manager,
     * would registered at {@link Fragment#onCreateView}
     */
    fun addViewObserver() {
        fragment?.viewLifecycleOwner?.lifecycle?.addObserver(this)
    }

    /**
     * Remove fragment view lifecycle observer to current api manager,
     * would registered at {@link Fragment#onDestroyView}
     */
    fun removeViewObserver() {
        fragment?.viewLifecycleOwner?.lifecycle?.addObserver(this)
    }

}

/**
 * returns an Api manager instance corresponding to the fragment matching the given [create] for view and view model interaction
 * @suppress
 */
inline fun <reified T : Fragment, R : BaseApiManager<T>> T.createApiManger(noinline create: (T) -> R): R {
    return create(this)
}

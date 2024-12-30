package com.arcxp.commons.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * ConnectionUtil is a utility object responsible for checking network connectivity status within the ArcXP Commerce module.
 * It provides methods to determine if the device is connected to the internet and if the connection is via WiFi.
 *
 * The object defines the following operations:
 * - Check if the device has an active internet connection
 * - Check if the device is connected to a WiFi network
 *
 * Usage:
 * - Use the provided methods to check the network connectivity status.
 *
 * Example:
 *
 * val isConnected = ConnectionUtil.isInternetAvailable(context)
 * val isOnWiFi = ConnectionUtil.isOnWiFi(context)
 *
 * Note: Ensure that the context is properly configured before using ConnectionUtil.
 *
 * @method isInternetAvailable Check if the device has an active internet connection.
 * @method isOnWiFi Check if the device is connected to a WiFi network.
 */
object ConnectionUtil {

    fun isInternetAvailable(context: Context): Boolean {
        var result = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }

                }
            }
        }

        return result
    }

    fun isOnWiFi(context: Context): Boolean {
        var result = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        else -> false
                    }

                }
            }
        }

        return result
    }
}
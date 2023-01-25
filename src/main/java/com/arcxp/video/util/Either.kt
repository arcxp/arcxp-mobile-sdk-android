package com.arc.arcvideo.util

import okhttp3.ResponseBody
import java.lang.Exception

/**
 * Wrapper class for linearly passing failure or success responses
 * @suppress
 */
sealed class Either<out L, out R>

/**
 * @suppress
 */
data class Failure<out L>(val l: L) : Either<L, Nothing>()

/**
 * @suppress
 */
data class Success<out R>(val r: R) : Either<Nothing, R>()


/**
 * handle error data presenting
 *
 * @param failure error or exception to be printing
 * @suppress
 */
fun handleFailure(failure: Any?): String = when(failure) {
    is ResponseBody -> failure.string()
    is Exception -> if (failure.localizedMessage != null) failure.localizedMessage else failure.toString()
    else -> failure.toString()
}

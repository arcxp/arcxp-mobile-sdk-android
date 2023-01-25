package com.arcxp.content.util

import androidx.annotation.Keep
import okhttp3.ResponseBody
import java.lang.Exception

/**
 * Wrapper class for linearly passing failure or success responses
 */
@Keep
sealed class Either<out L, out R>

@Keep
data class Failure<out L>(val failure: L) : Either<L, Nothing>()

@Keep
data class Success<out R>(val success: R) : Either<Nothing, R>()


/**
 * handle error data presenting
 *
 * @param failure error or exception to be printing
 */
fun handleFailure(failure: Any?): String = when(failure) {
    is ResponseBody -> failure.string()
    is Exception -> if (failure.localizedMessage != null) failure.localizedMessage else failure.toString()
    else -> failure.toString()
}

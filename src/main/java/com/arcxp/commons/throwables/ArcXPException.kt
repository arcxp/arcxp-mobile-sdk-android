package com.arcxp.commons.throwables

import com.google.errorprone.annotations.Keep
import org.json.JSONObject
import retrofit2.Response

@Keep
/**
 * [ArcXPException]
 * for catchable exceptions
 * Constructor
 * @param type [ArcXPSDKErrorType]
 * @param message Description of the error
 * @param value An object appropriate for the exception
 */
class ArcXPException(
    override var message: String? = null,
    var type: ArcXPSDKErrorType? = null,
    val value: Any? = null,
    var code: String? = null
) : Exception() {
    init {
        value?.let {
            try {
                if (it is Response<*>) {
                    val jsonObj =
                        JSONObject((value as Response<*>).errorBody()!!.charStream().readText())
                    this.code = jsonObj.getString("code")
                    this.message = jsonObj.getString("message")
                }
            } catch (_: Throwable) {}
        }

    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArcXPException

        if (message != other.message) return false
        if (type != other.type) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + (type?.hashCode() ?: 0)
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }
}
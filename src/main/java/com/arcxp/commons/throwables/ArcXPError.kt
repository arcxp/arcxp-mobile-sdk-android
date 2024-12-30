package com.arcxp.commons.throwables

import com.google.errorprone.annotations.Keep
import org.json.JSONObject
import retrofit2.Response

@Keep
/**
 * [ArcXPError]
 * for non recoverable errors
 * Constructor
 * @param type [ArcXPSDKErrorType]
 * @param code Numeric value of the error
 * @param message Description of the error
 * @param value An object appropriate for the exception
 */
class ArcXPError(
    val type: ArcXPSDKErrorType? = null,
    var code: String? = null,
    override var message: String,
    val value: Any? = null
) : Error() {
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

        other as ArcXPError

        if (type != other.type) return false
        if (code != other.code) return false
        if (message != other.message) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type?.hashCode() ?: 0
        result = 31 * result + (code?.hashCode() ?: 0)
        result = 31 * result + message.hashCode()
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }
}
package com.arcxp.commons.util

import com.squareup.moshi.*
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okio.Buffer
import org.json.JSONException
import org.json.JSONObject
import java.util.*

object MoshiController {

    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .add(JSONObjectAdapter())
        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
        .build()

    @FromJson
    fun <T> fromJson(obj: String, classT: Class<T>): T? {
        val jsonAdapter: JsonAdapter<T> = moshi.adapter(classT)
        return jsonAdapter.fromJson(obj)
    }

    @ToJson
    inline fun <reified T> toJson(obj: T): String? {
        val jsonAdapter: JsonAdapter<T> = moshi.adapter(T::class.java)
        return jsonAdapter.toJson(obj)
    }

    class JSONObjectAdapter {

        @FromJson
        fun fromJson(reader: JsonReader): JSONObject? {
            // Here we're expecting the JSON object, it is processed as Map<String, Any> by Moshi
            return (reader.readJsonValue() as? Map<String, Any>)?.let { data ->
                try {
                    JSONObject(data)
                } catch (e: JSONException) {
                    // Handle exception
                    return null
                }
            }
        }

        @ToJson
        fun toJson(writer: JsonWriter, value: JSONObject?) {
            value?.let { writer.value(Buffer().writeUtf8(value.toString())) }
        }
    }
}
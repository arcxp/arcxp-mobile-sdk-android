package com.arcxp.commerce.util

import java.lang.Exception

/**
 * Singleton class used to initialization with parameters
 * @suppress
 */
open class SingletonHolder<out T : Any, in A, in B>(creator: (A, B) -> T) {
    private var creator: ((A, B) -> T)? = creator

    @Volatile
    private var instance: T? = null

    /**
     * Initiate with the parameter needed
     *
     * @param arg object needed for initialization
     * @return The singleton object for used
     */
    fun initiate(arg: A, arg2: B): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg, arg2)
                instance = created
                creator = null
                created
            }
        }
    }

    /**
     * Once the singleton object initiated use this to call object
     *
     * @return The singleton object for used
     */
    fun getInstance(): T {
        return instance ?: throw Exception("Singleton class not initiated!")
    }
}

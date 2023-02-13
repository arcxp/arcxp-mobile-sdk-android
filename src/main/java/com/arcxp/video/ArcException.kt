package com.arcxp.video

import com.arcxp.video.model.ArcVideoSDKErrorType

/**
 * Thrown when there is a problem within the SDK.
 * @suppress
 */
class ArcException : RuntimeException {
    /**
     * Returns the type of the error
     * @return [ArcVideoSDKErrorType]
     */
    var type: ArcVideoSDKErrorType? = null
        private set

    /**
     * Returns the attached value object
     * @return An object
     */
    var value: Any? = null
        private set

    /**
     * Constructor
     * @param type [ArcVideoSDKErrorType]
     * @param message Description of the error
     * @param value An object appropriate for the exception
     */
    constructor(type: ArcVideoSDKErrorType, message: String, value: Any?) : super(message) {
        this.type = type
        this.value = value
    }

    /**
     * Constructor
     * @param type [ArcVideoSDKErrorType]
     * @param message Description of the error
     */
    constructor(type: ArcVideoSDKErrorType, message: String) : super(message) {
        this.type = type
    }

    /**
     * Constructor
     * @param message Description of the Error
     */
    constructor(message: String) : super(message) {}
}
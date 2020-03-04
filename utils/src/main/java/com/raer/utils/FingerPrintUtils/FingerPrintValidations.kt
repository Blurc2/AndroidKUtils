package com.raer.utils.FingerPrintUtils

import android.app.Activity

/**
 * The enum Finger Print validations, used in [Extensions.validateFingerPrint] to handle the possible
 * states of the device.
 */
enum class FingerPrintValidations {
    /**
     * No enrolled finger validations.
     */
    NO_ENROLLED,
    /**
     * No hardware finger validations.
     */
    NO_HARDWARE,
    /**
     * No secure lock finger validations.
     */
    NO_SECURE_LOCK,
    /**
     * Valid finger validations.
     */
    VALID
}
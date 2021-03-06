package com.raer.utils.FingerPrintUtils

import android.hardware.fingerprint.FingerprintManager

/**
 * The interface Finger print callbacks, used to handle callbacks generated by [FingerprintHandler] in
 * the [GenericDialog].
 */
interface FingerPrintCallbacks {
    /**
     * On authentication error.
     *
     * @param errorCode         the error code
     * @param paramCharSequence the error message
     */
    fun onAuthenticationError(errorCode: Int, paramCharSequence: CharSequence)

    /**
     * On authentication failed.
     */
    fun onAuthenticationFailed()

    /**
     * On authentication succeeded.
     *
     * @param paramAuthenticationResult the param authentication result
     */
    fun onAuthenticationSucceeded(paramAuthenticationResult: FingerprintManager.AuthenticationResult)
}

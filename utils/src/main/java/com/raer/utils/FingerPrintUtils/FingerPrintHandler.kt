package com.raer.utils.FingerPrintUtils

import android.annotation.SuppressLint
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import androidx.annotation.RequiresApi


/**
 * The class Fingerprint handler, this class is used to handle the fingerprint events,
 * generated by the fingerprint manager API.
 *
 * @see FingerprintManager
 */
@RequiresApi(api = Build.VERSION_CODES.M)
class FingerPrintHandler
/**
 * Instantiates a new Fingerprint handler.
 *
 * @param paramFingerPrintCallbacks the param finger print callbacks, it is the interface which
 * is going to handle the fingerprint callbacks
 */
    (private val fingerPrintCallbacks: FingerPrintCallbacks) :
    FingerprintManager.AuthenticationCallback() {

    private val signal = CancellationSignal()

    /**
     * Cancels auth operation.
     */
    fun cancelOperation() {
        this.signal.cancel()
    }

    /**
     * Starts authentication service.
     *
     * @param paramFingerprintManager the param fingerprint manager
     * @param paramCryptoObject       the param crypto object
     */
    @SuppressLint("MissingPermission")
    fun doAuth(
        paramFingerprintManager: FingerprintManager,
        paramCryptoObject: FingerprintManager.CryptoObject
    ) {
        try {
            paramFingerprintManager.authenticate(paramCryptoObject, this.signal, 0, this, null)
            return
        } catch (securityException: SecurityException) {
            return
        }

    }

    override fun onAuthenticationError(paramInt: Int, paramCharSequence: CharSequence) {
        super.onAuthenticationError(paramInt, paramCharSequence)
        this.fingerPrintCallbacks.onAuthenticationError(paramInt, paramCharSequence)
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        this.fingerPrintCallbacks.onAuthenticationFailed()
    }

    override fun onAuthenticationHelp(paramInt: Int, paramCharSequence: CharSequence) {
        super.onAuthenticationHelp(paramInt, paramCharSequence)
    }

    override fun onAuthenticationSucceeded(paramAuthenticationResult: FingerprintManager.AuthenticationResult) {
        super.onAuthenticationSucceeded(paramAuthenticationResult)
        this.fingerPrintCallbacks.onAuthenticationSucceeded(paramAuthenticationResult)
    }
}
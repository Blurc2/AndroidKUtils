package com.raer.utils

import android.util.Base64
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * The AESEncrypt class which contains all the methods needed in order to cipher or decipher a String
 */
object AESEncrypt {

    private val ALGORITM = "AES/CBC/PKCS5Padding"
    private val UNICODE_FORMAT = "UTF-8"
    private val iv = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

    /**
     * Ciphers given string and returns the byte array with the ciphered data, the algorithm used is AES with the operation mode CBC,
     * the initial vector defined as iv is an array of bytes predefined, [AESEncrypt.UNICODE_FORMAT] is UTF-8
     *
     * @param data     string which is going to be ciphered
     * @param keyValue the key value which is used for the cipher class
     * @return a byte array which contains the given string ciphered
     * @see Cipher
     *
     * @see IvParameterSpec
     *
     * @see AESEncrypt.generateKey
     */
    fun encryptToByte(data: String?, keyValue: String): ByteArray? {
        var encVal: ByteArray? = null
        if (data != null) {
            try {
                val key = generateKey(keyValue)
                val c = Cipher.getInstance(ALGORITM)
                val ivspec = IvParameterSpec(iv)
                c.init(Cipher.ENCRYPT_MODE, key, ivspec)
                encVal = c.doFinal(data.toByteArray(charset(UNICODE_FORMAT)))
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return encVal
    }

    /**
     * Ciphers given string
     *
     * @param data     string which is going to be ciphered
     * @param keyValue the key value which is used for the cipher class
     * @return the result string after ciphering the `data`
     * @see AESEncrypt.encryptToByte
     * @see Base64
     */
    fun encryptToString(data: String, keyValue: String): String? {

        val encVal = encryptToByte(data, keyValue)
        var encryptedValue: String? = null
        if (encVal != null) {
            try {
                encryptedValue = String(
                    Base64.encode(
                        encVal,
                        Base64.DEFAULT
                    ), StandardCharsets.UTF_8
                )
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

        }
        return encryptedValue
    }

    /**
     * Deciphers given string and returns the byte array with the deciphered data, the algorithm used is AES with the operation mode CBC,
     * the initial vector defined as iv is an array of bytes predefined
     *
     * @param data     string which is going to be deciphered
     * @param keyValue the key value which is used for the cipher class
     * @return a byte array which contains the given string deciphered
     * @see Cipher,IvParameterSpec
     *
     * @see Base64
     *
     * @see AESEncrypt.generateKey
     */
    fun decryptToByte(data: String?, keyValue: String): ByteArray? {
        var decValue: ByteArray? = null
        if (data != null) {

            try {
                val key = generateKey(keyValue)
                val c = Cipher.getInstance(ALGORITM)
                val ivspec = IvParameterSpec(iv)
                c.init(Cipher.DECRYPT_MODE, key, ivspec)
                val decodedValue = Base64.decode(data, Base64.DEFAULT)
                decValue = c.doFinal(decodedValue)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return decValue
    }

    /**
     * Deciphers given string
     *
     * @param data     string which is going to be deciphered
     * @param keyValue the key value which is used for the cipher class
     * @return the result string after deciphering the `data`
     * @see AESEncrypt.decryptToByte
     */
    fun decryptToString(data: String, keyValue: String): String? {
        val decValue = decryptToByte(data, keyValue)
        var decryptedValue: String? = null
        if (decValue != null) {
            decryptedValue = String(decValue)
        }
        return decryptedValue
    }

    /**
     * Generates a `Key` for the given string key, it uses AES algorithm in order to generate the key
     *
     * @param keyText     the string key used in order to generating a `Key` object
     * @return the `Key` object generated with the `keyText` string
     * @see Key
     *
     * @see SecretKeySpec
     */
    private fun generateKey(keyText: String): Key {
        val keyValue = Arrays.copyOf(keyText.toByteArray(), 16)
        return SecretKeySpec(keyValue, "AES")
    }

}
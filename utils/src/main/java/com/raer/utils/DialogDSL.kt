package com.raer.utils

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import com.raer.utils.FingerPrintUtils.FingerPrintCallbacks
import com.raer.utils.FingerPrintUtils.FingerPrintHandler
import com.raer.utils.dtos.CustomLayoutDto
import com.raer.utils.dtos.DialogDto
import com.raer.utils.dtos.FingerAuthDto
import com.raer.utils.enums.DialogType
import java.security.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException

@DslMarker
annotation class DialogMarker

@DialogMarker
class DialogDSL : DialogFragment(),
    FingerPrintCallbacks {
    private lateinit var binding : ViewDataBinding
    private var tries = 0
    private var cryptoObject: FingerprintManager.CryptoObject? = null

    private var fingerprintManager: FingerprintManager? = null

    private var fph: FingerPrintHandler? = null

    private var keyGenerator: KeyGenerator? = null

    private var keyStore: KeyStore? = null

    private var authConfig: FingerAuthDto? = null

    private var customLayout: CustomLayoutDto? = null

    private var btnCancelAction: (() -> Unit)? = null

    private var btnAcceptAction: (() -> Unit)? = null

    var title: String? = null
    var msg: String? = null
    var btnCancelText: String? = null
    var btnAcceptText: String? = null
    var type: DialogType? = null



    private val dialogDto by lazy {
        DialogDto(title,msg,authConfig?.auth_msg,btnCancelText,btnAcceptText,{
            btnCancelAction?.invoke()
            dismiss()
        },{
            btnAcceptAction?.invoke()
            dismiss()
        },authConfig?.authSuccessAction,authConfig?.authErrorAction,
            authConfig?.let { DialogType.BIOMETRIC } ?: type?.takeIf { it != DialogType.BIOMETRIC })
    }

    fun authConfig(init: FingerAuthDto.() -> Unit) = FingerAuthDto().apply { init() }.also{ authConfig = it}

    fun customLayout(init: CustomLayoutDto.() -> Unit) = CustomLayoutDto().apply { init() }.also{ customLayout = it}

    fun btnAcceptAction(init: () -> Unit) = init.also{ btnAcceptAction = it}

    fun btnCancelAction(init: () -> Unit) = init.also{ btnCancelAction = it}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomThemeDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,customLayout?.layoutId ?: R.layout.generic_dialog,container,false)

        if (dialog != null && dialog?.window != null) {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(0) as Drawable)
            dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (dialogDto.type == DialogType.BIOMETRIC) {
            if (dialogDto.title.isNullOrEmpty())
                dialogDto.title = getString(R.string.finger_auth_header)
            if (dialogDto.msg.isNullOrEmpty())
                dialogDto.msg = getString(R.string.finger_auth_msg)
            if (dialogDto.auth_msg.isNullOrEmpty())
                dialogDto.auth_msg = getString(R.string.finger_prompt_msg)
        }

        binding.setVariable(customLayout?.bindingVariable ?: BR.data,customLayout?.bindingObject ?: dialogDto)
        binding.setLifecycleOwner(this)
        if (dialogDto.type == DialogType.BIOMETRIC)
            beginAuth()
        binding.executePendingBindings()
    }

    /**
     * Starts biometric auth service
     */
    private fun beginAuth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager =
                activity?.getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
            fph = FingerPrintHandler(this)
            generateKey()
            try {
                cryptoObject = FingerprintManager.CryptoObject(generateCipher())
            } catch (e: NoSuchPaddingException) {
                e.printStackTrace()
                return
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
                return
            } catch (e: UnrecoverableKeyException) {
                e.printStackTrace()
                return
            } catch (e: KeyStoreException) {
                e.printStackTrace()
                return
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
                return
            }

            executeIfNotNull(fingerprintManager, fph, cryptoObject) {
                fph!!.doAuth(fingerprintManager!!, cryptoObject!!)
            }
        } else
            requireActivity().customDialog {
                        title = R.string.no_api23_header.stringFromResource()
                        msg = R.string.no_api23_msg.stringFromResource()
                        btnAcceptText = R.string.btn_ok.stringFromResource()
                        btnAcceptAction {
                            dismiss()
                        }
                    }
    }

    /**
     * Generates a cipher with the AES algorithm and the CBC operation mode, it is used as the [FingerprintManager.CryptoObject].
     *
     * @return the Cipher
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     * @throws KeyStoreException
     * @throws InvalidKeyException
     */
    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        UnrecoverableKeyException::class,
        KeyStoreException::class,
        InvalidKeyException::class
    )
    private fun generateCipher(): Cipher {
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        cipher.init(1, keyStore?.getKey("SwA", null))
        return cipher
    }

    /**
     * Generates a key Store which is used in [GenericDialog.generateCipher]
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore")
            keyStore?.load(null)
            keyGenerator?.init(
                KeyGenParameterSpec.Builder("SwA", KeyProperties.PURPOSE_ENCRYPT).setBlockModes(
                    KeyProperties.BLOCK_MODE_CBC
                ).setUserAuthenticationRequired(true).setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_PKCS7
                ).build()
            )
            keyGenerator?.generateKey()
        } catch (keyStoreException: KeyStoreException) {
            keyStoreException.printStackTrace()
        } catch (keyStoreException: NoSuchAlgorithmException) {
            keyStoreException.printStackTrace()
        } catch (keyStoreException: NoSuchProviderException) {
            keyStoreException.printStackTrace()
        } catch (keyStoreException: InvalidAlgorithmParameterException) {
            keyStoreException.printStackTrace()
        } catch (keyStoreException: java.security.cert.CertificateException) {
            keyStoreException.printStackTrace()
        } catch (keyStoreException: java.io.IOException) {
            keyStoreException.printStackTrace()
        }

    }

    override fun onDismiss(paramDialogInterface: DialogInterface) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            try {
                fph?.cancelOperation()
            } catch (exception: Exception) {

            }

        super.onDismiss(paramDialogInterface)
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && fph != null && dialogDto.type == DialogType.BIOMETRIC)
            beginAuth()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onAuthenticationError(errorCode: Int, paramCharSequence: CharSequence) {
        if (dialogDto.type == DialogType.BIOMETRIC) {
            if (errorCode != FingerprintManager.FINGERPRINT_ERROR_CANCELED)
                try {

                    binding.root.findViewById<TextView>(R.id.tv_finger).setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_trazado_292,
                        0,
                        0,
                        0
                    )
                    context?.let {
                        binding.root.findViewById<TextView>(R.id.tv_finger).setTextColor(it.getCompatColor(R.color.orangey_red))
                    }

                    if (errorCode == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT) {
                        dialogDto.auth_msg = getString(R.string.finger_auth_error5)

                        dialogDto.AuthErrorAction?.let {
                            dialogDto.msg = paramCharSequence.toString()
                            dialogDto.btnAcceptAction = {
                                dialogDto.AuthErrorAction?.invoke()
                                dismiss()
                            }
                        } ?: run {
                            dialogDto.msg = paramCharSequence.toString()
                        }
                    } else {
                        dialogDto.auth_msg = getString(R.string.finger_auth_error)
                    }
                    binding.invalidateAll()
                } catch (nullPointerException: NullPointerException) {
                    return
                }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onAuthenticationFailed() {
        if (dialogDto.type == DialogType.BIOMETRIC) {
            try {
                tries++
                binding.root.findViewById<TextView>(R.id.tv_finger).setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_trazado_292,
                    0,
                    0,
                    0
                )
                context?.let {
                    binding.root.findViewById<TextView>(R.id.tv_finger).setTextColor(it.getCompatColor(R.color.orangey_red))
                }
                dialogDto.auth_msg = getString(R.string.finger_auth_error)
                binding.invalidateAll()
                return
            } catch (nullPointerException: NullPointerException) {
                return
            }

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onAuthenticationSucceeded(paramAuthenticationResult: FingerprintManager.AuthenticationResult) {
        if (dialogDto.type == DialogType.BIOMETRIC) {
            try {
                binding.root.findViewById<TextView>(R.id.tv_finger).setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_success_black_24_px,
                    0,
                    0,
                    0
                )
                context?.let {
                    binding.root.findViewById<TextView>(R.id.tv_finger).setTextColor(it.getCompatColor(R.color.soft_blue))
                    binding.root.findViewById<TextView>(R.id.tv_action_accept).setTextColor(
                        getColorWithAlpha(
                            it.getCompatColor(R.color.soft_blue),
                            127
                        )
                    )
                }
                binding.root.findViewById<TextView>(R.id.tv_action_accept).isEnabled = false

                dialogDto.auth_msg = getString(R.string.finger_auth_success)
                Handler().postDelayed({
                    dialogDto.AuthSuccessAction?.invoke()
                    dismiss()
                }, 500L)
                binding.invalidateAll()
                return
            } catch (nullPointerException: NullPointerException) {
                return
            }

        }

    }
}
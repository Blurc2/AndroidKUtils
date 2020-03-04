package com.raer.utils

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.raer.utils.enums.PermissionCallbackType
import com.tbruyelle.rxpermissions2.RxPermissions

class Permissions(val activity: AppCompatActivity) {
    private var rxPermissions : RxPermissions = RxPermissions(activity)

//    @SuppressLint("CheckResult")
//    fun requestPermission(vararg name : String, callback: ((PermissionCallbackType) -> Unit?)?){
//        rxPermissions
//            .requestEachCombined(*name)
//            .subscribe{ permission ->
//                if (permission.granted) {
//                    callback?.invoke(PermissionCallbackType.ACCEPTED)
//                } else if (permission.shouldShowRequestPermissionRationale)
//                {
//                    activity.customDialog {
//                                title = R.string.permission_need_title.stringFromResource()
//                                msg = R.string.permission_need_msg.stringFromResource()
//                                btnAcceptText = R.string.btn_ok.stringFromResource()
//                                btnCancelText = R.string.btn_cancel.stringFromResource()
//                                btnAcceptAction {
//                                    requestPermission(name= *name,callback = callback)
//                                }
//                                btnCancelAction {
//                                    callback?.invoke(PermissionCallbackType.REJECTED)
//                                }
//                            }
//                }
//                else {
//                    activity.customDialog {
//                        title = R.string.permission_need_title.stringFromResource()
//                        msg = R.string.permission_need_msg.stringFromResource()
//                        btnAcceptText = R.string.btn_ok.stringFromResource()
//                        btnCancelText = R.string.btn_cancel.stringFromResource()
//                        btnAcceptAction {
//                            this@Permissions.activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)))
//                        }
//                        btnCancelAction {
//                            callback?.invoke(PermissionCallbackType.REJECTED_NO_ASK)
//                        }
//                    }
//                }
//            }
//    }
//
//    @SuppressLint("CheckResult")
//    fun requestPermissions(callback: ((PermissionCallbackType) -> Unit?)?){
//        val permissions = activity.packageManager.getPackageInfo(activity.packageName, PackageManager.GET_PERMISSIONS).requestedPermissions
//
//        rxPermissions
//            .requestEachCombined(*permissions)
//            .subscribe{ permission ->
//                if (permission.granted) {
//                    callback?.invoke(PermissionCallbackType.ACCEPTED)
//                } else if (permission.shouldShowRequestPermissionRationale)
//                {
//                    activity.customDialog {
//                        title = R.string.permission_need_title.stringFromResource()
//                        msg = R.string.permission_need_msg.stringFromResource()
//                        btnAcceptText = R.string.btn_ok.stringFromResource()
//                        btnCancelText = R.string.btn_cancel.stringFromResource()
//                        btnAcceptAction {
//                            requestPermissions(null)
//                        }
//                        btnCancelAction {
//                            callback?.invoke(PermissionCallbackType.REJECTED)
//                        }
//                    }
//                }
//                else {
//                    activity.customDialog {
//                        title = R.string.permission_need_title.stringFromResource()
//                        msg = R.string.permission_need_msg.stringFromResource()
//                        btnAcceptText = R.string.btn_ok.stringFromResource()
//                        btnCancelText = R.string.btn_cancel.stringFromResource()
//                        btnAcceptAction {
//                            this@Permissions.activity.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)))
//                        }
//                        btnCancelAction {
//                            callback?.invoke(PermissionCallbackType.REJECTED_NO_ASK)
//                        }
//                    }
//                }
//            }
//    }
}
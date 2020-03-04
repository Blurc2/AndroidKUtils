package com.raer.utils.dtos

import com.raer.utils.enums.DialogType

data class DialogDto(
    var title : String? = null,
    var msg : String? = null,
    var auth_msg : String? = null,
    var btnCancelText : String? = null,
    var btnAcceptText : String? = null,
    var btnCancelAction : (() -> Unit)? = null,
    var btnAcceptAction : (() -> Unit)? = null,
    var AuthSuccessAction : (() -> Unit)? = null,
    var AuthErrorAction : (() -> Unit)? = null,
    var type : DialogType? = null
)
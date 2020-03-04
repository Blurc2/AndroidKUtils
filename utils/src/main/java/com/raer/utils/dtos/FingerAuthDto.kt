package com.raer.utils.dtos

import com.raer.utils.DialogMarker

@DialogMarker
data class FingerAuthDto(
    var auth_msg : String? = null,
    var authSuccessAction : (() -> Unit)? = null,
    var authErrorAction : (() -> Unit)? = null
){
    fun authSuccessAction(init: () -> Unit) = init.also{ authSuccessAction = it}

    fun authErrorAction(init: () -> Unit) = init.also{ authErrorAction = it}
}
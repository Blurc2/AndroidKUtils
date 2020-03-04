package com.raer.utils.dtos

import androidx.annotation.LayoutRes
import com.raer.utils.DialogMarker
import com.raer.utils.isNull
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@DialogMarker
class CustomLayoutDto {
    @LayoutRes var layoutId : Int? = null
     var bindingVariable : Int? = null
        set(value) {
            if(!layoutId.isNull())
                field = value
        }
    var bindingObject : Any? = null
        set(value) {
            if(!layoutId.isNull() && !bindingVariable.isNull())
                field = value
        }
}
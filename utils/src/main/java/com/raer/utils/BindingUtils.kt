package com.raer.utils

import com.raer.utils.enums.DialogType
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("setDialogType")
fun ImageView.setDialogType(type : DialogType?){
    when(type){
        DialogType.SUCCESS -> setImageResource(R.drawable.ic_success_black_24_px)
        DialogType.INFO -> setImageResource(R.drawable.ic_warning_black_24_px)
        DialogType.ERROR -> setImageResource(R.drawable.ic_error_black_24_px)
        else ->   setImageResource(R.drawable.ic_error_black_24_px)
    }
}

@BindingAdapter("regex")
fun RegexEditText.regex(regex: String?) {
    this.regex = regex?.let { if(it.isNotEmpty()) it.toRegex() else null }
}

@BindingAdapter("setTextOrGone")
fun TextView.setTextOrGone(msg : String?){
    if (msg.isNullOrEmpty())
        visibility = View.GONE
    else
    {
        text = msg
        visibility = View.VISIBLE
    }
}

@BindingAdapter("setTextOrInvisible")
fun TextView.setTextOrInvisible(msg : String?){
    if (msg.isNullOrEmpty())
        visibility = View.INVISIBLE
    else
    {
        text = msg
        visibility = View.VISIBLE
    }
}
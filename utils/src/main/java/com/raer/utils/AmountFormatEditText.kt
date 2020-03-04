package com.raer.utils

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import org.jetbrains.anko.singleLine


class AmountFormatEditText : EditText{

    private val regexAmount = "^[1-9][0-9]*\\.?([0-9]{1,2})?\$".toRegex()
    private var str: String = ""
    private var lastValid = ""
    var max = 0F
    var min = 0F
    var onValidAmount: ((String) -> Unit)? = null

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {

        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            removeTextChangedListener(this)
            str = s.toString().replace("$","")
            when {
                str.matches(regexAmount) -> {
                    if(max == 0F || str.toFloat() in min..max)
                    {
                        error = null
                        onValidAmount?.invoke(str.toFloat().amountFormat())
                    }
                    else
                    {
                        error = context.getString(R.string.amount_limit,min.amountFormat(),max.amountFormat())
                    }

                    lastValid = context.getString(R.string.amount_format,str)
                    setText(context.getString(R.string.amount_format ,str))
                    setSelection(context.getString(R.string.amount_format,str).length)
                }
                str.isEmpty() -> {
                    onValidAmount?.invoke(str)
                    error = null
                    lastValid = str
                    setText(str)
                }
                else -> {
                    if(lastValid.replace("$","").isNotEmpty()){
                        val answer = lastValid.replace("$","").toFloat()
                        if(!(max == 0F || answer in min..max))
                        {
                            error = context.getString(R.string.amount_limit ,min.amountFormat(),max.amountFormat())
                        }
                    }
                    setText(lastValid)
                    setSelection(lastValid.length)
                }
            }

            addTextChangedListener(this)
        }
    }

    constructor(context: Context) : super(context){
        keyListener = DigitsKeyListener.getInstance("0123456789.")
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context,attributeSet){
        context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.AmountFormatEditText,
            0, 0).apply {

            try {
                max = getFloat(R.styleable.AmountFormatEditText_max, 0F)
                min = getFloat(R.styleable.AmountFormatEditText_min, 0F)
                keyListener = DigitsKeyListener.getInstance("0123456789.")

            } finally {
                recycle()
            }
        }
    }

    constructor(context: Context, attributeSet: AttributeSet, int: Int) : super(context,attributeSet,int){
        context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.AmountFormatEditText,
            0, 0).apply {
            try {
                max = getFloat(R.styleable.AmountFormatEditText_max, 0F)
                min = getFloat(R.styleable.AmountFormatEditText_min, 0F)
                keyListener = DigitsKeyListener.getInstance("0123456789.")

            } finally {
                recycle()
            }
        }
    }

    init {
        inputType = InputType.TYPE_CLASS_TEXT  or InputType.TYPE_CLASS_NUMBER
        singleLine = false
        maxLines = 2
        addTextChangedListener(textWatcher)
    }

}
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
import org.jetbrains.anko.sdk25.coroutines.onFocusChange
import org.jetbrains.anko.singleLine


class RegexEditText : EditText{

    var regex : Regex? = null
    var max = 0F
    var min = 0F
    var onValid: ((String) -> Unit)? = null

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {

        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            s.toString().let { str ->
                regex?.let {reg ->
                    removeTextChangedListener(this)

                    if(str.matches(reg))
                    {
                        str.toFloatOrNull()?.let {number ->
                            if(max == 0F || number in min..max)
                            {
                                error = null
                                onValid?.invoke(number.amountFormat())
                            }
                            else
                            {
                                error = context.getString(R.string.amount_limit_no_icon,min.amountFormat(),max.amountFormat())

                            }
                        } ?: run{
                            error = null
                            onValid?.invoke(str)
                        }
                    }
                    else
                    {
                        error = if(str.isNotEmpty())
                            context.getString(R.string.regex_error)
                        else
                            null

                    }
                    addTextChangedListener(this)
                } ?: onValid?.invoke(str)
            }
        }
    }

    constructor(context: Context) : super(context){

    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context,attributeSet){
        context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.RegexEditText,
            0, 0).apply {
            try {
                max = getFloat(R.styleable.RegexEditText_max, 0F)
                min = getFloat(R.styleable.RegexEditText_min, 0F)
                regex = getString(R.styleable.RegexEditText_regex)?.let { if(it.isNotEmpty()) it.toRegex() else null }

            } finally {
                recycle()
            }
        }
    }

    constructor(context: Context, attributeSet: AttributeSet, int: Int) : super(context,attributeSet,int){
        context.theme.obtainStyledAttributes(
            attributeSet,
            R.styleable.RegexEditText,
            0, 0).apply {
            try {
                max = getFloat(R.styleable.RegexEditText_max, 0F)
                min = getFloat(R.styleable.RegexEditText_min, 0F)
                regex = getString(R.styleable.RegexEditText_regex)?.let { if(it.isNotEmpty()) it.toRegex() else null }

            } finally {
                recycle()
            }
        }
    }

    init {
        singleLine = false
        maxLines = 2
        addTextChangedListener(textWatcher)
    }

}
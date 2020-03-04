package com.raer.utils

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import com.google.android.material.textfield.TextInputLayout

class SmartTextInputLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextInputLayout(context, attrs, defStyleAttr) {

    private val scrollView by lazy(LazyThreadSafetyMode.NONE) {
        findParentOfType<ScrollView>() ?: findParentOfType<NestedScrollView>()
    }

    private fun scrollIfNeeded() {
        // Wait a bit (like 10 frames) for other UI changes to happen
        scrollView?.postDelayed({
            scrollView?.scrollDownTo(this)
        },160)
    }

    override fun setError(value: CharSequence?) {
        val changed = error != value

        super.setError(value)

        if (value == null) isErrorEnabled = false

        if (changed) scrollIfNeeded()
    }
}
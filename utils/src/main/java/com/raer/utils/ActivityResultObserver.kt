package com.raer.utils

import android.content.Intent

interface ActivityResultObserver {
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
}
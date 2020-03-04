package com.raer.utils

interface ActivityResultObservable {
    fun addObserver(activityResultObserver: ActivityResultObserver)
    fun removeObserver(activityResultObserver: ActivityResultObserver)
}
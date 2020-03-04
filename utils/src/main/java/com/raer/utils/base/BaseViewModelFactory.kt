package com.raer.utils.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BaseViewModelFactory<VIEWMODEL>(val creator: () -> VIEWMODEL) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VIEWMODEL : ViewModel?> create(modelClass: Class<VIEWMODEL>): VIEWMODEL {
        return creator() as VIEWMODEL
    }
}
package com.ar7lab.cherieapp.base.extension

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * Extension function for casting MutableLiveData to LiveData
 */
fun <T> MutableLiveData<T>.asLiveData() = this as LiveData<T>
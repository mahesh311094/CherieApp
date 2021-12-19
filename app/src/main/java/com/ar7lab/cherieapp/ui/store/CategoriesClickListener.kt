package com.ar7lab.cherieapp.ui.store

import android.view.View
import com.ar7lab.cherieapp.model.Artist

interface CategoriesClickListener {
    fun onClickCategory(view : View,category : String)
}
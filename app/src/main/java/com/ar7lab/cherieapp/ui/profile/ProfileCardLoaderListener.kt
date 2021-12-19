package com.ar7lab.cherieapp.ui.profile

import android.view.View
import com.airbnb.epoxy.EpoxyRecyclerView

interface ProfileCardLoaderListener {
    fun onLoad(rv : EpoxyRecyclerView)
}
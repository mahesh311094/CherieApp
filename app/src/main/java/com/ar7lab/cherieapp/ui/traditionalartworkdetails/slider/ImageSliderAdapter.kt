package com.ar7lab.cherieapp.ui.traditionalartworkdetails.slider

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ar7lab.cherieapp.model.FileUrls
import com.ar7lab.cherieapp.ui.traditionalartworkdetails.TraditionalArtworkDetailsViewModel

class ImageSliderAdapter(fa: FragmentActivity,var currentList : List<FileUrls>) :
    FragmentStateAdapter(fa) {

    var onScaleClick: () -> Unit = {}
    var onLeftClick:() -> Unit={

    }

    var onRightClick:() -> Unit={

    }

    override fun getItemCount() = currentList.size

    override fun createFragment(position: Int) = ImageSliderFragment().apply {
        val numberofImage=(position+1).toString()+"/"+(currentList.size).toString()
        arguments = Bundle().apply {
            putString("url", currentList[position].url)
            putString("number_of_image",numberofImage)
        }

        onScaleClick = this@ImageSliderAdapter.onScaleClick
        onLeftClick=this@ImageSliderAdapter.onLeftClick
        onRightClick=this@ImageSliderAdapter.onRightClick
    }

    companion object {
        var globalDefaultHeight = 0
        var globalState = "collapse"
        var iaFirstTime = true
    }
}
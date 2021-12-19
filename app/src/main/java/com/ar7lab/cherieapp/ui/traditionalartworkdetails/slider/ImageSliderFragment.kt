package com.ar7lab.cherieapp.ui.traditionalartworkdetails.slider

import android.os.Bundle
import android.view.View
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.databinding.FragmentImageSliderBinding
import com.bumptech.glide.Glide

class ImageSliderFragment : BaseFragment(R.layout.fragment_image_slider) {

    private val binding: FragmentImageSliderBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(this)
            .load(requireArguments().getString("url"))
            .placeholder(R.drawable.ic_placeholder)
            .into(binding.ivArt)
        binding.tvCounter.text=requireArguments().getString("number_of_image")

        binding.ivLeftArrow.setOnClickListener {
            onLeftClick()
        }

        binding.ivRightArrow.setOnClickListener {
            onRightClick()
        }
    }

    var onScaleClick : () -> Unit = {

    }

    var onLeftClick:() -> Unit={

    }

    var onRightClick:() -> Unit={

    }

}
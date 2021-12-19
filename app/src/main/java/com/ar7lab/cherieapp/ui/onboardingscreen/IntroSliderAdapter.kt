package com.ar7lab.cherieapp.ui.onboardingscreen

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.databinding.SlideItemContainerBinding
import com.ar7lab.cherieapp.model.Features
import com.ar7lab.cherieapp.utils.imgUrlWithLoader
import com.ar7lab.cherieapp.utils.imgUrlWithLoaderFitXY

/**
 * IntroSliderAdapter for show hold slider items
 * @property introSlides for slider items list
 * */
class IntroSliderAdapter(private val introSlides: List<Features>,private val context: Context) : RecyclerView.Adapter<IntroSliderAdapter.IntroSlideViewHolder>() {
    //holder class
    inner class IntroSlideViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = SlideItemContainerBinding.bind(view)

        //Put slider values
        fun bind(introSlide: Features) {
            binding.imageSlideIcon.imgUrlWithLoaderFitXY(introSlide.fileUrl)
        }
    }

    //Inflate view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroSlideViewHolder {

        return IntroSlideViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.slide_item_container,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: IntroSlideViewHolder, position: Int) {
        //load specific position
        holder.bind(introSlides[position])
    }



    override fun getItemCount(): Int {
        return introSlides.size
    }
}
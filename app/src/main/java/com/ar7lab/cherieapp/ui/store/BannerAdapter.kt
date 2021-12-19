package com.ar7lab.cherieapp.ui.store

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide


class BannerAdapter(val banners : ArrayList<String>) : PagerAdapter() {

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val url = banners[position]
        val imageView = ImageView(collection.context)
        imageView.layoutParams = collection.layoutParams
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        collection.addView(imageView)
        Glide.with(collection.context).load(url).into(imageView)
        return imageView
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View?)
    }

    override fun getCount(): Int {
        return banners.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }
}
package com.ar7lab.cherieapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.utils.EnchantedViewPager
import com.ar7lab.cherieapp.utils.EnchantedViewPagerAdapter
import java.util.*

class EnchantedPagerAdapter(mContext: Context, albumList: ArrayList<Int>) : EnchantedViewPagerAdapter(albumList) {

    var inflater: LayoutInflater = LayoutInflater.from(mContext)
    private val mAlbums: ArrayList<Int> = albumList

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val mCurrentView = inflater.inflate(R.layout.item_view, container, false) as ImageView
        mCurrentView.setImageResource(mAlbums[position])
        mCurrentView.tag = EnchantedViewPager.ENCHANTED_VIEWPAGER_POSITION + position
        container.addView(mCurrentView)
        return mCurrentView
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

}
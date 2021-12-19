package com.ar7lab.cherieapp.widget

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import com.ar7lab.cherieapp.R
import com.google.android.material.imageview.ShapeableImageView

//Custom DotView for the dots on the banner
class DotView : LinearLayout {

    private var dotSize = 30

    private var dots = ArrayList<View>()

    @DrawableRes
    private var selectedResource : Int = 0

    @DrawableRes
    private var unSelectedResource : Int = 0

    private var dotCount  = 0

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView(attrs)
    }

    private fun initView(attrs: AttributeSet?) {

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.DotView)

        dotSize = attributes.getDimensionPixelSize(R.styleable.DotView_dotSize,dotSize)

        selectedResource = attributes.getResourceId(R.styleable.DotView_selectedResource, R.drawable.banner_active)
        unSelectedResource = attributes.getResourceId(R.styleable.DotView_unSelectedResource, R.drawable.banner_inactive )
        dotCount = attributes.getInteger(R.styleable.DotView_dotCount, 0 )

        if(dotCount != 0)
            addDot(dotCount)

        attributes.recycle()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        gravity = Gravity.CENTER
        orientation = HORIZONTAL

        dotSize = resources.getDimension(R.dimen._10sdp).toInt()
    }

    //This will add the total dots as per total size of the banner
    fun addDot(count: Int) {
        removeAllViews()
        dots.clear()
        repeat((1..count).count()) {
            val view = View(context)
            if(it == 0)
                view.setBackgroundResource(selectedResource)
            else
                view.setBackgroundResource(unSelectedResource)

            view.layoutParams = LayoutParams(dotSize, dotSize).apply {
                marginStart = 16
            }
            addView(view)
            dots.add(view)
        }

    }

    //This will change the dots as per banner appear in front
    fun selectDot(position: Int) {
        dots.forEachIndexed { index, view ->
            if (index == position) {
                view.setBackgroundResource(selectedResource)
            } else {
                view.setBackgroundResource(unSelectedResource)
            }
        }
    }
}
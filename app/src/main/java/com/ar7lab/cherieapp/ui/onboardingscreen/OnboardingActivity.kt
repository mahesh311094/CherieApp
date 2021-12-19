package com.ar7lab.cherieapp.ui.onboardingscreen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.databinding.ActivityOnboardingBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.model.Features
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * OnboardingActivity for show onboarding screens
 * @property activity_onboarding is the xml file of this activity
 * */

@AndroidEntryPoint
class OnboardingActivity : BaseActivity() {
    //binding
    private val binding: ActivityOnboardingBinding by viewBinding()
    //viewmodel
    private val viewModel: OnBoardingViewModel by viewModels()
    //item of slider object
    private var featureList: ArrayList<Features> = arrayListOf()
    //adapter object
    private lateinit var introSliderAdapter: IntroSliderAdapter
    //share preference
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    override fun isNeedWindowLightStatusBar() = true

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<OnBoardingViewModel.ViewState> { features ->
        binding.isLoading = features.isLoading

        if (features.isError) {
            showError(features.message)
        }

        features.features?.let {
            featureList.clear()
            featureList = it.map { fea -> fea.copy() } as ArrayList
            initAdapter()
        }

    }

    private fun initAdapter() {
        //adapter for Slides
        introSliderAdapter = IntroSliderAdapter(featureList,applicationContext)
        binding.introSliderViewpager.adapter = introSliderAdapter
        //indicater basci setup method call for init
        setupIndicators(binding.indicatorsContainer)
        //by default first selected indicators
        setCurrentIndicator(0,binding.indicatorsContainer)

        //Navigate to Next pages on buttons click
        binding.btnNext.setOnClickListener {
            //move to next slide condition if finish open Sign In activity
            if (binding.introSliderViewpager.currentItem + 1 < introSliderAdapter.itemCount) {
                binding.introSliderViewpager.currentItem += 1
            } else {
                Intent(applicationContext, SignInActivity::class.java).also { startActivity(it) }
                sharePreferencesManager.introComplete = true
            }
        }
        //skip button clicked start Sign in Activity
        binding.textSkip.setOnClickListener {
            Intent(applicationContext, SignInActivity::class.java).also { startActivity(it) }
            sharePreferencesManager.introComplete = true
        }
        //slider position change listener
        binding.introSliderViewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                //change indicators
                setCurrentIndicator(position,binding.indicatorsContainer)
                //Description value change
                binding.textDescription.text = featureList[position].title
                //Sub title value change
                binding.textSubDescription.text = featureList[position].description
            }
        })

        //viewpager animation added
        binding.introSliderViewpager.setPageTransformer { page, position ->
            val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.margin_extra_small)
            val offsetPx = resources.getDimensionPixelOffset(R.dimen.button_corner_radius)

            page.scaleY = 1 - (0.25f * kotlin.math.abs(position))
            val offset = position * -(2 * offsetPx + pageMarginPx)
            page.translationX = offset
        }
        //load all images other wise animation not showing next images
        binding.introSliderViewpager.offscreenPageLimit=featureList.size-1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)
        viewModel.getFeatures()
    }

    /**
     * Set page indicators
     * @return indicator icons from drawable folder
     * */
    fun setupIndicators(linearLayout: LinearLayout) {
        val indicators = arrayOfNulls<ImageView>(featureList.size)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        layoutParams.setMargins(8, 0, 8, 0)
        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i].apply {
                this?.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.indicator_inactive
                    )
                )
                this?.layoutParams = layoutParams
            }
            linearLayout.addView(indicators[i])
        }
    }

    /**
     * Indicator for which page is your current page
     * @return current indicator
     * */
    private fun setCurrentIndicator(index: Int,linearLayout: LinearLayout) {
        val childCount = linearLayout.childCount
        for (i in 0 until featureList.size) {
            val imageView = linearLayout[i] as ImageView
            if (i == index) {
                imageView.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.indicator_active))
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.indicator_inactive))
            }
        }
    }


}



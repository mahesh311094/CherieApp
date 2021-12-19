package com.ar7lab.cherieapp.ui.chooselanguage

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.databinding.ActivityInitialLanguageBinding
import com.ar7lab.cherieapp.enums.ChooseLanguageEnum
import com.ar7lab.cherieapp.ui.onboardingscreen.OnboardingActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class InitialLanguageActivity : BaseActivity() {

    /**
     * view binding from activity_initial_language.xml
     */
    private val binding: ActivityInitialLanguageBinding by viewBinding()

    private val viewModel: InitialLanguageViewModel by viewModels()

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<InitialLanguageViewModel.ViewState> {
        if (it.isError) {
            showError(it.message)
        }


    }

    override fun isNeedWindowLightStatusBar(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.viewModel = viewModel

        observe(viewModel.stateLiveData, stateObserver)

        if (viewModel.languageChose.get() == ChooseLanguageEnum.JAPANESE.locale)
            viewModel.changeLanguage(ChooseLanguageEnum.JAPANESE)
        else
            viewModel.changeLanguage(ChooseLanguageEnum.ENGLISH)

        binding.btnSave.setOnDebouncedClickListener {
            viewModel.save()
            // language change of this activity and update configuration
            changeLocale(viewModel.languageChose.get()!!)
            Intent(applicationContext, OnboardingActivity::class.java).also { startActivity(it) }
        }

    }

    // change the locale of the app on language change
    private fun changeLocale(language: String) {
        val myLocale = Locale(language)
        val res: Resources = resources
        val dm: DisplayMetrics = res.displayMetrics
        val conf: Configuration = res.configuration
        conf.setLocale(myLocale)
        res.updateConfiguration(conf, dm)
        onConfigurationChanged(conf)
    }
}
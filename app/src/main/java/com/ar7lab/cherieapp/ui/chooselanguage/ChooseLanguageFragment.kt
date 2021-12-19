package com.ar7lab.cherieapp.ui.chooselanguage

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.databinding.FragmentChooseLanguageBinding
import com.ar7lab.cherieapp.enums.ChooseLanguageEnum
import com.ar7lab.cherieapp.ui.dashboard.NotificationRedirectProfileListener
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ClassCastException
import java.util.*

@AndroidEntryPoint
class ChooseLanguageFragment : BaseFragment(R.layout.fragment_choose_language) {

    /**
     * view binding from fragment_choose_language.xml
     */
    private val binding: FragmentChooseLanguageBinding by viewBinding()

    private val viewModel: ChooseLanguageViewModel by viewModels()

    //Language change listener
    lateinit var languageChangeListener: LanguageChangeListener

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<ChooseLanguageViewModel.ViewState> {
        if (it.isError) {
            showError(it.message)
        }
    }

    override fun isNeedWindowLightStatusBar(): Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)

        if (viewModel.languageChose.get() == ChooseLanguageEnum.JAPANESE.locale)
            viewModel.changeLanguage(ChooseLanguageEnum.JAPANESE)
        else
            viewModel.changeLanguage(ChooseLanguageEnum.ENGLISH)

        binding.ivBack.setOnDebouncedClickListener {
            findNavController().popBackStack()
        }

        binding.btnSave.setOnDebouncedClickListener {
            viewModel.save()
            // language change of this fragment and update configuration
            changeLocale(viewModel.languageChose.get()!!)
            // call the language change listener on the language change
            languageChangeListener.onLanguageChange(viewModel.languageChose.get()!!)
            findNavController().popBackStack()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            languageChangeListener = activity as LanguageChangeListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement onSomeEventListener")
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
        activity?.onConfigurationChanged(conf)
    }
}
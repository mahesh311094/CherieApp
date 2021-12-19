package com.ar7lab.cherieapp.ui.currencysettings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.databinding.FragmentChooseCurrencyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChooseCurrencyFragment : BaseFragment(R.layout.fragment_choose_currency) {

    /**
     * view binding from fragment_choose_language.xml
     */
    private val binding: FragmentChooseCurrencyBinding by viewBinding()

    private val viewModel: ChooseCurrencyViewModel by viewModels()

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<ChooseCurrencyViewModel.ViewState> {
        if (it.isError) {
            showError(it.message)
        }

    }

    override fun isNeedWindowLightStatusBar(): Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)

        binding.ivBack.setOnDebouncedClickListener {
            findNavController().popBackStack()
        }

        binding.btnSave.setOnDebouncedClickListener {
            viewModel.save()
            findNavController().popBackStack()
        }
    }

}
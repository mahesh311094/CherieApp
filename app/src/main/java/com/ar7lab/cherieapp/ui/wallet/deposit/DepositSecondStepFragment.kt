package com.ar7lab.cherieapp.ui.wallet.deposit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.base.navigation.NavManager
import com.ar7lab.cherieapp.databinding.FragmentDepositBinding
import com.ar7lab.cherieapp.databinding.FragmentSecondStepDepositBindingImpl
import com.ar7lab.cherieapp.databinding.FragmentTraditionalArtworkDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * this fragment is copy of Deposite fragment in design there two step first step is selection bank type or card
 * if select card than we will fetch card & if select bank than we will fetch it
 * currenly this fragment not used after API don't required we will remove this
 */
@AndroidEntryPoint
class DepositSecondStepFragment : BaseFragment(R.layout.fragment_second_step_deposit) {

    @Inject
    lateinit var navManager: NavManager

    //binding
    private val binding: FragmentSecondStepDepositBindingImpl by viewBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun isNeedWindowLightStatusBar() = true

}
package com.ar7lab.cherieapp.ui.requestcallback

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.databinding.FragmentRequestCallbackBinding
import com.ar7lab.cherieapp.databinding.HelpSupportThankyouAlertBinding
import com.ar7lab.cherieapp.databinding.LackOfBalanceAlertLayoutBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.utils.openInfoDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Fragment for request a callback screen
 * Annotation @AndroidEntryPoint used for Hilt component
 */
@AndroidEntryPoint
class RequestCallBackFragment : BaseFragment(R.layout.fragment_request_callback), HelpSupportCallBackInterface {

    /**
     * View binding from fragment_request_callback.xml
     */
    private val binding: FragmentRequestCallbackBinding by viewBinding()

    /**
     * viewModel
     */
    private val viewModel: RequestCallBackViewModel by viewModels()
    //shareprefrence object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager
    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<RequestCallBackViewModel.ViewState> {
        binding.isSubmitting = it.isSubmitting
        if (it.isError) {
            showError(it.message)

        }
        if(it.isSessionExpired)
        {
            requireContext().openInfoDialog(layoutInflater,
                object : InfoDialogOkayButtonListener {
                    override fun onOkayButtonClicked() {
                        sharePreferencesManager.clearData()
                        startActivity(
                            Intent(requireContext(), SignInActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                    }
                })
        }

        if (it.isSubmitted) {
            findNavController().popBackStack()
        }
    }

    override fun isNeedWindowLightStatusBar(): Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.listener = this
        observe(viewModel.stateLiveData, stateObserver)
        viewModel.init()

        binding.ivBack.setOnDebouncedClickListener {
            findNavController().popBackStack()
        }
    }

    private fun cherieSupportAlertDialog() {
        val bind: HelpSupportThankyouAlertBinding = HelpSupportThankyouAlertBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(bind.root)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.window?.setGravity(Gravity.CENTER);
        alertDialog.show()
        bind.ivClose.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    override fun onSubmitClick() {
        cherieSupportAlertDialog()
        viewModel.submit()
    }
}
package com.ar7lab.cherieapp.ui.resetpassword

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.ResetPasswordSuccessDialogListener
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.databinding.ActivityResetPasswordBinding
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.utils.*
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ResetPasswordActivity : BaseActivity() {

    private val binding: ActivityResetPasswordBinding by viewBinding()

    //Initialize ResetPassword ViewModel
    private val viewModel: ResetPasswordViewModel by viewModels()

    override fun isNeedWindowLightStatusBar() = true

    private var signupType: String = ""
    private var signupEmailMobile: String = ""

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<ResetPasswordViewModel.ViewState> {
        binding.isLoading = it.isLoading
        if (it.isError) {
            showError(it.message)
        }

        //After ResetPassword success it will go to the SignInActivity page
        if (it.isResetPasswordSuccess) {
            showDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)
        viewModel.init()

        getForgetData()

        //Click event for confirm
        binding.btnSubmit.setOnDebouncedClickListener {
            hideKeyboard()
            if (signupType == EMAIL)
                viewModel.resetEmailPassword()
            else
                viewModel.resetMobilePassword()
        }

        //Set the title
        binding.include.ivBack.visibility = View.INVISIBLE
        binding.include.tvTitle.text = getString(R.string.reset_password)

        binding.include.ivBack.setOnDebouncedClickListener {
            hideKeyboard()
            finish()
        }

    }

    private fun getForgetData() {
        intent?.extras?.let {
            signupType = it.getString(SIGNUP_TYPE)!!
            signupEmailMobile = it.getString(SIGNUP_EMAIL_MOBILE)!!

            viewModel.signupEmailMobile = signupEmailMobile
        }
    }

    private fun showDialog() {
        openSuccessDialog(layoutInflater,
                object : ResetPasswordSuccessDialogListener {
                    override fun onCloseButtonClicked() {
                        finish()
                    }
                })
    }
}
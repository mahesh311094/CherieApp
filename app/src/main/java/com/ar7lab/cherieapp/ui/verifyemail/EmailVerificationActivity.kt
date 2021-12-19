package com.ar7lab.cherieapp.ui.verifyemail

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.databinding.ActivityEmailVerificationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmailVerificationActivity : BaseActivity() {

    lateinit var emailAddress: String

    private val binding: ActivityEmailVerificationBinding by viewBinding()
    private val viewModel: EmailVerificationModel by viewModels()

    override fun isNeedWindowLightStatusBar() = true

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<EmailVerificationModel.ViewState> {
        binding.isLoading = it.isLoading
        showError(it.message)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        emailAddress = intent.getStringExtra("email") ?: ""

        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)

        binding.btnResend.setOnDebouncedClickListener {
            hideKeyboard()
            viewModel.resendLink(emailAddress)
        }

        binding.include.tvTitle.text = getString(R.string.sign_up)
        binding.include.ivBack.setOnClickListener { finish() }
    }

}
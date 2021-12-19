package com.ar7lab.cherieapp.ui.forgotpassword

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.databinding.ActivityForgotPasswordBinding
import com.ar7lab.cherieapp.ui.forgetotp.ForgetOtpActivity
import com.ar7lab.cherieapp.ui.signup.CountryPhoneCodeAdapter
import com.ar7lab.cherieapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ForgotPasswordActivity : BaseActivity() {

    private val binding: ActivityForgotPasswordBinding by viewBinding()

    private val viewModel: ForgotPasswordViewModel by viewModels()
    private var forgetType: Boolean = false

    override fun isNeedWindowLightStatusBar() = true

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<ForgotPasswordViewModel.ViewState> {
        binding.isLoading = it.isLoading
        if (it.isError) {
            showError(it.message)
        }

        if (it.isSubmitted) {
            Handler(Looper.getMainLooper()).postDelayed({
                val bundle = Bundle().apply {
                    if (!viewModel.isSignInWithMobile.get()) {
                        putString(SIGNUP_TYPE, EMAIL)
                        putString(SIGNUP_EMAIL_MOBILE, viewModel.email.get().toString())
                    } else {
                        putString(SIGNUP_TYPE, MOBILE)
                        putString(SIGNUP_EMAIL_MOBILE, viewModel.countryPhoneCode.get()!! + "-" + viewModel.contactNo.get()!!)
                        putParcelable(RESEND_TOKEN, viewModel.resendToken)
                        putString(STORED_VERIFICATION_ID, viewModel.storedVerificationId)
                    }
                }

                startActivity(Intent(this@ForgotPasswordActivity, ForgetOtpActivity::class.java).apply {
                    putExtras(bundle)
                    finish()
                })
            }, 2000)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)
        viewModel.init()

        if (intent.hasExtra(FORGET_TYPE)) {
            forgetType = intent.getBooleanExtra(FORGET_TYPE, false)
            viewModel.isSignInWithMobile.set(forgetType)
        }

        binding.include.tvTitle.text = getString(R.string.forget_pass)

        //get country list
        val countries = getCountries()
        //set adapter
        binding.actCountryPhoneCode.setAdapter(CountryPhoneCodeAdapter(this, countries))
        //country code item listener
        binding.actCountryPhoneCode.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                viewModel.setCountryPhoneCode(countries[position].phone) //set country phone code
            }

        //set drop down width
        binding.actCountryPhoneCode.dropDownWidth = (resources.displayMetrics.widthPixels - resources.getDimension(R.dimen.margin_normal) * 2).toInt()

        //Hide keyboard after selecting
        binding.actCountryPhoneCode.setOnClickListener {
            hideKeyboard()
        }

        binding.btnSubmit.setOnDebouncedClickListener {
            hideKeyboard()
            if (!viewModel.isSignInWithMobile.get()) {
                viewModel.submit()
            } else if (viewModel.isSignInWithMobile.get() && viewModel.isMobileNumberValid.get()) {
                viewModel.sendVerificationCode(this)
            }
        }

        //Callback for the OTP send.
        viewModel.callBackOTP(this)

        binding.include.ivBack.setOnDebouncedClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.e("onDestroy onDestroy")
    }
}
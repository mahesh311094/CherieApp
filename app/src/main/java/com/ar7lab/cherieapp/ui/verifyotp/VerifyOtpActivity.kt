package com.ar7lab.cherieapp.ui.verifyotp

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.databinding.ActivityOtpVerificationBinding
import com.ar7lab.cherieapp.enums.AccountTypeEnum
import com.ar7lab.cherieapp.utils.*
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class VerifyOtpActivity : BaseActivity() {

    private val binding: ActivityOtpVerificationBinding by viewBinding()
    private val viewModel: VerifyOtpViewModel by viewModels()

    override fun isNeedWindowLightStatusBar() = true

    private var signupType: String = ""
    private var signupEmailMobile: String = ""
    private var signupPassword: String = ""
    private var storedVerificationId: String = ""

    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<VerifyOtpViewModel.ViewState> {
        binding.isLoading = it.isLoading

        if (it.isError) {
            showError(it.message)
        }

        //After verify the OTP user will redirect to the sign in screen.
        if (it.isVerify) {
            it.message?.let { msg -> showError(msg) }
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 2000)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.viewModel = viewModel
        viewModel.init()
        observe(viewModel.stateLiveData, stateObserver)

        //Set the title
        binding.include.tvTitle.text = getString(R.string.verification_code)
        binding.include.ivBack.visibility = View.INVISIBLE

        viewModel.callBackOTP()
        getSignupData()
        startTimer()

        binding.btnNext.setOnDebouncedClickListener {
            hideKeyboard()
            if (viewModel.isOTPValid.get()) {
                if (signupType == AccountTypeEnum.PERSONAL.name)
                    viewModel.signUpCodeVerify()
                else
                    viewModel.submitCode(this)
            } else {
                showError(getString(R.string.otp_valid))
            }
        }

        binding.tvResend.setOnDebouncedClickListener {
            if (binding.tvResend.text == getString(R.string.resend_code) && signupType == AccountTypeEnum.PERSONAL.name) {
                viewModel.resendCode()
            } else if (binding.tvResend.text == getString(R.string.resend_code) && signupType == AccountTypeEnum.COMPANY.name) {
                viewModel.resendVerificationCode(this, viewModel.signupEmailMobile, viewModel.resendToken)
            }
        }

    }

    private fun getSignupData() {
        intent?.extras?.let {
            signupType = it.getString(SIGNUP_TYPE)!!
            signupEmailMobile = it.getString(SIGNUP_EMAIL_MOBILE)!!
            signupPassword = it.getString(SIGNUP_PASSWORD)!!
            storedVerificationId = it.getString(STORED_VERIFICATION_ID)!!

            if (signupType == AccountTypeEnum.PERSONAL.name)
                viewModel.signupType = EMAIL
            else {
                viewModel.signupType = MOBILE
                resendToken = it.getParcelable(RESEND_TOKEN)!!
                viewModel.resendToken = resendToken
            }

            viewModel.signupEmailMobile = signupEmailMobile
            viewModel.signupPassword = signupPassword
            viewModel.storedVerificationId = storedVerificationId
        }
    }

    private fun startTimer() {
        object : CountDownTimer(60000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
//                binding.tvResend.text = "" + java.lang.String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished), TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)))
                binding.tvResend.text = getString(R.string.resend, TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)))
                binding.tvResend.setTextColor(ContextCompat.getColor(this@VerifyOtpActivity, R.color.philippine_silver))
            }

            override fun onFinish() {
                binding.tvResend.text = getString(R.string.resend_code)
                binding.tvResend.setTextColor(ContextCompat.getColor(this@VerifyOtpActivity, R.color.dove_gray))
            }
        }.start()
    }

}
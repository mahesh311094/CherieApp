package com.ar7lab.cherieapp.ui.forgetotp

import android.content.Intent
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
import com.ar7lab.cherieapp.databinding.ActivityForgetOtpBinding
import com.ar7lab.cherieapp.ui.resetpassword.ResetPasswordActivity
import com.ar7lab.cherieapp.utils.*
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class ForgetOtpActivity : BaseActivity() {

    private val binding: ActivityForgetOtpBinding by viewBinding()
    private val viewModel: ForgetOtpViewModel by viewModels()

    override fun isNeedWindowLightStatusBar() = true

    private var signupType: String = ""
    private var signupEmailMobile: String = ""
    private var storedVerificationId: String = ""

    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<ForgetOtpViewModel.ViewState> {
        binding.isLoading = it.isLoading

        if (it.isError) {
            showError(it.message)
        }

        //After verify the OTP user will redirect to the sign in screen.
        if (it.isVerify) {
            it.message?.let { msg -> showError(msg) }
            Handler(Looper.getMainLooper()).postDelayed({
                val bundle = Bundle().apply {
                    putString(SIGNUP_TYPE, signupType)
                    putString(SIGNUP_EMAIL_MOBILE, signupEmailMobile)
                }

                startActivity(Intent(this@ForgetOtpActivity, ResetPasswordActivity::class.java).apply {
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
        viewModel.init()
        observe(viewModel.stateLiveData, stateObserver)

        //Set the title
        binding.include.tvTitle.text = getString(R.string.verification_code)
        binding.include.ivBack.visibility = View.INVISIBLE

        viewModel.callBackOTP(this)
        getSignupData()
        startTimer()

        binding.btnNext.setOnDebouncedClickListener {
            hideKeyboard()
            if (viewModel.isOTPValid.get()) {
                if (signupType == EMAIL)
                    viewModel.emailCodeVerification()
                else
                    viewModel.submitCode(this)
            } else {
                showError(getString(R.string.otp_valid))
            }
        }

        binding.tvResend.setOnDebouncedClickListener {
            if (binding.tvResend.text == getString(R.string.resend_code) && signupType == EMAIL) {
                viewModel.resendCode()
            } else if (binding.tvResend.text == getString(R.string.resend_code) && signupType == MOBILE) {
                viewModel.resendVerificationCode(this, viewModel.signupEmailMobile, viewModel.resendToken)
            }
        }

    }

    private fun getSignupData() {
        intent?.extras?.let {
            signupType = it.getString(SIGNUP_TYPE)!!
            signupEmailMobile = it.getString(SIGNUP_EMAIL_MOBILE)!!

            if (signupType == MOBILE) {
                storedVerificationId = it.getString(STORED_VERIFICATION_ID)!!
                resendToken = it.getParcelable(RESEND_TOKEN)!!
                viewModel.resendToken = resendToken
                viewModel.storedVerificationId = storedVerificationId
            }

            viewModel.signupType = signupType
            viewModel.signupEmailMobile = signupEmailMobile
        }
    }

    private fun startTimer() {
        object : CountDownTimer(60000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
//                binding.tvResend.text = "" + java.lang.String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished), TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)))
                binding.tvResend.text = getString(R.string.resend, TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)))
                binding.tvResend.setTextColor(ContextCompat.getColor(this@ForgetOtpActivity, R.color.philippine_silver))
            }

            override fun onFinish() {
                binding.tvResend.text = getString(R.string.resend_code)
                binding.tvResend.setTextColor(ContextCompat.getColor(this@ForgetOtpActivity, R.color.dove_gray))
            }
        }.start()
    }

}
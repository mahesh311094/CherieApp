package com.ar7lab.cherieapp.ui.verifyotp

import android.app.Activity
import android.widget.Toast
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.enums.AccountTypeEnum
import com.ar7lab.cherieapp.network.repositories.AuthRepository
import com.ar7lab.cherieapp.ui.forgotpassword.ForgotPasswordViewModel
import com.ar7lab.cherieapp.ui.signup.SignUpViewModel
import com.ar7lab.cherieapp.utils.EMAIL
import com.ar7lab.cherieapp.utils.isValidEmail
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthOptions


@HiltViewModel
internal class VerifyOtpViewModel @Inject constructor(private val authRepository: AuthRepository) : BaseViewModel<VerifyOtpViewModel.ViewState, VerifyOtpViewModel.Action>(ViewState()) {

    val otpView = ObservableField("")
    var isOTPValid = ObservableBoolean(false)

    private lateinit var auth: FirebaseAuth
    var storedVerificationId: String = ""

    var signupType: String = ""
    var signupEmailMobile: String = ""
    var signupPassword: String = ""

    lateinit var resendToken: ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    fun init() {
        auth = FirebaseAuth.getInstance()

        // Observe changes from email input, then validate email
        otpView.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isOTPValid.set(otpView.get()!!.length > 5)
            }
        })
    }

    fun submitCode(activity: Activity) {
        if (isOTPValid.get()) {
            val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(storedVerificationId, otpView.get()!!)
            signInWithPhoneAuthCredential(credential, activity)
            sendAction(Action.StartLoading)
        } else {
            Toast.makeText(activity, "Please enter OTP", Toast.LENGTH_SHORT).show()
        }
    }

    fun signUpCodeVerify() {
        viewModelScope.launch {
            try {
                if (signupType == EMAIL)
                    sendAction(Action.StartLoading)
                val result = if (signupType == EMAIL)
                    authRepository.newSignUpVerifyEmailCode(signupEmailMobile, signupPassword, signupType, otpView.get()!!.toInt())
                else
                    authRepository.newSignUpVerifyMobile(signupEmailMobile, signupPassword, signupType)

                sendAction(Action.LoadingSuccess(result.message))
            } catch (e: Exception) {
                e.printStackTrace()
                sendAction(Action.LoadingFailure(getErrorMessage(e).message))
            }
        }
    }

    /**
     * Network call for resendCode
     * @return code
     * */
    fun resendCode() {
        viewModelScope.launch {
            try {
                sendAction(Action.StartLoading)
                val result = authRepository.newSignUp(signupEmailMobile)
                sendAction(Action.ResendSuccess(result.message))
            } catch (e: Exception) {
                e.printStackTrace()
                sendAction(Action.LoadingFailure(getErrorMessage(e).message))
            }
        }
    }

    fun callBackOTP() {
        // Callback function for Phone Auth
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            }

            override fun onVerificationFailed(e: FirebaseException) {
            }

            override fun onCodeSent(verificationId: String, token: ForceResendingToken) {
                storedVerificationId = verificationId
                resendToken = token
                sendAction(Action.ResendSuccess("A verification code has been sent to $signupEmailMobile"))
            }
        }
    }

    fun resendVerificationCode(activity: Activity, phoneNumber: String, token: ForceResendingToken) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .setForceResendingToken(token) // ForceResendingToken from callbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, activity: Activity) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    signUpCodeVerify()
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        sendAction(Action.LoadingFailure("Invalid OTP"))
                    }
                }
            }
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isLoading: Boolean = true,
        val isError: Boolean = false,
        val isSubmitted: Boolean = false,
        val isVerify: Boolean = false,
        val message: String? = null
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartLoading : Action()
        class LoadingSuccess(val message: String) : Action()
        class ResendSuccess(val message: String) : Action()
        class LoadingFailure(val message: String) : Action()
    }

    // Return a state based on action
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.StartLoading -> state.copy(
            isLoading = true,
            isError = false,
            isSubmitted = false,
            isVerify = false,
            message = null
        )
        is Action.LoadingSuccess -> state.copy(
            isLoading = false,
            isError = false,
            isSubmitted = true,
            isVerify = true,
            message = viewAction.message
        )
        is Action.ResendSuccess -> state.copy(
            isLoading = false,
            isError = false,
            isSubmitted = true,
            isVerify = false,
            message = viewAction.message
        )
        is Action.LoadingFailure -> state.copy(
            isLoading = false,
            isError = true,
            isSubmitted = false,
            isVerify = false,
            message = viewAction.message
        )
    }

}
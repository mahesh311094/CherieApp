package com.ar7lab.cherieapp.ui.forgotpassword

import android.app.Activity
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.network.repositories.AuthRepository
import com.ar7lab.cherieapp.utils.isValidEmail
import com.ar7lab.cherieapp.utils.isValidMobileNumber
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
internal class ForgotPasswordViewModel @Inject constructor(private val authRepository: AuthRepository) : BaseViewModel<ForgotPasswordViewModel.ViewState, ForgotPasswordViewModel.Action>(ViewState()) {

    val email = ObservableField("")
    var isEmailValid = ObservableBoolean(false)
    var isMobileNumberValid = ObservableBoolean(false)
    val isSendClick = ObservableBoolean(false)

    //Check whether show mobile or email.
    var isSignInWithMobile = ObservableBoolean(false)

    val countryPhoneCode = ObservableField("+91")
    var contactNo = ObservableField("")

    private lateinit var auth: FirebaseAuth
    var storedVerificationId: String = ""

    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    fun init() {
        auth = FirebaseAuth.getInstance()

        // Observe changes from email input, then validate email
        email.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isEmailValid.set(isValidEmail(email.get()))
            }
        })

        // Observe changes from mobile number input, then validate mobile number
        contactNo.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isMobileNumberValid.set(isValidMobileNumber(contactNo.get()))
            }
        })
    }

    fun submit() {
        isSendClick.set(true)

        if (!isEmailValid.get()) {
            return
        }

        viewModelScope.launch {
            try {
                // View will show the state loading from this action
                sendAction(Action.StartLoading)
                val response = authRepository.forgotPassword(email.get()!!)
                sendAction(Action.LoadingSuccess(response.message))
            } catch (e: Exception) {
                e.printStackTrace()
                sendAction(Action.LoadingFailure(getErrorMessage(e).message))
            }
        }
    }

    fun callBackOTP(activity: Activity) {
        // Callback function for Phone Auth
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            }

            override fun onVerificationFailed(e: FirebaseException) {
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                storedVerificationId = verificationId
                resendToken = token
                sendAction(Action.LoadingSuccess(activity.getString(R.string.a_verification, countryPhoneCode.get()!! + "-" + contactNo.get()!!)))
            }
        }
    }

    fun sendVerificationCode(activity: Activity) {
        sendAction(Action.StartLoading)
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(countryPhoneCode.get()!! + "-" + contactNo.get()!!) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    //set phone code on click
    fun setCountryPhoneCode(phoneCode: String) {
        countryPhoneCode.set(phoneCode)
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isLoading: Boolean = true,
        val isError: Boolean = false,
        val isSubmitted: Boolean = false,
        val message: String? = null
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartLoading : Action()
        class LoadingSuccess(val message: String) : Action()
        class LoadingFailure(val message: String) : Action()
    }

    // Return a state based on action
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.StartLoading -> state.copy(
            isLoading = true,
            isError = false,
            isSubmitted = false,
            message = null
        )
        is Action.LoadingSuccess -> state.copy(
            isLoading = false,
            isError = false,
            isSubmitted = true,
            message = viewAction.message
        )
        is Action.LoadingFailure -> state.copy(
            isLoading = false,
            isError = true,
            isSubmitted = false,
            message = viewAction.message
        )
    }
}
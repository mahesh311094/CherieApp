package com.ar7lab.cherieapp.ui.signup

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.enums.AccountTypeEnum
import com.ar7lab.cherieapp.network.repositories.AuthRepository
import com.ar7lab.cherieapp.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.widget.CompoundButton
import com.ar7lab.cherieapp.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import timber.log.Timber
import java.util.concurrent.TimeUnit


/**
 * SignUpViewModel view model
 * @property authRepository for signup
 * */
@HiltViewModel
internal class SignUpViewModel @Inject constructor(private val authRepository: AuthRepository) :
    BaseViewModel<SignUpViewModel.ViewState, SignUpViewModel.Action>(ViewState()) {

    var email = ObservableField("")
    var password = ObservableField("")
    var confirmPassword = ObservableField("")
    var userName = ObservableField("")
    var firstName = ObservableField("")
    var lastName = ObservableField("")
    var contactNo = ObservableField("")
    val countryPhoneCode = ObservableField("+93")
    var companyName = ObservableField("")
    var country = ObservableField("")
    var isUserNameValid = ObservableBoolean(false)
    var isFirstNameValid = ObservableBoolean(false)
    var isLastNameValid = ObservableBoolean(false)
    var isCompanyNameValid = ObservableBoolean(false)
    var isEmailValid = ObservableBoolean(false)
    var isMobileNumberValid = ObservableBoolean(false)
    var isPasswordValid = ObservableBoolean(false)
    var isConfirmPasswordValid = ObservableBoolean(false)
    val isSignUpClicked = ObservableBoolean(false)
    val isPasswordMatch = ObservableBoolean(true)
    val isTermsAccept = ObservableBoolean(false)
    val signUpTypeSelected = ObservableField(AccountTypeEnum.PERSONAL)

    private lateinit var auth: FirebaseAuth
    var storedVerificationId: String = ""

    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    /**
     * Function for validation
     * @return Observe changes from username input, then validate inputs
     * */
    fun init() {
        auth = FirebaseAuth.getInstance()

        // Observe changes from username input, then validate username
        userName.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isUserNameValid.set(isValidUserName(userName.get()))
            }
        })
        // Observe changes from first name input, then validate first name
        firstName.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isFirstNameValid.set(isValidFirstName(firstName.get()))
            }
        })
        // Observe changes from last name input, then validate last name
        lastName.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isLastNameValid.set(isValidLastName(lastName.get()))
            }
        })
        // Observe changes from company name input, then validate company name
        companyName.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isCompanyNameValid.set(isValidCompanyName(companyName.get()))
            }
        })
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
        // Observe changes from password input, then validate password
        password.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isPasswordValid.set(isValidPassword(password.get()))
                isPasswordMatch.set(password.get() == confirmPassword.get())
            }
        })
        // Observe changes from confirm password input, then validate confirm password
        confirmPassword.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isConfirmPasswordValid.set(isValidPassword(confirmPassword.get()))
                isPasswordMatch.set(confirmPassword.get() == password.get())
            }
        })
    }

    /**
     * Set country
     * Add [countryCode] for set country
     * @return country code
     * */
    fun setCountry(countryCode: String) {
        country.set(countryCode)
    }

    /**
     * Set country phone code
     * Add [phoneCode] for set country
     * @return country phone code
     * */
    fun setCountryPhoneCode(phoneCode: String) {
        countryPhoneCode.set(phoneCode)
    }

    /**
     * Set type "Company"
     * @return Company
     * */
    fun selectCompany() {
        signUpTypeSelected.set(AccountTypeEnum.COMPANY)
        /**
         * reset all validate messages
         */
        isSignUpClicked.set(false)
    }

    /**
     * Set type "Personal"
     * @return Personal
     * */
    fun selectPersonal() {
        signUpTypeSelected.set(AccountTypeEnum.PERSONAL)
        /**
         * reset all validate messages
         */
        isSignUpClicked.set(false)
    }

    /**
     * Signup fun for "Personal" and "Company" type
     * @return signup API calling
     * */
    fun signUp(activity: Activity) {
        if (signUpTypeSelected.get() == AccountTypeEnum.PERSONAL) {
            newSignUp()
        } else {
            if (!isMobileNumberValid.get() || !isPasswordValid.get() || !isPasswordMatch.get() || !isTermsAccept.get()) {
                if (!isTermsAccept.get())
                    sendAction(Action.ValidationFailed())
                return
            }

            sendVerificationCode(countryPhoneCode.get()!! + "-" + contactNo.get()!!, activity)
            sendAction(Action.StartSignUp)
        }
    }

    /**
     * Network call for Personal Signup
     * @return personal signup
     * */
    private fun signUpPersonal() {
        //Check each input passed validation or not
        isSignUpClicked.set(true)
        if (!isFirstNameValid.get() || !isLastNameValid.get() || !isUserNameValid.get() || !isEmailValid.get() || !isPasswordValid.get() || !isPasswordMatch.get() || !isTermsAccept.get() || !isMobileNumberValid.get()) {
            if (!isTermsAccept.get())
                sendAction(Action.ValidationFailed())
            return
        }

        viewModelScope.launch {
            try {
                sendAction(Action.StartSignUp)
                val result = authRepository.personalSignup(
                    userName.get()!!,
                    email.get()!!,
                    countryPhoneCode.get()!! + "-" + contactNo.get()!!,
                    password.get()!!,
                    AccountTypeEnum.PERSONAL.name,
                    firstName.get()!!,
                    lastName.get()!!,
                    country.get()!!
                )
                sendAction(Action.LoadingSuccess(result.message))
            } catch (e: Exception) {
                e.printStackTrace()
                sendAction(Action.LoadingFailure(getErrorMessage(e).message))
            }
        }
    }

    /**
     * Network call for Signup
     * @return signup
     * */
    private fun newSignUp() {
        //Check each input passed validation or not
        isSignUpClicked.set(true)

        if (!isEmailValid.get() || !isPasswordValid.get() || !isPasswordMatch.get() || !isTermsAccept.get()) {
            if (!isTermsAccept.get())
                sendAction(Action.ValidationFailed())
            return
        }

        viewModelScope.launch {
            try {
                sendAction(Action.StartSignUp)
                val result = authRepository.newSignUp(email.get()!!)
                sendAction(Action.LoadingSuccess(result.message))
            } catch (e: Exception) {
                e.printStackTrace()
                sendAction(Action.LoadingFailure(getErrorMessage(e).message))
            }
        }
    }

    /**
     * Network call for Company Signup
     * @return company signup
     * */
    private fun signUpCompany() {
        isSignUpClicked.set(true)

        if (!isUserNameValid.get() || !isCompanyNameValid.get() || !isEmailValid.get() || !isPasswordValid.get() || !isPasswordMatch.get() || !isTermsAccept.get() || !isMobileNumberValid.get()) {
            if (!isTermsAccept.get())
                sendAction(Action.ValidationFailed())
            return
        }

        viewModelScope.launch {
            try {
                sendAction(Action.StartSignUp)
                val result = authRepository.companySignup(
                    email.get()!!,
                    countryPhoneCode.get()!! + "-" + contactNo.get()!!,
                    password.get()!!,
                    AccountTypeEnum.COMPANY.name,
                    companyName.get()!!,
                    country.get()!!,
                    firstName.get()!!,
                    lastName.get()!!,
                    userName.get()!!
                )
                sendAction(Action.LoadingSuccess(result.message))
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

    private fun sendVerificationCode(number: String, activity: Activity) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun onCheckedChange(button: CompoundButton?, check: Boolean) {
        isTermsAccept.set(check)
    }

    /**
     * Define view state for Activity to use
     * @return state
     * */
    internal data class ViewState(
        val isLoading: Boolean = true,
        val isError: Boolean = false,
        val isTermsNotChecked: Boolean = false,
        val message: String? = null,
        val isSignUpSuccess: Boolean = false
    ) : BaseViewState

    /**
     * Define action of the ViewModel
     * @return action
     * */
    internal sealed class Action : BaseAction {
        object StartSignUp : Action()
        class LoadingSuccess(val message: String) : Action()
        class LoadingFailure(val message: String) : Action()
        class ValidationFailed() : Action()
    }

    /**
     * State based on action
     * @return a state based on action
     * */
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.StartSignUp -> state.copy(
            isLoading = true,
            isError = false,
            message = null,
            isTermsNotChecked = false
        )
        is Action.LoadingSuccess -> state.copy(
            isLoading = false,
            isError = false,
            message = viewAction.message,
            isSignUpSuccess = true,
            isTermsNotChecked = false
        )
        is Action.LoadingFailure -> state.copy(
            isLoading = false,
            isError = true,
            message = viewAction.message,
            isTermsNotChecked = false
        )
        is Action.ValidationFailed -> state.copy(isTermsNotChecked = true, isLoading = false)
    }
}
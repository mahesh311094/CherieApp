package com.ar7lab.cherieapp.ui.signin

import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.SocialMediaTypeEnum
import com.ar7lab.cherieapp.model.User
import com.ar7lab.cherieapp.network.repositories.AuthRepository
import com.ar7lab.cherieapp.network.response.ErrorResponse
import com.ar7lab.cherieapp.utils.isValidEmail
import com.ar7lab.cherieapp.utils.isValidMobileNumber
import com.ar7lab.cherieapp.utils.isValidPassword
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
internal class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sharePreferencesManager: SharePreferencesManager
) :
    BaseViewModel<SignInViewModel.ViewState, SignInViewModel.Action>(ViewState()) {

    var email = ObservableField("")
    var password = ObservableField("")
    var isEmailValid = ObservableBoolean(false)
    var isPasswordValid = ObservableBoolean(false)
    var isLoginClicked = ObservableBoolean(false)
    var contactNo = ObservableField("")
    val countryPhoneCode = ObservableField("+93")
    var isPasswordRequired = ObservableBoolean(false)
    var isSignInWithMobile = ObservableBoolean(false)
    var isMobileNumberValid = ObservableBoolean(false)

    fun init() {
        // Observe changes from email input, then validate email
        email.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isEmailValid.set(isValidEmail(email.get()))
            }
        })

        // Observe changes from password input, then validate password
        password.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isPasswordValid.set(isValidPassword(password.get()))
            }
        })

        // Observe changes from mobile number input, then validate mobile number
        contactNo.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isMobileNumberValid.set(isValidMobileNumber(contactNo.get()))
            }
        })
    }

    fun login() {
        isLoginClicked.set(true)

        if ((isEmailValid.get() && isPasswordValid.get()) || (isMobileNumberValid.get() && isPasswordValid.get())) {
            viewModelScope.launch {
                try {
                    // View will show the state loading from this action
                    sendAction(Action.StartLogin)
                    val result = if (!isSignInWithMobile.get()) {
                        authRepository.login(email.get()!!, password.get()!!, sharePreferencesManager.deviceToken)
                    } else {
                        authRepository.loginWithNumber(countryPhoneCode.get()!! + "-" + contactNo.get()!!, password.get()!!, sharePreferencesManager.deviceToken)
                    }
                    handleAfterLogin(result.data?.token ?: "", result.data?.user, SocialMediaTypeEnum.NONE.name)
                } catch (e: HttpException) {
                    e.printStackTrace()
                    val errorParser = e.response()?.errorBody()?.let {
                        val adapter = Gson().getAdapter(ErrorResponse::class.java)
                        adapter.fromJson(it.string())
                    }
                    sendAction(Action.LoadingFailure(errorParser?.message ?: "", errorParser?.data == null))
                }
            }
        }

    }

    //set phone code on click
    fun setCountryPhoneCode(phoneCode: String) {
        countryPhoneCode.set(phoneCode)
    }

    fun loginWithGoogle(googleId: String, googleEmail: String, firstName: String) {
        viewModelScope.launch {
            try {
                sendAction(Action.StartGoogleLogin)
                val result = authRepository.loginWithGoogle(
                    googleId, googleEmail, firstName,
                    sharePreferencesManager.deviceToken
                )
                // save access token and userId
                handleAfterLogin(result.data?.token ?: "", result.data?.user, SocialMediaTypeEnum.GOOGLE.name)
            } catch (e: Exception) {
                e.printStackTrace()
                sendAction(Action.LoadingFailure(getErrorMessage(e).message))
            }
        }
    }

    fun loginWithFacebook(facebookId: String, facebookEmail: String, firstName: String) {
        viewModelScope.launch {
            try {
                sendAction(Action.StartFacebookLogin)
                val result = authRepository.loginWithFacebook(
                    facebookId, facebookEmail,
                    sharePreferencesManager.deviceToken
                )
                // save access token and userId
                handleAfterLogin(result.data?.token ?: "", result.data?.user, SocialMediaTypeEnum.FACEBOOK.name)
            } catch (e: Exception) {
                e.printStackTrace()
                sendAction(Action.LoadingFailure(getErrorMessage(e).message))
            }
        }
    }

    // Login with kakao
    fun loginWithKakao(serviceId: String, email: String, nickName: String) {
        //send login request to server
        viewModelScope.launch {
            try {
                sendAction(Action.StartKakaoLogin)
                //API calling
                val result = authRepository.loginWithKakao(
                    serviceId, email, nickName,
                    sharePreferencesManager.deviceToken
                )
                // save access token and userId
                handleAfterLogin(result.data?.token ?: "", result.data?.user, SocialMediaTypeEnum.KAKAO.name)
            } catch (e: Exception) {
                e.printStackTrace()
                sendAction(Action.LoadingFailure(getErrorMessage(e).message))
            }
        }
    }

    //Login with line
    fun loginWithLine(serviceId: String, email: String, nickName: String) {
        viewModelScope.launch {
            try {
                sendAction(Action.StartLineLogin)
                //API calling
                val result = authRepository.loginWithLine(
                    serviceId, email, nickName,
                    sharePreferencesManager.deviceToken
                )
                // save access token and userId
                handleAfterLogin(result.data?.token ?: "", result.data?.user, SocialMediaTypeEnum.LINE.name)
            } catch (e: Exception) {
                e.printStackTrace()
                sendAction(Action.LoadingFailure(getErrorMessage(e).message))
            }
        }
    }

    //Check which login is need to show. (With password or only OTP)
    fun getPasswordRequired() {
        isPasswordRequired.set(!isPasswordRequired.get())
    }

    //Check which login type is need to show
    fun getSignInMobile() {
        isSignInWithMobile.set(!isSignInWithMobile.get())
        isPasswordRequired.set(!isSignInWithMobile.get())
    }

    /**
     * Handle the user data after successful login
     */
    private fun handleAfterLogin(token: String, user: User?, loginType: String) {
        sharePreferencesManager.token = token
        sharePreferencesManager.userId = user?.userId ?: ""
        sharePreferencesManager.userName = user?.userName ?: ""
        sharePreferencesManager.firstName = user?.firstName ?: ""
        sharePreferencesManager.lastName = user?.lastName ?: ""
        sharePreferencesManager.companyName = user?.companyName ?: ""
        sharePreferencesManager.userType = user?.accountType?.name ?: ""
        sharePreferencesManager.profileImage = user?.profileImage ?: ""
        sharePreferencesManager.loginType = loginType
        sharePreferencesManager.isKYCCompleted=user?.isKYCCompleted?:false
        sendAction(Action.LoadingSuccess(user?.isFirstTimeLogin ?: false, user))

    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isLoggingIn: Boolean = true,
        val isGoogleLoggingIn: Boolean = true,
        val isFacebookLoggingIn: Boolean = true,
        val isKakaoLoggingIn: Boolean = true,
        val isLineLoggingIn: Boolean = true,
        val isError: Boolean = false,
        val isLoggedIn: Boolean = false,
        val isVerified: Boolean = true,
        val isFirstTimeLogin: Boolean = false,
        val message: String? = null,
        val user: User? = null
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartLogin : Action()
        object StartGoogleLogin : Action()
        object StartFacebookLogin : Action()
        object StartKakaoLogin : Action()
        object StartLineLogin : Action()
        class LoadingSuccess(val isFirstTimeLogin: Boolean, val user: User?) : Action()
        class LoadingFailure(val message: String, val isVerified: Boolean = true) : Action()
    }

    // Return a state based on action
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {

        Action.StartLogin -> state.copy(
            isLoggingIn = true,
            isGoogleLoggingIn = false,
            isFacebookLoggingIn = false,
            isKakaoLoggingIn = false,
            isLineLoggingIn = false,
            isError = false,
            isVerified = true,
            isLoggedIn = false,
            message = null
        )
        Action.StartGoogleLogin -> state.copy(
            isLoggingIn = false,
            isGoogleLoggingIn = true,
            isFacebookLoggingIn = false,
            isKakaoLoggingIn = false,
            isLineLoggingIn = false,
            isError = false,
            isLoggedIn = false,
            message = null
        )
        Action.StartFacebookLogin -> state.copy(
            isLoggingIn = false,
            isGoogleLoggingIn = false,
            isFacebookLoggingIn = true,
            isKakaoLoggingIn = false,
            isLineLoggingIn = false,
            isError = false,
            isLoggedIn = false,
            message = null
        )
        Action.StartKakaoLogin -> state.copy(
            isLoggingIn = false,
            isGoogleLoggingIn = false,
            isFacebookLoggingIn = false,
            isLineLoggingIn = false,
            isKakaoLoggingIn = true,
            isError = false,
            isLoggedIn = false,
            message = null
        )
        Action.StartLineLogin -> state.copy(
            isLoggingIn = false,
            isGoogleLoggingIn = false,
            isFacebookLoggingIn = false,
            isKakaoLoggingIn = false,
            isLineLoggingIn = true,
            isError = false,
            isLoggedIn = false,
            message = null
        )
        is Action.LoadingSuccess -> state.copy(
            isLoggingIn = false,
            isGoogleLoggingIn = false,
            isFacebookLoggingIn = false,
            isKakaoLoggingIn = false,
            isLineLoggingIn = false,
            isError = false,
            isLoggedIn = true,
            message = null,
            isFirstTimeLogin = viewAction.isFirstTimeLogin,
            user = viewAction.user
        )
        is Action.LoadingFailure -> state.copy(
            isLoggingIn = false,
            isGoogleLoggingIn = false,
            isFacebookLoggingIn = false,
            isKakaoLoggingIn = false,
            isLineLoggingIn = false,
            isVerified = viewAction.isVerified,
            isError = true,
            isLoggedIn = false,
            message = viewAction.message
        )
    }
}
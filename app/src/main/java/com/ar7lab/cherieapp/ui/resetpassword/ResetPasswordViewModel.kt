package com.ar7lab.cherieapp.ui.resetpassword

import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.network.repositories.AuthRepository
import com.ar7lab.cherieapp.network.response.ErrorResponse
import com.ar7lab.cherieapp.utils.isValidPassword
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
internal class ResetPasswordViewModel @Inject constructor(private val authRepository: AuthRepository) :
        BaseViewModel<ResetPasswordViewModel.ViewState, ResetPasswordViewModel.Action>
        (ViewState()) {

    var newPassword = ObservableField("")
    var confirmPassword = ObservableField("")
    var isPasswordValid = ObservableBoolean(false)
    val isConfirmClicked = ObservableBoolean(false)

    var isConfirmPasswordValid = ObservableBoolean(false)
    val isPasswordMatch = ObservableBoolean(true)

    var signupEmailMobile: String = ""

    fun init() {
        // Observe changes from password input, then validate password
        newPassword.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isPasswordValid.set(isValidPassword(newPassword.get()))
                isPasswordMatch.set(newPassword.get() == confirmPassword.get())
            }
        })

        // Observe changes from confirm password input, then validate confirm password
        confirmPassword.addOnPropertyChangedCallback(object :
                Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isConfirmPasswordValid.set(isValidPassword(confirmPassword.get()))
                isPasswordMatch.set(confirmPassword.get() == newPassword.get())
            }
        })
    }


    fun resetEmailPassword() {
        isConfirmClicked.set(true)

        if ((isPasswordMatch.get() && isPasswordValid.get() && signupEmailMobile != "")) {
            viewModelScope.launch {
                try {
                    // View will show the state loading from this action
                    sendAction(Action.StartResetPassword)
                    authRepository.resetEmailPassword(signupEmailMobile, newPassword.get()!!)
                    sendAction(Action.LoadingSuccess())
                } catch (e: HttpException) {
                    e.printStackTrace()
                    val errorParser = e.response()?.errorBody()?.let {
                        val adapter = Gson().getAdapter(ErrorResponse::class.java)
                        adapter.fromJson(it.string())
                    }
                    sendAction(Action.LoadingFailure(errorParser?.message ?: ""))
                }
            }
        }

    }

    fun resetMobilePassword() {
        isConfirmClicked.set(true)

        if ((isPasswordMatch.get() && isPasswordValid.get() && signupEmailMobile != "")) {
            viewModelScope.launch {
                try {
                    // View will show the state loading from this action
                    sendAction(Action.StartResetPassword)
                    authRepository.resetMobilePassword(signupEmailMobile, newPassword.get()!!)
                    sendAction(Action.LoadingSuccess())
                } catch (e: HttpException) {
                    e.printStackTrace()
                    val errorParser = e.response()?.errorBody()?.let {
                        val adapter = Gson().getAdapter(ErrorResponse::class.java)
                        adapter.fromJson(it.string())
                    }
                    sendAction(Action.LoadingFailure(errorParser?.message ?: ""))
                }
            }
        }

    }

    /**
     * Define view state for Activity to use
     * @return state
     * */
    internal data class ViewState(
            val isLoading: Boolean = true,
            val isError: Boolean = false,
            val message: String? = null,
            val isResetPasswordSuccess: Boolean = false
    ) : BaseViewState


    /**
     * Define action of the ViewModel
     * @return action
     * */
    internal sealed class Action : BaseAction {
        object StartResetPassword : Action()
        class LoadingSuccess() : Action()
        class LoadingFailure(val message: String) : Action()
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.StartResetPassword -> state.copy(
                isLoading = true,
                isError = false,
                message = null,
        )
        is Action.LoadingFailure -> state.copy(
                isLoading = false,
                isError = true,
                message = viewAction.message,
        )
        is Action.LoadingSuccess -> state.copy(
                isLoading = false,
                isError = false,
                isResetPasswordSuccess = true,
        )

    }
}
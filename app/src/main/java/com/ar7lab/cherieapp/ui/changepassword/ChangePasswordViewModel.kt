package com.ar7lab.cherieapp.ui.changepassword

import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.model.User
import com.ar7lab.cherieapp.network.repositories.AuthRepository
import com.ar7lab.cherieapp.ui.signin.SignInViewModel
import com.ar7lab.cherieapp.utils.isValidEmail
import com.ar7lab.cherieapp.utils.isValidPassword
import com.ar7lab.cherieapp.utils.passwordLengthValidation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.xml.validation.Validator

@HiltViewModel
internal class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sharePreferencesManager: SharePreferencesManager
) : BaseViewModel<ChangePasswordViewModel.ViewState, ChangePasswordViewModel.Action>(ViewState()) {
    //old password observable
    var existingPassword = ObservableField("")

    //New Password Observable
    var newPassword = ObservableField("")

    //confirm password observable
    var confirmPassword = ObservableField("")

    //old password validation check observable
    var isValidExistPassword = ObservableBoolean(false)

    //password validation check observable
    var isPasswordValid = ObservableBoolean(false)

    //Confirm Password validation check observable
    var isValidConfirmPassword = ObservableBoolean(false)
    val isChangeButtonClicked = ObservableBoolean(false)

    fun init() {
        // Observe changes from new password input, then validate new password
        newPassword.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isPasswordValid.set(isValidPassword(newPassword.get()))
            }
        })
        //  Observe changes from confirm password input, then validate cofirm password
        confirmPassword.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isValidConfirmPassword.set(newPassword.get() == confirmPassword.get())
            }
        })
        //  Observe changes from old password input, then validate old password
        existingPassword.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isValidExistPassword.set(isValidPassword(existingPassword.get()))
            }
        })
    }

    //password validation
    fun passwordLenthCondtion(password: String): Boolean {
        return passwordLengthValidation(password)
    }

    //On change password button clicked
    fun changePassword() {
        isChangeButtonClicked.set(true)
        if (!isPasswordValid.get() || !isValidConfirmPassword.get() || !isValidExistPassword.get()) {
            return
        }

        viewModelScope.launch {
            try {
                // View will show the state loading from this action
                sendAction(Action.ChangePassword)
                val result =
                    authRepository.changePassword(existingPassword.get()!!, newPassword.get()!!)
                sendAction(Action.LoadingSuccess(result.status, result.message))
            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }
    }

    //action class for state
    internal sealed class Action : BaseAction {
        object ChangePassword : Action()
        class LoadingSuccess(val status: String, val message: String) : Action()
        class LoadingFailure(val message: String, val isSessinExpired: Boolean) : Action()
    }

    //view state for all
    internal data class ViewState(

        val isError: Boolean = false,
        val isSessinExpired: Boolean = false,
        val status: String? = null,
        val message: String? = null,
        val isLoading: Boolean = false,
        val isSucessfullyChange: Boolean = false
    ) : BaseViewState


    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        //Initialization
        Action.ChangePassword -> state.copy(
            isError = false,
            isSessinExpired = false,
            status = null,
            message = null,
            isLoading = true,
            isSucessfullyChange = false

        )
        //API Response Success
        is Action.LoadingSuccess -> state.copy(
            isError = false,
            isSessinExpired = false,
            status = viewAction.status,
            message = viewAction.message,
            isSucessfullyChange = viewAction.status == "success",
            isLoading = false
        )
        //API Response Fail
        is Action.LoadingFailure -> state.copy(
            isError = true,
            isSessinExpired = viewAction.isSessinExpired,
            message = viewAction.message,
            isLoading = false

        )
    }

}
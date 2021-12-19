package com.ar7lab.cherieapp.ui.verifyemail

import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.network.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class EmailVerificationModel @Inject constructor(private val authRepository: AuthRepository) : BaseViewModel<EmailVerificationModel.ViewState, EmailVerificationModel.Action>(EmailVerificationModel.ViewState()) {

    //Call this function for resend the link of the email verification.
    fun resendLink(email: String) {
        viewModelScope.launch {
            try {
                // View will show the state loading from this action
                sendAction(Action.StartLoading)
                val response = authRepository.resendVerification(email)
                sendAction(Action.LoadingSuccess(response.message))
            } catch (e: Exception) {
                e.printStackTrace()
                sendAction(Action.LoadingFailure(getErrorMessage(e).message))
            }
        }
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
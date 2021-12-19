package com.ar7lab.cherieapp.ui.notificationsetting

import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.model.User
import com.ar7lab.cherieapp.network.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class NotificationSettingViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel<NotificationSettingViewModel.ViewState, NotificationSettingViewModel.Action>(
    ViewState()
){

    fun init(){
        loadData()
    }

    override fun onLoadData() {
        viewModelScope.launch {
            try {
                getUserProfile()
            }catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    private suspend fun getUserProfile(){
        val response = authRepository.getUserProfileDetails()
        sendAction(Action.LoadingSuccessUser(response.data?.user))
    }

    //Update Notification Settings
    fun updateNotificationSettings(isNotice: Boolean, isNews: Boolean, isWork: Boolean, isLikeFollowComment: Boolean, isAssetChange: Boolean){
        viewModelScope.launch {
            try {
                sendAction(Action.StartEditNotification)
                val result = authRepository.notificationSettingsUpdate(
                    isNotice, isNews, isWork, isLikeFollowComment, isAssetChange
                )
                loadData()
                sendAction(Action.LoadingSuccess(result.message))
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }


    // Define view state for Activity to use
    internal data class ViewState(
        val isLoading: Boolean = false,
        val isError: Boolean = false,
        val isSessionExpired: Boolean = false,
        val message: String? = null,
        val user: User? = null,
        val isEditNotificationSuccess: Boolean = false
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartEditNotification : Action()
        class LoadingSuccessUser(val user: User?) : Action()
        class LoadingSuccess(val message: String) : Action()
        class LoadingFailure(val message: String,val isSessionExpired: Boolean) : Action()
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.LoadingSuccessUser -> state.copy(
            isError = false,
            isSessionExpired = false,
            message = null,
            user = viewAction.user
        )
        is Action.StartEditNotification -> state.copy(
            isLoading = true,
            isError = false,
            isSessionExpired = false,
            message = null
        )
        is Action.LoadingSuccess -> state.copy(
            isLoading = false,
            isError = false,
            isSessionExpired = false,
            message = viewAction.message,
            isEditNotificationSuccess = true
        )
        is Action.LoadingFailure -> state.copy(
            isError = true,
            isLoading = false,
            isSessionExpired = viewAction.isSessionExpired,
            message = viewAction.message
        )
    }
}

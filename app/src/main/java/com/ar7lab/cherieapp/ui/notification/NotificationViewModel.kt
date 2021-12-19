package com.ar7lab.cherieapp.ui.notification

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.model.Notifications
import com.ar7lab.cherieapp.network.repositories.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val sharePreferencesManager: SharePreferencesManager
) : BaseViewModel<NotificationViewModel.ViewState, NotificationViewModel.Action>(
    NotificationViewModel.ViewState()
) {

    //Page No
    private var _page = 1

    //Per Page Limit
    private var _limit = 10

    var yesterdayCheck = ObservableField(true)
    var todayCheck = ObservableField(true)

    //Notification list
    private val _notifications = ArrayList<Notifications>()
    private var _filter: String? = null

    fun init() {

        onRefresh()

    }


    fun onRefresh() {

        // reset the page to 1
        _page = 1
        sendAction(Action.StartRefreshing)
        loadData()
    }

    fun loadMore() {
        //Page plus
        _page++
        viewModelScope.launch {
            try {
                getNotificationList()
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    private suspend fun getNotificationList() {

        val response =
            notificationRepository.getNotifications(_page, _limit)

        response.data?.let { data ->
            // if refreshing, clear data first
            if (_page == 1) {
                _notifications.clear()
            }
            _notifications.addAll(data.notificationDetailObj)
            sendAction(Action.LoadingSuccess(_notifications))
        }
    }

    fun notificationMarkAllAsRead(){
        viewModelScope.launch {
            try {
                val result = notificationRepository.setNotificationMakeAllAsRead()
                sendAction(Action.LoadingSuccessMarkAllAsRead(result.message))
                onRefresh()
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    internal data class ViewState(
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = true,
        val isLoadingMore: Boolean = false,
        val isError: Boolean = false,
        val isSessionExpired: Boolean = false,
        val message: String? = null,
        val notificationList: ArrayList<Notifications>? = null,
        val isMarkAllReadSuccess: Boolean = false
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartRefreshing : Action()
        object StartLoadingMore : Action()
        class LoadingSuccessMarkAllAsRead(val message: String) : Action()
        class LoadingSuccess(val notificationList: ArrayList<Notifications>?) : Action()
        class LoadingFailure(val message: String,val isSessionExpired: Boolean) : Action()
    }

    // Return a state based on action
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.StartRefreshing -> state.copy(
            isRefreshing = true,
            isError = false,
            isSessionExpired = false,
            message = null,
        )
        is Action.StartLoadingMore -> state.copy(
            isLoadingMore = true,
        )
        is Action.LoadingSuccessMarkAllAsRead -> state.copy(
            isLoading = false,
            isError = false,
            isSessionExpired = false,
            message = viewAction.message,
            isMarkAllReadSuccess = true
        )
        is Action.LoadingSuccess -> state.copy(
            isRefreshing = false,
            isLoadingMore = false,
            isSessionExpired = false,
            message = null,
            notificationList = viewAction.notificationList,

            )

        is Action.LoadingFailure -> state.copy(
            isRefreshing = false,
            isLoadingMore = false,
            isError = true,
            isSessionExpired = viewAction.isSessionExpired,
            message = viewAction.message,
        )
    }

    override fun onLoadData() {
        super.onLoadData()
        viewModelScope.launch {
            try {
                getNotificationList()
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }
}
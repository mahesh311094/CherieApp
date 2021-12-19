package com.ar7lab.cherieapp.ui.payment.successpayment

import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.network.repositories.PaymentRepository
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SuccessPaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val sharePreferencesManager: SharePreferencesManager
) : BaseViewModel<SuccessPaymentViewModel.ViewState, SuccessPaymentViewModel.Action>(
    ViewState()
){


    internal var art: Art? =null

    override fun onLoadData() {
        viewModelScope.launch {
            sendAction(Action.StartLoading)
            delay(200L)
        }
    }

    fun getArtName():String
    {
        return art?.name?:"";
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isLoading: Boolean = true,
        val isError: Boolean = false,
        val message: String? = null,
        val isDataLoaded: Boolean = false
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartLoading : Action()
        class LoadingSuccess(val isDataLoaded: Boolean) : Action()
        class LoadingFailure(val message: String) : Action()
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.StartLoading -> state.copy(
            isLoading = true,
            isError = false,
            message = null,
            isDataLoaded = false
        )
        is Action.LoadingSuccess -> state.copy(
            isLoading = false,
            isError = false,
            message = null,
            isDataLoaded = viewAction.isDataLoaded
        )
        is Action.LoadingFailure -> state.copy(
            isLoading = false,
            isError = true,
            message = viewAction.message,
            isDataLoaded = false
        )
    }

}
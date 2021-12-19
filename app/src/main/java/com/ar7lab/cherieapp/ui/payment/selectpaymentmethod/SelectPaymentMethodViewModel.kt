package com.ar7lab.cherieapp.ui.payment.selectpaymentmethod

import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.CardDetails
import com.ar7lab.cherieapp.model.PaymentTransactionDetails
import com.ar7lab.cherieapp.network.repositories.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SelectPaymentMethodViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val sharePreferencesManager: SharePreferencesManager
) : BaseViewModel<SelectPaymentMethodViewModel.ViewState, SelectPaymentMethodViewModel.Action>(
    ViewState()
) {

    private val _cards = ArrayList<CardDetails>()
    private var _page = 1
    internal var art: Art? =null
    internal var pieces: Int = 0
    internal var item_price: Double = 0.0

    override fun onLoadData() {
        viewModelScope.launch {
            try {
                loadCards()
                sendAction(Action.LoadingCompletelySuccess(true))
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }

        }
    }

    //Network call for load cards
    private suspend fun loadCards() {
        if (state.isRefreshing) {
            _cards.clear()
        }
        val response = paymentRepository.getCards()
        response?.data?.cardDetails?.let {
            _cards.addAll(it)
        }
        sendAction(Action.LoadingCardSuccess(_cards))

    }

    //Refresh layout calling
    fun onRefresh() {
        _page = 1
        sendAction(Action.StartRefreshing)
        loadData()
    }

    //Create new payment
    fun createPayment(paymentMethodId: String) {
        if (paymentMethodId != "") {
            var total=pieces * item_price
            viewModelScope.launch {
                try {
                    sendAction(Action.StartLoadingCreatePayment)
                    val currency=if(art?.currency.equals("$"))"INR" else "JPY"
                    val result = paymentRepository.paymentThoughCard(
                        art?.id!!,
                        "",
                        "",
                        "",
                        ""
                    )
                    if (result.status == "success") {
                        sendAction(Action.LoadingCreatePaymentSuccess(result.data.paymentTransectionDetail))
                    }else{
                        sendAction(Action.LoadingCreatePaymentFailed(result.data.paymentTransectionDetail))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    val error=getErrorMessage(e)
                    sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
                }
            }
        }
    }

    fun handleSelectCard(cardDetails: CardDetails) {
        val cardDetailsSource = _cards.find { it.id == cardDetails.id }
        cardDetailsSource?.let { card ->
            card.isSelected = !card.isSelected
            _cards.filter { it.id != card.id }.map { it.isSelected = false }
            sendAction(Action.LoadingCardSuccess(_cards))
        }

    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isRefreshing: Boolean = true,
        val isRefreshed: Boolean = false,
        val isLoading: Boolean = true,
        val isLoadingCreatePayment: Boolean = false,
        val isError: Boolean = false,
        val isSessionExpired: Boolean = false,
        val message: String? = null,
        val cards: ArrayList<CardDetails>? = arrayListOf(),
        val paymentTransactionDetails: PaymentTransactionDetails? = null,
        val paymentTransactionDetailsFailed: PaymentTransactionDetails? = null,
        val isDataLoaded: Boolean = false,
        val isHaveData: Boolean = false,
        val isCreatePaymentSuccess: Boolean = false,
        val isCreatePaymentFailed: Boolean = false,
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartRefreshing : Action()
        object StartLoading : Action()
        object StartLoadingCreatePayment : Action()
        class LoadingCardSuccess(val cards: ArrayList<CardDetails>) : Action()
        class LoadingSuccess(val isDataLoaded: Boolean) : Action()
        class LoadingCreatePaymentSuccess(val paymentTransactionDetails: PaymentTransactionDetails) : Action()
        class LoadingCreatePaymentFailed(val paymentTransactionDetailsFailed: PaymentTransactionDetails) : Action()
        class LoadingFailure(val message: String,val isSessionExpired: Boolean) : Action()
        class LoadingCompletelySuccess(val isRefreshed: Boolean = false) : Action()
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.StartRefreshing -> state.copy(
            isRefreshing = true,
            isError = false,
            isSessionExpired = false,
            message = null,
            isDataLoaded = false,
            isRefreshed = false,
        )
        is Action.StartLoading -> state.copy(
            isLoading = true,
            isError = false,
            isSessionExpired = false,
            message = null,
            isDataLoaded = false
        )
        is Action.StartLoadingCreatePayment -> state.copy(
            isLoadingCreatePayment = true,
            isError = false,
            isSessionExpired = false,
            message = null,
        )
        is Action.LoadingCardSuccess -> state.copy(
            cards = viewAction.cards,
            isHaveData = viewAction.cards.isNotEmpty()
        )
        is Action.LoadingSuccess -> state.copy(
            isLoading = false,
            isError = false,
            isSessionExpired = false,
            message = null,
            isDataLoaded = viewAction.isDataLoaded
        )
        is Action.LoadingCreatePaymentSuccess -> state.copy(
            isLoading = false,
            isLoadingCreatePayment = false,
            isError = false,
            isSessionExpired = false,
            paymentTransactionDetails = viewAction.paymentTransactionDetails,
            isCreatePaymentSuccess = true,
            isCreatePaymentFailed = false
        )
        is Action.LoadingCreatePaymentFailed -> state.copy(
            isLoading = false,
            isLoadingCreatePayment = false,
            isError = false,
            isSessionExpired = false,
            paymentTransactionDetailsFailed = viewAction.paymentTransactionDetailsFailed,
            isCreatePaymentSuccess = false,
            isCreatePaymentFailed = true
        )
        is Action.LoadingCompletelySuccess -> state.copy(
            isRefreshing = false,
            isDataLoaded = true,
            isRefreshed = viewAction.isRefreshed,
            isError = false,
            isSessionExpired = false,
            message = null
        )
        is Action.LoadingFailure -> state.copy(
            isRefreshing = false,
            isLoading = false,
            isError = true,
            isSessionExpired = viewAction.isSessionExpired,
            message = viewAction.message,
            isDataLoaded = false
        )
    }
}
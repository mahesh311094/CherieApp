package com.ar7lab.cherieapp.ui.payment.selectpayment

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.databinding.BankPaymentDoneLayoutBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.CheckoutTypeEnum
import com.ar7lab.cherieapp.model.BankDetails
import com.ar7lab.cherieapp.model.CardDetails
import com.ar7lab.cherieapp.network.repositories.PaymentRepository
import com.ar7lab.cherieapp.network.repositories.WalletRepository
import com.ar7lab.cherieapp.network.response.AddCardResponse
import com.ar7lab.cherieapp.network.response.GetWalletBalanceResponse
import com.ar7lab.cherieapp.ui.wallet.WalletViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SelectPaymentViewModel @Inject constructor(
    private val sharePreferencesManager: SharePreferencesManager,
    private val paymentRepository: PaymentRepository,
    private val walletRepository: WalletRepository,
) : BaseViewModel<SelectPaymentViewModel.ViewState, SelectPaymentViewModel.Action>(ViewState()) {

    //Tab Selection Variable
    val typeSelected = ObservableField(CheckoutTypeEnum.CHERIE)

    //bank transfer detail save
    private val _bankDetails = ArrayList<BankDetails>()

    //Store the cards in list
    private val _cards = ArrayList<CardDetails>()

    //selected card position
    val _selected_card_position = ObservableInt(-1)

    var cardStatus: String? = null
    var bankStatus: String? = null

    var result = AddCardResponse("error", "")

    //userbalance fetching from server
    var userBalance: GetWalletBalanceResponse.Assets? = null

    //selected bank position
    val _selected_bank_position = ObservableInt(-1)

    init {
        loadData()
    }

    var numberOfPeas = ObservableField("1")

    //select card radio button with method will called
    fun selectedCardPosition(position: Int) {
        _selected_card_position.set(position)
        _selected_bank_position.set(-1)
        sendAction(Action.LoadingCardSuccess(_cards))
    }

    //get Card position is selected
    fun isCardPositionSelected(position: Int): Boolean {
        return _selected_card_position.get() == position
    }

    //get bank position is selected
    fun isBankPositionSelected(position: Int): Boolean {
        return _selected_bank_position.get() == position
    }

    //on bank item radio button clicked save selected position
    fun selectedBankPosition(position: Int) {
        _selected_card_position.set(-1)
        _selected_bank_position.set(position)
        sendAction(Action.LoadingBankDetailSuccess(_bankDetails))
    }

    //load bank details api calling
    private suspend fun loadBankDetails() {
        val result = paymentRepository.getBanksDetailList()
        result?.data?.bankDetails?.let {
            _bankDetails.addAll(it)
        }

        sendAction(Action.LoadingBankDetailSuccess(_bankDetails))
    }

    //Network call for load cards api calling
    private suspend fun loadCards() {
        val response = paymentRepository.getCards()
        response.data?.cardDetails?.let {
            _cards.clear()
            _cards.addAll(it)
        }
        sendAction(Action.LoadingCardSuccess(_cards))

    }

    //Network call for payment through the bank
    fun paymentThroughCard(cardDetailsId: String, artId: String, totalPrice: String){
        viewModelScope.launch {

            try {
                val result = paymentRepository.paymentThoughCard(
                    artId, totalPrice, "INR", cardDetailsId, "Testing"
                )

                if(result.status.equals("success")){
                    cardStatus = result.status
                    sendAction(Action.CardPaymentSuccess(cardStatus!!))
                }


            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, false))
            }
        }
    }

    //Network call for payment through the Cherie wallet
    fun paymentThroughCherieWallet(artId: String, artShare: String, totalPrice: String, salesDateId: String){
        viewModelScope.launch {

            try {
                result = paymentRepository.paymentThroughCherieWallet(
                    artId, artShare, totalPrice, salesDateId, "$"
                )
                sendAction(Action.CheriePaymentSuccess(result.status))

            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.CheriePaymentSuccess(cherieStatus = result.status))
            }
        }
    }

    fun getUserTotalBalance(): Int {
        var total: Int = 0
        if (userBalance != null) {
            if (!userBalance?.walletBalance?.fiat.isNullOrEmpty()) {
                total += userBalance?.walletBalance?.fiat?.toInt() ?: 0
            }

        }
        return total
    }

    //wallet balance getting API
    suspend fun getWalletBalance() {
        sendAction(Action.LoadingStarting)
        try {
            val response = walletRepository.getUserBalance()
            userBalance = response.data?.assets
            sendAction(Action.WalletBallanceSuccess(response.data?.assets))
        } catch (e: Exception) {
            val error = getErrorMessage(e)
            sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
        }

    }

    override fun onLoadData() {
        //load data from API
        viewModelScope.launch {
            try {
                sendAction(Action.LoadingStarting)
                getWalletBalance()
                loadBankDetails()
                loadCards()
            } catch (e: Exception) {
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isError: Boolean = false,
        val message: String? = null,
        val isRefreshed: Boolean = false,
        val isLoading: Boolean = false,
        val cardStatus: String? = null,
        val cherieStatus: String? = null,
        val isSessionExpired: Boolean = false,
        val userBalance: GetWalletBalanceResponse.Assets? = null,
        val typeSelected: CheckoutTypeEnum = CheckoutTypeEnum.CHERIE,
        val isDataLoaded: Boolean = false,
        val bankDetails: ArrayList<BankDetails>? = arrayListOf(),
        val cards: ArrayList<CardDetails>? = arrayListOf()
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object LoadingStarting : SelectPaymentViewModel.Action()
        object StartLoading : Action()
        class LoadingSuccess(val isDataLoaded: Boolean) : Action()
        class CardPaymentSuccess(val cardStatus: String) : Action()
        class CheriePaymentSuccess(val cherieStatus: String) : Action()
        class WalletBallanceSuccess(val userBalance: GetWalletBalanceResponse.Assets?) : Action()
        class TypeChanged(val depositeSelected: CheckoutTypeEnum) : SelectPaymentViewModel.Action()
        class LoadingBankDetailSuccess(val bankDetails: ArrayList<BankDetails>) : SelectPaymentViewModel.Action()
        class LoadingFailure(val message: String, val isSessionExpired: Boolean) : SelectPaymentViewModel.Action()
        class LoadingCardSuccess(val cards: ArrayList<CardDetails>) : SelectPaymentViewModel.Action()
    }


    // Return a state based on action
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.LoadingFailure -> state.copy(
            isError = true,
            message = viewAction.message,
            isRefreshed = false,
            isLoading = false,
            cardStatus = null,
            cherieStatus = null,
            isSessionExpired = viewAction.isSessionExpired
        )
        is Action.StartLoading-> state.copy(
            isLoading = true,
            cherieStatus = null,
            cardStatus = null
        )
        is Action.LoadingStarting -> state.copy(
            isLoading = true,
            cherieStatus = null,
            cardStatus = null
        )
        is Action.LoadingBankDetailSuccess -> state.copy(
            bankDetails = viewAction.bankDetails,
            isLoading = false,
            isRefreshed = false,
            cardStatus = null,
            cherieStatus = null
        )
        is Action.TypeChanged -> state.copy(typeSelected = viewAction.depositeSelected,
            cardStatus = null,
            cherieStatus = null)
        is Action.LoadingCardSuccess -> state.copy(
            cards = viewAction.cards,
            isError = false,
            isLoading = true,
            cardStatus = null,
            cherieStatus = null
        )
        is Action.LoadingSuccess -> state.copy(
            isLoading = false,
            isError = false,
            message = null,
            cardStatus = null,
            cherieStatus = null,
            isDataLoaded = viewAction.isDataLoaded
        )
        is Action.CardPaymentSuccess -> state.copy(
            isLoading = false,
            isError = false,
            message = null,
            cherieStatus = null,
            cardStatus = viewAction.cardStatus
        )
        is Action.CheriePaymentSuccess -> state.copy(
            isLoading = false,
            isError = false,
            message = null,
            cardStatus = null,
            cherieStatus = viewAction.cherieStatus
        )
        is Action.WalletBallanceSuccess -> state.copy(
            userBalance = viewAction.userBalance,
            isError = false,
            isRefreshed = false,
            isLoading = false

        )
    }

    //On click on any Type
    fun onSelectType(walletTypeEnum: CheckoutTypeEnum) {
        if (walletTypeEnum == typeSelected.get()) return
        typeSelected.set(walletTypeEnum)
        _selected_card_position.set(-1)
        _selected_bank_position.set(-1)
        sendAction(Action.TypeChanged(walletTypeEnum))
    }

    //refresh all data
    fun onRefresh() {
        _bankDetails.clear()
        _cards.clear()
        onLoadData()
    }
}
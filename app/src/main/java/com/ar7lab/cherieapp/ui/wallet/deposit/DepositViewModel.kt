package com.ar7lab.cherieapp.ui.wallet.deposit

import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.DepositeTypeEnum
import com.ar7lab.cherieapp.model.BankDetails
import com.ar7lab.cherieapp.model.CardDetails
import com.ar7lab.cherieapp.network.repositories.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class DepositViewModel @Inject constructor(
    private val sharePreferencesManager: SharePreferencesManager,
    private val paymentRepository: PaymentRepository,
) : BaseViewModel<DepositViewModel.ViewState, DepositViewModel.Action>(ViewState()) {
    //payment type save variable
    val typeSelected = ObservableField(DepositeTypeEnum.BANK)

    //bank transfer detail save
    private val _bankDetails = ArrayList<BankDetails>()

    //Store the cards in list
    private val _cards = ArrayList<CardDetails>()

    //selected card position
    val _selected_card_position = ObservableInt(-1)

    //selected bank position
    val _selected_bank_position = ObservableInt(-1)

    //selected amount
    val _selected_amount = ObservableField(0)

    init {
        loadData()
    }

    //50 button clicked set value
    fun on50ButtonClicked() {
        var value: Int = _selected_amount.get()!!
        value += 50
        _selected_amount.set(value)
        sendAction(Action.TypeChanged(typeSelected.get()!!))
    }

    //100 button clicked set value
    fun on100ButtonClicked() {
        var value: Int = _selected_amount.get()!!
        value += 100
        _selected_amount.set(value)
        sendAction(Action.TypeChanged(typeSelected.get()!!))
    }

    //300 button clicked set value
    fun on300ButtonClicked() {
        var value: Int = _selected_amount.get()!!
        value += 300
        _selected_amount.set(value)
        sendAction(Action.TypeChanged(typeSelected.get()!!))
    }

    //500 button clicked set value
    fun on500ButtonClicked() {
        var value: Int = _selected_amount.get()!!
        value += 500
        _selected_amount.set(value)
        sendAction(Action.TypeChanged(typeSelected.get()!!))
    }

    //1000 button clicked set value
    fun on1000ButtonClicked() {
        var value: Int = _selected_amount.get()!!
        value += 1000
        _selected_amount.set(value)
        sendAction(Action.TypeChanged(typeSelected.get()!!))
    }

    //3000 button clicked set value
    fun on3000ButtonClicked() {
        var value: Int = _selected_amount.get()!!
        value += 3000
        _selected_amount.set(value)
        sendAction(Action.TypeChanged(typeSelected.get()!!))
    }

    //on reset button clicked
    fun onResetButtonClicked() {
        _selected_amount.set(0)
        sendAction(Action.TypeChanged(typeSelected.get()!!))
    }

    //get Selected Amount
    fun getSelectedAmount(): Int {
        return _selected_amount.get()!!
    }

    override fun onLoadData() {
        //load data from API
        viewModelScope.launch {
            try {
                sendAction(Action.LoadingStarting)
                loadBankDetails()
                loadCards()
            } catch (e: Exception) {
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }
    }

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

    fun sendCardTrasferReqest(card: CardDetails, amount: Int) {
        viewModelScope.launch {
            try {

                sendAction(Action.LoadingStarting)
                val result = paymentRepository.cardDeposit(
                    amount,
                    "INR",
                    card.id,
                    "Deposite amount:- " + amount
                )
                sendAction(Action.LoadingCardTranferSuccess(result.message, result.status))
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
        val status: String? = null,
        val isRefreshed: Boolean = false,
        val isLoading: Boolean = false,
        val isCardTranferSuccess: Boolean = false,
        val isSessionExpired: Boolean = false,
        val typeSelected: DepositeTypeEnum = DepositeTypeEnum.BANK,
        val bankDetails: ArrayList<BankDetails>? = arrayListOf(),
        val cards: ArrayList<CardDetails>? = arrayListOf(),
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object LoadingStarting : Action()
        class TypeChanged(val depositeSelected: DepositeTypeEnum) : Action()
        class LoadingBankDetailSuccess(val bankDetails: ArrayList<BankDetails>) : Action()
        class LoadingFailure(val message: String, val isSessionExpired: Boolean) : Action()
        class LoadingCardSuccess(val cards: ArrayList<CardDetails>) : Action()
        class LoadingCardTranferSuccess(val message: String, val status: String) : Action()

    }

    //On click on any Type bank or card
    fun onSelectType(depositeSelected: DepositeTypeEnum) {
        if (depositeSelected == typeSelected.get()) return
        typeSelected.set(depositeSelected)
        _selected_card_position.set(-1)
        _selected_bank_position.set(-1)
        sendAction(Action.TypeChanged(depositeSelected))
    }

    //refresh all data
    fun onRefresh() {
        _bankDetails.clear()
        _cards.clear()
        onLoadData()
    }

    // Return a state based on action
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.LoadingFailure -> state.copy(
            isError = true,
            message = viewAction.message,
            isRefreshed = false,
            isLoading = false,
            isCardTranferSuccess = false,
            isSessionExpired = viewAction.isSessionExpired
        )
        is Action.LoadingStarting -> state.copy(
            isLoading = true,
        )
        is Action.LoadingBankDetailSuccess -> state.copy(
            bankDetails = viewAction.bankDetails,
            isLoading = false,
            isRefreshed = false
        )
        is Action.TypeChanged -> state.copy(typeSelected = viewAction.depositeSelected)
        is Action.LoadingCardSuccess -> state.copy(
            cards = viewAction.cards,
            isError = false
        )
        is Action.LoadingCardTranferSuccess -> state.copy(
            isCardTranferSuccess = true,
            status = viewAction.status,
            isLoading = false,
            isRefreshed = false
        )
    }
}
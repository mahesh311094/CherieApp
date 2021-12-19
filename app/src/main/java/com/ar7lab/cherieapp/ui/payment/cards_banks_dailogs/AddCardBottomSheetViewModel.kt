package com.ar7lab.cherieapp.ui.payment.cards_banks_dailogs

import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.*
import com.ar7lab.cherieapp.model.WalletPurchaseDetail
import com.ar7lab.cherieapp.network.repositories.PaymentRepository
import com.ar7lab.cherieapp.network.repositories.WalletRepository
import com.ar7lab.cherieapp.network.response.GetWalletBalanceResponse
import com.ar7lab.cherieapp.ui.payment.addnewcard.AddNewCardViewModel
import com.ar7lab.cherieapp.ui.profile.ProfileViewModel
import com.ar7lab.cherieapp.utils.isCardHolderNameValid
import com.ar7lab.cherieapp.utils.isValidCVC
import com.ar7lab.cherieapp.utils.isValidCard
import com.ar7lab.cherieapp.utils.isValidCardExpiry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class AddCardBottomSheetViewModel @Inject constructor(
    private val sharePreferencesManager: SharePreferencesManager,
    private val paymentRepository: PaymentRepository,
) : BaseViewModel<AddCardBottomSheetViewModel.ViewState, AddCardBottomSheetViewModel.Action>(ViewState()) {
    //Tab Selection Variable
    val cardNumber = ObservableField("")
    val expiredMonthYear = ObservableField("")
    val cvvNo = ObservableField("")
    val cardHolderName = ObservableField("")
    val isCardNoValid=ObservableBoolean(false);
    val isCardExpiredValid=ObservableBoolean(false);
    val isCvvNoValid=ObservableBoolean(false);
    val isCardHolderValid=ObservableBoolean(false);
    val defaultCard = ObservableBoolean(false)
    val isSaveButtonClicked = ObservableBoolean(false)



    init {
        loadData()
        cardNumber.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isCardNoValid.set(isValidCard(cardNumber.get()))
            }
        })
        expiredMonthYear.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isCardExpiredValid.set(isValidCardExpiry(expiredMonthYear.get()))
            }
        })
        cvvNo.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isCvvNoValid.set(isValidCVC(cvvNo.get()))
            }
        })
        cardHolderName.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isCardHolderValid.set(isCardHolderNameValid(cardHolderName.get()))
            }
        })
    }

    fun onDefaultCardClicked()
    {
        defaultCard.set(!defaultCard.get()!!)
    }

    override fun onLoadData() {
        //load data from API
        viewModelScope.launch {
            try {

            } catch (e: Exception) {
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }
    }

    fun onSaveButtonClicked()
    {
        isSaveButtonClicked.set(true)
        if (!isCardNoValid.get() || !isCardExpiredValid.get() || !isCvvNoValid.get() || !isCardHolderValid.get())
            return
        val tempExpired:String=expiredMonthYear.get()?:""
        val monthYear: List<String> = tempExpired.split("/")
        val cardNo=cardNumber.get()?:""
        viewModelScope.launch {
            try {
               sendAction(Action.LoadingStarting)
                val result = paymentRepository.addCard(
                    cardNumber.get()!!,
                    monthYear[0],
                    monthYear[1],
                    cvvNo.get()!!
                )
                sendAction(Action.LoadingSuccess(result.message))
            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }

    }


    // Define view state for Activity to use
    internal data class ViewState(
        val isError: Boolean = false,
        val message: String? = null,
        val isLoading: Boolean = false,
        val isSessionExpired: Boolean = false,
        val isSucess: Boolean = false,
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object LoadingStarting : Action()
        class LoadingFailure(val message: String, val isSessionExpired: Boolean) : Action()
        class LoadingSuccess(val message: String) : Action()

    }

    fun onRefresh()
    {

    }

    // Return a state based on action
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.LoadingFailure -> state.copy(
            isError = true,
            message = viewAction.message,
            isLoading = false,
            isSessionExpired = viewAction.isSessionExpired
        )
        is Action.LoadingStarting -> state.copy(
            isLoading = true,
        )
        is Action.LoadingSuccess -> state.copy(
            isError = false,
            message = viewAction.message,
            isLoading = false,
            isSessionExpired=false,
            isSucess=true
        )

    }
}
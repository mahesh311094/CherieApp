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
import com.ar7lab.cherieapp.ui.signup.SignUpViewModel
import com.ar7lab.cherieapp.utils.isAccountNumberValid
import com.ar7lab.cherieapp.utils.isValidCountryName
import com.ar7lab.cherieapp.utils.isValidUserName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class AddBankBottomSheetViewModel @Inject constructor(
    private val sharePreferencesManager: SharePreferencesManager,
    private val paymentRepository: PaymentRepository,
) : BaseViewModel<AddBankBottomSheetViewModel.ViewState, AddBankBottomSheetViewModel.Action>(ViewState()) {
    //Tab Selection Variable
    val country = ObservableField("")
    val bankName = ObservableField("")
    val accountHolderName = ObservableField("")
    val accountNumber = ObservableField("")
    val isSaveButtonClicked = ObservableBoolean(false)
    val isContryValid = ObservableBoolean(false)
    val isBankNameValid = ObservableBoolean(false)
    val isAccountHolderValid = ObservableBoolean(false)
    val isAccountNumberValid = ObservableBoolean(false)
    val defaultCard = ObservableBoolean(false)




    init {
        loadData()
        country.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isContryValid.set(isValidCountryName(country.get()))
            }
        })
        bankName.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isBankNameValid.set(isValidCountryName(bankName.get()))
            }
        })
        accountHolderName.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isAccountHolderValid.set(isValidCountryName(accountHolderName.get()))
            }
        })
        accountNumber.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isAccountNumberValid.set(isAccountNumberValid(accountNumber.get()))
            }
        })
    }


    fun onDefaultCardClicked()
    {
        defaultCard.set(!defaultCard.get()!!)
    }

    fun onSaveButtonClicked()
    {
        isSaveButtonClicked.set(true)
        if (!isContryValid.get() || !isBankNameValid.get() || !isAccountHolderValid.get() || !isAccountNumberValid.get())
            return
        viewModelScope.launch {
            try {
                sendAction(Action.LoadingStarting)
                val result=paymentRepository.addBank(sharePreferencesManager.userId,country.get()!!,bankName.get()!!,accountHolderName.get()!!,accountNumber.get()!!,defaultCard.get())
                sendAction(Action.LoadingSuccess(result.message))
            } catch (e: Exception) {
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }

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





    // Define view state for Activity to use
    internal data class ViewState(
        val isError: Boolean = false,
        val message: String? = null,
        val isLoading: Boolean = false,
        val isSucess: Boolean = false,
        val isSessionExpired: Boolean = false,
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object LoadingStarting : Action()
        class LoadingSuccess(val message: String) : Action()
        class LoadingFailure(val message: String,val isSessionExpired: Boolean) : Action()
    }


    fun onRefresh()
    {

    }

    // Return a state based on action
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.LoadingStarting -> state.copy(
            isError = false,
            message = null,
            isLoading = true,
            isSessionExpired=false,
            isSucess=false
        )
        is Action.LoadingSuccess -> state.copy(
            isError = false,
            message = viewAction.message,
            isLoading = false,
            isSessionExpired=false,
            isSucess=true
        )
        is Action.LoadingFailure -> state.copy(
            isError = true,
            message = viewAction.message,
            isLoading = false,
            isSessionExpired=viewAction.isSessionExpired,
            isSucess=false
        )


    }
}
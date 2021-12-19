package com.ar7lab.cherieapp.ui.payment.addnewcard

import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.network.repositories.PaymentRepository
import com.ar7lab.cherieapp.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class AddNewCardViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val sharePreferencesManager: SharePreferencesManager
    ): BaseViewModel<AddNewCardViewModel.ViewState, AddNewCardViewModel.Action>(
    ViewState()
){

    var cardNumber = ObservableField("")
    var cardExpMonthYear = ObservableField("")
    var cardExpYear = ObservableField("")
    var cardCVC = ObservableField("")
    var isCardValid = ObservableBoolean(false)
    var isCardExpiryValid = ObservableBoolean(false)
    var isCVCValid = ObservableBoolean(false)
    var isSaveClicked = ObservableBoolean(false)

    // Observe changes from card input, then validate card
    fun init() {
        cardNumber.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isCardValid.set(isValidCard(cardNumber.get()))
            }
        })
        cardExpMonthYear.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isCardExpiryValid.set(isValidCardExpiry(cardExpMonthYear.get()))
            }
        })
        cardCVC.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isCVCValid.set(isValidCVC(cardCVC.get()))
            }
        })
    }

    //Network call for add new card
    fun addNewCard(){
        isSaveClicked.set(true)
        if(!isCardValid.get() || !isCardExpiryValid.get() || !isCVCValid.get()){
            return
        }
        val monthYear: List<String> = cardExpMonthYear.get()!!.split("/")
        viewModelScope.launch {
            try {
                sendAction(Action.StartAddCard)
                val result = paymentRepository.addCard(cardNumber.get()!!, monthYear[0], monthYear[1],cardCVC.get()!!)
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
        val isLoading: Boolean = true,
        val isError: Boolean = false,
        val isSessionExpired: Boolean = false,
        val message: String? = null,
        val isAccCardSuccess: Boolean = false
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartAddCard : Action()
        class LoadingSuccess(val message: String) : Action()
        class LoadingFailure(val message: String,val isSessionExpired: Boolean) : Action()
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.StartAddCard -> state.copy(
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
            isAccCardSuccess = true
        )
        is Action.LoadingFailure -> state.copy(
            isLoading = false,
            isSessionExpired = viewAction.isSessionExpired,
            message = viewAction.message,
        )
    }

}
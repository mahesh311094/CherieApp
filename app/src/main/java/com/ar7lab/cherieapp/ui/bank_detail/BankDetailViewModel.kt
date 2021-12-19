package com.ar7lab.cherieapp.ui.bank_detail

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.AccountTypeEnum
import com.ar7lab.cherieapp.enums.ChooseLanguageEnum
import com.ar7lab.cherieapp.enums.WalletTypeEnum
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.BankDetails
import com.ar7lab.cherieapp.model.WalletPurchaseDetail
import com.ar7lab.cherieapp.network.repositories.PaymentRepository
import com.ar7lab.cherieapp.network.repositories.WalletRepository
import com.ar7lab.cherieapp.network.response.GetWalletBalanceResponse
import com.ar7lab.cherieapp.ui.payment.cards_banks_dailogs.AddBankBottomSheetViewModel
import com.ar7lab.cherieapp.ui.payment.selectpayment.SelectPaymentViewModel
import com.ar7lab.cherieapp.ui.profile.ProfileViewModel
import com.ar7lab.cherieapp.ui.store.StoreViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class BankDetailViewModel @Inject constructor(
    private val sharePreferencesManager: SharePreferencesManager,
    private val paymentRepository: PaymentRepository,
) : BaseViewModel<BankDetailViewModel.ViewState, BankDetailViewModel.Action>(ViewState()) {




    init {
        loadData()
    }


    fun isReceivedSelected(type:String):Boolean
    {
        if(type.equals("Receive"))
        {
            return true
        }
        return false
    }

    //Network call for payment through the bank
    fun paymentThroughBank(bankId: String, artId: String, artShare: String, totalPrice: String, salesId: String){
        viewModelScope.launch {

            try {
                val result = paymentRepository.paymentThroughBank(
                    artId, "BANK", artShare, totalPrice, salesId, "$", bankId
                )

                sendAction(Action.BankPaymentSuccess(result.status))


            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, false))
            }
        }
    }



   fun sendBankTrasferReqest(bankDetails:BankDetails,amount:Float)
    {
        viewModelScope.launch {
            try {
                sendAction(Action.LoadingStarting)
                val result=paymentRepository.bankDeposit(bankDetails._id,"CASH",5,amount,"PENDING",sharePreferencesManager.userId,"$")
                sendAction(Action.LoadingSuccess(result.message,result.status))
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
                //getWalletBalance()
            } catch (e: Exception) {
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }
    }

    //get user name with @ sign from share pref
    fun getUserName(): String {
        return "@" + sharePreferencesManager.userName

    }

    //get profile pic from share pref
    fun getUserProfilePic(): String {
        return sharePreferencesManager.profileImage

    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isError: Boolean = false,
        val message: String? = null,
        val status: String? = null,
        val bankStatus: String? = null,
        val isLoading: Boolean = false,
        val isSessionExpired: Boolean = false
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object LoadingStarting : Action()
        class LoadingSuccess(val message: String,val status:String) : Action()
        class LoadingFailure(val message: String,val isSessionExpired: Boolean) : Action()
        class BankPaymentSuccess(val bankStatus: String) : Action()
    }




    // Return a state based on action
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.LoadingFailure -> state.copy(
            isError = true,
            message = viewAction.message,
            isLoading = false,
            isSessionExpired = viewAction.isSessionExpired
        )

        is Action.LoadingSuccess -> state.copy(
            isError = false,
            status = viewAction.status,
            message = viewAction.message,
            isLoading = false

        )
        is Action.LoadingStarting -> state.copy(
            isLoading = true,
        )
        is Action.BankPaymentSuccess -> state.copy(
            isLoading = false,
            isError = false,
            message = null,
            bankStatus = viewAction.bankStatus
        )

    }
}
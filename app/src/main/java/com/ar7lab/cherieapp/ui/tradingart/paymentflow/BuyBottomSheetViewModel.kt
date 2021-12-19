package com.ar7lab.cherieapp.ui.tradingart.paymentflow

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.*
import com.ar7lab.cherieapp.model.WalletPurchaseDetail
import com.ar7lab.cherieapp.network.repositories.WalletRepository
import com.ar7lab.cherieapp.network.response.GetWalletBalanceResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class BuyBottomSheetViewModel @Inject constructor(
    private val sharePreferencesManager: SharePreferencesManager,
    private val walletRepository: WalletRepository,
) : BaseViewModel<BuyBottomSheetViewModel.ViewState, BuyBottomSheetViewModel.Action>(ViewState()) {
    //Tab Selection Variable
    val typeSelected = ObservableField(OrderTypeEnum.FIX_PRICE)
    val price = ObservableField("$300")
    val progress = ObservableField(25)


    init {
        loadData()
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
    //on 25% imageview clicked set progress bar progress
    fun onProgress25()
    {
        progress.set(25)
    }
    //on 50% imageview clicked set progress bar progress
    fun onProgress50()
    {
        progress.set(50)
    }
    //on 75% imageview clicked set progress bar progress
    fun onProgress75()
    {
        progress.set(75)
    }
    //on 100% imageview clicked set progress bar progress
    fun onProgress100()
    {
        progress.set(100)
    }
    //on 0% imageview clicked set progress bar progress
    fun onProgress0()
    {
        progress.set(0)
    }

    //Plus Button clicked
    fun onPlusButtonClicked()
    {   price.set(price.get()?.replace("$",""))
        if (price.get()?.toIntOrNull() != null) {
            //Write your code you want to execute if myString is (Int)
            var data=price.get()?.toInt()?:0

            price.set("$"+(++data).toString())
        }
    }
    //Minus Button clicked
    fun onMinusButtonClicked()
    {
        price.set(price.get()?.replace("$",""))
        if (price.get()?.toIntOrNull() != null) {
            //Write your code you want to execute if myString is (Int)
            var data=price.get()?.toInt()?:0

            price.set("$"+(--data).toString())
        }
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isError: Boolean = false,
        val isLoadingMore: Boolean = true,
        val message: String? = null,
        val isRefreshed: Boolean = false,
        val isLoading: Boolean = false,
        val isSessionExpired: Boolean = false,
        val typeSelected: DepositeTypeEnum = DepositeTypeEnum.BANK,
        val userBalance: GetWalletBalanceResponse.Assets? = null,
        val walletPurchaseDetailsArtShares: ArrayList<WalletPurchaseDetail>? = arrayListOf(),
        val walletPurchaseDetailsNFT: ArrayList<WalletPurchaseDetail>? = arrayListOf(),
        val walletPurchaseDetailsCash: ArrayList<WalletPurchaseDetail>? = arrayListOf(),
        val walletPurchaseDetailsCryptoCurrency: ArrayList<WalletPurchaseDetail>? = arrayListOf()
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object LoadingStarting : Action()
        object StartLoadingMore : Action()
        class TypeChanged(val depositeSelected:DepositeTypeEnum) : Action()
        class WalletBallanceSuccess(val userBalance: GetWalletBalanceResponse.Assets?) : Action()
        class LoadingFailure(val message: String, val isSessionExpired: Boolean) : Action()
        class LoadingArtSharesSuccess(val walletPurchaseDetails: ArrayList<WalletPurchaseDetail>) :
            Action()


        object StartRefreshing : Action()
        class LoadingCompletelySuccess(val isRefreshed: Boolean = false) : Action()
    }

    //On click on any Type
    fun onSelectType(orderTypeEnum: OrderTypeEnum) {
        if (orderTypeEnum == typeSelected.get()) return
        typeSelected.set(orderTypeEnum)
    }

    fun onRefresh()
    {

    }

    // Return a state based on action
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.LoadingFailure -> state.copy(
            isError = true,
            message = viewAction.message,
            isRefreshed = false,
            isLoading = false,
            isSessionExpired = viewAction.isSessionExpired
        )
        Action.StartRefreshing -> state.copy(
            isError = false,
            isRefreshed = true
        )
        is Action.WalletBallanceSuccess -> state.copy(
            userBalance = viewAction.userBalance,
            isError = false,
            isRefreshed = false,
            isLoading = false

        )
        is Action.LoadingStarting -> state.copy(
            isLoading = true,
            isRefreshed = true
        )
        is Action.LoadingArtSharesSuccess -> state.copy(
            walletPurchaseDetailsArtShares = viewAction.walletPurchaseDetails,
            walletPurchaseDetailsCash = null,
            walletPurchaseDetailsNFT = null,
            walletPurchaseDetailsCryptoCurrency = null,
            isLoading = false,
            isRefreshed = false
        )
        is Action.LoadingCompletelySuccess -> state.copy(
            isLoading = false,
            isRefreshed = false,
            isLoadingMore = false
        )
        is Action.StartLoadingMore -> state.copy(isRefreshed = false, isLoadingMore = true)
        is Action.TypeChanged->state.copy(typeSelected = viewAction.depositeSelected)
    }
}
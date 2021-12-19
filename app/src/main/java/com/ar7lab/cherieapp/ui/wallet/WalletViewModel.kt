package com.ar7lab.cherieapp.ui.wallet

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.AccountTypeEnum
import com.ar7lab.cherieapp.enums.WalletTypeEnum
import com.ar7lab.cherieapp.model.WalletPurchaseDetail
import com.ar7lab.cherieapp.network.repositories.WalletRepository
import com.ar7lab.cherieapp.network.response.GetWalletBalanceResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class WalletViewModel @Inject constructor(
    private val sharePreferencesManager: SharePreferencesManager,
    private val walletRepository: WalletRepository,
) : BaseViewModel<WalletViewModel.ViewState, WalletViewModel.Action>(ViewState()) {
    //Tab Selection Variable
    val tabTypeSelected = ObservableField(WalletTypeEnum.CRYPTOCURRENCY)

    //Selected Tab Title Variable
    val tabTypeSelectedTitle = ObservableField(tabTypeSelected.get()?.name)

    //cash crypto inner tab seletion
    val innerTabSelected = ObservableField(WalletTypeEnum.ALL)

    //userbalance fetching from server
    var userBalance: GetWalletBalanceResponse.Assets? = null

    //page counter
    private var _page = 1

    //limit counter
    private var _limit = 10

    //Artshare data list
    private var _walletActivities = ArrayList<WalletPurchaseDetail>()


    //user type wise first name & company name getting from share pref
    fun getUserFirstLastNameOrCompanyName(): String {
        return if (sharePreferencesManager.userType == AccountTypeEnum.PERSONAL.name) {
            sharePreferencesManager.firstName + " " + sharePreferencesManager.lastName
        } else {
            sharePreferencesManager.companyName
        }
    }

    init {
        loadData()
    }

    fun isReceivedSelected(type: String): Boolean {
        if (type.equals("Receive")) {
            return true
        }
        return false
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

    //Purchase detail getting API call
    suspend fun getWalletPurchaseDetail() {

        val response = walletRepository.getPurchaseDetail(
            _page,
            _limit)
        response.data?.let {

            _walletActivities.addAll(it.transactionHistory)
            sendAction(
                Action.LoadingActivitiesSuccess(
                    _walletActivities
                )
            )


        }

    }

    override fun onLoadData() {
        //load data from API
        viewModelScope.launch {
            try {
                getWalletBalance()
                getWalletPurchaseDetail()
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
        val isLoadingMore: Boolean = true,
        val message: String? = null,
        val isRefreshed: Boolean = false,
        val isLoading: Boolean = false,
        val isSessionExpired: Boolean = false,
        val walletType: WalletTypeEnum = WalletTypeEnum.CRYPTOCURRENCY,
        val userBalance: GetWalletBalanceResponse.Assets? = null,
        val walletActivities: ArrayList<WalletPurchaseDetail>? = arrayListOf()
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object LoadingStarting : Action()
        object StartLoadingMore : Action()
        class WalletBallanceSuccess(val userBalance: GetWalletBalanceResponse.Assets?) : Action()
        class LoadingFailure(val message: String, val isSessionExpired: Boolean) : Action()
        class LoadingActivitiesSuccess(val walletPurchaseDetails: ArrayList<WalletPurchaseDetail>) :
            Action()

        object StartRefreshing : Action()
        class LoadingCompletelySuccess(val isRefreshed: Boolean = false) : Action()
    }

    //auto pagination this method call from fragment
    fun loadMore() {
        _page++
        sendAction(Action.StartLoadingMore)
        viewModelScope.launch {
            try {
                getWalletPurchaseDetail()
                sendAction(Action.LoadingCompletelySuccess())
            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }
    }

    //this method call when you want to refresh every thing
    fun onRefresh() {
        _page = 1
        _walletActivities.clear()
        sendAction(Action.StartRefreshing)
        loadData()
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

        is Action.LoadingActivitiesSuccess -> state.copy(
            walletActivities = viewAction.walletPurchaseDetails,
            isLoading = false,
            isRefreshed = false,

        )
        is Action.LoadingCompletelySuccess -> state.copy(
            isLoading = false,
            isRefreshed = false,
            isLoadingMore = false
        )
        is Action.StartLoadingMore -> state.copy(isRefreshed = false, isLoadingMore = true)
    }
}
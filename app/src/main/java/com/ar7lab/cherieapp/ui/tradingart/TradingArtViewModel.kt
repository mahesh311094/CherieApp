package com.ar7lab.cherieapp.ui.tradingart

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.enums.MarketChartTypeEnum
import com.ar7lab.cherieapp.enums.MarketMyTradesTypeEnum
import com.ar7lab.cherieapp.enums.MarketTradingTypeEnum
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.ArtTransactionDetails
import com.ar7lab.cherieapp.network.repositories.ArtRepository
import com.ar7lab.cherieapp.ui.market.MarketViewModel
import com.ar7lab.cherieapp.utils.FILTER_MY_TRADING
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TradingArtViewModel @Inject constructor(
    private val artRepository: ArtRepository
): BaseViewModel<TradingArtViewModel.ViewState, TradingArtViewModel.Action>(
    ViewState()
){
    private var _page = 1
    private var _limit = 10

    //Save the selected market traditional type
    var marketTradingTypeSelected = ObservableField(MarketTradingTypeEnum.CHART)
    var marketChartTypeSelected = ObservableField(MarketChartTypeEnum.MONTHS_3)
    var marketMyTradesTypeSelected = ObservableField(MarketMyTradesTypeEnum.OPEN_ORDERS)
    private val _artTransactionDetailsFullTrading = ArrayList<ArtTransactionDetails>()
    private val _artTransactionDetailsMyTrading = ArrayList<ArtTransactionDetails>()
    private val arts = MutableLiveData<List<Art>>(emptyList())
    private var artId: String = ""

    fun getArts() = arts.value

    fun init(_artId: String){
        artId = _artId
    }

    override fun onLoadData() {
        viewModelScope.launch {
            try {
                loadArt()
                sendAction(Action.LoadingCompletelySuccess(true))
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    private suspend fun loadArt() {
        //When Refresh calling
        if (state.isRefreshing) {
            when (marketTradingTypeSelected.get()) {
                MarketTradingTypeEnum.MARKET_TRADES -> _artTransactionDetailsFullTrading.clear()
                MarketTradingTypeEnum.MY_TRADES -> _artTransactionDetailsMyTrading.clear()
            }
        }

        //Check which tab is selected
        when (marketTradingTypeSelected.get()) {
            //Load data from API calling When "Traditional Art" tab is selected
            MarketTradingTypeEnum.MARKET_TRADES -> {
                val response = artRepository.artTransactionHistory(
                    artId,
                    _page,
                    _limit,
                    ""
                )
                response.data?.artTransactionDetails?.let {
                    if (state.isLoadingMore && it.isEmpty()) {
                        //sendAction(Action.LoadingFailure("No more data!",false))
                    }else {
                        _artTransactionDetailsFullTrading.addAll(it)
                        sendAction(Action.LoadingFullTradingSuccess(_artTransactionDetailsFullTrading))
                    }
                }
            }

            MarketTradingTypeEnum.MY_TRADES -> {
                // get arts based on user input or not
                val response = artRepository.artTransactionHistory(
                    artId,_page, _limit, FILTER_MY_TRADING
                )
                response.data?.artTransactionDetails?.let {
                    if (state.isLoadingMore && it.isEmpty()) {
                        //sendAction(Action.LoadingFailure("No more data!",false))
                    } else {
                        _artTransactionDetailsMyTrading.addAll(it)
                        sendAction(Action.LoadingMyTradingSuccess(_artTransactionDetailsMyTrading))
                    }
                }
            }
        }

    }

    //this method change trading type
    fun changeMarketTradingType(marketTradingTypeEnum: MarketTradingTypeEnum) {
        marketTradingTypeSelected.set(marketTradingTypeEnum)
        onRefresh()
    }
    //this method change chart selection type
    fun changeMarketChartType(marketChartType: MarketChartTypeEnum) {
        marketChartTypeSelected.set(marketChartType)
        onRefresh()
    }
    //this method will change my tradestype
    fun changeMarketMyTradesType(marketMyTradesTypeEnum: MarketMyTradesTypeEnum) {
        marketMyTradesTypeSelected.set(marketMyTradesTypeEnum)
        onRefresh()
    }

    fun onRefresh() {
        _page = 1
        sendAction(Action.StartRefreshing)
        loadData()
    }

    fun loadMore() {
        _page++
        sendAction(Action.StartLoadingMore)
        viewModelScope.launch {
            try {
                loadArt()
                sendAction(Action.LoadingCompletelySuccess())
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isRefreshing: Boolean = true,
        val isLoadingMore: Boolean = true,
        val isRefreshed: Boolean = false,
        val isLoading: Boolean = true,
        val isError: Boolean = false,
        val isSessionExpired: Boolean = false,
        val message: String? = null,
        val isDataLoaded: Boolean = false,
        val artTransactionDetailsFullTrading: ArrayList<ArtTransactionDetails>? = arrayListOf(),
        val artTransactionDetailsMyTrading: ArrayList<ArtTransactionDetails>? = arrayListOf(),
        val isHaveData: Boolean = false
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartRefreshing : Action()
        object StartLoadingMore : Action()
        object StartLoading : Action()
        class LoadingSuccess(val isDataLoaded: Boolean) : Action()
        class LoadingFailure(val message: String,val isSessionExpired: Boolean) : Action()
        class LoadingFullTradingSuccess(val artTransactionDetailsFullTrading: ArrayList<ArtTransactionDetails>) : Action()
        class LoadingMyTradingSuccess(val artTransactionDetailsMyTrading: ArrayList<ArtTransactionDetails>) : Action()
        class LoadingCompletelySuccess(val isRefreshed: Boolean = false) : Action()
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.StartRefreshing -> state.copy(
            isRefreshing = true,
            isLoadingMore = false,
            isError = false,
            isSessionExpired = false,
            message = null,
            isDataLoaded = false,
            isRefreshed = false,
        )
        is Action.StartLoadingMore -> state.copy(
            isRefreshing = false,
            isLoadingMore = true,
            isError = false,
            isSessionExpired = false,
            message = null,
            isDataLoaded = false,
            isRefreshed = false
        )
        is Action.StartLoading -> state.copy(
            isLoading = true,
            isError = false,
            isSessionExpired = false,
            message = null,
            isDataLoaded = false
        )
        is Action.LoadingSuccess -> state.copy(
            isLoading = false,
            isError = false,
            isSessionExpired = false,
            message = null,
            isDataLoaded = viewAction.isDataLoaded
        )
        is  Action.LoadingFullTradingSuccess -> state.copy(
            artTransactionDetailsFullTrading = viewAction.artTransactionDetailsFullTrading,
            artTransactionDetailsMyTrading = null,
            isHaveData = viewAction.artTransactionDetailsFullTrading.isNotEmpty()
        )
        is  Action.LoadingMyTradingSuccess -> state.copy(
            artTransactionDetailsMyTrading = viewAction.artTransactionDetailsMyTrading,
            artTransactionDetailsFullTrading = null,
            isHaveData = viewAction.artTransactionDetailsMyTrading.isNotEmpty()
        )
        is Action.LoadingCompletelySuccess -> state.copy(
            isRefreshing = false,
            isLoadingMore = false,
            isDataLoaded = true,
            isRefreshed = viewAction.isRefreshed,
            isError = false,
            isSessionExpired = false,
            message = null
        )
        is Action.LoadingFailure -> state.copy(
            isLoading = false,
            isError = true,
            message = viewAction.message,
            isSessionExpired = viewAction.isSessionExpired,
            isDataLoaded = false
        )
    }

}
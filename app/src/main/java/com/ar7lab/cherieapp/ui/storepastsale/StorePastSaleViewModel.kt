package com.ar7lab.cherieapp.ui.storepastsale

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.MarketTypeEnum
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.network.repositories.ArtRepository
import com.ar7lab.cherieapp.ui.traditionalartworkdetails.TraditionalArtworkDetailsViewModel
import com.ar7lab.cherieapp.utils.FILTER_NFT_ART
import com.ar7lab.cherieapp.utils.FILTER_TRADITIONAL_ART
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Marketplace view model
 * @property artRepository for get Art data after API calling
 * @property sharePreferencesManager for get the saved value from app local db
 * */
@HiltViewModel
internal class StorePastSaleViewModel @Inject constructor(
    private val artRepository: ArtRepository,
    private val sharePreferencesManager: SharePreferencesManager
) :
    BaseViewModel<StorePastSaleViewModel.ViewState, StorePastSaleViewModel.Action>(ViewState()) {

    // Save the selected art type
    var marketTypeSelected = ObservableField(MarketTypeEnum.TRADITIONAL_ART)

    //searchbox hide show
    var isSearchBoxShow = ObservableField(false)

    //Initialize Traditional arts array
    private val _forYouArts = ArrayList<Art>()

    //Initialize Ntf arts array
    private val _nftArts = ArrayList<Art>()
    private var _query = ""
    private var _page = 1

    //Set load item limit
    private var _limit = 10

    var totalResult = ObservableField("")
    var searchQuery = ObservableField("")
    var isNeedSearch = ObservableField(false)

    override fun onLoadData() {
        viewModelScope.launch {
            try {
                loadArt()
                sendAction(Action.LoadingCompletelySuccess(true))
            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))

            }
        }
    }

    /**
     * @return Art list
     * */
    private suspend fun loadArt() {
        //When Refresh calling
        if (state.isRefreshing) {
            when (marketTypeSelected.get()) {
                MarketTypeEnum.TRADITIONAL_ART -> _forYouArts.clear()
                MarketTypeEnum.NTF_ART -> _nftArts.clear()
            }
        }

        //Check which tab is selected
        when (marketTypeSelected.get()) {
            //Load data from API calling When "Traditional Art" tab is selected
            MarketTypeEnum.TRADITIONAL_ART -> {
                val response = artRepository.searchArtsStore(
                    _page,
                    _limit,
                    FILTER_TRADITIONAL_ART,
                    "PAST SALE",
                    sharePreferencesManager.userId,
                    _query
                )
                response.data?.pastArts?.let {
                    Timber.e("size-- ${it.size}")
                    if (state.isLoadingMore && it.isEmpty()) {
                        sendAction(Action.LoadingFailure("No more data!", false))
                    } else {
                        _forYouArts.addAll(it)
                        totalResult.set(_forYouArts.size.toString())
                        sendAction(Action.LoadingForYouArtSuccess(_forYouArts))
                    }
                }
            }
            //Load data from API calling When "NFT Art" tab is selected
            MarketTypeEnum.NTF_ART -> {
                // get arts based on user input or not
                val response = artRepository.searchArtsStore(
                    _page,
                    _limit,
                    FILTER_NFT_ART,
                    "ON SALE",
                    sharePreferencesManager.userId,
                    _query
                )
                response.data?.pastArts?.let { arts ->
                    arts.map { art -> art.isNFT = true }
                    if (state.isLoadingMore && arts.isEmpty()) {
                        sendAction(Action.LoadingFailure("No more data!", false))
                    } else {
                        _nftArts.addAll(arts)
                        totalResult.set(_nftArts.size.toString())
                        sendAction(Action.LoadingNTFArtSuccess(_nftArts))
                    }
                }
            }
        }

    }

    /**
     * Search art with query from search box
     * @param query from input of search box
     */
    fun search(query: String) {
        _query = query
        searchQuery.set(query)
        isNeedSearch.set(query.isNotEmpty())
        onRefresh()
    }

    /**
     * Reflect to the change when click on "Traditional Art", "NTF Art"
     * @return type "Traditional Art" or "NTF Art"
     * Also call Refresh layout
     * */
    fun changeMarketType(marketTypeEnum: MarketTypeEnum) {
        if (marketTypeEnum == marketTypeSelected.get()) return
        marketTypeSelected.set(marketTypeEnum)
        onRefresh()
    }

    /**
     * Refresh view is calling
     * @return load more data
     * */
    fun onRefresh() {
        _page = 1
        sendAction(Action.StartRefreshing)
        loadData()
    }

    /**
     * Add more data onclick on "Show More" button
     * @return load data as per limit. Ex. If limit = 10 after calling page=1, the total data will be 10+10 = 20
     * */
    fun loadMore() {
        _page++
        sendAction(Action.StartLoadingMore)
        viewModelScope.launch {
            try {
                loadArt()
//                sendAction(Action.LoadingCompletelySuccess())
            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }
    }

    fun like(art: Art) {
        viewModelScope.launch {
            try {
                val isLiked = art.liked
                updateLikeArt(art.id)
                if (isLiked) {
                    artRepository.unLikeArt(art.id)
                } else {
                    artRepository.likeArt(art.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                updateLikeArt(art.id)
            }
        }
    }

    private fun updateLikeArt(artId: String) {

        //Check which tab is selected
        when (marketTypeSelected.get()) {
            MarketTypeEnum.TRADITIONAL_ART -> {
                // find the art item in the array and then update liked
                _forYouArts.find { it.id == artId }?.let {
                    it.liked = !it.liked
                    it.likeCount += if (it.liked) 1 else -1
                }
                sendAction(Action.LoadingForYouArtSuccess(_forYouArts))
            }

            MarketTypeEnum.NTF_ART -> {
                // find the art item in the array and then update liked
                _nftArts.find { it.id == artId }?.let {
                    it.liked = !it.liked
                    it.likeCount += if (it.liked) 1 else -1
                }
                sendAction(Action.LoadingNTFArtSuccess(_nftArts))
            }
        }

    }

    /**
     * Define view state for Activity to use
     * @return state
     * */
    internal data class ViewState(
        val isRefreshing: Boolean = true,
        val isLoadingMore: Boolean = false,
        val isRefreshed: Boolean = false,
        val isLoading: Boolean = true,
        val isError: Boolean = false,
        val isSessionExpired: Boolean = false,
        val message: String? = null,
        val isDataLoaded: Boolean = false,
        val forYouArts: ArrayList<Art>? = arrayListOf(),
        val nftArts: ArrayList<Art>? = arrayListOf(),
        val isHaveData: Boolean = false,
        val stateCalled: Boolean = false
    ) : BaseViewState

    /**
     * Define action of the ViewModel
     * @return action
     * */
    internal sealed class Action : BaseAction {
        object StartRefreshing : Action()
        object StartLoadingMore : Action()
        object StartLoading : Action()
        class LoadingSuccess(val isDataLoaded: Boolean) : Action()
        class LoadingFailure(val message: String, val isSessionExpired: Boolean) : Action()
        class LoadingForYouArtSuccess(val arts: ArrayList<Art>) : Action()
        class LoadingNTFArtSuccess(val ntfArt: ArrayList<Art>) : Action()
        class LoadingCompletelySuccess(val isRefreshed: Boolean = false) : Action()
    }

    /**
     * State based on action
     * @return a state based on action
     * */
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.StartRefreshing -> state.copy(
            isRefreshing = true,
            isLoadingMore = false,
            isError = false,
            isSessionExpired = false,
            message = null,
            isDataLoaded = false,
            isRefreshed = false,
            stateCalled = false
        )
        is Action.StartLoadingMore -> state.copy(
            isRefreshing = false,
            isLoadingMore = true,
            isLoading = true,
            isError = false,
            isSessionExpired = false,
            message = null,
            isDataLoaded = false,
            isRefreshed = false,
            stateCalled = false
        )
        is Action.StartLoading -> state.copy(
            isLoading = true,
            isError = false,
            isSessionExpired = false,
            message = null,
            isDataLoaded = false,
            stateCalled = false
        )

        is Action.LoadingSuccess -> state.copy(
            isLoading = false,
            isError = false,
            isSessionExpired = false,
            message = null,
            isDataLoaded = viewAction.isDataLoaded,
            stateCalled = false
        )
        is Action.LoadingForYouArtSuccess -> state.copy(
            forYouArts = viewAction.arts,
            isLoading = false,
            isLoadingMore = true,
            nftArts = null,
            isHaveData = viewAction.arts.isNotEmpty(),
            stateCalled = false
        )
        is Action.LoadingNTFArtSuccess -> state.copy(
            nftArts = viewAction.ntfArt,
            isLoading = false,
            isLoadingMore = true,
            forYouArts = null,
            isHaveData = viewAction.ntfArt.isNotEmpty(),
            stateCalled = false
        )
        is Action.LoadingCompletelySuccess -> state.copy(
            isRefreshing = false,
            isLoading = false,
            isLoadingMore = true,
            isDataLoaded = true,
            isRefreshed = viewAction.isRefreshed,
            isError = false,
            isSessionExpired = false,
            message = null,
            stateCalled = true,
        )
        is Action.LoadingFailure -> state.copy(
            isRefreshing = false,
            isLoading = false,
            isLoadingMore = false,
            isError = true,
            message = viewAction.message,
            isSessionExpired = viewAction.isSessionExpired,
            isDataLoaded = false,
            isHaveData = false,
            stateCalled = false
        )
    }
}
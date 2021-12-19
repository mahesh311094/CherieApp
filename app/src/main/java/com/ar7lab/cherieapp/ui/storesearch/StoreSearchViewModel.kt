package com.ar7lab.cherieapp.ui.storesearch

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.LikesTypeEnum
import com.ar7lab.cherieapp.enums.MarketTypeEnum
import com.ar7lab.cherieapp.enums.SearchTypeEnum
import com.ar7lab.cherieapp.enums.StoreTypeEnum
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.Artist
import com.ar7lab.cherieapp.network.repositories.ArtRepository
import com.ar7lab.cherieapp.network.repositories.ArtistRepository
import com.ar7lab.cherieapp.ui.market.MarketViewModel
import com.ar7lab.cherieapp.ui.store.StoreViewModel
import com.ar7lab.cherieapp.ui.topartists.TopArtistsViewModel
import com.ar7lab.cherieapp.utils.FILTER_NFT_ART
import com.ar7lab.cherieapp.utils.FILTER_TRADITIONAL_ART
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * StoreSearchViewModel view model
 * @property artRepository for get Art data after API calling
 * @property sharePreferencesManager for get the saved value from app local db
 * */
@HiltViewModel
internal class StoreSearchViewModel @Inject constructor(
    private val artRepository: ArtRepository,
    private val artistRepository: ArtistRepository,
    private val sharePreferencesManager: SharePreferencesManager
) :
    BaseViewModel<StoreSearchViewModel.ViewState, StoreSearchViewModel.Action>(ViewState()) {

    // Save the selected art type
    var storeTypeSelected = ObservableField(StoreTypeEnum.TRADITIONAL_ART)
    var searchTypeSelected = ObservableField(SearchTypeEnum.ON_SALE)

    //searchbox hide show
    var isSearchBoxShow = ObservableField(false)

    //Initialize Traditional arts array
    private val _forYouArts = ArrayList<Art>()

    //Traditional Art Section
    private val _traditionalArtsOnSale = ArrayList<Art>()
    private val _traditionalArtsUpcomingDeals = ArrayList<Art>()
    private val _traditionalArtsPastDeals = ArrayList<Art>()
    private val _traditionalArtist = ArrayList<Artist>()
    //NFT Art Section
    private val _nftArtsOnsale = ArrayList<Art>()
    private val _nftArtsUpcomingDeals = ArrayList<Art>()
    private val _nftArtsPastDeals = ArrayList<Art>()
    private val _nftArtist = ArrayList<Artist>()

    //Initialize Ntf arts array
    private val _nftArts = ArrayList<Art>()
    private var _query = ""
    private var _page = 1

    //Set load item limit
    private var _limit = 10
    private var _storeTypeEnum = StoreTypeEnum.TRADITIONAL_ART

    //Show total z
    var onsaleCount = ObservableField("")
    var upcomingDealsCount = ObservableField("")
    var pastDealsCount = ObservableField("")
    var artistCount = ObservableField("")

    var totalResult = ObservableField("")
    var searchQuery = ObservableField("")
    var isNeedSearch = ObservableField(false)

    fun init(storeTypeEnum: StoreTypeEnum) {
        _storeTypeEnum = storeTypeEnum
    }

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
            when (storeTypeSelected.get()) {
                //MarketTypeEnum.TRADITIONAL_ART -> _forYouArts.clear()
                StoreTypeEnum.TRADITIONAL_ART -> {
                    _traditionalArtsOnSale.clear()
                    _traditionalArtsUpcomingDeals.clear()
                    _traditionalArtsPastDeals.clear()
                    _traditionalArtist.clear()
                }
                StoreTypeEnum.NFT_ART -> {
                    _nftArtsOnsale.clear()
                    _nftArtsUpcomingDeals.clear()
                    _nftArtsPastDeals.clear()
                    _nftArtist.clear()
                }
            }
        }

        //Check which tab is selected
        when (_storeTypeEnum) {
            //Load data from API calling When "Traditional Art" tab is selected
            StoreTypeEnum.TRADITIONAL_ART -> {
                val response = artRepository.searchArtsStore(
                    _page,
                    _limit,
                    FILTER_TRADITIONAL_ART,
                    "",
                    userId = sharePreferencesManager.userId,
                    _query
                )
                var resultSize = response.data?.onSaleArts?.size!! + response.data?.upcomingArts?.size!! + response.data?.pastArts?.size!!
                totalResult.set(resultSize.toString())
                //Onsale Array for Traditional Art
                response.data?.onSaleArts?.let {
                    Timber.e("size-- ${it.size}")
                    if (state.isLoadingMore && it.isEmpty()) {
                        sendAction(Action.LoadingFailure("No more data!", false))
                    } else {
                        _traditionalArtsOnSale.clear()
                        _traditionalArtsOnSale.addAll(it)
                        onsaleCount.set(response.data?.onSaleArtsCount.toString())
                        sendAction(Action.LoadingTraditionalArtOnsaleSuccess(_traditionalArtsOnSale))
                    }
                }
                //Upcoming Deals Array for Traditional Arts
                response.data?.upcomingArts?.let {
                    Timber.e("size-- ${it.size}")
                    if (state.isLoadingMore && it.isEmpty()) {
                        sendAction(Action.LoadingFailure("No more data!", false))
                    } else {
                        _traditionalArtsUpcomingDeals.clear()
                        _traditionalArtsUpcomingDeals.addAll(it)
                        upcomingDealsCount.set(response.data?.upcomingArtsCount.toString())
                        sendAction(Action.LoadingTraditionalArtUpcomingDealsSuccess(_traditionalArtsUpcomingDeals))
                    }
                }
                //Past Deals Array for Traditional Arts
                response.data?.pastArts?.let {
                    Timber.e("size-- ${it.size}")
                    if (state.isLoadingMore && it.isEmpty()) {
                        sendAction(Action.LoadingFailure("No more data!", false))
                    } else {
                        _traditionalArtsPastDeals.clear()
                        _traditionalArtsPastDeals.addAll(it)
                        pastDealsCount.set(response.data?.pastArtsCount.toString())
                        sendAction(Action.LoadingTraditionalArtPastDealsSuccess(_traditionalArtsPastDeals))
                    }
                }
                //Array for Traditional Artist
                response.data?.artists?.let {
                    Timber.e("size-- ${it.size}")
                    if (state.isLoadingMore && it.isEmpty()) {
                        sendAction(Action.LoadingFailure("No more data!", false))
                    } else {
                        _traditionalArtist.clear()
                        _traditionalArtist.addAll(it)
                        artistCount.set(response.data?.artistsCount.toString())
                        sendAction(Action.LoadingTraditionalArtistSuccess(_traditionalArtist))
                    }
                }

            }
            //Load data from API calling When "NFT Art" tab is selected
            StoreTypeEnum.NFT_ART -> {
                // get arts based on user input or not
                val response = artRepository.searchArtsStore(
                    _page,
                    _limit,
                    FILTER_NFT_ART,
                    "",
                    userId = sharePreferencesManager.userId,
                    _query
                )
                response.data?.onSaleArts?.let {
                    Timber.e("size-- ${it.size}")
                    if (state.isLoadingMore && it.isEmpty()) {
                        sendAction(Action.LoadingFailure("No more data!", false))
                    } else {
                        _nftArtsOnsale.clear()
                        _nftArtsOnsale.addAll(it)
                        //onsaleCount.set(_nftArtsOnsale.size.toString())
                        onsaleCount.set(response.data?.onSaleArtsCount.toString())
                        sendAction(Action.LoadingNFTArtOnsaleSuccess(_nftArtsOnsale))
                    }
                }
                //Upcoming Deals Array for Traditional Arts
                response.data?.upcomingArts?.let {
                    Timber.e("size-- ${it.size}")
                    if (state.isLoadingMore && it.isEmpty()) {
                        sendAction(Action.LoadingFailure("No more data!", false))
                    } else {
                        _nftArtsUpcomingDeals.clear()
                        _nftArtsUpcomingDeals.addAll(it)
                        //upcomingDealsCount.set(_nftArtsUpcomingDeals.size.toString())
                        upcomingDealsCount.set(response.data?.upcomingArtsCount.toString())
                        sendAction(Action.LoadingNFTArtUpcomingDealsSuccess(_nftArtsUpcomingDeals))
                    }
                }
                //Past Deals Array for Traditional Arts
                response.data?.pastArts?.let {
                    Timber.e("size-- ${it.size}")
                    if (state.isLoadingMore && it.isEmpty()) {
                        sendAction(Action.LoadingFailure("No more data!", false))
                    } else {
                        _nftArtsPastDeals.clear()
                        _nftArtsPastDeals.addAll(it)
                        //pastDealsCount.set(_nftArtsPastDeals.size.toString())
                        pastDealsCount.set(response.data?.pastArtsCount.toString())
                        sendAction(Action.LoadingNFTArtPastDealsSuccess(_nftArtsPastDeals))
                    }
                }

                //Array for NFT Artist
                response.data?.artists?.let {
                    Timber.e("size-- ${it.size}")
                    if (state.isLoadingMore && it.isEmpty()) {
                        sendAction(Action.LoadingFailure("No more data!", false))
                    } else {
                        _nftArtist.clear()
                        _nftArtist.addAll(it)
                        artistCount.set(response.data?.artistsCount.toString())
                        sendAction(Action.LoadingTraditionalArtistSuccess(_nftArtist))
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

    /*fun search(query: String) {
        _query = query
        searchQuery.set(query)
        isNeedSearch.set(query.isNotEmpty())
        // Do not call API if query is less than 3
        if (query.length in 1..2) {
            isNeedSearch.set(false)
            //Check which tab is selected
            when (_storeTypeEnum) {
                //Load data from API calling When "Traditional Art" tab is selected
                StoreTypeEnum.TRADITIONAL_ART -> {
                    //sendAction(Action.LoadingForYouArtSuccess(arrayListOf()))
                    sendAction(Action.LoadingTraditionalArtOnsaleSuccess(_traditionalArtsOnSale))
                    sendAction(Action.LoadingTraditionalArtUpcomingDealsSuccess(_traditionalArtsUpcomingDeals))
                    sendAction(Action.LoadingTraditionalArtPastDealsSuccess(_traditionalArtsPastDeals))
                }
                //Load data from API calling When "NFT Art" tab is selected
                StoreTypeEnum.NFT_ART -> {
                    sendAction(Action.LoadingNFTArtOnsaleSuccess(_nftArtsOnsale))
                    sendAction(Action.LoadingNFTArtUpcomingDealsSuccess(_nftArtsUpcomingDeals))
                    sendAction(Action.LoadingNFTArtPastDealsSuccess(_nftArtsPastDeals))
                }
            }
        } else {
            onRefresh()
        }
    }*/

    /**
     * Reflect to the change when click on "Traditional Art", "NTF Art"
     * @return type "Traditional Art" or "NTF Art"
     * Also call Refresh layout
     * */
    fun changeMarketType(storeTypeEnum: StoreTypeEnum) {
        if (storeTypeEnum == storeTypeSelected.get()) return
        storeTypeSelected.set(storeTypeEnum)
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

    /*private fun updateLikeArt(artId: String) {
        //Check which tab is selected
        when (storeTypeSelected.get()) {
            StoreTypeEnum.TRADITIONAL_ART -> {
                // find the art item in the array and then update liked
                _forYouArts.find { it.id == artId }?.let {
                    it.liked = !it.liked
                    it.likeCount += if (it.liked) 1 else -1
                }
                sendAction(Action.LoadingForYouArtSuccess(_forYouArts))
            }

            StoreTypeEnum.NFT_ART-> {
                // find the art item in the array and then update liked
                _nftArts.find { it.id == artId }?.let {
                    it.liked = !it.liked
                    it.likeCount += if (it.liked) 1 else -1
                }
                sendAction(Action.LoadingNTFArtSuccess(_nftArts))
            }
        }
    }*/
    private fun updateLikeArt(artId: String) {
        //Traditional Onsale like
        val artTraditionalSourceOnsale = _traditionalArtsOnSale.find { it.id == artId }
        artTraditionalSourceOnsale?.let {
            it.liked = !it.liked
            it.likeCount += if (it.liked) 1 else -1
        }
        //Traditional Upcoming Deals like
        val artTraditionalUpcomingDeals = _traditionalArtsUpcomingDeals.find { it.id == artId }
        artTraditionalUpcomingDeals?.let {
            it.liked = !it.liked
            it.likeCount += if (it.liked) 1 else -1
        }
        //Traditional Past Deals like
        val artTraditionalPastDeals = _traditionalArtsPastDeals.find { it.id == artId }
        artTraditionalPastDeals?.let {
            it.liked = !it.liked
            it.likeCount += if (it.liked) 1 else -1
        }
        //Nft Onsale like
        val artNFTSourceOnsale = _nftArtsOnsale.find { it.id == artId }
        artNFTSourceOnsale?.let {
            it.liked = !it.liked
            it.likeCount += if (it.liked) 1 else -1
        }
        //Nft Upcoming Deals like
        val artNFTSourceUpcomingDeals = _nftArtsUpcomingDeals.find { it.id == artId }
        artNFTSourceUpcomingDeals?.let {
            it.liked = !it.liked
            it.likeCount += if (it.liked) 1 else -1
        }
        //Nft Past Deals like
        val artNFTSourcePastDeals = _nftArtsPastDeals.find { it.id == artId }
        artNFTSourcePastDeals?.let {
            it.liked = !it.liked
            it.likeCount += if (it.liked) 1 else -1
        }
        when (storeTypeSelected.get()) {
            StoreTypeEnum.FOR_YOU -> {
                sendAction(Action.LoadingForYouArtSuccess(_forYouArts))
            }
            StoreTypeEnum.TRADITIONAL_ART -> {
                sendAction(Action.LoadingTraditionalArtOnsaleSuccess(_traditionalArtsOnSale))
                sendAction(Action.LoadingTraditionalArtUpcomingDealsSuccess(_traditionalArtsUpcomingDeals))
                sendAction(Action.LoadingTraditionalArtPastDealsSuccess(_traditionalArtsOnSale))
            }
            StoreTypeEnum.NFT_ART -> {
                sendAction(Action.LoadingNFTArtOnsaleSuccess(_nftArtsOnsale))
                sendAction(Action.LoadingNFTArtUpcomingDealsSuccess(_nftArtsUpcomingDeals))
                sendAction(Action.LoadingNFTArtPastDealsSuccess(_nftArtsPastDeals))
            }
        }
    }

    //Add Notify
    fun notify(art: Art) {
        viewModelScope.launch {
            try {
                val isNotified = art.notified
                updateNotifyArt(art.id)
                if (isNotified) {
                    artRepository.deleteNotify(art.id)
                } else {
                    artRepository.addNotify(art.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                updateNotifyArt(art.id)
            }
        }
    }

    //Delete notify
    private fun updateNotifyArt(artId: String) {
        //Traditional Upcoming Deals like
        val artTraditionalUpcomingDeals = _traditionalArtsUpcomingDeals.find { it.id == artId }
        artTraditionalUpcomingDeals?.let {
            it.notified = !it.notified
        }
        //Nft Upcoming Deals like
        val artNFTSourceUpcomingDeals = _nftArtsUpcomingDeals.find { it.id == artId }
        artNFTSourceUpcomingDeals?.let {
            it.notified = !it.notified
        }
        when (storeTypeSelected.get()) {
            StoreTypeEnum.TRADITIONAL_ART -> {
                sendAction(Action.LoadingTraditionalArtUpcomingDealsSuccess(_traditionalArtsUpcomingDeals))
            }
            StoreTypeEnum.NFT_ART -> {
                sendAction(Action.LoadingNFTArtUpcomingDealsSuccess(_nftArtsUpcomingDeals))
            }
        }
    }

    fun followArtist(artist: Artist) {
        viewModelScope.launch {
            try {
                val isFollowed = artist.followed
                updateArtistFollowing(artist)
                if (isFollowed) {
                    artistRepository.unFollowArtist(artist.id, sharePreferencesManager.userId)
                } else {
                    artistRepository.followArtist(artist.id, sharePreferencesManager.userId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
                updateArtistFollowing(artist)
            }
        }
    }

    private fun updateArtistFollowing(artist: Artist) {
        val artistTraditionalSource = _traditionalArtist.find { it.id == artist.id }
        artistTraditionalSource?.let { artistSrc ->
            artistSrc.followed = !artistSrc.followed
        }

        val artistNftSource = _nftArtist.find { it.id == artist.id }
        artistNftSource?.let { artistSrc ->
            artistSrc.followed = !artistSrc.followed
        }

        when (storeTypeSelected.get()) {
            StoreTypeEnum.TRADITIONAL_ART -> {
                sendAction(Action.LoadingTraditionalArtistSuccess(_traditionalArtist))
            }
            StoreTypeEnum.NFT_ART -> {
                sendAction(Action.LoadingNFTArtistSuccess(_nftArtist))
            }
        }

    }

    /**
     * Reflect to the change when click on "On Sale", "Upcoming Deals", "Past Deals"
     * Add [typeEnum] to like type
     * @return like type
     * */
    fun changeTypeEnum(searchTypeEnum: SearchTypeEnum) {
        if (searchTypeEnum == searchTypeSelected.get()) return
        searchTypeSelected.set(searchTypeEnum)
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
        val traditionalArtsOnsale: ArrayList<Art>? = arrayListOf(),
        val traditionalArtsUpcomingDeals: ArrayList<Art>? = arrayListOf(),
        val traditionalArtsPastDeals: ArrayList<Art>? = arrayListOf(),
        val traditionalArtist: ArrayList<Artist>? = arrayListOf(),
        val nftArtsOnsale: ArrayList<Art>? = arrayListOf(),
        val nftArtsUpcomingDeals: ArrayList<Art>? = arrayListOf(),
        val nftArtsPastDeals: ArrayList<Art>? = arrayListOf(),
        val nftArtist: ArrayList<Artist>? = arrayListOf(),
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
        class LoadingTraditionalArtOnsaleSuccess(val traditionalArtsOnsale: ArrayList<Art>) : Action()
        class LoadingTraditionalArtUpcomingDealsSuccess(val traditionalArtsUpcomingDeals: ArrayList<Art>) : Action()
        class LoadingTraditionalArtPastDealsSuccess(val traditionalArtsPastDeals: ArrayList<Art>) : Action()
        class LoadingTraditionalArtistSuccess(val traditionalArtist:  ArrayList<Artist>) : Action()
        class LoadingNFTArtOnsaleSuccess(val nftArtsOnsale: ArrayList<Art>) : Action()
        class LoadingNFTArtUpcomingDealsSuccess(val nftArtsUpcomingDeals: ArrayList<Art>) : Action()
        class LoadingNFTArtPastDealsSuccess(val nftArtsPastDeals: ArrayList<Art>) : Action()
        class LoadingNFTArtistSuccess(val nftArtist:  ArrayList<Artist>) : Action()
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
        is Action.LoadingTraditionalArtOnsaleSuccess -> state.copy(
            traditionalArtsOnsale = viewAction.traditionalArtsOnsale,
            nftArtsOnsale = null,
            isHaveData = viewAction.traditionalArtsOnsale.isNotEmpty()
        )
        is Action.LoadingTraditionalArtUpcomingDealsSuccess -> state.copy(
            traditionalArtsUpcomingDeals = viewAction.traditionalArtsUpcomingDeals,
            nftArtsUpcomingDeals = null,
            isHaveData = viewAction.traditionalArtsUpcomingDeals.isNotEmpty()
        )
        is Action.LoadingTraditionalArtPastDealsSuccess -> state.copy(
            traditionalArtsPastDeals = viewAction.traditionalArtsPastDeals,
            nftArtsPastDeals = null,
            isHaveData = viewAction.traditionalArtsPastDeals.isNotEmpty()
        )
        is Action.LoadingTraditionalArtistSuccess -> state.copy(
            traditionalArtist = viewAction.traditionalArtist,
            nftArtist = null,
            isHaveData = viewAction.traditionalArtist.isNotEmpty()
        )
        is Action.LoadingNFTArtOnsaleSuccess -> state.copy(
            nftArtsOnsale = viewAction.nftArtsOnsale,
            traditionalArtsOnsale = null,
            isHaveData = viewAction.nftArtsOnsale.isNotEmpty()
        )
        is Action.LoadingNFTArtUpcomingDealsSuccess -> state.copy(
            nftArtsUpcomingDeals = viewAction.nftArtsUpcomingDeals,
            traditionalArtsUpcomingDeals = null,
            isHaveData = viewAction.nftArtsUpcomingDeals.isNotEmpty()
        )
        is Action.LoadingNFTArtPastDealsSuccess -> state.copy(
            nftArtsPastDeals = viewAction.nftArtsPastDeals,
            traditionalArtsPastDeals = null,
            isHaveData = viewAction.nftArtsPastDeals.isNotEmpty()
        )
        is Action.LoadingNFTArtistSuccess -> state.copy(
            nftArtist = viewAction.nftArtist,
            traditionalArtist = null,
            isHaveData = viewAction.nftArtist.isNotEmpty()
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
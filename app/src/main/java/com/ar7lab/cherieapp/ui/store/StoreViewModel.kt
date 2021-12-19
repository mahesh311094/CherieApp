package com.ar7lab.cherieapp.ui.store

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.StoreTypeEnum
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.Artist
import com.ar7lab.cherieapp.model.BannersItem
import com.ar7lab.cherieapp.network.repositories.ArtRepository
import com.ar7lab.cherieapp.network.repositories.ArtistRepository
import com.ar7lab.cherieapp.network.repositories.CommonRepository
import com.ar7lab.cherieapp.utils.FILTER_NFT_ART
import com.ar7lab.cherieapp.utils.FILTER_TRADITIONAL_ART
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class StoreViewModel @Inject constructor(
    private val artRepository: ArtRepository,
    private val artistRepository: ArtistRepository,
    private val commonRepository: CommonRepository,
    private val sharePreferencesManager: SharePreferencesManager
) :
    BaseViewModel<StoreViewModel.ViewState, StoreViewModel.Action>(ViewState()) {

    // Save the selected art type
    var storeTypeSelected = ObservableField(StoreTypeEnum.TRADITIONAL_ART)
    private val _forYouArts = ArrayList<Art>()

    //Traditional Art Section
    private val _traditionalArtsOnSale = ArrayList<Art>()
    private val _traditionalArtsUpcomingDeals = ArrayList<Art>()
    private val _traditionalArtsPastDeals = ArrayList<Art>()

    //NFT Art Section
    private val _nftArtsOnsale = ArrayList<Art>()
    private val _nftArtsUpcomingDeals = ArrayList<Art>()
    private val _nftArtsPastDeals = ArrayList<Art>()
    private val _traditionalArtists = ArrayList<Artist>()
    private val _nftArtists = ArrayList<Artist>()
    private val _bannerList = ArrayList<BannersItem>()
    private var _query = ""
    private var _page = 1
    private var _limit = 10
    private val _scrollPosition = MutableLiveData(0)

    //hide and show search bar
    var searchShow = ObservableField(false)

    // For Banner
    var bannerList = ArrayList<String>()
    var adapter = BannerAdapter(bannerList)
    var isNotify = ObservableField(false)

    //This is for the banner count
    var bannerCount = ObservableField(0)
    var bannerCurrentPosition = ObservableField(0)

    //This is for hide new arrivals while search
    var needHorizontal = ObservableField(true)

    fun init() {
        when (storeTypeSelected.get()) {
            StoreTypeEnum.TRADITIONAL_ART -> {
                if (_traditionalArtsOnSale.isEmpty() || _traditionalArtsUpcomingDeals.isEmpty() || _traditionalArtsPastDeals.isEmpty()) {
                    loadData()
                }
            }
            StoreTypeEnum.NFT_ART -> {
                if (_nftArtsOnsale.isEmpty() || _nftArtsUpcomingDeals.isEmpty() || _nftArtsPastDeals.isEmpty()) {
                    loadData()
                }
            }
        }
    }

    fun onAutoScrollBanner() {
        if (bannerCurrentPosition.get()!! >= bannerList.size - 1) {
            bannerCurrentPosition.set(0)
        } else {
            bannerCurrentPosition.set(bannerCurrentPosition.get()?.plus(1))
        }
    }

    override fun onLoadData() {
        viewModelScope.launch {
            try {
                getBannerList()
                loadArt()
                when (storeTypeSelected.get()) {
                    /*StoreTypeEnum.FOR_YOU -> {
                        loadTraditionalArtist()
                        loadNFTArtist()
                    }*/
                    StoreTypeEnum.TRADITIONAL_ART -> {
                        sendAction(
                            Action.LoadingNFTArtistSuccess(arrayListOf())
                        )
                        loadTraditionalArtist()
                    }
                    StoreTypeEnum.NFT_ART -> {
                        sendAction(Action.LoadingTraditionalArtistSuccess(arrayListOf()))
                        loadNFTArtist()
                    }
                }

                sendAction(Action.LoadingCompletelySuccess(true))
            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }
    }

    private suspend fun loadArt() {
        // if refreshing, then clear the current data for arts
        if (state.isRefreshing) {
            when (storeTypeSelected.get()) {
                //StoreTypeEnum.FOR_YOU -> _forYouArts.clear()
                StoreTypeEnum.TRADITIONAL_ART -> {
                    _traditionalArtsOnSale.clear()
                    _traditionalArtsUpcomingDeals.clear()
                    _traditionalArtsPastDeals.clear()
                }
                StoreTypeEnum.NFT_ART -> {
                    _nftArtsOnsale.clear()
                    _nftArtsUpcomingDeals.clear()
                    _nftArtsPastDeals.clear()
                }
            }
        }

        if (_query != "") {
            needHorizontal.set(false)
        } else {
            needHorizontal.set(true)
        }

        when (storeTypeSelected.get()) {
            StoreTypeEnum.TRADITIONAL_ART -> {
                // get arts based on user input or not
                val response = artRepository.searchArtsStore(
                    _page,
                    _limit,
                    FILTER_TRADITIONAL_ART,
                    "",
                    "",
                    _query
                )
                response.data?.onSaleArts?.let {
                    _traditionalArtsOnSale.clear()
                    _traditionalArtsOnSale.addAll(it)
                    sendAction(Action.LoadingTraditionalArtOnsaleSuccess(_traditionalArtsOnSale))
                }
                response.data?.upcomingArts?.let {
                    _traditionalArtsUpcomingDeals.clear()
                    _traditionalArtsUpcomingDeals.addAll(it)
                    sendAction(Action.LoadingTraditionalArtUpcomingDealsSuccess(_traditionalArtsUpcomingDeals))

                }
                response.data?.pastArts?.let {
                    _traditionalArtsPastDeals.clear()
                    _traditionalArtsPastDeals.addAll(it)
                    sendAction(Action.LoadingTraditionalArtPastDealsSuccess(_traditionalArtsPastDeals))
                }
            }
            StoreTypeEnum.NFT_ART -> {
                // get arts based on user input or not
                val response = artRepository.searchArtsStore(
                    _page,
                    _limit,
                    FILTER_NFT_ART,
                    "",
                    "",
                    _query
                )
                response.data?.onSaleArts?.let { arts ->
                    _nftArtsOnsale.clear()
                    _nftArtsOnsale.addAll(arts)
                    sendAction(Action.LoadingNFTArtOnsaleSuccess(_nftArtsOnsale))
                }
                response.data?.upcomingArts?.let { arts ->
                    _nftArtsUpcomingDeals.clear()
                    _nftArtsUpcomingDeals.addAll(arts)
                    sendAction(Action.LoadingNFTArtUpcomingDealsSuccess(_nftArtsUpcomingDeals))
                }
                response.data?.pastArts?.let { arts ->
                    _nftArtsPastDeals.clear()
                    _nftArtsPastDeals.addAll(arts)
                    sendAction(Action.LoadingNFTArtPastDealsSuccess(_nftArtsPastDeals))
                }

            }
        }
    }

    private suspend fun loadTraditionalArtist() {
        val response = artistRepository.getStoreTopTraditionalArtists()
        response.data?.artists?.let {
            _nftArtists.clear()
            _traditionalArtists.clear()
            _traditionalArtists.addAll(it)
            sendAction(
                Action.LoadingTraditionalArtistSuccess(_traditionalArtists)
            )
        }
    }

    private suspend fun loadNFTArtist() {
        val response = artistRepository.getStoreTopNFTArtists()
        response.data?.artists?.let {
            _traditionalArtists.clear()
            _nftArtists.clear()
            _nftArtists.addAll(it)
            sendAction(
                Action.LoadingNFTArtistSuccess(_nftArtists)
            )
        }
    }

    private suspend fun getBannerList() {
        val filterType: String = when (storeTypeSelected.get()) {
            StoreTypeEnum.TRADITIONAL_ART -> {
                FILTER_TRADITIONAL_ART
            }
            StoreTypeEnum.NFT_ART -> {
                FILTER_NFT_ART
            }
            else -> {
                FILTER_TRADITIONAL_ART
            }
        }

        val response = commonRepository.getBanners(_page, _limit, filterType)
        response.data?.banners?.let {
            _bannerList.clear()
            _bannerList.addAll(it)
            sendAction(Action.LoadingBannerSuccess(_bannerList))
        }
    }

    fun search(query: String) {
        _query = query
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
                sendAction(Action.LoadingTraditionalArtPastDealsSuccess(_traditionalArtsPastDeals))
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
        val artistTASource = _traditionalArtists.find { it.id == artist.id }
        artistTASource?.let { artistSrc ->
            artistSrc.followed = !artistSrc.followed
            sendAction(Action.LoadingTraditionalArtistSuccess(_traditionalArtists))
        }
        val artistNFTSource = _nftArtists.find { it.id == artist.id }
        artistNFTSource?.let { artistSrc ->
            artistSrc.followed = !artistSrc.followed
            sendAction(Action.LoadingNFTArtistSuccess(_nftArtists))
        }
    }

    fun saveScrollPosition(position: Int) {
        // in case after view created, recyclerview invoke this method
        if (position < 2) return
        _scrollPosition.postValue(position)
    }

    fun getSavedScrollPosition(): Int = _scrollPosition.value ?: 0

    // Reflect to the change when click on "For you", "Traditional Art", "NTF Art"
    fun changeStoreType(storeTypeEnum: StoreTypeEnum) {
        if (storeTypeEnum == storeTypeSelected.get()) return
        storeTypeSelected.set(storeTypeEnum)
        onRefresh()
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isRefreshing: Boolean = true,
        val isLoadingMore: Boolean = true,
        val isError: Boolean = false,
        val isSessionExpired: Boolean = false,
        val message: String? = null,
        val isDataLoaded: Boolean = false,
        val isRefreshed: Boolean = false,
        val isHaveData: Boolean = false,
        val forYouArts: ArrayList<Art>? = arrayListOf(),
        val traditionalArtsOnsale: ArrayList<Art>? = arrayListOf(),
        val traditionalArtsUpcomingDeals: ArrayList<Art>? = arrayListOf(),
        val traditionalArtsPastDeals: ArrayList<Art>? = arrayListOf(),
        val nftArtsOnsale: ArrayList<Art>? = arrayListOf(),
        val nftArtsUpcomingDeals: ArrayList<Art>? = arrayListOf(),
        val nftArtsPastDeals: ArrayList<Art>? = arrayListOf(),
        val traditionalArtists: ArrayList<Artist>? = arrayListOf(),
        val bannerList: ArrayList<BannersItem>? = arrayListOf(),
        val ntfArtists: ArrayList<Artist>? = arrayListOf(),
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartRefreshing : Action()
        object StartLoadingMore : Action()
        class LoadingForYouArtSuccess(val arts: ArrayList<Art>) : Action()
        class LoadingTraditionalArtOnsaleSuccess(val traditionalArtsOnsale: ArrayList<Art>) : Action()
        class LoadingTraditionalArtUpcomingDealsSuccess(val traditionalArtsUpcomingDeals: ArrayList<Art>) : Action()
        class LoadingTraditionalArtPastDealsSuccess(val traditionalArtsPastDeals: ArrayList<Art>) : Action()
        class LoadingNFTArtOnsaleSuccess(val nftArtsOnsale: ArrayList<Art>) : Action()
        class LoadingNFTArtUpcomingDealsSuccess(val nftArtsUpcomingDeals: ArrayList<Art>) : Action()
        class LoadingNFTArtPastDealsSuccess(val nftArtsPastDeals: ArrayList<Art>) : Action()
        class LoadingTraditionalArtistSuccess(val traditionalArtists: ArrayList<Artist>) : Action()
        class LoadingBannerSuccess(val bannerList: ArrayList<BannersItem>) : Action()
        class LoadingNFTArtistSuccess(val ntfArtists: ArrayList<Artist>) : Action()
        class LoadingCompletelySuccess(val isRefreshed: Boolean = false) : Action()
        class LoadingFailure(val message: String, val isSessionExpired: Boolean) : Action()
    }

    // Return a state based on action
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
        is Action.LoadingForYouArtSuccess -> state.copy(
            forYouArts = viewAction.arts,
            traditionalArtsOnsale = null,
            nftArtsOnsale = null,
            isHaveData = viewAction.arts.isNotEmpty()
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
        is Action.LoadingTraditionalArtistSuccess -> state.copy(
            traditionalArtists = viewAction.traditionalArtists
        )
        is Action.LoadingBannerSuccess -> state.copy(
            bannerList = viewAction.bannerList
        )
        is Action.LoadingNFTArtistSuccess -> state.copy(
            ntfArtists = viewAction.ntfArtists
        )
        is Action.LoadingCompletelySuccess -> state.copy(
            isRefreshing = false,
            isLoadingMore = false,
            isDataLoaded = true,
            isError = false,
            isSessionExpired = false,
            message = null
        )
        is Action.LoadingFailure -> state.copy(
            isRefreshing = false,
            isLoadingMore = false,
            isError = true,
            message = viewAction.message,
            isSessionExpired = viewAction.isSessionExpired,
            isDataLoaded = false,
            isHaveData = false
        )
    }
}
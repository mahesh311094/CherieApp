package com.ar7lab.cherieapp.ui.nftartistprofile

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.NftArtistArtsTypeEnum
import com.ar7lab.cherieapp.enums.TopArtistsTypeEnum
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.Artist
import com.ar7lab.cherieapp.network.repositories.ArtRepository
import com.ar7lab.cherieapp.network.repositories.ArtistRepository
import com.ar7lab.cherieapp.utils.ONSELL
import com.ar7lab.cherieapp.utils.OWNED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * View Model for NFT Artist Profile
 * @property artistRepository for get Artist data after API calling
 * @property artRepository for get Art data after API calling
 * @property sharePreferencesManager for get the saved value from app local db
 * */
@HiltViewModel
internal class NFTArtistProfileViewModel @Inject constructor(
    private val artistRepository: ArtistRepository,
    private val artRepository: ArtRepository,
    private val sharePreferencesManager: SharePreferencesManager
) : BaseViewModel<NFTArtistProfileViewModel.ViewState, NFTArtistProfileViewModel.Action>(
    ViewState()
) {

    //Initialize default nft artist type "Onsale"
    var nftArtistArtsTypeSelected = ObservableField(NftArtistArtsTypeEnum.ON_SALE)

    //Initialize Artist object
    private var _artist: Artist? = null

    //Initialize Art object for Traditional
    private val _artTraditional = ArrayList<Art>()

    //Initialize Art object for Onsale
    private val _artNFTOnsale = ArrayList<Art>()

    //Initialize Art object for Owned
    private val _artNFTOwned = ArrayList<Art>()
    private var _page = 1

    //Set date load limit for each time
    private var _limit = 10

    //Initialize default artist type "Traditional"
    var _topArtistTypeEnum: TopArtistsTypeEnum = TopArtistsTypeEnum.TRADITIONAL

    //Show the total count if the sales and owned
    var salesCount = ObservableField(0)
    var ownedCount = ObservableField(0)

    //Show no artwork
    var totalArtwork = ObservableField(0)

    fun init(artist: Artist?, topArtistTypeEnum: TopArtistsTypeEnum) {
        //Set Artist object
        _artist = artist
        //Set Artist type
        _topArtistTypeEnum = topArtistTypeEnum
        Timber.e("Type: $topArtistTypeEnum")
        when (_topArtistTypeEnum) {
            TopArtistsTypeEnum.TRADITIONAL -> {
                loadData()// When artist type is "Traditional"
            }
            TopArtistsTypeEnum.NFT -> {
                when (nftArtistArtsTypeSelected.get()) {
                    NftArtistArtsTypeEnum.ON_SALE -> {
                        /*
                        * When Artist type is "Nft"
                        * When select "Onsale" tab
                        * */
                        if (_artNFTOnsale.isEmpty()) {
                            loadData()
                        }
                    }
                    NftArtistArtsTypeEnum.OWNED -> {
                        /*
                        * When Artist type is "Nft"
                        *  When select "Owned" tab
                        * */
                        if (_artNFTOwned.isEmpty()) {
                            loadData()
                        }
                    }
                }
            }
        }
    }

    override fun onLoadData() {
        viewModelScope.launch {
            try {
                //Get Artist details from API call
                getArtistDetails()
                //Gey Arts by Artist id from API call
                getArtsByArtistId()
                when (_topArtistTypeEnum) {
                    TopArtistsTypeEnum.TRADITIONAL -> {
                        //When Artist type is Tradition, load Traditional Arts
                        loadTraditionalArts()
                    }
                    TopArtistsTypeEnum.NFT -> {
                        when (nftArtistArtsTypeSelected.get()) {
                            NftArtistArtsTypeEnum.ON_SALE -> {
                                /*
                                * When Artist type is Nft
                                * Wnen select "Onsale" tab load Nft Arts
                                *  */
                                loadNFTOnsaleArts()
                            }
                            NftArtistArtsTypeEnum.OWNED -> {
                                /*
                                * When Artist type is Nft
                                * Wnen select "Owned" tab load Nft Arts
                                * */
                                loadNFTOwnedArts()
                            }
                        }
                    }
                }
                sendAction(Action.LoadingCompletelySuccess(true))
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    /**
     * Get Artist details
     * @return Artist list
     * */
    private suspend fun getArtistDetails() {
        val response = artistRepository.getArtistDetails(_artist?.id ?: "")
        sendAction(Action.LoadingSuccess(response.data?.artist))
    }

    //Get Arts by Artist id
    private suspend fun getArtsByArtistId() {
        if (state.isRefreshing) {
            when (_topArtistTypeEnum) {
                TopArtistsTypeEnum.TRADITIONAL -> _artTraditional.clear()   //Clear Traditional list data
                TopArtistsTypeEnum.NFT -> {
                    when (nftArtistArtsTypeSelected.get()) {
                        NftArtistArtsTypeEnum.ON_SALE -> {
                            _artNFTOnsale.clear() //Clear Nft onsale list data
                        }
                        NftArtistArtsTypeEnum.OWNED -> {
                            _artNFTOwned.clear()  //Clear Nft onsale Owned data
                        }
                    }
                }
            }
        }
        when (_topArtistTypeEnum) {
            TopArtistsTypeEnum.TRADITIONAL -> {
                //Get Tradition Arts by Artist id from API calling
                val response = artRepository.getArtsByArtistIdTraditional(
                    _page, _limit, _artist?.id ?: ""
                )
                response.data?.arts?.let {
                    if (state.isLoadingMore && it.isEmpty()) {
                        //Send action is when arts list is empty
                        sendAction(Action.LoadingFailure("No more data!",false))
                    } else {
                        //Add Arts data
                        _artTraditional.addAll(it)
                        sendAction(Action.LoadingForArtistArtsSuccess(_artTraditional))
                    }
                }
            }
            TopArtistsTypeEnum.NFT -> {
                when (nftArtistArtsTypeSelected.get()) {
                    NftArtistArtsTypeEnum.ON_SALE -> {
                        /*
                        * When Artist type is "Nft"
                        * When selet "Onsale" tab
                        * Get Nft Artist art by artist id
                        * */
                        val response = artRepository.getArtsByArtistId(
                            _page, _limit, _artist?.id ?: "", ONSELL
                        )
                        response.data?.arts?.let {
                            if (state.isLoadingMore && it.isEmpty()) {
                                sendAction(Action.LoadingFailure("No more data!",false))
                            } else {
                                _artNFTOnsale.addAll(it)
//                                salesCount.set(_artNFTOnsale.size)
                                sendAction(Action.LoadingForArtistArtsSuccess(_artNFTOnsale))
                            }
                        }
                    }
                    NftArtistArtsTypeEnum.OWNED -> {
                        /*
                        * When Artist type is "Nft"
                        * When selet "Owned" tab
                        * Get Nft Artist art by artist id
                        * */
                        val response = artRepository.getArtsByArtistId(
                            _page, _limit, _artist?.id ?: "", OWNED
                        )
                        response.data?.arts?.let {
                            if (state.isLoadingMore && it.isEmpty()) {
                                sendAction(Action.LoadingFailure("No more data!",false))
                            } else {
                                _artNFTOwned.addAll(it)
//                                ownedCount.set(_artNFTOwned.size)
                                sendAction(Action.LoadingForArtistArtsSuccess(_artNFTOwned))
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get Traditional Arts
     * @return Traditional Arts
     * */
    private suspend fun loadTraditionalArts() {
        val response = artRepository.getArtsByArtistIdTraditional(
            _page, _limit, _artist?.id ?: ""
        )
        response.data?.arts?.let {
            _artTraditional.clear()
            _artTraditional.addAll(it)
            totalArtwork.set(_artTraditional.size)
            sendAction(Action.LoadingForArtistArtsSuccess(_artTraditional))
        }
    }

    /**
     * Get Nft Onsale arts
     * @return Nft Onsale Arts
     * */
    private suspend fun loadNFTOnsaleArts() {
        val response = artRepository.getArtsByArtistId(
            _page, _limit, _artist?.id ?: "", ONSELL
        )
        response.data?.arts?.let {
            _artNFTOnsale.clear()
            _artNFTOnsale.addAll(it)
            salesCount.set(response.data!!.totalCount)
            sendAction(Action.LoadingForArtistArtsSuccess(_artNFTOnsale))
        }
    }

    /**
     * Get Nft Owned Arts
     * @return Nft Owned Arts
     * */
    private suspend fun loadNFTOwnedArts() {
        val response = artRepository.getArtsByArtistId(
            _page, _limit, _artist?.id ?: "", OWNED
        )
        response.data?.arts?.let {
            _artNFTOwned.clear()
            _artNFTOwned.addAll(it)
            ownedCount.set(response.data!!.totalCount)
            sendAction(Action.LoadingForArtistArtsSuccess(_artNFTOwned))

        }
    }

    /**
     * Refresh layout call
     * @return load data
     * */
    fun onRefresh() {
        _page = 1
        sendAction(Action.StartRefreshing)
        loadData()
    }

    /**
     * Load more date on "Show More" button click
     * @return load data as per limit. Ex. If limit = 10 after calling page=1, the total data will be 10+10 = 20
     * */
    fun loadMore() {
        _page++
        sendAction(Action.StartLoadingMore)
        viewModelScope.launch {
            try {
                //Get Arts by Artist id
                getArtsByArtistId()
                sendAction(Action.LoadingCompletelySuccess())
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))

            }
        }
    }

    /**
     * Like Artist
     * Add [artist] to this like.
     * @return liked artist
     * */
    fun likeArtist(artist: Artist) {
        viewModelScope.launch {
            try {
                val isLiked = artist.liked
                //like/unlike API calling
                if (isLiked) {
                    artistRepository.unLikeArtist(artist.id)
                } else {
                    artistRepository.likeArtist(artist.id)
                }
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
                loadData()
            }
        }
    }

    /**
     * Like Art
     * Add [art] to this like.
     * @return liked art
     * */
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

    /**
     * Update like art
     * Add [artId] to this updateLikeArt.
     * @return liked art count and icon
     * */
    private fun updateLikeArt(artId: String) {
        //When Art Source is Traditional
        val artSourceTraditional = _artTraditional.find { it.id == artId }
        artSourceTraditional?.let {
            it.liked = !it.liked
            it.likeCount += if (it.liked) 1 else -1
        }
        //When Art source is Nft onsale
        val artSourceNFTOnsla = _artNFTOnsale.find { it.id == artId }
        artSourceNFTOnsla?.let {
            it.liked = !it.liked
            it.likeCount += if (it.liked) 1 else -1
        }
        //When Art source is Nft owned
        val artSourceNFTOwned = _artNFTOwned.find { it.id == artId }
        artSourceNFTOwned?.let {
            it.liked = !it.liked
            it.likeCount += if (it.liked) 1 else -1
        }

        when (_topArtistTypeEnum) {
            TopArtistsTypeEnum.TRADITIONAL -> {
                sendAction(Action.LoadingForArtistArtsSuccess(_artTraditional)) //Load data when artist type is traditional
            }
            TopArtistsTypeEnum.NFT -> {
                when (nftArtistArtsTypeSelected.get()) {
                    NftArtistArtsTypeEnum.ON_SALE -> {
                        sendAction(Action.LoadingForArtistArtsSuccess(_artNFTOnsale)) //Load date when artist type is Nft and Onsale tab is selected
                    }
                    NftArtistArtsTypeEnum.OWNED -> {
                        sendAction(Action.LoadingForArtistArtsSuccess(_artNFTOwned))  //Load date when artist type is Nft and Owned tab is selected
                    }
                }
            }
        }
    }

    /**
     * Follow Artist
     * Add [artId] to this updateLikeArt.
     * @return liked art count and icon
     * */
    fun followArtist(artist: Artist) {
        viewModelScope.launch {
            try {
                val isFollowed = artist.followed
                //Follow artist
                if (isFollowed) {
                    //Unfollow artist
                    artistRepository.unFollowArtist(artist.id, sharePreferencesManager.userId)
                } else {
                    //Follow artist
                    artistRepository.followArtist(artist.id, sharePreferencesManager.userId)
                }
                loadData()
            } catch (e: Exception) {
                e.printStackTrace()
                //send action when loading is failure
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
                loadData()
            }
        }
    }

    /**
     * Reflect to the change when click on "Onsale", "Owned"
     * Add [nftArtistArtsTypeEnum] to get nft artist type
     * @return nft artist type
     * */
    fun changeNftArtistTypeEnum(nftArtistArtsTypeEnum: NftArtistArtsTypeEnum) {
        if (nftArtistArtsTypeEnum == nftArtistArtsTypeSelected.get()) return
        nftArtistArtsTypeSelected.set(nftArtistArtsTypeEnum)
        onRefresh()
    }


    /**
     * Define view state for Activity to use
     * @return state
     * */
    internal data class ViewState(
        val isRefreshing: Boolean = true,
        val isLoadingMore: Boolean = true,
        val isDataLoaded: Boolean = false,
        val isRefreshed: Boolean = false,
        val isError: Boolean = false,
        val isSessionExpired: Boolean = false,
        val message: String? = null,
        val artist: Artist? = null,
        val art: ArrayList<Art>? = arrayListOf(),
        val isHaveData: Boolean = false
    ) : BaseViewState

    /**
     * Define action of the ViewModel
     * @return action
     * */
    internal sealed class Action : BaseAction {
        object StartRefreshing : Action()
        object StartLoadingMore : Action()
        class LoadingSuccess(val artist: Artist?) : Action()
        class LoadingFailure(val message: String,val isSessionExpired: Boolean) : Action()
        class LoadingForArtistArtsSuccess(val art: ArrayList<Art>) : Action()
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
            message = null
        )
        is Action.LoadingSuccess -> state.copy(
            isRefreshing = false,
            isSessionExpired = false,
            isError = false,
            message = null,
            artist = viewAction.artist
        )
        is Action.LoadingFailure -> state.copy(
            isRefreshing = false,
            isError = true,
            isSessionExpired = viewAction.isSessionExpired,
            message = viewAction.message,
            isLoadingMore = false,
            isDataLoaded = false,
        )
        is Action.LoadingForArtistArtsSuccess -> state.copy(
            isRefreshing = false,
            art = viewAction.art,
            isHaveData = viewAction.art.isNotEmpty()
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
        is Action.LoadingCompletelySuccess -> state.copy(
            isRefreshing = false,
            isLoadingMore = false,
            isDataLoaded = true,
            isError = false,
            isSessionExpired = false,
            message = null
        )
    }
}
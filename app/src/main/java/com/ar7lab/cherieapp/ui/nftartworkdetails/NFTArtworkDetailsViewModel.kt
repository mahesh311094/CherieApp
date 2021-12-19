package com.ar7lab.cherieapp.ui.nftartworkdetails

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.ArtworkDetailTabEnum
import com.ar7lab.cherieapp.enums.StoreTypeEnum
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.Artist
import com.ar7lab.cherieapp.model.CommentDetail
import com.ar7lab.cherieapp.network.repositories.ArtRepository
import com.ar7lab.cherieapp.network.repositories.ArtistRepository
import com.ar7lab.cherieapp.ui.traditionalartworkdetails.TraditionalArtworkDetailsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
* Nft artwork details view model
* */
@HiltViewModel
internal class NFTArtworkDetailsViewModel @Inject constructor(
    private val artRepository: ArtRepository,
    private val artistRepository: ArtistRepository,
    private val sharePreferencesManager: SharePreferencesManager
) :
    BaseViewModel<NFTArtworkDetailsViewModel.ViewState,
            NFTArtworkDetailsViewModel.Action>(ViewState()) {

    //Initialize Art object
    private var _art: Art? = null
    private var _page = 1
    //Set load data limit for each time
    private var _limit = 5
    private var _artPage = 1
    //Set load art data limit for each time
    private var _artLimit = 5
    //Initialize comments list
    private val _comments: ArrayList<CommentDetail> = arrayListOf()
    val commentTyped = ObservableField("")
    //Initialize Arts for Traditional arts
    private val _traditionalArts = ArrayList<Art>()

    //Like Count
    val artLikeCount = ObservableField(0)

    //Post is likes or not
    val artLiked = ObservableBoolean(false)

    //Comment Count
    val artCommentCount = ObservableField(0)

    // Save the selected tab type
    var tabTypeSelected = ObservableField(ArtworkDetailTabEnum.NONE_TAB)

    //Store the total image count of the fileurls to show dots
    var totalFileUrls =  ObservableField(0)

    var restoreHeight =  ObservableField(false)

    fun init(art: Art?) {
        // Don't need to load data every time the fragment is visible if the data already there
        if (_art != null) {
            return
        }
        _art = art
        onRefresh()
    }

    override fun onLoadData() {
        super.onLoadData()
        viewModelScope.launch {
            try {
                val response = artRepository.getArtDetails(_art?.id ?: "")
                _art = response.data?.art
                _art?.let {
                    artLiked.set(it.liked)
                    artLikeCount.set(it.likeCount)
                    totalFileUrls.set(it.fileUrls.size)
                }
                sendAction(Action.LoadingSuccess(response.data?.art))
                // load arts for artist
                loadNftArts()
                // load art's comments
                loadComments()
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    //Load comments from API calling
    private suspend fun loadComments() {
        val response = artRepository.getArtComments(_art?.id ?: "", _page, _limit)
        response.data?.let { data ->
            if (_page == 1) {
                _comments.clear()
            }
            _comments.addAll(data.commentDetail)
            artCommentCount.set(_comments.size)
            sendAction(Action.LoadingCommentsSuccess(_comments))
            if (data.commentDetail.isEmpty()) {
                _page--
            }
        }
    }

    //Load more comments click on show more button
    fun loadMoreComments() {
        _page++
        sendAction(Action.StartLoadingMore)
        viewModelScope.launch {
            try {
                loadComments()
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e);
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    //Load more arts click on show more button click
    fun loadMoreArts() {
        _artPage++
        sendAction(Action.StartLoadingMoreArts)
        viewModelScope.launch {
            try {
                loadNftArts()
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    //Refresh layout call
    fun onRefresh() {
        _page = 1
        sendAction(Action.StartRefreshing)
        //load data
        loadData()
    }

    //Post comments using API calling
    fun sendComment() {
        if (commentTyped.get().isNullOrBlank()) {
            return
        }
        viewModelScope.launch {
            try {
                artRepository.sendArtComment(_art?.id ?: "", commentTyped.get() ?: "")
                commentTyped.set("")
                onRefresh()
            } catch (e: Exception) {
                e.printStackTrace()
                val error =getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    //Change tab for  INVESTMENT_INFO, WORK_AND_ARTIST_INFO
    fun changeTab(artworkDetailTabEnum: ArtworkDetailTabEnum) {
        if (tabTypeSelected.get() == artworkDetailTabEnum) return
        tabTypeSelected.set(artworkDetailTabEnum)
        sendAction(Action.ChangeTab(artworkDetailTabEnum))
    }

    //Load Nft arts
    private suspend fun loadNftArts() {
        val response = artRepository.getArtsByArtistIdNft(
            _artPage, _artLimit, _art?.artist?.id ?: ""
        )
        response.data?.arts?.let { arts ->
            // if refresh data, need to clear current data
            if (_artPage == 1) {
                _traditionalArts.clear()
            }
            _traditionalArts.addAll(arts)
            sendAction(Action.LoadingArtsSuccess(_traditionalArts))
            // if data empty, need to keep the current page
            if (arts.isEmpty()) {
                _artPage--
            }
        }
    }

    //Follow artist
    fun followArtist(artist: Artist) {
        viewModelScope.launch {
            try {
                val isFollowed = artist.followed
                //Update following artist
                updateArtistFollowing()
                if (isFollowed) {
                    //Unfollow artist
                    artistRepository.unFollowArtist(artist.id, sharePreferencesManager.userId)
                } else {
                    //Follow arist
                    artistRepository.followArtist(artist.id, sharePreferencesManager.userId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
                updateArtistFollowing()
            }
        }
    }

    private fun updateArtistFollowing() {
        // update followed
        _art?.artist?.let { artist ->
            artist.followed = !artist.followed
        }
        sendAction(Action.LoadingSuccess(_art))
    }

    fun likeThisArt() {
        viewModelScope.launch {
            try {
                if (artLiked.get()) {
                    updateLikeCountForThisArt()
                    artRepository.unLikeArt(_art?.id ?: "")
                } else {
                    updateLikeCountForThisArt()
                    artRepository.likeArt(_art?.id ?: "")
                }
            } catch (e: Exception) {
                updateLikeCountForThisArt()
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    private fun updateLikeCountForThisArt() {
        artLiked.set(!artLiked.get())
        val newCount = artLikeCount.get()!! + if (artLiked.get()) 1 else -1
        artLikeCount.set(newCount)
        _art?.liked = artLiked.get()
        _art?.likeCount = artLikeCount.get()!!
    }

    //Like arts
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

    //Update like arts
    private fun updateLikeArt(artId: String) {
        // find the art item in the array and then update liked
        _traditionalArts.find { it.id == artId }?.let {
            it.liked = !it.liked
            it.likeCount += if (it.liked) 1 else -1
        }
        sendAction(Action.LoadingArtsSuccess(_traditionalArts))
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isRefreshing: Boolean = true,
        val isLoadingMore: Boolean = false,
        val isLoadingMoreArts: Boolean = false,
        val isError: Boolean = false,
        val isSessionExpired: Boolean = false,
        val message: String? = null,
        val art: Art? = null,
        val comments: ArrayList<CommentDetail>? = null,
        val tabSelected: ArtworkDetailTabEnum = ArtworkDetailTabEnum.NONE_TAB,
        val arts: ArrayList<Art>? = null
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartRefreshing : Action()
        object StartLoadingMore : Action()
        object StartLoadingMoreArts : Action()
        class LoadingSuccess(val art: Art?) : Action()
        class LoadingCommentsSuccess(val comments: ArrayList<CommentDetail>?) : Action()
        class LoadingFailure(val message: String,val isSessionExpired:Boolean) : Action()
        class ChangeTab(val tabSelected: ArtworkDetailTabEnum) : Action()
        class LoadingArtsSuccess(val arts: ArrayList<Art>) : Action()
    }

    // Return a state based on action
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.StartRefreshing -> state.copy(
            isRefreshing = true,
            isError = false,
            isSessionExpired = false,
            message = null
        )
        Action.StartLoadingMore -> state.copy(
            isLoadingMore = true
        )
        is Action.StartLoadingMoreArts -> state.copy(
            isLoadingMoreArts = true,
            art = null,
        )
        is Action.LoadingSuccess -> state.copy(
            isRefreshing = false,
            isError = false,
            isSessionExpired = false,
            message = null,
            art = viewAction.art
        )
        is Action.LoadingCommentsSuccess -> state.copy(
            isRefreshing = false,
            isError = false,
            isSessionExpired = false,
            message = null,
            comments = viewAction.comments,
            isLoadingMore = false
        )
        is Action.LoadingFailure -> state.copy(
            isRefreshing = false,
            isLoadingMore = false,
            isError = true,
            isSessionExpired = viewAction.isSessionExpired,
            message = viewAction.message
        )
        is Action.ChangeTab -> state.copy(
            tabSelected = viewAction.tabSelected,
            art = null
        )
        is Action.LoadingArtsSuccess -> state.copy(
            arts = viewAction.arts,
            art = null,
            isLoadingMoreArts = false
        )
    }
}
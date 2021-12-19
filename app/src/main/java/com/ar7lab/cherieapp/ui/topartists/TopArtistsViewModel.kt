package com.ar7lab.cherieapp.ui.topartists

import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.TopArtistsTypeEnum
import com.ar7lab.cherieapp.model.Artist
import com.ar7lab.cherieapp.network.repositories.ArtistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class TopArtistsViewModel @Inject constructor(private val artistRepository: ArtistRepository, private val sharePreferencesManager: SharePreferencesManager) : BaseViewModel<TopArtistsViewModel.ViewState, TopArtistsViewModel.Action>(ViewState()) {
    private var _page = 1
    private var _limit = 9
    private var _topArtistsTypeEnum = TopArtistsTypeEnum.TRADITIONAL
    private val _artists = arrayListOf<Artist>()

    fun init(topArtistsTypeEnum: TopArtistsTypeEnum) {
        _topArtistsTypeEnum = topArtistsTypeEnum
    }

    override fun onLoadData() {
        super.onLoadData()
        viewModelScope.launch {
            try {
                val response = artistRepository.getArtists(_page, _limit)

                response.data?.let {
                    if (it.artists.isNotEmpty()) {
                        _artists.addAll(it.artists)
                        sendAction(Action.LoadingSuccess(_artists))
                    } else {
                        sendAction(Action.LoadingFailure("", false))
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }
    }

    fun onRefresh() {
        _page = 1
        _artists.clear()
        sendAction(Action.StartRefreshing)
        loadData()
    }

    fun loadMore() {
        _page++
        sendAction(Action.StartLoadingMore)
        loadData()
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
        val artistSource = _artists.find { it.id == artist.id }
        artistSource?.let { artistSrc ->
            artistSrc.followed = !artistSrc.followed
            sendAction(Action.LoadingSuccess(_artists))
        }
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isRefreshing: Boolean = true,
        val isLoadingMore: Boolean = false,
        val isDataLoaded: Boolean = false,
        val isError: Boolean = false,
        val isSessionExpired: Boolean = false,
        val message: String? = null,
        val artists: ArrayList<Artist> = arrayListOf()
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartRefreshing : Action()
        object StartLoadingMore : Action()
        class LoadingSuccess(val artists: ArrayList<Artist>) : Action()
        class LoadingFailure(val message: String, val isSessionExpired: Boolean) : Action()
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
            isLoadingMore = true,
        )
        is Action.LoadingSuccess -> state.copy(
            isRefreshing = false,
            isLoadingMore = true,
            isDataLoaded = viewAction.artists.isNotEmpty(),
            isError = false,
            isSessionExpired = false,
            message = null,
            artists = viewAction.artists
        )
        is Action.LoadingFailure -> state.copy(
            isRefreshing = false,
            isLoadingMore = false,
            isError = true,
            isSessionExpired = viewAction.isSessionExpired,
            message = viewAction.message
        )
    }
}
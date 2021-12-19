package com.ar7lab.cherieapp.ui.viewartlikes

import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.LikeDetail
import com.ar7lab.cherieapp.network.repositories.ArtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ViewArtLikesViewModel @Inject constructor(
    private val artRepository: ArtRepository
) : BaseViewModel<ViewArtLikesViewModel.ViewState, ViewArtLikesViewModel.Action>(ViewState()) {

    private var _page = 1
    private var _limit = 10
    private val _likes = ArrayList<LikeDetail>()

    private var _art: Art? = null

    fun init(art: Art) {
        _art = art

        if (_likes.isNullOrEmpty()) {
            onRefresh()
        }
    }

    override fun onLoadData() {
        super.onLoadData()
        viewModelScope.launch {
            try {
                if (_likes.isNullOrEmpty()) {
                    sendAction(Action.StartRefreshing)
                }
                val result = artRepository.getArtLikes(_art?.id ?: "", _page, _limit)
                result.data?.let { data ->
                    if (_page == 1) {
                        _likes.clear()
                    }
                    _likes.apply {
                        addAll(data.likeDetail)
                        sendAction(Action.LoadingSuccess(this))
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    /**
     * Refresh data
     */
    fun onRefresh() {
        _page = 1
        loadData()
    }

    fun loadMore() {
        _page++
        sendAction(Action.StartLoadingMore)
        loadData()
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isRefreshing: Boolean = true,
        val isLoadingMore: Boolean = false,
        val isError: Boolean = false,
        val isSessionExpired: Boolean = false,
        val message: String? = null,
        val likes: ArrayList<LikeDetail>? = null,
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartRefreshing : Action()
        object StartLoadingMore : Action()
        class LoadingSuccess(val likes: ArrayList<LikeDetail>?) : Action()
        class LoadingFailure(val message: String,val isSessionExpired: Boolean) : Action()
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
            isLoadingMore = false,
            isError = false,
            isSessionExpired = false,
            message = null,
            likes = viewAction.likes,
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
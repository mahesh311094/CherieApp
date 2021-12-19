package com.ar7lab.cherieapp.ui.viewartcomments

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.CommentDetail
import com.ar7lab.cherieapp.network.repositories.ArtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ViewArtCommentsViewModel @Inject constructor(
    private val artRepository: ArtRepository
) :
    BaseViewModel<ViewArtCommentsViewModel.ViewState, ViewArtCommentsViewModel.Action>(ViewState()) {

    private var _page = 1
    private var _limit = 10
    private val _comments = MutableLiveData<ArrayList<CommentDetail>>(arrayListOf())
    private val _scrollPosition = MutableLiveData(0)
    val commentTyped = ObservableField("")
    private var _art: Art? = null

    fun init(art: Art) {
        _art = art

        if (_comments.value.isNullOrEmpty()) {
            onRefresh()
        }
    }

    override fun onLoadData() {
        super.onLoadData()
        viewModelScope.launch {
            try {
                if (_comments.value.isNullOrEmpty()) {
                    sendAction(Action.StartRefreshing)
                }
                val result = artRepository.getArtComments(_art?.id ?: "", _page, _limit)
                result.data?.let { data ->
                    _comments.value?.apply {
                        addAll(data.commentDetail)
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

    fun onRefresh() {
        _page = 1
        _comments.value = arrayListOf()
        loadData()
    }

    fun loadMore() {
        _page++
        sendAction(Action.StartLoadingMore)
        loadData()
    }

    fun saveScrollPosition(position: Int) {
        // in case after view created, recyclerview invoke this method
        if (position == 0) return
        _scrollPosition.value = position
    }

    fun getSavedScrollPosition(): Int = _scrollPosition.value ?: 0

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
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
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
        val comments: ArrayList<CommentDetail>? = null,
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartRefreshing : Action()
        object StartLoadingMore : Action()
        class LoadingSuccess(val comments: ArrayList<CommentDetail>?) : Action()
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
            isLoadingMore = false,
            isDataLoaded = !viewAction.comments.isNullOrEmpty(),
            isError = false,
            isSessionExpired = false,
            message = null,
            comments = viewAction.comments,
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
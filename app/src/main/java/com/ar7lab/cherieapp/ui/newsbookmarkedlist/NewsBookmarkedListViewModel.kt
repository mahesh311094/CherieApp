package com.ar7lab.cherieapp.ui.newsbookmarkedlist

import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.model.News
import com.ar7lab.cherieapp.network.repositories.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class NewsBookmarkedListViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : BaseViewModel<NewsBookmarkedListViewModel.ViewState, NewsBookmarkedListViewModel.Action>(
    NewsBookmarkedListViewModel.ViewState()
) {

    private var _page = 1
    private var _limit = 10
    private val _news = ArrayList<News>()

    fun init() {
        onRefresh()
    }

    override fun onLoadData() {
        super.onLoadData()
        viewModelScope.launch {
            try {
                val response = newsRepository.getNewsBookmarked(_page, _limit)
                response.data?.let { data ->
                    // if refreshing, need to clear old data
                    if (_page == 1) {
                        _news.clear()
                    }
                    _news.addAll(data.bookmarkNewsData)
                    sendAction(Action.LoadingSuccess(_news))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    /**
     * refresh the list
     */
    fun onRefresh() {
        // reset the page to 1
        _page = 1
        sendAction(Action.StartRefreshing)
        loadData()
    }

    /**
     * Load more bookmarked news list
     */
    fun loadMore() {
        // do not load more when still refreshing
        if (state.isRefreshing) return
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
        val newsList: ArrayList<News>? = null,
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartRefreshing : Action()
        object StartLoadingMore : Action()
        class LoadingSuccess(val newsList: ArrayList<News>?) : Action()
        class LoadingFailure(val message: String,val isSessionExpired: Boolean) : Action()
    }

    // Return a state based on action
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.StartRefreshing -> state.copy(
            isRefreshing = true,
            isError = false,
            isSessionExpired = false,
            message = null,
        )
        is Action.StartLoadingMore -> state.copy(
            isLoadingMore = true,
        )
        is Action.LoadingSuccess -> state.copy(
            isRefreshing = false,
            isLoadingMore = false,
            isError = false,
            isSessionExpired = false,
            message = null,
            newsList = viewAction.newsList,
        )
        is Action.LoadingFailure -> state.copy(
            isRefreshing = false,
            isLoadingMore = false,
            isError = true,
            isSessionExpired = viewAction.isSessionExpired,
            message = viewAction.message,
        )
    }
}
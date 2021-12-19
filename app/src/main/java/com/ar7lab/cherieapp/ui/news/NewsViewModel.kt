package com.ar7lab.cherieapp.ui.news

import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.model.News
import com.ar7lab.cherieapp.model.NewsCategory
import com.ar7lab.cherieapp.network.repositories.NewsRepository
import com.ar7lab.cherieapp.ui.market.MarketViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : BaseViewModel<NewsViewModel.ViewState, NewsViewModel.Action>(
    ViewState()
) {

    private var _page = 1
    private var _limit = 10
    private val _news = ArrayList<News>()
    private val _categories = ArrayList<NewsCategory>()
    private var _filter: String? = null
    private var _selectedTabPosition = 0

    fun init() {
        loadData()
    }

    override fun onLoadData() {
        super.onLoadData()
        viewModelScope.launch {
            try {
                if (_categories.isEmpty()) {
                    sendAction(Action.StartRefreshing)
                    val result = newsRepository.getNewsCategory()
                    result.data?.let { data ->
                        // add filter for all
                        _categories.add(NewsCategory(name = "All"))
                        _categories.addAll(data.categoryList)
                        sendAction(Action.LoadingCategorySuccess(_categories))
                    }
                }

                // load data after categories loaded
                filter(_filter)
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    fun filter(filter: String?) {
        // filter all with empty value
        _filter = if (filter == "All" || filter == null) null else filter
        _page = 1
        sendAction(Action.StartRefreshing)
        viewModelScope.launch {
            try {
                getNewsList()
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    private suspend fun getNewsList() {
        val response = newsRepository.getNews(_page, _limit, _filter)
        response.data?.let { data ->
            // if refreshing, clear data first
            if (_page == 1) {
                _news.clear()
            }

            _news.addAll(data.newsList)
            sendAction(Action.LoadingSuccess(_news))
        }
    }

    fun onRefresh() {
        // reset the page to 1
        _page = 1
        sendAction(Action.StartRefreshing)
        filter(_filter)
    }

    fun loadMore() {
        // if refreshing, don't load more
        if (state.isRefreshing) return
        _page++
        viewModelScope.launch {
            try {
                getNewsList()
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    /**
     * save the selected position to restore UI later
     * @param position of the selected tab item
     */
    fun updateSelectedTabPosition(position: Int) {
        _selectedTabPosition = position
        sendAction(Action.UpdateSelectedTabPosition(_selectedTabPosition))
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isRefreshing: Boolean = true,
        val isLoadingMore: Boolean = false,
        val isError: Boolean = false,
        val isSessionExpired: Boolean = false,
        val message: String? = null,
        val newsList: ArrayList<News>? = null,
        val categories: ArrayList<NewsCategory>? = null,
        val selectedTabPosition: Int = 0
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartRefreshing : Action()
        object StartLoadingMore : Action()
        class LoadingSuccess(val newsList: ArrayList<News>?) : Action()
        class LoadingCategorySuccess(val categories: ArrayList<NewsCategory>?) : Action()
        class LoadingFailure(val message: String, val isSessionExpired: Boolean) : Action()
        class UpdateSelectedTabPosition(val selectedTabPosition: Int) : Action()
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
        is Action.LoadingCategorySuccess -> state.copy(
            categories = viewAction.categories,
            isRefreshing = false
        )
        is Action.LoadingFailure -> state.copy(
            isRefreshing = false,
            isLoadingMore = false,
            isError = true,
            isSessionExpired = viewAction.isSessionExpired,
            message = viewAction.message,
        )
        is Action.UpdateSelectedTabPosition -> state.copy(
            selectedTabPosition = viewAction.selectedTabPosition
        )
    }
}
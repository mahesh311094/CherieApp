package com.ar7lab.cherieapp.ui.newsdetails

import androidx.databinding.ObservableBoolean
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
internal class NewsDetailsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : BaseViewModel<NewsDetailsViewModel.ViewState, NewsDetailsViewModel.Action>(ViewState()) {

    private lateinit var _news: News
    val isBookmarked = ObservableBoolean(false)

    fun init(news: News, isBookmarked: Boolean) {
        loadNewsDetails(news.id)
        _news = news
        this.isBookmarked.set(news.isBookmarked || isBookmarked)
    }

    fun loadNewsDetails(newsId: String) {
        viewModelScope.launch {
            try {
                val response = newsRepository.getNewsDetails(newsId)
                response.data?.let { data ->
                    sendAction(Action.LoadingSuccess(data.news))
                    _news = data.news
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    fun addRemoveBookmark() {
        viewModelScope.launch {
            try {
                if (isBookmarked.get()) {
                    isBookmarked.set(false)
                    newsRepository.removeBookmark(_news.id)
                } else {
                    isBookmarked.set(true)
                    newsRepository.addBookmark(_news.id)
                }
            } catch (e: Exception) {
                isBookmarked.set(!isBookmarked.get())
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isError: Boolean = false,
        val isSessionExpired: Boolean = false,
        val message: String? = null,
        val news: News? = null
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        class LoadingFailure(val message: String,val isSessionExpired: Boolean) : Action()
        class LoadingSuccess(val news: News) : Action()
    }

    // Return a state based on action
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.LoadingFailure -> state.copy(
            isError = true,
            message = viewAction.message,
            isSessionExpired = viewAction.isSessionExpired
        )
        is Action.LoadingSuccess -> state.copy(
            news = viewAction.news
        )
    }
}
package com.ar7lab.cherieapp.ui.newsbookmarkedlist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.base.navigation.NavManager
import com.ar7lab.cherieapp.databinding.FragmentNewsBookmarkedListBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.itemNewsBookmarkedItem
import com.ar7lab.cherieapp.model.News
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.utils.openInfoDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NewsBookmarkedListFragment : BaseFragment(R.layout.fragment_news_bookmarked_list) {

    private val binding: FragmentNewsBookmarkedListBinding by viewBinding()

    private val viewModel: NewsBookmarkedListViewModel by viewModels()

    @Inject
    lateinit var navManager: NavManager

    private var _newsList: ArrayList<News> = arrayListOf()

    //share preference object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<NewsBookmarkedListViewModel.ViewState> { state ->
        binding.isRefreshing = state.isRefreshing
        if (state.isError) {
            showError(state.message)

        }

        if(state.isSessionExpired)
        {
            requireContext().openInfoDialog(layoutInflater,
                object : InfoDialogOkayButtonListener {
                    override fun onOkayButtonClicked() {
                        sharePreferencesManager.clearData()
                        startActivity(
                            Intent(requireContext(), SignInActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                    }
                })
        }

        // copy data from viewmodel to prevent crash epoxy recyclerview
        state.newsList?.let { news ->
            _newsList = news.map { it.copy() } as ArrayList
        }
        binding.isHaveData = _newsList.isNotEmpty()

        binding.rvItems.requestModelBuild()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)

        viewModel.init()

        createEpoxyRecyclerView()

        binding.tvBack.setOnDebouncedClickListener {
            findNavController().popBackStack()
        }
    }

    /**
     * Build list item views with epoxy recyclerview
     * @param
     * @return
     */
    private fun createEpoxyRecyclerView() {
        binding.rvItems.withModels {
            _newsList.forEachIndexed { index, news ->
                itemNewsBookmarkedItem {
                    id(news.title)
                    news(news)
                    listener(object : NewsListener {
                        override fun onNewsClicked(news: News) {
                            val action =
                                NewsBookmarkedListFragmentDirections.actionNewsBookmarkedListToNewsDetails(
                                    news, true
                                )
                            navManager.navigate(action)
                        }

                    })
                }
            }
        }

        // Add scroll event to load more data when reach the bottom
        binding.rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(1)) {
                    viewModel.loadMore()
                }
            }
        })
    }
}
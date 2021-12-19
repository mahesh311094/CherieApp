package com.ar7lab.cherieapp.ui.news

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.base.navigation.NavManager
import com.ar7lab.cherieapp.databinding.FragmentNewsBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.itemNewsItem
import com.ar7lab.cherieapp.model.News
import com.ar7lab.cherieapp.model.NewsCategory
import com.ar7lab.cherieapp.ui.dashboard.NotificationRedirectProfileListener
import com.ar7lab.cherieapp.ui.notification.NotificationFragment
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.ui.store.StoreFragmentDirections
import com.ar7lab.cherieapp.utils.REDIRECT
import com.ar7lab.cherieapp.utils.USER_PROFILE
import com.ar7lab.cherieapp.utils.openInfoDialog
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.lang.ClassCastException
import javax.inject.Inject

/**
 * This fragment used to display news screen when click on bottom tab
 */
@AndroidEntryPoint
class NewsFragment : BaseFragment(R.layout.fragment_news) {

    /**
     * View binding from fragment_news.xml
     */
    private val binding: FragmentNewsBinding by viewBinding()

    private val viewModel: NewsViewModel by viewModels()

    private var _newsList: ArrayList<News> = arrayListOf()
    private var _categories: ArrayList<NewsCategory> = arrayListOf()
    private var _selectedTabPosition = 0

    @Inject
    lateinit var navManager: NavManager

    //share preference object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    //Notification redirect listener
    lateinit var notificationReDirectProfileListener: NotificationRedirectProfileListener

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<NewsViewModel.ViewState> { state ->
        binding.isRefreshing = state.isRefreshing
        if (state.isError) {
            showError(state.message)

        }
        if (state.isSessionExpired) {
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

        state.categories?.let { categories ->
            _categories = categories.map { it.copy() } as ArrayList
            initTabLayoutFilter(_categories)
        }

        _selectedTabPosition = state.selectedTabPosition

        binding.rvItems.requestModelBuild()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)

        viewModel.init()

        initTabLayoutFilter(_categories)

        // show the list of news
        createEpoxyRecyclerView()

        //for fragment result redirection
        setFragmentResultListener(NotificationFragment.REQUEST_KEY) { key, bundle ->
            if (bundle.getString(REDIRECT)!!.equals(USER_PROFILE)) {
                notificationReDirectProfileListener.onNoficationRedirect()
            }
        }
        binding.ivNotification.setOnDebouncedClickListener {
            val action =
                NewsFragmentDirections
                    .actionNewsToNotification()
            navManager.navigate(action)
        }
        binding.ivSetting.setOnDebouncedClickListener {
            val action =
                NewsFragmentDirections
                    .actionNewsToPreferences()
            navManager.navigate(action)
        }
        binding.ivBookmarked.setOnDebouncedClickListener {
            val action = NewsFragmentDirections.actionNewsToNewsBookmarkedList()
            navManager.navigate(action)
        }

        // restore last selected position
        lifecycleScope.launchWhenResumed {
            // need a delay time to prevent some UI bugs
            delay(200L)
            binding.tabFilter.getTabAt(_selectedTabPosition)?.select()
        }

        /**
         * Init click event on tab filter, then load new data
         */
        lifecycleScope.launchWhenResumed {
            // need a delay time to prevent some UI bugs
            delay(300L)

            binding.tabFilter.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val filter = tab?.tag as NewsCategory
                    viewModel.filter(filter.name)
                    viewModel.updateSelectedTabPosition(tab.position)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}

            })
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            notificationReDirectProfileListener = activity as NotificationRedirectProfileListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement onSomeEventListener")
        }
    }

    /**
     * Set margin left and right for each tab item
     */
    private fun initTabLayoutFilter(categories: ArrayList<NewsCategory>) {
        // prevent rebuild tabs so many times
        if (binding.tabFilter.tabCount > 0) return

        categories.forEach {
            val tab = binding.tabFilter.newTab()
            tab.text = it.name
            tab.tag = it
            tab.contentDescription = it.name
            binding.tabFilter.addTab(tab)
        }

        val tabs = binding.tabFilter.getChildAt(0) as ViewGroup

        // add margin to each tab
        for (i in 0 until tabs.childCount) {
            val tab = tabs.getChildAt(i)
            val layoutParams = tab.layoutParams as LinearLayout.LayoutParams
            layoutParams.weight = 0f
            layoutParams.marginEnd = resources.getDimension(R.dimen.margin_small).toInt()
            layoutParams.marginStart = resources.getDimension(R.dimen.margin_small).toInt()
            tab.layoutParams = layoutParams
            binding.tabFilter.requestLayout()
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
                itemNewsItem {
                    id(news.title)
                    news(news)
                    listener(object : NewsListener {
                        override fun onNewsClicked(news: News) {
                            val action = NewsFragmentDirections.actionNewsToNewsDetails(news, false)
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

    //For reload the fragment when click on bottom icon
    fun reload() {
        binding.rvItems.smoothScrollToPosition(0)
        viewModel.onRefresh()
    }
}
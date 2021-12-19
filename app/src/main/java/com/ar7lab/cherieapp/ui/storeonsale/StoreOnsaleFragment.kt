package com.ar7lab.cherieapp.ui.storeonsale

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ar7lab.cherieapp.*
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.base.navigation.NavManager
import com.ar7lab.cherieapp.databinding.FragmentStoreOnsaleBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.ui.dashboard.NotificationRedirectProfileListener
import com.ar7lab.cherieapp.ui.notification.NotificationFragment
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * Marketplace Fragment for Traditional and Nft Art
 * @property fragment_market is the xml file for this fragment
 * */
@AndroidEntryPoint
class StoreOnsaleFragment : BaseFragment(R.layout.fragment_store_onsale) {

    //View binding of fragment_market
    private val binding: FragmentStoreOnsaleBinding by viewBinding()

    //Initialize StoreOnsale ViewModel
    private val viewModel: StoreOnsaleViewModel by viewModels()

    //Set boolean false for "Show More" button
    private val isLoadingMore = ObservableBoolean(false)

    //Initialize arts array
    private var arts: ArrayList<Art> = arrayListOf()

    @Inject
    lateinit var navManager: NavManager

    //Notification redirect listener
    lateinit var notificationReDirectProfileListener: NotificationRedirectProfileListener

    //share preference object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    //This 3 flag is use for show and hide the loading at the bottom of the screen.
    var showLoading = ObservableField(false)
    var isLoadMore: Boolean = true
    var isLoading: Boolean = true

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<StoreOnsaleViewModel.ViewState> { state ->
        binding.isRefreshing = state.isRefreshing
        isLoadingMore.set(state.isLoadingMore)

        //Show error message when getting error message from network calling
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

        // Set list for "Tradition Arts" tab
        // need to copy data to avoid crash with epoxy
        state.forYouArts?.let { _arts ->
            arts = _arts.map { it.copy() } as ArrayList<Art>
        }

        // Set list for "Nft Arts" tab
        // need to copy data to avoid crash with epoxy
        state.nftArts?.let { _arts ->
            arts = _arts.map { it.copy() } as ArrayList<Art>
        }

        // Ask Epoxy RecyclerView to rebuild the items
        binding.rvItems.requestModelBuild()

        //Set position top when Refresh layout is calling
        if (state.isRefreshed) {
            binding.rvItems.scrollToPosition(0)
        }

        //Get the value of loading status and based on that show the loading button.
        if (!state.stateCalled) {
            isLoadMore = state.isLoadingMore
            isLoading = state.isLoading
        }

        //Bind data if arts array is not empty
        binding.isHaveData = arts.isNotEmpty()
        binding.ivSearch.setOnDebouncedClickListener {
            viewModel.isSearchBoxShow.set(!viewModel.isSearchBoxShow.get()!!)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //bind view model
        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)
        addClickEvent()
        createEpoxyRecyclerView()
        viewModel.onRefresh()

        binding.ivBack.setOnDebouncedClickListener {
            findNavController().popBackStack()
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
     * Show list of views with epoxy recyclerview
     * @return the list of recyclerview items
     * */
    private fun createEpoxyRecyclerView() {
        binding.rvItems.withModels {
            if (binding.isHaveData != true) return@withModels
            itemOnsaleTitle {
                id("On sale")
                spanSizeOverride { _, _, _ -> 2 }
            }
            arts.forEachIndexed { index, art ->
                itemStoreOnsaleSeeAll {
                    id("traditional_art $index")
                    /*
                        Epoxy RecyclerView in xml have spanCount = 2,
                        so set this item spanSize to 1 to have a grid view
                     */
                    spanSizeOverride { _, _, _ -> 1 }
                    //Pass art array for show arts data
                    art(art)
                    //Listener for item click
                    listener(object : StoreOnsalArtListener {
                        override fun onClick(art: Art) {
                            /*val bundle = Bundle().apply {
                                putSerializable("art", art)
                            }
                            startActivity(Intent(requireContext(), TradingArtActivity::class.java).apply { putExtras(bundle) })*/
                            val action = StoreOnsaleFragmentDirections.actionStoreToTraditionalArtworkDetails(art)
                            navManager.navigate(action)
                        }

                        override fun likeClick(art: Art) {
                            viewModel.like(art)
                        }
                    })

                }
            }

            /*//Add "Show More" view
            if (arts.isNotEmpty()) {
                itemShowMoreMarket {
                    id("show_more")
                    spanSizeOverride { _, _, _ -> 2 }
                    viewModel(viewModel)
                    isLoadingMore(isLoadingMore)
                }
            }*/

            //This will check there is any need to show the loading button at the bottom or not.
            if (showLoading.get() == true && isLoadMore) {
                itemLoading {
                    id("show_loading")
                    spanSizeOverride { _, _, _ -> 2 }
                }
            }
        }

        // Add scroll event to load more data when reach the bottom
        binding.rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                //isLoadMore & isLoading is use for show the loading at the end of items and also hide after get the data from server.
                if (!binding.rvItems.canScrollVertically(1) && isLoadMore && !isLoading) {
                    showLoading.set(true)
                    viewModel.loadMore()
                } else {
                    showLoading.set(false)
                }
            }
        })
    }

    /**
     * Click event for "Traditional Art" and "NFT Art"
     * @return "Traditional Art" and "NFT Art" list of items
     * */
    private fun addClickEvent() {
        //for fragment result redirection
        setFragmentResultListener(NotificationFragment.REQUEST_KEY) { key, bundle ->
            if (bundle.getString(REDIRECT)!!.equals(USER_PROFILE)) {
                notificationReDirectProfileListener.onNoficationRedirect()
            }
        }

        binding.ivCloseSearch.setOnDebouncedClickListener {
            binding.etSearch.setText("")
            viewModel.search("")
            viewModel.isNeedSearch.set(false)
        }

        binding.etSearch.textChanges()
            .filterNot { it.isNullOrBlank() }
            .debounce(800L)
            .map { it.toString() }
            .onEach {
                viewModel.search(it)
            }
            .launchIn(lifecycleScope)
        binding.parentView.setOnClickListener {
            (activity as BaseActivity).hideKeyboard()
        }

        binding.tilSearch.setEndIconOnClickListener {
            binding.etSearch.setText("")
            viewModel.search("")
        }
    }

    override fun isNeedWindowLightStatusBar() = true

    //For reload the fragment when click on bottom icon
    fun reload() {
        binding.rvItems.smoothScrollToPosition(0)
        viewModel.onRefresh()
    }
}
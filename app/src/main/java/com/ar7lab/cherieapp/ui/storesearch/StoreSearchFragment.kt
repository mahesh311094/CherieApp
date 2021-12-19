package com.ar7lab.cherieapp.ui.storesearch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
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
import com.ar7lab.cherieapp.databinding.FragmentStoreSearchBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.LikesTypeEnum
import com.ar7lab.cherieapp.enums.SearchTypeEnum
import com.ar7lab.cherieapp.enums.StoreTypeEnum
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.Artist
import com.ar7lab.cherieapp.ui.dashboard.NotificationRedirectProfileListener
import com.ar7lab.cherieapp.ui.notification.NotificationFragment
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.ui.store.StoreFragmentDirections
import com.ar7lab.cherieapp.ui.tradingart.TradingArtActivity
import com.ar7lab.cherieapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Store Store Search Fragment
 * @property fragment_store_search is the xml file for this fragment
 * */
@AndroidEntryPoint
class StoreSearchFragment : BaseFragment(R.layout.fragment_store_search) {

    companion object {
        private const val KEY_STORE_TYPE = "storeType"
    }

    //View binding of fragment_market
    private val binding: FragmentStoreSearchBinding by viewBinding()

    //Initialize MarketViewModel
    private val viewModel: StoreSearchViewModel by viewModels()

    //Set boolean false for "Show More" button
    private val isLoadingMore = ObservableBoolean(false)

    private var storeTypeEnum: StoreTypeEnum = StoreTypeEnum.TRADITIONAL_ART

    //Initialize arts array
    private var arts: ArrayList<Art> = arrayListOf()
    private var artsOnsale: ArrayList<Art> = arrayListOf()
    private var artsUpcomingDeals: ArrayList<Art> = arrayListOf()
    private var artsPastDeals: ArrayList<Art> = arrayListOf()
    private var artists: ArrayList<Artist> = arrayListOf()

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

    override fun isNeedWindowLightStatusBar() = true

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<StoreSearchViewModel.ViewState> { state ->
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

        state.traditionalArtsOnsale?.let {
            artsOnsale = it.map { art -> art.copy() } as ArrayList
            if (artsOnsale.isNotEmpty()) {
                createEpoxyRecyclerView(ON_SALE)
            }
        }
        state.traditionalArtsUpcomingDeals?.let {
            artsUpcomingDeals = it.map { art -> art.copy() } as ArrayList
            if (artsUpcomingDeals.isNotEmpty()) {
                createEpoxyRecyclerView(UPCOMING_DEALS)
            }
        }
        state.traditionalArtsPastDeals?.let {
            artsPastDeals = it.map { art -> art.copy() } as ArrayList
            if (artsPastDeals.isNotEmpty()) {
                createEpoxyRecyclerView(PAST_DEALS)
            }
        }

        state.traditionalArtist?.let {
            artists = it.map { artist -> artist.copy() } as ArrayList
            if (artists.isNotEmpty()) {
                createEpoxyRecyclerView(ARTISTS)
            }
        }

        state.nftArtsOnsale?.let {
            artsOnsale = it.map { art -> art.copy() } as ArrayList
            if (artsOnsale.isNotEmpty()) {
                createEpoxyRecyclerView(ON_SALE)
            }
        }
        state.nftArtsUpcomingDeals?.let {
            artsUpcomingDeals = it.map { art -> art.copy() } as ArrayList
            if (artsUpcomingDeals.isNotEmpty()) {
                createEpoxyRecyclerView(UPCOMING_DEALS)
            }
        }
        state.nftArtsPastDeals?.let {
            artsPastDeals = it.map { art -> art.copy() } as ArrayList
            if (artsPastDeals.isNotEmpty()) {
                createEpoxyRecyclerView(PAST_DEALS)
            }
        }
        state.nftArtist?.let {
            artists = it.map { artist -> artist.copy() } as ArrayList
            if (artists.isNotEmpty()) {
                createEpoxyRecyclerView(ARTISTS)
            }
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
        binding.isHaveData = arts.isNotEmpty() || artsOnsale.isNotEmpty() || artsUpcomingDeals.isNotEmpty() || artsPastDeals.isNotEmpty() || artists.isNotEmpty()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storeTypeEnum = (arguments?.getSerializable(KEY_STORE_TYPE) as StoreTypeEnum?) ?: StoreTypeEnum.TRADITIONAL_ART
        //bind view model
        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)
        viewModel.init(storeTypeEnum)
        addClickEvent()
        searchListener()
        createEpoxyRecyclerView(ON_SALE)
        //viewModel.onRefresh()
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
    private fun createEpoxyRecyclerView(type: String) {
        binding.rvItems.withModels {
            if (binding.isHaveData != true) return@withModels
            when (type) {
                ON_SALE -> {
                    if (artsOnsale.isNotEmpty()) {
                        artsOnsale.forEachIndexed { index, art ->
                            itemStoreSearchOnsale {
                                id("traditional_art $index")
                                /*
                                Epoxy RecyclerView in xml have spanCount = 2,
                                so set this item spanSize to 1 to have a grid view
                             */
                                spanSizeOverride { _, _, _ -> 1 }
                                //Pass art array for show arts data
                                art(art)
                                //Listener for item click
                                listener(object : StoreSearchArtListener {
                                    override fun onClick(art: Art) {
                                        if (art.category == FILTER_TRADITIONAL_ART) {
                                            val action = StoreSearchFragmentDirections.actionStoreSearchToTraditionalArtworkDetails(art)
                                            navManager.navigate(action)

                                        } else {
                                            val action = StoreSearchFragmentDirections.actionStoreSearchToNftArtworkDetails(art)
                                            navManager.navigate(action)
                                        }
                                    }

                                    override fun likeClick(art: Art) {
                                        viewModel.like(art)
                                    }

                                    override fun onClickTrade() {
                                        TODO("Not yet implemented")
                                    }

                                    override fun notifyClick(art: Art) {
                                        TODO("Not yet implemented")
                                    }

                                    override fun onFollowClick(artist: Artist) {
                                        TODO("Not yet implemented")
                                    }
                                })
                            }
                        }
                    }
                }
                UPCOMING_DEALS -> {
                    if (artsUpcomingDeals.isNotEmpty()) {
                        artsUpcomingDeals.forEachIndexed { index, art ->
                            itemStoreSearchUpcomingDeals {
                                id("traditional_art $index")
                                /*
                                Epoxy RecyclerView in xml have spanCount = 2,
                                so set this item spanSize to 1 to have a grid view
                             */
                                spanSizeOverride { _, _, _ -> 1 }
                                //Pass art array for show arts data
                                art(art)
                                //Listener for item click
                                listener(object : StoreSearchArtListener {
                                    override fun onClick(art: Art) {
                                        /*val bundle = Bundle().apply {
                                            putSerializable("art", art)
                                        }
                                        startActivity(
                                            Intent(
                                                requireContext(),
                                                TradingArtActivity::class.java
                                            ).apply { putExtras(bundle) })*/
                                        if (art.category == FILTER_TRADITIONAL_ART) {
                                            val action = StoreSearchFragmentDirections.actionStoreSearchToTraditionalArtworkDetails(art)
                                            navManager.navigate(action)

                                        } else {
                                            val action = StoreSearchFragmentDirections.actionStoreSearchToNftArtworkDetails(art)
                                            navManager.navigate(action)
                                        }
                                    }

                                    override fun likeClick(art: Art) {
                                        viewModel.like(art)
                                    }

                                    override fun onClickTrade() {
                                        TODO("Not yet implemented")
                                    }

                                    override fun notifyClick(art: Art) {
                                        viewModel.notify(art)
                                    }

                                    override fun onFollowClick(artist: Artist) {
                                        TODO("Not yet implemented")
                                    }
                                })
                            }
                        }
                    }
                }
                PAST_DEALS -> {
                    if (artsPastDeals.isNotEmpty()) {
                        artsPastDeals.forEachIndexed { index, art ->
                            itemStoreSearchPastDeals {
                                id("traditional_art $index")
                                /*
                                Epoxy RecyclerView in xml have spanCount = 2,
                                so set this item spanSize to 1 to have a grid view
                             */
                                spanSizeOverride { _, _, _ -> 1 }
                                //Pass art array for show arts data
                                art(art)
                                //Listener for item click
                                listener(object : StoreSearchArtListener {
                                    override fun onClick(art: Art) {
                                        if (art.category == FILTER_TRADITIONAL_ART) {
                                            val action = StoreSearchFragmentDirections.actionStoreSearchToTraditionalArtworkDetails(art)
                                            navManager.navigate(action)

                                        } else {
                                            val action = StoreSearchFragmentDirections.actionStoreSearchToNftArtworkDetails(art)
                                            navManager.navigate(action)
                                        }
                                    }

                                    override fun likeClick(art: Art) {
                                        viewModel.like(art)
                                    }

                                    override fun onClickTrade() {
                                        val action =
                                            StoreSearchFragmentDirections.actionStoreSearchToTrade()
                                        navManager.navigate(action)
                                    }

                                    override fun notifyClick(art: Art) {
                                        TODO("Not yet implemented")
                                    }

                                    override fun onFollowClick(artist: Artist) {
                                        TODO("Not yet implemented")
                                    }
                                })
                            }
                        }
                    }
                }

                ARTISTS -> {
                    if (artists.isNotEmpty()) {
                        artists.forEachIndexed { index, artist ->
                            itemStoreSearchArtistsItem {
                                id("traditional_art $index")
                                /*
                                Epoxy RecyclerView in xml have spanCount = 2,
                                so set this item spanSize to 1 to have a grid view
                             */
                                spanSizeOverride { _, _, _ -> 1 }
                                //Pass art array for show arts data
                                artist(artist)
                                //Listener for item click
                                listener(object : StoreSearchArtListener {
                                    override fun onClick(art: Art) {
                                    }

                                    override fun likeClick(art: Art) {
                                    }

                                    override fun onClickTrade() {
                                    }

                                    override fun notifyClick(art: Art) {

                                    }

                                    override fun onFollowClick(artist: Artist) {
                                        viewModel.followArtist(artist)
                                    }
                                })
                            }
                        }
                    }
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
            //viewModel.search("")
            viewModel.isNeedSearch.set(false)
        }

        /*binding.etSearch.textChanges()
            .filterNot { it.isNullOrBlank() }
            .debounce(800L)
            .map { it.toString() }
            .onEach {
                viewModel.search(it)
            }
            .launchIn(lifecycleScope)

        binding.tilSearch.setEndIconOnClickListener {
            binding.etSearch.setText("")
            viewModel.search("")
        }*/

        binding.parentView.setOnClickListener {
            (activity as BaseActivity).hideKeyboard()
        }

        binding.ivBack.setOnDebouncedClickListener {
            findNavController().popBackStack()
        }

        binding.tvOnSale.setOnDebouncedClickListener {
            createEpoxyRecyclerView(ON_SALE)
            viewModel.changeTypeEnum(SearchTypeEnum.ON_SALE)
        }

        binding.tvUpcomingDeals.setOnDebouncedClickListener {
            createEpoxyRecyclerView(UPCOMING_DEALS)
            viewModel.changeTypeEnum(SearchTypeEnum.UPCOMING_DEALS)
        }

        binding.tvPastDeals.setOnDebouncedClickListener {
            createEpoxyRecyclerView(PAST_DEALS)
            viewModel.changeTypeEnum(SearchTypeEnum.PAST_DEALS)
        }
        binding.tvArtist.setOnDebouncedClickListener {
            createEpoxyRecyclerView(ARTISTS)
            viewModel.changeTypeEnum(SearchTypeEnum.ARTIST)
        }
    }

    //For reload the fragment when click on bottom icon
    fun reload() {
        binding.rvItems.smoothScrollToPosition(0)
        viewModel.onRefresh()
    }

    private fun searchListener() {
        val imm: InputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.etSearch.requestFocus()
        imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)

        //binding.ivBack.setOnDebouncedClickListener { finish() }

        binding.etSearch.textChanges()
            .filterNot { it.isNullOrBlank() }
            .debounce(800L)
            .map { it.toString() }
            .onEach {
                //<editor-fold desc="If letter length is more then 0 then show result, otherwise show no data found">
                if (it.length > 2)
                    viewModel.search(it)
                else {
                    binding.isHaveData = false
                    viewModel.isNeedSearch.set(false)
                }
                //</editor-fold>
            }
            .launchIn(lifecycleScope)

    }
}
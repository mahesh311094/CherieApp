package com.ar7lab.cherieapp.ui.store

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.Carousel
import com.airbnb.epoxy.carousel
import com.ar7lab.cherieapp.*
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.base.navigation.NavManager
import com.ar7lab.cherieapp.databinding.FragmentStoreBinding
import com.ar7lab.cherieapp.enums.TopArtistsTypeEnum
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.Artist
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.databinding.RemoveAccountAlertLayoutBinding
import com.ar7lab.cherieapp.databinding.VerificationPopUpDialogBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.StoreTypeEnum
import com.ar7lab.cherieapp.model.BankDetails
import com.ar7lab.cherieapp.model.BannersItem
import com.ar7lab.cherieapp.ui.dashboard.NotificationRedirectProfileListener
import com.ar7lab.cherieapp.ui.kycinfo.KYCInfoActivity
import com.ar7lab.cherieapp.ui.notification.NotificationFragment
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.ui.tradingart.TradingArtActivity
import com.ar7lab.cherieapp.ui.wallet.payment_success.DepositSuccessPaymentActivity
import com.ar7lab.cherieapp.utils.*
import java.lang.ClassCastException


@AndroidEntryPoint
class StoreFragment : BaseFragment(R.layout.fragment_store) {

    private val binding: FragmentStoreBinding by viewBinding()

    private val viewModel: StoreViewModel by viewModels()

    @Inject
    lateinit var navManager: NavManager

    // Define here for the xml databinding
    private val isLoadingMore = ObservableBoolean(false)
    private var arts: ArrayList<Art> = arrayListOf()
    private var artsOnsale: ArrayList<Art> = arrayListOf()
    private var artsUpcomingDeals: ArrayList<Art> = arrayListOf()
    private var artsPastDeals: ArrayList<Art> = arrayListOf()
    private var traditionalArtists: ArrayList<Artist> = arrayListOf()
    private var nftArtists: ArrayList<Artist> = arrayListOf()
    private var allBannerList: ArrayList<BannersItem> = arrayListOf()
    private var topArtistTypeEnum: TopArtistsTypeEnum = TopArtistsTypeEnum.TRADITIONAL
    private var storeTypeEnum: StoreTypeEnum = StoreTypeEnum.TRADITIONAL_ART

    private var mHandler = Handler(Looper.getMainLooper())
    private var mRunnable = Runnable { }

    //share preference object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    //Notification redirect listener
    lateinit var notificationReDirectProfileListener: NotificationRedirectProfileListener

    //Hide the show more after all data load
    var isDataComplete: Boolean = true

    private var isTraditionalTabClicked: Boolean = true
    private var isNftTabClicked: Boolean = false

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<StoreViewModel.ViewState> { state ->
        binding.isRefreshing = state.isRefreshing
        isLoadingMore.set(state.isLoadingMore)

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
        state.forYouArts?.let {
            arts = it.map { art -> art.copy() } as ArrayList
        }
        state.traditionalArtsOnsale?.let {
            artsOnsale = it.map { art -> art.copy() } as ArrayList
        }
        state.traditionalArtsUpcomingDeals?.let {
            artsUpcomingDeals = it.map { art -> art.copy() } as ArrayList
        }
        state.traditionalArtsPastDeals?.let {
            artsPastDeals = it.map { art -> art.copy() } as ArrayList
        }

        state.nftArtsOnsale?.let {
            artsOnsale = it.map { art -> art.copy() } as ArrayList
        }
        state.nftArtsUpcomingDeals?.let {
            artsUpcomingDeals = it.map { art -> art.copy() } as ArrayList
        }
        state.nftArtsPastDeals?.let {
            artsPastDeals = it.map { art -> art.copy() } as ArrayList
        }

        state.traditionalArtists?.let {
            traditionalArtists = it.map { artist -> artist.copy() } as ArrayList<Artist>
        }
        state.ntfArtists?.let {
            nftArtists = it.map { artist -> artist.copy() } as ArrayList<Artist>
        }
        state.bannerList?.let {
            allBannerList = it.map { banner -> banner.copy() } as ArrayList<BannersItem>
        }

        isDataComplete = state.isHaveData

        //Add all the banner images into viewpager
        viewModel.bannerList.clear()
        viewModel.bannerList.addAll(state.bannerList!!.map { it.fileUrl!! })
        viewModel.isNotify.set(true)
        viewModel.bannerCount.set(state.bannerList.size)



        startAutoScroll()

        // Ask Epoxy RecyclerView to rebuild the items
        binding.rvItems.requestModelBuild()
        binding.rvOnsale.requestModelBuild()
        binding.rvUpcomingDeals.requestModelBuild()
        binding.rvPastDeals.requestModelBuild()
        binding.rvTopArtist.requestModelBuild()

        binding.isHaveData = traditionalArtists.isNotEmpty() || nftArtists.isNotEmpty()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            notificationReDirectProfileListener = activity as NotificationRedirectProfileListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement onSomeEventListener")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        observe(viewModel.stateLiveData, stateObserver)

        // Get data into previousBackStackEntry is set from Dashboard Activity
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("key")?.observe(
            viewLifecycleOwner
        ) {
            reload()
        }

        binding.isHaveData = traditionalArtists.isNotEmpty() || nftArtists.isNotEmpty()

        addClickEvent()

        createEpoxyRecyclerView()
        createEpoxyRecyclerViewOnsale()
        createEpoxyRecyclerViewUpcomingDeals()
        createEpoxyRecyclerViewPastDeals()
        createEpoxyRecyclerViewTopArtist()

        viewModel.init()

        /**
         * hide keyboard to prevent bug after login
         */
        (requireActivity() as BaseActivity).hideKeyboard()

        storeTypeEnum = StoreTypeEnum.TRADITIONAL_ART

        viewModel.changeStoreType(storeTypeEnum)

    }

    //For auto scroll the banner
    private fun startAutoScroll() {
        mHandler.removeCallbacks(mRunnable)
        mRunnable = Runnable {
            viewModel.onAutoScrollBanner()
            mHandler.postDelayed(mRunnable, 2000)
        }
        mHandler.postDelayed(mRunnable, 2000)
    }

    private fun createEpoxyRecyclerView() {
        binding.rvItems.withModels {
            // If data is not found, no need to build view items
            if (binding.isHaveData != true) return@withModels

            /*// store header inflate this layout contain ads banner & tab
            itemStoreArtHeader {
                id("store_header")
                viewModel(viewModel)
                spanSizeOverride { _, _, _ -> 2 }
            }*/

            //<editor-fold desc="This will show the banner item with 1.1f of next item">
            val bannerModel = allBannerList.map { banner ->
                ItemStoreBannerBindingModel_().id("banner_art ${banner.id}")
                    .spanSizeOverride { _, _, _ -> 1 }
                    .banner(banner)
            }

            val carouselModel = CustomSnappingCarouselModel_()
                .numViewsToShowOnScreen(1.1f)
                .id("trending")
                .models(bannerModel)
                .onBind { _, view, _ ->
                    view.setSnapHelperCallback {}
                }

            carouselModel.addTo(this)
            //</editor-fold>
        }

        // Add scroll event to save scroll position
        binding.rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val scrollPosition =
                    (recyclerView.layoutManager as GridLayoutManager).findLastCompletelyVisibleItemPosition()
                Timber.e("ddd $scrollPosition")
                viewModel.saveScrollPosition(scrollPosition)
            }
        })
    }

    //Onsale
    private fun createEpoxyRecyclerViewOnsale() {
        binding.rvOnsale.withModels {
            if (binding.isHaveData != true) return@withModels
            binding.clOnsaleTitle.visibility = View.GONE
            binding.rvOnsale.visibility = View.GONE
            binding.view1.visibility = View.GONE

            if (artsOnsale.isNotEmpty()) {
                binding.clOnsaleTitle.visibility = View.VISIBLE
                binding.rvOnsale.visibility = View.VISIBLE
                binding.view1.visibility = View.VISIBLE
                artsOnsale.forEachIndexed { index, art ->
                    itemStoreOnSale {
                        id("store_art $index")
                        /*
                        Epoxy RecyclerView in xml have spanCount = 2,
                        so set this item spanSize to 1 to have a grid view
                     */
                        spanSizeOverride { _, _, _ -> 2 }
                        art(art)
                        artist(art.artist)

                        listener(object : ArtListener {
                            override fun onClick(art: Art) {
                                /*if (art.category == FILTER_TRADITIONAL_ART) {
                                    val action = StoreFragmentDirections.actionStoreToTraditionalArtworkDetails(art)
                                    navManager.navigate(action)
                                } else {
                                    val action = StoreFragmentDirections.actionStoreToNftArtworkDetails(art)
                                    navManager.navigate(action)
                                }*/
                                val action = StoreFragmentDirections.actionStoreToTraditionalArtworkDetails(art)
                                navManager.navigate(action)
                            }

                            override fun viewCommentClick(art: Art) {
                                val action =
                                    StoreFragmentDirections.actionStoreToViewArtComments(art)
                                navManager.navigate(action)
                            }

                            override fun likeClick(art: Art) {
                                viewModel.like(art)
                            }

                            override fun artistProfileClick(artist: Artist) {
                                if (art.category == "NFT") {
                                    topArtistTypeEnum = TopArtistsTypeEnum.NFT

                                } else if (art.category == "TA") {
                                    topArtistTypeEnum = TopArtistsTypeEnum.TRADITIONAL
                                }
                                Timber.e("Type Store Artist: $topArtistTypeEnum")
                                val action = StoreFragmentDirections.actionStoreToNftArtistProfile(
                                    artist,
                                    topArtistTypeEnum
                                )
                                navManager.navigate(action)
                            }

                            override fun viewLikeClick(art: Art) {
                                val action = StoreFragmentDirections.actionStoreToViewArtLikes(art)
                                navManager.navigate(action)
                            }

                            override fun onClickTrade() {
                                TODO("Not yet implemented")
                            }

                            override fun notifyClick(art: Art) {
                                TODO("Not yet implemented")
                            }

                        })
                    }
                }
            }
        }
    }

    //Upcoming deals
    private fun createEpoxyRecyclerViewUpcomingDeals() {
        binding.rvUpcomingDeals.withModels {
            if (binding.isHaveData != true) return@withModels
            binding.clUpcomingDealsTitle.visibility = View.GONE
            binding.rvUpcomingDeals.visibility = View.GONE
            binding.view2.visibility = View.GONE

            if (artsUpcomingDeals.isNotEmpty()) {
                binding.clUpcomingDealsTitle.visibility = View.VISIBLE
                binding.rvUpcomingDeals.visibility = View.VISIBLE
                binding.view2.visibility = View.VISIBLE
                artsUpcomingDeals.forEachIndexed { index, art ->
                    itemStoreUpcomingDeals {
                        id("upcoming_deals $index")
                        /*
                        Epoxy RecyclerView in xml have spanCount = 2,
                        so set this item spanSize to 1 to have a grid view
                     */
                        spanSizeOverride { _, _, _ -> 2 }
                        art(art)
                        artist(art.artist)

                        listener(object : ArtListener {
                            override fun onClick(art: Art) {
                                /*if (art.category == FILTER_TRADITIONAL_ART) {
                                    val action = StoreFragmentDirections.actionStoreToTraditionalArtworkDetails(art)
                                    navManager.navigate(action)
                                } else {
                                    val action = StoreFragmentDirections.actionStoreToNftArtworkDetails(art)
                                    navManager.navigate(action)
                                }*/
                                val action = StoreFragmentDirections.actionStoreToTraditionalArtworkDetails(art)
                                navManager.navigate(action)
                            }

                            override fun viewCommentClick(art: Art) {
                                val action =
                                    StoreFragmentDirections.actionStoreToViewArtComments(art)
                                navManager.navigate(action)
                            }

                            override fun likeClick(art: Art) {
                                viewModel.like(art)
                            }

                            override fun artistProfileClick(artist: Artist) {
                                if (art.category == "NFT") {
                                    topArtistTypeEnum = TopArtistsTypeEnum.NFT

                                } else if (art.category == "TA") {
                                    topArtistTypeEnum = TopArtistsTypeEnum.TRADITIONAL
                                }
                                Timber.e("Type Store Artist: $topArtistTypeEnum")
                                val action = StoreFragmentDirections.actionStoreToNftArtistProfile(
                                    artist,
                                    topArtistTypeEnum
                                )
                                navManager.navigate(action)
                            }

                            override fun viewLikeClick(art: Art) {
                                val action = StoreFragmentDirections.actionStoreToViewArtLikes(art)
                                navManager.navigate(action)
                            }

                            override fun onClickTrade() {
                                TODO("Not yet implemented")
                            }

                            override fun notifyClick(art: Art) {
                                viewModel.notify(art)
                            }

                        })

                    }
                }
            }
        }
    }

    //Past deals
    private fun createEpoxyRecyclerViewPastDeals() {
        binding.rvPastDeals.withModels {
            if (binding.isHaveData != true) return@withModels
            binding.clPastDealsTitle.visibility = View.GONE
            binding.rvPastDeals.visibility = View.GONE
            binding.view3.visibility = View.GONE

            if (artsPastDeals.isNotEmpty()) {
                binding.clPastDealsTitle.visibility = View.VISIBLE
                binding.rvPastDeals.visibility = View.VISIBLE
                binding.view3.visibility = View.VISIBLE

                artsPastDeals.forEachIndexed { index, art ->
                    itemStorePastDeals {
                        id("store_art $index")
                        /*
                        Epoxy RecyclerView in xml have spanCount = 2,
                        so set this item spanSize to 1 to have a grid view
                     */
                        spanSizeOverride { _, _, _ -> 2 }
                        art(art)
                        artist(art.artist)

                        listener(object : ArtListener {
                            override fun onClick(art: Art) {
                                /*if (art.category == FILTER_TRADITIONAL_ART) {
                                    val action = StoreFragmentDirections.actionStoreToTraditionalArtworkDetails(art)
                                    navManager.navigate(action)
                                } else {
                                    val action = StoreFragmentDirections.actionStoreToNftArtworkDetails(art)
                                    navManager.navigate(action)
                                }*/

                                /*val action = StoreFragmentDirections.actionStoreToTraditionalArtworkDetails(art)
                                navManager.navigate(action)*/

                                val action = StoreFragmentDirections.actionStoreToTrade()
                                navManager.navigate(action)
                            }

                            override fun viewCommentClick(art: Art) {
                                val action =
                                    StoreFragmentDirections.actionStoreToViewArtComments(art)
                                navManager.navigate(action)
                            }

                            override fun likeClick(art: Art) {
                                viewModel.like(art)
                            }

                            override fun artistProfileClick(artist: Artist) {
                                if (art.category == "NFT") {
                                    topArtistTypeEnum = TopArtistsTypeEnum.NFT

                                } else if (art.category == "TA") {
                                    topArtistTypeEnum = TopArtistsTypeEnum.TRADITIONAL
                                }
                                Timber.e("Type Store Artist: $topArtistTypeEnum")
                                val action = StoreFragmentDirections.actionStoreToNftArtistProfile(
                                    artist,
                                    topArtistTypeEnum
                                )
                                navManager.navigate(action)
                            }

                            override fun viewLikeClick(art: Art) {
                                val action = StoreFragmentDirections.actionStoreToViewArtLikes(art)
                                navManager.navigate(action)
                            }

                            override fun onClickTrade() {
                                /*val action = StoreFragmentDirections.actionStoreToTrade()
                                navManager.navigate(action)*/
                            }

                            override fun notifyClick(art: Art) {
                                val bundle = Bundle().apply {
                                    putSerializable("art", art)
                                }
                                startActivity(Intent(requireContext(), TradingArtActivity::class.java).apply { putExtras(bundle) })
                            }

                        })
                    }
                }
            }
        }
    }

    //Top Artist
    private fun createEpoxyRecyclerViewTopArtist() {
        binding.rvTopArtist.withModels {
            // Do the trick to make the UI match with design
            if (binding.isHaveData != true) return@withModels
            // Add the top padding
            if (traditionalArtists.isNotEmpty()) {

                itemTopTraditionalArtistsTitle {
                    id("top traditional artists")
                    spanSizeOverride { _, _, _ -> 2 }
                    listener(object : TopTraditionalArtistsListener {
                        override fun onSeeAllClick() {
                            val action = StoreFragmentDirections.actionStoreToTopArtists(TopArtistsTypeEnum.TRADITIONAL)
                            navManager.navigate(action)
                        }

                    })
                }

                val artistModels = traditionalArtists.map { artist ->
                    ItemTopTraditionalArtistsItemBindingModel_()
                        .id(artist.id)
                        .artist(artist)
                        .listener(object : ArtistListener {
                            override fun onFollowClick(artist: Artist) {
                                viewModel.followArtist(artist)
                            }

                            override fun onClick(artist: Artist) {
                                topArtistTypeEnum = TopArtistsTypeEnum.TRADITIONAL
                                val action = StoreFragmentDirections.actionStoreToNftArtistProfile(artist, topArtistTypeEnum)
                                navManager.navigate(action)
                            }
                        })
                }

                //This line is used for the disable auto scroll while scrolling
                Carousel.setDefaultGlobalSnapHelperFactory(null)
                carousel {
                    id("top_trading_view")
                    models(artistModels)
                }
            }

            if (nftArtists.isNotEmpty()) {
                itemTopNftArtistsTitle {
                    id("top nft artists")
                    spanSizeOverride { _, _, _ -> 2 }
                    listener(object : TopNFTArtistsListener {
                        override fun onSeeAllClick() {
                            val action = StoreFragmentDirections.actionStoreToTopArtists(TopArtistsTypeEnum.NFT)
                            navManager.navigate(action)
                        }
                    })
                }

                val artistModels = nftArtists.map { artist ->
                    ItemTopNftArtistsItemBindingModel_()
                        .id(artist.id)
                        .artist(artist)
                        .listener(object : ArtistListener {
                            override fun onFollowClick(artist: Artist) {
                                viewModel.followArtist(artist)
                            }

                            override fun onClick(artist: Artist) {
                                topArtistTypeEnum = TopArtistsTypeEnum.NFT
                                val action = StoreFragmentDirections.actionStoreToNftArtistProfile(artist, topArtistTypeEnum)
                                navManager.navigate(action)
                            }
                        })
                }

                //This line is used for the disable auto scroll while scrolling
                Carousel.setDefaultGlobalSnapHelperFactory(null)
                carousel {
                    id("nft_artist_view")
                    models(artistModels)
                }
            }

            itemBottomSpacer {
                id("spacing")
                spanSizeOverride { _, _, _ -> 2 }
            }
        }
    }

    private fun addClickEvent() {

        //Click on "Traditional Art" tab
        binding.tvTraditional.setOnDebouncedClickListener {
            viewModel.changeStoreType(StoreTypeEnum.TRADITIONAL_ART)
            binding.tvTraditional.visibility = View.GONE
            binding.tvNft.visibility = View.VISIBLE
            binding.tvNft.text = getString(R.string.fine_art)
            storeTypeEnum = StoreTypeEnum.TRADITIONAL_ART
        }
        //Click on "Nft Art" tab
        binding.tvNft.setOnDebouncedClickListener {
            viewModel.changeStoreType(StoreTypeEnum.NFT_ART)
            binding.tvTraditional.visibility = View.VISIBLE
            binding.tvNft.visibility = View.GONE
            binding.tvTraditional.text = getString(R.string.digital_art)
            storeTypeEnum = StoreTypeEnum.NFT_ART
        }

        binding.ivNotification.setOnDebouncedClickListener {
            val action = StoreFragmentDirections.actionStoreToNotification()
            navManager.navigate(action)
        }

        //for fragment result redirection
        setFragmentResultListener(NotificationFragment.REQUEST_KEY) { key, bundle ->
            if (bundle.getString(REDIRECT)!! == USER_PROFILE) {
                notificationReDirectProfileListener.onNoficationRedirect()

            }
        }
        binding.ivSearch.setOnDebouncedClickListener {
            //viewModel.searchShow.set(!viewModel.searchShow.get()!!)
            val action =
                StoreFragmentDirections
                    .actionStoreToStoreSearch(storeTypeEnum)
            navManager.navigate(action)
        }

        binding.etSearch.textChanges()
            .filterNot { it.isNullOrBlank() }
            .debounce(500L)
            .map { it.toString() }
            .onEach {
                viewModel.search(it)
                binding.rvItems.smoothScrollToPosition(
                    0 // position of first comment
                )
            }
            .launchIn(lifecycleScope)


        binding.ivClose.setOnDebouncedClickListener {
            binding.etSearch.setText("")
            viewModel.search("")
            (requireActivity() as BaseActivity).hideKeyboard()
        }

        binding.tvSeeAllOnsale.setOnDebouncedClickListener {
            val action =
                StoreFragmentDirections
                    .actionStoreToStoreOnsale()
            navManager.navigate(action)
        }

        binding.tvSeeAllUpcomingDeals.setOnDebouncedClickListener {
            val action =
                StoreFragmentDirections
                    .actionStoreToStoreUpcomingDeals()
            navManager.navigate(action)
        }

        binding.tvSeeAllPastDeals.setOnDebouncedClickListener {
            val action =
                StoreFragmentDirections
                    .actionStoreToStorePastSale()
            navManager.navigate(action)
        }
        binding.ivLike.setOnDebouncedClickListener {
            val action =
                StoreFragmentDirections
                    .actionStoreToLikes()
            navManager.navigate(action)
        }

        if(!sharePreferencesManager.isKYCCompleted)
        {
            verificationPopUpAlertDialog()
        }


    }
    fun verificationPopUpAlertDialog() {

        binding.isKYCCompleted=false
        binding.ivClosePopup.setOnClickListener {
            binding.isKYCCompleted=true
        }
        binding.btnVerify.setOnClickListener {
            startActivity(Intent(requireContext(), KYCInfoActivity::class.java))
        }


    }


    //For reload the fragment when click on bottom icon
    fun reload() {
        binding.rvItems.smoothScrollToPosition(0)
        viewModel.onRefresh()
    }

}
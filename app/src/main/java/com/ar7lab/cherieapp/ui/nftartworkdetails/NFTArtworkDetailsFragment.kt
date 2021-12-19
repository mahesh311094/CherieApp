package com.ar7lab.cherieapp.ui.nftartworkdetails

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ar7lab.cherieapp.*
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.base.navigation.NavManager
import com.ar7lab.cherieapp.databinding.FragmentNftArtworkDetailsBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.ArtworkDetailTabEnum
import com.ar7lab.cherieapp.enums.TopArtistsTypeEnum
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.ArtTransactionDetails
import com.ar7lab.cherieapp.model.Artist
import com.ar7lab.cherieapp.model.Comment
import com.ar7lab.cherieapp.ui.payment.selectpayment.SelectPaymentActivity
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.ui.traditionalartworkdetails.ArtDetailsListenerCreator
import com.ar7lab.cherieapp.ui.traditionalartworkdetails.ArtListener
import com.ar7lab.cherieapp.ui.traditionalartworkdetails.ArtistInfoListener
import com.ar7lab.cherieapp.ui.traditionalartworkdetails.TraditionalArtworkDetailsFragmentDirections
import com.ar7lab.cherieapp.ui.traditionalartworkdetails.slider.ClickListener
import com.ar7lab.cherieapp.ui.traditionalartworkdetails.slider.ImageSliderAdapter
import com.ar7lab.cherieapp.utils.ART
import com.ar7lab.cherieapp.utils.TRANSACTION_DETAIL
import com.ar7lab.cherieapp.utils.openInfoDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/*
* Nft artwork details fragment
* */
@AndroidEntryPoint
class NFTArtworkDetailsFragment :
    BaseFragment(R.layout.fragment_nft_artwork_details) {

    //Define key for what object is passing from one fragment to another
    companion object {
        private const val KEY_ART = "art"
    }

    //View binding for fragment_nft_artwork_details
    private val binding: FragmentNftArtworkDetailsBinding by viewBinding()

    //Initialize NFTArtworkDetailsViewModel
    private val viewModel: NFTArtworkDetailsViewModel by viewModels()

    //Initialize Art object
    private var _art: Art? = null

    //Initialize Comment list
    private var _comments: ArrayList<Comment> = arrayListOf()
    private val isLoadingMore = ObservableBoolean(false)
    private val isLoadingMoreArts = ObservableBoolean(false)

    //Initialize Artist type "Investment Info" or "Work & Artist Info"
    // By default "Investment Info" is selected
    private var tabSelected = ArtworkDetailTabEnum.NONE_TAB

    //Initialize Artist list
    private var _artistArts: ArrayList<Art> = arrayListOf()

    override fun isNeedWindowLightStatusBar() = false

    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    //Inject NavManager for navigate this fragment to another fragment
    @Inject
    lateinit var navManager: NavManager

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<NFTArtworkDetailsViewModel.ViewState> { state ->
        binding.isRefreshing = state.isRefreshing
        if (state.isRefreshing) {
            (requireActivity() as BaseActivity).hideKeyboard()
        }
        //Set state for load more data
        isLoadingMore.set(state.isLoadingMore)
        //Set state for arts data
        isLoadingMoreArts.set(state.isLoadingMoreArts)
        //Set set for show error message
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
        // need to copy data to avoid crash with epoxy
        state.art?.let { art ->
            _art = art.copy(artist = art.artist.copy())
        }
        // need to copy data to avoid crash with epoxy
        state.comments?.let { comments ->
            _comments = comments.map { it.comments.copy() } as ArrayList<Comment>
        }

        //Get selected tab
        tabSelected = state.tabSelected

        // need to copy data to avoid crash with epoxy
        state.arts?.let { arts ->
            _artistArts = arts.map { it.copy() } as ArrayList<Art>
        }

        // Ask Epoxy RecyclerView to rebuild the items
        binding.rvItems.requestModelBuild()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //bind view model
        binding.viewModel = viewModel

        observe(viewModel.stateLiveData, stateObserver)

        //Get Art Object from previous view
        _art = arguments?.getSerializable(KEY_ART) as Art?

        //Pass Art object
        viewModel.init(_art)

        //Back to previous page
        binding.tvBack.setOnDebouncedClickListener {
            ImageSliderAdapter.globalState = "collapse"
            viewModel.restoreHeight.set(true)
            findNavController().popBackStack()
        }

        binding.tvComment.setOnClickListener {
            binding.rvItems.smoothScrollToPosition(
                22 // position of first comment
            )
        }

        binding.ivLike.setOnDebouncedClickListener {
            viewModel.likeThisArt()
        }

        binding.tvLike.setOnClickListener {
            val action =
                NFTArtworkDetailsFragmentDirections
                    .actionNftArtworkDetailsToViewArtLikes(_art!!)
            navManager.navigate(action)
        }

        binding.btnBuyNow.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable(ART, _art)
                putSerializable(TRANSACTION_DETAIL, ArtTransactionDetails())
            }
            startActivity(
                Intent(requireContext(), SelectPaymentActivity::class.java).apply {
                    putExtras(bundle)
                }
            )
        }

        //Adding views for both "Investment Info" and "Work and Artist Info" tab
        binding.rvItems.withModels {

            if (_art?.fileUrls?.isNotEmpty() == true) {
                itemNftArtDetailsGeneralScrollableImage {
                    id("multipleImage")
                    art(_art)
                    position("0")
                    adapter(_art?.let { ImageSliderAdapter(requireActivity(), it.fileUrls) })
                    viewModel(viewModel)
                    spanSizeOverride { _, _, _ -> 2 }
                }
            }

            itemNftArtDetailsGeneral {
                id("general")
                price(_art?.price)
                currency(_art?.currency)
                salesinfo(_art?.salesInfo)
                art(_art)
                artist(_art?.artist)
                /*
                 Epoxy RecyclerView in xml have spanCount = 2,
                 so set this item spanSize to 2 to have a full width view
                 */
                spanSizeOverride { _, _, _ -> 2 }
                listener(object : ArtDetailsListenerCreator {
                    override fun artistProfileClick(artist: Artist) {
                        val action =
                            NFTArtworkDetailsFragmentDirections.actionNftArtworkDetailsToNftArtistProfile(
                                artist,
                                TopArtistsTypeEnum.NFT
                            )
                        navManager.navigate(action)
                    }

                })
            }

            /*itemNftArtDetailsGeneral {
                id("general")
                art(_art)
                listener(object : ArtDetailsListener {
                    override fun viewCommentsClicked() {
                        binding.rvItems.smoothScrollToPosition(
                            22 // position of first comment
                        )
                    }

                    override fun viewLikesClicked() {
                        val action =
                            NFTArtworkDetailsFragmentDirections
                                .actionNftArtworkDetailsToViewArtLikes(_art!!)
                        navManager.navigate(action)
                    }

                    override fun onClickBuyNow(art: Art) {
                        startActivity(
                            Intent(requireContext(), SelectPaymentActivity::class.java)
                        )
                    }
                })
                *//*
                 Epoxy RecyclerView in xml have spanCount = 2,
                 so set this item spanSize to 2 to have a full width view
                 *//*
                spanSizeOverride { _, _, _ -> 2 }
            }*/

            //Add Creator section
            /*itemTraditionalArtDetailsCreator {
                id("creator")
                artist(_art?.artist)
                spanSizeOverride { _, _, _ -> 2 }
                listener(object : ArtDetailsListenerCreator {
                    override fun artistProfileClick(artist: Artist) {
                        val action =
                            NFTArtworkDetailsFragmentDirections.actionNftArtworkDetailsToNftArtistProfile(
                                artist,
                                TopArtistsTypeEnum.NFT
                            )
                        navManager.navigate(action)
                        *//*
                        Redirect Nft artist profile page with type "NFT"
                        From Traditional use type "TRADITIONAL"
                        *//*
                    }
                })
            }*/

            //Add "Investment Info" and "Work and Artist Info" tab
            itemNftArtDetailsTabInfo {
                id("tab")
                viewModel(viewModel)
                spanSizeOverride { _, _, _ -> 2 }
            }

            itemSpacer {
                id("spacing")
            }
            itemSpacer {
                id("spacing")
            }

            // Show investment info tab
            if (tabSelected == ArtworkDetailTabEnum.NONE_TAB) {
                /*itemSpacer {
                    id("spacing")
                }

                itemNftArtDetailsInvestSaleDate {
                    id("sale date")
                    salesInfo(_art?.salesInfo)
                    spanSizeOverride { _, _, _ -> 2 }
                }

                //Add Invest General section
                itemNftArtDetailsInvestGeneral {
                    id("invest general")
                    investmentInfo(_art?.investmentInfo)
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //Add Qty holding section
                itemNftArtDetailsInvestQtyHoldings {
                    id("qty holdings")
                    investmentInfo(_art?.investmentInfo)
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //Add sale qty 1st part
                itemNftArtDetailsInvestSalesQty {
                    id("sales qty 1st")
                    salesInfo(_art?.salesInfo)
                    number(1)
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //Add sale qty 2nd part
                itemNftArtDetailsInvestSalesQty {
                    id("sales qty 2nd")
                    salesInfo(_art?.salesInfo)
                    number(2)
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //Add sale qty 3rd part
                itemNftArtDetailsInvestSalesQty {
                    id("sales qty 3rd")
                    salesInfo(_art?.salesInfo)
                    number(3)
                    spanSizeOverride { _, _, _ -> 2 }
                }

                //Add Ownership section
                itemNftArtDetailsInvestOwnershipPrice {
                    id("ownership price")
                    investmentInfo(_art?.investmentInfo)
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //Add sales qty section
                (0..1).forEachIndexed { index, i ->
                    itemNftArtDetailsInvestSalesQty {
                        id("sales qty $index")
                        salesInfo(_art?.salesInfo)
                        number(index)
                        spanSizeOverride { _, _, _ -> 2 }
                    }
                }
                //Add Annual growth section
                itemNftArtDetailsInvestAnualGrowth {
                    id("annual growth")
                    statisticalInfo(_art?.statisticalInfo)
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //Add Statistics Artwork section
                itemNftArtDetailsInvestStatisticsArtwork {
                    id("statistics similar")
                    investmentInfo(_art?.investmentInfo)
                    spanSizeOverride { _, _, _ -> 2 }
                }

                //Add Statistics Similar Artwork section
                itemNftArtDetailsInvestStatisticsArtwork {
                    id("statistics similar")
                    investmentInfo(_art?.investmentInfo)
                    imageUrl("https://i.imgur.com/ky6Wg4g.png")
                    spanSizeOverride { _, _, _ -> 2 }
                }

                /*//Add Trading volume
                itemNftArtDetailsInvestTradingVolume {
                    id("trading volume")
                    investmentInfo(_art?.investmentInfo)
                    spanSizeOverride { _, _, _ -> 2 }
                }*/

                //Add statistics trading volume
                itemNftArtDetailsInvestTradingVolume {
                    id("statistics trading volume")
                    investmentInfo(_art?.investmentInfo)
                    imageUrl("https://i.imgur.com/p1Xc1Px.png")
                    spanSizeOverride { _, _, _ -> 2 }
                }

                itemSpacer {
                    id("spacing")
                }

                itemSpacer {
                    id("spacing")
                }

                itemSpacer {
                    id("spacing")
                }

                //Add trading volume item estimate value
                itemNftArtDetailsInvestTradingVolumeItem {
                    id("trading volume item estimate value")
                    title(getString(R.string.estimate_value))
                    value("${_art?.statisticalInfo?.estimatedValue} ${_art?.currency}")
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //Add trading volume item public offering price
                itemNftArtDetailsInvestTradingVolumeItem {
                    id("trading volume item public offering price")
                    title(getString(R.string.artwork_public_offering_price))
                    value("${_art?.investmentInfo?.publicOfferingPrice} ${_art?.currency}")
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //Add trading volume item Average annual value growth rate of similar artwork in last 3 years
                itemNftArtDetailsInvestTradingVolumeItem {
                    id("trading volume item Average annual value growth rate of similar artwork in last 3 years")
                    title(
                        getString(
                            R.string.annual_growth_rate_similar_3_years,
                            _art?.statisticalInfo?.avgAnnualValueGrowthRateOfSimilarArtwork
                        )
                    )
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //Add trading volume item Average annual Auction transaction in last three years
                itemNftArtDetailsInvestTradingVolumeItem {
                    id("trading volume item Average annual Auction transaction in last three years")
                    title(getString(R.string.annual_auction_transaction_3_years))
                    value("${_art?.statisticalInfo?.avgAnnualAuctionTransactionInLast3Years} ${_art?.currency}")
                    spanSizeOverride { _, _, _ -> 2 }
                }*/
            } else {

                // show work and artist info tab
                itemNftArtDetailsArtistInfoCreator {
                    id("artist info creator")
                    spanSizeOverride { _, _, _ -> 2 }
                    //Pass Artist object
                    artist(_art?.artist)
                    //Listener for follow artist
                    listener(object : ArtistInfoListener {
                        override fun onFollowClick(artist: Artist) {
                            viewModel.followArtist(artist)
                        }

                        override fun artistProfileClick(artist: Artist) {
                            val action =
                                NFTArtworkDetailsFragmentDirections.actionNftArtworkDetailsToNftArtistProfile(
                                    artist,
                                    TopArtistsTypeEnum.NFT
                                )
                            navManager.navigate(action)
                        }

                    })
                }
                //Add collection info
                itemNftArtDetailsArtistInfoCollectionInfo {
                    id("collection info")
                    //Pass Art object
                    art(_art)
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //Add Nft Art Details Artist art
                _artistArts.forEachIndexed { index, art ->
                    itemNftArtDetailsArtistInfoArtItem {
                        id("artist art $index")
                        //Pass Art object
                        art(art)
                        //Pass Artist object
                        artist(_art?.artist)
                        listener(object : ArtListener {
                            //Listener for like art
                            override fun likeClick(art: Art) {
                                viewModel.like(art)
                            }

                            override fun viewCommentClick(art: Art) {
                                //Listener for go to comment page
                                val action =
                                    NFTArtworkDetailsFragmentDirections
                                        .actionNftArtworkDetailsToViewArtComments(art)
                                navManager.navigate(action)
                            }

                            override fun viewLikeClick(art: Art) {
                                val action =
                                    NFTArtworkDetailsFragmentDirections
                                        .actionNftArtworkDetailsToViewArtLikes(art)
                                navManager.navigate(action)
                            }

                        })
                    }
                }
                if (_artistArts.isNotEmpty()) {
                    itemNftArtDetailsArtistInfoArtsShowMore {
                        id("show more arts")
                        isLoadingMore(isLoadingMoreArts)
                        viewModel(viewModel)
                        spanSizeOverride { _, _, _ -> 2 }
                    }
                }

            }

            //Add comments section
            itemNftArtDetailsComments {
                id("comments")
                comment(_art?.commentCount.toString())
                isLoadingMore(isLoadingMore)
                viewModel(viewModel)
                listener(object : ClickListener {
                    override fun click() {
                        val action =
                            NFTArtworkDetailsFragmentDirections
                                .actionNftArtworkDetailsToViewArtComments(_art!!)
                        navManager.navigate(action)
                    }
                })
                spanSizeOverride { _, _, _ -> 2 }
            }

            _comments.forEachIndexed { index, comment ->
                itemArtComment {
                    id("comment $index")
                    //Pass comment object
                    comment(comment)
                    spanSizeOverride { _, _, _ -> 2 }
                }
            }

            /*//Add show more comments section
            itemNftArtDetailsShowMoreComments {
                id("load more comments")
                isLoadingMore(isLoadingMore)
                //Pass current view model
                viewModel(viewModel)
                spanSizeOverride { _, _, _ -> 2 }
            }*/

            //Add post comment section
            itemNftArtDetailsPostComment {
                id("post comment")
                //Pass current view model
                viewModel(viewModel)
                spanSizeOverride { _, _, _ -> 2 }
            }

        }
    }

}
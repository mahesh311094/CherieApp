package com.ar7lab.cherieapp.ui.traditionalartworkdetails

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.databinding.ObservableBoolean
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.epoxy.carousel
import com.ar7lab.cherieapp.*
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.base.navigation.NavManager
import com.ar7lab.cherieapp.databinding.FragmentTraditionalArtworkDetailsBinding
import com.ar7lab.cherieapp.databinding.ViewFullScreenImagesDailogBinding
import com.ar7lab.cherieapp.databinding.WalletInfoDialogBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.ArtworkDetailTabEnum
import com.ar7lab.cherieapp.enums.TopArtistsTypeEnum
import com.ar7lab.cherieapp.model.*
import com.ar7lab.cherieapp.ui.nftartworkdetails.NFTArtworkDetailsFragmentDirections
import com.ar7lab.cherieapp.ui.payment.selectpayment.SelectPaymentActivity
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.ui.traditionalartworkdetails.slider.ClickListener
import com.ar7lab.cherieapp.ui.traditionalartworkdetails.slider.ImageSliderAdapter
import com.ar7lab.cherieapp.ui.traditionalartworkdetails.slider.ImageSliderAdapterZoom
import com.ar7lab.cherieapp.ui.wallet.WalletFragmentDirections
import com.ar7lab.cherieapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TraditionalArtworkDetailsFragment :
    BaseFragment(R.layout.fragment_traditional_artwork_details) {

    companion object {
        private const val KEY_ART = "art"
    }

    //binding
    private val binding: FragmentTraditionalArtworkDetailsBinding by viewBinding()

    //Model binding
    private val viewModel: TraditionalArtworkDetailsViewModel by viewModels()

    //Art model data
    private var _art: Art? = null

    //Loading flag
    private val isLoadingMore = ObservableBoolean(false)

    //Loading More arts flag
    private val isLoadingMoreArts = ObservableBoolean(false)

    private var tabSelected = ArtworkDetailTabEnum.DETAILS
    private var _artistArts: ArrayList<Art> = arrayListOf()
    private var _futureSale: List<FutureSale> = arrayListOf()
    private var _pastSale: List<PastSale> = arrayListOf()
    private var salesRate: Double = 0.0


    //shareprefrence object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    //Nav manager
    @Inject
    lateinit var navManager: NavManager

    override fun isNeedWindowLightStatusBar() = true

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<TraditionalArtworkDetailsViewModel.ViewState> { state ->
        binding.isRefreshing = state.isRefreshing
        if (state.isRefreshing) {
            (requireActivity() as BaseActivity).hideKeyboard()
        }
        isLoadingMore.set(state.isLoadingMore)
        isLoadingMoreArts.set(state.isLoadingMoreArts)
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
            _futureSale = art.salesInfo.futureSales as ArrayList<FutureSale>
            _pastSale = art.salesInfo.pastSales as ArrayList<PastSale>
        }


        tabSelected = state.tabSelected

        // need to copy data to avoid crash with epoxy
        state.arts?.let { arts ->
            _artistArts = arts.map { it.copy() } as ArrayList<Art>
        }

        binding.rvItems.requestModelBuild()
    }

    fun showSliderFullScreen() {
        val bind: ViewFullScreenImagesDailogBinding = ViewFullScreenImagesDailogBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(context)
        bind.title = _art?.name
        val adapter = _art?.let { ImageSliderAdapterZoom(requireActivity(), it.fileUrls) }
        bind.viewPager2.adapter = adapter
        builder.setView(bind.root)

        adapter?.onLeftClick = {
            if (bind.viewPager2.currentItem != 0)
                bind.viewPager2.currentItem = --bind.viewPager2.currentItem
        }

        adapter?.onRightClick = {
            if (bind.viewPager2.currentItem < _art?.fileUrls?.size!! - 1) {
                bind.viewPager2.currentItem = ++bind.viewPager2.currentItem
            }
        }

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        bind.ivClose.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as BaseActivity).clearWindowLightStatus()
        binding.viewModel = viewModel

        observe(viewModel.stateLiveData, stateObserver)

        _art = arguments?.getSerializable(KEY_ART) as Art?
        salesRate = _art?.shareSalesRate!!
        viewModel.init(_art)


        //back button click
        binding.ivBack.setOnDebouncedClickListener {
            ImageSliderAdapter.globalState = "collapse"
            viewModel.restoreHeight.set(true)
            findNavController().popBackStack()
        }

        //Like Button click listner
        binding.ivLike.setOnDebouncedClickListener {
            viewModel.likeThisArt()
        }

        //buy now button click listener
        binding.btnBuyNow.setOnClickListener {
            /*val bundle = Bundle().apply {
                putSerializable(ART, _art)
                putSerializable(TRANSACTION_DETAIL, ArtTransactionDetails())
            }
            startActivity(
                Intent(requireContext(), SelectPaymentActivity::class.java).apply {
                    putExtras(bundle)
                }
            )*/
            val tempArt = _art!!
            val action = TraditionalArtworkDetailsFragmentDirections.actionTraditionalArtworkDetailsToSelectPaymentFragment(tempArt)
            navManager.navigate(action)
        }

        binding.rvItems.withModels {
            //Multiple images show in slider
            if (_art?.fileUrls?.isNotEmpty() == true) {
                //image slider
                itemTraditionalArtDetailsGeneralScrollableImage {
                    id("multipleImage")
                    art(_art)
                    position("0")
                    adapter(_art?.let { ImageSliderAdapter(requireActivity(), it.fileUrls) })
                    viewModel(viewModel)
                    spanSizeOverride { _, _, _ -> 2 }
                    artListener(object : ArtListener {
                        override fun likeClick(art: Art) {
                            viewModel.likeThisArt()
                        }

                        override fun viewLikeClick(art: Art) {
                            val action =
                                TraditionalArtworkDetailsFragmentDirections
                                    .actionTraditionalArtworkDetailsToViewArtLikes(_art!!)
                            navManager.navigate(action)
                        }

                        override fun viewCommentClick(art: Art) {
                            showSliderFullScreen()
                        }
                    })
                }
            }


            //General Detail also artist detail
            itemTraditionalArtDetailsGeneral {
                id("general")
                price(_art?.price)
                currency(_art?.currency)
                art(_art)
                artist(_art?.artist)
                salesRate(salesRate)
                viewmodel(viewModel)
                /*
                 Epoxy RecyclerView in xml have spanCount = 2,
                 so set this item spanSize to 2 to have a full width view
                 */
                spanSizeOverride { _, _, _ -> 2 }
                listener(object : ArtDetailsListenerCreator {
                    override fun artistProfileClick(artist: Artist) {
                        val action =
                            TraditionalArtworkDetailsFragmentDirections.actionTraditionalArtworkDetailsToNftArtistProfile(
                                artist,
                                TopArtistsTypeEnum.TRADITIONAL
                            )
                        navManager.navigate(action)
                    }
                })
            }

            //On Sale Layout
            if(_art?.status.equals(STATUS_ON_SALE)) {
                itemTraditionalArtDetailsOnSaleView {
                    id("on_sale_layout")
                    spanSizeOverride { _, _, _ -> 2 }
                    price(_art?.price)
                    currency(_art?.currency)
                    art(_art)
                }
            }

            //basic investment work & Artist
            itemTraditionalArtDetailsTabInfo {
                id("tab")
                viewModel(viewModel)
                spanSizeOverride { _, _, _ -> 2 }
            }


            if (tabSelected == ArtworkDetailTabEnum.DETAILS) {
                val size = _art?.size?.hight.toString() + " x " + _art?.size?.width
                //Artist Label
                itemTraditionalArtDetailsDetailTabData {
                    id("artist")
                    title(getString(R.string.type_of_artwork_))
                    description(_art?.typeOfArtwork)
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //Title Detail Tab
                itemTraditionalArtDetailsDetailTabData {
                    id("title")
                    title(getString(R.string.title))
                    description(_art?.title)
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //Yeas for Detail Tab
                itemTraditionalArtDetailsDetailTabData {
                    id("years")
                    title(getString(R.string.year))
                    description(_art?.yearOfArtRelease)
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //Categories for Detail Tab
                itemTraditionalArtDetailsDetailTabData {
                    id("categories")
                    title(getString(R.string.category))
                    description(_art?.category)
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //Medium for Detail Tab
                itemTraditionalArtDetailsDetailTabData {
                    id("Medium")
                    title(getString(R.string.password_strength_medium))
                    description(_art?.medium)
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //Signature for Detail Tab
                itemTraditionalArtDetailsDetailTabData {
                    id("Signature")
                    title(getString(R.string.signature))
                    description(_art?.signature)
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //Size of art work for Detail Tab
                itemTraditionalArtDetailsDetailTabData {
                    id("size_of_artwork")
                    title(getString(R.string.size_of_artwork))
                    description(size)
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //Certificate issued by for Detail Tab
                itemTraditionalArtDetailsDetailTabData {
                    id("certificate_issued_by")
                    title(getString(R.string.certificate_issued_by))
                    description(_art?.certificateIssuedBy)
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //Condition for detail tab
                itemTraditionalArtDetailsDetailTabData {
                    id("Condition")
                    title(getString(R.string.condition))
                    description(_art?.condition)
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //observations title for detail tab
                itemTraditionalArtDetailsDetailTabData {
                    id("observations_title")
                    title(getString(R.string.observations))
                    spanSizeOverride { _, _, _ -> 2 }
                }
                //observations for detail tab
                itemTraditionalArtDetailsObservationsData {
                    id("observations")
                    description(_art?.observations)
                    spanSizeOverride { _, _, _ -> 2 }
                }
            }
            // Show investment info tab
            else if (tabSelected == ArtworkDetailTabEnum.SALES_HISTORY) { // show work and artist info tab

                //Start soon sale section
                /*if (_futureSale.isNotEmpty()) {
                    _futureSale.forEachIndexed { index, futureSale ->
                        itemTraditionalArtDetailsInvestFutureSale {
                            id("Start Soon $index")
                            futuresale(futureSale)
                            spanSizeOverride { _, _, _ -> 2 }
                        }
                    }
                }*/

                //Sales history section
                if (_pastSale.isNotEmpty()) {

                    _pastSale.forEachIndexed { index, pastSale ->
                        itemTraditionalArtDetailsInvestPastSale {
                            id("Past Sale $index")
                            title("${index + 1}st")
                            currency(_art?.currency)
                            art(_art)
                            pastsale(pastSale)
                            spanSizeOverride { _, _, _ -> 2 }
                        }
                    }
                }


            }


        }


    }

}
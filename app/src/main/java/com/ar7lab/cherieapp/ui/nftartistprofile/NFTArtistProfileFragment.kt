package com.ar7lab.cherieapp.ui.nftartistprofile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ar7lab.cherieapp.*
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.base.navigation.NavManager
import com.ar7lab.cherieapp.databinding.FragmentNftArtistProfileDetailsBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.NftArtistArtsTypeEnum
import com.ar7lab.cherieapp.enums.TopArtistsTypeEnum
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.Artist
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.utils.openInfoDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * NFT Artist Profile Fragment
 * Traditional Artist Profile Fragment
 * Traditional Artist Profile Fragment is almost similar as NFT Artist Profile Fragment
 * @property fragment_nft_artist_profile_details is the xml file for this fragment
 * */
@AndroidEntryPoint
class NFTArtistProfileFragment :
    BaseFragment(R.layout.fragment_nft_artist_profile_details) {

    //Define key for what object is passing from one fragment to another
    companion object {
        private const val KEY_ARTIST = "artist"
        private const val KEY_ARTIST_TYPE = "artistType"
    }

    //View binding fragment_nft_artist_profile_details
    private val binding: FragmentNftArtistProfileDetailsBinding by viewBinding()

    //Initialize NFTArtistProfileViewModel
    private val viewModel: NFTArtistProfileViewModel by viewModels()

    //Initialize Artist object
    private var artist: Artist? = null

    //Initialize Artist object which is coming from previous fragment
    private var artistGet: Artist? = null

    //Initialize Art object
    private var art: ArrayList<Art> = arrayListOf()

    //Initialize Artist type "Traditional" or "Nft"
    // By default "Traditional" is selected
    private var topArtistTypeEnum: TopArtistsTypeEnum = TopArtistsTypeEnum.TRADITIONAL

    //Define NavManager for go to next fragment
    @Inject
    lateinit var navManager: NavManager

    //share preference object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    // Observe ViewModel's state to take action on UI
    private val isLoadingMore = ObservableBoolean(false)
    private val stateObserver = Observer<NFTArtistProfileViewModel.ViewState> { state ->
        //Set page refresh state from it's view model
        binding.isRefreshing = state.isRefreshing
        //Set state for load more item
        isLoadingMore.set(state.isLoadingMore)

        //Set state for show error
        /*if (state.isError) {
            state.message?.let { msg -> showError(msg) }
        }*/

        if (state.isSessionExpired) {
            requireContext().openInfoDialog(layoutInflater,
                object : InfoDialogOkayButtonListener {
                    override fun onOkayButtonClicked() {
                        sharePreferencesManager.clearData()
                        startActivity(Intent(requireContext(), SignInActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    }
                })
        }

        //set state for load artist data
        state.artist?.let {
            artist = it
        }
        //set state for load art data
        state.art?.let {
            art = it.map { art -> art.copy() } as ArrayList
        }
        // Ask Epoxy RecyclerView to rebuild the items
        binding.rvItemsNftArtistProfile.requestModelBuild()

        //Binding Art date if it is not empty
        binding.isHaveData =
            art.isNotEmpty()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //bind view model
        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)
        artistGet =
            arguments?.getSerializable(KEY_ARTIST) as Artist? // Get Artist object from previous page
        topArtistTypeEnum =
            arguments?.getSerializable(KEY_ARTIST_TYPE) as TopArtistsTypeEnum //get Artist type from previous page
        //Pass Artist object and Artist type to it's view model
        viewModel.init(artistGet, topArtistTypeEnum)
        binding.isHaveData = art.isNotEmpty()

        //Set the title
        binding.include.tvTitle.text = getString(R.string.artist)

        //Back to previous page
        binding.include.ivBack.setOnDebouncedClickListener {
            findNavController().popBackStack()
        }

        //Add Items to Recyclerview
        binding.rvItemsNftArtistProfile.withModels {
            itemNftArtistProfileHeader {
                id("header")
                //Pass Artist object
                artist(artist)
                /*
                Epoxy RecyclerView in xml have spanCount = 2,
                so set this item spanSize to 2 to have a full width view
                */
                spanSizeOverride { _, _, _ -> 2 }
                //Pass current view model
                viewModel(viewModel)
                //Pass Artist type
                topArtistType(topArtistTypeEnum)

                listener(object : NFTArtistProfileListener {
                    override fun onClickOnSale() {
                        viewModel.changeNftArtistTypeEnum(NftArtistArtsTypeEnum.ON_SALE)//Click on "On Sale" tab
                    }

                    override fun onClickOwned() {
                        viewModel.changeNftArtistTypeEnum(NftArtistArtsTypeEnum.OWNED)//Click on "Owned" tab
                    }

                    //Click on Follow Artist tab
                    override fun onFollowClick(artist: Artist) {
                        viewModel.followArtist(artist)
                    }

                    //onclicked like button
                    override fun onLikeClicked(artist: Artist) {
                        viewModel.likeArtist(artist)
                    }
                })

            }

            if (viewModel.nftArtistArtsTypeSelected.get()?.name == NftArtistArtsTypeEnum.ON_SALE.name) {
                art.forEachIndexed { index, art ->
                    itemOnSale {
                        id("onsale item $index")
                        spanSizeOverride { _, _, _ -> 1 }
                        //Pass Artist object
                        artist(artist)
                        //Pass Art object
                        art(art)
                        listener(object : ArtistProfileItemListener {
                            //Click for like art
                            override fun likeClick(art: Art) {
                                viewModel.like(art)
                            }

                            //Click for go to Comment fragment
                            override fun viewCommentClick(art: Art) {
                                val action = NFTArtistProfileFragmentDirections.actionArtistProfileToViewArtComments(art)
                                navManager.navigate(action)

                            }
                        })
                    }
                }
            }

            /*//Add Show More view
            if (art.isNotEmpty()) {
                itemShowMoreArtistProfile {
                    id("show_more")
                    spanSizeOverride { _, _, _ -> 2 }
                    //Pass current view model
                    viewModel(viewModel)
                    //Pass isLoadingMore as false initially
                    isLoadingMore(isLoadingMore)
                }
            }*/

        }
    }
}
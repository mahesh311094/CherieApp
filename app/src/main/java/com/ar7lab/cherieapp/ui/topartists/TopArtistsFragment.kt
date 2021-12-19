package com.ar7lab.cherieapp.ui.topartists

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.ObservableField
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
import com.ar7lab.cherieapp.databinding.FragmentTopArtistsBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.TopArtistsTypeEnum
import com.ar7lab.cherieapp.itemLoading
import com.ar7lab.cherieapp.itemShowMore
import com.ar7lab.cherieapp.itemTopArtistsItem
import com.ar7lab.cherieapp.model.Artist
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.utils.openInfoDialog
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TopArtistsFragment : BaseFragment(R.layout.fragment_top_artists) {

    companion object {
        private const val KEY_ARTIST_TYPE = "artistType"
    }

    private val binding: FragmentTopArtistsBinding by viewBinding()

    private val viewModel: TopArtistsViewModel by viewModels()

    @Inject
    lateinit var navManager: NavManager

    override fun isNeedWindowLightStatusBar() = true

    // Define here for the xml data binding
    private var artists: ArrayList<Artist> = arrayListOf()
    private var topArtistTypeEnum: TopArtistsTypeEnum = TopArtistsTypeEnum.TRADITIONAL

    //shareprefrence object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    var showLoading = ObservableField(false)
    var isLoadMore: Boolean = true

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<TopArtistsViewModel.ViewState> {
        binding.isRefreshing = it.isRefreshing
        if (it.isError) {
            if (it.message!!.isNotEmpty())
                showError(it.message)

        }
        if (it.isSessionExpired) {
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

        isLoadMore = it.isLoadingMore
        Timber.e("isLoadMore $isLoadMore")

        artists = it.artists.map { artist -> artist.copy() } as ArrayList<Artist>
        binding.rvItems.requestModelBuild()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        topArtistTypeEnum = (arguments?.getSerializable(KEY_ARTIST_TYPE) as TopArtistsTypeEnum?) ?: TopArtistsTypeEnum.TRADITIONAL

        observe(viewModel.stateLiveData, stateObserver)
        binding.viewModel = viewModel
        binding.topArtistsType = topArtistTypeEnum

        viewModel.init(topArtistTypeEnum)
        viewModel.onRefresh()

        initClickEvent()

        createEpoxyRecyclerView()
    }

    private fun initClickEvent() {
        binding.ivBack.setOnDebouncedClickListener {
            findNavController().popBackStack()
        }

        binding.tvTitle.setOnDebouncedClickListener {
            topArtistTypeEnum = if (topArtistTypeEnum.name == TopArtistsTypeEnum.TRADITIONAL.name)
                TopArtistsTypeEnum.NFT
            else
                TopArtistsTypeEnum.TRADITIONAL

            binding.topArtistsType = topArtistTypeEnum
            viewModel.init(topArtistTypeEnum)
        }
    }

    private fun createEpoxyRecyclerView() {
        binding.rvItems.withModels {
            artists.forEachIndexed { index, artist ->
                itemTopArtistsItem {
                    id("artist $index")
                    artist(artist)
                    listener(object : ArtistListener {
                        override fun onFollowClick(artist: Artist) {
                            viewModel.followArtist(artist)
                        }

                        override fun onClick(artist: Artist) {
                            val action = TopArtistsFragmentDirections.actionNfArtistProfile(artist, topArtistTypeEnum)
                            navManager.navigate(action)
                        }

                    })
                }
            }

            if (showLoading.get() == true && isLoadMore) {
                itemLoading {
                    id("show_loading")
                    spanSizeOverride { _, _, _ -> 3 }
                }
            }

        }

        // Add scroll event to load more data when reach the bottom
        binding.rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!binding.rvItems.canScrollVertically(1) && isLoadMore) {
                    showLoading.set(true)
                    viewModel.loadMore()
                } else {
                    showLoading.set(false)
                }
            }
        })
    }
}
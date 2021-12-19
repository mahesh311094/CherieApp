package com.ar7lab.cherieapp.ui.viewartlikes

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
import com.ar7lab.cherieapp.databinding.FragmentViewArtLikesBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.itemArtLike
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.LikeDetail
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.utils.openInfoDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ViewArtLikesFragment : BaseFragment(R.layout.fragment_view_art_likes) {


    companion object {
        /**
         * Unique key for passing data to this fragment
         */
        private const val KEY_ART = "art"
    }

    private val binding: FragmentViewArtLikesBinding by viewBinding()

    private val viewModel: ViewArtLikesViewModel by viewModels()

    override fun isNeedWindowLightStatusBar(): Boolean = true

    /**
     * Data for the list of users who likes art
     */
    private var _likes: ArrayList<LikeDetail> = arrayListOf()

    //share preference
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<ViewArtLikesViewModel.ViewState> {
        binding.isRefreshing = it.isRefreshing
        if (it.isError) {
            showError(it.message)

        }

        if(it.isSessionExpired)
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

        it.likes?.let { likes ->
            _likes = likes
            binding.isHaveData = likes.isNotEmpty()
            when {
                likes.isEmpty() -> {
                    binding.tvNoLikesYet.text = getString(R.string.no_likes_yet)
                }
            }
        }
        binding.rvItems.requestModelBuild()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)

        binding.tvBack.setOnDebouncedClickListener {
            findNavController().popBackStack()
        }

        (arguments?.getSerializable(KEY_ART) as Art?)?.let { art ->
            viewModel.init(art)
        }

        binding.rvItems.withModels {
            // If data is not found, no need to build view items
            if (binding.isHaveData != true) return@withModels

            _likes.forEachIndexed { index, like ->
                itemArtLike {
                    id("like $index")
                    like(like.users)
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
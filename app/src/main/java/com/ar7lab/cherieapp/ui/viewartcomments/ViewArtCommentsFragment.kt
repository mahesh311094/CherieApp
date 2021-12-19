package com.ar7lab.cherieapp.ui.viewartcomments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.databinding.FragmentViewArtCommentsBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.itemArtComment
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.CommentDetail
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.utils.openInfoDialog
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ViewArtCommentsFragment : BaseFragment(R.layout.fragment_view_art_comments) {

    companion object {
        private const val KEY_ART = "art"
    }

    private val binding: FragmentViewArtCommentsBinding by viewBinding()

    private val viewModel: ViewArtCommentsViewModel by viewModels()

    override fun isNeedWindowLightStatusBar() = true

    // Define here for the xml databinding
    private var _comments: ArrayList<CommentDetail> = arrayListOf()
    //share preference
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<ViewArtCommentsViewModel.ViewState> {
        binding.isRefreshing = it.isRefreshing
        if (it.isError) {
            it.message?.let { msg -> showError(msg) }

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

        it.comments?.let { comments ->
            _comments = comments
            Timber.d("ddd vo day")
            binding.rvItems.requestModelBuild()
        }

        binding.isHaveData = _comments.isNotEmpty()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        observe(viewModel.stateLiveData, stateObserver)

        binding.isHaveData = _comments.isNotEmpty()

        binding.tvBack.setOnDebouncedClickListener {
            findNavController().popBackStack()
        }

        (arguments?.getSerializable(KEY_ART) as Art?)?.let { art ->
            viewModel.init(art)
        }

        binding.rvItems.withModels {
            // If data is not found, no need to build view items
            if (binding.isHaveData != true) return@withModels

            _comments.forEachIndexed { index, comment ->
                itemArtComment {
                    id("comment $index")
                    comment(comment.comments)
                }
            }
        }

        // Add scroll event to load more data when reach the bottom
        binding.rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val scrollPosition =
                    (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                viewModel.saveScrollPosition(scrollPosition)
                if (!recyclerView.canScrollVertically(1)) {
                    viewModel.loadMore()
                }
            }
        })
    }
}
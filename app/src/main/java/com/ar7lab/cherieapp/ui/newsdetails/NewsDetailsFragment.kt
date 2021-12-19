package com.ar7lab.cherieapp.ui.newsdetails

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.databinding.FragmentNewsDetailsBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.model.News
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.utils.*
import com.google.firebase.dynamiclinks.ktx.dynamicLink
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.iosParameters
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * News details screen when navigate from NEWS screen
 */
@AndroidEntryPoint
class NewsDetailsFragment : BaseFragment(R.layout.fragment_news_details) {

    companion object {
        /**
         * This key need to match with the argument name in the nav_graph.xml
         */
        private const val KEY_NEWS = "news"
        private const val KEY_FROM_BOOKMARKED = "isFromBookmarked"

    }

    /**
     * View binding from fragment_news_details.xml
     */
    private val binding: FragmentNewsDetailsBinding by viewBinding()

    private val viewModel: NewsDetailsViewModel by viewModels()


    private var _news: News? = null
    private var _newsId: String? = null
    private var _isBookmarked: Boolean = false

    //share preference object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<NewsDetailsViewModel.ViewState> { state ->
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

        state.news?.let {
            _news = it
            loadWebContent()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)

        /**
         * Get news object from arguments
         */
        _news = arguments?.getSerializable(KEY_NEWS) as News?
        _isBookmarked = arguments?.getBoolean(KEY_FROM_BOOKMARKED) as Boolean

        _news?.let {
            viewModel.init(it, _isBookmarked)
            loadWebContent()
        }

        /**
         * Get deeplink data from navigation component
         */
        arguments?.keySet()?.firstOrNull()?.let { key ->
            if (requireArguments().get(key) is Intent) {
                val deepLink = (requireArguments().get(key) as Intent).data
                _newsId = deepLink?.getQueryParameter(DYNAMIC_LINK_QUERY_NEWS)
            }
        }

        _newsId?.let {
            viewModel.loadNewsDetails(it)
        }

        binding.tvBack.setOnDebouncedClickListener {
            findNavController().popBackStack()
        }

        binding.ivBookmark.setOnDebouncedClickListener {
            viewModel.addRemoveBookmark()
        }

        binding.ivShare.setOnDebouncedClickListener {
            createDynamicLinksToShare()
        }
    }

    private fun createDynamicLinksToShare() {
        val dynamicLink = Firebase.dynamicLinks.dynamicLink {
            link = Uri.parse(String.format(NEWS_DETAILS_DYNAMIC_LINK, _news?.id))
            domainUriPrefix = DEEP_LINK_HOST
            // Open links on iOS
            iosParameters(IOS_BUNDLE_ID) { }

        }

        val dynamicLinkUri = dynamicLink.uri

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "$dynamicLinkUri")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun loadWebContent() {
        binding.news = _news
        /**
         * Load the html content into webview
         */
        val encodedHtml = Base64.encodeToString(_news?.overview?.toByteArray(), Base64.NO_PADDING)
        binding.webView.loadData(encodedHtml, "text/html", "base64")
    }
}
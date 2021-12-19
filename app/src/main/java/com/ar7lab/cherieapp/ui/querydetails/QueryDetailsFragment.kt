package com.ar7lab.cherieapp.ui.querydetails

import android.os.Bundle
import android.util.Base64
import android.view.View
import androidx.navigation.fragment.findNavController
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.databinding.FragmentQueryDetailsBinding
import com.ar7lab.cherieapp.model.FAQ

class QueryDetailsFragment : BaseFragment(R.layout.fragment_query_details) {

    companion object {
        /**
         * This key need to match with the argument name in the nav_graph.xml
         */
        private const val KEY_FAQ = "faq"
    }

    /**
     * View binding from fragment_query_details.xml
     */
    private val binding: FragmentQueryDetailsBinding by viewBinding()

    /**
     * This screen have white background so should set light status bar
     */
    override fun isNeedWindowLightStatusBar(): Boolean = true

    private var _faq: FAQ? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
         * Get faq object from arguments
         */
        _faq = arguments?.getSerializable(KEY_FAQ) as FAQ?

        /**
         * Load the html content into webview
         */
        val encodedHtml = Base64.encodeToString(_faq?.answer?.toByteArray(), Base64.NO_PADDING)
        binding.webView.loadData(encodedHtml, "text/html", "base64")

        binding.tvBack.setOnDebouncedClickListener {
            findNavController().popBackStack()
        }

    }
}
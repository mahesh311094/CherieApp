package com.ar7lab.cherieapp.ui.termsscreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.databinding.ActivityTermsBinding
import com.ar7lab.cherieapp.enums.TermsConditionTypeEnum
import com.ar7lab.cherieapp.utils.LOAD_URL
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TermsActivity : BaseActivity() {

    lateinit var intentUrl: String
    lateinit var loadUrl: String
    lateinit var binding: ActivityTermsBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get the terms name which is clicked by the user.
        intentUrl = intent.getStringExtra(LOAD_URL) ?: ""

        //get the url from string file as per user click in terms.
        loadUrl = when (intentUrl) {
            TermsConditionTypeEnum.TERMS_1.name -> {
                getString(R.string.term_1)
            }
            TermsConditionTypeEnum.TERMS_2.name -> {
                getString(R.string.term_2)
            }
            else -> {
                getString(R.string.term_3)
            }
        }
        title = when (intentUrl) {
            TermsConditionTypeEnum.TERMS_1.name -> {
                getString(R.string.terms_title)
            }
            TermsConditionTypeEnum.TERMS_2.name -> {
                getString(R.string.data_policy)
            }
            else -> {
                getString(R.string.cookie_policy)
            }
        }

        binding.tvTitle.text = title

        //load the url in web view
        binding.vwView.settings.javaScriptEnabled = true
        binding.vwView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                url?.let { view?.loadUrl(it) }
                return true
            }
        }
        binding.vwView.loadUrl(loadUrl)

        binding.tvBack.setOnDebouncedClickListener {
            onBackPressed()
        }

    }

}
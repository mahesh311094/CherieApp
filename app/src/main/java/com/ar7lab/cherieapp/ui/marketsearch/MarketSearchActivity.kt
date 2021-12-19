package com.ar7lab.cherieapp.ui.marketsearch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.databinding.ActivityMarketSearchBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.MarketTypeEnum
import com.ar7lab.cherieapp.itemLoading
import com.ar7lab.cherieapp.itemMarketplace
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.ui.market.MarketArtListener
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.ui.tradingart.TradingArtActivity
import com.ar7lab.cherieapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@AndroidEntryPoint
class MarketSearchActivity : BaseActivity() {

    private val binding: ActivityMarketSearchBinding by viewBinding()
    private val viewModel: MarketSearchViewModel by viewModels()

    //Set boolean false for "Show More" button
    private val isLoadingMore = ObservableBoolean(false)

    //Initialize arts array
    private var arts: ArrayList<Art> = arrayListOf()

    //share preference object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    //This 3 flag is use for show and hide the loading at the bottom of the screen.
    var showLoading = ObservableField(false)
    var isLoadMore: Boolean = true
    var isLoading: Boolean = true

    override fun isNeedWindowLightStatusBar() = true

    lateinit var marketTypeEnum: MarketTypeEnum

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<MarketSearchViewModel.ViewState> { state ->
        binding.isRefreshing = state.isRefreshing
        isLoadingMore.set(state.isLoadingMore)

        if (state.isSessionExpired) {

            openInfoDialog(layoutInflater,
                object : InfoDialogOkayButtonListener {
                    override fun onOkayButtonClicked() {
                        sharePreferencesManager.clearData()
                        startActivity(Intent(this@MarketSearchActivity, SignInActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    }
                })

        }

        // Set list for "Tradition Arts" tab
        // need to copy data to avoid crash with epoxy
        state.forYouArts?.let { _arts ->
            arts = _arts.map { it.copy() } as ArrayList<Art>
        }

        // Set list for "Nft Arts" tab
        // need to copy data to avoid crash with epoxy
        state.nftArts?.let { _arts ->
            arts = _arts.map { it.copy() } as ArrayList<Art>
        }

        // Ask Epoxy RecyclerView to rebuild the items
        binding.rvItems.requestModelBuild()

        //Set position top when Refresh layout is calling
        if (state.isRefreshed) {
            binding.rvItems.scrollToPosition(0)
        }

        //Get the value of loading status and based on that show the loading button.
        if (!state.stateCalled) {
            isLoadMore = state.isLoadingMore
            isLoading = state.isLoading
        }

        //Bind data if arts array is not empty
        binding.isHaveData = arts.isNotEmpty()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        intent?.extras?.let {
            marketTypeEnum = it.get(MARKET_SEARCH_KEY) as MarketTypeEnum
            viewModel.marketTypeSelected.set(marketTypeEnum)
        }

        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)

        binding.parentView.setOnClickListener {
            hideKeyboard()
        }

        searchListener()
        createEpoxyRecyclerView()
    }

    /**
     * Show list of views with epoxy recyclerview
     * @return the list of recyclerview items
     * */
    private fun createEpoxyRecyclerView() {
        binding.rvItems.withModels {
            if (binding.isHaveData != true) return@withModels
            arts.forEachIndexed { index, art ->
                itemMarketplace {
                    id("traditional_art $index")
                    /*
                        Epoxy RecyclerView in xml have spanCount = 2,
                        so set this item spanSize to 1 to have a grid view
                     */
                    spanSizeOverride { _, _, _ -> 1 }
                    //Pass art array for show arts data
                    art(art)
                    //Listener for item click
                    listener(object : MarketArtListener {
                        override fun onClick(art: Art) {
                            val bundle = Bundle().apply {
                                putSerializable("art", art)
                            }
                            startActivity(Intent(this@MarketSearchActivity, TradingArtActivity::class.java).apply { putExtras(bundle) })
                        }

                        override fun likeClick(art: Art) {
                            viewModel.like(art)
                        }
                    })
                }
            }

            //This will check there is any need to show the loading button at the bottom or not.
            if (showLoading.get() == true && isLoadMore) {
                itemLoading {
                    id("show_loading")
                    spanSizeOverride { _, _, _ -> 2 }
                }
            }
        }

        // Add scroll event to load more data when reach the bottom
        binding.rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                //isLoadMore & isLoading is use for show the loading at the end of items and also hide after get the data from server.
                if (!binding.rvItems.canScrollVertically(1) && isLoadMore && !isLoading) {
                    showLoading.set(true)
                    viewModel.loadMore()
                } else {
                    showLoading.set(false)
                }
            }
        })
    }


    private fun searchListener() {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.etSearch.requestFocus()
        imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)

        binding.ivBack.setOnDebouncedClickListener { finish() }

        binding.etSearch.textChanges()
            .filterNot { it.isNullOrBlank() }
            .debounce(800L)
            .map { it.toString() }
            .onEach {
                //<editor-fold desc="If letter length is more then 0 then show result, otherwise show no data found">
                if (it.length > 2)
                    viewModel.search(it)
                else {
                    binding.isHaveData = false
                    viewModel.isNeedSearch.set(false)
                }
                //</editor-fold>
            }
            .launchIn(lifecycleScope)

    }
}
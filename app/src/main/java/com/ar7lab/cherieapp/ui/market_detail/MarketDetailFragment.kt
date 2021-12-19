package com.ar7lab.cherieapp.ui.market_detail

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
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
import com.ar7lab.cherieapp.databinding.FragmentDepositBinding
import com.ar7lab.cherieapp.databinding.FragmentMarketDetailBinding
import com.ar7lab.cherieapp.databinding.FragmentSecondStepDepositBindingImpl
import com.ar7lab.cherieapp.databinding.FragmentTraditionalArtworkDetailsBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.MarketMyTradesTypeEnum
import com.ar7lab.cherieapp.enums.MarketTradingTypeEnum
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.ArtTransactionDetails
import com.ar7lab.cherieapp.ui.market.MarketFragmentDirections
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.ui.tradingart.TradingArtViewModel
import com.ar7lab.cherieapp.ui.tradingart.paymentflow.BuyBottomSheetFragment
import com.ar7lab.cherieapp.ui.tradingart.paymentflow.SellBottomSheetFragment
import com.ar7lab.cherieapp.utils.openInfoDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * this fragment is copy of Deposite fragment in design there two step first step is selection bank type or card
 * if select card than we will fetch card & if select bank than we will fetch it
 * currenly this fragment not used after API don't required we will remove this
 */
@AndroidEntryPoint
class MarketDetailFragment : BaseFragment(R.layout.fragment_market_detail),WalletTopUpRedirectionListener {

    @Inject
    lateinit var navManager: NavManager

    private val isLoadingMore = ObservableBoolean(false)
    private val viewModel: TradingArtViewModel by viewModels()
    private var artTransactionDetails: ArrayList<ArtTransactionDetails> = arrayListOf()

    //share pref object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager


    //binding
    private val binding: FragmentMarketDetailBinding by viewBinding()


    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<TradingArtViewModel.ViewState> { state ->
        //binding.isRefreshing = state.isRefreshing
        isLoadingMore.set(state.isLoadingMore)
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
        state.artTransactionDetailsFullTrading?.let {
            artTransactionDetails = it
        }
        state.artTransactionDetailsMyTrading?.let {
            artTransactionDetails = it
        }
        binding.rvItems.requestModelBuild()
        binding.isHaveData = artTransactionDetails.isNotEmpty()
        when {
            artTransactionDetails.isEmpty() -> {
                binding.tvNoItemFound.text = getString(R.string.no_items_found)
                binding.tvNoItemsFoundDesc.text =
                    getString(R.string.you_dont_have_item_to_trade_buy_one_to_trade)
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            binding.art = it.getSerializable("art") as Art
        }
        binding.viewModel = viewModel

        observe(viewModel.stateLiveData, stateObserver)

        addClickEvent()

        viewModel.init(binding.art?.id!!)
        createEpoxyRecyclerView()
        viewModel.loadData()

        binding.include.tvTitle.text = getString(R.string.trade)

    }
    private fun createEpoxyRecyclerView() {
        binding.rvItems.withModels {
            //art header
            itemMarketDetailHeader {
                id("header")
                art(binding.art)
            }
            //First Tabing Chart Market Trades My Trading
            itemMarketDetailTabing {
                id("tabbing")
                art(binding.art)
                viewModel(viewModel)
            }
            //if selected tab is Chart than Chart related item will show
            if (viewModel.marketTradingTypeSelected.get() == MarketTradingTypeEnum.CHART) {
                //Chart Tabbing
                itemMarketDetailTabingChart {
                    id("chart_tab")
                    art(binding.art)
                    viewModel(viewModel)
                }
                //Chart Layout
                itemMarketDetailChartLayout {
                    id("chart_layout")
                    art(binding.art)
                    viewModel(viewModel)
                }
                //Chart list header
                itemMarketDetailChartTimePriceAmountHeader {
                    id("chart_data_header")
                }
                //Chart list data
                (0..10).forEachIndexed { index, value ->
                    itemMarketDetailChartTimePriceAmountData {
                        id("data" + index)
                    }
                }
                //Chart Pagination footer
                itemWalletPageNoFooter {
                    id("footer")
                }

            } else if (viewModel.marketTradingTypeSelected.get() == MarketTradingTypeEnum.MARKET_TRADES) {
                //selected tab is market trades than this part will show
                itemMarketDetailMarketTradeHeader {
                    id("market_trade_header")
                }
                //Sell Item will show
                (1..25 step 5).forEachIndexed { index, value ->
                    itemMarketDetailMarketTradeData {
                        id("market_trade_data$index")
                        progress(value + 10)
                        value(value.toString())
                    }
                }
                //divider will show here
                itemMarketDetailMarketTradeDataDivider {
                    id("divider")
                }
                //buy data will show using this layout
                (1..25 step 5).forEachIndexed { index, value ->
                    itemMarketDetailMarketTradeDataBuy {
                        id("market_trade_data$index")
                        progress(value + 10)
                        value(value.toString())
                    }
                }

            } else if (viewModel.marketTradingTypeSelected.get() == MarketTradingTypeEnum.MY_TRADES) {
                //Last tab selection time my trades wil show
                // My trade tabbing
                itemMarketDetailTabingMyTrade {
                    id("item_tab_my_trade")
                    viewModel(viewModel)
                }
                //first tab is selected
                if (viewModel.marketMyTradesTypeSelected.get() == MarketMyTradesTypeEnum.OPEN_ORDERS) {
                    //open order header layout
                    itemMarketDetailTabingMyTradeOpenOrderHeader {
                        id("item_tab_open_order_header")
                    }
                    //open order data layout
                    (1..10).forEachIndexed { index, value ->
                        itemMarketDetailTabingMyTradeOpenOrderData {
                            id("market_trade_data$index")

                        }
                    }
                    //pagination footer
                    itemWalletPageNoFooter {
                        id("footer")
                    }
                } else {
                    //Trade History Header
                    itemMarketDetailTabingMyTradeTradeHistoryHeader {
                        id("item_tab_trade_history_header")
                    }
                    //Trade History  data
                    (1..10).forEachIndexed { index, value ->
                        itemMarketDetailTabingMyTradeTradeHistoryData {
                            id("market_trade_data$index")

                        }
                    }
                    //pagination bottom
                    itemWalletPageNoFooter {
                        id("footer")
                    }
                }
            }

            /*artTransactionDetails.forEachIndexed { index, artTransactionDetails ->
                itemMarketplaceTraditionalArtTrading {
                    id("store_art $index")
                    *//*
                        Epoxy RecyclerView in xml have spanCount = 2,
                        so set this item spanSize to 1 to have a grid view
                     *//*
                    spanSizeOverride { _, _, _ -> 2 }
                    arttransactiondetails(artTransactionDetails)
                    art(binding.art)
                    listener(object : ArtTransactionListener {
                        override fun onClick(art: Art, transactionDetail: ArtTransactionDetails) {
                            val bundle = Bundle().apply {
                                putSerializable(ART, art)
                                putSerializable(TRANSACTION_DETAIL, transactionDetail)
                            }
                            startActivity(
                                Intent(
                                    applicationContext,
                                    SelectPaymentActivity::class.java
                                ).apply { putExtras(bundle) })
                        }
                    })
                }
            }

            if (artTransactionDetails.isNotEmpty()) {
                itemShowMoreTrading {
                    id("show_more")
                    spanSizeOverride { _, _, _ -> 2 }
                    viewModel(viewModel)
                    isLoadingMore(isLoadingMore)
                }
            }*/
        }
    }

    private fun addClickEvent() {
        /*binding.tvFullTrading.setOnDebouncedClickListener {
            viewModel.changeMarketTradingType(MarketTradingTypeEnum.FULL_TRADING)
        }
        binding.tvMyTrading.setOnDebouncedClickListener {
            viewModel.changeMarketTradingType(MarketTradingTypeEnum.MY_TRADING)
        }*/
        binding.include.ivBack.setOnDebouncedClickListener {
            findNavController().popBackStack()
        }
        //on buy Button clicked
        binding.btnBuy.setOnClickListener {
            val modalBottomSheet = BuyBottomSheetFragment(this)
            modalBottomSheet.show(childFragmentManager, BuyBottomSheetFragment.TAG)

        }
        //on sell button clicked
        binding.btnSell.setOnClickListener {
            val modalBottomSheet = SellBottomSheetFragment(this)
            modalBottomSheet.show(childFragmentManager, SellBottomSheetFragment.TAG)
        }
    }

    override fun isNeedWindowLightStatusBar() = true
    override fun onTopUpClicked() {
        val action = MarketDetailFragmentDirections.actionMarketDetailToDeposit()
        navManager.navigate(action)
    }

}
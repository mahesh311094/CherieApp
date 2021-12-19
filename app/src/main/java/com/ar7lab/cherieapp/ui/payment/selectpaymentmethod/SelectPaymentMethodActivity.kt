package com.ar7lab.cherieapp.ui.payment.selectpaymentmethod

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.databinding.ActivitySelectPaymentMethodBinding
import com.ar7lab.cherieapp.itemSavedCards
import com.ar7lab.cherieapp.ui.payment.addnewcard.AddNewCardActivity
import androidx.lifecycle.Observer
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.itemSelectPaymentMethodAddNewCard
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.CardDetails
import com.ar7lab.cherieapp.model.PaymentTransactionDetails
import com.ar7lab.cherieapp.ui.payment.failurepayment.FailurePaymentActivity
import com.ar7lab.cherieapp.ui.payment.successpayment.SuccessPaymentActivity
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.ui.wallet.WalletListener
import com.ar7lab.cherieapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SelectPaymentMethodActivity : BaseActivity() {
    private val binding: ActivitySelectPaymentMethodBinding by viewBinding()
    private val viewModel: SelectPaymentMethodViewModel by viewModels()
    private var cards: ArrayList<CardDetails> = arrayListOf()
    private lateinit var paymentTransactionDetails: PaymentTransactionDetails
    private var paymentMethodId: String = ""
    private var art: Art? = null
    private var pieces: Int = 0
    private var item_price: Double = 0.0

    //share pref object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager


    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<SelectPaymentMethodViewModel.ViewState> { state ->
        binding.isRefreshing = state.isRefreshing
        binding.isLoadingCreatePayment = state.isLoadingCreatePayment
        if (state.isError) {
            showError(state.message)

        }

        if (state.isSessionExpired) {

            applicationContext.openInfoDialog(layoutInflater,
                object : InfoDialogOkayButtonListener {
                    override fun onOkayButtonClicked() {
                        sharePreferencesManager.clearData()
                        startActivity(
                            Intent(applicationContext, SignInActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                    }
                })

        }

        // need to copy data to avoid crash with epoxy
        state.cards?.let { cardsDetails ->
            cards = cardsDetails.map { card -> card.copy() } as ArrayList<CardDetails>
        }
        //Redirect to Success page after successful payment
        state.paymentTransactionDetails?.let { paymentTransactionDetails ->
            val bundle = Bundle().apply {
                putSerializable("payment_transaction_details", paymentTransactionDetails)
                putSerializable(ART, art)
                putInt(PIECES, pieces)
                putDouble(ITEM_PRICE, item_price)
            }
            startActivity(
                Intent(this, SuccessPaymentActivity::class.java).apply {
                    putExtras(bundle)
                }
            )
            finish()
        }
        //Redirect to Failed page after fail payment
        state.paymentTransactionDetailsFailed?.let { paymentTransactionDetails ->
            val bundle = Bundle().apply {
                putSerializable("payment_transaction_details", paymentTransactionDetails)
                putSerializable(ART, art)
                putInt(PIECES, pieces)
                putDouble(ITEM_PRICE, item_price)
            }
            startActivity(
                Intent(this, FailurePaymentActivity::class.java).apply {
                    putExtras(bundle)
                }
            )
            finish()
        }
        binding.rvSavedCard.requestModelBuild()
        if (state.isRefreshed) {
            binding.rvSavedCard.scrollToPosition(0)
        }
        binding.isHaveData = cards.isNotEmpty()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.viewModel = viewModel
        intent?.extras?.let {
            art = it.getSerializable(ART) as Art
            viewModel.art = art!!
            pieces = it.getInt(PIECES)
            viewModel.pieces = pieces
            item_price = it.getDouble(ITEM_PRICE)
            viewModel.item_price = item_price
            binding.totalAmount = (pieces * item_price).toString() + " " + art?.currency

        }
        observe(viewModel.stateLiveData, stateObserver)
        addClickEvent()
        createEpoxyRecyclerView()
        viewModel.onRefresh()
    }

    //For Show the Saved Card listing
    private fun createEpoxyRecyclerView() {
        var resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    viewModel.onRefresh()
                }
            }
        binding.rvSavedCard.withModels {
            if (binding.isHaveData != null && binding.isHaveData!!) {
                cards.forEachIndexed { index, card_details ->
                    itemSavedCards {
                        id("cards $index")
                        spanSizeOverride { _, _, _ -> 1 }
                        cardDetails(card_details)
                        listenerCreatePayment(object : CreatePaymentListener {
                            override fun onClick(cardDetails: CardDetails) {
                                Timber.e(cardDetails.id)
                                paymentMethodId = card_details.id
                                viewModel.handleSelectCard(cardDetails)
                            }

                        })
                    }

                }
            }

            itemSelectPaymentMethodAddNewCard {
                id("add_new_card")
                    .walletListener(object : AddCardListener {
                        override fun onClick() {
                            val intent = Intent(applicationContext, AddNewCardActivity::class.java)
                            resultLauncher.launch(intent)
                        }


                    })
            }
        }
    }

    private fun addClickEvent() {

        binding.tvBack.setOnDebouncedClickListener {
            onBackPressed()
        }
        binding.btnCreatePayment.setOnDebouncedClickListener {
            viewModel.createPayment(paymentMethodId)
        }

    }

}
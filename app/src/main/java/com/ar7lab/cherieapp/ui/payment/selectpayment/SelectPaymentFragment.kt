package com.ar7lab.cherieapp.ui.payment.selectpayment

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.ar7lab.cherieapp.*
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.base.navigation.NavManager
import com.ar7lab.cherieapp.databinding.*
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.CheckoutTypeEnum
import com.ar7lab.cherieapp.model.*
import com.ar7lab.cherieapp.network.response.GetWalletBalanceResponse
import com.ar7lab.cherieapp.ui.bank_detail.BankDetailFragment
import com.ar7lab.cherieapp.ui.payment.cards_banks_dailogs.AddBankBottomSheetFragment
import com.ar7lab.cherieapp.ui.payment.cards_banks_dailogs.AddCardBottomSheetFragment
import com.ar7lab.cherieapp.ui.payment.failurepayment.FailurePaymentActivity
import com.ar7lab.cherieapp.ui.payment.successpayment.SuccessPaymentActivity
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.ui.traditionalartworkdetails.TraditionalArtworkDetailsFragment
import com.ar7lab.cherieapp.ui.wallet.WalletFragment
import com.ar7lab.cherieapp.ui.wallet.WalletFragmentDirections
import com.ar7lab.cherieapp.ui.wallet.WalletViewModel
import com.ar7lab.cherieapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * This Fragment is used to selection deposite amount from multiple button like$50,$100
 * also This flagment will inflate after press deposite button
 *
 */
@AndroidEntryPoint
class SelectPaymentFragment : BaseFragment(R.layout.fragment_select_payment) {
    //View binding of select payment
    private val binding: FragmentSelectPaymentBinding by viewBinding()

    @Inject
    lateinit var navManager: NavManager

    //User balance variable
    private var userBalance: GetWalletBalanceResponse.Assets? = null

    //Share pref object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    //Initialize SelectPaymentViewModel
    private val viewModel: SelectPaymentViewModel by viewModels()

    var itemPrice: Double = 0.0
    private var totalPieces: Int = 1
    private var totalPrice: Double = 0.0
    private var art: Art? = null

    //bank detail getting API
    private var bankDetails: ArrayList<BankDetails> = arrayListOf()

    //Store the cards in list
    private var cards: ArrayList<CardDetails> = arrayListOf()

    /* // Observe ViewModel's state to take action on UI
     private val stateObserver = Observer<TradingArtViewModel.ViewState> {

     }*/

    private val stateObserver = Observer<SelectPaymentViewModel.ViewState> {
        //loading time show refresh layout
        binding.isRefreshing = it.isLoading
        //if error true error message will show
        if (it.isError) {
            showError(it.message)
        }
        //session expired dilog show
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
        //if bankdetails not null than get bank detail set in global variable
        it.bankDetails?.let { bankDetails ->
            Log.v("banlD", it.bankDetails.toString() )
            this.bankDetails.clear()
            this.bankDetails = bankDetails.map { card -> card.copy() } as ArrayList<BankDetails>
        }
        //if cardlist not emapty set data
        it.cards?.let { cardsDetails ->
            cards.clear()
            cards = cardsDetails.map { card -> card.copy() } as ArrayList<CardDetails>
        }

        it.cardStatus?.let {
            if(it == "success"){
                startPaymentSucessActivity()
            }else{
                startPaymentFailActivity()
            }
        }

        it.userBalance?.let{
            userBalance = it
        }

        if(it.cherieStatus.equals("success")){
            startPaymentSucessActivity()
        }else if(it.cherieStatus.equals("error")){
            if(totalPrice>viewModel.getUserTotalBalance()){
                cherieLackBalanceAlertDialog()
            }else{
                showError(it.cherieStatus)
            }
        }

        //refresh layout
        binding.rvDeposit.requestModelBuild()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*intent?.extras?.let {
            art = it.getSerializable(ART) as Art
            binding.art = art

        }*/
        (requireActivity() as BaseActivity).clearWindowLightStatus()

        binding.viewModel = viewModel

        art = arguments?.getSerializable("art") as Art?
        binding.art = art


        observe(viewModel.stateLiveData, stateObserver)

        //binding.include.tvTitle.text = getString(R.string.checkout)

        if (art?.yearOfArtRelease != null) {
            if (art?.yearOfArtRelease!!.isEmpty()) {
                val span: Spannable = SpannableString(art?.name)
                binding.tvArtName.text = span
            } else {
                val artRelease = getString(R.string.art_name, art?.name, art?.yearOfArtRelease)

                val span: Spannable = SpannableString(artRelease)
                if (art?.name != null) {
                    span.setSpan(RelativeSizeSpan(0.75f), art?.name!!.length+1, artRelease.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    span.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.dove_gray)), art?.name!!.length+1, artRelease.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    binding.tvArtName.text = span
                }
            }
        }

        //binding.tvArtName.text = getString(R.string.art_name, art?.name, art?.yearOfArtRelease)
        binding.tvPerPiecePrice.text = getString(R.string.per_piece, art?.currency, itemPrice)

        totalPrice = art!!.price

        addClickEvent()
        //load data
        viewModel.loadData()
    }

    //Click event for go to payment method page
    private fun addClickEvent() {
        binding.ivBack.setOnDebouncedClickListener {
            findNavController().popBackStack()
        }
        binding.amountMinusBtn.setOnDebouncedClickListener {

            if(totalPieces>1){
                totalPrice -= art!!.price
                totalPieces -= 1
                binding.totalPrice.text = getString(R.string.total_price, art?.currency, totalPrice)
                binding.tvAmount.text = requireContext().getString(R.string.pieces_number, totalPieces)
            }

        }

        binding.amountPlusBtn.setOnDebouncedClickListener {

            totalPieces += 1
            totalPrice = (art!!.price) * (totalPieces)
            binding.totalPrice.text = getString(R.string.total_price, art?.currency, totalPrice)
            binding.tvAmount.text = requireContext().getString(R.string.pieces_number, totalPieces)

        }

        addExpoxyLayout()
    }

    private fun addExpoxyLayout() {
        binding.rvDeposit.withModels {

            itemWalletDepositSelectPaymentHeader{
                id("select_payment_header")
            }

            itemCheckoutCheriePayment {
                id("cherie_payment")
                viewModel(viewModel)
                totalBalance(viewModel.getUserTotalBalance().toString())
                paymentListener(object : SelectPaymentListener {
                    override fun onClicked() {
                        cherieLackBalanceAlertDialog()
                    }
                })
            }

            //if payment type bank selected
            if (viewModel.typeSelected.get() == CheckoutTypeEnum.BANK) {
                //if there are no data then show empty layout
                if (bankDetails.isNullOrEmpty()) {
                    itemCheckoutDepositSelectEmptyBankTransfer {
                        id("empty_bank_tranfer")
                        viewModel(viewModel)
                        paymentListener(object : SelectPaymentListener {
                            override fun onClicked() {
                                addBankAccountDialog()
                            }
                        })
                    }
                } else {
                    //bank transfer header
                    itemCheckoutSelectDataBankTransferHeader {
                        id("bank_transfer_header")
                        viewModel(viewModel)
                        paymentListener(object : SelectPaymentListener {
                            override fun onClicked() {
                                addBankAccountDialog()
                            }
                        })

                    }
                    //bank list item inflate
                    bankDetails.forEachIndexed { index, bankDetail ->
                        //last item inflate
                        if (index == bankDetails.lastIndex) {
                            itemCheckoutSelectDataBankTransferListItemLast {
                                id("bank_transfer_item_last" + index)
                                bankDetail(bankDetail)
                                viewModel(viewModel)
                                indexValue(index)
                                isChecked(viewModel.isBankPositionSelected(index))
                            }
                        } else {
                            //inter item inflate
                            itemCheckoutSelectDataBankTransferListItem {
                                id("bank_transfer_item" + index)
                                bankDetail(bankDetail)
                                viewModel(viewModel)
                                indexValue(index)
                                isChecked(viewModel.isBankPositionSelected(index))
                            }
                        }

                    }
                }
                //empty card view show
                itemCheckoutSelectEmptyCreditCards {
                    id("empty_credit_card")
                    viewModel(viewModel)
                    paymentListener(object : SelectPaymentListener {
                        override fun onClicked() {
                            creaditCardDialog()
                        }
                    })
                }
            } else if(viewModel.typeSelected.get() == CheckoutTypeEnum.CARD) {
                // payment type card selection time this part will execute
                if (cards.isNullOrEmpty()) {
                    //empty bank layout header
                    itemCheckoutSelectEmptyBankTransfer {
                        id("empty_bank_tranfer")
                        viewModel(viewModel)
                        paymentListener(object : SelectPaymentListener {
                            override fun onClicked() {
                                addBankAccountDialog()
                            }
                        })
                    }
                    //emtpy credit card layout
                    itemCheckoutSelectEmptyCreditCards {
                        id("empty_credit_card")
                        viewModel(viewModel)
                        paymentListener(object : SelectPaymentListener {
                            override fun onClicked() {
                                creaditCardDialog()
                            }
                        })
                    }
                } else {
                    //empty bank layout
                    itemCheckoutSelectEmptyBankTransfer {
                        id("empty_bank_tranfer")
                        viewModel(viewModel)
                        paymentListener(object : SelectPaymentListener {
                            override fun onClicked() {
                                addBankAccountDialog()
                            }
                        })
                    }
                    //Card Layout header
                    itemCheckoutSelectDataCardHeader {
                        id("header_data")
                        viewModel(viewModel)
                        paymentListener(object : SelectPaymentListener {
                            override fun onClicked() {
                                creaditCardDialog()
                            }
                        })

                    }
                    //Card loop
                    cards.forEachIndexed { index, cardDetails ->
                        //last index inflate
                        if (index == cards.lastIndex) {
                            itemCheckoutSelectDataCardListItemLast {
                                id("last_item" + index)
                                cardDetails(cardDetails)
                                viewModel(viewModel)
                                indexValue(index)
                                isChecked(viewModel.isCardPositionSelected(index))
                            }
                        } else {
                            //Inner item inflate
                            itemCheckoutSelectDataCardListItem {
                                id("inner_item" + index)
                                cardDetails(cardDetails)
                                viewModel(viewModel)
                                indexValue(index)
                                isChecked(viewModel.isCardPositionSelected(index))
                            }
                        }
                    }
                }

            }else
            {
                //empty bank layout header
                itemCheckoutSelectEmptyBankTransfer {
                    id("empty_bank_tranfer")
                    viewModel(viewModel)
                    paymentListener(object : SelectPaymentListener {
                        override fun onClicked() {
                            addBankAccountDialog()
                        }
                    })
                }
                //emtpy credit card layout
                itemCheckoutSelectEmptyCreditCards {
                    id("empty_credit_card")
                    viewModel(viewModel)
                    paymentListener(object : SelectPaymentListener {
                        override fun onClicked() {
                            creaditCardDialog()
                        }
                    })
                }

            }

            //button layout inflater
            itemCheckoutDepositSelectPayment {
                id("button ")
                viewModel(viewModel)
                paymentListener(object : SelectPaymentListener {
                    override fun onClicked() {

                        if (viewModel.typeSelected.get() == CheckoutTypeEnum.BANK) {

                            if (viewModel._selected_card_position.get() == -1 && viewModel._selected_bank_position.get() == -1) {
                                showError(getString(R.string.please_select_bank_or_cards))
                            } else {
                                WALLETORCHECKOUT = "checkout"

                                val action =
                                    SelectPaymentFragmentDirections.actionSelectPaymentFragmentToBankDetail(false, art?.id, art?.salesInfo?.currentSale?.id.toString(), totalPieces.toString(), totalPrice.toString(), bankDetails[viewModel._selected_bank_position.get()]._id,"checkout", null,0f)
                                navManager.navigate(action)
                            }

                        } else if (viewModel.typeSelected.get() == CheckoutTypeEnum.CARD) {
                            if (viewModel._selected_card_position.get() == -1 && viewModel._selected_bank_position.get() == -1) {
                                showError(getString(R.string.please_select_bank_or_cards))
                            } else {
                                viewModel.paymentThroughCard(cards[0].id,  art?.id.toString(), totalPrice.toString())
                            }

                        }else if (viewModel.typeSelected.get() == CheckoutTypeEnum.CHERIE) {
                            viewModel.paymentThroughCherieWallet(art?.id.toString(), totalPieces.toString(), totalPrice.toString(), art?.salesInfo?.currentSale?.id.toString())
                        }
                    }
                })
            }
        }
    }

    private fun addBankDetailsFragmentToActivity(fragment: Fragment?){

       /* if (fragment == null) return

        val bundle = Bundle()
        bundle.putBoolean("isSetting", false)
        bundle.putString("checkoutOrWallet", "checkout")
        bundle.putString("artId", art?.id)
        bundle.putString("artShares", totalPieces.toString())
        bundle.putString("totalPrice", totalPrice.toString())
        bundle.putString("bankId", bankDetails[viewModel._selected_bank_position.get()]._id)
        fragment.arguments = bundle

        val fm = supportFragmentManager
        val tr = fm.beginTransaction()
        tr.add(R.id.root_container, fragment)
        tr.commit()*/
    }

    fun cherieLackBalanceAlertDialog() {
        val bind: LackOfBalanceAlertLayoutBinding = LackOfBalanceAlertLayoutBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(bind.root)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.window?.setGravity(Gravity.CENTER);
        alertDialog.show()
        bind.ivClose.setOnClickListener {
            alertDialog.dismiss()
        }

        bind.btnTopUp.setOnClickListener {
            alertDialog.dismiss()

            val action =
                SelectPaymentFragmentDirections.actionSelectPaymentFragmentToWallet()
            navManager.navigate(action)
        }
    }

    //Open add creadit card dialog
    fun creaditCardDialog() {
        val modalBottomSheet = AddCardBottomSheetFragment()
        modalBottomSheet.show(parentFragmentManager, AddCardBottomSheetFragment.TAG)
        modalBottomSheet.setFragmentResultListener(AddCardBottomSheetFragment.REQUEST_KEY) { key, bundle ->
            viewModel.onRefresh()
        }
    }

    //Open bank add dialog
    fun addBankAccountDialog() {
        val modalBottomSheet = AddBankBottomSheetFragment()
        modalBottomSheet.show(parentFragmentManager, AddBankBottomSheetFragment.TAG)
        modalBottomSheet.setFragmentResultListener(AddBankBottomSheetFragment.REQUEST_KEY) { key, bundle ->
            viewModel.onRefresh()
        }
    }

    //call this activity on payment through card failure
    private fun startPaymentFailActivity() {
        val bundle = Bundle().apply {
            putSerializable(ART, "Art")
            putInt(PIECES, totalPieces)
            putDouble(ITEM_PRICE, totalPrice)
        }
        startActivity(Intent(requireContext(), FailurePaymentActivity::class.java).apply {
            putExtras(bundle)
        })
    }

    //call this activity on payment through card success
    private fun startPaymentSucessActivity() {
        val bundle = Bundle().apply {
            putSerializable(ART, "Art")
            putInt(PIECES, totalPieces)
            putDouble(ITEM_PRICE, art!!.price)
            putDouble(TOTAL_PRICE, totalPrice)
        }
        startActivity(Intent(requireContext(), SuccessPaymentActivity::class.java).apply {
            putExtras(bundle)
        })
    }

    override fun isNeedWindowLightStatusBar() = true

}
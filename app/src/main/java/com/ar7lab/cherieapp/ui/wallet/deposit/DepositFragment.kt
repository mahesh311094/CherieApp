package com.ar7lab.cherieapp.ui.wallet.deposit

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
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
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.DepositeTypeEnum
import com.ar7lab.cherieapp.model.BankDetails
import com.ar7lab.cherieapp.model.CardDetails
import com.ar7lab.cherieapp.ui.notification.NotificationFragment
import com.ar7lab.cherieapp.ui.payment.cards_banks_dailogs.AddBankBottomSheetFragment
import com.ar7lab.cherieapp.ui.payment.cards_banks_dailogs.AddCardBottomSheetFragment
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.ui.wallet.WalletFragment
import com.ar7lab.cherieapp.ui.wallet.payment_failed.DepositFailurePaymentActivity
import com.ar7lab.cherieapp.ui.wallet.payment_success.DepositSuccessPaymentActivity
import com.ar7lab.cherieapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * This Fragment is used to selection deposite amount from multiple button like$50,$100
 * also This flagment will inflate after press deposite button
 *
 */
@AndroidEntryPoint
class DepositFragment : BaseFragment(R.layout.fragment_deposit) {
    //navigation manager
    @Inject
    lateinit var navManager: NavManager

    //binding
    private val binding: FragmentDepositBinding by viewBinding()

    //viewModel
    private val viewModel: DepositViewModel by viewModels()

    //bank detail getting API
    private var bankDetails: ArrayList<BankDetails> = arrayListOf()

    //Store the cards in list
    private var cards: ArrayList<CardDetails> = arrayListOf()

    //Share pref object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    //state change observer
    private val stateObserver = Observer<DepositViewModel.ViewState> {
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
            this.bankDetails.clear()
            this.bankDetails = bankDetails.map { card -> card.copy() } as ArrayList<BankDetails>
        }
        //if cardlist not emapty set data
        it.cards?.let { cardsDetails ->
            cards.clear()
            cards = cardsDetails.map { card -> card.copy() } as ArrayList<CardDetails>
        }
        if(it.isCardTranferSuccess)
        {
            if(it.status.equals("success"))
            {
                showError(it.message)
                startDepositPaymentSucessActivity()
            }
            else
            {
                showError(it.message)
            }
        }
        //refresh layout
        binding.rvDeposit.requestModelBuild()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //binding
        binding.viewModel = viewModel
        //observer observe
        observe(viewModel.stateLiveData, stateObserver)
        //on back button clicked
        binding.ivBack.setOnDebouncedClickListener {
            setFragmentResult(NotificationFragment.REQUEST_KEY, bundleOf(REDIRECT to "None"))
            findNavController().popBackStack()
        }
        //epoxy layout create method call
        addExpoxyLayout()
    }

    //here codding for expo layout inflate
    fun addExpoxyLayout() {
        binding.rvDeposit.withModels {
            //header with plus minus button
            itemWalletDepositeHeader {
                id("deposite_header")
                viewModel(viewModel)
                selectedValue(viewModel.getSelectedAmount())
            }
            itemWalletDepositSelectPaymentHeader {
                id("select_payment_header")
            }
            //if payment type bank selected
            if (viewModel.typeSelected.get() == DepositeTypeEnum.BANK) {
                //if there are no data then show empty layout
                if (bankDetails.isNullOrEmpty()) {
                    itemWalletDepositSelectEmptyBankTransfer {
                        id("empty_bank_tranfer")
                        viewModel(viewModel)
                        depositListener(object : DepositListener {
                            override fun onClicked() {
                                addBankAccountDialog()
                            }
                        })
                    }
                } else {
                    //bank transfer header
                    itemWalletDepositSelectDataBankTransferHeader {
                        id("bank_transfer_header")
                        viewModel(viewModel)
                        depositListener(object : DepositListener {
                            override fun onClicked() {
                                addBankAccountDialog()
                            }
                        })

                    }
                    //bank list item inflate
                    bankDetails.forEachIndexed { index, bankDetail ->
                        //last item inflate
                        if (index == bankDetails.lastIndex) {
                            itemWalletDepositSelectDataBankTransferListItemLast {
                                id("bank_transfer_item_last" + index)
                                bankDetail(bankDetail)
                                viewModel(viewModel)
                                indexValue(index)
                                isChecked(viewModel.isBankPositionSelected(index))
                            }
                        } else {
                            //inter item inflate
                            itemWalletDepositSelectDataBankTransferListItem {
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
                itemWalletDepositSelectEmptyCreditCards {
                    id("empty_credit_card")
                    viewModel(viewModel)
                    depositListener(object : DepositListener {
                        override fun onClicked() {
                            creaditCardDialog()
                        }
                    })
                }
            } else {
                // payment type card selection time this part will execute
                if (cards.isNullOrEmpty()) {
                    //empty bank layout header
                    itemWalletDepositSelectEmptyBankTransfer {
                        id("empty_bank_tranfer")
                        viewModel(viewModel)
                        depositListener(object : DepositListener {
                            override fun onClicked() {
                                addBankAccountDialog()
                            }
                        })
                    }
                    //emtpy credit card layout
                    itemWalletDepositSelectEmptyCreditCards {
                        id("empty_credit_card")
                        viewModel(viewModel)
                        depositListener(object : DepositListener {
                            override fun onClicked() {
                                creaditCardDialog()
                            }
                        })
                    }
                } else {
                    //empty bank layout
                    itemWalletDepositSelectEmptyBankTransfer {
                        id("empty_bank_tranfer")
                        viewModel(viewModel)
                        depositListener(object : DepositListener {
                            override fun onClicked() {
                                addBankAccountDialog()
                            }
                        })
                    }
                    //Card Layout header
                    itemWalletDepositSelectDataCardHeader {
                        id("header_data")
                        viewModel(viewModel)
                        depositListener(object : DepositListener {
                            override fun onClicked() {
                                creaditCardDialog()
                            }
                        })

                    }
                    //Card loop
                    cards.forEachIndexed { index, cardDetails ->
                        //last index inflate
                        if (index == cards.lastIndex) {
                            itemWalletDepositSelectDataCardListItemLast {
                                id("last_item" + index)
                                cardDetails(cardDetails)
                                viewModel(viewModel)
                                indexValue(index)
                                isChecked(viewModel.isCardPositionSelected(index))
                            }
                        } else {
                            //Inner item inflate
                            itemWalletDepositSelectDataCardListItem {
                                id("inner_item" + index)
                                cardDetails(cardDetails)
                                viewModel(viewModel)
                                indexValue(index)
                                isChecked(viewModel.isCardPositionSelected(index))
                            }
                        }
                    }
                }

            }

            //button layout inflater
            itemWalletDepositSelectPayment {
                id("button ")
                viewModel(viewModel)
                depositListener(object : DepositListener {
                    override fun onClicked() {
                        if (viewModel._selected_card_position.get() == -1 && viewModel._selected_bank_position.get() == -1) {
                            showError(getString(R.string.please_select_bank_or_cards))
                        } else {
                            if (viewModel.typeSelected.get() == DepositeTypeEnum.BANK) {
                                if (viewModel._selected_amount.get() == 0) {
                                    showError(getString(R.string.please_select_amount))
                                } else {
                                    WALLETORCHECKOUT = "wallet"
                                    val action =
                                        DepositFragmentDirections.actionDepositToBankDetail(
                                            false, null, null,null, null, null,
                                            WALLETORCHECKOUT,
                                            bankDetails.get(viewModel._selected_bank_position.get()),
                                            viewModel.getSelectedAmount().toFloat()
                                        )
                                    navManager.navigate(action)
                                }
                            } else {
                                if (viewModel._selected_amount.get() == 0) {
                                    showError(getString(R.string.please_select_amount))
                                } else {
                                    viewModel.sendCardTrasferReqest(
                                        cards.get(viewModel._selected_card_position.get()),
                                        viewModel.getSelectedAmount()
                                    )
                                }
                            }

                        }
                    }
                })
            }
        }
    }

    //start sucess payment activity
    fun startDepositPaymentSucessActivity() {
        val bundle = Bundle().apply {
            putSerializable(CARD_DETAILS, cards.get(viewModel._selected_card_position.get()))
            putFloat(ITEM_PRICE, viewModel.getSelectedAmount().toFloat())
            putBoolean(IS_BANK_DETAIL,false)
        }
        startActivity(Intent(requireContext(), DepositSuccessPaymentActivity::class.java).apply {
            putExtras(bundle)
        })
    }

    //startPayment Failed Activity
    fun startPaymentFailActivity() {
        val bundle = Bundle().apply {
            putSerializable(ART, "Art")
            putInt(PIECES, 1)
            putDouble(ITEM_PRICE, 22.0)
        }
        startActivity(Intent(requireContext(), DepositFailurePaymentActivity::class.java).apply {
            putExtras(bundle)
        })
    }

    //Open add creadit card dialog
    fun creaditCardDialog() {
        val modalBottomSheet = AddCardBottomSheetFragment()
        modalBottomSheet.show(childFragmentManager, AddCardBottomSheetFragment.TAG)
        modalBottomSheet.setFragmentResultListener(AddCardBottomSheetFragment.REQUEST_KEY) { key, bundle ->
            viewModel.onRefresh()
        }


    }

    //Open bank add dialog
    fun addBankAccountDialog() {
        val modalBottomSheet = AddBankBottomSheetFragment()
        modalBottomSheet.show(childFragmentManager, AddBankBottomSheetFragment.TAG)
        modalBottomSheet.setFragmentResultListener(AddBankBottomSheetFragment.REQUEST_KEY) { key, bundle ->
            viewModel.onRefresh()
        }
    }

    override fun isNeedWindowLightStatusBar() = true

}
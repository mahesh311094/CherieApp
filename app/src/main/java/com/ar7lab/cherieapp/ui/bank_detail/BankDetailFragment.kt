package com.ar7lab.cherieapp.ui.bank_detail

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ar7lab.cherieapp.*
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.base.navigation.NavManager
import com.ar7lab.cherieapp.databinding.BankPaymentDoneLayoutBinding
import com.ar7lab.cherieapp.databinding.FragmentBankDetailBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.model.BankDetail
import com.ar7lab.cherieapp.ui.payment.selectpayment.SelectPaymentViewModel
import com.ar7lab.cherieapp.model.BankDetails
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.ui.wallet.payment_success.DepositSuccessPaymentActivity
import com.ar7lab.cherieapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


/**
 * A simple [BaseFragment] subclass.
 * selected bank detail will show here also cherie bank detail will show in this fragment
 */
@AndroidEntryPoint
class BankDetailFragment : BaseFragment(R.layout.fragment_bank_detail) {

    @Inject
    lateinit var navManager: NavManager

    //binding
    private val binding: FragmentBankDetailBinding by viewBinding()

    //view model
    private val viewModel: BankDetailViewModel by viewModels()

    //Initialize SelectPaymentViewModel
    private val paymentViewModel: SelectPaymentViewModel by viewModels()

    //bank detail list
    val bankDetailList: ArrayList<BankDetail> = arrayListOf()

    var bankDetails:BankDetails= BankDetails()
    var amount:Float=0f

    //share preference object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    private val stateObserver = Observer<BankDetailViewModel.ViewState> {

        //show progressbar

        //user balance getting from model

        if (it.isError) {
            showError(it.message)

        }
        if(it.status.equals("success")) {
            successDialog()
        }

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

        it.bankStatus?.let{
            if(it.equals("success")) {
                successDialog()
            }
        }

        //refresh recyclerview
        binding.rvBankDetail.requestModelBuild()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe(viewModel.stateLiveData, stateObserver)
        addData()
        addEpoxyLayout()
        arguments?.getSerializable(BANK_DETAILS)?.let {
            bankDetails=it as BankDetails
        }

        arguments?.getFloat("amount").let {
            amount=it!!
        }

        binding.ivBack.setOnClickListener {

            if(arguments?.getString("checkoutOrWallet").equals("wallet")){
                findNavController().popBackStack()
            }else if(arguments?.getString("checkoutOrWallet").equals("checkout")){
                requireActivity().onBackPressed()
            }
        }
    }

    fun addData() {
        //country
        bankDetailList.add(
            BankDetail(
                getString(R.string.country),
                getString(R.string.country_name)
            )
        )
        //bank name
        bankDetailList.add(
            BankDetail(
                getString(R.string.bank_name),
                getString(R.string.united_overseas_bank_limited)
            )
        )
        //bank address
        bankDetailList.add(
            BankDetail(
                getString(R.string.bank_address),
                getString(R.string.bank_address_value)
            )
        )
        //bank swift code
        bankDetailList.add(
            BankDetail(
                getString(R.string.bank_swift_code_label),
                getString(R.string.bank_swift_code_value)
            )
        )
        //beneficiary name
        bankDetailList.add(
            BankDetail(
                getString(R.string.beneficiary_name),
                getString(R.string.treasuregate_pte_ltd)
            )
        )
        //beneficiary account no
        bankDetailList.add(
            BankDetail(
                getString(R.string.beneficiary_account_no),
                getString(R.string.beneficiary_account_no_value)
            )
        )
        //Account branch
        bankDetailList.add(
            BankDetail(
                getString(R.string.account_branch),
                getString(R.string.account_branch_value)
            )
        )
        //beneficiary address
        bankDetailList.add(
            BankDetail(
                getString(R.string.beneficiary_address),
                getString(R.string.beneficiary_address_detail)
            )
        )
    }

    //add expoxy layout
    fun addEpoxyLayout() {
        binding.rvBankDetail.withModels {
            //header title & description
            itemBankDetailHeadPart {
                id("header_part")
            }
            //table header
            itemBankDetailHeadPartHeader {
                id("header_title")
            }
            //table data
            bankDetailList.forEachIndexed { index, bankDetail ->
                if (index == bankDetailList.lastIndex) {
                    //last item layout
                    itemBankDetailInnerItemLast {
                        id("last_item")
                        label(bankDetail.title)
                        value(bankDetail.value)
                    }

                } else {
                    //if 4 index add new header
                    if (index == 4) {
                        itemBankDetailHeadPartTitle {
                            id("header_title")
                        }
                    }
                    //bank inner item
                    itemBankDetailInnerItem {
                        id("inner_item" + index)
                        label(bankDetail.title)
                        value(bankDetail.value)
                    }
                }
            }
            //if user get from setting hide confirm button
            if (!arguments?.getBoolean("isSetting")!!) {
                itemBankDetailConfirmButton {
                    id("confirm_button")
                    listener(object : OnConfirmButtonClicked {
                        override fun onConfirmButtonClicked() {
                            if(arguments?.getString("checkoutOrWallet").equals("wallet")){

                                viewModel.sendBankTrasferReqest(bankDetails,amount)

                            }else if(arguments?.getString("checkoutOrWallet").equals("checkout")){
                                val bankId = arguments?.getString("bankId")
                                val artId = arguments?.getString("artId")
                                val salesId = arguments?.getString("salesId")
                                val artShares = arguments?.getString("totalPieces")
                                val totalPrice = arguments?.getString("totalPrice")
                                viewModel.paymentThroughBank(bankId!!, artId!!, artShares!!, totalPrice!!, salesId!!)
                            }
                        }
                    })
                }
            }
        }
    }

    //start sucess payment activity
    fun startDepositPaymentSucessActivity() {
        val bundle = Bundle().apply {
            putSerializable(BANK_DETAILS, bankDetails)
            putFloat(ITEM_PRICE, amount)
            putBoolean(IS_BANK_DETAIL,true)
        }
        startActivity(Intent(requireContext(), DepositSuccessPaymentActivity::class.java).apply {
            putExtras(bundle)
        })
        findNavController().popBackStack()
    }

    private fun successDialog()
    {
        val bind: BankPaymentDoneLayoutBinding = BankPaymentDoneLayoutBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(bind.root)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.window?.setGravity(Gravity.CENTER);
        alertDialog.show()
        bind.ivClose.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.setOnDismissListener {
            findNavController().popBackStack()
        }
    }

    override fun isNeedWindowLightStatusBar() = true


}
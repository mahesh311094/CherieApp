package com.ar7lab.cherieapp.ui.wallet.payment_success

import android.os.Bundle
import androidx.activity.viewModels
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import androidx.lifecycle.Observer
import com.ar7lab.cherieapp.databinding.ActivityDepositSuccessPaymentBinding
import com.ar7lab.cherieapp.databinding.ActivitySuccessPaymentBinding
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.BankDetails
import com.ar7lab.cherieapp.model.CardDetails
import com.ar7lab.cherieapp.model.PaymentTransactionDetails
import com.ar7lab.cherieapp.ui.payment.successpayment.SuccessPaymentViewModel
import com.ar7lab.cherieapp.utils.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DepositSuccessPaymentActivity  : BaseActivity() {
    //View binding for activity_success_payment
    private val binding: ActivityDepositSuccessPaymentBinding by viewBinding()
    //Initialize  SuccessPaymentViewModel
    private val viewModel: DepositSuccessPaymentViewModel by viewModels()

    /*private var art: Art? = null
    private var pieces: Int = 0
    private var item_price: Double = 0.0*/
    // Observe ViewModel's state to take action on UI

    private var bankDetails:BankDetails?=null
    private var cardDetails:CardDetails?=null
    private var amount:Float?=null
    private var isBankTranfer=false
    private val stateObserver = Observer<SuccessPaymentViewModel.ViewState> {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.viewModel = viewModel
        intent?.extras?.let {
            if(it.getBoolean(IS_BANK_DETAIL,false)) {
                bankDetails = it.getSerializable(BANK_DETAILS) as BankDetails
                amount = it.getFloat(ITEM_PRICE)
                binding.bankDetails=bankDetails
                binding.price="$"+amount.toString()
                isBankTranfer=true
                binding.isBankTrasfer=true;
            }
            else{
                cardDetails=it.getSerializable(CARD_DETAILS) as CardDetails
                amount=it.getFloat(ITEM_PRICE)
                isBankTranfer=false
                binding.price="$"+amount.toString()
                binding.isBankTrasfer=false;
                binding.cardDetails=cardDetails
            }
        }


        addClickEvent()

        viewModel.loadData()
    }


    //Go back to previous screen
    private fun addClickEvent() {
        binding.ivBack.setOnDebouncedClickListener {
            onBackPressed()
        }
        binding.btnDone.setOnDebouncedClickListener {
            onBackPressed()
        }
    }

    override fun isNeedWindowLightStatusBar(): Boolean {
        return true
    }

}
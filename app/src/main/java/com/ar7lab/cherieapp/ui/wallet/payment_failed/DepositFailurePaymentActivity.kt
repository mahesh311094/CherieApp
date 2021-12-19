package com.ar7lab.cherieapp.ui.wallet.payment_failed

import android.os.Bundle
import androidx.activity.viewModels
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import androidx.lifecycle.Observer
import com.ar7lab.cherieapp.databinding.ActivityDepositFailurePaymentBinding
import com.ar7lab.cherieapp.databinding.ActivityFailurePaymentBinding
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.PaymentTransactionDetails
import com.ar7lab.cherieapp.ui.payment.failurepayment.FailurePaymentViewModel
import com.ar7lab.cherieapp.utils.ART
import com.ar7lab.cherieapp.utils.ITEM_PRICE
import com.ar7lab.cherieapp.utils.PIECES

class DepositFailurePaymentActivity  : BaseActivity() {
    //View binding for activity_success_payment
    private val binding: ActivityDepositFailurePaymentBinding by viewBinding()
    //Initialize  SuccessPaymentViewModel
    private val viewModel: DepositFailurePaymentViewModel by viewModels()

    private var art: Art? = null
    private var pieces: Int = 0
    private var item_price: Double = 0.0
    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<FailurePaymentViewModel.ViewState> {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
       /* intent?.extras?.let {
            binding.paymentTransactionDetails = it.getSerializable("payment_transaction_details") as PaymentTransactionDetails
            art = it.getSerializable(ART) as Art
            viewModel.art=art!!
            pieces=it.getInt(PIECES)
            binding.piece=pieces.toString()
            item_price=it.getDouble(ITEM_PRICE)
            binding.price=item_price.toString()+" "+art?.currency
        }*/
        binding.viewModel = viewModel

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

}
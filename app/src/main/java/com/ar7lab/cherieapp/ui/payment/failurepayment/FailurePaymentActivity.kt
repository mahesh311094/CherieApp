package com.ar7lab.cherieapp.ui.payment.failurepayment

import android.os.Bundle
import androidx.activity.viewModels
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import androidx.lifecycle.Observer
import com.ar7lab.cherieapp.databinding.ActivityFailurePaymentBinding
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.PaymentTransactionDetails
import com.ar7lab.cherieapp.utils.ART
import com.ar7lab.cherieapp.utils.ITEM_PRICE
import com.ar7lab.cherieapp.utils.PIECES
import com.ar7lab.cherieapp.utils.TOTAL_PRICE

class FailurePaymentActivity  : BaseActivity() {
    //View binding for activity_success_payment
    private val binding: ActivityFailurePaymentBinding by viewBinding()
    //Initialize  SuccessPaymentViewModel
    //private val viewModel: FailurePaymentViewModel by viewModels()

    private var art: Art? = null
    private var pieces: Int = 0
    private var item_price: Double = 0.0
    private var total_price: Double = 0.0
    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<FailurePaymentViewModel.ViewState> {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        intent?.extras?.let {
            //binding.paymentTransactionDetails = it.getSerializable("payment_transaction_details") as PaymentTransactionDetails
            //art = it.getSerializable(ART) as Art
            //viewModel.art=art!!
            pieces=it.getInt(PIECES)
            binding.piece=pieces.toString()
            item_price=it.getDouble(ITEM_PRICE)
            total_price=it.getDouble(TOTAL_PRICE)
            //binding.price = item_price.toString()+" "+art?.currency

            binding.noPieces.text = pieces.toString()
            binding.perPrice.text = "$"+item_price.toString()
            binding.totalPrice.text = "$"+total_price.toString()
        }
        //binding.viewModel = viewModel

        addClickEvent()

        //viewModel.loadData()
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
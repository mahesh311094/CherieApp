package com.ar7lab.cherieapp.ui.payment.successpayment

import android.os.Bundle
import androidx.activity.viewModels
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import androidx.lifecycle.Observer
import com.ar7lab.cherieapp.databinding.ActivitySuccessPaymentBinding
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.PaymentTransactionDetails
import com.ar7lab.cherieapp.utils.ART
import com.ar7lab.cherieapp.utils.ITEM_PRICE
import com.ar7lab.cherieapp.utils.PIECES
import com.ar7lab.cherieapp.utils.TOTAL_PRICE
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SuccessPaymentActivity  : BaseActivity() {
    //View binding for activity_success_payment
    private val binding: ActivitySuccessPaymentBinding by viewBinding()
    //Initialize  SuccessPaymentViewModel
    //private val viewModel: SuccessPaymentViewModel by viewModels()

    private var art: Art? = null
    private var pieces: Int = 0
    private var item_price: Double = 0.0
    private var total_price: Double = 0.0
    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<SuccessPaymentViewModel.ViewState> {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        //binding.viewModel = viewModel
        intent?.extras?.let {
            /*art = it.getSerializable(ART) as Art*/
            //viewModel.art=art!!
            //binding.paymentTransactionDetails = it.getSerializable("payment_transaction_details") as PaymentTransactionDetails
            pieces=it.getInt(PIECES)
            binding.piece=pieces.toString()
            item_price=it.getDouble(ITEM_PRICE)
            total_price=it.getDouble(TOTAL_PRICE)
            binding.price = item_price.toString()+"$"

            binding.noPieces.text = pieces.toString()
            binding.perPrice.text = "$"+item_price.toString()
            binding.totalPrice.text = "$"+total_price.toString()
        }


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
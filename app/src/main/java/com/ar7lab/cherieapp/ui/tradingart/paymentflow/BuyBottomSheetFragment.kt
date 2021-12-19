package com.ar7lab.cherieapp.ui.tradingart.paymentflow

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.ar7lab.cherieapp.databinding.MarketplaceBuyBottomSheetsBinding
import com.ar7lab.cherieapp.ui.market_detail.WalletTopUpRedirectionListener
import com.ar7lab.cherieapp.ui.payment.successpayment.SuccessPaymentActivity
import com.ar7lab.cherieapp.ui.wallet.payment_failed.DepositFailurePaymentActivity
import com.ar7lab.cherieapp.utils.ART
import com.ar7lab.cherieapp.utils.ITEM_PRICE
import com.ar7lab.cherieapp.utils.PIECES
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
/***
 * buy bottomsheet for market place
 * */
@AndroidEntryPoint
class BuyBottomSheetFragment(val walletTopUpRedirectionListener: WalletTopUpRedirectionListener) : BottomSheetDialogFragment() {
    //binding
    lateinit var binding: MarketplaceBuyBottomSheetsBinding
    //view model
    private val viewModel: BuyBottomSheetViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MarketplaceBuyBottomSheetsBinding.inflate(inflater)
        initAllControls()
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel=viewModel
    }
    fun initAllControls() {
        //on close button clicked
        binding.ivClose.setOnClickListener { dismiss() }
        binding.btnConfirm.setOnClickListener {
            startPaymentSucessActivity()
        }
        binding.tvWalletTopUpLabel.setOnClickListener {
            walletTopUpRedirectionListener.onTopUpClicked()
        }
    }
    fun startPaymentSucessActivity() {
        val bundle = Bundle().apply {
            putSerializable(ART, "Art")
            putInt(PIECES, 1)
            putDouble(ITEM_PRICE, 22.0)
        }
        startActivity(Intent(requireContext(), SuccessPaymentActivity::class.java).apply {
            putExtras(bundle)
        })
    }

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

    companion object {
        const val TAG = "BuyBottomSheetFragment"
    }
}
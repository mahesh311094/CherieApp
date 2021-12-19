package com.ar7lab.cherieapp.ui.payment.cards_banks_dailogs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.databinding.BottomSheetAddBankBinding
import com.ar7lab.cherieapp.databinding.BottomSheetAddCardBinding
import com.ar7lab.cherieapp.databinding.MarketplaceBuyBottomSheetsBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.ui.notification.NotificationFragment
import com.ar7lab.cherieapp.ui.payment.successpayment.SuccessPaymentActivity
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.ui.tradingart.paymentflow.BuyBottomSheetViewModel
import com.ar7lab.cherieapp.ui.wallet.payment_failed.DepositFailurePaymentActivity
import com.ar7lab.cherieapp.utils.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/***
 * buy bottomsheet for market place
 * */
@AndroidEntryPoint
class AddCardBottomSheetFragment : BottomSheetDialogFragment() {
    //binding
    lateinit var binding: BottomSheetAddCardBinding
    //view model
    private val viewModel: AddCardBottomSheetViewModel by viewModels()

    //share preference
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager
    //stateObserver
    private val stateObserver = Observer<AddCardBottomSheetViewModel.ViewState> {
        binding.isLoading = it.isLoading
        //Show error message when getting error message from network calling
        if (it.isError) {
            it.message?.let { msg -> showError(msg) }

        }
        //session expired time show infow dialog
        if(it.isSessionExpired)
        {
            context?.applicationContext?.openInfoDialog(layoutInflater,
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
        //Successfully added the bank
        if (it.isSucess) {
            it.message?.let { msg -> showError(msg) }
            setFragmentResult(REQUEST_KEY, bundleOf())
            dismiss()
        }
    }

    //show error message
    fun showError(message: String?) {
        if (message == null) return
        val snackbar= Snackbar.make(binding.ivClose, message, Snackbar.LENGTH_SHORT)
        snackbar.show()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetAddCardBinding.inflate(inflater)
        initAllControls()
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel=viewModel
        observe(viewModel.stateLiveData, stateObserver)
    }
    fun initAllControls() {
        //on close button clicked
        binding.ivClose.setOnClickListener {
            dismiss()
        }
        /*binding.btnConfirm.setOnClickListener {
            startPaymentSucessActivity()
        }*/
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
        const val TAG = "AddCardBottomSheetFragment"
        val REQUEST_KEY="REQUEST_KEY"
    }
}
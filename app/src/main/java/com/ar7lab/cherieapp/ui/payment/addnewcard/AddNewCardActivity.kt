package com.ar7lab.cherieapp.ui.payment.addnewcard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.widget.doOnTextChanged
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.databinding.ActivityAddNewCardBinding
import androidx.lifecycle.Observer
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.ui.payment.selectpaymentmethod.SelectPaymentMethodActivity
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.utils.openInfoDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class AddNewCardActivity  : BaseActivity() {
    //View binding for add new card
    private val binding: ActivityAddNewCardBinding by viewBinding()
    //Initialize AddNewCardViewModel
    private val viewModel: AddNewCardViewModel by viewModels()
    //share pref object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager
    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<AddNewCardViewModel.ViewState> {
        binding.isLoading = it.isLoading
        //Show error message when getting error message from network calling
        if (it.isError) {
            it.message?.let { msg -> showError(msg) }

        }

        if(it.isSessionExpired)
        {
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
        //Successfully added the card
        if (it.isAccCardSuccess) {
            it.message?.let { msg -> showError(msg) }
            setResult(RESULT_OK)
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)
        viewModel.init()
        addClickEvent()

        binding.etExpiryDate.doOnTextChanged { text, start, count, after ->
            // action which will be invoked when the text is changing
            if(text?.length != 0) {
                /*
                Split Expiry date using separator "/"
                Format MM/YYYY
                */
                if (text!!.length == 2) {
                    if(start==2 && count==1 && !text.toString().contains("/")){
                        binding.etExpiryDate.setText(""+ text.toString()[0]);
                        binding.etExpiryDate.setSelection(1);
                    }
                    else {
                        binding.etExpiryDate.setText("$text/")
                        binding.etExpiryDate.setSelection(3);
                    }
                }
            }
        }

        //load data
        viewModel.loadData()
    }

    //Click event for add new card
    private fun addClickEvent() {
        binding.tvBack.setOnDebouncedClickListener {
            onBackPressed()
        }
        binding.btnSave.setOnDebouncedClickListener {
            viewModel.addNewCard()
        }
    }

    override fun isNeedWindowLightStatusBar()=false

}
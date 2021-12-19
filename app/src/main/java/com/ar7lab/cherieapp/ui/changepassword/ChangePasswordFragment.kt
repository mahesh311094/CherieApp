package com.ar7lab.cherieapp.ui.changepassword

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ar7lab.cherieapp.BuildConfig
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.databinding.ChangePasswordSuccessDialogBinding
import com.ar7lab.cherieapp.databinding.FragmentChangePasswordBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.utils.openInfoDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Change password Fragment
 * @property fragment_change_password is the xml file for this fragment
 * */
@AndroidEntryPoint
class ChangePasswordFragment : BaseFragment(R.layout.fragment_change_password) {
    //binding view
    private val binding: FragmentChangePasswordBinding by viewBinding()

    //View Model Object
    private val viewModel: ChangePasswordViewModel by viewModels()

    //share preference object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    //Google Sign In
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun isNeedWindowLightStatusBar() = false

    //stateObserver of sucess result
    private val stateObserver = Observer<ChangePasswordViewModel.ViewState> {
        binding.isLoading = it.isLoading
        if (it.isError) {
            showError(it.message)

        }

        if (it.isSessinExpired) {
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
        //After signup success it will go to the SignUp Screen page
        if (it.isSucessfullyChange) {
            openInfoDialog()
        }
    }

    //success message dialog
    private fun openInfoDialog() {
        val bind: ChangePasswordSuccessDialogBinding =
            ChangePasswordSuccessDialogBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(context)
        builder.setView(bind.root)
        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
        bind.ivClose.setOnClickListener {
            alertDialog.dismiss()
            logout()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as BaseActivity).clearWindowLightStatus()
        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)
        //init model
        viewModel.init()
        //google object
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.DEFAULT_WEB_CLIENT_ID)
            .requestEmail()
            .build()
        //Google Sign In Client
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        //On Back Button Clicked
        binding.ivBack.setOnDebouncedClickListener {
            findNavController().popBackStack()
        }
        //On reset Password button click
        binding.btnResetPassword.setOnClickListener {

            viewModel.changePassword()
        }


    }

    //after change password logout method will call
    fun logout() {
        sharePreferencesManager.clearData()
        googleSignInClient.signOut()
        startActivity(
            Intent(requireContext(), SignInActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }


}
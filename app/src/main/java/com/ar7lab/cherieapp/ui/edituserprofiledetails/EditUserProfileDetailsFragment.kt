package com.ar7lab.cherieapp.ui.edituserprofiledetails

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.databinding.FragmentEditUserProfileDetailsBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.AccountTypeEnum
import com.ar7lab.cherieapp.model.User
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.ui.signup.CountryCodeAdapter
import com.ar7lab.cherieapp.ui.signup.CountryPhoneCodeAdapter
import com.ar7lab.cherieapp.ui.verifyotp.VerifyOtpActivity
import com.ar7lab.cherieapp.utils.getCountries
import com.ar7lab.cherieapp.utils.openInfoDialog
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * EditUserProfileDetailsFragment for edit Personal anc Company user profile
 * @property fragment_edit_user_profile_details is the xml file for this fragment
 * */
@AndroidEntryPoint
class EditUserProfileDetailsFragment : BaseFragment(R.layout.fragment_edit_user_profile_details) {

    companion object {
        private const val KEY_USER = "user"
    }

    private val binding: FragmentEditUserProfileDetailsBinding by viewBinding()
    private val viewModel: EditUserProfileDetailsViewModel by viewModels()
    private var user: User? = null

    //share preference object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    lateinit var auth: FirebaseAuth

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<EditUserProfileDetailsViewModel.ViewState> {
        binding.isLoading = it.isLoading
        if (it.isError) {
            showError(it.message)

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
        //After signup success it will go to the SignInActivity page
        if (it.isEditUserProfileSuccess) {
            it.message?.let { msg -> showError(msg) }
            findNavController().popBackStack()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //(requireActivity() as BaseActivity).clearWindowLightStatus()
        auth = FirebaseAuth.getInstance()
        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)
        user = arguments?.getSerializable(KEY_USER) as User?
        viewModel.init()

        //Check the current user whether he logged in already to auto-login
        val currentUser = auth.currentUser
        if (currentUser != null) {
            /*startActivity(Intent(applicationContext, Home::class.java))
            finish()*/
        }

        //binding.etUserName.setText(user?.userName)
        viewModel.setUserName(user?.userName)
        viewModel.setFirstName(user?.firstName)
        viewModel.setLastName(user?.lastName)
        viewModel.setEmail(user?.email)
        viewModel.setCompanyName(user?.companyName)
        viewModel.setContactNo(user?.contactNo)

        if (user?.accountType == AccountTypeEnum.COMPANY) {
            binding.tilCompanyName.visibility = View.VISIBLE
            viewModel.selectCompany()
        } else {
            binding.tilCompanyName.visibility = View.GONE
            viewModel.selectPersonal()
        }

        //Click event for Signup
        binding.btnSave.setOnDebouncedClickListener {
            if (viewModel.updatedContactNumber() == user?.contactNo) {
                viewModel.editUserProfile()
            } else {
                if (auth.currentUser == null) {
                    validContactNumber(viewModel.contactNo.get()!!)
                }
            }
        }

        //Country dropdown
        // get list countries to show on dropdown
        val countries = requireContext().getCountries()

        countries.forEachIndexed { index, country ->
            if (country.code3 == user?.country) {
                binding.actCountry.setText(country.name)
                viewModel.setCountry(country.code3)
                viewModel.setCountryPhoneCode(country.phone)
            }
        }

        binding.actCountry.setAdapter(CountryCodeAdapter(requireContext(), countries))
        binding.actCountry.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                binding.actCountry.setText(countries[position].name)
                viewModel.setCountry(countries[position].code3)
            }

        //Phone number country code dropdown
        binding.actCountryPhoneCode.setAdapter(CountryPhoneCodeAdapter(requireContext(), countries))
        binding.actCountryPhoneCode.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                viewModel.setCountryPhoneCode(countries[position].phone) //set country phone code
            }
        //set drop down width
        binding.actCountryPhoneCode.dropDownWidth =
            (resources.displayMetrics.widthPixels - resources.getDimension(R.dimen.margin_normal) * 2).toInt()

        binding.tvBack.setOnDebouncedClickListener {
            findNavController().popBackStack()
        }

    }

    private fun validContactNumber(updatedContactNumber: String) {
        var number = updatedContactNumber

        if (number.isNotEmpty()) {
            number = "+91$number"
            val intent = Intent(requireContext(), VerifyOtpActivity::class.java)
            intent.putExtra("updatedContactNumber", number)
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "Enter mobile number", Toast.LENGTH_SHORT).show()
        }
    }

}
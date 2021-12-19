package com.ar7lab.cherieapp.ui.signup

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.Constants
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.databinding.ActivitySignUpBinding
import com.ar7lab.cherieapp.enums.AccountTypeEnum
import com.ar7lab.cherieapp.enums.TermsConditionTypeEnum
import com.ar7lab.cherieapp.ui.payment.successpayment.SuccessPaymentActivity
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.ui.termsscreen.TermsActivity
import com.ar7lab.cherieapp.ui.verifyotp.VerifyOtpActivity
import com.ar7lab.cherieapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * SignUpActivity for signup
 * @property activity_sign_up is the xml file for this activity
 * */
@AndroidEntryPoint
class SignUpActivity : BaseActivity() {
    //View binding for activity_sign_up
    private val binding: ActivitySignUpBinding by viewBinding()

    //Initialize SignUpViewModel
    private val viewModel: SignUpViewModel by viewModels()

    override fun isNeedWindowLightStatusBar() = true

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<SignUpViewModel.ViewState> {
        binding.isLoading = it.isLoading
        if (it.isError) {
            showError(it.message)
        }
        if (it.isTermsNotChecked) {
            showError(getString(R.string.terms_condition_accept))
        }

        //After signup success it will go to the SignInActivity page
        if (it.isSignUpSuccess) {
            it.message?.let { msg -> showError(msg) }

            Handler(Looper.getMainLooper()).postDelayed({
                val bundle = Bundle().apply {
                    putString(SIGNUP_TYPE, viewModel.signUpTypeSelected.get()?.name)
                    if (viewModel.signUpTypeSelected.get() == AccountTypeEnum.PERSONAL)
                        putString(SIGNUP_EMAIL_MOBILE, viewModel.email.get().toString())
                    else {
                        putString(SIGNUP_EMAIL_MOBILE, viewModel.countryPhoneCode.get()!! + "-" + viewModel.contactNo.get()!!)
                        putParcelable(RESEND_TOKEN, viewModel.resendToken)
                    }
                    putString(SIGNUP_PASSWORD, viewModel.password.get().toString())
                    putString(STORED_VERIFICATION_ID, viewModel.storedVerificationId)
                }

                startActivity(Intent(this@SignUpActivity, VerifyOtpActivity::class.java).apply {
                    putExtras(bundle)
                    finish()
                })
            }, 2000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)
        viewModel.init()

        //Click event for Signup
        binding.btnSignUp.setOnDebouncedClickListener {
            hideKeyboard()
            viewModel.signUp(this)
        }

        //Set the title
        binding.include.ivBack.visibility = View.INVISIBLE
        binding.include.tvTitle.text = getString(R.string.create_cherie_account)

        binding.include.ivBack.setOnDebouncedClickListener {
            hideKeyboard()
            finish()
        }

        viewModel.callBackOTP(this)

        //Country dropdown
        // get list countries to show on dropdown
        val countries = getCountries()
        binding.actCountry.setAdapter(CountryCodeAdapter(this, countries))
        binding.actCountry.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                binding.actCountry.setText(countries[position].name)
                viewModel.setCountry(countries[position].code3)
            }

        //Phone number country code dropdown
        binding.actCountryPhoneCode.setAdapter(CountryPhoneCodeAdapter(this, countries))
        binding.actCountryPhoneCode.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                viewModel.setCountryPhoneCode(countries[position].phone) //set country phone code
            }


        //set drop down width
        binding.actCountryPhoneCode.dropDownWidth =
            (resources.displayMetrics.widthPixels - resources.getDimension(R.dimen.margin_normal) * 2).toInt()

        //Hide keyboard after selecting
        binding.actCountry.setOnClickListener {
            hideKeyboard()
        }
        //Hide keyboard after selecting
        binding.actCountryPhoneCode.setOnClickListener {
            hideKeyboard()
        }

        //Go to Signin page
        binding.tvSignIn.setOnDebouncedClickListener {
            finish()
        }

        val intent = Intent(this, TermsActivity::class.java)

        //Check with terms clicked and based on that show the url in next screen
        binding.tvTerm.makeLinks(
            Pair(getString(R.string.terms), View.OnClickListener {
                intent.putExtra(LOAD_URL, TermsConditionTypeEnum.TERMS_1.name)
                startActivity(intent)
            }),
            Pair(getString(R.string.data_policy), View.OnClickListener {
                intent.putExtra(LOAD_URL, TermsConditionTypeEnum.TERMS_2.name)
                startActivity(intent)
            }),
            Pair(getString(R.string.cookie_policy), View.OnClickListener {
                intent.putExtra(LOAD_URL, TermsConditionTypeEnum.TERMS_3.name)
                startActivity(intent)
            })
        )
    }
}
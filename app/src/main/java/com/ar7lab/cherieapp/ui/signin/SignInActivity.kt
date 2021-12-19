package com.ar7lab.cherieapp.ui.signin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.ar7lab.cherieapp.BuildConfig
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.databinding.ActivitySignInBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.ui.dashboard.DashboardActivity
import com.ar7lab.cherieapp.ui.forgotpassword.ForgotPasswordActivity
import com.ar7lab.cherieapp.ui.signup.CountryPhoneCodeAdapter
import com.ar7lab.cherieapp.ui.signup.SignUpActivity
import com.ar7lab.cherieapp.utils.CHANNEL_ID
import com.ar7lab.cherieapp.utils.FORGET_TYPE
import com.ar7lab.cherieapp.utils.getCountries
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.linecorp.linesdk.LineApiResponseCode
import com.linecorp.linesdk.Scope
import com.linecorp.linesdk.auth.LineAuthenticationParams
import com.linecorp.linesdk.auth.LineLoginApi
import com.linecorp.linesdk.auth.LineLoginApi.*
import com.linecorp.linesdk.auth.LineLoginResult
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class SignInActivity : BaseActivity() {

    private val binding: ActivitySignInBinding by viewBinding()
    private val viewModel: SignInViewModel by viewModels()

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    override fun isNeedWindowLightStatusBar() = true

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<SignInViewModel.ViewState> {
        binding.isLoggingIn = it.isLoggingIn
        binding.isGoogleLoggingIn = it.isGoogleLoggingIn
        binding.isFacebookLoggingIn = it.isFacebookLoggingIn
        binding.isKakaoLoggingIn = it.isKakaoLoggingIn
        binding.isLineLoggingIn = it.isLineLoggingIn
        (this as BaseActivity).setWindowLightStatus()

        if (it.isError) {
            it.message?.let { msg ->
                if (msg.isNotBlank())
                    showError(msg)
            }
        }

        /**
         * Check user has verified the email or not.
         */
        /*if (!it.isVerified) {
            startActivity(Intent(this, EmailVerificationActivity::class.java).putExtra("email", binding.etEmail.text.toString()))
        }*/

        /**
         * Check if user login the first time, navigate to the welcome screen
         */
        if (it.isLoggedIn) {
            val intent = Intent(this, DashboardActivity::class.java)
            /*if (it.isFirstTimeLogin) {
                intent = Intent(this, WelcomeUserActivity::class.java)
                intent.putExtras(WelcomeUserActivity.createBundle(it.user))
            }*/
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
        }
    }

    private var googleSignInResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                // do login with google with our server
                viewModel.loginWithGoogle(
                    account.id ?: "", account.email ?: "",
                    account.givenName ?: ""
                )
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Timber.w("Google sign in failed $e")
                showError("Google sign in failed $e")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.viewModel = viewModel

        observe(viewModel.stateLiveData, stateObserver)
        viewModel.init()

        //Set the title
        binding.include.tvTitle.text = getString(R.string.sign_in)
        binding.include.ivBack.visibility = View.INVISIBLE

        binding.tvForgotPassword.setOnDebouncedClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java).putExtra(FORGET_TYPE, viewModel.isSignInWithMobile.get()))
        }

        //get country list
        val countries = getCountries()
        //set adapter
        binding.actCountryPhoneCode.setAdapter(CountryPhoneCodeAdapter(this, countries))
        //country code item listener
        binding.actCountryPhoneCode.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                viewModel.setCountryPhoneCode(countries[position].phone) //set country phone code
            }


        //set drop down width
        binding.actCountryPhoneCode.dropDownWidth =
            (resources.displayMetrics.widthPixels - resources.getDimension(R.dimen.margin_normal) * 2).toInt()

        //Hide keyboard after selecting
        binding.actCountryPhoneCode.setOnClickListener {
            hideKeyboard()
        }
        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            binding.etEmail.setText("")
//            binding.etPassword.setText("")
        }

        binding.tvSignup.setOnDebouncedClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            resultLauncher.launch(intent)
        }

        binding.btnSignIn.setOnDebouncedClickListener {
            hideKeyboard()
            viewModel.login()
        }

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.DEFAULT_WEB_CLIENT_ID)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.btnGoogle.setOnDebouncedClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInResultLauncher.launch(signInIntent)
        }
        //kakao social login button clicked
        binding.btnKakao.setOnDebouncedClickListener {
            kakaoLogin()
        }


        //Line Login
        lineLogin()
        // Facebook sign in
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {
                    // get user's info from facebook
                    val request = GraphRequest.newMeRequest(
                        loginResult?.accessToken
                    ) { jsonObject, response ->
                        Timber.d("$jsonObject $response")
                        val facebookId = jsonObject?.getString("id") ?: ""
                        val facebookFirstName = jsonObject?.getString("first_name") ?: ""
                        val facebookEmail = jsonObject?.getString("email") ?: ""
                        // do login with facebook to our server
                        viewModel.loginWithFacebook(facebookId, facebookEmail, facebookFirstName)
                    }
                    val parameters = Bundle()
                    // put fields want to get from facebook
                    parameters.putString("fields", "id,name,first_name,link,email")
                    request.parameters = parameters
                    request.executeAsync()
                }

                override fun onCancel() {}

                override fun onError(exception: FacebookException?) {
                    Timber.e("loi $exception")
                    showError("Facebook login error.")
                }
            })

        binding.btnFacebook.setOnDebouncedClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"));
        }

    }

    //Line Login Method
    fun lineLogin() {
        //activity result
        var resultLauncherLine =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                //getting line result
                val lineLoginResult: LineLoginResult = LineLoginApi.getLoginResultFromIntent(result.data)
                //Email Address getting
                val email = lineLoginResult.lineIdToken?.email ?: ""
                when (lineLoginResult.responseCode) {
                    //If response code sucess
                    LineApiResponseCode.SUCCESS -> {
                        viewModel.loginWithLine(lineLoginResult.lineProfile?.userId ?: "", email, lineLoginResult.lineProfile?.displayName ?: "")

                    }
                    //If user click cancel
                    LineApiResponseCode.CANCEL -> {
                        showError(getString(R.string.line_login_canceled))
                    }
                    LineApiResponseCode.NETWORK_ERROR -> {
                        showError(getString(R.string.please_check_internet))
                    }
                    else -> {
                        showError(lineLoginResult.getErrorData().toString())
                    }
                }
            }
        //line button clicked listener
        binding.btnLine.setOnDebouncedClickListener {
            //start activity intent
            val loginIntent: Intent = getLoginIntent(
                this@SignInActivity,
                CHANNEL_ID,
                LineAuthenticationParams.Builder()
                    .scopes(Arrays.asList(Scope.PROFILE, Scope.OPENID_CONNECT, Scope.OC_EMAIL))
                    .build()
            )
            resultLauncherLine.launch(loginIntent)

        }
    }

    //kakao login
    fun kakaoLogin() {
        //kakao login call back
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            //check if get error
            if (error != null) {
                showError(error.message)
            } else if (token != null) {//if token generated
                //user detail getting
                UserApiClient.instance.me { user, error ->
                    if (error != null) {
                        showError(error.message)
                    } else if (user != null) {
                        //login request send
                        viewModel.loginWithKakao(user.id.toString() ?: "", user.kakaoAccount?.email ?: "", user.kakaoAccount?.profile?.nickname ?: "")
                    }
                }
            }
        }
        //check if kakao app install or not
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    //activity result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
package com.ar7lab.cherieapp.ui.welcome

import android.content.Intent
import android.os.Bundle
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.databinding.ActivityWelcomeBinding
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.ui.signup.SignUpActivity

class WelcomeActivity : BaseActivity() {
    private val binding: ActivityWelcomeBinding by viewBinding()

    override fun isNeedWindowLightStatusBar() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnSignUp.setOnDebouncedClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.tvSignIn.setOnDebouncedClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}
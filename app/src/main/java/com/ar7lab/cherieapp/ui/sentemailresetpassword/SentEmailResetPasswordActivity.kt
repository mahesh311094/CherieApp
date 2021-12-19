package com.ar7lab.cherieapp.ui.sentemailresetpassword

import android.content.Intent
import android.os.Bundle
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.databinding.ActivitySentEmailResetPasswordBinding
import com.ar7lab.cherieapp.ui.forgotpassword.ForgotPasswordActivity
import android.content.pm.PackageManager
import timber.log.Timber


class SentEmailResetPasswordActivity : BaseActivity() {

    private val binding: ActivitySentEmailResetPasswordBinding by viewBinding()

    override fun isNeedWindowLightStatusBar() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        binding.tvSkip.setOnDebouncedClickListener {
            finish()
        }

        binding.tvTryAgain.setOnDebouncedClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.btnOpenMail.setOnDebouncedClickListener {

            // Use package name which we want to check
            val isAppInstalled = appInstalledOrNot()

            if (isAppInstalled) {
                //This intent will help you to launch if the package is already installed
                startActivity(packageManager.getLaunchIntentForPackage("com.google.android.gm"))
            } else {
                showError("Gmail is not installed on your device")
            }
        }
    }

    private fun appInstalledOrNot(): Boolean {
        val pm = packageManager
        try {
            pm.getPackageInfo("com.google.android.gm", PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e("appInstalledOrNot ${e.message}")
        }
        return false
    }

    override fun onBackPressed() {}
}
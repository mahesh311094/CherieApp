package com.ar7lab.cherieapp.ui.splash

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.ui.chooselanguage.InitialLanguageActivity
import com.ar7lab.cherieapp.ui.dashboard.DashboardActivity
import com.ar7lab.cherieapp.ui.onboardingscreen.OnboardingActivity
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject
import android.util.DisplayMetrics
import java.util.*


@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //throw RuntimeException("Test Crash")
        // If user is not logged in yet, open welcome screen to login or signup, otherwise open dashboard screen
        // change the locale of the app based on the selection
        changeLocale(sharePreferencesManager.language)
        when {
            sharePreferencesManager .isLoggedIn -> {
                startActivity(Intent(this, DashboardActivity::class.java))
            }
            sharePreferencesManager.introComplete -> {
                startActivity(Intent(this, SignInActivity::class.java))
            }
            sharePreferencesManager.initialLanguage -> {
                startActivity(Intent(this, OnboardingActivity::class.java))
            }
            else -> {
                startActivity(Intent(this, InitialLanguageActivity::class.java))
            }
        }
    }

    // change the locale of the app on language change
    private fun changeLocale(language: String) {
        val myLocale = Locale(language)
        val res: Resources = resources
        val dm: DisplayMetrics = res.displayMetrics
        val conf: Configuration = res.configuration
        conf.setLocale(myLocale)
        res.updateConfiguration(conf, dm)
        onConfigurationChanged(conf)
    }
}
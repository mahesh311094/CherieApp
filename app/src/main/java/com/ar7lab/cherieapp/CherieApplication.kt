package com.ar7lab.cherieapp

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class CherieApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initTimber()
        // Initialize Android SDK
        KakaoSdk.init(this, "8b23369bde00f648e979004734fb4e6e")
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
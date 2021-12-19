package com.ar7lab.cherieapp.di

import android.content.Context
import android.content.SharedPreferences
import com.ar7lab.cherieapp.enums.AccountTypeEnum
import com.ar7lab.cherieapp.enums.ChooseCurrencyEnum
import com.ar7lab.cherieapp.enums.ChooseLanguageEnum
import com.ar7lab.cherieapp.enums.SocialMediaTypeEnum

/**
 * This class will help saving data into share preferences
 */
class SharePreferencesManager constructor(context: Context) {

    companion object {
        private const val SHARED_PREF_NAME = "cherie_pref"
        private const val TOKEN = "token"
        private const val USER_ID = "userId"
        private const val USER_NAME = "userName"
        private const val LANGUAGE = "language"
        private const val CURRENCY = "currency"
        private const val DEVICE_TOKEN = "deviceToken"
        private const val FIRST_NAME = "firstName"
        private const val LAST_NAME = "lastName"
        private const val USER_TYPE = "user_type"
        private const val COMPANY_NAME = "companyName"
        private const val PROFILE_IMAGE = "profile_image"
        private const val INTRO_COMPLETE = "intro_complete"
        private const val SIGN_IN_TYPE = "sign_in_type"
        private const val INITIAL_LANGUAGE = "initial_language"
        private const val IS_KYC_COMPLETED = "isKYCCompleted"
    }

    private val sharedPreferences by lazy(LazyThreadSafetyMode.NONE) {
        context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
    }

    private inline fun SharedPreferences.put(body: SharedPreferences.Editor.() -> Unit) {
        val editor = edit()
        editor.body()
        editor.apply()
    }

    fun clearData() {
        val editor = sharedPreferences.edit()
        editor.remove(TOKEN).apply()
    }

    var token: String?
        get() = sharedPreferences.getString(TOKEN, "")
        set(value) = sharedPreferences.put { putString(TOKEN, value) }

    val isLoggedIn: Boolean
        get() = !token.isNullOrBlank()

    //for user id
    var userId: String
        get() = sharedPreferences.getString(USER_ID, "") ?: ""
        set(value) = sharedPreferences.put { putString(USER_ID, value) }

    //for user name
    var userName: String
        get() = sharedPreferences.getString(USER_NAME, "") ?: ""
        set(value) = sharedPreferences.put { putString(USER_NAME, value) }

    //for first name
    var firstName: String
        get() = sharedPreferences.getString(FIRST_NAME, "") ?: ""
        set(value) = sharedPreferences.put { putString(FIRST_NAME, value) }

    //for last name
    var lastName: String
        get() = sharedPreferences.getString(LAST_NAME, "") ?: ""
        set(value) = sharedPreferences.put { putString(LAST_NAME, value) }

    //for account type
    var userType: String
        get() = sharedPreferences.getString(USER_TYPE, "") ?: ""
        set(value) = sharedPreferences.put { putString(USER_TYPE, value) }

    //for company name
    var companyName: String
        get() = sharedPreferences.getString(COMPANY_NAME, "") ?: ""
        set(value) = sharedPreferences.put { putString(COMPANY_NAME, value) }

    //for image profile
    var profileImage: String
        get() = sharedPreferences.getString(PROFILE_IMAGE, "") ?: ""
        set(value) = sharedPreferences.put { putString(PROFILE_IMAGE, value) }

    //for language settings
    var language: String
        get() = sharedPreferences.getString(LANGUAGE, ChooseLanguageEnum.ENGLISH.locale)
            ?: ChooseLanguageEnum.ENGLISH.locale
        set(value) = sharedPreferences.put { putString(LANGUAGE, value) }

    //for currecy settings
    var currency: String
        get() = sharedPreferences.getString(CURRENCY, ChooseCurrencyEnum.AMERICAN_DOLLAR.sign)
            ?: ChooseCurrencyEnum.JAPANESE_YEN.sign
        set(value) = sharedPreferences.put { putString(CURRENCY, value) }

    var deviceToken: String
        get() = sharedPreferences.getString(DEVICE_TOKEN, "") ?: ""
        set(value) = sharedPreferences.put { putString(DEVICE_TOKEN, value) }

    //for check intro complete or not
    var introComplete: Boolean
        get() = sharedPreferences.getBoolean(INTRO_COMPLETE, false) ?: false
        set(value) = sharedPreferences.put { putBoolean(INTRO_COMPLETE, value) }

    //login type
    var loginType: String
        get() = sharedPreferences.getString(SIGN_IN_TYPE, SocialMediaTypeEnum.NONE.name)
            ?: SocialMediaTypeEnum.NONE.name
        set(value) = sharedPreferences.put { putString(SIGN_IN_TYPE, value) }

    //for check initital language or not
    var initialLanguage: Boolean
        get() = sharedPreferences.getBoolean(INITIAL_LANGUAGE, false) ?: false
        set(value) = sharedPreferences.put { putBoolean(INITIAL_LANGUAGE, value) }

    //for check intro complete or not
    var isKYCCompleted: Boolean
        get() = sharedPreferences.getBoolean(IS_KYC_COMPLETED, false) ?: false
        set(value) = sharedPreferences.put { putBoolean(IS_KYC_COMPLETED, value) }
}
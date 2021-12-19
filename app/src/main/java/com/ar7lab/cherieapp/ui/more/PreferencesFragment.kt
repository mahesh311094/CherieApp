package com.ar7lab.cherieapp.ui.more

import android.os.Bundle
import android.view.View
import com.airbnb.epoxy.Carousel
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.base.navigation.NavManager
import com.ar7lab.cherieapp.databinding.FragmentPreferencesBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.SocialMediaTypeEnum
import com.ar7lab.cherieapp.itemBottomSpacer
import com.ar7lab.cherieapp.itemPreferenceHeader
import com.ar7lab.cherieapp.itemPreferences
import com.ar7lab.cherieapp.model.BankDetails
import com.ar7lab.cherieapp.model.Preferences
import com.ar7lab.cherieapp.ui.wallet.WalletFragmentDirections
import com.ar7lab.cherieapp.utils.WALLETORCHECKOUT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PreferencesFragment : BaseFragment(R.layout.fragment_preferences) {

    private val binding: FragmentPreferencesBinding by viewBinding()
    private var preferences: ArrayList<Preferences> = arrayListOf()

    @Inject
    lateinit var navManager: NavManager

    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    override fun isNeedWindowLightStatusBar(): Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferences.clear()
        //change password only show login without social media
        if (sharePreferencesManager.loginType.equals(SocialMediaTypeEnum.NONE.name)) {
            preferences.add(
                Preferences(
                    getString(R.string.change_password),
                    getString(R.string.change_password_description),
                    R.drawable.ic_more_lock
                )
            )
        }
        preferences.add(
            Preferences(
                getString(R.string.notification_setting),
                getString(R.string.notification_setting_description),
                R.drawable.ic_more_notification
            )
        )
        preferences.add(
            Preferences(
                getString(R.string.help_and_support),
                getString(R.string.help_and_support_description),
                R.drawable.ic_more_help_support
            )
        )
        preferences.add(
            Preferences(
                getString(R.string.currency_setting),
                getString(R.string.currency_setting_description),
                R.drawable.ic_more_currency_settings
            )
        )
        preferences.add(
            Preferences(
                getString(R.string.display_language_setting),
                getString(R.string.display_language_setting_description),
                R.drawable.ic_more_display_settings
            )
        )

        preferences.add(
            Preferences(
                getString(R.string.pref_bank_details),
                getString(R.string.pref_bank_details_description),
                R.drawable.ic_bank_detail
            )
        )
        //Notification clicked
        binding.ivNotification.setOnDebouncedClickListener {
            val action =
                PreferencesFragmentDirections
                    .actionPreferencesToNotification()
            navManager.navigate(action)
        }

        createEpoxyRecyclerView()
    }

    /**
     * Show list of views with epoxy recyclerview
     * @return the list of recyclerview items
     * */
    private fun createEpoxyRecyclerView() {
        binding.rvItems.withModels {
            itemPreferenceHeader {
                id("item_preferences_header")
            }

            preferences.forEachIndexed { index, preferences ->
                itemPreferences {
                    id("item_preferences")
                    preferences(preferences)
                    listener(object : PreferencesItemListener {
                        override fun preferenceItemClick() {
                            if (sharePreferencesManager.loginType.equals(SocialMediaTypeEnum.NONE.name)) {
                                when (index) {
                                    0 -> {
                                        val action =
                                            PreferencesFragmentDirections.actionPreferencesToChangePassword()
                                        navManager.navigate(action)
                                    }
                                    1 -> {
                                        val action =
                                            PreferencesFragmentDirections.actionPreferencesToNotificationSetting()
                                        navManager.navigate(action)
                                    }
                                    2 -> {
                                        val action =
                                            PreferencesFragmentDirections.actionPreferencesToHelpAndSupport()
                                        navManager.navigate(action)
                                    }
                                    3 -> {
                                        //On Currency Item Clicked
                                        val action =
                                            PreferencesFragmentDirections.actionPreferencesToChooseCurrency()
                                        navManager.navigate(action)
                                    }
                                    4 -> {
                                        val action =
                                            PreferencesFragmentDirections.actionPreferencesToChooseLanguage()
                                        navManager.navigate(action)
                                    }
                                    5 -> {
                                        val isSetting=true
                                        val action =
                                            PreferencesFragmentDirections.actionPreferencesToBankDetail(isSetting,null, null,null, null, null, WALLETORCHECKOUT,
                                                BankDetails(),0f
                                            )
                                        navManager.navigate(action)
                                    }
                                }
                            } else {
                                when (index) {

                                    0 -> {
                                        val action =
                                            PreferencesFragmentDirections.actionPreferencesToNotificationSetting()
                                        navManager.navigate(action)
                                    }
                                    1 -> {
                                        val action =
                                            PreferencesFragmentDirections.actionPreferencesToHelpAndSupport()
                                        navManager.navigate(action)
                                    }
                                    2 -> {
                                        //On Currency Item Clicked
                                        val action =
                                            PreferencesFragmentDirections.actionPreferencesToChooseCurrency()
                                        navManager.navigate(action)
                                    }
                                    3 -> {
                                        val action =
                                            PreferencesFragmentDirections.actionPreferencesToChooseLanguage()
                                        navManager.navigate(action)
                                    }
                                    4 -> {
                                        val isSetting=true
                                        val action =
                                            PreferencesFragmentDirections.actionPreferencesToBankDetail(isSetting, null, null,null, null, null, WALLETORCHECKOUT,BankDetails(),0f)
                                        navManager.navigate(action)
                                    }
                                }
                            }
                        }

                    })
                }
            }

            itemBottomSpacer {
                id("spacing")
                spanSizeOverride { _, _, _ -> 2 }
            }
        }
    }
}
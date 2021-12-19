package com.ar7lab.cherieapp.ui.dashboard

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUiSaveStateControl
import androidx.navigation.ui.setupWithNavController
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.hide
import com.ar7lab.cherieapp.base.extension.navigateSafe
import com.ar7lab.cherieapp.base.extension.show
import com.ar7lab.cherieapp.base.navigation.NavManager
import com.ar7lab.cherieapp.databinding.ActivityDashboardBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.ui.chooselanguage.LanguageChangeListener
import com.ar7lab.cherieapp.ui.market.MarketFragment
import com.ar7lab.cherieapp.ui.news.NewsFragment
import com.ar7lab.cherieapp.ui.profile.ProfileFragment
import com.ar7lab.cherieapp.ui.store.StoreFragment
import com.ar7lab.cherieapp.ui.storesearch.StoreSearchFragment
import com.ar7lab.cherieapp.ui.wallet.WalletFragment
import com.ar7lab.cherieapp.utils.DYNAMIC_LINK_QUERY_NEWS
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import android.widget.SeekBar

import android.graphics.drawable.Drawable
import eightbitlab.com.blurview.RenderScriptBlur


@AndroidEntryPoint
class DashboardActivity : BaseActivity(), NotificationRedirectProfileListener,
    LanguageChangeListener {

    public val binding: ActivityDashboardBinding by viewBinding()
    private val navController get() = Navigation.findNavController(this, R.id.nav_host_fragment)

    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    @Inject
    lateinit var navManager: NavManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupNavigationBottomTab()
        initNavManager()
        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->

                // Get deep link from result (may be null if no link is found)
                if (pendingDynamicLinkData != null) {
                    val deepLink = pendingDynamicLinkData.link
                    deepLink?.let {
                        // navigate to tab news first
                        if (it.getQueryParameter(DYNAMIC_LINK_QUERY_NEWS) != null) {
                            navController.navigate(R.id.news)
                        }
                        navController.navigate(it)
                    }
                }
            }
            .addOnFailureListener(this) { e -> Timber.e("getDynamicLink:onFailure", e) }

        //This will set the view blur
        setupBlurView()
    }

    @NavigationUiSaveStateControl
    private fun setupNavigationBottomTab() {
        // Set up navigation with bottom tab
        binding.bottomNavigation.setupWithNavController(navController)

        // Fragment will reload when click on the bottom icon again
        binding.bottomNavigation.setOnItemReselectedListener {
//            if (it.itemId == R.id.store || it.itemId == R.id.market || it.itemId == R.id.wallet || it.itemId == R.id.news) {
            reloadStoreFragment()
//            }
        }

        // Reload the store fragment everytime when store item menu click in bottom navigation view
        binding.bottomNavigation.setOnItemSelectedListener {
            if (it.itemId == R.id.store) {
                // Pass data to store fragment
                navController.previousBackStackEntry?.savedStateHandle?.set("key", "reload")
            }

            // Pass controller to navigation component to handle addOnDestinationChangedListener callback
            NavigationUI.onNavDestinationSelected(it, navController)
        }
    }

    //Check which fragment need to reload
    private fun reloadStoreFragment() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        when (val currentFragment = navHostFragment?.childFragmentManager?.fragments?.first()) {
            is StoreFragment -> currentFragment.reload()
            is MarketFragment -> currentFragment.reload()
            is WalletFragment -> currentFragment.reload()
            is NewsFragment -> currentFragment.reload()
            is ProfileFragment -> currentFragment.reload()
            is StoreSearchFragment -> currentFragment.reload()
        }
    }

    private fun initNavManager() {
        navManager.setOnNavEvent {
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            (navHostFragment as NavHostFragment).navController.addOnDestinationChangedListener { controller, destination, arguments ->
                if (destination.id == R.id.notification ||
                    destination.id == R.id.change_password ||
                    destination.id == R.id.notification_setting ||
                    destination.id == R.id.help_and_support ||
                    destination.id == R.id.choose_currency ||
                    destination.id == R.id.choose_language ||
                    destination.id == R.id.traditional_artwork_details ||
                    destination.id == R.id.nft_artist_profile ||
                    destination.id == R.id.nft_artwork_details ||
                    destination.id == R.id.news_details ||
                    destination.id == R.id.edit_user_profile ||
                    destination.id == R.id.view_art_likes ||
                    destination.id == R.id.view_art_comments ||
                    destination.id == R.id.top_artists ||
                    destination.id == R.id.deposit ||
                    destination.id == R.id.deposit_second_step ||
                    destination.id == R.id.store_likes ||
                    destination.id == R.id.store_search ||
                    destination.id == R.id.storeonsale ||
                    destination.id == R.id.storeupcomingdeals ||
                    destination.id == R.id.storepastsale ||
                    destination.id == R.id.market_detail ||
                    destination.id == R.id.request_callback ||
                    destination.id == R.id.selectPaymentFragment

                ) {
                    binding.bottomNavigation.hide()
                    // remove margin for the fragment content

                    //This is commented because bottom sheet is not blur after get back from any other fragment
                    /*val params = binding.frame.layoutParams as ViewGroup.MarginLayoutParams
                    params.setMargins(0, 0, 0, 0)
                    binding.frame.layoutParams = params*/
                } else {
                    binding.bottomNavigation.show()
                    // add margin for the fragment content

                    //This is commented because bottom sheet is not blur after get back from any other fragment
                    /*val params = binding.frame.layoutParams as ViewGroup.MarginLayoutParams
                    params.setMargins(0, 0, 0, resources.getDimension(R.dimen.bottom_nav_height).toInt())
                    binding.frame.layoutParams = params*/
                }
            }

            val currentFragment = navHostFragment.childFragmentManager.fragments.first()
            currentFragment?.navigateSafe(it)
        }
    }

    override fun onNoficationRedirect() {
        binding.bottomNavigation.selectedItemId = R.id.profile
    }

    // language change listener interface on the language change by the user
    override fun onLanguageChange(language: String) {
        changeLocale(language)
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

        reloadBottomNavigationView()
    }

    // reload the bottom navigation view on the language change
    private fun reloadBottomNavigationView() {
        binding.bottomNavigation.menu.findItem(R.id.store).title = resources.getString(R.string.buy)
        binding.bottomNavigation.menu.findItem(R.id.market).title =
            resources.getString(R.string.trade)
        binding.bottomNavigation.menu.findItem(R.id.wallet).title =
            resources.getString(R.string.wallet)
        binding.bottomNavigation.menu.findItem(R.id.profile).title =
            resources.getString(R.string.my)
        binding.bottomNavigation.menu.findItem(R.id.preferences).title =
            resources.getString(R.string.more)
    }

    private fun setupBlurView() {
        val radius = 10f

        val decorView = window.decorView
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)

        //set background, if your root layout doesn't have one
        val windowBackground = window.decorView.background

        binding.bgBlur.setupWith(rootView)
            .setFrameClearDrawable(windowBackground)
            .setBlurAlgorithm(RenderScriptBlur(this))
            .setBlurRadius(radius)
            .setHasFixedTransformationMatrix(true)

    }
}
package com.ar7lab.cherieapp.ui.wallet

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.ar7lab.cherieapp.*
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.base.navigation.NavManager
import com.ar7lab.cherieapp.databinding.FragmentWalletBinding
import com.ar7lab.cherieapp.databinding.WalletInfoDialogBinding
import com.ar7lab.cherieapp.ui.dashboard.NotificationRedirectProfileListener
import com.ar7lab.cherieapp.ui.notification.NotificationFragment
import com.ar7lab.cherieapp.utils.REDIRECT
import com.ar7lab.cherieapp.utils.USER_PROFILE
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ClassCastException
import javax.inject.Inject
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.databinding.WalletSelectionDialogBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.model.WalletPurchaseDetail
import com.ar7lab.cherieapp.network.response.GetWalletBalanceResponse
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.utils.openInfoDialog

@AndroidEntryPoint
class WalletFragment : BaseFragment(R.layout.fragment_wallet) {

    private val binding: FragmentWalletBinding by viewBinding()
    private val viewModel: WalletViewModel by viewModels()

    @Inject
    lateinit var navManager: NavManager
    //User balance variable
    private var userBalance: GetWalletBalanceResponse.Assets? = null
    //Wallet purchase list
    private var walletPurchaseDetais: ArrayList<WalletPurchaseDetail> = arrayListOf()

    //Notification redirect listener
    lateinit var notificationReDirectProfileListener: NotificationRedirectProfileListener

    //share preference object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager
    override fun isNeedWindowLightStatusBar()=true
    private val stateObserver = Observer<WalletViewModel.ViewState> {

        //show progressbar
        binding.isRefreshing = it.isRefreshed
        //user balance getting from model
        it.userBalance?.let {
            userBalance = it
        }
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

        it.walletActivities?.let {
            walletPurchaseDetais = it.map { walletPurchaseDetail -> walletPurchaseDetail.copy() } as ArrayList
        }
        //refresh recyclerview
        binding.rvWallet.requestModelBuild()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as BaseActivity).clearWindowLightStatus()
        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)
        createEpoxyRecyclerView()

        addClickEvent()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            notificationReDirectProfileListener = activity as NotificationRedirectProfileListener
        } catch (e: ClassCastException) {
           // throw ClassCastException(activity.toString() + " must implement onSomeEventListener")
        }
    }

    private fun addClickEvent() {
        //for fragment result redirection
        setFragmentResultListener(NotificationFragment.REQUEST_KEY) { key, bundle ->
            if (bundle.getString(REDIRECT)!!.equals(USER_PROFILE)) {
                notificationReDirectProfileListener.onNoficationRedirect()
            }
            else
            {
                viewModel.onRefresh()
            }
        }
        //Notification clicked
        binding.ivNotification.setOnDebouncedClickListener {
            val action =
                WalletFragmentDirections.actionWalletToNotification()
            navManager.navigate(action)
        }
    }

    //info icon click open dialog
    private fun openInfoDialog() {
        val bind: WalletInfoDialogBinding = WalletInfoDialogBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(context)
        builder.setView(bind.root)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        bind.ivClose.setOnClickListener {
            alertDialog.dismiss()
        }
    }


    //info icon click open dialog
    private fun openSelectionDialog() {
        val bind: WalletSelectionDialogBinding = WalletSelectionDialogBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(context)
        builder.setView(bind.root)
        bind.viewModel=viewModel
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.window?.setGravity(Gravity.BOTTOM);
        alertDialog.show()
        bind.ivClose.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    //epoxy layout creation
    private fun createEpoxyRecyclerView() {

        binding.rvWallet.withModels {
            //Wallet Total value layout
            itemWalletTotalValue {
                id("total_value")
                totalbalance(viewModel.getUserTotalBalance().toString())

            }

            //WithDraw Receive Button Layout
            itemWalletWithDrawReceiveButton {
                id("with_draw_receive_button")
                    .walletListener(object : WalletListener {
                        override fun withdrawButtonClicked() {

                            //val action = WalletFragmentDirections.actionWalletToBankDetail(false)
                           // navManager.navigate(action)
                        }

                        override fun depositButtonClicked() {

                            val action = WalletFragmentDirections.actionWalletToDeposit()
                            navManager.navigate(action)
                        }
                    })
            }
            //table header
            itemWalletInnerTabDataHeader {
                id("data header")
            }

            walletPurchaseDetais.forEachIndexed { index, walletPuchaseDetail ->
                itemWalletDataItem{
                    id("static-data"+index)
                    walletPurchaseDetail(walletPuchaseDetail)
                }
            }
            if(walletPurchaseDetais.size>=10) {
                itemWalletPageNoFooter {
                    id("footer")
                }
            }

            itemBottomSpacer {
                id("spacing")
                spanSizeOverride { _, _, _ -> 2 }
            }

        }

       /* // Add scroll event to load more data when reach the bottom
        binding.rvWallet.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!binding.rvWallet.canScrollVertically(1)) {
                    if(walletPurchaseDetais?.size>0)
                        viewModel.loadMore()
                }
            }
        })*/
    }




    //For reload the fragment when click on bottom icon
    fun reload() {
        binding.rvWallet.smoothScrollToPosition(0)
        viewModel.onRefresh()
    }
    companion object{
        val REQUEST_KEY = "REQUEST_KEY"
    }

}
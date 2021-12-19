package com.ar7lab.cherieapp.ui.notification

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ar7lab.cherieapp.*
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.databinding.FragmentNotificationBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.model.Notifications
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.utils.*
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class NotificationFragment : BaseFragment(R.layout.fragment_notification) {
    //databinding object
    private val binding: FragmentNotificationBinding by viewBinding()

    //notification model
    private val viewModel: NotificationViewModel by viewModels()

    //Notification List
    private var notificatinList: ArrayList<Notifications> = arrayListOf()

    private var todayCheck: Boolean = true
    private var yesterdayCheck: Boolean = true
    private var firstTime: Boolean = true
    private var notificationTitleDate: String = ""
    private var tempDate: Date? = null

    //share preference object
    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    //StateObserver
    private val stateObserver = Observer<NotificationViewModel.ViewState> { state ->

        binding.isRefreshing = state.isRefreshing
        if (state.isError) {
            showError(state.message)
        }
        if (state.isSessionExpired) {
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
        if (state.isMarkAllReadSuccess) {
            state.message?.let { msg -> showError(msg) }
        }
        // copy data from viewmodel to prevent crash epoxy recyclerview
        state.notificationList?.let { notifications ->
            initVars()
            notificatinList = notifications.map { it.copy() } as ArrayList
        }
        binding.rvItems.requestModelBuild()
    }


    companion object {
        val REQUEST_KEY = "REQUEST_KEY"
    }

    //StatusBar color
    override fun isNeedWindowLightStatusBar(): Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe(viewModel.stateLiveData, stateObserver)
        binding.viewModel = viewModel
        binding.ivBack.setOnDebouncedClickListener {
            findNavController().popBackStack()
        }
        viewModel.init()
        createEpoxyRecyclerView()

        binding.ivMoreMenu.setOnClickListener {
            viewModel.notificationMarkAllAsRead()
        }
    }

    /**
     * Show list of views with epoxy recyclerview
     * @return the list of recyclerview items
     * */
    private fun createEpoxyRecyclerView() {
        //Epoxy binding
        binding.rvItems.withModels {
            notificatinList.forEachIndexed { index, notifications ->

                if (isTitleEnable(notifications)) {
                    itemNotificationTitle {

                        date(notificationTitleDate)

                        id("item_notification_new$index")
                        notification(notifications)

                        listner(object : NotificationItemClickedListener {
                            override fun onNotificationItemClicked(notification: Notifications) {
                                if (notification.type.equals("1") || notification.type.equals("2") || notification.type.equals("3")) {
                                    setFragmentResult(REQUEST_KEY, bundleOf(REDIRECT to USER_PROFILE))

                                    findNavController().navigateUp()
                                }

                                //2021-11-25T12:58:58.728Z
                            }
                        })
                        /*
                        Epoxy RecyclerView in xml have spanCount = 2,
                        so set this item spanSize to 1 to have a grid view
                        */
                        spanSizeOverride { _, _, _ -> 2 }

                    }
                } else {
                    itemNotificationNew {
                        id("item_notification_new$index")
                        notification(notifications)

                        listner(object : NotificationItemClickedListener {
                            override fun onNotificationItemClicked(notification: Notifications) {
                                if (notification.type.equals("1") || notification.type.equals("2") || notification.type.equals("3")) {
                                    setFragmentResult(REQUEST_KEY, bundleOf(REDIRECT to USER_PROFILE))

                                    findNavController().navigateUp()
                                }
                            }
                        })
                        /*
                        Epoxy RecyclerView in xml have spanCount = 2,
                        so set this item spanSize to 1 to have a grid view
                        */
                        spanSizeOverride { _, _, _ -> 2 }

                    }
                }

            }
        }
        // Add scroll event to load more data when reach the bottom
        binding.rvItems.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!binding.rvItems.canScrollVertically(1)) {
                    viewModel.loadMore()
                }
            }
        })
    }

    //initialize variables after refresh
    private fun initVars() {
        todayCheck = true
        yesterdayCheck = true
        firstTime = true
        notificationTitleDate = ""
        tempDate = null
    }

    //check for today, yesterday or any date of notification
    private fun isTitleEnable(notification: Notifications): Boolean {

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -1)
        val yesterday: Date = calendar.time

        val currentDate: String = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
        val yesterdayDate: String = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(yesterday)
        val formatter = SimpleDateFormat(DATE_FORMAT)

        val notificationDate = formatter.parse(notification.createdAt)
        val todayDate = formatter.parse(currentDate)
        val dayBeforeDate = formatter.parse(yesterdayDate)

        if (notificationDate != dayBeforeDate && tempDate == null) {
            tempDate = notificationDate
        }

        if (notificationDate == todayDate && todayCheck) {
            notificationTitleDate = requireContext().getString(R.string.today)
            todayCheck = false

        } else if (notificationDate == dayBeforeDate && yesterdayCheck) {
            yesterdayCheck = false
            tempDate = notificationDate

            notificationTitleDate = requireContext().getString(R.string.yesterday)

        }else if(notificationDate <= tempDate && firstTime){

            val formatterWithMonth = SimpleDateFormat(DATE_FORMAT_WITH_MONTH)
            val date = formatterWithMonth.format(notificationDate.time)

            firstTime = false

            notificationTitleDate = date
            tempDate = notificationDate

        } else if (notificationDate < tempDate && notificationDate != tempDate) {
            val formatterWithMonth = SimpleDateFormat(DATE_FORMAT_WITH_MONTH)
            val date = formatterWithMonth.format(notificationDate.time)

            notificationTitleDate = date
            tempDate = notificationDate

        } else {
            return false
        }
        return true
    }

}
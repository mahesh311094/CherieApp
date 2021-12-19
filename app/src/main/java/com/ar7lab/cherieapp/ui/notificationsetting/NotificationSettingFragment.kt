package com.ar7lab.cherieapp.ui.notificationsetting

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.databinding.FragmentNotificationSettingBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.model.User
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.utils.openInfoDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationSettingFragment : BaseFragment(R.layout.fragment_notification_setting) {

    private val binding: FragmentNotificationSettingBinding by viewBinding()
    private val viewModel: NotificationSettingViewModel by viewModels()
    private var user: User? = null
    private var isNotice: Boolean = false
    private var isNews: Boolean = false
    private var isWork: Boolean = false
    private var isLikeFollowComment: Boolean = false
    private var isAssetChange: Boolean = false

    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    private val stateObserver = Observer<NotificationSettingViewModel.ViewState> { state ->
        if (state.isEditNotificationSuccess) {
            state.message?.let { msg -> showError(msg) }
        }
        if (state.isError) {
            state.message?.let { msg -> showError(msg) }

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
        state.user?.let {
            user = it
            binding.switchNoticeNotificationOnOff.isChecked =
                user?.settings?.notification!!.isNotice
            binding.switchNewsNotificationOnOff.isChecked = user?.settings?.notification!!.isNews
            binding.switchWorkNotificationOnOff.isChecked = user?.settings?.notification!!.isWork
            binding.switchLikeFollowCommentNotificationOnOff.isChecked =
                user?.settings?.notification!!.isLikeFollowComment
            binding.switchAssetNotificationOnOff.isChecked =
                user?.settings?.notification!!.isAssetChange
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as BaseActivity).clearWindowLightStatus()
        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)
        viewModel.init()
        binding.ivBack.setOnDebouncedClickListener {
            findNavController().popBackStack()
        }

        binding.switchNoticeNotificationOnOff.setOnCheckedChangeListener { _, isChecked ->
            isNotice = isChecked
        }
        binding.switchNewsNotificationOnOff.setOnCheckedChangeListener { _, isChecked ->
            isNews = isChecked
        }
        binding.switchWorkNotificationOnOff.setOnCheckedChangeListener { _, isChecked ->
            isWork = isChecked
        }
        binding.switchLikeFollowCommentNotificationOnOff.setOnCheckedChangeListener { _, isChecked ->
            isLikeFollowComment = isChecked
        }
        binding.switchAssetNotificationOnOff.setOnCheckedChangeListener { _, isChecked ->
            isAssetChange = isChecked
        }

        binding.btnSave.setOnDebouncedClickListener {
            viewModel.updateNotificationSettings(
                isNotice,
                isNews,
                isWork,
                isLikeFollowComment,
                isAssetChange
            )
        }
    }
}

package com.ar7lab.cherieapp.ui.kycinfo

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.audiofx.BassBoost
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.databinding.Observable
import androidx.lifecycle.Observer
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.databinding.ActivityKycInfoBinding
import com.ar7lab.cherieapp.databinding.ContentKycDialogLayoutBinding
import com.ar7lab.cherieapp.databinding.InfoDialogWithOkayButtonBinding
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.ui.signup.CountryCodeAdapter
import com.ar7lab.cherieapp.utils.alertDialog
import com.ar7lab.cherieapp.utils.getCountries
import com.ar7lab.cherieapp.utils.openInfoDialog
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import androidx.core.app.ActivityCompat.startActivityForResult

import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.enums.KYCStep
import com.ar7lab.cherieapp.utils.VERIFICATION_START_OR_NOT
import com.ar7lab.cherieapp.utils.FileUtil
import retrofit2.http.Url
import java.io.File

@AndroidEntryPoint
class KYCInfoActivity : BaseActivity() {

    private val binding: ActivityKycInfoBinding by viewBinding()
    private val viewModel: KYVInfoViewModel by viewModels()

    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    private var mCurrentCode = 0
    private var mFileUrl: Uri? = null

    override fun isNeedWindowLightStatusBar() = true

    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<KYVInfoViewModel.ViewState> {
        (this as BaseActivity).setWindowLightStatus()

        if (it.isError) {
            it.message?.let { msg ->
                if (msg.isNotBlank())
                    showError(msg)
            }
        }
        if(it.isPersonalKYCSuccess)
        {
            showError(it.message)
        }
        if(it.isIdentityUploaded)
        {
            showError(it.message)
        }
        if(it.faceUploaded)
        {
            showError(it.message)
        }

    }

    override fun onResume() {
        super.onResume()
        if(VERIFICATION_START_OR_NOT == "START"){
            viewModel.startVerification()
            VERIFICATION_START_OR_NOT = "NOT"
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.viewModel = viewModel

        observe(viewModel.stateLiveData, stateObserver)
        viewModel.init()

        //Set the title
        if (viewModel.currentStep.get() == KYCStep.STEP_1)
            binding.tvTitle.text = getString(R.string.personal_verification)
        else if (viewModel.currentStep.get() == KYCStep.STEP_2 || viewModel.currentStep.get() == KYCStep.STEP_3)
            binding.tvTitle.text = getString(R.string.identity_verification)
        else if (viewModel.currentStep.get() == KYCStep.STEP_4)
            binding.tvTitle.text = getString(R.string.facial_recognition)
        else
            binding.tvTitle.text = getString(R.string.personal_verification)

        //Country dropdown
        // get list countries to show on dropdown
        val countries = getCountries()
        binding.include1.actCountry.setAdapter(CountryCodeAdapter(this, countries))
        binding.include1.actCountry.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                binding.include1.actCountry.setText(countries[position].name)
                //viewModel.setCountry(countries[position].code3)
            }

        viewModel.motionProgress.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                binding.constraintLayout3.progress = viewModel.motionProgress.get()
            }
        })

        /*viewModel.onDobClick.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (viewModel.onDobClick.get()) {
                    openDatePickerDialog()
                }
            }
        })*/

        binding.include1.tvDateOfBirth.setOnClickListener {
            openDatePickerDialog()
        }

        viewModel.showDialog.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                if (viewModel.showDialog.get()) {
                    showThankDialog()
                }
            }
        })

        viewModel.onClickPhoto.observe(this) {
            if (it != -1) {
                val file = File(cacheDir, "image${System.currentTimeMillis()}")
                /*mFileUrl = FileProvider.getUriForFile(
                    this,
                    applicationContext.packageName + ".provider",
                    file
                )*/
                mCurrentCode = when (it) {
                    0 -> FRONT_CODE
                    1 -> BACK_CODE
                    2 -> SELFIE_CODE
                    else -> {
                        SELFIE_CODE
                    }
                }
                lifecycleScope.launchWhenStarted {
                    getTmpFileUri().let { uri ->
                        mFileUrl = uri
                        takePicture.launch(uri)//.launch(uri)
                    }
                }

                //takePicture.launch(mFileUrl)
            }
        }

    }

    private fun getTmpFileUri(): Uri {
        val tmpFile =
            File.createTempFile("tmp_image_file${System.currentTimeMillis()}", ".png",cacheDir).apply {
                createNewFile()
                deleteOnExit()
            }

        return FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            tmpFile
        )
    }

    val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                // The image was saved into the given Uri -> do something with it
                mFileUrl?.let {
                    FileUtil.getImagePathFromInputStreamUri(this, it)?.let { path ->
                        when (mCurrentCode) {
                            FRONT_CODE -> viewModel.frontPhoto.set(path)
                            BACK_CODE -> viewModel.backPhoto.set(path)
                            SELFIE_CODE -> viewModel.selfiePhoto.set(path)
                        }
                    }
                }


            }
        }

    private fun showThankDialog() {

        val bind: ContentKycDialogLayoutBinding =
            ContentKycDialogLayoutBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
        builder.setView(bind.root)
        val alertDialog = builder.create()
        bind.ivClose.setOnClickListener {
            alertDialog?.dismiss()
            finish()
        }
        bind.btnContinue.setOnClickListener {
            alertDialog?.dismiss()
            finish()

        }
        alertDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.show()

    }

    fun openDatePickerDialog() {
        Calendar.getInstance().apply {
            DatePickerDialog(
                this@KYCInfoActivity,
                { _, year, monthOfYear, dayOfMonth ->
                    val month=monthOfYear+1
                    viewModel.dateOfBirth.set("$month-$dayOfMonth-$year")
                },
                get(Calendar.YEAR), get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    companion object {
        private const val FRONT_CODE = 1001
        private const val BACK_CODE = 1002
        private const val SELFIE_CODE = 1003
    }
}
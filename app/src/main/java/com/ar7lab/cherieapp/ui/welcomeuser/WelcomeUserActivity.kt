package com.ar7lab.cherieapp.ui.welcomeuser

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.ar7lab.cherieapp.R
import com.ar7lab.cherieapp.base.activity.BaseActivity
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.databinding.ActivityWelcomeUserBinding
import com.ar7lab.cherieapp.model.User
import com.ar7lab.cherieapp.ui.dashboard.DashboardActivity
import com.ar7lab.cherieapp.ui.verifyotp.VerifyOtpActivity
import com.ar7lab.cherieapp.utils.FileUtil
import com.ar7lab.cherieapp.utils.getCountries
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File


@AndroidEntryPoint
class WelcomeUserActivity : BaseActivity() {

    companion object {
        private const val KEY_USER = "user"

        fun createBundle(user: User?): Bundle =
            Bundle().apply {
                putSerializable(KEY_USER, user)
            }
    }

    private val binding: ActivityWelcomeUserBinding by viewBinding()

    private val viewModel: WelcomeUserViewModel by viewModels()

    private var _user: User? = null
    var avatarImageUri: Uri? = null
    var coverImageUri: Uri? = null
    var coverImagePath: String? = null
    var avtarImagePath: String? = null

    override fun isNeedWindowLightStatusBar(): Boolean = true

    /**
     * Register activity result for pick from gallery
     */
    private val avatarFileChooserContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                binding.ivAvatar.setImageURI(it)
                binding.isAvatarSelected = true

                /**
                 * Have to use this to cover all cases of pick gallery
                 */
                FileUtil.getImagePathFromInputStreamUri(this, it)?.let { path ->
                    avtarImagePath=path
                }
            }
        }

    private val avatarTakePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                // The image was saved into the given Uri -> do something with it
                Timber.e("$avatarImageUri")
                binding.ivAvatar.setImageURI(avatarImageUri)
                binding.isAvatarSelected = true
                /**
                 * Have to use this to cover all cases of pick gallery
                 */
                avatarImageUri?.let {
                    FileUtil.getImagePathFromInputStreamUri(this, it)?.let { path ->
                        avtarImagePath=path
                    }
                }
            }
        }

    private val coverFileChooserContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                binding.ivCover.setImageURI(it)
                binding.isCoverSelected = true

                FileUtil.getImagePathFromInputStreamUri(this, it)?.let { path ->
                    coverImagePath=path
                }
            }
        }

    private val coverTakePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                // The image was saved into the given Uri -> do something with it
                Timber.e("$coverImageUri")
                binding.ivCover.setImageURI(coverImageUri)
                binding.isCoverSelected = true
                /**
                 * Have to use this to cover all cases of pick gallery
                 */
                coverImageUri?.let {
                    FileUtil.getImagePathFromInputStreamUri(this, it)?.let { path ->
                        //viewModel.updateCoverPicture(path)
                        coverImagePath=path
                    }
                }
            }
        }


    // Observe ViewModel's state to take action on UI
    private val stateObserver = Observer<WelcomeUserViewModel.ViewState> {
        binding.isLoading = it.isLoading
        if (it.isError) {
            showError(it.message)
        }
        //After signup success it will go to the SignInActivity page
        if (it.isUserSaved) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        observe(viewModel.stateLiveData, stateObserver)

        _user = intent?.extras?.getSerializable(KEY_USER) as User?
        _user?.let {
            viewModel.init(it)
        }

        binding.viewModel = viewModel

        binding.btnSkip.setOnDebouncedClickListener {
            startActivity(Intent(this, DashboardActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
        }

        binding.btnSave.setOnDebouncedClickListener {
            //validContactNumber(viewModel.contactNo.get().toString())
            avtarImagePath?.let {path->viewModel.updateProfilePicture(path)}
            coverImagePath?.let {path-> viewModel.updateCoverPicture(path) }
            viewModel.save()
        }

        //Set the title
        binding.include.tvTitle.text = getString(R.string.welcome_to_cherie)
        binding.include.ivBack.visibility = View.INVISIBLE

        //Country dropdown
        // get list countries to show on dropdown
        val countries = getCountries()
        countries.forEachIndexed { index, country ->
            if (country.code3 == _user?.country) {
                binding.actCountry.setText(country.name)
                viewModel.setCountry(country.code3)
            }
        }
        binding.actCountry.setAdapter(CountryCodeAdapter(this, countries))
        binding.actCountry.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                binding.actCountry.setText(countries[position].name)
                viewModel.setCountry(countries[position].code3)
            }

        //Phone number country code dropdown
        binding.actCountryPhoneCode.setAdapter(CountryPhoneCodeAdapter(this, countries))
        binding.actCountryPhoneCode.setText(viewModel.countryPhoneCode.get())
        binding.actCountryPhoneCode.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                viewModel.setCountryPhoneCode(countries[position].phone) //set country phone code
            }
        binding.actCountryPhoneCode.setText(viewModel.country.get())
        //set drop down width
        binding.actCountryPhoneCode.dropDownWidth =
            (resources.displayMetrics.widthPixels - resources.getDimension(R.dimen.margin_normal) * 2).toInt()

        //Hide keyboard after selecting
        binding.actCountry.setOnClickListener {
            hideKeyboard()
        }
        //Hide keyboard after selecting
        binding.actCountryPhoneCode.setOnClickListener {
            hideKeyboard()
        }

        binding.ivAvatar.setOnDebouncedClickListener {
            openDialogUploadProfilePicture()
        }

        binding.ivCover.setOnDebouncedClickListener {
            openDialogUploadCoverPicture()
        }
    }

    private fun validContactNumber() {
        val intent = Intent(this, VerifyOtpActivity::class.java)
        intent.putExtra(
            "updatedContactNumber",
            "${viewModel.countryPhoneCode.get()}${viewModel.contactNo.get()}"
        )
        startActivity(intent)
    }

    /**
     * show a dialog to select upload from gallery for take a photo with camera for profile
     */
    private fun openDialogUploadProfilePicture() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Upload from")
            .setItems(R.array.upload_photo_from,
                DialogInterface.OnClickListener { dialog, which ->
                    // The 'which' argument contains the index position
                    // of the selected item
                    if (which == 0) {
                        avatarFileChooserContract.launch("image/*")
                    } else {
                        lifecycleScope.launchWhenStarted {
                            getTmpFileUri().let { uri ->
                                avatarImageUri = uri
                                avatarTakePicture.launch(uri)
                            }
                        }
                    }
                })
        builder.create()
        builder.show()
    }

    /**
     * show a dialog to select upload from gallery for take a photo with camera for cover
     */
    private fun openDialogUploadCoverPicture() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Upload from")
            .setItems(R.array.upload_photo_from,
                DialogInterface.OnClickListener { dialog, which ->
                    // The 'which' argument contains the index position
                    // of the selected item
                    if (which == 0) {
                        coverFileChooserContract.launch("image/*")
                    } else {
                        lifecycleScope.launchWhenStarted {
                            getTmpFileUri().let { uri ->
                                coverImageUri = uri
                                coverTakePicture.launch(uri)
                            }
                        }
                    }
                })
        builder.create()
        builder.show()
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(
            applicationContext,
            "${applicationContext.packageName}.provider",
            tmpFile
        )
    }
}
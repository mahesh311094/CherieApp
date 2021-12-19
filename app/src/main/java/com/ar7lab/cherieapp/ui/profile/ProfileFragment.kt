package com.ar7lab.cherieapp.ui.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.ar7lab.cherieapp.*
import com.ar7lab.cherieapp.base.InfoDialogOkayButtonListener
import com.ar7lab.cherieapp.base.delegate.viewBinding
import com.ar7lab.cherieapp.base.extension.observe
import com.ar7lab.cherieapp.base.extension.setOnDebouncedClickListener
import com.ar7lab.cherieapp.base.fragment.BaseFragment
import com.ar7lab.cherieapp.base.navigation.NavManager
import com.ar7lab.cherieapp.databinding.*
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.DepositeTypeEnum
import com.ar7lab.cherieapp.enums.TabMyTabTypeEnum
import com.ar7lab.cherieapp.model.*
import com.ar7lab.cherieapp.ui.collectionnft.MyCollectionNFTActivity
import com.ar7lab.cherieapp.ui.kycinfo.KYCInfoActivity
import com.ar7lab.cherieapp.ui.payment.addnewcard.AddNewCardActivity
import com.ar7lab.cherieapp.ui.payment.cards_banks_dailogs.AddBankBottomSheetFragment
import com.ar7lab.cherieapp.ui.payment.cards_banks_dailogs.AddCardBottomSheetFragment
import com.ar7lab.cherieapp.ui.signin.SignInActivity
import com.ar7lab.cherieapp.ui.signup.CountryCodeAdapter
import com.ar7lab.cherieapp.ui.wallet.deposit.DepositListener
import com.ar7lab.cherieapp.ui.wallet.deposit.DepositViewModel
import com.ar7lab.cherieapp.utils.FileUtil
import com.ar7lab.cherieapp.utils.VERIFICATION_START_OR_NOT
import com.ar7lab.cherieapp.utils.getCountries
import com.ar7lab.cherieapp.utils.openInfoDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.kakao.sdk.user.UserApiClient
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : BaseFragment(R.layout.fragment_profile) {

    private val binding: FragmentProfileBinding by viewBinding()
    private val viewModel: ProfileViewModel by viewModels()

    //bank detail getting API
    private var bankDetails: ArrayList<BankDetails> = arrayListOf()

    private var user: User? = null
    private var isLoading: Boolean = false
    var avatarImageUri: Uri? = null
    var coverImageUri: Uri? = null


    //Store the cards in list
    private var cards: ArrayList<CardDetails> = arrayListOf()

    //Initialize Art object
    private var art: ArrayList<Art> = arrayListOf()

    lateinit var auth: FirebaseAuth

    /**
     * Register activity result for pick from gallery
     */
    private val avatarFileChooserContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            Timber.d("onActivityResult: uri $uri")
            /**
             * Have to use this to cover all cases of pick gallery
             */
            uri?.let {
                viewModel.setImageUriProfile(uri.toString(), true)
                FileUtil.getImagePathFromInputStreamUri(requireContext(), uri)?.let { path ->
                    viewModel.updateProfilePicture(path)
                }
            }
        }

    private val avatarTakePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                // The image was saved into the given Uri -> do something with it
                Timber.e("$avatarImageUri")
                viewModel.setImageUriProfile(avatarImageUri.toString(), true)

                /**
                 * Have to use this to cover all cases of pick gallery
                 */
                avatarImageUri?.let {
                    FileUtil.getImagePathFromInputStreamUri(requireContext(), it)?.let { path ->
                        viewModel.updateProfilePicture(path)
                    }
                }
            }
        }

    private val coverFileChooserContract =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            Timber.d("onActivityResult: uri $uri")

            uri?.let {
                viewModel.setImageUriCover(uri.toString(), true)
                FileUtil.getImagePathFromInputStreamUri(requireContext(), uri)?.let { path ->
                    viewModel.updateCoverPicture(path)
                }
            }
        }

    private val coverTakePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                // The image was saved into the given Uri -> do something with it
                Timber.e("$coverImageUri")
                viewModel.setImageUriCover(coverImageUri.toString(), true)

                /**
                 * Have to use this to cover all cases of pick gallery
                 */
                coverImageUri?.let {
                    FileUtil.getImagePathFromInputStreamUri(requireContext(), it)?.let { path ->
                        viewModel.updateCoverPicture(path)
                    }
                }
            }
        }

    @Inject
    lateinit var sharePreferencesManager: SharePreferencesManager

    private lateinit var googleSignInClient: GoogleSignInClient

    @Inject
    lateinit var navManager: NavManager

    private var tabSelected = TabMyTabTypeEnum.COLLECTION
    private var collections = arrayListOf<MyCollectionDetails>()

    private var mBottomSheetAddCardDialog: BottomSheetDialog? = null
    private var mAddCardBinding: BottomSheetAddCardBinding? = null

    private val stateObserver = Observer<ProfileViewModel.ViewState> { state ->
        binding.isRefreshing = state.isRefreshing
        if (state.isEditOverviewUserProfileSuccess && state.isHaveData) {
            state.message?.let { msg -> showError(msg) }
        }
        if (state.isError) {
            state.message?.let { msg -> showError(msg) }
        }
        if (state.isSessionExpired) {
            requireContext().openInfoDialog(
                layoutInflater,
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
        }
        //if bankdetails not null than get bank detail set in global variable
        state.bankDetails?.let { bankDetails ->
            this.bankDetails.clear()
            this.bankDetails = bankDetails.map { card -> card.copy() } as ArrayList<BankDetails>
        }
        // need to copy data to avoid crash with epoxy
        state.cards?.let { cardsDetails ->
            cards.clear()
            cards = cardsDetails.map { card -> card.copy() } as ArrayList<CardDetails>
        }
        //set state for load art data
        state.art?.let {
            art = it.map { art -> art.copy() } as ArrayList
        }

        state.myCollections?.let { collection ->
            collections = collection
        }

        state.isLoading.let {
            isLoading = it
        }
        tabSelected = state.tabSelected
        binding.rvItems.requestModelBuild()

        //Refresh the card list to show
        viewModel.isNeedRefresh.set(cards.isNotEmpty())
        viewModel.isNeedRefresh.set(bankDetails.isNotEmpty())
        viewModel.cardCount.set(true)
    }

    //state change observer
    /*private val stateObserver = Observer<ProfileViewModel.ViewState> {
        //loading time show refresh layout
        binding.isRefreshing = it.isLoading
        //if error true error message will show
        if (it.isError) {
            showError(it.message)

        }
        //session expired dilog show
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
        //if bankdetails not null than get bank detail set in global variable
        it.bankDetails?.let { bankDetails ->
            this.bankDetails.clear()
            this.bankDetails = bankDetails.map { card -> card.copy() } as ArrayList<BankDetails>
        }
        //if cardlist not emapty set data
        it.cards?.let { cardsDetails ->
            cards.clear()
            cards = cardsDetails.map { card -> card.copy() } as ArrayList<CardDetails>
        }
        //refresh layout
        binding.rvItems.requestModelBuild()
    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        observe(viewModel.stateLiveData, stateObserver)
        viewModel.init()

        auth = FirebaseAuth.getInstance()
        var currentUser = auth.currentUser

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.DEFAULT_WEB_CLIENT_ID)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        //Show notifications
        /*binding.ivNotification.setOnDebouncedClickListener {
            val action =
                ProfileFragmentDirections.actionProfileToNotification()
            navManager.navigate(action)
        }*/

        binding.tvSignOut.setOnDebouncedClickListener {
            logout()
        }


        user?.profileImage?.let { viewModel.setImageUriProfile(it, true) }

        //Country dropdown
        // get list countries to show on dropdown
//        val countries = requireContext().getCountries()
//        binding.actCountry.setAdapter(CountryCodeAdapter(this, countries))
//        binding.actCountry.onItemClickListener =
//            AdapterView.OnItemClickListener { parent, view, position, id ->
//                binding.actCountry.setText(countries[position].name)
//                viewModel.setCountry(countries[position].code3)
//            }

        binding.rvItems.withModels {
            itemProfileHeader {
                id("header")
                user(user)
                viewModel(viewModel)
                listener(object : ProfileHeaderListener {

                    override fun uploadProfilePic() {
                        openDialogUploadProfilePicture()
                    }
                })
            }

            // Show data for the tab collection
            if (tabSelected == TabMyTabTypeEnum.COLLECTION) {
                if (collections.isEmpty()) {
                    itemProfileCollectionEmpty {
                        id("collection empty")
                    }
                } else {
                    collections.forEachIndexed { index, col ->
                        itemProfileCollectionItem {
                            id("art $index")
                            collection(col)
                            listener(object : ProfileListener {
                                override fun onSaleClicked() {

                                }

                                override fun onOwnedClicked() {
                                    startActivity(Intent(requireContext(), MyCollectionNFTActivity::class.java).putExtra("collection",col))
                                }

                            })
                        }
                    }
                }
            }

            // Show data for the tab payment method
            /*if (tabSelected == TabMyTabTypeEnum.PAYMENT_METHOD) {
                if (cards.isEmpty()) {
                    itemProfilePaymentMethodAddCard {
                        id("add cards")
                        listener(object : ProfileCardListener {
                            override fun addPaymentMethod() {
                                showDialogAddCard()
                            }
                        })
                    }
                } else {
                    itemProfilePaymentMethodListCardTitle {
                        id("card title")
                        listener(object : ProfileCardListener {
                            override fun addPaymentMethod() {
                                showDialogAddCard()
                            }
                        })
                    }
                    cards.forEachIndexed { index, cardDetails ->
                        if (index == cards.lastIndex) {
                            itemProfilePaymentMethodListCardItemLast {
                                id("card last")
                                cardDetails(cardDetails)
                            }
                        } else {
                            itemProfilePaymentMethodListCardItem {
                                id("card $index")
                                cardDetails(cardDetails)
                            }
                        }
                    }
                }
            }
*/

            if (tabSelected == TabMyTabTypeEnum.PAYMENT_METHOD) {

                //if there are no data then show empty layout
                if (bankDetails.isNullOrEmpty()) {
                    itemMyRegisterBank {
                        id("empty_bank_tranfer")
                        viewModel(viewModel)
                        paymentListener(object : ProfilePaymentListener {
                            override fun onClicked() {
                                addBankAccountDialog()
                            }

                        })
                    }
                } else {
                    //bank transfer header
                    itemMyRegisterDataBankTransferHeader {
                        id("bank_transfer_header")
                        viewModel(viewModel)
                        paymentListener(object : ProfilePaymentListener {
                            override fun onClicked() {
                                addBankAccountDialog()
                            }

                        })

                    }
                    //bank list item inflate
                    bankDetails.forEachIndexed { index, bankDetail ->
                        //last item inflate
                        if (index == bankDetails.lastIndex) {
                            itemMyRegisterDataBankTransferListItemLast {
                                id("bank_transfer_item_last" + index)
                                bankDetail(bankDetail)
                                viewModel(viewModel)
                                indexValue(index)
                                isChecked(viewModel.isBankPositionSelected(index))
                                paymentListener(object : ProfilePaymentListener {
                                    override fun onClicked() {
                                        removeAccountAlertDialog(bankDetail, index)
                                    }

                                })
                            }
                        } else {
                            //inter item inflate
                            itemMyRegisterDataBankTransferListItem {
                                id("bank_transfer_item" + index)
                                bankDetail(bankDetail)
                                viewModel(viewModel)
                                indexValue(index)
                                isChecked(viewModel.isBankPositionSelected(index))
                                paymentListener(object : ProfilePaymentListener {
                                    override fun onClicked() {
                                        removeAccountAlertDialog(bankDetail, index)
                                    }

                                })
                            }
                        }
                    }
                }

                // payment type card selection time this part will execute
                if (cards.isNullOrEmpty()) {
                    //emtpy credit card layout
                    itemMyRegisterEmptyCreditCards {
                        id("empty_credit_card")
                        viewModel(viewModel)
                        paymentListener(object : ProfilePaymentListener {
                            override fun onClicked() {
                                creaditCardDialog()
                            }

                        })
                    }
                } else {

                    //Card Layout header
                    itemMySelectDataCardHeader {
                        id("header_data")
                        viewModel(viewModel)
                        paymentListener(object : ProfilePaymentListener {
                            override fun onClicked() {
                                creaditCardDialog()
                            }

                        })

                    }
                    //Card loop
                    cards.forEachIndexed { index, cardDetails ->
                        //last index inflate
                        if (index == cards.lastIndex) {
                            itemMySelectDataCardListItemLast {
                                id("last_item" + index)
                                cardDetails(cardDetails)
                                viewModel(viewModel)
                                indexValue(index)
                                isChecked(viewModel.isCardPositionSelected(index))
                                paymentListener(object : ProfilePaymentListener {
                                    override fun onClicked() {
                                        removeCardAlertDialog(cardDetails, index)
                                    }

                                })
                            }
                        } else {
                            //Inner item inflate
                            itemMySelectDataCardListItem {
                                id("inner_item" + index)
                                cardDetails(cardDetails)
                                viewModel(viewModel)
                                indexValue(index)
                                isChecked(viewModel.isCardPositionSelected(index))
                                paymentListener(object : ProfilePaymentListener {
                                    override fun onClicked() {
                                        removeCardAlertDialog(cardDetails, index)
                                    }

                                })
                            }
                        }
                    }
                }


                //if payment type bank selected
                if (viewModel.typeSelected.get() == DepositeTypeEnum.DEFAULT) {

                } else {

                }
            }


            // Show tab profile
            if (tabSelected == TabMyTabTypeEnum.PROFILE) {
                itemProfileProfile {
                    id("profile")
                    viewModel(viewModel)
                }
            }

            // Show tab VerificationScreen
            if (tabSelected == TabMyTabTypeEnum.VALIDATION) {

                if(sharePreferencesManager.isKYCCompleted){
                    itemVerifiedLayout {
                        id("verified_screen")
                    }
                }else{

                    itemVerificationLayout {
                    id("verification_screen")
                    paymentListener(object : ProfilePaymentListener {
                        override fun onClicked() {
                            VERIFICATION_START_OR_NOT = "START"
                            startActivity(Intent(requireContext(), KYCInfoActivity::class.java))
                        }

                    })
                }
                }
            }

            itemBottomSpacer {
                id("spacing")
                spanSizeOverride { _, _, _ -> 2 }
            }
        }

    }

    private fun showDialogAddCard() {
        if (mBottomSheetAddCardDialog == null) {
            mBottomSheetAddCardDialog = BottomSheetDialog(requireContext())
            mAddCardBinding = DataBindingUtil.inflate(
                LayoutInflater.from(requireContext()),
                R.layout.bottom_sheet_add_card, null, false
            )
            mBottomSheetAddCardDialog?.setContentView(mAddCardBinding!!.root)
            mBottomSheetAddCardDialog?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED

            mAddCardBinding?.ivClose?.setOnDebouncedClickListener {
                mBottomSheetAddCardDialog?.cancel()
            }

            mAddCardBinding?.addCard = viewModel.addCard

            mAddCardBinding?.btnSave?.setOnDebouncedClickListener {
                mBottomSheetAddCardDialog?.cancel()
                viewModel.addNewCard()
            }
        }
        mBottomSheetAddCardDialog?.show()
    }

    /**
     * Logout user out, clear all data
     */
    private fun logout() {
        // logout
        UserApiClient.instance.logout { error ->

        }

        sharePreferencesManager.clearData()

        googleSignInClient.signOut()

        auth.signOut()

        startActivity(
            Intent(requireContext(), SignInActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    /**
     * show a dialog to select upload from gallery for take a photo with camera for profile
     */
    private fun openDialogUploadProfilePicture() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Upload from")
            .setItems(R.array.upload_photo_from) { _, which ->
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
            }
        builder.create()
        builder.show()
    }

    /**
     * show a dialog to select upload from gallery for take a photo with camera for cover
     */
    private fun openDialogUploadCoverPicture() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Upload from")
            .setItems(R.array.upload_photo_from) { _, which ->
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
            }
        builder.create()
        builder.show()
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile =
            File.createTempFile("tmp_image_file", ".png", requireContext().cacheDir).apply {
                createNewFile()
                deleteOnExit()
            }

        return FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            tmpFile
        )
    }

    //For reload the fragment when click on bottom icon
    fun reload() {
        viewModel.onRefresh()
    }

    var resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            binding.rvItems.smoothScrollToPosition(0)
            viewModel.onRefresh()
        }
    }

    fun openAddCard() {
        val intent = Intent(requireContext(), AddNewCardActivity::class.java)
        resultLauncher.launch(intent)
    }

    //Open add creadit card dialog
    fun creaditCardDialog() {
        val modalBottomSheet = AddCardBottomSheetFragment()
        modalBottomSheet.show(childFragmentManager, AddCardBottomSheetFragment.TAG)
        modalBottomSheet.setFragmentResultListener(AddCardBottomSheetFragment.REQUEST_KEY) { key, bundle ->
            viewModel.onRefresh()
        }


    }

    //Open bank add dialog
    fun addBankAccountDialog() {
        val modalBottomSheet = AddBankBottomSheetFragment()
        modalBottomSheet.show(childFragmentManager, AddBankBottomSheetFragment.TAG)
        modalBottomSheet.setFragmentResultListener(AddBankBottomSheetFragment.REQUEST_KEY) { key, bundle ->
            viewModel.onRefresh()
        }
    }

    fun removeAccountAlertDialog(bankDetail: BankDetails, index: Int) {
        val bind: RemoveAccountAlertLayoutBinding = RemoveAccountAlertLayoutBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(bind.root)
        bind.bankDetail = bankDetail
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.window?.setGravity(Gravity.CENTER);
        alertDialog.show()
        bind.btnConfirm.setOnClickListener{
            viewModel.deleteBankDetails(index)
            alertDialog.dismiss()
        }
        bind.ivClose.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    fun removeCardAlertDialog(cardDetails: CardDetails, index: Int) {
        val bind: RemoveCardAlertLayoutBinding = RemoveCardAlertLayoutBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireContext())
        builder.setView(bind.root)
        bind.cardDetail = cardDetails
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.window?.setGravity(Gravity.CENTER);
        alertDialog.show()
        bind.btnConfirm.setOnClickListener{
            viewModel.deleteCardDetails(index)
            alertDialog.dismiss()
        }
        bind.ivClose.setOnClickListener {
            alertDialog.dismiss()
        }
    }
}
package com.ar7lab.cherieapp.ui.profile

import android.view.KeyEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.AccountTypeEnum
import com.ar7lab.cherieapp.enums.DepositeTypeEnum
import com.ar7lab.cherieapp.enums.TabMyTabTypeEnum
import com.ar7lab.cherieapp.model.*
import com.ar7lab.cherieapp.network.repositories.ArtRepository
import com.ar7lab.cherieapp.network.repositories.AuthRepository
import com.ar7lab.cherieapp.network.repositories.PaymentRepository
import com.ar7lab.cherieapp.network.repositories.UserRepository
import com.ar7lab.cherieapp.ui.wallet.deposit.DepositViewModel
import com.ar7lab.cherieapp.utils.ONSELL
import com.ar7lab.cherieapp.utils.OWNED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
internal class ProfileViewModel @Inject constructor(
    private val sharePreferencesManager: SharePreferencesManager,
    private val authRepository: AuthRepository,
    private val paymentRepository: PaymentRepository,
    private val artRepository: ArtRepository,
    private val userRepository: UserRepository
) : BaseViewModel<ProfileViewModel.ViewState, ProfileViewModel.Action>(
    ViewState()
) {
    var overView = ObservableField("")
    var imageUriProfile = ObservableField("")
    var imageUriCover = ObservableField("")
    var isAvatarSelected = ObservableField(false)
    var isCoverSelected = ObservableField(false)
    var isOverViewFocus = ObservableField(false)
    var isNeedRefresh = ObservableField(false)

    val typeSelected = ObservableField(DepositeTypeEnum.DEFAULT)

    var tabSelected = ObservableField(TabMyTabTypeEnum.COLLECTION)

    //bank transfer detail save
    private val _bankDetails = ArrayList<BankDetails>()
    private val _accounts = ArrayList<String>()

    //Store the cards in list
    private val _cards = ArrayList<CardDetails>()

    //Initialize Art object for OnSale
    private val _artNFTOnSale = ArrayList<Art>()

    //Initialize Art object for Owned
    private val _artNFTOwned = ArrayList<Art>()

    private var _page = 1

    //Set date load limit for each time
    private var _limit = 10

    //Show the total count if the sales and owned
    var salesCount = ObservableField(0)
    var ownedCount = ObservableField(0)

    //Get the total number of card count for the progress show and hide
    var cardCount = ObservableField(false)

    val addCard: AddCard = AddCard()

    var isCardValid = ObservableBoolean(false)
    var isCardExpiryValid = ObservableBoolean(false)
    var isCVCValid = ObservableBoolean(false)

    var firstName = ObservableField("")
    var lastName = ObservableField("")
    var country = ObservableField("")

    //selected card position
    val _selected_card_position = ObservableInt(-1)

    //selected bank position
    val _selected_bank_position = ObservableInt(-1)

    var email: String = ""
    var mobileNumber = ""

    fun init() {
        loadData()

        addCard.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
//                isCardValid.set(isValidCard(addCard.cardNumber))
//                isCardExpiryValid.set(isValidCardExpiry(addCard.cardExpMonthYear))
//                isCVCValid.set(isValidCVC(addCard.cardCVC))

                isCardValid.set(addCard.cardNumber.isNotBlank())
                isCardExpiryValid.set(addCard.cardExpMonthYear.isNotBlank())
                isCVCValid.set(addCard.cardCVC.isNotBlank())
            }
        })
    }

    //user type wise first name & company name getting from share pref
    fun getUserFirstLastNameOrCompanyName(): String {
        return if (sharePreferencesManager.userType == AccountTypeEnum.PERSONAL.name) {
            sharePreferencesManager.firstName + " " + sharePreferencesManager.lastName
        } else {
            sharePreferencesManager.companyName
        }
    }

    var onFocusChangeListener = OnFocusChangeListener { _, isFocused ->
        if (!isFocused) {
            isOverViewFocus.set(false)
        } else {
            isOverViewFocus.set(true)
        }

    }

    //Network call for add new card
    fun addNewCard() {
//        if (!isCardValid.get() || !isCardExpiryValid.get() || !isCVCValid.get()) {
        if (addCard.cardNumber.isBlank() || addCard.cardExpMonthYear.isBlank() || addCard.cardCVC.isBlank()) {
            sendAction(Action.LoadingFailure("Card is invalid", false))
            return
        }
        val monthYear: List<String> = addCard.cardExpMonthYear.split("/")
        viewModelScope.launch {
            try {
//                sendAction(AddNewCardViewModel.Action.StartAddCard)
                val result = paymentRepository.addCard(
                    addCard.cardNumber,
                    monthYear[0],
                    monthYear[1],
                    addCard.cardCVC
                )
//                sendAction(AddNewCardViewModel.Action.LoadingSuccess(result.message))
            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, false))
            }
        }
    }

    fun deleteBankDetails(index: Int){
        _accounts.add(_bankDetails[index].accountNumber)
        viewModelScope.launch {
            try {
                val result = paymentRepository.deleteBank(
                    _accounts
                )
                
                if(result.status == "success"){
                    _accounts.clear()
                    onRefresh()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _accounts.clear()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, false))
            }
        }
    }

    fun deleteCardDetails(index: Int){
        viewModelScope.launch {
            try {
                val result = paymentRepository.deleteCard(
                    _cards[index].id
                )

                if(result.status == "success"){
                    onRefresh()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, false))
            }
        }
    }

    fun setImageUriProfile(uri: String, _isAvatarSelected: Boolean) {
        imageUriProfile.set(uri)
        isAvatarSelected.set(_isAvatarSelected)
    }

    fun setImageUriCover(uri: String, _isCoverSelected: Boolean) {
        imageUriCover.set(uri)
        isCoverSelected.set(_isCoverSelected)
    }

    //select card radio button with method will called
    fun selectedCardPosition(position: Int) {
        _selected_card_position.set(position)
        _selected_bank_position.set(-1)
        sendAction(Action.LoadingCardSuccess(_cards))
    }

    //get Card position is selected
    fun isCardPositionSelected(position: Int): Boolean {
        return _selected_card_position.get() == position
    }

    //get bank position is selected
    fun isBankPositionSelected(position: Int): Boolean {
        return _selected_bank_position.get() == position
    }

    //on bank item radio button clicked save selected position
    fun selectedBankPosition(position: Int) {
        _selected_card_position.set(-1)
        _selected_bank_position.set(position)
        sendAction(Action.LoadingBankDetailSuccess(_bankDetails))
    }

    //load bank details api calling
    private suspend fun loadBankDetails() {
        val result = paymentRepository.getBanksDetailList()
        result?.data?.bankDetails?.let {
            _bankDetails.addAll(it)
        }
        sendAction(Action.LoadingBankDetailSuccess(_bankDetails))
    }

    //Network call for load cards api calling
    private suspend fun loadCards() {
        val response = paymentRepository.getCards()
        response.data?.cardDetails?.let {
            _cards.clear()
            _cards.addAll(it)
        }
        sendAction(Action.LoadingCardSuccess(_cards))

    }

   /* override fun onLoadData() {
        //load data from API
        viewModelScope.launch {
            try {
                sendAction(DepositViewModel.Action.LoadingStarting)
                loadBankDetails()
                loadCards()
            } catch (e: Exception) {
                val error = getErrorMessage(e)
                sendAction(DepositViewModel.Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }
    }*/

    override fun onLoadData() {
        viewModelScope.launch {
            try {
                sendAction(Action.LoadingStarting)

                //Get user profile
                getUserProfile()

                //GetUserArtList
                getUserArtList()

                //Get all the Bank registered
                loadBankDetails()

                //Get all cards list of the user
                loadCards()

                //Get artist profile
                getArtsByArtistId()


                loadNFTOwnedArts()
            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }
    }

    //Network call for load cards
    /*private suspend fun loadCards() {
        val response = paymentRepository.getCards()
        response.data?.cardDetails?.let {
            _cards.clear()
            _cards.addAll(it)
        }
        sendAction(Action.LoadingCardSuccess(_cards))

    }*/

    private suspend fun getUserProfile() {
        val response = authRepository.getUserProfileDetails()
        response.data?.user?.let { user ->
            imageUriProfile.set(user.profileImage)
            firstName.set(user.firstName)
            lastName.set(user.lastName)
            email = user.email?:""
            mobileNumber = user.contactNo?:""
            country.set(user.country)
        }

        sendAction(Action.LoadingSuccessUser(response.data?.user))
    }

    private suspend fun getUserArtList() {
        val response = userRepository.getMyCollection(_page,_limit)
        response.data?.let { collectionData ->
            sendAction(Action.LoadingUserCollectionsSuccess(collectionData.assets as ArrayList<MyCollectionDetails>))
        }

    }

    fun resetUserDetail() {
        viewModelScope.launch {
            try {
                getUserProfile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveUserDetails() {
        viewModelScope.launch {

        }
    }

    fun editUserDetailsOverview() {
        isOverViewFocus.set(false)

        viewModelScope.launch {
            try {
                sendAction(Action.StartEditOverview)
                val result = authRepository.editOverviewUserProfile(
                    overView.get()!!
                )
                loadData()
                sendAction(Action.LoadingSuccess(result.message))
            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }
    }

    //Get Arts by Artist id
    private suspend fun getArtsByArtistId() {
        if (state.isRefreshing) {

        }

    }

    /**
     * Get Nft Onsale arts
     * @return Nft Onsale Arts
     * */
    private suspend fun loadNFTOnSaleArts() {
        val response = artRepository.getArtsByArtistId(
            _page, _limit, sharePreferencesManager.userId, ONSELL
        )
        response.data?.arts?.let {
            _artNFTOnSale.clear()
            _artNFTOnSale.addAll(it)
            salesCount.set(response.data!!.totalCount)
            sendAction(Action.LoadingForArtistArtsSuccess(_artNFTOnSale))
        }
    }

    /**
     * Get Nft Owned Arts
     * @return Nft Owned Arts
     * */
    private suspend fun loadNFTOwnedArts() {
        val response = artRepository.getArtsByArtistId(
            _page, _limit, sharePreferencesManager.userId, OWNED
        )
        response.data?.arts?.let {
            _artNFTOwned.clear()
            _artNFTOwned.addAll(it)
            ownedCount.set(response.data!!.totalCount)
            sendAction(Action.LoadingForArtistArtsSuccess(_artNFTOwned))

        }
    }

    fun onDoneClicked(view: View, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            editUserDetailsOverview()
            return true
        }
        return false
    }

    /*fun onRefresh() {
        sendAction(Action.StartRefreshing)
        loadData()
    }*/

    fun updateProfilePicture(filePath: String) {
        viewModelScope.launch {
            try {
                authRepository.updateProfilePicture(filePath)
            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }
    }

    fun updateCoverPicture(filePath: String) {
        viewModelScope.launch {
            try {
                authRepository.updateCoverPicture(filePath)
            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }
    }

    /**
     * Change tab content base on user's click
     */
    fun changeTabEnum(tabMyTabTypeEnum: TabMyTabTypeEnum) {
        if (tabMyTabTypeEnum == tabSelected.get()) return
        tabSelected.set(tabMyTabTypeEnum)
        sendAction(Action.ChangeTab(tabMyTabTypeEnum))
        onRefresh()
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isRefreshing: Boolean = true,
        val isLoadingMore: Boolean = true,
        val isDataLoaded: Boolean = false,
        val isRefreshed: Boolean = false,
        val isLoading: Boolean = false,
        val isError: Boolean = false,
        val isSessionExpired: Boolean = false,
        val message: String? = null,
        val user: User? = null,
        val art: ArrayList<Art>? = arrayListOf(),
        val myCollections : ArrayList<MyCollectionDetails>? = arrayListOf(),
        val isHaveData: Boolean = false,
        val isEditOverviewUserProfileSuccess: Boolean = false,
        val tabSelected: TabMyTabTypeEnum = TabMyTabTypeEnum.COLLECTION,
        val typeSelected: DepositeTypeEnum = DepositeTypeEnum.DEFAULT,
        val bankDetails: ArrayList<BankDetails>? = arrayListOf(),
        val cards: ArrayList<CardDetails>? = arrayListOf(),
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartRefreshing : Action()
        object StartLoadingMore : Action()
        object StartEditOverview : Action()
        class LoadingSuccessUser(val user: User?) : Action()
        class LoadingSuccess(val message: String) : Action()
        class LoadingForArtistArtsSuccess(val art: ArrayList<Art>) : Action()
        class LoadingCompletelySuccess(val isRefreshed: Boolean = false) : Action()
        class ChangeTab(val tabSelected: TabMyTabTypeEnum) : Action()

        object LoadingStarting : Action()
        class TypeChanged(val depositeSelected: DepositeTypeEnum) : Action()
        class LoadingBankDetailSuccess(val bankDetails: ArrayList<BankDetails>) : Action()
        class LoadingFailure(val message: String, val isSessionExpired: Boolean) : Action()
        class LoadingCardSuccess(val cards: ArrayList<CardDetails>) : Action()
        class LoadingUserCollectionsSuccess(val collection: ArrayList<MyCollectionDetails>) :Action()
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.StartRefreshing -> state.copy(
            isRefreshing = true,
            isLoadingMore = false,
            isError = false,
            isSessionExpired = false,
            message = null
        )
        is Action.LoadingSuccessUser -> state.copy(
            isRefreshing = false,
            isError = false,
            isSessionExpired = false,
            message = null,
            user = viewAction.user
        )
        Action.StartEditOverview -> state.copy(
            isLoading = true,
            isError = false,
            isSessionExpired = false,
            message = null
        )
        is Action.LoadingSuccess -> state.copy(
            isLoading = false,
            isError = false,
            isSessionExpired = false,
            message = viewAction.message,
            isEditOverviewUserProfileSuccess = true
        )
        is Action.LoadingFailure -> state.copy(
            isRefreshing = false,
            isError = true,
            isSessionExpired = viewAction.isSessionExpired,
            isLoading = false,
            message = viewAction.message,
            isLoadingMore = false,
            isDataLoaded = false,
        )
        is Action.LoadingCardSuccess -> state.copy(
            cards = viewAction.cards,
            isError = false,
            isHaveData = viewAction.cards.isNotEmpty()
        )
        is Action.LoadingForArtistArtsSuccess -> state.copy(
            isRefreshing = false,
            isError = false,
            art = viewAction.art,
            isHaveData = viewAction.art.isNotEmpty()
        )
        is Action.LoadingCompletelySuccess -> state
        Action.StartLoadingMore -> state
        is Action.ChangeTab -> state.copy(
            tabSelected = viewAction.tabSelected
        )
        is Action.LoadingStarting -> state.copy(
            isLoading = true,
        )
        is Action.LoadingBankDetailSuccess -> state.copy(
            bankDetails = viewAction.bankDetails,
            isLoading = false,
            isRefreshed = false
        )
        is Action.TypeChanged -> state.copy(typeSelected = viewAction.depositeSelected)
        is Action.LoadingCardSuccess -> state.copy(
            cards = viewAction.cards,
            isError = false
        )

        is Action.LoadingUserCollectionsSuccess -> state.copy(
            isRefreshing = false,
            isError = false,
            myCollections = viewAction.collection,
            isHaveData = viewAction.collection.isNotEmpty()
        )
    }

    //On click on any Type bank or card
    fun onSelectType(depositeSelected: DepositeTypeEnum) {
        if (depositeSelected == typeSelected.get()) return
        typeSelected.set(depositeSelected)
        _selected_card_position.set(-1)
        _selected_bank_position.set(-1)
        sendAction(Action.TypeChanged(depositeSelected))
    }

    //refresh all data
    fun onRefresh() {
        _bankDetails.clear()
        _cards.clear()
        onLoadData()
    }

}
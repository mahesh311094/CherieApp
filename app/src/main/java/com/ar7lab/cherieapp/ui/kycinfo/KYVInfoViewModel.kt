package com.ar7lab.cherieapp.ui.kycinfo

import androidx.databinding.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.KYCStep
import com.ar7lab.cherieapp.model.User
import com.ar7lab.cherieapp.network.repositories.AuthRepository
import com.ar7lab.cherieapp.network.response.GetKYCResponse
import com.ar7lab.cherieapp.network.response.PersonalKYCResponse
import com.ar7lab.cherieapp.utils.DOCUMENT_TYPE_DRIVER_LICENCE
import com.ar7lab.cherieapp.utils.DOCUMENT_TYPE_GOVERNMENT
import com.ar7lab.cherieapp.utils.DOCUMENT_TYPE_PASSPORT
import com.ar7lab.cherieapp.utils.isCardHolderNameValid
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class KYVInfoViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sharePreferencesManager: SharePreferencesManager
) :
    BaseViewModel<KYVInfoViewModel.ViewState, KYVInfoViewModel.Action>(ViewState()) {

    var fullname = ObservableField("")
    var dateOfBirth = ObservableField("Select Date Of Birth")
    var address = ObservableField("")
    var postalCode = ObservableField("")
    var city = ObservableField("")
    val country = ObservableField("India")
    val frontPhoto = ObservableField("")
    val backPhoto = ObservableField("")
    val selfiePhoto = ObservableField("")
    var hideStartVerification = ObservableBoolean(false)
    var currentStep = ObservableField(KYCStep.STEP_1)
    var currentSelectedDoc = ObservableInt(0)
    var motionProgress = ObservableFloat(0.0f)
    var onDobClick = ObservableBoolean(false)
    var showDialog = ObservableBoolean(false)
    var onClickPhoto = MutableLiveData(-1)

    var isFullNameValid = ObservableBoolean(false)
    var isDOBValid = ObservableBoolean(false)
    var isValidAddress = ObservableBoolean(false)
    var isValidPostCode = ObservableBoolean(false)
    var isValidCity = ObservableBoolean(false)
    var _personalKYCResponse: GetKYCResponse? = null

    fun init() {

        fullname.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isFullNameValid.set(isCardHolderNameValid(fullname.get()))
            }
        })
        dateOfBirth.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {

            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isDOBValid.set(dateOfBirth.get()?.contains("-")!!)
            }
        })
        address.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {

            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isValidAddress.set(address.get()?.length!! > 5)
            }
        })
        postalCode.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {

            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isValidPostCode.set(postalCode.get()?.length!! > 5)
            }
        })

        city.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {

            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isValidCity.set(city.get()?.length!! > 2)
            }
        })

        getKVCDetail()
    }

    fun startVerification() {
        hideStartVerification.set(true)
    }

    fun onContinue(step: KYCStep) {

        when (step) {
            KYCStep.STEP_2 -> {
                kycPersonal(step)
            }
            KYCStep.STEP_3 -> {
                identityVerificationStep(step)
            }
            KYCStep.STEP_4 -> {
                identityUploadStep(step)
            }
            KYCStep.DONE -> {
                facePhotoUpload(step)
            }
        }
    }

    fun getKVCDetail() {
        viewModelScope.launch {
            try {
                val response = authRepository.getKYCDetail()
                _personalKYCResponse = response
                setData()
                sendAction(Action.LoadingSuccessKYCDetail(response))
            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }
    }

    fun setData() {
        _personalKYCResponse?.data?.personalVerification?.name.let { fullname.set(it) }
        _personalKYCResponse?.data?.personalVerification?.country.let { country.set(it) }
       // _personalKYCResponse?.data?.personalVerification?.birthday.let { dateOfBirth.set(it) }
        _personalKYCResponse?.data?.personalVerification?.address.let { address.set(it) }
        _personalKYCResponse?.data?.personalVerification?.postalCode.let { postalCode.set(it) }
        _personalKYCResponse?.data?.personalVerification?.city.let { city.set(it) }
    }

    fun facePhotoUpload(step: KYCStep) {
        if (selfiePhoto.get().equals("")) {
            return
        }
        viewModelScope.launch {
            try {

                val response = authRepository.faceUpload(selfiePhoto.get()!!)
                motionProgress.set(1.0f)
                showDialog.set(true)
                sendAction(Action.LoadingSuccessFaceUpload(response.message))
            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }
    }

    fun identityVerificationStep(step: KYCStep) {

        motionProgress.set(0.6f)
        currentStep.set(step)
    }

    fun identityUploadStep(step: KYCStep) {
        if (backPhoto.get().equals("") || frontPhoto.get().equals("")) {
            return
        }

        viewModelScope.launch {
            try {
                val currentSelectionDoc =
                    if (currentSelectedDoc.get() == 0) DOCUMENT_TYPE_GOVERNMENT else if (currentSelectedDoc.get() == 1) DOCUMENT_TYPE_PASSPORT else DOCUMENT_TYPE_DRIVER_LICENCE
                val response = authRepository.identityUpload(
                    frontPhoto.get()!!,
                    backPhoto.get()!!,
                    currentSelectionDoc
                )
                motionProgress.set(0.95f)
                currentStep.set(step)
                sendAction(Action.LoadingSuccessDocumentUpload(response.message))
            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }


    }

    fun kycPersonal(step: KYCStep) {
        if (!isFullNameValid.get() || !isDOBValid.get() || !isValidAddress.get() || !isValidCity.get() || !isValidPostCode.get()) {
            return
        }

        viewModelScope.launch {

            try {
                val response = authRepository.personalKYC(
                    country.get()!!,
                    fullname.get()!!,
                    dateOfBirth.get()!!,
                    address.get()!!,
                    postalCode.get()!!,
                    city.get()!!
                )
                motionProgress.set(0.3f)
                if (step != KYCStep.DONE)
                    currentStep.set(step)
                sendAction(
                    Action.LoadingSuccessPersonaKYC(
                        response.message,
                        response.data?.kyc?.personalVerification
                    )
                )
            } catch (e: Exception) {
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }


    }

    fun onSelectDocuments(type: Int) {
        currentSelectedDoc.set(type)
    }

    fun onDobClick() {
        onDobClick.set(true)
    }

    fun onClickPhoto(type: Int) {
        onClickPhoto.postValue(type)
    }


    // Define view state for Activity to use
    internal data class ViewState(
        val isLoggingIn: Boolean = true,
        val isLoding: Boolean = false,
        val isError: Boolean = false,
        val isVerified: Boolean = true,
        val isPersonalKYCSuccess: Boolean = false,
        val isIdentityUploaded: Boolean = false,
        val faceUploaded: Boolean = false,
        val message: String? = null,
        val personalKYCResponse: PersonalKYCResponse.PersonalVerification? = null,
        val personalKYCRespon: GetKYCResponse? = null
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartLoding : Action()
        class LoadingSuccessPersonaKYC(
            val message: String,
            val personalKYCResponse: PersonalKYCResponse.PersonalVerification?
        ) : Action()

        class LoadingSuccessDocumentUpload(val message: String) : Action()
        class LoadingSuccessFaceUpload(val message: String) : Action()
        class LoadingSuccessKYCDetail(val personalKYCResponse: GetKYCResponse) : Action()
        class LoadingFailure(val message: String, val isVerified: Boolean = true) : Action()
    }

    // Return a state based on action
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {

        Action.StartLoding -> state.copy(
            isLoding = true,
            isError = false,
            isVerified = true,
            message = null,
            faceUploaded = false,
            isPersonalKYCSuccess = false,
            isIdentityUploaded = false,
            personalKYCResponse = null
        )
        is Action.LoadingSuccessPersonaKYC -> state.copy(
            isLoding = false,
            isError = false,
            isPersonalKYCSuccess = true,
            isIdentityUploaded = false,
            faceUploaded = false,
            message = viewAction.message,
            personalKYCResponse = viewAction.personalKYCResponse
        )
        is Action.LoadingSuccessDocumentUpload -> state.copy(
            isLoding = false,
            isError = false,
            isPersonalKYCSuccess = false,
            isIdentityUploaded = true,
            faceUploaded = false,
            message = viewAction.message
        )
        is Action.LoadingFailure -> state.copy(
            isLoding = false,
            isLoggingIn = false,
            isIdentityUploaded = false,
            isPersonalKYCSuccess = false,
            isVerified = viewAction.isVerified,
            isError = true,
            faceUploaded = false,
            message = viewAction.message,
            personalKYCResponse = null
        )
        is Action.LoadingSuccessFaceUpload -> state.copy(
            isLoding = false,
            isError = false,
            isPersonalKYCSuccess = false,
            isIdentityUploaded = false,
            faceUploaded = true,
            message = viewAction.message
        )
        is Action.LoadingSuccessKYCDetail -> state.copy(
            isLoding = false,
            isError = false,
            isPersonalKYCSuccess = false,
            isIdentityUploaded = false,
            faceUploaded = false,
            personalKYCRespon = viewAction.personalKYCResponse
        )
    }
}
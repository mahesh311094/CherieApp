package com.ar7lab.cherieapp.ui.edituserprofiledetails

import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.enums.AccountTypeEnum
import com.ar7lab.cherieapp.network.repositories.AuthRepository
import com.ar7lab.cherieapp.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * EditUserProfileDetailsViewModel view model
 * @property authRepository for signup
 * */
@HiltViewModel
internal class EditUserProfileDetailsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel<EditUserProfileDetailsViewModel.ViewState, EditUserProfileDetailsViewModel.Action>(
    ViewState()
) {

    var email = ObservableField("")
    var password = ObservableField("")
    var userName = ObservableField("")
    var firstName = ObservableField("")
    var lastName = ObservableField("")
    var contactNo = ObservableField("")
    val countryPhoneCode = ObservableField("+93")
    var companyName = ObservableField("")
    var country = ObservableField("")
    var isUserNameValid = ObservableBoolean(false)
    var isFirstNameValid = ObservableBoolean(false)
    var isLastNameValid = ObservableBoolean(false)
    var isCompanyNameValid = ObservableBoolean(false)
    var isEmailValid = ObservableBoolean(false)
    var isMobileNumberValid = ObservableBoolean(false)
    var isPasswordValid = ObservableBoolean(false)
    val isSignUpClicked = ObservableBoolean(false)
    val signUpTypeSelected = ObservableField(AccountTypeEnum.PERSONAL)

    /**
     * Function for validation
     * @return Observe changes from username input, then validate inputs
     * */
    fun init() {
        // Observe changes from username input, then validate username
        userName.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isUserNameValid.set(isValidUserName(userName.get()))
            }
        })
        // Observe changes from first name input, then validate first name
        firstName.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isFirstNameValid.set(isValidFirstName(firstName.get()))
            }
        })
        // Observe changes from last name input, then validate last name
        lastName.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isLastNameValid.set(isValidLastName(lastName.get()))
            }
        })
        // Observe changes from company name input, then validate company name
        companyName.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isCompanyNameValid.set(isValidCompanyName(companyName.get()))
            }
        })
        // Observe changes from email input, then validate email
        email.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isEmailValid.set(isValidEmail(email.get()))
            }
        })
        // Observe changes from mobile number input, then validate mobile number
        contactNo.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isMobileNumberValid.set(isValidMobileNumber(contactNo.get()))
            }
        })
        // Observe changes from password input, then validate password
        password.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isPasswordValid.set(isValidPassword(password.get()))
            }
        })
    }

    //set username
    fun setUserName(username: String?) {
        userName.set(username)
    }

    //Set firstname
    fun setFirstName(firstname: String?) {
        firstName.set(firstname)
    }

    fun setLastName(lastname: String?) {
        lastName.set(lastname)
    }

    fun setEmail(_email: String?) {
        email.set(_email)
    }

    fun setCompanyName(_companyName: String?) {
        companyName.set(_companyName)
    }

    fun setContactNo(contact: String?) {
        if (!contact.equals("") || contact != null) {
            val splitContactNumber: List<String> = contact!!.split("-")
            if (splitContactNumber.size > 1)
                contactNo.set(splitContactNumber[1])
            else
                contactNo.set(contact)
        } else {
            contactNo.set(contact)
        }
    }

    /**
     * Set country
     * Add [countryCode] for set country
     * @return country code
     * */
    //Set country
    fun setCountry(countryCode: String) {
        country.set(countryCode)
    }

    //Set country phone code
    fun setCountryPhoneCode(phoneCode: String) {
        countryPhoneCode.set(phoneCode)
    }

    //Set type "Company"
    fun selectCompany() {
        signUpTypeSelected.set(AccountTypeEnum.COMPANY)
    }

    //Set type "Personal"
    fun selectPersonal() {
        signUpTypeSelected.set(AccountTypeEnum.PERSONAL)
    }

    fun updatedContactNumber():String{
      return countryPhoneCode.get()!! + "-" + contactNo.get()!!
    }

    fun editUserProfile() {
        if (signUpTypeSelected.get() == AccountTypeEnum.PERSONAL) {
            editUserDetailsPersonal()
        } else {
            editUserDetailsCompany()
        }
    }


    private fun editUserDetailsPersonal() {
        isSignUpClicked.set(true)
        if (!isUserNameValid.get() || !isFirstNameValid.get() || !isLastNameValid.get()) {
            return
        }
        viewModelScope.launch {
            try {
                sendAction(Action.StartEditProfile)
                val result = authRepository.editPersonalUserProfile(
                    userName.get()!!,
                    AccountTypeEnum.PERSONAL.name,
                    firstName.get()!!,
                    lastName.get()!!,
                    country.get()!!,
                    countryPhoneCode.get()!! + "-" + contactNo.get()!!,
                )
                sendAction(Action.LoadingSuccess(result.message))
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    private fun editUserDetailsCompany() {
        isSignUpClicked.set(true)
        viewModelScope.launch {
            try {
                sendAction(Action.StartEditProfile)
                val result = authRepository.editCompanyUserProfile(
                    userName.get()!!,
                    AccountTypeEnum.COMPANY.name,
                    firstName.get()!!,
                    lastName.get()!!,
                    companyName.get()!!,
                    country.get()!!,
                    countryPhoneCode.get()!! + "-" + contactNo.get()!!,
                )
                sendAction(Action.LoadingSuccess(result.message))
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message,error.isSessionExpired))
            }
        }
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isLoading: Boolean = true,
        val isError: Boolean = false,
        val isSessionExpired: Boolean = false,
        val message: String? = null,
        val isEditUserProfileSuccess: Boolean = false
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartEditProfile : Action()
        class LoadingSuccess(val message: String) : Action()
        class LoadingFailure(val message: String,val isSessionExpired:Boolean) : Action()
    }

    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.StartEditProfile -> state.copy(
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
            isEditUserProfileSuccess = true
        )
        is Action.LoadingFailure -> state.copy(
            isLoading = false,
            isError = true,
            isSessionExpired = viewAction.isSessionExpired,
            message = viewAction.message
        )
    }
}
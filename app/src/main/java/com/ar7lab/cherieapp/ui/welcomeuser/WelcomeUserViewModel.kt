package com.ar7lab.cherieapp.ui.welcomeuser

import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.AccountTypeEnum
import com.ar7lab.cherieapp.enums.SocialMediaTypeEnum
import com.ar7lab.cherieapp.model.User
import com.ar7lab.cherieapp.network.repositories.AuthRepository
import com.ar7lab.cherieapp.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class WelcomeUserViewModel @Inject constructor(
    private val authRepository: AuthRepository,private val sharePreferencesManager: SharePreferencesManager
) :
    BaseViewModel<WelcomeUserViewModel.ViewState, WelcomeUserViewModel.Action>(ViewState()) {

    var email = ObservableField("")
    var firstName = ObservableField("")
    var lastName = ObservableField("")
    var contactNo = ObservableField("")
    val countryPhoneCode = ObservableField("+93")
    var companyName = ObservableField("")
    var country = ObservableField("")
    var isEmailValid = ObservableBoolean(false)
    var isFirstNameValid = ObservableBoolean(false)
    var isLastNameValid = ObservableBoolean(false)
    var isCompanyNameValid = ObservableBoolean(false)
    var isMobileNumberValid = ObservableBoolean(false)
    val isSaveClicked = ObservableBoolean(false)
    val accountTypeSelected = ObservableField(AccountTypeEnum.PERSONAL)

    /**
     * Function for validation
     * @return Observe changes from username input, then validate inputs
     * */
    fun init(user: User) {
        email.set(user.email)
        firstName.set(user.firstName)
        lastName.set(user.lastName)
        companyName.set(user.companyName)
        if(!user.contactNo.isNullOrEmpty() && user.contactNo!!.contains("-")) {
            val mobileNumberSplit = user.contactNo!!.split("-")
            val mobileNumber=if(mobileNumberSplit.size>0) mobileNumberSplit.get(1) else ""
            val contryCode=if(mobileNumberSplit.size>0) mobileNumberSplit.get(0) else ""
            contactNo.set(mobileNumber)
            countryPhoneCode.set(contryCode)


        }
        // Observe changes from email input, then validate email
        email.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isEmailValid.set(isValidEmail(email.get()))
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
        // Observe changes from mobile number input, then validate mobile number
        contactNo.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isMobileNumberValid.set(isValidMobileNumber(contactNo.get()))
            }
        })
    }
    fun getUserLoginType():Boolean
    {
        return if(sharePreferencesManager.loginType.equals(SocialMediaTypeEnum.NONE.name))true else false
    }

    /**
     * Set country
     * Add [countryCode] for set country
     * @return country code
     * */
    fun setCountry(countryCode: String) {
        country.set(countryCode)
    }

    /**
     * Set country phone code
     * Add [phoneCode] for set country
     * @return country phone code
     * */
    fun setCountryPhoneCode(phoneCode: String) {
        countryPhoneCode.set(phoneCode)
    }

    /**
     * Set type "Company"
     * @return Company
     * */
    fun selectCompany() {
        accountTypeSelected.set(AccountTypeEnum.COMPANY)
    }

    /**
     * Set type "Personal"
     * @return Personal
     * */
    fun selectPersonal() {
        accountTypeSelected.set(AccountTypeEnum.PERSONAL)
    }

    fun save() {
        isSaveClicked.set(true)
        if (accountTypeSelected.get() == AccountTypeEnum.PERSONAL) {
            savePersonalInfo()
        } else {
            saveCompanyInfo()
        }
    }

    /**
     * Network call for Personal Signup
     * @return personal signup
     * */
    private fun savePersonalInfo() {
        //Check each input passed validation or not
        if (!isFirstNameValid.get() || !isEmailValid.get() || !isLastNameValid.get()) {
            return
        }

        viewModelScope.launch {
            try {
                sendAction(Action.StartSignUp)
                val result = authRepository.updateUser(
                    firstName.get()!!,
                    lastName.get()!!,
                    countryPhoneCode.get()!! +"-"+ contactNo.get()!!,
                    accountTypeSelected.get()!!,
                    companyName.get()!!,
                    country.get()!!,
                    "",
                    ""
                )
                sendAction(Action.LoadingSuccess())
            } catch (e: Exception) {
                e.printStackTrace()
                sendAction(Action.LoadingFailure(getErrorMessage(e).message))
            }
        }
    }

    /**
     * Network call for Company Signup
     * @return company signup
     * */
    private fun saveCompanyInfo() {
        if (!isFirstNameValid.get() || !isLastNameValid.get() || !isEmailValid.get() || !isCompanyNameValid.get()) {
            return
        }
        viewModelScope.launch {
            try {
                sendAction(Action.StartSignUp)
                val result = authRepository.updateUser(
                    firstName.get()!!,
                    lastName.get()!!,
                    countryPhoneCode.get()!! +"-"+ contactNo.get()!!,
                    accountTypeSelected.get()!!,
                    companyName.get()!!,
                    country.get()!!,
                    "",
                    ""
                )
                sendAction(Action.LoadingSuccess())
            } catch (e: Exception) {
                e.printStackTrace()
                sendAction(Action.LoadingFailure(getErrorMessage(e).message))
            }
        }
    }

    fun updateProfilePicture(filePath: String) {
        viewModelScope.launch {
            try {
                authRepository.updateProfilePicture(filePath)
            } catch (e: Exception) {
                e.printStackTrace()
                sendAction(Action.LoadingFailure(getErrorMessage(e).message))
            }
        }
    }

    fun updateCoverPicture(filePath: String) {
        viewModelScope.launch {
            try {
                authRepository.updateCoverPicture(filePath)
            } catch (e: Exception) {
                e.printStackTrace()
                sendAction(Action.LoadingFailure(getErrorMessage(e).message))
            }
        }
    }

    /**
     * Define view state for Activity to use
     * @return state
     * */
    internal data class ViewState(
        val isLoading: Boolean = true,
        val isError: Boolean = false,
        val message: String? = null,
        val isUserSaved: Boolean = false
    ) : BaseViewState

    /**
     * Define action of the ViewModel
     * @return action
     * */
    internal sealed class Action : BaseAction {
        object StartSignUp : Action()
        class LoadingSuccess() : Action()
        class LoadingFailure(val message: String) : Action()
    }

    /**
     * State based on action
     * @return a state based on action
     * */
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.StartSignUp -> state.copy(
            isLoading = true,
            isError = false,
            message = null
        )
        is Action.LoadingSuccess -> state.copy(
            isLoading = false,
            isError = false,
            isUserSaved = true
        )
        is Action.LoadingFailure -> state.copy(
            isLoading = false,
            isError = true,
            message = viewAction.message
        )
    }
}
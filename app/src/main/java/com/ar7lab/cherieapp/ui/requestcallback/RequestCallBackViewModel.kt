package com.ar7lab.cherieapp.ui.requestcallback

import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.network.repositories.CommonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class RequestCallBackViewModel @Inject constructor(
    private val commonRepository: CommonRepository
) :
    BaseViewModel<RequestCallBackViewModel.ViewState, RequestCallBackViewModel.Action>(ViewState()) {

    val queryTitle = ObservableField("")
    val queryDescription = ObservableField("")
    val isCanSubmit = ObservableBoolean(false)

    fun init() {
        queryTitle.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isCanSubmit.set(
                    !queryTitle.get().isNullOrBlank() && !queryDescription.get().isNullOrBlank()
                )
            }

        })
        queryDescription.addOnPropertyChangedCallback(object :
            Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                isCanSubmit.set(
                    !queryTitle.get().isNullOrBlank() && !queryDescription.get().isNullOrBlank()
                )
            }

        })
    }

    fun submit() {
        if (!isCanSubmit.get()) {
            return
        }
        viewModelScope.launch {
            try {
                sendAction(Action.StartSubmitting)
                commonRepository.submitRequestCallback(
                    queryTitle.get()!!,
                    queryDescription.get()!!
                )
                sendAction(Action.SubmitSuccess)
            } catch (e: Exception) {
                e.printStackTrace()
                val error=getErrorMessage(e)
                sendAction(Action.SubmitFailure(error.message,error.isSessionExpired))
            }
        }
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isSubmitting: Boolean = false,
        val isSubmitted: Boolean = false,
        val isError: Boolean = false,
        val isSessionExpired: Boolean = false,
        val message: String? = null,
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartSubmitting : Action()
        object SubmitSuccess : Action()
        class SubmitFailure(val message: String,val isSessionExpired: Boolean) : Action()
    }

    // Return a state based on action
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.StartSubmitting -> state.copy(
            isSubmitting = true,
            isError = false,
            isSessionExpired = false,
            message = null
        )
        Action.SubmitSuccess -> state.copy(
            isSubmitting = false,
            isSubmitted = true,
        )
        is Action.SubmitFailure -> state.copy(
            isSubmitting = false,
            isSubmitted = false,
            isError = true,
            isSessionExpired = viewAction.isSessionExpired,
            message = viewAction.message
        )
    }

}
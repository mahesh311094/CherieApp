package com.ar7lab.cherieapp.ui.onboardingscreen

import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.model.Features
import com.ar7lab.cherieapp.network.repositories.CommonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class OnBoardingViewModel @Inject constructor(private val commonRepository: CommonRepository) :
    BaseViewModel<OnBoardingViewModel.ViewState, OnBoardingViewModel.Action>(ViewState()) {

    private val _featuresList = ArrayList<Features>()

    fun getFeatures() {
        viewModelScope.launch {
            try {
                // View will show the state loading from this action
                sendAction(Action.StartLoading)
                val response = commonRepository.getFeaturesList()
                response.data?.featureScreens?.let {
                    _featuresList.clear()
                    _featuresList.addAll(it)
                    sendAction(Action.LoadingFeaturesSuccess(_featuresList))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                sendAction(Action.LoadingFailure(getErrorMessage(e).message))
            }
        }
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isLoading: Boolean = true,
        val isError: Boolean = false,
        val isSubmitted: Boolean = false,
        val features: ArrayList<Features>? = arrayListOf(),
        val message: String? = null
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartLoading : Action()
        class LoadingSuccess(val message: String) : Action()
        class LoadingFeaturesSuccess(val features: ArrayList<Features>) : Action()
        class LoadingFailure(val message: String) : Action()
    }

    // Return a state based on action
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.StartLoading -> state.copy(
            isLoading = true,
            isError = false,
            isSubmitted = false,
            message = null
        )
        is Action.LoadingSuccess -> state.copy(
            isLoading = false,
            isError = false,
            isSubmitted = true,
            message = viewAction.message
        )
        is Action.LoadingFeaturesSuccess -> state.copy(
            isLoading = false,
            features = viewAction.features
        )
        is Action.LoadingFailure -> state.copy(
            isLoading = false,
            isError = true,
            isSubmitted = false,
            message = viewAction.message
        )
    }

}
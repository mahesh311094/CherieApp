package com.ar7lab.cherieapp.ui.chooselanguage

import androidx.databinding.ObservableField
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.ChooseLanguageEnum
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class ChooseLanguageViewModel @Inject constructor(
    private val sharePreferencesManager: SharePreferencesManager
) : BaseViewModel<ChooseLanguageViewModel.ViewState, ChooseLanguageViewModel.Action>(ViewState()) {
    val languageChose = ObservableField(sharePreferencesManager.language)

    fun changeLanguage(chooseLanguageEnum: ChooseLanguageEnum) {
        languageChose.set(chooseLanguageEnum.locale)
    }


    fun save() {
        sharePreferencesManager.language = languageChose.get()!!
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isError: Boolean = false,
        val message: String? = null,
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        class LoadingFailure(val message: String) : Action()
    }

    // Return a state based on action
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        is Action.LoadingFailure -> state.copy(
            isError = true,
            message = viewAction.message
        )
    }
}
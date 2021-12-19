package com.ar7lab.cherieapp.ui.helpandsupport

import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.enums.CheckoutTypeEnum
import com.ar7lab.cherieapp.enums.FaqsTypeEnum
import com.ar7lab.cherieapp.model.FAQ
import com.ar7lab.cherieapp.network.repositories.CommonRepository
import com.ar7lab.cherieapp.ui.payment.selectpayment.SelectPaymentViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class HelpAndSupportViewModel @Inject constructor(
    private val commonRepository: CommonRepository
) : BaseViewModel<HelpAndSupportViewModel.ViewState, HelpAndSupportViewModel.Action>(
    ViewState()
) {

    private var _search: String = ""

    //support faq open
    val faqSelected = ObservableField(FaqsTypeEnum.NONE)

    override fun onLoadData() {
        super.onLoadData()
        viewModelScope.launch {
            try {
                sendAction(Action.StartRefreshing)
                val response = commonRepository.getFAQs(_search)
                response.data?.let { data ->
                    sendAction(Action.LoadingSuccess(data.faqs as ArrayList<FAQ>))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                sendAction(Action.LoadingFailure(getErrorMessage(e).message))
            }
        }
    }

    fun onRefresh() {
        loadData()
    }

    fun search(query: String) {
        _search = query
        loadData()
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isRefreshing: Boolean = false,
        val isError: Boolean = false,
        val message: String? = null,
        val faqSelected: FaqsTypeEnum = FaqsTypeEnum.NONE,
        val FAQs: ArrayList<FAQ>? = null
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartRefreshing : Action()
        class LoadingSuccess(val FAQs: ArrayList<FAQ>?) : Action()
        class LoadingFailure(val message: String) : Action()
        class TypeChanged(val faqSelected: FaqsTypeEnum) : Action()
    }

    // Return a state based on action
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.StartRefreshing -> state.copy(
            isRefreshing = true,
            isError = false,
            message = null
        )
        is Action.LoadingSuccess -> state.copy(
            FAQs = viewAction.FAQs,
            isRefreshing = false
        )
        is Action.LoadingFailure -> state.copy(
            isRefreshing = false,
            isError = true,
            message = viewAction.message
        )
        is Action.TypeChanged -> state.copy(faqSelected = viewAction.faqSelected)
    }

    fun onSelectType(faqsSelected: FaqsTypeEnum) {
        if (faqsSelected == faqSelected.get()) return
        faqSelected.set(faqsSelected)
        sendAction(Action.TypeChanged(faqsSelected))
    }
}
package com.ar7lab.cherieapp.ui.collectionnft

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.ArtData
import com.ar7lab.cherieapp.model.AssetsListItem
import com.ar7lab.cherieapp.model.MyCollectionDetails
import com.ar7lab.cherieapp.network.repositories.AuthRepository
import com.ar7lab.cherieapp.network.repositories.UserRepository
import com.ar7lab.cherieapp.ui.profile.ProfileViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class MyCollectionNFTViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : BaseViewModel<MyCollectionNFTViewModel.ViewState, MyCollectionNFTViewModel.Action>(ViewState()) {

    private var _myCollectionDetails: MyCollectionDetails? =null



    private var _page = 1

    //Set date load limit for each time
    private var _limit = 10

    fun init(myCollectionDetails: MyCollectionDetails?) {
        _myCollectionDetails = myCollectionDetails
        loadData()
    }

    override fun onLoadData() {
        viewModelScope.launch {
            try {
                //Get user profile
                getUserAssestData()

            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message))
            }
        }
    }

    private suspend fun getUserAssestData(){
        val response = userRepository.getMyAssestDataById(_myCollectionDetails?.id?:"",_page,_limit)
        response.data?.let { collectionData ->
            sendAction(Action.LoadingAssestDataSuccess(collectionData.payload.assetsList as ArrayList<AssetsListItem> , collectionData.payload.artData ,collectionData.totalCount))
        }
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isLoading: Boolean = true,
        val isError: Boolean = false,
        val isSubmitted: Boolean = false,
        val message: String? = null,
        val myAssestData : ArrayList<AssetsListItem>? = arrayListOf(),
        val artData:ArtData? = null,
        val pieces:Int?=null
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartLoading : Action()
        class LoadingSuccess(val message: String) : Action()
        class LoadingFailure(val message: String) : Action()
        class LoadingAssestDataSuccess(val assestData :ArrayList<AssetsListItem> , val artData: ArtData? , val pieces: Int?) :Action()
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
        is Action.LoadingFailure -> state.copy(
            isLoading = false,
            isError = true,
            isSubmitted = false,
            message = viewAction.message
        )
        is Action.LoadingAssestDataSuccess -> state.copy(
            isLoading = false,
            isError = false,
            isSubmitted = true,
            myAssestData = viewAction.assestData,
            artData = viewAction.artData,
            pieces = viewAction.pieces
        )
    }
}
package com.ar7lab.cherieapp.ui.traditionalartworkdetails

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.ar7lab.cherieapp.base.viewmodel.BaseAction
import com.ar7lab.cherieapp.base.viewmodel.BaseViewModel
import com.ar7lab.cherieapp.base.viewmodel.BaseViewState
import com.ar7lab.cherieapp.di.SharePreferencesManager
import com.ar7lab.cherieapp.enums.ArtworkDetailTabEnum
import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.CurrentSale
import com.ar7lab.cherieapp.network.repositories.ArtRepository
import com.ar7lab.cherieapp.network.repositories.ArtistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class TraditionalArtworkDetailsViewModel @Inject constructor(
    private val artRepository: ArtRepository,
    private val artistRepository: ArtistRepository,
    private val sharePreferencesManager: SharePreferencesManager
) :
    BaseViewModel<TraditionalArtworkDetailsViewModel.ViewState,
            TraditionalArtworkDetailsViewModel.Action>(ViewState()) {

    private var _art: Art? = null
    val commentTyped = ObservableField("")
    private val _traditionalArts = ArrayList<Art>()

    //Like Count
    val artLikeCount = ObservableField(0)

    //Post is likes or not
    val artLiked = ObservableBoolean(false)

    //Comment Count
    val artCommentCount = ObservableField(0)


    val artQty = ObservableField(1)
    val totalAmount = ObservableField(1.0)

    // Save the selected tab type
    var tabTypeSelected = ObservableField(ArtworkDetailTabEnum.DETAILS)

    //Store the total image count of the fileurls to show dots
    var totalFileUrls = ObservableField(0)

    var restoreHeight = ObservableField(false)

    //To check if current object is empty then disable buy now button
    var currentObjectCheck = ObservableField(false)

    fun init(art: Art?) {
        // Don't need to load data every time the fragment is visible if the data already there
//        if (_art != null) {
//            return
//        }
        _art = art
        onRefresh()
    }

    //plus button clicked increase qty
    fun increaseQty() {
        var qty: Int = artQty.get()!!
        artQty.set(++qty)
        val temp = artQty.get()!! * _art?.price!!;
        totalAmount.set(temp)
        sendAction(Action.ChangeTab(tabTypeSelected.get()!!))
    }

    //minus button clicked descrise qty
    fun descreaseQty() {
        var qty: Int = artQty.get()!!
        if (qty > 1) {
            artQty.set(--qty)
        }
        val temp = artQty.get()!! * _art?.price!!
        totalAmount.set(temp)
        sendAction(Action.ChangeTab(tabTypeSelected.get()!!))
    }


    override fun onLoadData() {
        super.onLoadData()
        viewModelScope.launch {
            try {
                // load art details
                val response = artRepository.getArtDetails(_art?.id ?: "")
                _art = response.data?.art
                totalAmount.set(_art?.price!!)
                _art?.let {
                    artLiked.set(it.liked)
                    artLikeCount.set(it.likeCount)
                    totalFileUrls.set(it.fileUrls.size)
                    if (it.salesInfo.currentSale != CurrentSale()) {
                        currentObjectCheck.set(false)
                    } else {
                        currentObjectCheck.set(true)
                    }
                }
                sendAction(Action.LoadingSuccess(_art))

            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }
    }


    fun onRefresh() {
        sendAction(Action.StartRefreshing)
        loadData()
    }

    fun sendComment() {
        if (commentTyped.get().isNullOrBlank()) {
            return
        }
        viewModelScope.launch {
            try {
                artRepository.sendArtComment(_art?.id ?: "", commentTyped.get() ?: "")
                commentTyped.set("")
                onRefresh() // refresh screen to see latest comment!
            } catch (e: Exception) {
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }
    }

    //on tap any tab this function will called
    fun changeTab(artworkDetailTabEnum: ArtworkDetailTabEnum) {
        if (tabTypeSelected.get() == artworkDetailTabEnum) return
        tabTypeSelected.set(artworkDetailTabEnum)
        sendAction(Action.ChangeTab(artworkDetailTabEnum))
    }

    //like this art
    fun likeThisArt() {
        viewModelScope.launch {
            try {
                if (artLiked.get()) {
                    updateLikeCountForThisArt()
                    artRepository.unLikeArt(_art?.id ?: "")
                } else {
                    updateLikeCountForThisArt()
                    artRepository.likeArt(_art?.id ?: "")
                }
            } catch (e: Exception) {
                updateLikeCountForThisArt()
                e.printStackTrace()
                val error = getErrorMessage(e)
                sendAction(Action.LoadingFailure(error.message, error.isSessionExpired))
            }
        }
    }

    //update like counter or art
    private fun updateLikeCountForThisArt() {
        artLiked.set(!artLiked.get())
        val newCount = artLikeCount.get()!! + if (artLiked.get()) 1 else -1
        artLikeCount.set(newCount)
        _art?.liked = artLiked.get()
        _art?.likeCount = artLikeCount.get()!!
    }

    //updates likes
    private fun updateLikeArt(artId: String) {
        // find the art item in the array and then update liked
        _traditionalArts.find { it.id == artId }?.let {
            it.liked = !it.liked
            it.likeCount += if (it.liked) 1 else -1
        }
        sendAction(Action.LoadingArtsSuccess(_traditionalArts))
    }

    // Define view state for Activity to use
    internal data class ViewState(
        val isRefreshing: Boolean = true,
        val isLoadingMore: Boolean = false,
        val isLoadingMoreArts: Boolean = false,
        val isError: Boolean = false,
        val isSessionExpired: Boolean = false,
        val message: String? = null,
        val art: Art? = null,
        val tabSelected: ArtworkDetailTabEnum = ArtworkDetailTabEnum.DETAILS,
        val arts: ArrayList<Art>? = null
    ) : BaseViewState

    // Define action of the ViewModel
    internal sealed class Action : BaseAction {
        object StartRefreshing : Action()
        object StartLoadingMore : Action()
        class LoadingSuccess(val art: Art?) : Action()
        class LoadingFailure(val message: String, val isSessionExpired: Boolean) : Action()
        class ChangeTab(val tabSelected: ArtworkDetailTabEnum) : Action()
        class LoadingArtsSuccess(val arts: ArrayList<Art>) : Action()
    }

    // Return a state based on action
    override fun onReduceState(viewAction: Action): ViewState = when (viewAction) {
        Action.StartRefreshing -> state.copy(
            isRefreshing = true,
            isError = false,
            isSessionExpired = false,
            message = null,
            art = null
        )
        Action.StartLoadingMore -> state.copy(
            isLoadingMore = true,
            art = null
        )

        is Action.LoadingSuccess -> state.copy(
            isRefreshing = false,
            isError = false,
            isSessionExpired = false,
            message = null,
            art = viewAction.art
        )

        is Action.LoadingFailure -> state.copy(
            isRefreshing = false,
            isLoadingMore = false,
            isError = true,
            isSessionExpired = viewAction.isSessionExpired,
            message = viewAction.message,
            art = null
        )
        is Action.ChangeTab -> state.copy(
            tabSelected = viewAction.tabSelected,
            art = null
        )
        is Action.LoadingArtsSuccess -> state.copy(
            arts = viewAction.arts,
            art = null,
            isLoadingMoreArts = false
        )
    }
}
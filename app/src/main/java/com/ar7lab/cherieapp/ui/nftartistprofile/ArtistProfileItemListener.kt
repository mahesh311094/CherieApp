package com.ar7lab.cherieapp.ui.nftartistprofile

import com.ar7lab.cherieapp.model.Art

/**
* Listeners for Artist Profile
* */
interface ArtistProfileItemListener {
    /**
     * Add [Art] data class to Artist Profile
     * @return Art object
     * */
    fun likeClick(artistArts: Art)
    fun viewCommentClick(artistArts: Art)
}
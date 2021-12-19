package com.ar7lab.cherieapp.ui.storesearch

import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.Artist

/**
 * Listener for Marketplace
 * */
interface StoreSearchArtListener {
    /**
     * Add [Art] data class to Market Art Listener
     * @return Art object
     * */
    fun onClick(art: Art)
    fun likeClick(art: Art)
    fun onClickTrade()
    fun notifyClick(art: Art)
    fun onFollowClick(artist: Artist)
}
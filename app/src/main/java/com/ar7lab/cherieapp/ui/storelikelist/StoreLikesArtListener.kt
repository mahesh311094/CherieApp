package com.ar7lab.cherieapp.ui.storelikelist

import com.ar7lab.cherieapp.model.Art

/**
 * Listener for Marketplace
 * */
interface StoreLikesArtListener {
    /**
     * Add [Art] data class to Market Art Listener
     * @return Art object
     * */
    fun onClick(art: Art)
    fun likeClick(art: Art)
    fun notifyClick(art: Art)
}
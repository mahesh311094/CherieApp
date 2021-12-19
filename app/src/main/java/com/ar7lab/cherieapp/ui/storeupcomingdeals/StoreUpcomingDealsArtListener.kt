package com.ar7lab.cherieapp.ui.storeupcomingdeals

import com.ar7lab.cherieapp.model.Art

/**
 * Listener for Marketplace
 * */
interface StoreUpcomingDealsArtListener {
    /**
     * Add [Art] data class to Market Art Listener
     * @return Art object
     * */
    fun onClick(art: Art)
    fun likeClick(art: Art)
    fun notifyClick(art: Art)
}
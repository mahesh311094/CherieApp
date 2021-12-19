package com.ar7lab.cherieapp.ui.storepastsale

import com.ar7lab.cherieapp.model.Art

/**
 * Listener for Marketplace
 * */
interface StorePastSaleArtListener {
    /**
     * Add [Art] data class to Market Art Listener
     * @return Art object
     * */
    fun onClick(art: Art)
    fun likeClick(art: Art)
}
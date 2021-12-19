package com.ar7lab.cherieapp.ui.market

import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.ArtTransactionDetails

/**
 * Listener for Marketplace
 * */
interface ArtTransactionListener {
    /**
     * Add [Art] data class to Market Art Listener
     * @return Art object
     * */
    fun onClick(art: Art, transactionDetail: ArtTransactionDetails)
}
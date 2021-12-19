package com.ar7lab.cherieapp.ui.nftartistprofile

import com.ar7lab.cherieapp.model.Artist

/**
 *Listener for OnSale tab, Owned tab, Follow Artist
 * */
interface NFTArtistProfileListener {
    /**
     * Add [Artist] data class to Artist follow
     * @return Art object
     * */
    fun onClickOnSale()
    fun onClickOwned()
    fun onFollowClick(artist: Artist)
    fun onLikeClicked(artist: Artist)
}
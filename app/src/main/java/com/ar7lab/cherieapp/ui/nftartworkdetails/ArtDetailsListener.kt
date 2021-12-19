package com.ar7lab.cherieapp.ui.nftartworkdetails

import com.ar7lab.cherieapp.model.Art

/*
* Listener for Comment
* */
interface ArtDetailsListener {
    //Click event for art comment click
    fun viewCommentsClicked()
    fun viewLikesClicked()
    fun onClickBuyNow(art: Art)
}
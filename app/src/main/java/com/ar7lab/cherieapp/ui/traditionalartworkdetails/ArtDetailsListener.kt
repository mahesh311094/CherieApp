package com.ar7lab.cherieapp.ui.traditionalartworkdetails

import com.ar7lab.cherieapp.model.Art

interface ArtDetailsListener {
    fun viewCommentsClicked()
    fun viewLikesClicked()
    fun onClickBuyNow(art: Art)
}
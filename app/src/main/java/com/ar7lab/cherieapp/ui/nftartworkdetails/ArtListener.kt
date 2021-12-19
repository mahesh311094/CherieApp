package com.ar7lab.cherieapp.ui.nftartworkdetails

import com.ar7lab.cherieapp.model.Art

/*
* */
interface ArtListener {
    //Click event for like art click
    fun likeClick(art: Art)
    //Click event for art comment
    fun viewCommentClick(art: Art)
}
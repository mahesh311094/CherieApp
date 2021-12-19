package com.ar7lab.cherieapp.ui.nftartworkdetails

import com.ar7lab.cherieapp.model.Artist

/*
* Listener for Artist profile click
* */
interface ArtDetailsListenerCreator {
    //Click event for Artist profile click
    fun artistProfileClick(artist: Artist)
}
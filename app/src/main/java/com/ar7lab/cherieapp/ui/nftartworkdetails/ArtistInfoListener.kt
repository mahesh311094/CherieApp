package com.ar7lab.cherieapp.ui.nftartworkdetails

import com.ar7lab.cherieapp.model.Artist

/*
* Listener for Follow Artist
* */
interface ArtistInfoListener {
    //Click event for follow artist click
    fun onFollowClick(artist: Artist)
}
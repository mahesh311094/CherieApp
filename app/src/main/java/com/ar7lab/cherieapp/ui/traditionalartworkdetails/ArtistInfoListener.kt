package com.ar7lab.cherieapp.ui.traditionalartworkdetails

import com.ar7lab.cherieapp.model.Artist

interface ArtistInfoListener {
    fun onFollowClick(artist: Artist)
    fun artistProfileClick(artist: Artist)
}
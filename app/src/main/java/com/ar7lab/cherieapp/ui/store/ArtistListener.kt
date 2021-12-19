package com.ar7lab.cherieapp.ui.store

import com.ar7lab.cherieapp.model.Artist

interface ArtistListener {
    fun onFollowClick(artist: Artist)
    fun onClick(artist: Artist)
}
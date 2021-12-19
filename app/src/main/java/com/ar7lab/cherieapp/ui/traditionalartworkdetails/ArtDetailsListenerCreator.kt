package com.ar7lab.cherieapp.ui.traditionalartworkdetails

import com.ar7lab.cherieapp.model.Artist

interface ArtDetailsListenerCreator {
    fun artistProfileClick(artist: Artist)
}
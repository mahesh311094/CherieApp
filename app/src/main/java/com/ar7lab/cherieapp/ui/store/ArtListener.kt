package com.ar7lab.cherieapp.ui.store

import com.ar7lab.cherieapp.model.Art
import com.ar7lab.cherieapp.model.Artist

interface ArtListener {
    fun onClick(art: Art)
    fun viewCommentClick(art: Art)
    fun likeClick(art: Art)
    fun artistProfileClick(artist: Artist)
    fun viewLikeClick(art: Art)
    fun onClickTrade()
    fun notifyClick(art: Art)
}
package com.ar7lab.cherieapp.ui.traditionalartworkdetails

import com.ar7lab.cherieapp.model.Art

interface ArtListener {
    fun likeClick(art: Art)
    fun viewCommentClick(art: Art)
    fun viewLikeClick(art: Art)
}
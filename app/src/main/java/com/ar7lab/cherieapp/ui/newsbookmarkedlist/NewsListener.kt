package com.ar7lab.cherieapp.ui.newsbookmarkedlist

import com.ar7lab.cherieapp.model.News

/**
 * Listener for item news to handle event click
 */
interface NewsListener {
    fun onNewsClicked(news: News)
}
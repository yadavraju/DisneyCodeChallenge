package com.raju.disney.ui.factory

import com.raju.disney.data.GData
import com.raju.disney.ui.adapter.GiphyAdapter
import javax.inject.Inject

class AppFactory @Inject constructor() {

  fun createGiphyAdapter(GData: GData, listener: (uri: String?) -> Unit): GiphyAdapter {
    return GiphyAdapter(GData, listener)
  }
}

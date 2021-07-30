package com.raju.disney.ui.factory

import com.raju.disney.data.ImageThumbUri
import com.raju.disney.ui.adapter.CharacterAdapter
import javax.inject.Inject

class AppFactory @Inject constructor() {

  fun createCharacterAdapter(character: ImageThumbUri): CharacterAdapter {
    return CharacterAdapter(character)
  }
}

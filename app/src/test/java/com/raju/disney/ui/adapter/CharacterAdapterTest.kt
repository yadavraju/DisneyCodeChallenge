package com.raju.disney.ui.adapter

import android.view.View
import com.raju.disney.data.ImageThumbUri
import com.raju.disney.ui.viewholder.CharacterViewHolder
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class CharacterAdapterTest {

  private lateinit var testObject: CharacterAdapter

  @Before
  fun setUp() {
    testObject = CharacterAdapter(ImageThumbUri())
  }

  @Test
  fun createItemViewHolder() {
    val view  = mock<View> {  }
    assertNotNull(testObject.createItemViewHolder(view))
  }

  @Test
  fun bindItemViewHolder() {
    val holder = mock<CharacterViewHolder> {  }
    testObject.bindItemViewHolder(holder)
    verify(holder).bind(any())
  }

  @Test
  fun getViewType() {
    assertEquals(ViewType.CHARACTER_IMAGE, testObject.viewType)
  }
}

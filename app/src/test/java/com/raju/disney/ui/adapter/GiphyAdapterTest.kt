package com.raju.disney.ui.adapter

import android.view.View
import com.raju.disney.data.GData
import com.raju.disney.ui.viewholder.GiphyViewHolder
import junit.framework.Assert.assertEquals
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class GiphyAdapterTest {

  private lateinit var testObject: GiphyAdapter

  @Before
  fun setUp() {
    fun itemClicked(s: String?) {
    }
    testObject = GiphyAdapter(GData(), ::itemClicked)
  }

  @Test
  fun createItemViewHolder() {
    val view  = mock<View> {  }
    Assert.assertNotNull(testObject.createItemViewHolder(view))
  }

  @Test
  fun bindItemViewHolder() {
    val holder = mock<GiphyViewHolder> {  }
    testObject.bindItemViewHolder(holder)

    verify(holder).bind(any(), any())
  }

  @Test
  fun getViewType() {
    assertEquals(ViewType.GIPHY_IMAGE, testObject.viewType)
  }
}

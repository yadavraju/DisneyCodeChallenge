package com.raju.disney.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpaceItemDecoration(
    private val left: Int,
    private val right: Int,
    private val top: Int,
    private val bottom: Int
) : RecyclerView.ItemDecoration() {

  override fun getItemOffsets(
      outRect: Rect,
      view: View,
      parent: RecyclerView,
      state: RecyclerView.State
  ) {
    outRect.left = left
    outRect.right = right
    outRect.top = top
    outRect.bottom = bottom
  }
}

package com.raju.disney.ui.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.raju.disney.api.databinding.bindSrcUrl
import com.raju.disney.data.GData
import kotlinx.android.synthetic.main.cell_image_layout.view.*

class GiphyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  fun bind(giphy: GData, listener: () -> Unit) {
    itemView.setOnLongClickListener {
      listener()
      true
    }
    itemView.imageView.bindSrcUrl(giphy.images.original.url)
  }
}

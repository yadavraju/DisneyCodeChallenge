package com.raju.disney.ui.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import bindSrcUrl
import com.raju.disney.data.ImageThumbUri
import kotlinx.android.synthetic.main.character_view_layout.view.*

class CharacterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
  fun bind(character: ImageThumbUri) {
    itemView.ivCharacter.bindSrcUrl(character.imageThumbUri)
  }
}

package com.raju.disney.ui.adapter

import android.view.View
import com.raju.disney.R
import com.raju.disney.data.ImageThumbUri
import com.raju.disney.ui.adapter.ViewType.CHARACTER_IMAGE
import com.raju.disney.ui.viewholder.CharacterViewHolder

class CharacterAdapter(private val character: ImageThumbUri)
    : BaseDataBoundAdapter<CharacterViewHolder>(R.layout.character_view_layout) {
    
    override fun createItemViewHolder(view: View): CharacterViewHolder {
        return CharacterViewHolder(view)
    }
    
    override fun bindItemViewHolder(holder: CharacterViewHolder) {
        holder.bind(character)
    }
    
    override val viewType: ViewType
        get() = CHARACTER_IMAGE
}
